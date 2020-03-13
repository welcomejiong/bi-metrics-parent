package org.corps.bi.metrics;

import java.util.LinkedList;
import java.util.List;

import org.corps.bi.utils.DateUtils;


public class CustomMetric extends AbstractMetric implements IMetric{
	
	/**
	 */
	private static final long serialVersionUID = 8033743379319578295L;
	
	private String customMetircName;
	
	private String metricDate;

	private String metricTime;
	
	private  FieldOrderList fieldOrderList;

	public CustomMetric() {
		super();
	}

	public CustomMetric(String customMetirc,FieldOrderList fieldOrderList) {
		this.customMetircName = customMetirc;
		this.fieldOrderList=fieldOrderList;
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

	@Override
	public String metric() {
		return this.customMetircName;
	}

	@Override
	public String prepareForDB() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getUserId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.getClientId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		if(this.fieldOrderList!=null){
//			for (String fieldName : this.fieldOrderList.getFieldList()) {
//				Object fieldVal=this.get(fieldName);
//				buf.append(fieldVal!=null?fieldVal:"");
//				buf.append(AbstractMetric.FIELD_SEPARATOR);
//			}
		}
		return buf.toString();
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
		this.metricDate = (DateUtils.verifyDate(metricDate));
		this.metricTime = (DateUtils.verifyTime(metricTime));
	}
	
	public static class FieldOrderList{
		
		private final List<String> fieldList=new LinkedList<String>();

		public FieldOrderList() {
			super();
		}

		public void addField(int order,String fieldName){
			if(this.fieldList.get(order)!=null){
				throw new RuntimeException("the position "+ order +" has already ele. ");
			}
			this.fieldList.add(order, fieldName);
		}
		
		public void addField(String fieldName){
			this.fieldList.add(fieldName);
		}

		public List<String> getFieldList() {
			return fieldList;
		}
		
		
		
	}

}
