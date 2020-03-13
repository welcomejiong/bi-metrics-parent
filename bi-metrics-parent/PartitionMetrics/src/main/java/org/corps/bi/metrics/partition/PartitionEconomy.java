package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.Economy;
import org.corps.bi.metrics.Meta;

public class PartitionEconomy extends AbstractPartitionMetric<Economy>{
	
	public PartitionEconomy() {
		super(new Meta(), new Economy());
	}

	public PartitionEconomy(Meta partition, Economy entity) {
		super(partition, entity);
	}
	
	@Override
	public String getEventTimestampStr() {
		Economy entity=this.getEntity();
		String dateStr=entity.getEconomyDate();
		String timeStr=entity.getEconomyTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}
	
	
	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		Economy entity=this.getEntity();
		entity.setEconomyDate(dateStr);
		entity.setEconomyTime(timeStr);
	}

	
}
