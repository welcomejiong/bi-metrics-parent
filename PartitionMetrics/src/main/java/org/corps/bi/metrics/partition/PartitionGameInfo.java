package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.GameInfo;
import org.corps.bi.metrics.Meta;

public class PartitionGameInfo extends AbstractPartitionMetric<GameInfo>{
	
	public PartitionGameInfo() {
		super(new Meta(), new GameInfo());
	}

	public PartitionGameInfo(Meta partition, GameInfo entity) {
		super(partition, entity);
	}
	
	@Override
	public String getEventTimestampStr() {
		GameInfo entity=this.getEntity();
		String dateStr=entity.getGameinfoDate();
		String timeStr=entity.getGameinfoTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}
	
	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		GameInfo entity=this.getEntity();
		entity.setGameinfoDate(dateStr);
		entity.setGameinfoTime(timeStr);
	}

	
	
}
