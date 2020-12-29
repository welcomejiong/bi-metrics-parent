package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.Meta;
import org.corps.bi.metrics.Milestone;

public class PartitionMilestone extends AbstractPartitionMetric<Milestone>{
	
	public PartitionMilestone() {
		super(new Meta(), new Milestone());
	}

	public PartitionMilestone(Meta partition, Milestone entity) {
		super(partition, entity);
	}
	
	@Override
	public String getEventTimestampStr() {
		Milestone entity=this.getEntity();
		String dateStr=entity.getMilestoneDate();
		String timeStr=entity.getMilestoneTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}
	
	
	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		Milestone entity=this.getEntity();
		entity.setMilestoneDate(dateStr);
		entity.setMilestoneTime(timeStr);
	}

	
}
