package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.Dau;
import org.corps.bi.metrics.Meta;

public class PartitionDau extends AbstractPartitionMetric<Dau>{
	
	public PartitionDau() {
		super(new Meta(), new Dau());
	}

	public PartitionDau(Meta partition, Dau entity) {
		super(partition, entity);
	}

	@Override
	public String getEventTimestampStr() {
		Dau entity=this.getEntity();
		String dateStr=entity.getDauDate();
		String timeStr=entity.getDauTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}

	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		Dau entity=this.getEntity();
		entity.setDauDate(dateStr);
		entity.setDauTime(timeStr);
	}

	
	
}
