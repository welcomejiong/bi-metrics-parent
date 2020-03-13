package org.corps.bi.metrics;

import java.util.Date;

import org.corps.bi.utils.DateUtils;



@SuppressWarnings({ "rawtypes", "serial" })
public class AdTracking extends AbstractMetric implements IMetric{
	
    public AdTracking() {
		super();
	}

    private String appkey;

    private String mac;

    private String macMd5;

    private String ifa;

    private String ifaMd5;

    private String uuid;

    private Integer actType;

    private String actTime;

    private String pf;

    private String ip;

    private String userAgent;

    private String trackingDs;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getMacMd5() {
        return macMd5;
    }

    public void setMacMd5(String macMd5) {
        this.macMd5 = macMd5;
    }

    public String getIfa() {
        return ifa;
    }

    public void setIfa(String ifa) {
        this.ifa = ifa;
    }

    public String getIfaMd5() {
        return ifaMd5;
    }

    public void setIfaMd5(String ifaMd5) {
        this.ifaMd5 = ifaMd5;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getActType() {
        return actType;
    }

    public void setActType(Integer actType) {
        this.actType = actType;
    }

    public String getActTime() {
        return actTime;
    }

    public void setActTime(String actTime) {
        this.actTime = actTime;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }


	public String getTrackingDs() {
		return trackingDs;
	}

	public void setTrackingDs(String trackingDs) {
		this.trackingDs = trackingDs;
	}

	@Override
	public String prepareForDB() {
		StringBuilder buf = new StringBuilder();
		buf.append(cleanString(this.appkey));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.mac));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.macMd5));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.ifa));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.ifaMd5));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.uuid));
		buf.append(FIELD_SEPARATOR);
		buf.append(this.actType!=null?this.actType.intValue():"");
		buf.append(FIELD_SEPARATOR);
		buf.append(this.actTime!=null?this.actTime:DateUtils.formatSeconds(new Date()));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.pf));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.ip));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.userAgent));
		buf.append(FIELD_SEPARATOR);
		buf.append(cleanString(this.trackingDs));
		buf.append(FIELD_SEPARATOR);
		String jsonExtra = getExtra();
		buf.append(jsonExtra);
		return buf.toString();
	}

	@Override
	public String metric() {
		return "adtracking";
	}

	@Override
	public boolean checkBaseParmIsNull() {
		return false;
	}

	@Override
	public void processDate() {
	}
}