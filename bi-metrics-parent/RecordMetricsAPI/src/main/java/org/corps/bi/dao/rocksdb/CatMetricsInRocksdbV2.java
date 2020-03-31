package org.corps.bi.dao.rocksdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.corps.bi.core.Constants;
import org.corps.bi.metrics.Meta;
import org.corps.bi.metrics.converter.MetaConverter;
import org.corps.bi.protobuf.BytesList;
import org.corps.bi.protobuf.KVEntity;
import org.corps.bi.protobuf.LongEntity;
import org.corps.bi.tools.util.JSONUtils;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatMetricsInRocksdbV2 {
	
	public static Logger LOGGER =LoggerFactory.getLogger(CatMetricsInRocksdbV2.class.getName());
	
	private static CatMetricsInRocksdbV2 INSTANCE=null;
	
	private CatMetricsInRocksdbV2() {
		super();
	}
	
	public synchronized static CatMetricsInRocksdbV2 getInstance() {
		
		if(INSTANCE==null) {
			INSTANCE=new CatMetricsInRocksdbV2();
		}
		
		return INSTANCE;
	}

	public List<Map<String,Object>> catAll() {
		
		List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();
		for (MetricRocksdbColumnFamilys dataCenterTopic : MetricRocksdbColumnFamilys.values()) {
			Map<String,Object> tmpMess=this.cat(dataCenterTopic.getMetric());
			ret.add(tmpMess);
		}
		
		return ret;
		
	}
	
	public List<Map<String,Object>> catAll(String day) {
		
		List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();
		for (MetricRocksdbColumnFamilys dataCenterTopic : MetricRocksdbColumnFamilys.values()) {
			Map<String,Object> tmpMess=this.cat(dataCenterTopic.getMetric(),day);
			ret.add(tmpMess);
		}
		
		return ret;
		
	}
	
	/**
	 * 
	 * @param metric
	 * @param day format:yyyyMMdd  eg:20200310
	 * @return
	 */
	public Map<String,Object> cat(String metric,String day) {
		return this.getMetricMess(metric,day);
		
	}
	
	public Map<String,Object> cat(String metric) {
		String day=DateFormatUtils.format(new Date(), Constants.DATE_FORMAT_NUM);
		return this.getMetricMess(metric,day);
	}
	
	private Map<String,Object> getMetricMess(String metric,String day) {
		Map<String,Object> ret=new 	HashMap<String,Object>();
		ret.put("metric", metric);
		ret.put("currentMess", this.getMetricSendMess(metric,day));
		ret.put("needCleanMess", this.getMetricNeedCleanMess(metric,day));
		return ret;
	}

	private Map<String,Object> getMetricSendMess(String metric,String day) {
		Map<String,Object> ret=new 	HashMap<String,Object>();
		try {
			RocksDB rockdb=RocksdbManager.getInstance().getRocksdb();
			//RocksIterator rocksIterator=rockdb.newIterator(RocksdbManager.getInstance().getColumnFamilyHandle(metric));
			long currentMetricId=RocksdbGlobalManager.getInstance().getCurrentId(metric);
			long processedMetricId=RocksdbGlobalManager.getInstance().getProcessedId(metric);
			
			Long beginProcessedId=this.getSendMetricDayBeginProcessedId(metric, day);
			Long endProcessedId=this.getSendMetricDayEndProcessedId(metric, day);
			if(beginProcessedId==null) {
				ret.put("msg", "the metric:{"+metric+"} day:{"+day+"} is null.");
				return ret;
			}
			if(endProcessedId==null) {
				endProcessedId=beginProcessedId;
			}
			
			
			Map<String,Integer> gameMetricNumMap=new TreeMap<String,Integer>();
			
			ColumnFamilyHandle metricColumnFamilyHandle=RocksdbManager.getInstance().getColumnFamilyHandle(metric);
			
			final List<ColumnFamilyHandle> queryCfList=new ArrayList<ColumnFamilyHandle>();
			final List<byte[]> keys = new ArrayList<byte[]>();
			
			long idx=0;
			
			for (Long i = beginProcessedId; i <=endProcessedId; i++) {
				queryCfList.add(metricColumnFamilyHandle);
				keys.add(new LongEntity(i).toByteArray());
				if(idx++%100==0) {
					Map<String,Integer> metricNumTmpMap=this.parseMetricMess(queryCfList, keys);
					this.mergeMetricMess(gameMetricNumMap, metricNumTmpMap);
					queryCfList.clear();
					keys.clear();
				}
			}
			// 把最后的重新合并
			Map<String,Integer> metricNumTmpMap=this.parseMetricMess(queryCfList, keys);
			this.mergeMetricMess(gameMetricNumMap, metricNumTmpMap);
			queryCfList.clear();
			keys.clear();
			
			ret.put("currentMetricId", currentMetricId);
			ret.put("processedMetricId", processedMetricId);
			ret.put("minId", beginProcessedId);
			ret.put("maxId", endProcessedId);
			ret.put("gameMetricNumMap", gameMetricNumMap);
			return ret;
		} catch (Exception e) {
			ret.put("error", e.getMessage());
			LOGGER.error(e.getMessage(),e);
		}
		return ret;
	}
	
	private void mergeMetricMess(Map<String,Integer> masterGameMetricNumMap,Map<String,Integer> clusterGameMetricNumMap) {
		if(clusterGameMetricNumMap==null||clusterGameMetricNumMap.isEmpty()) {
			return;
		}
		for (Entry<String,Integer> entry : clusterGameMetricNumMap.entrySet()) {
			if(masterGameMetricNumMap.containsKey(entry.getKey())) {
				masterGameMetricNumMap.put(entry.getKey(), masterGameMetricNumMap.get(entry.getKey())+entry.getValue());
			}else {
				masterGameMetricNumMap.put(entry.getKey(),entry.getValue());
			}
		}
	}
	
	
	private Map<String,Integer> parseMetricMess(List<ColumnFamilyHandle> queryCfList,List<byte[]> keys) throws Exception, RocksDBException{
		Map<String,Integer> gameMetricNumMap=new HashMap<String,Integer>();
		RocksDB rockdb=RocksdbManager.getInstance().getRocksdb();
		List<byte[]> values=rockdb.multiGetAsList(queryCfList,keys);
		for (int i=0;i<keys.size();i++) {
			//byte[] key=keys.get(i);
			byte[] val=values.get(i);
			if(val==null) {
				continue;
			}
			KVEntity kvEntity=new KVEntity(val);
			MetaConverter metaConverter=new MetaConverter(kvEntity.getK());
			Meta meta=metaConverter.getEntity();
			String metaKey=meta.getMetric()+"_"+meta.getSnId()+"_"+meta.getGameId()+"_"+meta.getDs();
			Integer metricNum=new Integer(0);
			if(gameMetricNumMap.containsKey(metaKey)) {
				metricNum=gameMetricNumMap.get(metaKey);
			}
			metricNum++;
			gameMetricNumMap.put(metaKey, metricNum);
		}
		return gameMetricNumMap;
	}
	
	/**
	 * 
	 * @param metric
	 * @param day
	 * @return
	 * @throws Exception 
	 */
	public Long getSendMetricDayBeginProcessedId(String metric,String day) throws Exception {
		
		Date dayDate=DateUtils.parseDate(day, Constants.DATE_FORMAT_NUM);
		// 1:首先找前一天最大的id
		Long preDayMaxId=this.getSendMetricDayMaxId(metric, DateFormatUtils.format(DateUtils.addDays(dayDate,-1), Constants.DATE_FORMAT_NUM));
		// 2:如果前一天的没有找到，则找当天的最小id
		if(preDayMaxId!=null) {
			return preDayMaxId;
		}
		Long dayMinId=this.getSendMetricDayMinId(metric, day);
		if(dayMinId!=null) {
			return dayMinId;
		}
		return null;
	}
	
	public Long getSendMetricDayEndProcessedId(String metric,String day) throws Exception {
		
		Date dayDate=DateUtils.parseDate(day, Constants.DATE_FORMAT_NUM);
		// 1:首先找后一天最小的id
		Long afterDayMinId=this.getSendMetricDayMinId(metric, DateFormatUtils.format(DateUtils.addDays(dayDate,1), Constants.DATE_FORMAT_NUM));
		// 2:如果前一天的没有找到，则找当天的最小id
		if(afterDayMinId!=null) {
			return afterDayMinId;
		}
		Long dayMaxId=this.getSendMetricDayMaxId(metric, day);
		if(dayMaxId!=null) {
			return dayMaxId;
		}
		return null;
	}
	
	public Long getSendMetricDayMinId(String metric,String day) throws Exception {
		// 1:首先当天最后一个小时的id,如果没有，一次找之前小时的
		for (int i = 0; i <=23 ; i++) {
			String hour=i+"";
			if(i<10) {
				hour="0"+i;
			}
			String hourOfDay=day+hour;
			BytesList bylist=RocksdbCleanedGlobalManagerV2.getInstance().getNeedCleanIds(metric, hourOfDay);
			if(bylist==null||bylist.size()<1) {
				continue;
			}
			List<Long> metricSendKeyList=new ArrayList<Long>();
			for (byte[] bs : bylist) {
				KVEntity kvEntity=new KVEntity(bs);
				LongEntity beginKeyEntity=new LongEntity(kvEntity.getK());
				LongEntity endKeyEntity=new LongEntity(kvEntity.getV());
				metricSendKeyList.add(beginKeyEntity.getValue());
				metricSendKeyList.add(endKeyEntity.getValue());
			}
			Long minId=Collections.min(metricSendKeyList);
			return minId;
		}
		
		return null;
	}
	
	public Long getSendMetricDayMaxId(String metric,String day) throws Exception {
		// 1:首先当天最后一个小时的id,如果没有，一次找之前小时的
		for (int i = 23; i >=0 ; i--) {
			String hour=i+"";
			if(i<10) {
				hour="0"+i;
			}
			String hourOfDay=day+hour;
			BytesList bylist=RocksdbCleanedGlobalManagerV2.getInstance().getNeedCleanIds(metric, hourOfDay);
			if(bylist==null||bylist.size()<1) {
				continue;
			}
			List<Long> metricSendKeyList=new ArrayList<Long>();
			for (byte[] bs : bylist) {
				KVEntity kvEntity=new KVEntity(bs);
				LongEntity beginKeyEntity=new LongEntity(kvEntity.getK());
				LongEntity endKeyEntity=new LongEntity(kvEntity.getV());
				metricSendKeyList.add(beginKeyEntity.getValue());
				metricSendKeyList.add(endKeyEntity.getValue());
			}
			Long maxId=Collections.max(metricSendKeyList);
			return maxId;
		}
		
		return null;
	}
	
	public Map<String,Object> getMetricNeedCleanMess(String metric,String day) {
		Map<String,Object> ret=new 	HashMap<String,Object>();
		int metricAlreadySendNum=this.getMetricNeedCleanDayMess(metric,day);
		ret.put(day, metricAlreadySendNum);
		return ret;
	}
	
	
	private int getMetricNeedCleanDayMess(String metric,String day) {
		try {
			int cleanIdNum=0;
			for (int i = 0; i < 24; i++) {
				String hour=i+"";
				if(i<10) {
					hour="0"+i;
				}
				String hourOfDay=day+hour;
				BytesList bylist=RocksdbCleanedGlobalManagerV2.getInstance().getNeedCleanIds(metric, hourOfDay);
				if(bylist==null) {
					continue;
				}
				for (byte[] bs : bylist) {
					KVEntity kvEntity=new KVEntity(bs);
					LongEntity beginKeyEntity=new LongEntity(kvEntity.getK());
					LongEntity endKeyEntity=new LongEntity(kvEntity.getV());
					cleanIdNum+=(endKeyEntity.getValue()-beginKeyEntity.getValue()+1);
				}
			}
			
			return cleanIdNum;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		return 0;
	}
	
	

}
