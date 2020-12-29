package org.corps.bi.recording.clients.rollfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.corps.bi.recording.GameHeader;
import org.corps.bi.recording.clients.rollfile.RollFileClient.SystemThreadFactory;
import org.corps.bi.recording.clients.rollfile.serializer.RollFileEventSerializer;
import org.corps.bi.recording.clients.rollfile.serializer.RollFileEventSerializerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RollFileEventDispather {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RollFileEventDispather.class);
	
	private final RollFileClientConfig rollFileClientConfig;
	
	private final ConcurrentHashMap<String, RollFileEventSerializer> gameEventSerializerMap;
	
	private final ThreadPoolExecutor threadPoolExecutor;
	
	public RollFileEventDispather(RollFileClientConfig rollFileClientConfig) {
		super();
		this.rollFileClientConfig = rollFileClientConfig;
		this.gameEventSerializerMap=new ConcurrentHashMap<String, RollFileEventSerializer>(8);
		this.threadPoolExecutor = new ThreadPoolExecutor(
				this.rollFileClientConfig.getThreadCoreSize()>1?(this.rollFileClientConfig.getThreadCoreSize()-1):1,		//指的是保留的线程池大小
				this.rollFileClientConfig.getMaxThreadSize(), 	//最大线程池， 指的是线程池的最大大小
				100, 	//指的是空闲线程结束的超时时间
				TimeUnit.SECONDS, 	//表示 keepAliveTime 的单位
				new LinkedBlockingQueue<Runnable>(100000),
				new SystemThreadFactory("bi-rollFile-dispather"),
				new ThreadPoolExecutor.DiscardPolicy() //直接放弃当前任务
		);
	}

	private RollFileEventSerializer getSerializer(RollFileEvent event){
		if(event==null){
			return null;
		}
		try {
			GameHeader gameHeader=new GameHeader(event.getCategory(),event.getEvent());
			RollFileEventSerializer gameEventSerializer=this.getEventSerializer(gameHeader);
			return gameEventSerializer;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		return null;
	}
	
	private synchronized RollFileEventSerializer getEventSerializer(GameHeader gameHeader){
		String headerId=gameHeader.getHeaderId();
		RollFileEventSerializer rollFileEventSerializer=null;
		if(this.gameEventSerializerMap.containsKey(headerId)){
			rollFileEventSerializer=this.gameEventSerializerMap.get(headerId);
//			/**
//			 * 安全期间，审核以及清除下过期的序列化对象
//			 */
//			if(rollFileEventSerializer.isExpire()){
//				this.clearExpireEventSerializer();
//				return this.getEventSerializer(gameHeader);
//			}
			return rollFileEventSerializer;
		}
		
		LOGGER.info("create new RollFileEventSerializer for headerId:{} thisId:{}",headerId,this.hashCode());
		
		rollFileEventSerializer=new RollFileEventSerializerImpl(this.rollFileClientConfig,gameHeader);
		this.gameEventSerializerMap.put(headerId, rollFileEventSerializer);
		
		return rollFileEventSerializer;
	}
	
	public boolean send(RollFileEvent rollFileEvent){
		RollFileEventSerializer rollFileEventSerializer=this.getSerializer(rollFileEvent);
		if(rollFileEventSerializer==null){
			return false;
		}
		return rollFileEventSerializer.send(rollFileEvent);
	}
	
	/**
	 * 这个地方有可能有并发的问题
	 * eg:
	 * 如果判断的这一刻过期了，正准备关闭序列化流，但是刚好又有数据匹配上
	 * 一般情况下，这种情况比较的少，除非发送的是昨天的数据（为了保证序列化是最新的，在获取序列化的时候，也做了一次校验操作）
	 * 因此需要同步来屏蔽这个问题
	 * @return
	 */
	public synchronized  boolean clearExpireEventSerializer(){
		try {
			List<String> removeWapperKeyList=new ArrayList<String>();
			for (Entry<String, RollFileEventSerializer>  entry: this.gameEventSerializerMap.entrySet()) {
				RollFileEventSerializer rollFileEventSerializer=entry.getValue();
				if(!rollFileEventSerializer.isExpire()){
					continue;
				}
				removeWapperKeyList.add(entry.getKey());
			}
			if(removeWapperKeyList.isEmpty()){
				return true;
			}
			
			this.destory(removeWapperKeyList);
			
			for (String key : removeWapperKeyList) {
				this.gameEventSerializerMap.remove(key);
			}
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		return false;
	}
	
	public void flush(Collection<String> headerIdList){
		if(headerIdList.isEmpty()){
			return ;
		}
		for (String headerId : headerIdList) {
			try {
				RollFileEventSerializer rollFileEventSerializer=this.gameEventSerializerMap.get(headerId);
				if(rollFileEventSerializer==null){
					continue;
				}
				rollFileEventSerializer.flush();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
	}
	
	public void destoryAll(){
		for (Entry<String, RollFileEventSerializer>  entry: this.gameEventSerializerMap.entrySet()) {
			RollFileEventSerializer rollFileEventSerializer=entry.getValue();
			rollFileEventSerializer.destory();
		}
	}
	
	public void destory(Collection<String> headerIdList){
		if(headerIdList.isEmpty()){
			return ;
		}
		for (String headerId : headerIdList) {
			RollFileEventSerializer rollFileEventSerializer=this.gameEventSerializerMap.get(headerId);
			if(rollFileEventSerializer==null){
				continue;
			}
			rollFileEventSerializer.destory();
		}
	}
	
	public void notifyRotate(){
		for (Entry<String, RollFileEventSerializer>  entry: this.gameEventSerializerMap.entrySet()) {
			RollFileEventSerializer rollFileEventSerializer=entry.getValue();
			boolean isSucc=rollFileEventSerializer.rotate();
			if(!isSucc){
				LOGGER.warn("rotate fail to {}",entry.getKey());
			}
		}
	}
	
	public void persistent(){
		for (Entry<String, RollFileEventSerializer>  entry: this.gameEventSerializerMap.entrySet()) {
			RollFileEventSerializer rollFileEventSerializer=entry.getValue();
			if(rollFileEventSerializer.isPersistenting()){
				continue;
			}
			AsyncPersistentRollFileEventSerializerThread asyncPersistentRollFileEventSerializerThread=new AsyncPersistentRollFileEventSerializerThread(rollFileEventSerializer, this.rollFileClientConfig.getBatchSize());;
			this.threadPoolExecutor.submit(asyncPersistentRollFileEventSerializerThread);
		}
	}
	
	public class AsyncPersistentRollFileEventSerializerThread implements Runnable{
		
		private RollFileEventSerializer rollFileEventSerializer;
		
		private int batchSize;

		public AsyncPersistentRollFileEventSerializerThread(RollFileEventSerializer rollFileEventSerializer,int batchSize) {
			super();
			this.rollFileEventSerializer = rollFileEventSerializer;
			this.batchSize=batchSize;
		}

		@Override
		public void run() {
			try {
				this.rollFileEventSerializer.persistent(batchSize);
				this.rollFileEventSerializer.flush();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		
	}

}
