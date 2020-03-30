package org.corps.bi.dao.rocksdb;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.corps.bi.core.Constants;
import org.corps.bi.protobuf.BytesList;
import org.corps.bi.protobuf.IntEntity;
import org.corps.bi.protobuf.KVEntity;
import org.corps.bi.protobuf.LongEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksdbCleanedGlobalManagerV2 {
	
	public static Logger LOGGER =LoggerFactory.getLogger(RocksdbCleanedGlobalManagerV2.class.getName());
	
	private static final String PRE_CLEAN_ID_KEY="clean_id_key_v2";
	
	private static final String PRE_CLEAN_FLAG_KEY="clean_flag_key_v2";
	
	private static final String PRE_NEED_CLEAN_HOUR_KEY="need_clean_hour_key_v2";
	
	private static final long PROCESSED_FLAG_EXPIRE_MILLS=5*60*1000;
	
	private static final String DAY_OF_HOUR_FORMAT="yyyyMMddHH";
	
	private static final RocksdbCleanedGlobalManagerV2 INSTANCE=new RocksdbCleanedGlobalManagerV2();
	
	private final DefaultMetricRocksdbImpl defaultMetricRocksdbImpl;
	
	private  Map<String,AtomicBoolean> metricCleanedDataFlagMap;
	
	public RocksdbCleanedGlobalManagerV2() {
		super();
		this.defaultMetricRocksdbImpl=new DefaultMetricRocksdbImpl("default",RocksdbManager.getInstance().getRocksdb(), RocksdbManager.getInstance().getColumnFamilyHandle("default"));
		if(!Constants.ROCKSDB_DB_IS_READONLY) {
			this.metricCleanedDataFlagMap=this.initCleanedDataFlags();
		}
	}
	
	private byte[] getMetricCleanedIdKey(String metric) {
		return (PRE_CLEAN_ID_KEY+"_"+metric).getBytes();
	}
	
	private byte[] getMetricCleanedFlagKey(String metric) {
		return (PRE_CLEAN_FLAG_KEY+"_"+metric).getBytes();
	}
	
	/**
	 * 
	 * @param metric
	 * @param dayOfHourStr yyyyMMddHH eg:2019112121
	 * @return
	 */
	private byte[] getMetricCleanedHourKey(String metric,String dayOfHourStr) {
		return (PRE_NEED_CLEAN_HOUR_KEY+"_"+metric+"_"+dayOfHourStr).getBytes();
	}
	
	private byte[] getMetricCleanedCurrentHourKey(String metric) {
		String dayOfHourStr=DateFormatUtils.format(new Date(), DAY_OF_HOUR_FORMAT);
		return getMetricCleanedHourKey(metric, dayOfHourStr);
	}
	
	private Map<String,AtomicBoolean> initCleanedDataFlags() {
		Map<String,AtomicBoolean> ret=new ConcurrentHashMap<String,AtomicBoolean>();
		for (MetricRocksdbColumnFamilys metricRocksdbColumnFamily : MetricRocksdbColumnFamilys.values()) {
			String metric=metricRocksdbColumnFamily.getMetric();
			ret.put(metric, this.initCleanedDataFlag(metric));
		}
		return ret;
		
	}
	
	private AtomicBoolean initCleanedDataFlag(String metric) {
		byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricCleanedFlagKey(metric));
		if(existVal==null) {
			this.saveCleanedDataFlag(metric,false);
			return new AtomicBoolean(false);
		}
		KVEntity kvEntity=new KVEntity(existVal);
		
		IntEntity keyEnity=new IntEntity(kvEntity.getK());
		LongEntity valEntity=new LongEntity(kvEntity.getV());
		
		if(keyEnity.getValue()==1) {
			LOGGER.warn("metric:{} pre clean data thread is not noraml shutdown. key:{} value:{}",metric,keyEnity.getValue(),valEntity.getValue());
		}
		
		this.saveCleanedDataFlag(metric,false);
		
		return new AtomicBoolean(false);
	}
	
	
	public boolean tryLockCleaned(String metric) {
		boolean ret=this.metricCleanedDataFlagMap.get(metric).compareAndSet(false, true);
		if(ret) {
			this.saveCleanedDataFlag(metric,true);
		}else {
			byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricCleanedFlagKey(metric));
			if(existVal==null){
				return ret;
			}
			KVEntity kvEntity=new KVEntity(existVal);
			
			//IntEntity keyEnity=new IntEntity(kvEntity.getK());
			LongEntity valEntity=new LongEntity(kvEntity.getV());
			if((System.currentTimeMillis()-valEntity.getValue())>PROCESSED_FLAG_EXPIRE_MILLS) {
				this.metricCleanedDataFlagMap.get(metric).set(false);
				this.saveCleanedDataFlag(metric,false);
			}
		}
		return ret;
	}
	
	public boolean unLockCleaned(String metric) {
		boolean ret=this.metricCleanedDataFlagMap.get(metric).compareAndSet(true, false);
		if(ret) {
			this.saveCleanedDataFlag(metric,false);
		}
		return ret;
	}
	
	private void saveCleanedDataFlag(String metric,boolean flag) {
		
		IntEntity keyEnity=new IntEntity(flag?1:0);
		
		LongEntity valEntity=new LongEntity(System.currentTimeMillis());
		
		KVEntity kvEntity=new KVEntity(keyEnity.toByteArray(),valEntity.toByteArray());
		
		this.defaultMetricRocksdbImpl.save(this.getMetricCleanedFlagKey(metric), kvEntity.toByteArray());
	}
	
	public void saveCleanedId(String metric,long id) {
		LongEntity longEnity=new LongEntity(id);
		this.defaultMetricRocksdbImpl.save(this.getMetricCleanedIdKey(metric), longEnity.toByteArray());
	}

	public long getCleanedId(String metric) {
		byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricCleanedIdKey(metric));
		if(existVal==null) {
			return 0l;
		}
		LongEntity longEnity=new LongEntity(existVal);
		return longEnity.getValue();
	}
	
	public void addNeedCleanIds(String metric,Long beginKeyId,Long endKeyId) {
		try {
			BytesList bytesList=this.getCurrentHourNeedCleanIds(metric);
			if(bytesList==null) {
				bytesList=new BytesList();
			}
			LongEntity beginKeyEntity=new LongEntity(beginKeyId);
			LongEntity endKeyEntity=new LongEntity(endKeyId);
			KVEntity kvEntity=new KVEntity(beginKeyEntity.toByteArray(), endKeyEntity.toByteArray());
			bytesList.add(kvEntity.toByteArray());
			this.defaultMetricRocksdbImpl.save(this.getMetricCleanedCurrentHourKey(metric), bytesList.toByteArray());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	private BytesList getCurrentHourNeedCleanIds(String metric) {
		byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricCleanedCurrentHourKey(metric));
		if(existVal==null) {
			return null;
		}
		BytesList bytesList=new BytesList(existVal);
		return bytesList;
	}
	
	public BytesList getNeedCleanIds(String metric,String dayOfHour) {
		byte[] existVal=this.defaultMetricRocksdbImpl.get(this.getMetricCleanedHourKey(metric, dayOfHour));
		if(existVal==null) {
			return null;
		}
		BytesList bytesList=new BytesList(existVal);
		return bytesList;
	}
	
	public void delExpiredNeedCleanIds(String metric,String dayOfHour) {
		byte[] hourCleanIdsKey=this.getMetricCleanedHourKey(metric, dayOfHour);
		byte[] existVal=this.defaultMetricRocksdbImpl.get(hourCleanIdsKey);
		if(existVal==null) {
			return ;
		}
				
		BytesList bytesList=new BytesList(existVal);
		if(bytesList.isEmpty()) {
			return ;
		}
		
		
		for (byte[] bs : bytesList) {
			KVEntity kvEntity=new KVEntity(bs);
			LongEntity beginKeyEntity=new LongEntity(kvEntity.getK());
			LongEntity endKeyEntity=new LongEntity(kvEntity.getV());
			
			this.delExpiredNeedCleanIds(metric,beginKeyEntity.getValue(), endKeyEntity.getValue());
		}
		this.defaultMetricRocksdbImpl.del(hourCleanIdsKey);
	}
	
	private void delExpiredNeedCleanIds(String metric,Long beginKeyId,Long endKeyId) {
		
		MetricRocksdbColumnFamilys metricRocksdbColumnFamily=MetricRocksdbColumnFamilys.parseFromName(metric);
		if(metricRocksdbColumnFamily==null) {
			return;
		}

		for (long keyId = beginKeyId; keyId <= endKeyId; keyId++) {
			LongEntity keyEntity=new LongEntity(keyId);
			metricRocksdbColumnFamily.getMetricDao().del(keyEntity.toByteArray());
		}
		
	}

	public static RocksdbCleanedGlobalManagerV2 getInstance() {
		return INSTANCE;
	}

}
