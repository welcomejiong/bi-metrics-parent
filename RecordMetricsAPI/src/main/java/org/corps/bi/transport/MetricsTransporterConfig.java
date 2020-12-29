package org.corps.bi.transport;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.corps.bi.core.Constants;
import org.corps.bi.recording.exception.TrackingException;
import org.corps.bi.utils.MD5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsTransporterConfig {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(MetricsTransporterConfig.class);
	
	private static final String DEFAULT_DATA_DIR="/tmp";
	
	private static final int DEFAULT_ROLL_INTERVAL=300000;
	
	private final boolean trackingOn;
	
	private final AtomicBoolean transportOn;
	
	private final String transporterImplClass;
	
	private final String dataCenterServerUrl;
	
	private final File dataDirFile;
	
	private final int transportInterval;
	
	private final int batchSize;
	
	private final int threadCoreSize;
	
	private final int maxThreadSize;
	
	private final String instanceMd5;
	
	private final boolean appendNewline;
	
	private final byte[] lineTerminateBy;
	
	private final int batchSendServerSize;
	
	private final int rotateInterval;
	
	private final int maxDataFileSize;
	
	private final int maxRetryFileNum;
	
	private final int cleanInterval;
	
	private final int cleanThreadCoreSize;
	
	private final int cleanMaxThreadSize;
	
	private final int cleanBatchSize;
	
	private final int cleanPreDay;
	
	private static MetricsTransporterConfig CONFIG;
	
	public static MetricsTransporterConfig getInstance(){
		if(CONFIG==null){
			CONFIG=new MetricsTransporterConfig();
		}
		return CONFIG;
	}
	

	private MetricsTransporterConfig() {
		super();
		Properties configs=Constants.getConfigs();
		
		this.trackingOn = Boolean.parseBoolean(configs.getProperty("tracking_on", "false"));
		
		this.transportOn = new AtomicBoolean(Boolean.parseBoolean(configs.getProperty("transport.on", "false")));
		
		this.transporterImplClass=this.checkValue(configs.getProperty("transporter.impl.class"), "org.corps.bi.transport.http.inner.MetricsInnerTransporterHttpImpl");
		
		this.dataCenterServerUrl=this.checkValue(configs.getProperty("transporter.dataCenterServerUrl"), "http://127.0.0.1/collectMetrics/");
		
		String dataDir=this.checkValue(configs.getProperty("transporter.dataDir"), DEFAULT_DATA_DIR);
		this.dataDirFile=new File(dataDir);
		
		String rollIntervalStr=this.checkValue(configs.getProperty("transporter.transportInterval"), DEFAULT_ROLL_INTERVAL+"");
		this.transportInterval=Integer.parseInt(rollIntervalStr);
		
		String batchSizeStr=this.checkValue(configs.getProperty("transporter.batchSize"), "100");
		this.batchSize=Integer.parseInt(batchSizeStr);
		
		String threadCoreSizeStr=this.checkValue(configs.getProperty("transporter.threadCoreSize"), "1");
		this.threadCoreSize=Integer.parseInt(threadCoreSizeStr);
		
		String maxThreadSizeStr=this.checkValue(configs.getProperty("transporter.maxThreadSize"), "3");
		this.maxThreadSize=Integer.parseInt(maxThreadSizeStr);
		
		String appendNewlineStr=this.checkValue(configs.getProperty("transporter.appendNewline"), "true");
		this.appendNewline=Boolean.parseBoolean(appendNewlineStr);
		
		String lineTerminateByStr=this.checkValue(configs.getProperty("transporter.lineTerminateBy"), "\n");
		this.lineTerminateBy=lineTerminateByStr.getBytes(Charset.forName("UTF-8"));
		
		
		String instanceClassPath=null;
		if(this.getClass().getClassLoader()==null||this.getClass().getClassLoader().getResource("./")==null) {
			instanceClassPath=dataDir+File.pathSeparator+getCurrentJVMProgressId();
		}else {
			instanceClassPath=this.getClass().getClassLoader().getResource("./").getPath();
		}
		
		this.instanceMd5=MD5Utils.MD5(instanceClassPath);
		
		String batchSendServerSizeStr=this.checkValue(configs.getProperty("transporter.batchSendServerSize"), "10");
		this.batchSendServerSize=Integer.parseInt(batchSendServerSizeStr);
		
		String rotateIntervalStr=this.checkValue(configs.getProperty("transporter.rotateInterval"), "120000");
		this.rotateInterval=Integer.parseInt(rotateIntervalStr);
		
		String maxDataFileSizeStr=this.checkValue(configs.getProperty("transporter.maxDataFileSize"), "120000");
		this.maxDataFileSize=Integer.parseInt(maxDataFileSizeStr);
		
		String maxRetryFileNumStr=this.checkValue(configs.getProperty("transporter.maxRetryFileNum"), "1");
		this.maxRetryFileNum=Integer.parseInt(maxRetryFileNumStr);
		
		String cleanIntervalStr=this.checkValue(configs.getProperty("cleaner.cleanInterval"), DEFAULT_ROLL_INTERVAL+"");
		this.cleanInterval=Integer.parseInt(cleanIntervalStr);
		
		String cleanThreadCoreSizeStr=this.checkValue(configs.getProperty("clean.cleanThreadCoreSize"), "2");
		this.cleanThreadCoreSize=Integer.parseInt(cleanThreadCoreSizeStr);
		
		String cleanMaxThreadSizeStr=this.checkValue(configs.getProperty("clean.cleanMaxThreadSize"), "3");
		this.cleanMaxThreadSize=Integer.parseInt(cleanMaxThreadSizeStr);
		
		String cleanBatchSizeStr=this.checkValue(configs.getProperty("clean.cleanBatchSize"), "100");
		this.cleanBatchSize=Integer.parseInt(cleanBatchSizeStr);
		
		String cleanPreDayStr=this.checkValue(configs.getProperty("clean.cleanPreDay"), "7");
		this.cleanPreDay=Integer.parseInt(cleanPreDayStr);
		
		
		LOGGER.info("current instance classpath is :{} for md5:{}",instanceClassPath,this.instanceMd5);
		
		this.checkPermissions();
		
		this.repairDataFile();
		
	}
	
	public static int getCurrentJVMProgressId() {
		try {  
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();  
            Field jvm = runtime.getClass().getDeclaredField("jvm");  
            jvm.setAccessible(true);  
            Object mgmt = jvm.get(runtime);  
            Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");  
            pidMethod.setAccessible(true);  
            int pid = (Integer) pidMethod.invoke(mgmt);  
            return pid;  
        } catch (Exception e) {  
            return -1;  
        }  
	}
	
	/**
	 * 检查相关权限（目录读写权限等）
	 */
	private void checkPermissions(){
		this.checkDataDirPermissions();
	}
	/**
	 * 检测数据目录是否存在，如果不存在则创建
	 * 
	 */
	private void checkDataDirPermissions(){
	    try {
	    	
	    	if(!this.dataDirFile.exists()){
	    		this.dataDirFile.mkdirs();
	    	}
	    	
			File canary = File.createTempFile("rollfile-datadir-perm-check-", ".canary", dataDirFile);
			
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(canary,true));
			outputStream.write("testing roll file permissions\n".getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();
			
			BufferedReader reader=new BufferedReader(new FileReader(canary));
			String tempString = null;
            int line = 0;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                System.out.println("line " + line + ": " + tempString);
                line++;
            }
            reader.close();
			
			if(line<1){
				throw new IOException("Empty canary file  " + canary);
			}
			if (!canary.delete()) {
			  throw new IOException("Unable to delete canary file " + canary);
			}
			LOGGER.debug("Successfully created and deleted canary file:{} ",canary.getPath());
	    } catch (IOException e) {
	      throw new TrackingException("Unable to read and modify files" +
	          " in the roll directory: " + this.dataDirFile, e);
	    }
	}
	
	/**
	 * 由于重启等原因，造成.buffer文件没有正确切换为.rowdata文件
	 * 为了解决多个tomcat，写入同一个目录。会造成重启一台tomcat会把另外一台tomcat正在写入的buff文件重命名。增加时间检测
	 */
	public void repairDataFile(){
		File firstFileDir=new File(this.dataDirFile.getAbsolutePath()+File.separator+"first");
		File secondFileDir=new File(this.dataDirFile.getAbsolutePath()+File.separator+"second");
		this.doRepairDataFile(firstFileDir);
		this.doRepairDataFile(secondFileDir);
	}
	
	
	private void doRepairDataFile(File subDataDirFile){
		File[] bufferFile=subDataDirFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.contains(".buffer") && name.contains(instanceMd5)){
					return true;
				}
				return false;
			}
		});
		if(bufferFile==null){
			return ;
		}
		for (File file : bufferFile) {
			this.renameToRowData(file);
		}
	}
	
	private void renameToRowData(File file){
		try {
			if(FileUtils.sizeOf(file)<=0){
				file.delete();
			}
			String currPath=file.getPath();
			String fileNameSubfix=currPath.substring(0, currPath.lastIndexOf("."));
			File dest = new File(fileNameSubfix + ".rowdata");
			boolean renamed = file.renameTo(dest);
			if (renamed) {
				LOGGER.debug("Successfully rolled file {} to {}",file.getPath(),dest.getPath());
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	private <T> T checkValue(T value,T defaultValue){
		if(value==null){
			return defaultValue;
		}
		return value;
	}

	public boolean isTrackingOn() {
		return trackingOn;
	}

	public boolean isTransportOn() {
		return transportOn.get();
	}
	
	public void handControlTransportOn(boolean transportOn) {
		this.transportOn.set(transportOn);
	}


	public String getTransporterImplClass() {
		return transporterImplClass;
	}


	public String getDataCenterServerUrl() {
		return dataCenterServerUrl;
	}

	public File getDataDirFile() {
		return dataDirFile;
	}

	public int getTransportInterval() {
		return transportInterval;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public int getThreadCoreSize() {
		return threadCoreSize;
	}

	public boolean isAppendNewline() {
		return appendNewline;
	}

	public byte[] getLineTerminateBy() {
		return lineTerminateBy;
	}

	public int getMaxThreadSize() {
		return maxThreadSize;
	}

	public String getInstanceMd5() {
		return instanceMd5;
	}

	public int getBatchSendServerSize() {
		return batchSendServerSize;
	}

	public int getRotateInterval() {
		return rotateInterval;
	}

	public int getMaxDataFileSize() {
		return maxDataFileSize;
	}


	public int getMaxRetryFileNum() {
		return maxRetryFileNum;
	}


	public int getCleanInterval() {
		return cleanInterval;
	}


	public int getCleanThreadCoreSize() {
		return cleanThreadCoreSize;
	}


	public int getCleanMaxThreadSize() {
		return cleanMaxThreadSize;
	}
	

	public int getCleanBatchSize() {
		return cleanBatchSize;
	}


	public int getCleanPreDay() {
		return cleanPreDay;
	}


	public static void main(String[] args) {
		System.out.println(getCurrentJVMProgressId());
	}
	
	

}
