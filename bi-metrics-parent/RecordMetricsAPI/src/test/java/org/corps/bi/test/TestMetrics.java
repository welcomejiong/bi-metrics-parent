package org.corps.bi.test;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.corps.bi.core.Constants;
import org.corps.bi.metrics.AdTracking;
import org.corps.bi.metrics.Counter;
import org.corps.bi.metrics.CustomBinaryBodyMetric;
import org.corps.bi.metrics.Dau;
import org.corps.bi.metrics.Economy;
import org.corps.bi.metrics.GameInfo;
import org.corps.bi.metrics.IMetric;
import org.corps.bi.metrics.Install;
import org.corps.bi.metrics.Milestone;
import org.corps.bi.metrics.Payment;
import org.corps.bi.services.RecordingServices;
import org.corps.bi.tools.util.JSONUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMetrics {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(TestMetrics.class);
	
	@Test
	public void test() {
		LOGGER.debug("test 1:{},2:{},3:{}",new Object[] {"one","two","three"});
	}
	
	@Test
	public void testDau() throws Exception {
		Constants.init("recording.properties");
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		Date now=new Date();
		String gameId="10";
		String ds=DateFormatUtils.format(now,"yyyy-MM-dd");
		for (int i = 0; i < 86; i++) {
			Dau dau=new Dau();
			dau.setClientId(i+"");
			dau.setAffiliate("affiliate_"+i);
			dau.setCreative("creative_"+i);
			dau.setDauDate(DateFormatUtils.format(DateUtils.addDays(now, i),"yyyy-MM-dd"));
			dau.setDauTime(DateFormatUtils.format(DateUtils.addDays(now, i),"hh:mm:ss"));
			dau.setFamily("family_"+i);
			dau.setFromUid("f_u_id-"+i);
			dau.setGenus("genus_"+i);
			dau.setIp("193.112.30.49");
			dau.setSource("source_"+i);
			dau.setUserId("userid_"+i);

			Map<String,String> extraMap=new HashMap<String,String>();
			extraMap.put("username", "username_"+i);
			extraMap.put("password", "password_"+i);
			dau.setExtra(JSONUtils.toJSON(extraMap));
			
			batchMetrics.add(dau);
			System.out.println(JSONUtils.toJSON(dau));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		
		Thread.sleep(10*60*1000L);
	}
	
	@Test
	public void testFixTimesDau() throws Exception {
		
		Constants.init("recording-template.properties");
		
		String gameId="12";
		
		int forTimes=7;
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		
		for (int j = 1; j <= forTimes; j++) {
			
			Date now=new Date();
			
			String time=DateFormatUtils.format(now,"HH:mm:ss");
			
			for (int i = 0; i < 70; i++) {
				
				String userId="userid_"+j+"_"+i;
				
				for (int q = 0; q < forTimes; q++) {
					//String time="14:30:20";
					
					String clientId=j+"";
					
					String ds=DateFormatUtils.format(DateUtils.addDays(now, -q),"yyyy-MM-dd");
					
					Random random=new Random();
					
					int rn=random.nextInt(i+1);
					
					Dau dau=new Dau();
					dau.setClientId(clientId);
					
					dau.setAffiliate("affiliate_"+(i*j)+"_"+rn);
					dau.setCreative("creative_"+(i*j)+"_"+rn);
					dau.setDauDate(ds);
					dau.setDauTime(time);
					dau.setFamily("family_"+(i*j)+"_"+rn);
					dau.setFromUid("f_u_id-"+(i*j)+"_"+rn);
					dau.setGenus("genus_"+(i*j)+"_"+rn);
					dau.setIp("193.112.30.49");
					dau.setSource("source_"+(i*j)+"_"+rn);
					dau.setUserId(userId);

					Map<String,String> extraMap=new HashMap<String,String>();
					extraMap.put("username", "username_"+(i*j)+"_"+rn);
					extraMap.put("password", "password_"+(i*j)+"_"+rn);
					dau.setExtra(JSONUtils.toJSON(extraMap));
					
					recordingServices.add(gameId,ds,dau);
					
					LOGGER.info(JSONUtils.toJSON(dau));
				}
				
			}
			
			
			
			Thread.sleep(2000L);
		}
		
		Thread.sleep(60*1000L);
	}
	
	@Test
	public void testContinueTimesDau() throws Exception {
		int i=10000;
		do {
			this.testFixTimesDau();
			i--;
		}while(i>=0);
	}
	
	
	@Test
	public void testInstall() throws Exception {
		Constants.init("recording-template.properties");
		
		Date now=new Date();
		String gameId="12";
		for(int j=1;j<=7;j++) {
			List<IMetric> batchMetrics=new ArrayList<IMetric>();
			String ds=DateFormatUtils.format(DateUtils.addDays(now, -j),"yyyy-MM-dd");
			String time=DateFormatUtils.format(now,"HH:mm:ss");
			for (int i = 0; i < 100; i++) {
				Install entity=new Install();
				entity.setClientId(i+"");
				
				entity.setAffiliate("affiliate_"+i);
				entity.setCreative("100reative_"+i);
				entity.setInstallDate(ds);
				entity.setInstallTime(time);
				entity.setFamily("family_"+i);
				entity.setFromUid("f_u_id-"+i);
				entity.setGenus("genus_"+i);
				entity.setSource("source_"+i);
				entity.setUserId("userid_"+j+"_"+i);

				Map<String,String> extraMap=new HashMap<String,String>();
				extraMap.put("username", "username_"+i);
				extraMap.put("password", "password_"+i);
				entity.setExtra(JSONUtils.toJSON(extraMap));
				
				batchMetrics.add(entity);
				System.out.println(JSONUtils.toJSON(entity));
			}
			
			RecordingServices recordingServices=RecordingServices.getInstance();
			recordingServices.add(gameId,ds,batchMetrics);
			Thread.sleep(2*1000L);
		}
		
		
		Thread.sleep(10*60*1000L);
	}
	
	@Test
	public void testCounter() throws Exception {
		Constants.init("recording.properties");
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		Date now=new Date();
		String gameId="10";
		String ds=DateFormatUtils.format(now,"yyyy-MM-dd");
		for (int i = 0; i < 10; i++) {
			Counter entity=new Counter();
			entity.setClientId(i+"");
			
			entity.setCounter("counter_"+i);
			entity.setValue("value_"+i);
			entity.setKingdom("kingdom_"+i);
			entity.setPhylum("phylum_"+i);
			entity.setClassfield("classfield_"+i);
			
			entity.setCounterDate(DateFormatUtils.format(DateUtils.addDays(now, i),"yyyy-MM-dd"));
			entity.setCounterTime(DateFormatUtils.format(DateUtils.addDays(now, i),"hh:mm:ss"));
			entity.setFamily("family_"+i);
			entity.setGenus("genus_"+i);
			entity.setUserId("userid_"+i);

			Map<String,String> extraMap=new HashMap<String,String>();
			extraMap.put("username", "username_"+i);
			extraMap.put("password", "password_"+i);
			entity.setExtra(JSONUtils.toJSON(extraMap));
			
			batchMetrics.add(entity);
			System.out.println(JSONUtils.toJSON(entity));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		
		Thread.sleep(10*60*1000L);
	}
	
	@Test
	public void testEconomy() throws Exception {
		Constants.init("recording-template.properties");
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		Date now=new Date();
		String gameId="10";
		String ds=DateFormatUtils.format(now,"yyyy-MM-dd");
		for (int i = 0; i < 10; i++) {
			Economy entity=new Economy();
			entity.setClientId(i+"");
			entity.setCurrency("currency_"+i);
			entity.setAmount(i*8+"");
			entity.setValue("value_"+i);
			entity.setKingdom("kingdom_"+i);
			entity.setPhylum("phylum_"+i);
			entity.setClassfield("classfield_"+i);
			
			entity.setEconomyDate(DateFormatUtils.format(DateUtils.addDays(now, i),"yyyy-MM-dd"));
			entity.setEconomyTime(DateFormatUtils.format(DateUtils.addDays(now, i),"hh:mm:ss"));
			entity.setFamily("family_"+i);
			entity.setGenus("genus_"+i);
			entity.setUserId("userid_"+i);

			Map<String,String> extraMap=new HashMap<String,String>();
			extraMap.put("username", "username_"+i);
			extraMap.put("password", "password_"+i);
			entity.setExtra(JSONUtils.toJSON(extraMap));
			
			batchMetrics.add(entity);
			System.out.println(JSONUtils.toJSON(entity));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		
		Thread.sleep(10*60*1000L);
	}
	
	@Test
	public void testGameInfo() throws Exception {
		Constants.init("recording-template.properties");
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		Date now=new Date();
		String gameId="10";
		String ds=DateFormatUtils.format(now,"yyyy-MM-dd");
		for (int i = 0; i < 10; i++) {
			GameInfo entity=new GameInfo();
			entity.setClientId(i+"");
			entity.setUserLevel(i*6+"");
			entity.setGameinfo("gameinfo_"+i);
			entity.setValue("value_"+i);
			entity.setKingdom("kingdom_"+i);
			entity.setPhylum("phylum_"+i);
			entity.setClassfield("classfield_"+i);
			
			entity.setGameinfoDate(DateFormatUtils.format(DateUtils.addDays(now, i),"yyyy-MM-dd"));
			entity.setGameinfoTime(DateFormatUtils.format(DateUtils.addDays(now, i),"hh:mm:ss"));
			entity.setFamily("family_"+i);
			entity.setGenus("genus_"+i);
			entity.setUserId("userid_"+i);

			Map<String,String> extraMap=new HashMap<String,String>();
			extraMap.put("username", "username_"+i);
			extraMap.put("password", "password_"+i);
			entity.setExtra(JSONUtils.toJSON(extraMap));
			
			batchMetrics.add(entity);
			System.out.println(JSONUtils.toJSON(entity));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		
		Thread.sleep(10*60*1000L);
	}

	@Test
	public void testMilestone() throws Exception {
		Constants.init("recording-template.properties");
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		Date now=new Date();
		String gameId="10";
		String ds=DateFormatUtils.format(now,"yyyy-MM-dd");
		for (int i = 0; i < 10; i++) {
			Milestone entity=new Milestone();
			entity.setClientId(i+"");
			entity.setMilestone("milestone_"+i);
			entity.setValue("value_"+i);
			
			entity.setMilestoneDate(DateFormatUtils.format(DateUtils.addDays(now, i),"yyyy-MM-dd"));
			entity.setMilestoneTime(DateFormatUtils.format(DateUtils.addDays(now, i),"hh:mm:ss"));
			entity.setUserId("userid_"+i);

			Map<String,String> extraMap=new HashMap<String,String>();
			extraMap.put("username", "username_"+i);
			extraMap.put("password", "password_"+i);
			entity.setExtra(JSONUtils.toJSON(extraMap));
			
			batchMetrics.add(entity);
			System.out.println(JSONUtils.toJSON(entity));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		
		Thread.sleep(10*60*1000L);
	}
	
	@Test
	public void testPayment() throws Exception {
		Constants.init("recording-template.properties");
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		Date now=new Date();
		String gameId="12";
		//String ds=DateFormatUtils.format(now,"yyyy-MM-dd");
		//String paymentTime=DateFormatUtils.format(now,"HH:mm:ss");
		String ds="2019-07-09";
		String paymentTime="7:00:00";
		for (int i = 0; i < 10; i++) {
			Payment entity=new Payment();
			entity.setClientId(i+"");
			entity.setCurrency("RMB");
			entity.setAmount(i*8+"");
			entity.setProvider("tencent_"+i);
			entity.setKingdom("kingdom_"+i);
			entity.setPhylum("phylum_"+i);
			
			entity.setPaymentDate(ds);
			entity.setPaymentTime(paymentTime);
			
			entity.setUserId("userid_"+i);
			
			entity.setIp("192.168.0.112");
			entity.setTransactionid("tran_"+i);
			entity.setStatus("paying");
			entity.setValue2(""+i);
			

			Map<String,String> extraMap=new HashMap<String,String>();
			extraMap.put("username", "username_"+i);
			extraMap.put("password", "password_"+i);
			entity.setExtra(JSONUtils.toJSON(extraMap));
			
			batchMetrics.add(entity);
			System.out.println(JSONUtils.toJSON(entity));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		
		Thread.sleep(10*60*1000L);
	}
	
	@Test
	public void testAdTracking() throws Exception {
		Constants.init("recording-template.properties");
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		Date now=new Date();
		String gameId="10";
		String ds=DateFormatUtils.format(now,"yyyy-MM-dd");
		for (int i = 0; i < 10; i++) {
			AdTracking entity=new AdTracking();
			entity.setClientId(i+"");
			
			entity.setAppkey("appkey_"+i);
			entity.setMac("mac_"+i);
			entity.setMacMd5("macMd5_"+i);
			entity.setIfa("ifa_"+i);
			entity.setIfaMd5("ifamd5_"+i);
			entity.setUuid("uuid_"+i);
			entity.setActType(i);
			entity.setActTime(org.corps.bi.utils.DateUtils.formatSeconds(now));
			entity.setPf("pf_"+i);
			entity.setUserAgent("userAgent_"+i);
			
			entity.setUserId("userid_"+i);
			
			entity.setIp("192.168.0.112");

			Map<String,String> extraMap=new HashMap<String,String>();
			extraMap.put("username", "username_"+i);
			extraMap.put("password", "password_"+i);
			entity.setExtra(JSONUtils.toJSON(extraMap));
			
			batchMetrics.add(entity);
			System.out.println(JSONUtils.toJSON(entity));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		
		Thread.sleep(10*60*1000L);
	}
	
	@Test
	public void testCustomBinaryBodyMetric() throws Exception {
		Constants.init("recording-template.properties");
		List<IMetric> batchMetrics=new ArrayList<IMetric>();
		Date now=new Date();
		String gameId="10";
		String ds=DateFormatUtils.format(now,"yyyy-MM-dd");
		for (int i = 0; i < 10; i++) {
			CustomBinaryBodyMetric entity=new CustomBinaryBodyMetric();
			entity.setClientId(i+"");
			
			entity.setUserId("userid_"+i);
			entity.setCustomMetircName("custombinarybodymetric");
			entity.setBody(("jiong_"+i).getBytes());
			entity.setMetricDate(DateFormatUtils.format(DateUtils.addDays(now, i),"yyyy-MM-dd"));
			entity.setMetricTime(DateFormatUtils.format(DateUtils.addDays(now, i),"HH:mm:ss"));

			Map<String,String> extraMap=new HashMap<String,String>();
			extraMap.put("username", "username_"+i);
			extraMap.put("password", "password_"+i);
			entity.setExtra(JSONUtils.toJSON(extraMap));
			
			batchMetrics.add(entity);
			System.out.println(JSONUtils.toJSON(entity));
		}
		
		RecordingServices recordingServices=RecordingServices.getInstance();
		recordingServices.add(gameId,ds,batchMetrics);
		
		Thread.sleep(10*60*1000L);
	}

}
