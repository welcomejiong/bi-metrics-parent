package org.corps.bi.transport.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.corps.bi.core.Constants;
import org.corps.bi.core.MetricRequestParams;
import org.corps.bi.core.MetricResponse;
import org.corps.bi.metrics.Dau;
import org.corps.bi.recording.clients.rollfile.RollFileClient.SystemThreadFactory;
import org.corps.bi.tools.util.HttpClientUtils;
import org.corps.bi.tools.util.JSONUtils;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.corps.bi.transport.persist.file.FilePersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsTransporterHttpProcesser {
	
	private final static Logger LOGGER=LoggerFactory.getLogger(MetricsTransporterHttpProcesser.class);
	
	private final MetricsTransporterConfig transporterConfig;
	
	private final ThreadPoolExecutor threadPoolExecutor;
	
	private final int batchSendServerSize;
	
	private final FilePersister firstFilePersister;
	
	private final FilePersister secondFilePersister;
	
	public MetricsTransporterHttpProcesser(MetricsTransporterConfig transporterConfig){
		
		this.transporterConfig=transporterConfig;
		
		this.threadPoolExecutor = new ThreadPoolExecutor(
				this.transporterConfig.getThreadCoreSize()>1?(this.transporterConfig.getThreadCoreSize()-1):1,		//指的是保留的线程池大小
				this.transporterConfig.getMaxThreadSize(), 	//最大线程池， 指的是线程池的最大大小
				100, 	//指的是空闲线程结束的超时时间
				TimeUnit.SECONDS, 	//表示 keepAliveTime 的单位
				new LinkedBlockingQueue<Runnable>(100000),
				new SystemThreadFactory("metric-transporter-executor"),
				new ThreadPoolExecutor.DiscardPolicy() //直接放弃当前任务
		);
		
		this.batchSendServerSize=this.transporterConfig.getBatchSendServerSize();
		
		this.firstFilePersister=new FilePersister("first");
		
		this.secondFilePersister=new FilePersister("second");
		
	}
	
	public void doTransport(List<MetricRequestParams> batchMetrics){
		
		if(batchMetrics==null||batchMetrics.isEmpty()){
			return ;
		}
		int sendNum=batchMetrics.size();
		int temp=(sendNum/this.batchSendServerSize)+1;
		
		for(int i=0;i<temp;i++){
			try {
				int fromIndex=i*this.batchSendServerSize;
				int toIndex=(fromIndex+this.batchSendServerSize)>sendNum?sendNum:(fromIndex+this.batchSendServerSize);
				List<MetricRequestParams> subMetrics=batchMetrics.subList(fromIndex, toIndex);
				RequestServerThread requestServerThread=new RequestServerThread(subMetrics);
				this.threadPoolExecutor.submit(requestServerThread);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
	}
	
	private class RequestServerThread implements Runnable{
		
		private final List<MetricRequestParams> batchMetrics;

		public RequestServerThread(List<MetricRequestParams> batchMetrics) {
			super();
			this.batchMetrics = batchMetrics;
		}

		@Override
		public void run() {
			if(this.batchMetrics==null||this.batchMetrics.isEmpty()){
				return ;
			}
			String reqDataJson=null;
			try {
				if(transporterConfig.getDataCenterServerUrl()!=null&&!"".equals(transporterConfig.getDataCenterServerUrl())) {
					reqDataJson=JSONUtils.toJSON(this.batchMetrics);
					Map<String,String> params=new HashMap<String,String>();
					params.put("datas", reqDataJson);
					String res=HttpClientUtils.executePostRequest(transporterConfig.getDataCenterServerUrl(), params, Constants.DEFAULT_CHARSET);
					MetricResponse metricResponse=JSONUtils.fromJSON(res, MetricResponse.class);
					LOGGER.debug("reqServerUrl:{} params:{} res:{}",transporterConfig.getDataCenterServerUrl(),reqDataJson,res);
					if(metricResponse.isSucc()){
						return ;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(e.getMessage(), e);
			}
			// 失败的情况（1：网络异常，2：服务器返回为成功）
			try {
				for (MetricRequestParams metricRequestParams : batchMetrics) {
					metricRequestParams.incrRetry();
					boolean succ=false;
					if(metricRequestParams.getRetry()<=1){
						succ=firstFilePersister.persist(metricRequestParams);
					}else{
						succ=secondFilePersister.persist(metricRequestParams);
					}
					if(!succ){
						LOGGER.debug("reqServerUrl:{} params:{} persist to disk fail.",transporterConfig.getDataCenterServerUrl(),reqDataJson);
					}
					
				}
				
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		
		List<Dau> batchMetrics=new ArrayList<Dau>();
		String gameId="1";
		String clientId="2";
		String ds="20181218";
		for (int i = 0; i < 86; i++) {
			Dau dau=new Dau();
			dau.setClientId(clientId);
			dau.setAffiliate("affiliate_"+i);
			dau.setCreative("creative_"+i);
			dau.setDauDate("20181218");
			dau.setDauTime("10:52:32");
			dau.setFamily("famility_"+i);
			dau.setFromUid("f_u_id-"+i);
			dau.setGenus("genus_"+i);
			batchMetrics.add(dau);
			System.out.println(JSONUtils.toJSON(dau));
			System.out.println(JSONUtils.toJSONwithOutNullProp(dau));
		}
		MetricsTransporterConfig metricsTransporterConfig=MetricsTransporterConfig.getInstance();
		MetricsTransporterHttpProcesser metricsTransporterHttpProcesser=new MetricsTransporterHttpProcesser(metricsTransporterConfig);
		//metricsTransporterHttpProcesser.doTransport(batchMetrics);
		
		Thread.sleep(10*60*1000L);
		
	}

}
