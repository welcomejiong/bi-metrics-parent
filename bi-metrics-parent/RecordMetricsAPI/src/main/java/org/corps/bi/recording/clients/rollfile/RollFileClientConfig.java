package org.corps.bi.recording.clients.rollfile;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.corps.bi.core.Constants;
import org.corps.bi.recording.exception.TrackingException;
import org.corps.bi.utils.MD5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollFileClientConfig {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(RollFileClientConfig.class);
	
	private static final String DEFAULT_DATA_DIR="/tmp";
	
	private static final int DEFAULT_ROLL_INTERVAL=300000;
	
	private final File dataDirFile;
	
	private final int rollInterval;
	
	private final int batchSize;
	
	private final int threadCoreSize;
	
	private final int maxThreadSize;
	
	private final String instanceMd5;
	
	private final boolean appendNewline;
	
	private final byte[] lineTerminateBy;
	

	public RollFileClientConfig() {
		super();
		Properties configs=Constants.getConfigs();
		
		String dataDir=this.checkValue(configs.getProperty("rollfile.dataDir"), DEFAULT_DATA_DIR);
		this.dataDirFile=new File(dataDir);
		
		String rollIntervalStr=this.checkValue(configs.getProperty("rollfile.rollInterval"), DEFAULT_ROLL_INTERVAL+"");
		this.rollInterval=Integer.parseInt(rollIntervalStr);
		
		String batchSizeStr=this.checkValue(configs.getProperty("rollfile.batchSize"), "100");
		this.batchSize=Integer.parseInt(batchSizeStr);
		
		String threadCoreSizeStr=this.checkValue(configs.getProperty("rollfile.threadCoreSize"), "1");
		this.threadCoreSize=Integer.parseInt(threadCoreSizeStr);
		
		String maxThreadSizeStr=this.checkValue(configs.getProperty("rollfile.maxThreadSize"), "3");
		this.maxThreadSize=Integer.parseInt(maxThreadSizeStr);
		
		String appendNewlineStr=this.checkValue(configs.getProperty("rollfile.appendNewline"), "true");
		this.appendNewline=Boolean.parseBoolean(appendNewlineStr);
		
		String lineTerminateByStr=this.checkValue(configs.getProperty("rollfile.lineTerminateBy"), "\n");
		this.lineTerminateBy=lineTerminateByStr.getBytes(Charset.forName("UTF-8"));
		
		String instanceClassPath=this.getClass().getClassLoader().getResource("./").getPath();
		this.instanceMd5=MD5Utils.MD5(instanceClassPath);
		
		LOGGER.info("current instance classpath is :{} for md5:{}",instanceClassPath,this.instanceMd5);
		
		this.checkPermissions();
		
		this.repairDataFile();
		
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
			LOGGER.debug("Successfully created and deleted canary file: "+canary.getPath());
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
		File[] bufferFile=this.dataDirFile.listFiles(new FilenameFilter() {
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
			String currPath=file.getPath();
			String fileNameSubfix=currPath.substring(0, currPath.lastIndexOf("."));
			File dest = new File(fileNameSubfix + ".rowdata");
			boolean renamed = file.renameTo(dest);
			if (renamed) {
				LOGGER.debug("Successfully rolled file "+file.getPath()+" to "+dest.getPath());
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

	public File getDataDirFile() {
		return dataDirFile;
	}

	public int getRollInterval() {
		return rollInterval;
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
	
	

}
