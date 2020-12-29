package org.corps.bi.metrics;

@SuppressWarnings("serial")
public class CustomBinaryBodyMetric extends AbstractMetric implements IMetric{
	
	private String customMetircName;
	
	private String metricDate;

	private String metricTime;
	
	private byte[] body;
	
	public CustomBinaryBodyMetric() {
		super();
	}
	
	public CustomBinaryBodyMetric( String customMetircName, byte[] body) {
		this.customMetircName = customMetircName;
		this.body = body;
	}

	public String getMetricDate() {
		return metricDate;
	}

	public void setMetricDate(String metricDate) {
		this.metricDate = metricDate;
	}

	public String getMetricTime() {
		return metricTime;
	}

	public void setMetricTime(String metricTime) {
		this.metricTime = metricTime;
	}
	
	public String getCustomMetircName() {
		return customMetircName;
	}

	public void setCustomMetircName(String customMetircName) {
		this.customMetircName = customMetircName;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	@Override
	public String metric() {
		return "custombinarybodymetric";
	}

	@Override
	public String prepareForDB() {
		return "";
	}
	
	@Override
	public byte[] persistentBody() {
		return this.body;
	}

	@Override
	public boolean checkBaseParmIsNull() {
		if(this.customMetircName!=null&&!"".equals(this.customMetircName)){
			return true;
		}
		return false;
	}

	@Override
	public void processDate() {
	}
	

}
