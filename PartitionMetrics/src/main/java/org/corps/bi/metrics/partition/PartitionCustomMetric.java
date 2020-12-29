package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.CustomMetric;
import org.corps.bi.metrics.Meta;

public class PartitionCustomMetric extends AbstractPartitionMetric<CustomMetric>{
	
	public PartitionCustomMetric() {
		super(new Meta(), new CustomMetric());
	}

	public PartitionCustomMetric(Meta partition, CustomMetric entity) {
		super(partition, entity);
	}
	
	@Override
	public String getEventTimestampStr() {
		CustomMetric entity=this.getEntity();
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
		CustomMetric entity=this.getEntity();
		entity.setMetricDate(dateStr);
		entity.setMetricTime(timeStr);
	}

	
	
}
