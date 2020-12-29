package org.corps.bi.transport.retry;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.corps.bi.core.Constants;
import org.corps.bi.core.MetricRequestParams;
import org.corps.bi.recording.clients.rollfile.RollFileClient.SystemThreadFactory;
import org.corps.bi.tools.util.JSONUtils;
import org.corps.bi.transport.MetricsTransporter;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsRetryer {
	
	private final static Logger LOGGER=LoggerFactory.getLogger("metricsRetryer");
	
	private final ScheduledExecutorService retryScheduledExecutorService;
	
	private boolean isStarted=false;
	
	private final MetricsTransporter metricsTransporter;
	
	private final MetricsTransporterConfig transporterConfig;
	
	public MetricsRetryer(MetricsTransporter metricsTransporter) {
		super();
		this.metricsTransporter=metricsTransporter;
		this.transporterConfig=MetricsTransporterConfig.getInstance();
		this.retryScheduledExecutorService = Executors.newScheduledThreadPool(1,new SystemThreadFactory("metric-retryer-scheduled"));
		this.init();
	}
	
	private void init(){
		
		this.isStarted=true;
		
		this.retryScheduledExecutorService.scheduleAtFixedRate(new ScheduledRetryFileManager(), 2,120, TimeUnit.SECONDS);
	}
	
	public void start(){
		if(this.isStarted){
			return ;
		}
		this.init();
	}
	
	private class ScheduledRetryFileManager implements Runnable{

		@Override
		public void run() {
			try {
				
				File retryDir = new File(transporterConfig.getDataDirFile()+File.separator+"first");
				
				LOGGER.info(" scanning retryDir {}  begin...",retryDir.getPath());
				
				SearchDataRetryFileDirTask searchDataRetryFileDirTask=new SearchDataRetryFileDirTask(retryDir);
				
				List<File> fileList=searchDataRetryFileDirTask.call();
				
				if(fileList==null||fileList.isEmpty()) {
					LOGGER.info(" scanned retryDir {}  is null ",retryDir.getPath());
					return ;
				}
				LOGGER.info(" scanned retryDir {}  have {} files ",retryDir.getPath(),fileList.size());
				
				List<File> prepareFileList=fileList.subList(0, fileList.size()>transporterConfig.getMaxRetryFileNum()?transporterConfig.getMaxRetryFileNum():fileList.size());
				
				ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(
						1,		//指的是保留的线程池大小
						this.calMaxThreadNum(prepareFileList.size()), 	//最大线程池， 指的是线程池的最大大小
						100, 	//指的是空闲线程结束的超时时间
						TimeUnit.SECONDS, 	//表示 keepAliveTime 的单位
						new LinkedBlockingQueue<Runnable>(100000),
						new SystemThreadFactory("bi-retryer-manager"),
						new ThreadPoolExecutor.DiscardPolicy() //直接放弃当前任务
				);
				
				for (File file : prepareFileList) {
					MetricRetryFileProcesserTask metricRetryFileProcesserTask=new MetricRetryFileProcesserTask(file);
					threadPoolExecutor.submit(metricRetryFileProcesserTask);
				}
				
				threadPoolExecutor.shutdown();
				
				// 防止大量数据堆积
				threadPoolExecutor.awaitTermination(12, TimeUnit.HOURS);
				
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		private int calMaxThreadNum(int fileNum) {
			
			if(fileNum<=1) {
				return 1;
			}
			
			if(fileNum<5) {
				return fileNum;
			}
			return 5;
		}
		
	}
	
	private class MetricRetryFileProcesserTask implements Runnable{
		
		private final File retryFile;

		public MetricRetryFileProcesserTask(File retryFile) {
			super();
			this.retryFile = retryFile;
		}
		
		@Override
		public void run() {
			
			try {
				this.retry();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			
		}
		
		private void retry(){
			long begin=System.currentTimeMillis();
			LOGGER.info("retryFile:{} begin...", this.retryFile.getPath());
			LineIterator lineIterator=null;
			try {
				lineIterator=FileUtils.lineIterator(this.retryFile, Constants.DEFAULT_CHARSET);
				while (lineIterator.hasNext()) {
					String line = lineIterator.nextLine();
					this.retryMetryc(line);
				} 
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}finally {
				if(lineIterator!=null){
					try {
						lineIterator.close();
					} catch (IOException e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
			this.renameCurrentFileToCompleted();
			
			LOGGER.info("retryFile:{} end... spendMills:{}", this.retryFile.getPath(),(System.currentTimeMillis()-begin));
		}
		
		private void retryMetryc(String metricJsonData){
			try {
				MetricRequestParams metricRequestParams=JSONUtils.fromJSON(metricJsonData, MetricRequestParams.class);
				metricsTransporter.transport(metricRequestParams);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		private void renameCurrentFileToCompleted(){
			if(this.retryFile==null){
				return ;
			}
			String currPath=this.retryFile.getPath();
			String fileNameSubfix=currPath.substring(0, currPath.lastIndexOf("."));
			File dest = new File(fileNameSubfix + ".COMPLETED");
			boolean renamed = this.retryFile.renameTo(dest);
		    if (renamed) {
		    	LOGGER.info("Successfully rolled file {} to {}",this.retryFile.getPath(),dest.getPath());
		    }
		}

		
	}


	private class SearchDataRetryFileDirTask implements  Callable<List<File>>{
		
		private final File retryDir;
		
		private final List<File> matchedFiles;
		
		public SearchDataRetryFileDirTask(File retryDir) {
			super();
			this.retryDir = retryDir;
			this.matchedFiles=new ArrayList<File>();
		}
		
		@Override
		public List<File> call() throws Exception {
			this.doSearch();
			return this.matchedFiles;
		}

		private void doSearch(){
			try {
				FileFilter filter = new FileFilter() {
					public boolean accept(File candidate) {
						String fileName = candidate.getName();
						if (fileName.endsWith("rowdata")) {
							return true;
						}
						return false;
					}
				};
				File[] bufferFile=retryDir.listFiles(filter);
				if(bufferFile==null){
					return ;
				}
				List<File> tmpFileList=this.orderFile(bufferFile);
				this.matchedFiles.addAll(tmpFileList);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		private List<File> orderFile(File[] candidateFiles){
			List<File> retFiles = Arrays.asList(candidateFiles);
			Collections.sort(retFiles, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					long compare = o1.lastModified() - o2.lastModified();
					if (compare == 0) {
						long cl = o1.length() - o2.length();
						if (cl > 0) {
							return 1;
						} else if (cl < 0) {
							return -1;
						}
					} else if (compare > 0) {
						return 1;
					} else {
						return -1;
					}
					return 0;
				}
			});
			return retFiles;
		}
		
	}


}
