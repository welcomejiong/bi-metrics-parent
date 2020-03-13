package org.corps.bi.dao.rocksdb;

import java.util.HashMap;
import java.util.Map;

import org.corps.bi.dao.MetricDao;

public enum MetricRocksdbColumnFamilys {
	
	DAU("dau"),
	
	INSTALL("install"),
	
	COUNTER("counter"),
	
	ECONOMY("economy"),
	
	GAMEINFO("gameinfo"),
	
	MILESTONE("milestone"),
	
	PAYMENT("payment"),
	
	ADTRACKING("adtracking"),
	
	CUSTOMBINARYBODYMETRIC("custombinarybodymetric");
	
	private static final Map<String,MetricRocksdbColumnFamilys> TOPIC_METRIC_MAP=new HashMap<String,MetricRocksdbColumnFamilys>();
	
	private final String metric;
	
	private final MetricDao metricDao;

	private MetricRocksdbColumnFamilys(String metric){
		this.metric = metric;
		this.metricDao=new DefaultMetricRocksdbImpl(metric,RocksdbManager.getInstance().getRocksdb(), RocksdbManager.getInstance().getColumnFamilyHandle(metric));
	}

	public String getMetric() {
		return metric;
	}
	
	public MetricDao getMetricDao() {
		return metricDao;
	}
	

	static {
		for (MetricRocksdbColumnFamilys dataCenterTopic : MetricRocksdbColumnFamilys.values()) {
			TOPIC_METRIC_MAP.put(dataCenterTopic.metric, dataCenterTopic);
		}
	}
	
	public static MetricRocksdbColumnFamilys parseFromName(String metric) {
		if(TOPIC_METRIC_MAP.containsKey(metric)) {
			return TOPIC_METRIC_MAP.get(metric);
		}
		return null;
	}
	
	

}
