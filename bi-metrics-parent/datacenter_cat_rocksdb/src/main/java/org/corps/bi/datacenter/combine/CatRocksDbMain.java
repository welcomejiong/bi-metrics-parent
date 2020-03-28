package org.corps.bi.datacenter.combine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.PropertyConfigurator;
import org.corps.bi.core.Constants;
import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
import org.corps.bi.metrics.IMetric;
import org.corps.bi.transport.MetricsInnerReTransporter;
import org.corps.bi.transport.http.inner.MetricsInnerReTransporterHttpImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatRocksDbMain {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(CatRocksDbMain.class);
	
	public static final String CONSTANT_GLOBLE_FILE_PATH = "constant_globle.properties";
	
	private CatRocksdbService catRocksdbService;
	
	//private RecordingServices recordServices=RecordingServices.getInstance();
	
	public CatRocksDbMain() {
		super();
		this.catRocksdbService=new CatRocksdbService();
		this.init();
	}
	
	private void init() {
		try {
			InputStream log4jIs=this.getClass().getClassLoader().getResourceAsStream("log4j_other.properties");
			PropertyConfigurator.configure(log4jIs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void cat(String[] metrics,String[] days) {
		for (String metric : metrics) {
			for (String day : days) {
				this.catRocksdbService.cat(metric,day);
			}
		}
	}
	
	private MetricsInnerReTransporter resend(String resend,String[] metrics,String[] days) {
		if(resend==null||!"true".equals(resend)) {
			System.out.println("not resend");
			return null;
		}
		MetricsInnerReTransporter metricsInnerReTransporter=new MetricsInnerReTransporterHttpImpl(metrics, days);
		
		LOGGER.info("resend begin... isTransporting:"+metricsInnerReTransporter.isTransporting());
		metricsInnerReTransporter.reTransport();
		LOGGER.info("resend have already trigger reTransport... isTransporting:"+metricsInnerReTransporter.isTransporting());
		return metricsInnerReTransporter;
	}
	
	private static  Options generateOptions() {
		Options options=new Options();
		
		options.addOption("metrics",true, "all metrics for all,other for install ,dau ,payment...");
		
		options.addOption("days",true, " format:yyyyMMdd eg:20200310");
		
		options.addOption("resend",true, ",resend metrics");

		return options;
	}
	
	private static String getConfigPath() {
		try {
			InputStream globleIn = CatRocksDbMain.class.getClassLoader().getResourceAsStream(CONSTANT_GLOBLE_FILE_PATH);
			Properties globleProperties = new Properties();
			if(globleIn==null){
				throw new RuntimeException("constant_globle.properties is not exists!");
			}
			globleProperties.load(globleIn);
			// 项目本事的常量文件
			String constantFilePath=globleProperties.getProperty("recording_constant_file_path");
			return constantFilePath;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}


	public static void main(String[] args) throws Exception {
		
		CommandLineParser parser = new DefaultParser();
		
		Options options=generateOptions();

		CommandLine commandLine=parser.parse(options, args);
		
		//String confPath=commandLine.getOptionValue("conf","recording.properties");
		
		String[] metrics=commandLine.getOptionValue("metrics","install").split(",");
		
		String resend=commandLine.getOptionValue("resend","false");
		
		String[] days=commandLine.getOptionValue("days",DateFormatUtils.format(new Date(), Constants.DATE_FORMAT_NUM)).split(",");
		
		String confPath=getConfigPath();
		
		Constants.init(confPath);
			
		CatRocksDbMain combineMain=new CatRocksDbMain();
		
		combineMain.cat(metrics,days);
		
		MetricsInnerReTransporter metricsInnerReTransporter=combineMain.resend(resend,metrics,days);
		
		if(metricsInnerReTransporter!=null) {
			metricsInnerReTransporter.shutdown();
		}
		System.exit(0);
		
		
	}
	
}
