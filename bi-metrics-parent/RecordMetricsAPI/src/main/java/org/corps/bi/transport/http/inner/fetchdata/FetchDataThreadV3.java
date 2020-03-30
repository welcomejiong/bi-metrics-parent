package org.corps.bi.transport.http.inner.fetchdata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.tuple.MutablePair;
import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
import org.corps.bi.dao.rocksdb.RocksdbCleanedGlobalManagerV2;
import org.corps.bi.dao.rocksdb.RocksdbGlobalManager;
import org.corps.bi.dao.rocksdb.RocksdbManager;
import org.corps.bi.protobuf.BytesList;
import org.corps.bi.protobuf.LongEntity;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.corps.bi.transport.http.inner.MetricsInnerTransporterHttpProcesser;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1:发送成功后，保持的id，是id区间段
 */
public class FetchDataThreadV3 extends  AbstractFetchDataThread{
	
	private static final Logger LOGGER=LoggerFactory.getLogger(FetchDataThreadV3.class);
	
	public FetchDataThreadV3(final MetricRocksdbColumnFamilys metricRocksdbColumnFamily,final MetricsTransporterConfig transporterConfig,final MutablePair<AtomicLong, AtomicLong> processedRecordNumPair) {
		super(metricRocksdbColumnFamily, transporterConfig, processedRecordNumPair);
	}
	/**
	 * 通过自增id的模式，批量获取，至少可以保证接收到的顺序和发出的顺序是一致的
	 */
	protected void pollMetrics(){
		try {
			long fdt=this.fetchDataTimes.incrementAndGet();
			
			long begin=System.currentTimeMillis();
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
			
			List<byte[]> rowValues=rockdb.multiGetAsList(queryCfList,keys);
			
			int addTimes=0;
			
			for (int i = 0; i < keys.size(); i++) {
				//byte[] key= keys.get(i);
				byte[] value= rowValues.get(i);
				if(value==null) {
					continue;
				}
				transportDataList.add(value);
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
			
			long tmpTriggerProcessedNum=this.processedRecordNum.addAndGet(addTimes);
			
			if(isSucc) {
				RocksdbGlobalManager.getInstance().saveProcessedId(this.metric, endKeyId);
				RocksdbCleanedGlobalManagerV2.getInstance().addNeedCleanIds(this.metric,beginKeyId,endKeyId);
			} else {
				LOGGER.error("metric:{} isSucc:{} processedId:{} currentMetricId:{} beginKeyId:{}  endKeyId:{} triggerProcessedNum:{} currentProcessSize:{} spendMills:({})",this.metric,isSucc,processedId,currentMetricId,beginKeyId,endKeyId,this.processedRecordNum.get(),addTimes,(end-begin));
			}
			
			if(fdt%super.metricLoggerPerNum==0) {
				LOGGER.info("metric:{} fetchDataTimes:{} isSucc:{} processedId:{} currentMetricId:{} beginKeyId:{}  endKeyId:{} triggerProcessedNum:{} currentProcessSize:{} spendMills:({})",this.metric,fdt,isSucc,processedId,currentMetricId,beginKeyId,endKeyId,tmpTriggerProcessedNum,addTimes,(end-begin));
			}
			
			LOGGER.debug(" transport end... beginKeyId:{}  endKeyId:{} succRecordsNum:{} spendMills:({})",beginKeyId,endKeyId,(end-begin));
			
		}  catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
}