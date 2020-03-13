package org.corps.bi.metrics.partition;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.metrics.Meta;
import org.corps.bi.metrics.Payment;

public class PartitionPayment extends AbstractPartitionMetric<Payment>{
	
	public PartitionPayment() {
		super(new Meta(), new Payment());
	}

	public PartitionPayment(Meta partition, Payment entity) {
		super(partition, entity);
	}
	
	@Override
	public String getEventTimestampStr() {
		Payment entity=this.getEntity();
		String dateStr=entity.getPaymentDate();
		String timeStr=entity.getPaymentTime();
		if(StringUtils.isEmpty(dateStr)||StringUtils.isEmpty(timeStr)) {
			return null;
		}
		String timeStampStr=dateStr+" "+timeStr;
		return timeStampStr;
	}
	
	@Override
	protected void setEventTimestampStr(String dateStr, String timeStr) {
		Payment entity=this.getEntity();
		entity.setPaymentDate(dateStr);
		entity.setPaymentTime(timeStr);
	}

	
	
}
