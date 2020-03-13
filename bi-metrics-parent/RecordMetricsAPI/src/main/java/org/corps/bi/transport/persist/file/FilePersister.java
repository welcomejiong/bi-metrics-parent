package org.corps.bi.transport.persist.file;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.corps.bi.core.MetricRequestParams;
import org.corps.bi.recording.clients.rollfile.RollFileClient.SystemThreadFactory;
import org.corps.bi.tools.util.JSONUtils;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.corps.bi.transport.persist.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePersister  implements Persister{
	
	private static final Logger LOGGER=LoggerFactory.getLogger(FilePersister.class);
	
	private ScheduledExecutorService scheduledExecutorService;
	
	private final long createTimeMills;
	
	private final AtomicInteger fileIndex;
	
	private final BlockingQueue<MetricRequestParams> blockingQueue;
	
	private final MetricsTransporterConfig metricsTransporterConfig;
	
	private FileOperator currentFileOperator;
	
	private FileOperator nextFileOperator;
	
	private final String dataSubFold;

	public FilePersister(String dataSubFold) {
		super();
		this.dataSubFold=dataSubFold;
		this.scheduledExecutorService = Executors.newScheduledThreadPool(1,new SystemThreadFactory("bi-file-persister"));
		this.createTimeMills=System.currentTimeMillis();
		this.fileIndex=new AtomicInteger(0);
		this.blockingQueue = new LinkedBlockingDeque<MetricRequestParams>(1000000);
		this.metricsTransporterConfig=MetricsTransporterConfig.getInstance();
		
		this.initFileOperator();
				
		this.initService();
	}
	
	private  void initFileOperator(){
		this.currentFileOperator=new FileOperator(this.dataSubFold,this.createTimeMills+"-"+this.fileIndex.incrementAndGet());
		this.nextFileOperator=new FileOperator(this.dataSubFold,this.createTimeMills+"-"+this.fileIndex.incrementAndGet());
	}
	
	private void initService(){
		
		RollFilePersistentThread rollFilePersistentThread=new RollFilePersistentThread(this.metricsTransporterConfig.getBatchSize());
		this.scheduledExecutorService.scheduleAtFixedRate(rollFilePersistentThread, 200, 200, TimeUnit.MILLISECONDS);
		
	}
	
	private synchronized void checkFileOperator(){
		if(!this.currentFileOperator.isNeedRotate()){
			return ;
		}
		this.currentFileOperator.rotate();
		this.destoryCurrentFileOperator();
		this.exchangeCurrentFileOperator();
	}
	
	private void destoryCurrentFileOperator(){
		this.currentFileOperator.flush();
		this.currentFileOperator.destory();
	}
	
	private synchronized void exchangeCurrentFileOperator(){
		this.currentFileOperator=this.nextFileOperator;
		this.nextFileOperator=new FileOperator(this.dataSubFold,this.createTimeMills+"-"+this.fileIndex.incrementAndGet());
	}

	@Override
	public boolean persist(List<MetricRequestParams> metricRequestParamsList) {
		return this.blockingQueue.addAll(metricRequestParamsList);
	}

	@Override
	public boolean persist(MetricRequestParams metricRequestParams) {
		return this.blockingQueue.add(metricRequestParams);
	}
	
	private class RollFilePersistentThread implements Runnable{
		
		private final int batchSize;
		
		public RollFilePersistentThread(int batchSize) {
			super();
			this.batchSize = batchSize;
		}

		@Override
		public void run() {
			try {
				
				checkFileOperator();
				
				for (int i = 0; i < batchSize; i++) {
					try {
						MetricRequestParams metricRequestParams=blockingQueue.poll();
						if(metricRequestParams==null){
							break ;
						}
						currentFileOperator.append(JSONUtils.toJSON(metricRequestParams));
					} catch (Exception e) {
						LOGGER.error(e.getMessage(),e);
					}
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	

}
