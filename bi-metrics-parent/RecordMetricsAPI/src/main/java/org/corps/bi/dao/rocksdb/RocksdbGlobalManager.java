package org.corps.bi.dao.rocksdb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.corps.bi.core.Constants;
import org.corps.bi.protobuf.IntEntity;
import org.corps.bi.protobuf.KVEntity;
import org.corps.bi.protobuf.LongEntity;
import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksdbGlobalManager {
	
	public static Logger LOGGER =LoggerFactory.getLogger(RocksdbGlobalManager.class);
	
	private static final String GLOBAL_ID_KEY="global_id_key";
	
	private static final String PROCESSED_ID_KEY="proed_id_key";
	
	private static final String PROCESSED_FLAG_KEY="proed_flag_key";
	
	private static final long PROCESSED_FLAG_EXPIRE_MILLS=5*60*1000;
	
	private static final RocksdbGlobalManager INSTANCE=new RocksdbGlobalManager();
	
	private final DefaultMetricRocksdbImpl defaultMetricRocksdbImpl;
	
	private final Map<String,AtomicLong> metricIdGeneratorMap;
	
	private final Map<String,Long> processedMetricIdGeneratorMap;
	
	private  Map<String,AtomicBoolean> metricProcessedDataFlagMap;
	
	public RocksdbGlobalManager() {
		super();
		RocksDB rocksDB=RocksdbManager.getInstance().getRocksdb();
		this.defaultMetricRocksdbImpl=new DefaultMetricRocksdbImpl("default",rocksDB, RocksdbManager.getInstance().getColumnFamilyHandle("default"));
		this.metricIdGeneratorMap=this.initMetricIdGenerators();
		this.processedMetricIdGeneratorMap=this.initProcessedMetricIdGenerators();
		if(!Constants.ROCKSDB_DB_IS_READONLY) {
			this.metricProcessedDataFlagMap=this.initProcessedDataFlags();
		}
	}
	
	private byte[] getMetricGlobalIdKey(String metric) {
		return (GLOBAL_ID_KEY+"_"+metric).getBytes();
	}
	
	private byte[] getMetricProcessedIdKey(String metric) {
		return (PROCESSED_ID_KEY+"_"+metric).getBytes();
	}
	
	private byte[] getMetricProcessedFlagKey(String metric) {
		return (PROCESSED_FLAG_KEY+"_"+metric).getBytes();
	}
	
	
	private Map<String,AtomicLong> initMetricIdGenerators() {
		Map<String,AtomicLong> ret=new ConcurrentHashMap<String,AtomicLong>();
		for (MetricRocksdbColumnFamilys metricRocksdbColumnFamily : MetricRocksdbColumnFamilys.values()) {
			String metric=metricRocksdbColumnFamily.getMetric();
			ret.put(metric, this.initMetricIdGenerator(metric));
		}
		return ret;
	}
	
	private AtomicLong initMetricIdGenerator(String metric) {
		byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricGlobalIdKey(metric));
		if(existVal==null) {
			return new AtomicLong(0);
		}
		LongEntity longEnity=new LongEntity(existVal);
		AtomicLong tmp=new AtomicLong(longEnity.getValue());
		return tmp;
	}
	
	private Map<String,Long> initProcessedMetricIdGenerators() {
		Map<String,Long> ret=new ConcurrentHashMap<String,Long>();
		for (MetricRocksdbColumnFamilys metricRocksdbColumnFamily : MetricRocksdbColumnFamilys.values()) {
			String metric=metricRocksdbColumnFamily.getMetric();
			ret.put(metric, this.initProcessedMetricIdGenerator(metric));
		}
		return ret;
	}
	
	private Long initProcessedMetricIdGenerator(String metric) {
		byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricProcessedIdKey(metric));
		if(existVal==null) {
			LOGGER.warn("init the metric:{} processedId to 0!",metric);
			return 0L;
		}
		LongEntity longEnity=new LongEntity(existVal);
		return longEnity.getValue();
	}
	
	private Map<String,AtomicBoolean> initProcessedDataFlags() {
		Map<String,AtomicBoolean> ret=new ConcurrentHashMap<String,AtomicBoolean>();
		for (MetricRocksdbColumnFamilys metricRocksdbColumnFamily : MetricRocksdbColumnFamilys.values()) {
			String metric=metricRocksdbColumnFamily.getMetric();
			ret.put(metric, this.initProcessedDataFlag(metric));
		}
		return ret;
		
	}
	
	
	private AtomicBoolean initProcessedDataFlag(String metric) {
		byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricProcessedFlagKey(metric));
		if(existVal==null) {
			this.saveProcessDataFlag(metric,false);
			return new AtomicBoolean(false);
		}
		KVEntity kvEntity=new KVEntity(existVal);
		
		IntEntity keyEnity=new IntEntity(kvEntity.getK());
		LongEntity valEntity=new LongEntity(kvEntity.getV());
		
		if(keyEnity.getValue()==1) {
			LOGGER.warn("metric:{} pre process data thread is not noraml shutdown. key:{} value:{}",metric,keyEnity.getValue(),valEntity.getValue());
		}
		
		this.saveProcessDataFlag(metric,false);
		
		return new AtomicBoolean(false);
	}
	
	
	public long getIdIncrAndGet(String metric) {
		Long currentId=this.metricIdGeneratorMap.get(metric).incrementAndGet();
		synchronized (currentId) {
			this.defaultMetricRocksdbImpl.save(this.getMetricGlobalIdKey(metric), new LongEntity(currentId).toByteArray());
		}
		return currentId;
	}
	
	public long getCurrentId(String metric) {
		return this.metricIdGeneratorMap.get(metric).get();
	}
	
	public boolean tryLockProcessed(String metric) {
		boolean ret=this.metricProcessedDataFlagMap.get(metric).compareAndSet(false, true);
		if(ret) {
			this.saveProcessDataFlag(metric,true);
		}else {
			byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricProcessedFlagKey(metric));
			if(existVal==null){
				throw new RuntimeException("tryLockProcessed for metric:"+metric+" of value is null!") ;
			}
			KVEntity kvEntity=new KVEntity(existVal);
			
			//IntEntity keyEnity=new IntEntity(kvEntity.getK());
			LongEntity valEntity=new LongEntity(kvEntity.getV());
			Long sysMills=System.currentTimeMillis();
			Long diffMiss=sysMills-valEntity.getValue();
			if((diffMiss)>PROCESSED_FLAG_EXPIRE_MILLS) {
				this.metricProcessedDataFlagMap.get(metric).set(false);
				this.saveProcessDataFlag(metric,false);
			}
			LOGGER.info("tryLockProcessed metric:{} res:{} value:{} sysMills:{} diffMiss:{}",metric,ret,valEntity.getValue(),sysMills,diffMiss);
		}
		return ret;
	}
	
	public boolean unLockProcessed(String metric) {
		boolean ret=this.metricProcessedDataFlagMap.get(metric).compareAndSet(true, false);
		if(ret) {
			this.saveProcessDataFlag(metric,false);
		}
		return ret;
	}
	
	private void saveProcessDataFlag(String metric,boolean flag) {
		
		IntEntity keyEnity=new IntEntity(flag?1:0);
		
		LongEntity valEntity=new LongEntity(System.currentTimeMillis());
		
		KVEntity kvEntity=new KVEntity(keyEnity.toByteArray(),valEntity.toByteArray());
		
		this.defaultMetricRocksdbImpl.save(this.getMetricProcessedFlagKey(metric), kvEntity.toByteArray());
	}
	
	public  synchronized void saveProcessedId(String metric,long id) {
		LongEntity longEnity=new LongEntity(id);
		this.defaultMetricRocksdbImpl.save(this.getMetricProcessedIdKey(metric), longEnity.toByteArray());
		if(this.processedMetricIdGeneratorMap.containsKey(metric)) {
			this.processedMetricIdGeneratorMap.put(metric, id);
		}
	}

	public long getProcessedId(String metric) {
		byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricProcessedIdKey(metric));
		if(existVal==null) {
			if(this.processedMetricIdGeneratorMap.containsKey(metric)) {
				Long hisProcessId=this.processedMetricIdGeneratorMap.get(metric);
				LOGGER.warn("the metric:{} getProcessedId of db is null but the cache is {}.so return the value of cache!",metric,hisProcessId);
				return hisProcessId;
			}
			//LOGGER.warn("the metric:{} getProcessedId of db is null.so inited to 0!",metric);
			return 0l;
		}
		LongEntity longEnity=new LongEntity(existVal);
		return longEnity.getValue();
	}
	

	public static RocksdbGlobalManager getInstance() {
		return INSTANCE;
	}

}
