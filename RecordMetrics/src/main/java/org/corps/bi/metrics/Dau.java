package org.corps.bi.metrics;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.utils.DateUtils;
import org.corps.bi.utils.Utils;

/**
 * DAU是记录用户每天登陆情况的类.
 * 接入DAU时要注意要按天去除重复，还有就是当用户被记录Install之后也要记录DAU，因为Install的用户在当天也是DAU用户。
 * 
 *  手游扩展分类extra:"udid:安卓IMEI码/IOS IDFA(与gameinfo中userid对应）,
		client_version:客户端版本,
		phone_type:手机型号,
		phone_version:操作系统版本号(安卓为内核版本),
		ratio:分辨率,
		service:网络服务商(联通、移动、电信等),
		download_from:渠道来源(游戏包ID)
		os_type:操作系统（eg：ANDROID/IOS)
		network_type:4G/WIFI
		"
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class Dau extends AbstractMetric implements IMetric{

	/**
	 * 获取系统源
	 * 页游为空，手游：android/ios/win
	 */
	private String source;

	/**
	 * 获取使用网络
	 * 页游为空，手游：null/wifi/2g/3g/4g
	 */
	private String affiliate;

	/**
	 * 记录用户来源渠道，例如用户或者是通过搜索进入游戏，或者是通过好友介绍进入游戏等。
	 * 渠道由游戏平台提供，例如腾讯平台通过参数via传递。
	 */
	/**
	 * 设置渠道来源
	 * 尽量取到最小渠道 如集市任务，粉钻等，不能取到最小渠道，可输入大的渠道，如qzone等或者游戏包ID
	 */
	private String creative;

	/**
	 * 获取4级分类，没有做分类可不传
	 * 页游为空，手游：角色ID
	 */
	private String family;

	/**
	 * 获取5级分类，没有做分类可不传
	 */
	private String genus;

	private String ip;

	private String dauDate;

	private String dauTime;

	/**
	 * 用户登陆进来如果是通过好友进入到游戏的，则通过from_uid记录这个用户好友的platformId。
	 */
	private String fromUid;
	
	/**
	 * extra
	 * json格式：
	 * mobileType:iphone9
	 * udid:设备唯一id（安卓：mac，IOS：idfa）
	 */
	
	public Dau() {
		super();
	}

	private void init() {
		this.source = "";
		this.affiliate = "";
		this.creative = "";
		this.family = "";
		this.genus = "";
		this.ip = "";
		this.fromUid = "";
	}

	/**
	 * 获取系统源
	 * 页游为空，手游：android/ios/win
	 * @return
	 */
	public String getSource() {
		return source;
	}

	/**
	 * 页游为空，手游可设置为：android/ios/win
	 * @param source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * 获取使用网络
	 * 页游为空，手游：null/wifi/2g/3g/4g
	 * @return
	 */
	public String getAffiliate() {
		return affiliate;
	}

	/**
	 * 设置使用网络
	 * 页游为空，手游：null/wifi/2g/3g/4g
	 * @param affiliate
	 */
	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	/**
	 * 获取渠道来源
	 * 尽量取到最小渠道 如集市任务，粉钻等，不能取到最小渠道，可输入大的渠道，如qzone等
	 * @return 渠道来源
	 */
	public String getCreative() {
		return creative;
	}

	/**
	 * 设置渠道来源
	 * 尽量取到最小渠道 如集市任务，粉钻等，不能取到最小渠道，可输入大的渠道，如qzone等或者游戏包ID
	 * @param creative
	 */
	public void setCreative(String creative) {
		this.creative = creative;
	}

	/**
	 * 获取4级分类，没有做分类可不传
	 * 页游为空，手游：角色ID
	 * @return
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * 获取4级分类，没有做分类可不传
	 * 页游为空，手游：角色ID
	 * @param family
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * 获取5级分类，没有做分类可不传
	 * @return
	 */
	public String getGenus() {
		return genus;
	}

	/**
	 * 设置5级分类，没有做分类可不传
	 * @param genus
	 */
	public void setGenus(String genus) {
		this.genus = genus;
	}

	/**
	 * 获取活跃的日期，格式(yyyy-MM-dd)
	 * @return
	 */
	public String getDauDate() {
		return dauDate;
	}

	/**
	 * 设置活跃的日期，格式(yyyy-MM-dd)
	 * @param dauDate
	 */
	public void setDauDate(String dauDate) {
		this.dauDate = dauDate;
	}

	/**
	 * 获取活跃的时间，格式(HH:mm:ss)
	 * @return
	 */
	public String getDauTime() {
		return dauTime;
	}

	/**
	 * 设置活跃的时间，格式(HH:mm:ss)
	 * @param dauTime
	 */
	public void setDauTime(String dauTime) {
		this.dauTime = dauTime;
	}

	public String getFromUid() {
		return fromUid;
	}

	public void setFromUid(String fromUid) {
		this.fromUid = fromUid;
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	@Override
	public String prepareForDB() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getUserId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.getClientId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.source));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.affiliate));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(Utils.CutDownCreative(this.creative)));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.family));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.genus));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		//buf.append(Utils.IP2Long(this.ip));
		buf.append(this.ip);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.dauDate);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.dauTime);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.fromUid);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(super.getExtra());
		return buf.toString();
	}

	@Override
	public boolean checkBaseParmIsNull() {
		if(StringUtils.isEmpty(this.getUserId())){
			return true;
		}
		return false;
	}

	@Override
	public void processDate() {
		this.setDauDate(DateUtils.verifyDate(dauDate));
		this.setDauTime(DateUtils.verifyTime(dauTime));
	}

	@Override
	public String metric() {
		return "dau";
	}


}
