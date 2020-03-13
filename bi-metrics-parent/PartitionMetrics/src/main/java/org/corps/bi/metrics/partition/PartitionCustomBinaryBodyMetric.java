package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.CustomBinaryBodyMetric;
import org.corps.bi.metrics.Meta;

public class PartitionCustomBinaryBodyMetric extends AbstractPartitionMetric<CustomBinaryBodyMetric>{
	
	public PartitionCustomBinaryBodyMetric() {
		super(new Meta(), new CustomBinaryBodyMetric());
	}

	public PartitionCustomBinaryBodyMetric(Meta partition, CustomBinaryBodyMetric entity) {
		super(partition, entity);
	}
	
	@Override
	public String getEventTimestampStr() {
		CustomBinaryBodyMetric entity=this.getEntity();
		String dateStr=entity.getMetricDate();
		String timeStr=entity.getMetricTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}
	
	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		CustomBinaryBodyMetric entity=this.getEntity();
		entity.setMetricDate(dateStr);
		entity.setMetricTime(timeStr);
	}

	
	
}
