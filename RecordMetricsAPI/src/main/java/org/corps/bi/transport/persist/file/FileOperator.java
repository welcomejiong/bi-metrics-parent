package org.corps.bi.transport.persist.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.corps.bi.core.Constants;
import org.corps.bi.recording.exception.TrackingException;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileOperator {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(FileOperator.class);
	
	private final MetricsTransporterConfig metricsTransporterConfig;
	
	private final String filePrefix;
	
	private final File currentFile;
	
	private final long createTimeMills;
	
	private final boolean appendNewline;
	
	// 换行符
	private final byte[] lineTerminateBy;
	
	private final OutputStream outputStream;
	
	private long writeBytes;
	
	private final AtomicBoolean shouldRotate;
	
	private final AtomicBoolean destoryed;
	/**
	 * 是否正在持久化
	 */
	private final AtomicBoolean isPersistenting;
	
	public FileOperator(String subFold,String filePrefix) {
		super();
		this.metricsTransporterConfig=MetricsTransporterConfig.getInstance();
		this.filePrefix=filePrefix;
		this.createTimeMills=System.currentTimeMillis();
		this.appendNewline=this.metricsTransporterConfig.isAppendNewline();
		this.lineTerminateBy=this.metricsTransporterConfig.getLineTerminateBy();
		this.shouldRotate=new AtomicBoolean(false);
		this.destoryed=new AtomicBoolean(false);
		this.isPersistenting=new AtomicBoolean(false);
		
		try {
			//文件名增加一个随机数，防止多个项目采集数据时生成相同的文件名
			StringBuilder targetFilePath=new StringBuilder(this.metricsTransporterConfig.getDataDirFile().getPath());
			targetFilePath.append(File.separator).append(subFold).append(File.separator).append("retry");
			targetFilePath.append("_").append(this.metricsTransporterConfig.getInstanceMd5()).append("_").append(this.filePrefix).append("_").append(this.createTimeMills).append(".buffer");
			
			File targetFile=new File(targetFilePath.toString());
			if(!targetFile.getParentFile().exists()) {
				targetFile.getParentFile().mkdirs();
			}
			if(!targetFile.exists()){
				targetFile.createNewFile();
				LOGGER.info("threadId:{} createNewFile for path:{}",Thread.currentThread().getId(),targetFile.getPath());
			}else{
				LOGGER.info("instance output for path:{}",targetFile.getPath());
			}
			
			this.currentFile=targetFile;
			this.outputStream = new BufferedOutputStream(new FileOutputStream(targetFile,true));
			this.writeBytes=0l;
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			throw new TrackingException("initOutput meet exception"+e.getMessage());
		}
	}
	
	/**
	 * 重命名为flume认识的log文件名
	 */
	private void renameCurrentFileToFlumeLog(){
		if(this.currentFile==null){
			return ;
		}
		String currPath=this.currentFile.getPath();
		String fileNameSubfix=currPath.substring(0, currPath.lastIndexOf("."));
		File dest = new File(fileNameSubfix + ".rowdata");
		boolean renamed = this.currentFile.renameTo(dest);
	    if (renamed) {
	    	LOGGER.info("Successfully rolled file {} to {}",this.currentFile.getPath(),dest.getPath());
	    }
	    /**
	     * 重命名把当前实例的所有置空
	     */
	    this.writeBytes=0l;
	}
	

	public boolean append(String content) {
		if(!this.isCanReceiveContent()){
			return false;
		}
		boolean allowPersistent=this.applyPersistent();
		try {
			if(!allowPersistent){
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("{} isExpire:{} createTime:{} is persistenting ...",this.currentFile.getAbsolutePath(),this.isExpire(),this.createTimeMills);
				}
				return false;
			}
			
			byte[] bytes=content.getBytes(Constants.DEFAULT_CHARSET);
			if(bytes==null){
				return false;
			}
			this.outputStream.write(bytes);
			this.writeBytes+=bytes.length;
			if (this.appendNewline && this.lineTerminateBy!=null) {
				this.outputStream.write(this.lineTerminateBy);
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}finally{
			if(allowPersistent){
				this.relievePersistent();
			}
		}
		return false;
	}
	
	
	public boolean append(List<String> contents) {
		if(contents==null||contents.isEmpty()){
			// 不需要处理所以返回true
			return true;
		}
		if(!this.isCanReceiveContent()){
			return false;
		}
		boolean allowPersistent=this.applyPersistent();
		try {
			if(!allowPersistent){
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("{} isExpire:{} createTime:{} is persistenting ...",this.currentFile.getAbsolutePath(),this.isExpire(),this.createTimeMills);
				}
				return false;
			}
			for (String content : contents) {
				byte[] bytes=content.getBytes(Constants.DEFAULT_CHARSET);
				if(bytes==null){
					return false;
				}
				this.outputStream.write(bytes);
				this.writeBytes+=bytes.length;
				if (this.appendNewline && this.lineTerminateBy!=null) {
					this.outputStream.write(this.lineTerminateBy);
				}
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}finally{
			if(allowPersistent){
				this.relievePersistent();
			}
		}
		return false;
	}
	
	public void flush(){
		try {
			this.outputStream.flush();
			LOGGER.debug("{} flush....",this.currentFile.getAbsolutePath());
			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	private void close(){
		try {
			this.flush();
			this.outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.debug("{} close....",this.currentFile.getAbsolutePath());
	}
	
	public void destory(){
		boolean succ=this.destoryed.compareAndSet(false, true);
		if(!succ){
			LOGGER.debug("triggerDestory:current instance have already destory!");
			return ;
		}
		//2：关系流
		this.close();
		// 重命名文件
		this.renameCurrentFileToFlumeLog();
	}
	
	/**
	 * 通知需要切换文件
	 * @return
	 */
	public boolean rotate(){
		return this.shouldRotate.compareAndSet(false, true);
	}
	/**
	 * 修改不需奥切换文件
	 * @return
	 */
	public boolean unRotate(){
		return this.shouldRotate.compareAndSet(true, false);
	}
	
	public boolean isNeedRotate(){
		if((this.isExpire()||this.isReachedMaxFileSize())&&this.writeBytes>0){
			return true;
		}
		return false;
	}
	
	public boolean isReachedMaxFileSize(){
		return this.writeBytes>=this.metricsTransporterConfig.getMaxDataFileSize();
	}
	
	/**
	 * 是否过期，默认在内存中存放不超过一天
	 * @return
	 */
	public boolean isExpire(){
		return (System.currentTimeMillis()-this.createTimeMills)/1000>=this.metricsTransporterConfig.getRotateInterval();
	}
	
	private boolean isCanReceiveContent(){
		return !this.shouldRotate.get();
	}
	
	/**
	 * 申请持久化
	 * @return
	 */
	private boolean applyPersistent(){
		return this.isPersistenting.compareAndSet(false, true);
	}
	/**
	 * 解除持久化
	 * @return
	 */
	private boolean relievePersistent(){
		return this.isPersistenting.compareAndSet(true, false);
	}
	/**
	 * 是否正在持久化
	 * @return
	 */
	public boolean isPersistenting(){
		return this.isPersistenting.get();
	}
}
