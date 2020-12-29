package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.AdTracking;
import org.corps.bi.metrics.Meta;

public class PartitionAdTracking extends AbstractPartitionMetric<AdTracking>{
	
	public PartitionAdTracking() {
		super(new Meta(), new AdTracking());
	}

	public PartitionAdTracking(Meta partition, AdTracking entity) {
		super(partition, entity);
	}

	@Override
	public String getEventTimestampStr() {
		AdTracking entity=this.getEntity();
		String dateStr=entity.getTrackingDs();
		String timeStr=entity.getActTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}

	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		AdTracking entity=this.getEntity();
		entity.setTrackingDs(dateStr);
		entity.setActTime(timeStr);
	}

	
	
}
