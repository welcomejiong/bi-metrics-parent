package org.corps.bi.transport.http.inner.fetchdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
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
import org.corps.bi.tools.util.JSONUtils;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.corps.bi.transport.http.inner.MetricsInnerTransporterHttpProcesser;
import org.corps.bi.utils.KV;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1:发送成功后，保持所有成功发送的id，有可能这个小时的value值会非常的大，建议采用v3版本
 */
public class FetchDataThreadV2 extends  AbstractFetchDataThread{
	
	private static final Logger LOGGER=LoggerFactory.getLogger(FetchDataThreadV2.class);
	
	public FetchDataThreadV2(final MetricRocksdbColumnFamilys metricRocksdbColumnFamily,final MetricsTransporterConfig transporterConfig,final AtomicLong processedRecordNum) {
		super(metricRocksdbColumnFamily, transporterConfig, processedRecordNum);
	}
	/**
	 * 通过自增id的模式，批量获取，至少可以保证接收到的顺序和发出的顺序是一致的
	 */
	protected void pollMetrics(){
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
}