package org.corps.bi.recording.clients.rollfile;

import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.corps.bi.recording.exception.TrackingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RollFileClient {
	
	private static final Logger LOGGER=LoggerFactory.getLogger("rollfile");
	
	private final RollFileClientConfig rollFileClientConfig;
	
	private ScheduledExecutorService rollService;
	
	private final RollFileEventDispather rollFileEventDispather;

	public RollFileClient() {
		super();
		rollFileClientConfig=new RollFileClientConfig();
		this.init();
		this.rollFileEventDispather=new RollFileEventDispather(this.rollFileClientConfig);
	}
	
	private void init(){
		this.initService();
	}
	
	private void initService() {
		try {
			int rollInterval = rollFileClientConfig.getRollInterval();
			if (rollInterval <= 0) {
				throw new TrackingException("RollInterval is error !");
			}
			rollService = Executors.newScheduledThreadPool(1,
					new SystemThreadFactory("bi-rollFile-client"));
			
			RollFileRollerThread rollerThread = new RollFileRollerThread();
			rollService.scheduleAtFixedRate(rollerThread, rollInterval, rollInterval, TimeUnit.SECONDS);
			
			RollFilePersistentThread rollFilePersistentThread=new RollFilePersistentThread();
			rollService.scheduleAtFixedRate(rollFilePersistentThread, 500, 500, TimeUnit.MILLISECONDS);
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public boolean send(String category,String event,String body){
		RollFileEvent rollFileEvent=new RollFileEvent(category, event, body,Charset.forName("UTF-8"));
		return this.rollFileEventDispather.send(rollFileEvent);
	}
	
	public boolean send(String category,String event,String body,Charset bodyCharset){
		RollFileEvent rollFileEvent=new RollFileEvent(category, event, body,bodyCharset);
		return this.rollFileEventDispather.send(rollFileEvent);
	}
	
	public boolean send(String category,String event,byte[] body){
		RollFileEvent rollFileEvent=new RollFileEvent(category, event, body);
		return this.rollFileEventDispather.send(rollFileEvent);
	}
	
	private class RollFileRollerThread implements Runnable{
		@Override
		public void run() {
			try {
				rollFileEventDispather.notifyRotate();
				// 清楚过期的序列化实例
				rollFileEventDispather.clearExpireEventSerializer();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	private class RollFilePersistentThread implements Runnable{
		@Override
		public void run() {
			try {
				rollFileEventDispather.persistent();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	public static class SystemThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		public  SystemThreadFactory(String name) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = name+"-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}

	}

}
