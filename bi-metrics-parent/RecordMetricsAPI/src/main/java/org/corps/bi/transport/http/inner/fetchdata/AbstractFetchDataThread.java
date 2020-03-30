package org.corps.bi.transport.http.inner.fetchdata;

import java.util.concurrent.atomic.AtomicLong;

import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
import org.corps.bi.dao.rocksdb.RocksdbGlobalManager;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFetchDataThread implements Runnable{
	
	private static final Logger LOGGER=LoggerFactory.getLogger(AbstractFetchDataThread.class);
	
	protected final MetricRocksdbColumnFamilys metricRocksdbColumnFamily;
	
	protected final MetricsTransporterConfig transporterConfig;
	
	protected final String metric;
	
	protected final int batchSize;
	
	protected final AtomicLong processedRecordNum;

	public AbstractFetchDataThread(final MetricRocksdbColumnFamilys metricRocksdbColumnFamily,final MetricsTransporterConfig transporterConfig,final AtomicLong processedRecordNum) {
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
			this.pollMetrics();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}finally {
			if(isLock) {
				RocksdbGlobalManager.getInstance().unLockProcessed(this.metric);
			}
		}
	}
	
	protected abstract void pollMetrics();
	
}
