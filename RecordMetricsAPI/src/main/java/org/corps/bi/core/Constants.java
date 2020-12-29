package org.corps.bi.core;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(Constants.class);
	
	public static final String CONFIG_PATH="recordingConfig/recording.properties";
	
	public static final String DATE_FORMAT_DEFAULT="yyyy-MM-dd";
	
	public static final String DATE_FORMAT_NUM="yyyyMMdd";

	private static final Properties CONFIGS=new Properties();
	
	public final static long ONE_DAY_MILLS=24*60*60*1000;
	
	public final static long ONE_HOUR_MILLS=1*60*60*1000;
	
	public final static String DEFAULT_CHARSET="UTF-8";
	
	private static boolean IS_INIT=false;
	
	public static boolean IS_COUNT_METRICS=true; 
	
	public static boolean AD_TRACKING_CALLBACK_ISOPEN=false;
	
	public static String AD_TRACKING_CALLBACK_URL;
	
	public static boolean IS_TRACKINGON = true;
	
	public static boolean ROCKSDB_DB_IS_READONLY=false;
	
	public static String ROCKSDB_DB_PATH;
	
	public static String ROCKSDB_DB_LOGDIR;
	
	public static String ROCKSDB_DB_WALDIR;
	
	
	/**
	 * 初始化配置参数<br>
	 * 可以根据不同环境初始化不同的参数
	 * @param configPath 配置文件的路径
	 */
	public static void init(String configPath){
		try {
			if(IS_INIT){
				LOGGER.warn("this instance have already inited.");
				return ;
			}
			InputStream globleIn = Constants.class.getClassLoader().getResourceAsStream(configPath);
			if(globleIn==null){
				if(CONFIG_PATH.equals(configPath)){
					throw new RuntimeException(configPath+" is not exists!  You may ignore this error that the filepath is default.Howerver you must invoke this method by yourself.");
				}else{
					throw new RuntimeException(configPath+" is not exists!");
				}
			}
			CONFIGS.load(globleIn);
			
			parse();
			
			IS_INIT=true;
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	private static void parse(){
		if(CONFIGS.containsKey("is_count_metrics")){
			IS_COUNT_METRICS=Boolean.parseBoolean(CONFIGS.getProperty("is_count_metrics"));
		}
		if(CONFIGS.containsKey("ad_tracking_callback_isopen")){
			AD_TRACKING_CALLBACK_ISOPEN=Boolean.parseBoolean(CONFIGS.getProperty("ad_tracking_callback_isopen"));
		}
		if(CONFIGS.containsKey("ad_tracking_callback_url")){
			AD_TRACKING_CALLBACK_URL=CONFIGS.getProperty("ad_tracking_callback_url");
		}
		if(CONFIGS.containsKey("tracking_on")){
			String tracking_on = CONFIGS.getProperty("tracking_on");
			if(!StringUtils.isEmpty(tracking_on) && "1".equals(tracking_on)){
				IS_TRACKINGON = true;
			}else{
				IS_TRACKINGON = false;
			}
		}
		
		ROCKSDB_DB_IS_READONLY = Boolean.parseBoolean(CONFIGS.getProperty("rocksdb.db.readonly","false"));
		
		ROCKSDB_DB_PATH=CONFIGS.getProperty("rocksdb.db.path","/var/rocksdb/path");
		
		ROCKSDB_DB_LOGDIR=CONFIGS.getProperty("rocksdb.db.logDir","/var/rocksdb/logDir");
		
		ROCKSDB_DB_WALDIR=CONFIGS.getProperty("rocksdb.db.walDir","/var/rocksdb/walDir");
		
	}

	public static Properties getConfigs() {
		return CONFIGS;
	}
	
}
