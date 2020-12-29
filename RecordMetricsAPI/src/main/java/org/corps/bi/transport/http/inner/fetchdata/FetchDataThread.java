package org.corps.bi.transport.http.inner.fetchdata;

import java.util.concurrent.atomic.AtomicLong;

import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
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
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1:计划采用rockdb的itea模式，但是由于不保证顺序，故放到以后再研究
 */
public class FetchDataThread extends  AbstractFetchDataThread{
	
	private static final Logger LOGGER=LoggerFactory.getLogger(FetchDataThread.class);
	
	public FetchDataThread(final MetricRocksdbColumnFamilys metricRocksdbColumnFamily,final MetricsTransporterConfig transporterConfig,final AtomicLong processedRecordNum) {
		super(metricRocksdbColumnFamily, transporterConfig, processedRecordNum);
	}
	/**
	 * @TODO
	 * 通过itea的模式，不能保证发送出去的数据按照接收的先后顺序
	 * 后期仔细研究明白底层的存储机制之后，再做修改
	 */
	protected void pollMetrics(){
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
			
//			if(currentKeyId!=0 && currentKeyId > keyEnity.getValue()) {
//				LOGGER.error("metric:{} key:{} currentKeyId:{} needProcessKeyId:{} needProcessKeyId max larger currentKeyId",this.metric,keyEnity.getValue(),currentKeyId,keyEnity.getValue());
//				throw new RuntimeException("needProcessKeyId max larger currentKeyId");
//			}
			
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
