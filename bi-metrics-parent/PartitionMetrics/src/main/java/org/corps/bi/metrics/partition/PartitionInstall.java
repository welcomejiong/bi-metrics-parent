package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.Install;
import org.corps.bi.metrics.Meta;

public class PartitionInstall extends AbstractPartitionMetric<Install>{
	
	public PartitionInstall() {
		super(new Meta(), new Install());
	}

	public PartitionInstall(Meta partition, Install entity) {
		super(partition, entity);
	}
	
	@Override
	public String getEventTimestampStr() {
		Install entity=this.getEntity();
		String dateStr=entity.getInstallDate();
		String timeStr=entity.getInstallTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}
	
	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		Install entity=this.getEntity();
		entity.setInstallDate(dateStr);
		entity.setInstallTime(timeStr);
	}

	
	
}
