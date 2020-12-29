package org.corps.bi.recording.clients.rollfile.serializer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.corps.bi.core.Constants;
import org.corps.bi.recording.GameHeader;
import org.corps.bi.recording.clients.rollfile.RollFileClientConfig;
import org.corps.bi.recording.clients.rollfile.RollFileEvent;
import org.corps.bi.recording.exception.TrackingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollFileEventSerializerImpl implements RollFileEventSerializer{
	
	private static final Logger LOGGER =LoggerFactory.getLogger("BI_Serializer");
	
	private final RollFileClientConfig rollFileClientConfig;
	
	private final GameHeader gameHeader;
	
	private final AtomicInteger fileIndex;
	
	private final long createTimeMills;
	
	private final boolean appendNewline;
	
	// 换行符
	private final byte[] lineTerminateBy;
	
	private final AtomicBoolean shouldRotate;
	
	/**
	 * 最大支持50万条记录
	 */
	private final BlockingQueue<RollFileEvent> blockingQueue;
	
	private File currentFile;
	
	private OutputStream outputStream;
	
	private long writeBytes;
	
	private final AtomicBoolean destoryed;
	/**
	 * 是否正在持久化
	 */
	private final AtomicBoolean isPersistenting;

	public RollFileEventSerializerImpl(RollFileClientConfig rollFileClientConfig,GameHeader gameHeader) {
		super();
		this.rollFileClientConfig=rollFileClientConfig;
		this.gameHeader = gameHeader;
		this.fileIndex=new AtomicInteger(0);
		this.createTimeMills = System.currentTimeMillis();
		this.appendNewline=rollFileClientConfig.isAppendNewline();
		this.lineTerminateBy=rollFileClientConfig.getLineTerminateBy();
		this.shouldRotate=new AtomicBoolean(false);
		this.blockingQueue = new LinkedBlockingDeque<RollFileEvent>(500000);
		this.destoryed=new AtomicBoolean(false);
		this.isPersistenting=new AtomicBoolean(false);
		this.nextOutput();
	}
	
	

	private void rotateOutPut(){
		
		/**
		 * 1:关闭流
		 */
		this.close();
		
		/**
		 * 2:把当前文件重命名
		 */
		this.renameCurrentFileToFlumeLog();
		
		/**
		 * 3:切换为一个新的文件
		 */
		this.nextOutput();
		
		/**
		 * 4:切换为可以通知切换文
		 */
		this.unRotate();
	}
	
	private void nextOutput(){
		try {
			//文件名增加一个随机数，防止多个项目采集数据时生成相同的文件名
			StringBuilder targetFilePath=new StringBuilder(this.rollFileClientConfig.getDataDirFile().getPath());
			targetFilePath.append(File.separator).append(this.gameHeader.getEvent());
			targetFilePath.append("_").append(this.rollFileClientConfig.getInstanceMd5()).append("_").append(this.createTimeMills).append("_").append(this.fileIndex.incrementAndGet()).append(".buffer");
			
			File targetFile=new File(targetFilePath.toString());
			if(!targetFile.getParentFile().exists()) {
				targetFile.getParentFile().mkdirs();
			}
			if(!targetFile.exists()){
				targetFile.createNewFile();
				LOGGER.info("createNewFile for path:{}",targetFile.getPath());
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
	    this.currentFile=null;
	    this.outputStream=null;
	    this.writeBytes=0l;
	}
	
	private void persistent(RollFileEvent rollFileEvent) throws IOException{
		byte[] bytes=rollFileEvent.getBody();
		if(bytes==null){
			return ;
		}
		this.outputStream.write(bytes);
		this.writeBytes+=bytes.length;
	    if (this.appendNewline && this.lineTerminateBy!=null) {
	    	this.outputStream.write(this.lineTerminateBy);
	    }
	}
	
	/**
	 * 500毫秒执行一次，如果上次500毫秒没有执行完毕，下次500毫秒又开始执行，会造成写入文件混乱的情况
	 * @param batchSize
	 */
	public void persistent(int batchSize){
		/**
		 * 1:先处理是否需要切分
		 * 2:如果已经有内容写入，则切分
		 */
		boolean allowPersistent=this.applyPersistent();
		try {
			
			if(!allowPersistent){
				LOGGER.debug("{} isExpire:{} createTime:{} is persistenting ...",this.gameHeader,this.isExpire(),this.createTimeMills);
				return ;
			}
			
			long begin=System.currentTimeMillis();
			
			LOGGER.debug("{} isExpire:{} createTime:{} persistent begin ...",this.gameHeader,this.isExpire(),this.createTimeMills);
			
			if(this.shouldRotate.get() && this.writeBytes>0l){
				this.rotateOutPut();
			}
			
			if(blockingQueue.isEmpty()){
				return ;
			}
			int succ=0;
			for (int i = 0; i < batchSize; i++) {
				try {
					RollFileEvent rollFileEvent=blockingQueue.poll();
					if(rollFileEvent==null){
						return ;
					}
					this.persistent(rollFileEvent);
					succ++;
				} catch (Exception e) {
					LOGGER.error(e.getMessage(),e);
				}
			}
			long end=System.currentTimeMillis();
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("{} isExpire:{} createTime:{} persistent end... succRecordsNum:{} spendMills({})",this.gameHeader,this.isExpire(),this.createTimeMills,succ,(end-begin));
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}finally{
			if(allowPersistent){
				this.relievePersistent();
			}
		}
	}
	
	public boolean send(RollFileEvent rollFileEvent){
		if(this.destoryed.get()){
			throw new RuntimeException("sendMess:current instance have already destory!");
		}
		
		try {
			boolean ret=this.blockingQueue.add(rollFileEvent);
			
			LOGGER.debug("{} isExpire:{} createTime:{} body:{} add cache succ...",this.gameHeader,this.isExpire(),this.createTimeMills,rollFileEvent.getBody());
			
			
			return ret;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		return false;
	}
	
	public void flush(){
		try {
			this.outputStream.flush();
			if(LOGGER.isDebugEnabled()){
				
			}
			
			LOGGER.debug("{} flush....",this.gameHeader.getHeaderId());
			
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
		LOGGER.debug("{} close....",this.gameHeader.getHeaderId());
	}
	
	public void destory(){
		boolean succ=this.destoryed.compareAndSet(false, true);
		if(!succ){
			LOGGER.debug("triggerDestory:current instance have already destory!");
			return ;
		}
		//1： 先把所有缓存区中的数据，全部刷到硬盘上
		this.persistent(this.blockingQueue.size());
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
	
	/**
	 * 是否过期，默认在内存中存放不超过一天
	 * @return
	 */
	public boolean isExpire(){
		return System.currentTimeMillis()-this.createTimeMills>=Constants.ONE_DAY_MILLS;
	}
	
	public GameHeader getGameHeader() {
		return gameHeader;
	}


	public long getCreateTimeMills() {
		return createTimeMills;
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
