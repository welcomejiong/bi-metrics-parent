package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.Counter;
import org.corps.bi.metrics.Meta;

public class PartitionCounter extends AbstractPartitionMetric<Counter>{
	
	public PartitionCounter() {
		super(new Meta(), new Counter());
	}

	public PartitionCounter(Meta partition, Counter entity) {
		super(partition, entity);
	}
	
	@Override
	public String getEventTimestampStr() {
		Counter entity=this.getEntity();
		String dateStr=entity.getCounterDate();
		String timeStr=entity.getCounterTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}
	
	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		Counter entity=this.getEntity();
		entity.setCounterDate(dateStr);
		entity.setCounterTime(timeStr);
	}

	
}
