package org.corps.bi.cleaner.rocksdb;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.corps.bi.cleaner.MetricsRecordsCleaner;
import org.corps.bi.core.Constants;
import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
import org.corps.bi.dao.rocksdb.RocksdbCleanedGlobalManager;
import org.corps.bi.recording.clients.rollfile.RollFileClient.SystemThreadFactory;
import org.corps.bi.recording.exception.TrackingException;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsRecordsCleanerRocksdbImpl implements MetricsRecordsCleaner{
	
	private static final Logger LOGGER=LoggerFactory.getLogger(MetricsRecordsCleanerRocksdbImpl.class);
	
	public static final String DEFAULT_FORMAT_DATE_SECONDS="yyyy-MM-dd HH:mm:ss";
	
	private AtomicBoolean isCleaning=new AtomicBoolean(false);
	
	private  MetricsTransporterConfig cleanerConfig;
	
	private  ScheduledExecutorService cleanerIntervalService;
	
	private  TriggerThread triggerThread;
	

	public MetricsRecordsCleanerRocksdbImpl() {
		super();
		this.cleanerConfig=MetricsTransporterConfig.getInstance();
	}

	private boolean start() {
		if(!this.isCleaning.compareAndSet(false, true)) {
			LOGGER.warn("is transporting now!");
			return false;
		}
		int rollInterval = this.cleanerConfig.getCleanInterval();
		if (rollInterval <= 0) {
			throw new TrackingException("RollInterval is error !");
		}
		cleanerIntervalService = Executors.newScheduledThreadPool(1,new SystemThreadFactory("metric-inner-cleaner-scheduled"));
		
		this.triggerThread=new TriggerThread(this.cleanerConfig);
		
		this.cleanerIntervalService.scheduleAtFixedRate(triggerThread,rollInterval, rollInterval, TimeUnit.MILLISECONDS);
		
		return true;
	}
	
	
	private class TriggerThread implements Runnable{
		
		private final ThreadPoolExecutor threadPoolExecutor;
		
		private final MetricsTransporterConfig transporterConfig;
		
		public TriggerThread(MetricsTransporterConfig transporterConfig) {
			super();
			this.transporterConfig=transporterConfig;
			this.threadPoolExecutor = new ThreadPoolExecutor(
					/**
					 * 这里的corePoolSize就是连接池的maxActive的概念，它没有minIdle的概念(
					 * 每个线程可以设置keepAliveTime，超过多少时间多有任务后销毁线程，默认只会针对maximumPoolSize参数的线程生效，
					 * 可以设置allowCoreThreadTimeOut=true，就可以对corePoolSize进行idle回收)
					 */
					this.transporterConfig.getCleanThreadCoreSize(),
					/**
					 * 这里的maximumPoolSize，是一种救急措施的第一层。当threadPoolExecutor的工作threads存在满负荷，并且block queue队列也满了，这时代表接近崩溃边缘。
					 * 这时允许临时起一批threads，用来处理runnable，处理完后通过keepAliveTime进行调度回收
					 * 所以建议：  maximumPoolSize >= corePoolSize =期望的最大线程数。 
					 * (我曾经配置了corePoolSize=1, maximumPoolSize=20, blockqueue为无界队列，最后就成了单线程工作的pool。典型的配置错误)
					 */
					this.transporterConfig.getCleanMaxThreadSize(), 	
					100, 	//指的是空闲线程结束的超时时间
					TimeUnit.SECONDS, 	//表示 keepAliveTime 的单位
					new LinkedBlockingQueue<Runnable>(1000),
					new SystemThreadFactory("metric-inner-transporter-executor"),
					new ThreadPoolExecutor.DiscardPolicy() //直接放弃当前任务
			);
		}

		@Override
		public void run() {
			try {
				for (MetricRocksdbColumnFamilys metricRocksdbColumnFamily : MetricRocksdbColumnFamilys.values()) {
					this.processCleanMetric(metricRocksdbColumnFamily);
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
		
		private void processCleanMetric(MetricRocksdbColumnFamilys metricRocksdbColumnFamily) {
			this.threadPoolExecutor.submit(new CleanMetricDataThread(metricRocksdbColumnFamily.getMetric()));
		}
		
		public boolean shutdown() {
			try {
				this.threadPoolExecutor.shutdown();
				return true;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
				return false;
			}
			
		}
		
	}

	
	private class CleanMetricDataThread implements Runnable{
		
		private final String metric;

		public CleanMetricDataThread(final String metric) {
			super();
			this.metric=metric;
		}

		@Override
		public void run() {
			boolean isLock=false;
			try {
				isLock=RocksdbCleanedGlobalManager.getInstance().tryLockCleaned(this.metric);
				if(!isLock) {
					LOGGER.info("metric:{} clean data try lock is fail!",this.metric);
					return ;
				}
				this.pollMetricsV2();
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}finally {
				if(isLock) {
					RocksdbCleanedGlobalManager.getInstance().unLockCleaned(this.metric);
				}
			}
		}
		
		/**
		 * 通过自增id的模式，批量获取，至少可以保证接收到的顺序和发出的顺序是一致的
		 */
		private void pollMetricsV2(){
			try {
				
				
				Date now=new Date();
				
				Date deleteDate=DateUtils.addDays(now, -MetricsTransporterConfig.getInstance().getCleanPreDay());
				
				//this.deleteByDay(now,deleteDate);
				
				int preDaysIdx=MetricsTransporterConfig.getInstance().getCleanPreDay()*5;
				
				for (int i = 1; i <= preDaysIdx; i++) {
					this.deleteByDay(now,DateUtils.addDays(deleteDate,-i));
				}
				
				
				
			}  catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
		
		private void deleteByDay(Date nowDate,Date delDate) {
			try {
				long begin=System.currentTimeMillis();
				String day=DateFormatUtils.format(delDate, Constants.DATE_FORMAT_NUM);
				for (int i = 0; i < 24; i++) {
					String hour=i+"";
					if(i<10) {
						hour="0"+i;
					}
					String hourOfDay=day+hour;
					// 2个版本的先同步共存一段时间
					RocksdbCleanedGlobalManager.getInstance().delExpiredNeedCleanIds(this.metric, hourOfDay);
				}
				long end=System.currentTimeMillis();
				LOGGER.info("metric:{} now:{} delDate:{} spendMills:{}",
						this.metric,DateFormatUtils.format(nowDate,DEFAULT_FORMAT_DATE_SECONDS),day,(end-begin));
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
		}
	}
	
	@Override
	public boolean clean() {
		// 目前只有中转的机器，会用rocksdb存储
		if(!this.cleanerConfig.isTransportOn()) {
			LOGGER.info("transport the trigger of tracking on is off.please config the transprot.on to true");
			return false;
		}
		return this.start();
	}

	@Override
	public boolean shutdown() {
		try {
			if(!this.isCleaning.compareAndSet(true, false)) {
				this.isCleaning.set(false);
				LOGGER.warn("there is the other thread set cleaning to true.");
				return false;
			}
			this.cleanerIntervalService.shutdown();
			this.triggerThread.shutdown();
			return true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
			return false;
		}
	}

}
