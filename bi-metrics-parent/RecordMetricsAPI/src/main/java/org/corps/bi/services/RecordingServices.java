package org.corps.bi.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.corps.bi.cleaner.MetricsRecordsCleaner;
import org.corps.bi.cleaner.rocksdb.MetricsRecordsCleanerRocksdbImpl;
import org.corps.bi.core.Constants;
import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
import org.corps.bi.dao.rocksdb.RocksdbGlobalManager;
import org.corps.bi.dao.rocksdb.RocksdbManager;
import org.corps.bi.datacenter.core.DataCenterTopics;
import org.corps.bi.metrics.IMetric;
import org.corps.bi.metrics.converter.MetricEntityConverterManager;
import org.corps.bi.protobuf.KVEntity;
import org.corps.bi.protobuf.LongEntity;
import org.corps.bi.tools.util.JSONUtils;
import org.corps.bi.tools.util.NetUtils;
import org.corps.bi.transport.MetricsInnerTransporter;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.corps.bi.utils.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RecordingServices {
	
	public static Logger LOGGER =LoggerFactory.getLogger(RecordingServices.class);
	
	private static RecordingServices RECRIDING_SERVIES;
	
	//private final RecordingService recordingService;
	
	// 之后设计
	//private static final MonitorMetricsServices MONITOR_METRICS_SERVICES=new MonitorMetricsServices();
	
//	private final MetricsTransporterConfig transporterConfig;
	
//	private final MetricsTransporter metricsTransporter;
//	
//	private final MetricsRetryer metricsRetryer;
	
	private final MetricsInnerTransporter metricsInnerTransporter;
	
	private final MetricsRecordsCleaner metricsRecordsCleaner;
	
	private AtomicBoolean isShutdowned=new AtomicBoolean(false);
	
	private boolean isTransporting=false;
	
	private boolean isCleaning=false;
	
	private String localMachineIp="";

	private RecordingServices() {
		//this.recordingService=new RollFileRecordingServiceImpl();
//		this.transporterConfig=MetricsTransporterConfig.getInstance();
		
//		this.metricsTransporter=new MetricsTransporterHttpImpl();
//		
//		this.metricsRetryer=new MetricsRetryer(this.metricsTransporter);
//		
//		this.metricsRetryer.start();
		
		this.metricsInnerTransporter=this.initMetricsInnerTransporter();
		
		this.metricsRecordsCleaner=new MetricsRecordsCleanerRocksdbImpl();
		
		this.isTransporting=this.metricsInnerTransporter.transport();
		
		this.isCleaning=this.metricsRecordsCleaner.clean();
		
		this.localMachineIp=NetUtils.getLocalMachineIp();
		
	};
	
	private MetricsInnerTransporter initMetricsInnerTransporter() {
		MetricsTransporterConfig metricsTransporterConfig=MetricsTransporterConfig.getInstance();
		try {
			return (MetricsInnerTransporter)Class.forName(metricsTransporterConfig.getTransporterImplClass()).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("init metrics inner transporter used interface impl:"+metricsTransporterConfig.getTransporterImplClass()+ " by config meet error",e);
		}
	}
	
	public static final synchronized RecordingServices getInstance(){
		if(RECRIDING_SERVIES==null){
			RECRIDING_SERVIES=new RecordingServices();
		}
		return RECRIDING_SERVIES;
	}

	/**
	 * 报送数据，日期默认系统当前日期
	 * @param bean metric类
	 */
	public  void add(String gameId,String ds,IMetric metric) {
		try {
			
			KV<byte[], byte[]> combineKV=this.combinePersistDatas(gameId, ds, metric);
			
			MetricRocksdbColumnFamilys metricRocksdbColumnFamily=MetricRocksdbColumnFamilys.parseFromName(metric.metric());
			
			metricRocksdbColumnFamily.getMetricDao().save(combineKV.getK(), combineKV.getV());
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public void add(String gameId,String ds,List<IMetric> metricList){
		if(metricList==null){
			return ;
		}
		try {
			Map<String,Map<byte[],byte[]>> batchDatas=new HashMap<String,Map<byte[],byte[]>>();
			for (IMetric metric : metricList) {
				Map<byte[],byte[]> tmpMap=null;
				if(batchDatas.containsKey(metric.metric())) {
					tmpMap=batchDatas.get(metric.metric());
				}else {
					tmpMap=new HashMap<byte[],byte[]>();
					batchDatas.put(metric.metric(), tmpMap);
				}
				KV<byte[], byte[]> combineKV=this.combinePersistDatas(gameId, ds, metric);
				tmpMap.put(combineKV.getK(), combineKV.getV());
			}
			for (Entry<String,Map<byte[],byte[]>> entry : batchDatas.entrySet()) {
				MetricRocksdbColumnFamilys metricRocksdbColumnFamily=MetricRocksdbColumnFamilys.parseFromName(entry.getKey());
				if(metricRocksdbColumnFamily==null) {
					continue;
				}
				metricRocksdbColumnFamily.getMetricDao().saveBatch(entry.getValue());
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	
	private KV<byte[], byte[]> combinePersistDatas(String gameId,String ds,IMetric metric) {
		MetricEntityConverterManager metricEntityConvert=MetricEntityConverterManager.parseFromName(metric.metric());
		if(metricEntityConvert==null) {
			LOGGER.warn("MetricEntityConverterManager metric:"+metric.metric()+" is not match converter.");
			return null;
		}
		
		MetricRocksdbColumnFamilys metricRocksdbColumnFamily=MetricRocksdbColumnFamilys.parseFromName(metric.metric());
		if(metricRocksdbColumnFamily==null) {
			LOGGER.warn("MetricRocksdbColumnFamilys metric:"+metric.metric()+" is not match rocksdb column family.");
			return null;
		}
		
		long rockId=RocksdbGlobalManager.getInstance().getIdIncrAndGet(metric.metric());
		
		byte[] metricBody=metricEntityConvert.toProtobufBytes(metric);
		
		MetaExtraBuilder metaExtraBuilder=new MetaExtraBuilder();
		metaExtraBuilder.addExtra("ip", this.localMachineIp);
		metaExtraBuilder.addExtra("rid", rockId);
		
		byte[] metricKey=MetricEntityConverterManager.keyProtobufBytes(metric.metric(), "1", gameId, ds,metaExtraBuilder.getExtra());
		
		KVEntity kvEntity=new KVEntity(metricKey, metricBody);
		
		byte[] rockV=kvEntity.toByteArray();
		
		
		
		byte[] rockK=new LongEntity(rockId).toByteArray();
		
		if(rockId%1000==0) {
			LOGGER.info("the metric:{} have already  persisted to rocksdb which the leastest id is :{}",metric.metric(),rockId);
		}
		
		return new KV<byte[], byte[]>(rockK, rockV);
	}
	
	public synchronized boolean shutdown() {
		if(!this.isShutdowned.compareAndSet(false, true)) {
			return false;
		}
		RocksdbManager.getInstance().close();
		this.metricsInnerTransporter.shutdown();
		this.metricsRecordsCleaner.shutdown();
		return true;
	}
	
	
	
	public AtomicBoolean getIsShutdowned() {
		return isShutdowned;
	}

	public boolean isTransporting() {
		return isTransporting;
	}

	public boolean isCleaning() {
		return isCleaning;
	}

	public String getLocalMachineIp() {
		return localMachineIp;
	}



	public static class MetaExtraBuilder {
		
		private Map<String,Object> extraMap=new HashMap<String,Object>();
		
		public void addExtra(String key,Object value) {
			this.extraMap.put(key, value);
		}
		
		public String getExtra() {
			return JSONUtils.toJSON(this.extraMap);
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		Constants.init("recording-template.properties");
		/*
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		String gameId="1";
		String clientId="2";
		String ds="20181218";
		Date now=new Date();
		for (int i = 0; i < 120; i++) {
			Dau dau=new Dau();
			dau.setClientId(clientId);
			dau.setAffiliate("affiliate_"+i);
			dau.setCreative("creative_"+i);
			dau.setDauDate(DateFormatUtils.format(DateUtils.addDays(now, i),"yyyy-MM-dd"));
			dau.setDauTime("10:52:32");
			dau.setFamily("famility_"+i);
			dau.setFromUid("f_u_id-"+i);
			dau.setGenus("genus_"+i);
			batchMetrics.add(dau);
			//System.out.println(JSONUtils.toJSON(dau));
			System.out.println(JSONUtils.toJSONwithOutNullProp(dau));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		*/
		RecordingServices recordingServices=RecordingServices.getInstance();
		/*
		System.out.println("==========================s");
		int i=0;
		RocksDB rockdb=RocksdbManager.getInstance().getRocksdb();
		final ReadOptions readOptions=new ReadOptions();
		for (DataCenterTopics dataCenterTopic : DataCenterTopics.values()) {
			RocksIterator rocksIterator=rockdb.newIterator(RocksdbManager.getInstance().getColumnFamilyHandle(dataCenterTopic.getMetric()));
			for (rocksIterator.seekToFirst();rocksIterator.isValid();rocksIterator.next()) {
				try {
					rocksIterator.status();
					
					LongEntity keyEnity=new LongEntity(rocksIterator.key());
					
					KVEntity kvEntity=new KVEntity(rocksIterator.value());
					
					MetaConverter metaConverter=new MetaConverter(kvEntity.getK());
					
					IMetric metric=MetricEntityConverterManager.parseFromName(metaConverter.getEntity().getMetric()).parseMetricEntityFromBytes(kvEntity.getV());
					
					System.out.println("idx:"+(++i)+":"+keyEnity.getValue()+":"+JSONUtils.toJSON(metaConverter.getEntity())+":"+JSONUtils.toJSON(metric));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			rocksIterator.close();
		}
		
		*/
		MetricRocksdbColumnFamilys metricRocksdbColumnFamily=MetricRocksdbColumnFamilys.parseFromName(DataCenterTopics.DAU.getMetric());
		
		System.out.println("==========================s");
		
		/*
		List<ColumnFamilyHandle> queryCfList=new ArrayList<ColumnFamilyHandle>();
		final List<byte[]> keys = new ArrayList<>();
		long processedId=RocksdbGlobalManager.getInstance().getProcessedId(DataCenterTopics.DAU.getMetric());
		for(long j=processedId+1;j<=processedId+100;j++) {
			LongEntity keyEnity=new LongEntity(j);
			keys.add(keyEnity.toByteArray());
			queryCfList.add(RocksdbManager.getInstance().getColumnFamilyHandle(DataCenterTopics.DAU.getMetric()));
		}
		
		
		Map<byte[], byte[]> values = rockdb.multiGet(queryCfList,keys);
        for (Entry<byte[], byte[]> entry: values.entrySet()) {
        	LongEntity keyEnity=new LongEntity(entry.getKey());
			
			KVEntity kvEntity=new KVEntity(entry.getValue());
			
			DauConverter dauConverter=new DauConverter(kvEntity.getV());
			
			MetaConverter metaConverter=new MetaConverter(kvEntity.getK());
			
			System.out.println(":"+keyEnity.getValue()+":"+JSONUtils.toJSON(metaConverter.getEntity())+":"+JSONUtils.toJSON(dauConverter.getEntity()));
        }
		
		rockdb.close();
		*/
		Thread.sleep(10*60*1000L);
	}
	
//	/**
//	 * 报送数据
//	 * @param bean metric类
//	 * @param date 数据日期，格式（yyyyMMdd）,格式错误默认当天日期 覆盖IMetric实例中的日期字段
//	 */
//	public  void add(IMetric metric,String date) {
//		persistMessage(metric, date);
//	}
	
	/*

	private  void persistMessage(IMetric metric) {
		
		String category=metric.metric() + "_" + metric.getGameId() + "_" + metric.getDs();
		String sourceCategory=metric.metric();
		byte[] body=metric.persistentBody();
		persistentToFile(category, sourceCategory, body);
		
		
	}
	
	private void persistMessage(IMetric metric, String date) {
		String category=metric.metric() + "_" + metric.getGameId() + "_" + DateUtils.verifyDate(date);
		String sourceCategory=metric.metric();
		byte[] body=metric.persistentBody();
		persistentToFile(category, sourceCategory, body);
	}
	*/
	
//	private  void persistentToFile(final String category,final String sourceCategory,final byte[] body){
//		// 此处的category即使event（带snid和gameid）
//		boolean isSucc=this.recordingService.send(sourceCategory, category, body);
//		
//		/*
//		if(isMonitor(category, sourceCategory)){
//			if(isSucc){
//				MONITOR_METRICS_SERVICES.succ(category);
//			}else{
//				MONITOR_METRICS_SERVICES.fail(category);
//			}
//		}
//		*/
//	}
//	private static boolean isMonitor(String category, String sourceCategory){
//		if("metricrecordmess".equalsIgnoreCase(sourceCategory)){
//			return false;
//		}
//		return true;
//	}
}
