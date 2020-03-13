package org.corps.bi.transport.http.inner;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.corps.bi.core.Constants;
import org.corps.bi.metrics.Dau;
import org.corps.bi.metrics.converter.DauConverter;
import org.corps.bi.metrics.converter.MetricEntityConverterManager;
import org.corps.bi.protobuf.BytesList;
import org.corps.bi.protobuf.KVEntity;
import org.corps.bi.services.RecordingServices.MetaExtraBuilder;
import org.corps.bi.tools.util.JSONUtils;
import org.corps.bi.tools.util.NetUtils;
import org.corps.bi.transport.MetricsTransporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsInnerTransporterHttpProcesser {
	
	private final static Logger LOGGER=LoggerFactory.getLogger(MetricsInnerTransporterHttpProcesser.class.getSimpleName());
	
	private final String metric;
	
	private final BytesList batchMetrics;
	
	private final MetricsTransporterConfig transporterConfig;
	
	public MetricsInnerTransporterHttpProcesser(String metric,BytesList batchMetrics,MetricsTransporterConfig transporterConfig){
		this.metric=metric;
		this.batchMetrics=batchMetrics;
		this.transporterConfig=transporterConfig;
	}
	
	public boolean doTransport(){
		
		if(batchMetrics==null||batchMetrics.isEmpty()){
			return true;
		}
		
		if(transporterConfig.getDataCenterServerUrl()==null||"".equals(transporterConfig.getDataCenterServerUrl())) {
			throw new RuntimeException("please config data_center_server_url param!");
		}
		
		byte[] reqDatas=batchMetrics.toByteArray();
		
		
		boolean isSucc=this.doSend(reqDatas);
		
		return isSucc;
	}
	
	private boolean doSend(byte[] reqDatas) {
		long begin=System.currentTimeMillis(); 
		String resContent=null;
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			try {
				RequestConfig.Builder reqConfigBuilder=RequestConfig.custom();
				reqConfigBuilder.setConnectionRequestTimeout(5000);
				reqConfigBuilder.setConnectTimeout(5000);
				reqConfigBuilder.setSocketTimeout(10000);
				
				HttpPost httpPost = new HttpPost(this.transporterConfig.getDataCenterServerUrl());

				StringBody metricBody = new StringBody(this.metric, ContentType.TEXT_PLAIN);
				ByteArrayBody metricDatas = new ByteArrayBody(reqDatas,"metricDatas.bytes");
				StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);
				
				//FileBody uploadFiles = new FileBody(new File("/Users/guojianjiong/work/app_datas/rocksdb/walDir/000038.log"));
				
				MultipartEntityBuilder multipartEntityBuilder=MultipartEntityBuilder.create();
				multipartEntityBuilder.setMode(HttpMultipartMode.RFC6532);
				//multipartEntityBuilder.addPart("uploadFiles", uploadFiles);
				
				int recordNum=this.batchMetrics.size();
				
				long dataSize=reqDatas.length;
				
				multipartEntityBuilder.addPart("metricDatas", metricDatas);
				multipartEntityBuilder.addPart("metric", metricBody);
				multipartEntityBuilder.addPart("comment", comment);
				multipartEntityBuilder.addTextBody("version", "3.0");
				multipartEntityBuilder.addTextBody("recordNum", recordNum+"");
				multipartEntityBuilder.addTextBody("dataSize", dataSize+"");
				HttpEntity reqEntity = multipartEntityBuilder.build();

				httpPost.setEntity(reqEntity);

				LOGGER.debug("executing request line:{}" ,httpPost.getRequestLine());
				
				CloseableHttpResponse response = null;
				try {
					response = httpclient.execute(httpPost);
					StatusLine statusLine=response.getStatusLine();
//					if(statusLine.getStatusCode()!=200){
//						return false;
//					}
					HttpEntity resEntity = response.getEntity();
					if(resEntity==null){
						LOGGER.warn("Response resEntity is null" );
						return false;
					}
					
					resContent=EntityUtils.toString(resEntity, "UTF-8");
					LOGGER.debug("Response content: {}" , resContent);
					
					Map<String,Object> result=JSONUtils.fromJSON(resContent,HashMap.class);
					String status=(String)result.get("status");
					
					if(!"succ".equals(status)){
						LOGGER.error("send metric server status is error.the result:{}",resContent);
						return false;
					}
					
					EntityUtils.consume(resEntity);
					long end=System.currentTimeMillis();
					LOGGER.debug("http executing send metric:{} recordNum:{} bytes size:{} succ spendMills:{}",this.metric,recordNum,dataSize,(end-begin));
					return true;
					
				} finally {
					if(response!=null) {
						response.close();
					}
					
				}
			} finally {
				httpclient.close();
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage()+" resContent:"+resContent, e);
		}
//		finally{
//			long end=System.currentTimeMillis();
//			LOGGER.info("http executing send metric:{} bytes size:{} finished spendMills:{}",this.metric,reqDatas.length,(end-begin));
//		}
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		Constants.init("recording-template.properties");
		BytesList batchMetrics=new BytesList();
		String gameId="1";
		String clientId="2";
		String ds="20181218";
		
		for (int i = 0; i < 160; i++) {
			Dau dau=new Dau();
			dau.setClientId(clientId);
			dau.setAffiliate("affiliate_"+i);
			dau.setCreative("creative_"+i);
			dau.setDauDate("20181218");
			dau.setDauTime("10:52:32");
			dau.setFamily("famility_"+i);
			dau.setFromUid("f_u_id-"+i);
			dau.setGenus("genus_"+i);
			
			MetaExtraBuilder metaExtraBuilder=new MetaExtraBuilder();
			metaExtraBuilder.addExtra("ip", NetUtils.getLocalMachineIp());
			metaExtraBuilder.addExtra("pid", i);
			
			byte[] metricKey=MetricEntityConverterManager.keyProtobufBytes("dau", "1", gameId, ds,metaExtraBuilder.getExtra());
			
			KVEntity kvEntity=new KVEntity(metricKey, new DauConverter(dau).toByteArray());
			
			batchMetrics.add(kvEntity.toByteArray());
			System.out.println(JSONUtils.toJSON(dau));
		}
		MetricsTransporterConfig metricsTransporterConfig=MetricsTransporterConfig.getInstance();
		MetricsInnerTransporterHttpProcesser metricsTransporterHttpProcesser=new MetricsInnerTransporterHttpProcesser("dau",batchMetrics,metricsTransporterConfig);
		metricsTransporterHttpProcesser.doTransport();
		
		Thread.sleep(10*60*1000L);
		
	}

}
