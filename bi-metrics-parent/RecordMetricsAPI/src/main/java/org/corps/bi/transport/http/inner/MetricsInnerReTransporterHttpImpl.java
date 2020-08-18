package org.corps.bi.transport.http.inner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.corps.bi.dao.rocksdb.CatMetricsInRocksdbV2;
import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
import org.corps.bi.dao.rocksdb.RocksdbGlobalManager;
import org.corps.bi.dao.rocksdb.RocksdbManager;
import org.corps.bi.protobuf.BytesList;
import org.corps.bi.protobuf.LongEntity;
import org.corps.bi.recording.clients.rollfile.RollFileClient.SystemThreadFactory;
import org.corps.bi.recording.exception.TrackingException;
import org.corps.bi.transport.MetricsInnerReTransporter;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.corps.bi.utils.KV;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsInnerReTransporterHttpImpl implements MetricsInnerReTransporter {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(MetricsInnerReTransporterHttpImpl.class.getSimpleName());
	
	private AtomicBoolean isTransporting=new AtomicBoolean(false);
	
	private AtomicBoolean isReTransportEnd=new AtomicBoolean(false);
	
	private  MetricsTransporterConfig transporterConfig;
	
	private  TriggerThread triggerThread;
	
	private  String[] metrics;
	
	private  String[] days;
	
	public MetricsInnerReTransporterHttpImpl(String[] metrics,String[] days) {
		super();
		this.metrics=metrics;
		this.days=days;
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
		this.triggerThread=new TriggerThread(this.transporterConfig);
		
		triggerThread.run();
		
		
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
					new SystemThreadFactory("metric-inner-retransporter-executor"),
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
				
				for (String metric : metrics) {
					this.processMetric(MetricRocksdbColumnFamilys.parseFromName(metric));
				}
				
				this.threadPoolExecutor.shutdown();
				this.threadPoolExecutor.awaitTermination(6,TimeUnit.HOURS);
				
				transportEnd();
				
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
			try {
				for (String day : days) {
					this.pollMetricsV2(day);
				}
				
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}finally {
			}
		}
		
		/**
		 * 通过自增id的模式，批量获取，至少可以保证接收到的顺序和发出的顺序是一致的
		 */
		private void pollMetricsV2(String day){
			try {
				long begin=System.currentTimeMillis();
				RocksDB rockdb=RocksdbManager.getInstance().getRocksdb();
				Long processedId=RocksdbGlobalManager.getInstance().getProcessedId(this.metric);
				Long currentMetricId=RocksdbGlobalManager.getInstance().getCurrentId(this.metric);
				
				ColumnFamilyHandle metricColumnFamilyHandle=RocksdbManager.getInstance().getColumnFamilyHandle(this.metric);
				
				List<ColumnFamilyHandle> queryCfList=new ArrayList<ColumnFamilyHandle>();
				final List<byte[]> keys = new ArrayList<byte[]>();
				
				Long beginKeyId=CatMetricsInRocksdbV2.getInstance().getSendMetricDayBeginProcessedId(metric, day);
				if(beginKeyId==null||currentMetricId==null) {
					LOGGER.info("metric:{} day:{} beginKeyId:{} or currentMetricId {} is null",metric,day,beginKeyId,currentMetricId);
					return ;
				}
						
				Long endKeyId=currentMetricId;
				if(endKeyId<beginKeyId) {
					return ;
				}
				
				long keyIdx=0;
				for(long j=beginKeyId;j<=endKeyId;j++) {
					
					LongEntity keyEnity=new LongEntity(j);
					keys.add(keyEnity.toByteArray());
					queryCfList.add(metricColumnFamilyHandle);
					
					if(++keyIdx%this.batchSize==0) {
						Map<byte[], byte[]> values = rockdb.multiGet(queryCfList,keys);
						this.doReTransport(day,processedId,currentMetricId,values);
						queryCfList.clear();
						keys.clear();
					}
				}
				
				if(!keys.isEmpty()) {
					Map<byte[], byte[]> values = rockdb.multiGet(queryCfList,keys);
					this.doReTransport(day,processedId,currentMetricId,values);
					queryCfList.clear();
					keys.clear();
				}
				
				
				long end=System.currentTimeMillis();
				
				LOGGER.info("retransport finished! metric:{} day:{} beginKeyId:{} endKeyId:{} spendMills:{}",this.metric,day,beginKeyId,endKeyId,(end-begin));
				
			}  catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
		private boolean doReTransport(String day,Long processedId,Long currentMetricId,Map<byte[], byte[]> values) {
			
			try {
				long begin=System.currentTimeMillis();
				
				TreeMap<Long,KV<byte[], byte[]>> sortedKeyMap=new TreeMap<Long,KV<byte[], byte[]>>();
				for (Entry<byte[], byte[]> entry: values.entrySet()) {
					
					LongEntity keyEnity=new LongEntity(entry.getKey());
					
					sortedKeyMap.put(keyEnity.getValue(), new KV<byte[], byte[]>(entry.getKey(), entry.getValue()));
					
				}
				
				BytesList transportDataList=new BytesList();
				
				long currentMinProcessedId=Long.MAX_VALUE;
				long currentMaxProcessedId=0;
				int addTimes=0;
				for (Entry<Long,KV<byte[], byte[]>>  entry: sortedKeyMap.entrySet()) {
					
					if(currentMinProcessedId>entry.getKey()) {
						currentMinProcessedId=entry.getKey();
					}
					
					if(currentMaxProcessedId<entry.getKey()) {
						currentMaxProcessedId=entry.getKey();
					}
					
					transportDataList.add(entry.getValue().getV());
					
					addTimes++;
				}
				
				if(transportDataList.isEmpty()){
					
					return true;
				}
				
				MetricsInnerTransporterHttpProcesser metricsInnerTransporterHttpProcesser=new MetricsInnerTransporterHttpProcesser(this.metric,transportDataList,this.transporterConfig);
				
				boolean isSucc=false;
				for(int i=0;i<3;i++) {
					isSucc=metricsInnerTransporterHttpProcesser.doTransport();
					if(isSucc) {
						break;
					}
				}

				long end=System.currentTimeMillis();
				
				if(isSucc) {
					long tmpTriggerProcessedNum=this.processedRecordNum.addAndGet(addTimes);
					LOGGER.info("metric:{} isSucc:{} processedId:{} currentMetricId:{} currentMinProcessedId:{} currentMaxProcessedId:{} triggerProcessedNum:{} currentProcessSize:{} spendMills:({})",this.metric,isSucc,processedId,currentMetricId,currentMinProcessedId,currentMaxProcessedId,tmpTriggerProcessedNum,addTimes,(end-begin));
					
				} else {
					LOGGER.error("metric:{} isSucc:{} processedId:{} currentMetricId:{} currentMinProcessedId:{} currentMaxProcessedId:{} triggerProcessedNum:{} currentProcessSize:{} spendMills:({})",this.metric,isSucc,processedId,currentMetricId,currentMinProcessedId,currentMaxProcessedId,this.processedRecordNum.get(),addTimes,(end-begin));
				}
				
				return isSucc;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
			return false;
		}
		
	}
	
	private boolean transportEnd() {
		return this.isReTransportEnd.compareAndSet(false, true);
	}


	@Override
	public boolean reTransport() {
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
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			return false;
		}
	}

	@Override
	public boolean isTransporting() {
		return this.isTransporting.get();
	}

	@Override
	public boolean isTransportEnd() {
		return this.isReTransportEnd.get();
	}


	

}
