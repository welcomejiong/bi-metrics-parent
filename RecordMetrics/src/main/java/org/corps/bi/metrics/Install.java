package org.corps.bi.metrics;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.utils.DateUtils;
import org.corps.bi.utils.Utils;

/**
 * 
 * 记录每一个新安装用户，每一个用户一条记录,评测每日新进用户数量，质量等。
 * 注意：
 * 	1.当用户首次登陆，报送install，同时要报送dau，因为新安装用户也是活跃用户。
 * 	2.用户在多个服注册的时候，要每个服都报送install。
 * 	3.一个服可以创建多个角色的情况，也只需报送1条install数据。
 * 
 *  <br/>
 * 手游扩展分类extra:"udid:安卓IMEI码/IOS IDFA(与gameinfo中userid对应）,
		client_version:客户端版本,
		phone_type:手机型号,
		phone_version:操作系统版本号(安卓为内核版本),
		ratio:分辨率,
		service:网络服务商(联通、移动、电信等),
		download_from:渠道来源(游戏包ID)
		os_type:操作系统（eg：ANDROID/IOS)
		network_type:4G/WIFI
 * 
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class Install extends AbstractMetric implements IMetric{

	/**
	 * 页游为空，手游可设置为：android/ios/win
	 */
	private String source;

	/**
	 * 设置使用网络
	 * 页游为空，手游：wifi/2g/3g/4g
	 */
	private String affiliate;

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

	private String fromUid;

	private String installDate;

	private String installTime;
	
	public Install() {
		super();
	}

	private void init() {
		this.source = "";
		this.affiliate = "";
		this.creative = "";
		this.family = "";
		this.genus = "";
		this.fromUid = "";
	}
	
	
	/**
	 * 获取邀请者或者病毒发送者的ID
	 * 页游：如果当前记录的用户是通过病毒渠道安装的，则可以取到From uid，也就是邀请者，或者病毒消息发送者的ID
	 * 手游：null
	 * @return String userid
	 */
	public String getFromUid() {
		return fromUid;
	}

	/**
	 * 获取邀请者或者病毒发送者的ID
	 * 页游：如果当前记录的用户是通过病毒渠道安装的，则可以取到From uid，也就是邀请者，或者病毒消息发送者的ID
	 * 手游：null
	 * @param fromUid
	 */
	public void setFromUid(String fromUid) {
		this.fromUid = fromUid;
	}

	/**
	 * 获取安装日期，格式(yyyy-MM-dd)
	 * @return
	 */
	public String getInstallDate() {
		return installDate;
	}

	/**
	 * 设置安装日期，格式(yyyy-MM-dd)
	 * @param installDate
	 */
	public void setInstallDate(String installDate) {
		this.installDate = installDate;
	}

	/**
	 * 获取安装时间，格式(HH:mm:ss)
	 * @return
	 */
	public String getInstallTime() {
		return installTime;
	}

	/**
	 * 设置安装时间，格式(HH:mm:ss)
	 * @return
	 */
	public void setInstallTime(String installTime) {
		this.installTime = installTime;
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
	 * 页游为空，手游：wifi/2g/3g/4g
	 * @return
	 */
	public String getAffiliate() {
		return affiliate;
	}

	/**
	 * 设置使用网络
	 * 页游为空，手游：wifi/2g/3g/4g
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
		buf.append(this.fromUid);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.installDate);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.installTime);
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
		this.setInstallDate(DateUtils.verifyDate(installDate));
		this.setInstallTime(DateUtils.verifyTime(installTime));
	}

	@Override
	public String metric() {
		return "install";
	}
}
