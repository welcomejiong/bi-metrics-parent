package org.corps.bi.transport.http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.corps.bi.core.MetricRequestParams;
import org.corps.bi.recording.clients.rollfile.RollFileClient.SystemThreadFactory;
import org.corps.bi.recording.exception.TrackingException;
import org.corps.bi.transport.MetricsTransporter;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsTransporterHttpImpl implements MetricsTransporter {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(MetricsTransporterHttpImpl.class);
	
	private final BlockingQueue<MetricRequestParams> blockingQueue;
	
	private final MetricsTransporterConfig transporterConfig;
	
	private final ScheduledExecutorService transporterIntervalService;
	
	private final MetricsTransporterHttpProcesser metricsTransporterHttpProcesser;
	
	public MetricsTransporterHttpImpl() {
		super();
		
		this.blockingQueue=new LinkedBlockingDeque<MetricRequestParams>(2000000);
		
		this.transporterConfig=MetricsTransporterConfig.getInstance();
		
		int rollInterval = this.transporterConfig.getTransportInterval();
		if (rollInterval <= 0) {
			throw new TrackingException("RollInterval is error !");
		}
		transporterIntervalService = Executors.newScheduledThreadPool(1,new SystemThreadFactory("metric-transporter-scheduled"));
		
		this.metricsTransporterHttpProcesser=new MetricsTransporterHttpProcesser(this.transporterConfig);
		
		TransportThread transportThread=new TransportThread(this.blockingQueue,this.transporterConfig.getBatchSize());
		
		this.transporterIntervalService.scheduleAtFixedRate(transportThread,rollInterval, rollInterval, TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean transport(MetricRequestParams metricRequestParams) {
		return this.blockingQueue.add(metricRequestParams);
	}
	
	@Override
	public boolean transport(List<MetricRequestParams> metricRequestParams) {
		return this.blockingQueue.addAll(metricRequestParams);
	}
	
	
	private class TransportThread implements Runnable{
		
		private final BlockingQueue<MetricRequestParams> blockingQueue;
		
		private final int batchSize;
		
		private int pollTimes=0;
		
		private long processedRecordNum=0;

		public TransportThread(final BlockingQueue<MetricRequestParams> blockingQueue,final int batchSize) {
			super();
			this.blockingQueue=blockingQueue;
			this.batchSize = batchSize;
		}

		@Override
		public void run() {
			try {
				this.pollMetrics();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
		
		private void pollMetrics(){
			pollTimes++;
			long begin=System.currentTimeMillis();
			List<MetricRequestParams> tempList=new ArrayList<MetricRequestParams>();
			int succ=0;
			for (int i = 0; i < batchSize; i++) {
				try {
					MetricRequestParams imMetric=this.blockingQueue.poll();
					if(imMetric==null){
						break;
					}
					tempList.add(imMetric);
					succ++;
				} catch (Exception e) {
					LOGGER.error(e.getMessage(),e);
				}
			}
			if(tempList.isEmpty()){
				return ;
			}
			this.processedRecordNum+=tempList.size();
			if(this.pollTimes%100==0) {
				LOGGER.info("pollTimes:{} processedRecordNum:{} currentProcessSize:{}",this.pollTimes,this.processedRecordNum,tempList.size());
			}
			metricsTransporterHttpProcesser.doTransport(tempList);
			long end=System.currentTimeMillis();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(" transport end... succRecordsNum:{} spendMills:({})",succ,(end-begin));
			}
		}
		
	}


	

}
