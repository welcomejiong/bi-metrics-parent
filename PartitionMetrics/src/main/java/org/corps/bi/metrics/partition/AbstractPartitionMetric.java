package org.corps.bi.metrics.partition;

import java.sql.Timestamp;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.corps.bi.metrics.AbstractMetric;
import org.corps.bi.metrics.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class  AbstractPartitionMetric<T extends AbstractMetric> {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(AbstractPartitionMetric.class);
	
	public static final String DATE_FORMAT_DAY_SECONDS="yyyy-MM-dd HH:mm:ss";
	
	public static final String DATE_FORMAT_DAY="yyyy-MM-dd";
	
	public static final String DATE_FORMAT_SECONDS="HH:mm:ss";

	private Meta meta;
	
	private T entity;
	
	public AbstractPartitionMetric() {
		super();
	}

	public AbstractPartitionMetric(Meta meta, T entity) {
		super();
		this.meta = meta;
		this.entity = entity;
	}


	public String getSnId() {
		return this.meta.getSnId();
	}


	public void setSnId(String snId) {
		this.meta.setSnId(snId);
	}


	public String getGameId() {
		return this.meta.getGameId();
	}


	public void setGameId(String gameId) {
		this.meta.setGameId(gameId);
	}


	public String getDs() {
		return this.meta.getDs();
	}


	public void setDs(String ds) {
		this.meta.setDs(ds);
	}

	public  Timestamp getEventTimestamp() {
		try {
			String eventTimeStr=this.getEventTimestampStr();
			if(StringUtils.isEmpty(eventTimeStr)){
				return new Timestamp(System.currentTimeMillis());
			}
			return new Timestamp(DateUtils.parseDate(eventTimeStr, DATE_FORMAT_DAY_SECONDS).getTime());
		} catch (ParseException e) {
			LOGGER.error("parse metric:{} snid:{},gameid:{},ds:{},clientid:{},userid:{} event timestamp meet error! eventTimeStr:{} return now date",this.meta.getMetric(),this.getSnId(),this.getGameId(),this.getDs(),this.getUserId(),this.getEventTimestampStr());
			return new Timestamp(System.currentTimeMillis());
		}
	}
	
	protected abstract String getEventTimestampStr();
	
	public void setEventTimestamp(Timestamp eventTimestamp) {
		if(eventTimestamp==null) {
			eventTimestamp = new Timestamp(System.currentTimeMillis());
		}
		
		String dateStr= DateFormatUtils.format(eventTimestamp, DATE_FORMAT_DAY);
		String timeStr= DateFormatUtils.format(eventTimestamp, DATE_FORMAT_SECONDS);
		
		this.setEventTimestampStr(dateStr, timeStr);
	}
	
	protected abstract void setEventTimestampStr(String dateStr,String timeStr);

	public String getClientId() {
		return this.entity.getClientId();
	}

	public void setClientId(String clientId) {
		this.entity.setClientId(clientId);
	}

	public String getUserId() {
		return this.entity.getUserId();
	}

	public void setUserId(String userId) {
		this.entity.setUserId(userId);
	}

	public int getVersion() {
		return this.entity.getVersion();
	}

	public void setVersion(int version) {
		this.entity.setVersion(version);
	}

	public String getUdid() {
		return this.entity.getUdid();
	}

	public void setUdid(String udid) {
		this.entity.setUdid(udid);
	}

	public String getRoleid() {
		return this.entity.getRoleid();
	}

	public void setRoleid(String roleid) {
		this.entity.setRoleid(roleid);
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}
	
	

}
