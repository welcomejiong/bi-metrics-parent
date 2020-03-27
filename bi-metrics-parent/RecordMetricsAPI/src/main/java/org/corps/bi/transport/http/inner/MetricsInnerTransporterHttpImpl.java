package org.corps.bi.transport.http.inner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
import org.corps.bi.dao.rocksdb.RocksdbCleanedGlobalManager;
import org.corps.bi.dao.rocksdb.RocksdbGlobalManager;
import org.corps.bi.dao.rocksdb.RocksdbManager;
import org.corps.bi.metrics.IMetric;
import org.corps.bi.metrics.converter.MetaConverter;
import org.corps.bi.metrics.converter.MetricEntityConverterManager;
import org.corps.bi.protobuf.BytesList;
import org.corps.bi.protobuf.KVEntity;
import org.corps.bi.protobuf.LongEntity;
import org.corps.bi.recording.clients.rollfile.RollFileClient.SystemThreadFactory;
import org.corps.bi.recording.exception.TrackingException;
import org.corps.bi.tools.util.JSONUtils;
import org.corps.bi.transport.MetricsInnerTransporter;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.corps.bi.utils.KV;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsInnerTransporterHttpImpl implements MetricsInnerTransporter {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(MetricsInnerTransporterHttpImpl.class.getSimpleName());
	
	private AtomicBoolean isTransporting=new AtomicBoolean(false);
	
	private  MetricsTransporterConfig transporterConfig;
	
	private  ScheduledExecutorService transporterIntervalService;
	
	private  TriggerThread triggerThread;
	
	public MetricsInnerTransporterHttpImpl() {
		super();
		this.transporterConfig=MetricsTransporterConfig.getInstance();
	}
	
	private synchronized boolean start() {
		if(!this.isTransporting.compareAndSet(false, true)) {
			LOGGER.warn("is transporting now!");
			return false;
		}
		int rollInterval = this.transporterConfig.getTransportInterval();
		if (rollInterval <= 0) {
			throw new TrackingException("RollInterval is error !");
		}
		transporterIntervalService = Executors.newScheduledThreadPool(1,new SystemThreadFactory("metric-inner-transporter-scheduled"));
		
		this.triggerThread=new TriggerThread(this.transporterConfig);
		
		this.transporterIntervalService.scheduleAtFixedRate(triggerThread,rollInterval, rollInterval, TimeUnit.MILLISECONDS);
		
		return true;
	}
	
	private class TriggerThread implements Runnable{
		
		private final ThreadPoolExecutor threadPoolExecutor;
		
		private final MetricsTransporterConfig transporterConfig;
		
		private final Map<String,AtomicLong> metricProcessedRecordNumMap=new ConcurrentHashMap<String, AtomicLong>();
		
		public TriggerThread(MetricsTransporterConfig transporterConfig) {
			super();
			this.transporterConfig=transporterConfig;
			this.threadPoolExecutor = new ThreadPoolExecutor(
					this.transporterConfig.getThreadCoreSize()>1?(this.transporterConfig.getThreadCoreSize()-1):1,		//指的是保留的线程池大小
					this.transporterConfig.getMaxThreadSize(), 	//最大线程池， 指的是线程池的最大大小
					100, 	//指的是空闲线程结束的超时时间
					TimeUnit.SECONDS, 	//表示 keepAliveTime 的单位
					new LinkedBlockingQueue<Runnable>(100000),
					new SystemThreadFactory("metric-inner-transporter-executor"),
					new ThreadPoolExecutor.DiscardPolicy() //直接放弃当前任务
			);
		}

		@Override
		public void run() {
			try {
				
				if(!this.transporterConfig.isTransportOn()) {
					LOGGER.warn("the config properties is_tranpsort_on was updated by hand through the controller or other.");
					return ;
				}
				
				for (MetricRocksdbColumnFamilys metricRocksdbColumnFamily : MetricRocksdbColumnFamilys.values()) {
					this.processMetric(metricRocksdbColumnFamily);
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
		
		private void processMetric(MetricRocksdbColumnFamilys metricRocksdbColumnFamily) {
			
			String metric=metricRocksdbColumnFamily.getMetric();
			
			AtomicLong processedRecordNum=this.getMetricProcessedRecordNum(metric);
			
			this.threadPoolExecutor.submit(new FetchDataThread(metricRocksdbColumnFamily,this.transporterConfig,processedRecordNum));
		}
		
		private AtomicLong getMetricProcessedRecordNum(String metric) {
			if(!this.metricProcessedRecordNumMap.containsKey(metric)) {
				AtomicLong tmp=new AtomicLong(0);
				this.metricProcessedRecordNumMap.put(metric, tmp);
			}
			
			return this.metricProcessedRecordNumMap.get(metric);
		}
		
		public boolean shutdown() {
			try {
				this.threadPoolExecutor.shutdown();
				return true;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
				return false;
			}
			
		}
		
	}

	
	private class FetchDataThread implements Runnable{
		
		private final MetricRocksdbColumnFamilys metricRocksdbColumnFamily;
		
		private final MetricsTransporterConfig transporterConfig;
		
		private final String metric;
		
		private final int batchSize;
		
		private final AtomicLong processedRecordNum;

		public FetchDataThread(final MetricRocksdbColumnFamilys metricRocksdbColumnFamily,final MetricsTransporterConfig transporterConfig,final AtomicLong processedRecordNum) {
			super();
			this.metricRocksdbColumnFamily=metricRocksdbColumnFamily;
			this.transporterConfig=transporterConfig;
			this.processedRecordNum=processedRecordNum;
			this.metric=this.metricRocksdbColumnFamily.getMetric();
			this.batchSize = this.transporterConfig.getBatchSize();
		}

		@Override
		public void run() {
			boolean isLock=false;
			try {
				isLock=RocksdbGlobalManager.getInstance().tryLockProcessed(this.metric);
				if(!isLock) {
					//LOGGER.info("metric:{} fetch data try lock is fail!",this.metric);
					return ;
				}
				this.pollMetricsV2();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}finally {
				if(isLock) {
					RocksdbGlobalManager.getInstance().unLockProcessed(this.metric);
				}
			}
		}
		
		/**
		 * 通过自增id的模式，批量获取，至少可以保证接收到的顺序和发出的顺序是一致的
		 */
		private void pollMetricsV2(){
			try {
				long begin=System.currentTimeMillis();
				int succ=0;
				RocksDB rockdb=RocksdbManager.getInstance().getRocksdb();
				long processedId=RocksdbGlobalManager.getInstance().getProcessedId(this.metric);
				long currentMetricId=RocksdbGlobalManager.getInstance().getCurrentId(this.metric);
				
				ColumnFamilyHandle metricColumnFamilyHandle=RocksdbManager.getInstance().getColumnFamilyHandle(this.metric);
				List<ColumnFamilyHandle> queryCfList=new ArrayList<ColumnFamilyHandle>();
				final List<byte[]> keys = new ArrayList<byte[]>();
				long beginKeyId=processedId+1;
				long endKeyId=processedId+this.batchSize;
				if(endKeyId>currentMetricId) {
					endKeyId=currentMetricId;
				}
				if(endKeyId<beginKeyId) {
					return ;
				}
				for(long j=beginKeyId;j<=endKeyId;j++) {
					LongEntity keyEnity=new LongEntity(j);
					keys.add(keyEnity.toByteArray());
					queryCfList.add(metricColumnFamilyHandle);
				}
				
				BytesList transportDataList=new BytesList();
				
				TreeMap<Long,KV<byte[], byte[]>> sortedKeyMap=new TreeMap<Long,KV<byte[], byte[]>>();
				Map<byte[], byte[]> values = rockdb.multiGet(queryCfList,keys);
				for (Entry<byte[], byte[]> entry: values.entrySet()) {
					
					LongEntity keyEnity=new LongEntity(entry.getKey());
					
					sortedKeyMap.put(keyEnity.getValue(), new KV<byte[], byte[]>(entry.getKey(), entry.getValue()));
					
					if(LOGGER.isDebugEnabled()) {
						
						KVEntity kvEntity=new KVEntity(entry.getValue());
						
						MetricEntityConverterManager metricEntityConverterManager=MetricEntityConverterManager.parseFromName(this.metric);
						
						IMetric imetric=metricEntityConverterManager.parseMetricEntityFromBytes(kvEntity.getV());
						
						MetaConverter metaConverter=new MetaConverter(kvEntity.getK());
						
						LOGGER.debug("metric:{} key:{} metricMeta:{} metricData:{}",this.metric,keyEnity.getValue(),JSONUtils.toJSON(metaConverter.getEntity()),JSONUtils.toJSON(imetric));
					}
				}
				
				long currentMaxProcessedId=0;
				int addTimes=0;
				
				for (Entry<Long,KV<byte[], byte[]>>  entry: sortedKeyMap.entrySet()) {
					if(currentMaxProcessedId<entry.getKey()) {
						currentMaxProcessedId=entry.getKey();
					}else {
						LOGGER.warn("metric:{} key:{} currentKeyId:{} needProcessKeyId:{} needProcessKeyId larger currentKeyId",this.metric,entry.getKey(),currentMaxProcessedId,entry.getKey());
					}
					transportDataList.add(entry.getValue().getV());
					addTimes++;
				}
				
				if(transportDataList.isEmpty()){
					// 如果中间的key都没有值，直接把最后一个id设置成处理的id
					RocksdbGlobalManager.getInstance().saveProcessedId(this.metric, endKeyId);
					return ;
				}
				
				MetricsInnerTransporterHttpProcesser metricsInnerTransporterHttpProcesser=new MetricsInnerTransporterHttpProcesser(this.metric,transportDataList,this.transporterConfig);
				
				boolean isSucc=metricsInnerTransporterHttpProcesser.doTransport();
				
				long end=System.currentTimeMillis();
				
				if(isSucc) {
					RocksdbGlobalManager.getInstance().saveProcessedId(this.metric, currentMaxProcessedId);
					RocksdbCleanedGlobalManager.getInstance().addNeedCleanIds(this.metric,sortedKeyMap.keySet());
					
					long tmpTriggerProcessedNum=this.processedRecordNum.addAndGet(addTimes);
					LOGGER.info("metric:{} isSucc:{} processedId:{} currentMetricId:{} beginKeyId:{}  endKeyId:{} currentMaxProcessedId:{} triggerProcessedNum:{} currentProcessSize:{} spendMills:({})",this.metric,isSucc,processedId,currentMetricId,beginKeyId,endKeyId,currentMaxProcessedId,tmpTriggerProcessedNum,addTimes,(end-begin));
					
				} else {
					LOGGER.error("metric:{} isSucc:{} processedId:{} currentMetricId:{} beginKeyId:{}  endKeyId:{} currentMaxProcessedId:{} triggerProcessedNum:{} currentProcessSize:{} spendMills:({})",this.metric,isSucc,processedId,currentMetricId,beginKeyId,endKeyId,currentMaxProcessedId,this.processedRecordNum.get(),addTimes,(end-begin));
				}
				
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug(" transport end... lastKeyId:{} succRecordsNum:{} spendMills:({})",currentMaxProcessedId,succ,(end-begin));
				}
			}  catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
		
		
		/**
		 * @TODO
		 * 通过itea的模式，不能保证发送出去的数据按照接收的先后顺序
		 * 后期仔细研究明白底层的存储机制之后，再做修改
		 */
		private void pollMetrics(){
			long begin=System.currentTimeMillis();
			int succ=0;
			RocksDB rockdb=RocksdbManager.getInstance().getRocksdb();
			RocksIterator rocksIterator=rockdb.newIterator(RocksdbManager.getInstance().getColumnFamilyHandle(this.metric));
			long processedId=RocksdbGlobalManager.getInstance().getProcessedId(this.metric);
			byte[] seekKey=new LongEntity(processedId).toByteArray();
			BytesList transportDataList=new BytesList();
			long currentKeyId=0;
			int addTimes=0;
			for (rocksIterator.seek(seekKey);addTimes<=this.batchSize && rocksIterator.isValid();rocksIterator.next()) {
				
				//rocksIterator.status();
				
				LongEntity keyEnity=new LongEntity(rocksIterator.key());
				
//				if(currentKeyId!=0 && currentKeyId > keyEnity.getValue()) {
//					LOGGER.error("metric:{} key:{} currentKeyId:{} needProcessKeyId:{} needProcessKeyId max larger currentKeyId",this.metric,keyEnity.getValue(),currentKeyId,keyEnity.getValue());
//					throw new RuntimeException("needProcessKeyId max larger currentKeyId");
//				}
				
				currentKeyId=keyEnity.getValue();
				
				if(currentKeyId==processedId) {
					continue;
				}
				
				transportDataList.add(rocksIterator.value());
				
				addTimes++;
				
				if(LOGGER.isDebugEnabled()) {
					
					KVEntity kvEntity=new KVEntity(rocksIterator.value());
					
					MetricEntityConverterManager metricEntityConverterManager=MetricEntityConverterManager.parseFromName(this.metric);
					
					IMetric imetric=metricEntityConverterManager.parseMetricEntityFromBytes(kvEntity.getV());
					
					MetaConverter metaConverter=new MetaConverter(kvEntity.getK());
					
					LOGGER.debug("metric:{} key:{} metricMeta:{} metricData:{}",this.metric,keyEnity.getValue(),JSONUtils.toJSON(metaConverter.getEntity()),JSONUtils.toJSON(imetric));
				}
				
			}
			rocksIterator.close();
			
			if(transportDataList.isEmpty()){
				return ;
			}
			//this.processedRecordNum+=transportDataList.size();
			LOGGER.info("metric:{} lastKeyId:{} processedRecordNum:{} currentProcessSize:{}",this.metric,currentKeyId,this.processedRecordNum,transportDataList.size());
			
			MetricsInnerTransporterHttpProcesser metricsInnerTransporterHttpProcesser=new MetricsInnerTransporterHttpProcesser(this.metric,transportDataList,this.transporterConfig);
			
			boolean isSucc=metricsInnerTransporterHttpProcesser.doTransport();
			
			if(isSucc) {
				RocksdbGlobalManager.getInstance().saveProcessedId(this.metric, currentKeyId);
			}
			
			long end=System.currentTimeMillis();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(" transport end... lastKeyId:{} succRecordsNum:{} spendMills:({})",currentKeyId,succ,(end-begin));
			}
		}
		
	}


	@Override
	public boolean transport() {
		if(!this.transporterConfig.isTransportOn()) {
			LOGGER.info("transport the trigger of tracking on is off.please config the transprot.on to true");
			return false;
		}
		return this.start();
	}

	@Override
	public boolean shutdown() {
		try {
			if(!this.isTransporting.compareAndSet(true, false)) {
				this.isTransporting.set(false);
				LOGGER.warn("there is the other thread set transporting to true.");
				return false;
			}
			this.transporterIntervalService.shutdown();
			this.triggerThread.shutdown();
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			return false;
		}
	}


	

}
