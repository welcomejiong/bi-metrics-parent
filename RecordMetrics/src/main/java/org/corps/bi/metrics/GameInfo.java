package org.corps.bi.metrics;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.utils.DateUtils;
import org.corps.bi.utils.Utils;

/**
 * 主要记录游戏运行的相关信息，比如检查前期流失点、运行报错信息、卸载等等。
 * 目前手游应用。<br/>
 * 报送扩展分类extra:<br/>
 * 前期:"download_from:渠道来源"<br/>
 * error:"client_version：客户端版本，phone_type：手机型号，phone_version：操作系统版本号(安卓为内核版本)，
		ratio：分辨率,service:网络服务商,download_from:渠道来源(游戏包ID)"<br/>
 * uninstall:"download_from:渠道来源,actorid:角色ID"
 * 
 * 手游扩展分类extra:"udid:安卓IMEI码/IOS IDFA(与gameinfo中userid对应）,
		client_version:客户端版本,
		phone_type:手机型号,
		phone_version:操作系统版本号(安卓为内核版本),
		ratio:分辨率,
		service:网络服务商(联通、移动、电信等),
		download_from:渠道来源(游戏包ID)
		os_type:操作系统（eg：ANDROID/IOS)
		network_type:4G/WIFI
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class GameInfo extends AbstractMetric implements IMetric{

	private String userLevel;
	private String gameinfo;
	private String value;
	private String kingdom;
	private String phylum;
	private String classfield;
	private String family;
	private String genus;
	private String gameinfoDate;
	private String gameinfoTime;
	
	public GameInfo() {
		super();
	}

	/**
	 * 获取记录的信息<br/>
	 * 前期：early等<br/>
	 * 其他：error、uninstall等
	 * @return
	 */
	public String getGameinfo() {
		return gameinfo;
	}

	/**
	 * 设置记录的信息<br/>
	 * 前期：early等<br/>
	 * 其他：error、uninstall等
	 * @param gameinfo
	 */
	public void setGameinfo(String gameinfo) {
		this.gameinfo = gameinfo;
	}

	/**
	 * 获取游戏记录相对应的值
	 * 前期：累计到此步骤的次数等<br/>
	 * 其他：error:报错内容、uninstall:null等
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 设置游戏记录相对应的值<br/>
	 * 前期：累计到此步骤的次数等<br/>
	 * 其他：error:报错内容、uninstall:null等
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 获取1级分类<br/>
	 * 前期：步骤名称，实例：打开应用：open_app；热更版本检查：update_check；热更成功：update_check；
	 * 进入登陆页面：login_page；点击注册：register；点击一键注册：fat_register；点击登陆：login；
	 * 进入选服页面：server_choice；选角色：actor_choice等<br/>
	 * 注意：玩家还没创号完成未知服务器id时，clientid=0<br/>
	 * 其他：null
	 * @return
	 */
	public String getKingdom() {
		return kingdom;
	}

	/**
	 * 设置1级分类<br/>
	 * 前期：步骤名称，实例：打开应用：open_app；热更版本检查：update_check；热更成功：update_check；
	 * 进入登陆页面：login_page；点击注册：register；点击一键注册：fat_register；点击登陆：login；
	 * 进入选服页面：server_choice；选角色：actor_choice等<br/>
	 * 注意：玩家还没创号完成未知服务器id时，clientid=0<br/>
	 * 其他：null
	 * @param kingdom
	 */
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	/**
	 * 获取2级分类<br/>
	 * 前期：步骤顺序编号<br/>
	 * 其他：null
	 * @return
	 */
	public String getPhylum() {
		return phylum;
	}

	/**
	 * 设置2级分类<br/>
	 * 前期：步骤顺序编号<br/>
	 * 其他：null
	 * @param phylum
	 */
	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}

	/**
	 * 获取3级分类<br/>
	 * 前期：步骤完成状态等，示例：update_check执行步骤与状态：start、ok/fail等<br/>
	 * 其他：null
	 * @return
	 */
	public String getClassfield() {
		return classfield;
	}

	/**
	 * 设置3级分类<br/>
	 * 前期：步骤完成状态等，示例：update_check执行步骤与状态：start、ok/fail等<br/>
	 * 其他：null
	 * @param classfield
	 */
	public void setClassfield(String classfield) {
		this.classfield = classfield;
	}

	/**
	 * 获取4级分类，没有可不报送
	 * @return
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * 设置4级分类，没有可不报送
	 * @param family
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * 获取5级分类，没有可不报送
	 * @return
	 */
	public String getGenus() {
		return genus;
	}

	/**
	 * 设置5级分类，没有可不报送
	 * @param genus
	 */
	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(String userLevel) {
		this.userLevel = userLevel;
	}

	/**
	 * 获取信息记录日期，日期格式(yyyy-MM-dd)
	 * @return
	 */
	public String getGameinfoDate() {
		return gameinfoDate;
	}

	/**
	 * 设置信息记录日期，日期格式(yyyy-MM-dd)
	 * @param gameinfoDate
	 */
	public void setGameinfoDate(String gameinfoDate) {
		this.gameinfoDate = gameinfoDate;
	}

	/**
	 * 获取信息记录时间，时间格式(HH:mm:ss)
	 * @return
	 */
	public String getGameinfoTime() {
		return gameinfoTime;
	}

	/**
	 * 设置信息记录时间，时间格式(HH:mm:ss)
	 * @param gameinfoTime
	 */
	public void setGameinfoTime(String gameinfoTime) {
		this.gameinfoTime = gameinfoTime;
	}

	@Override
	public String prepareForDB() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getUserId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.getClientId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.userLevel);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.gameinfo));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.value);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.kingdom));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.phylum));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.classfield));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.family));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.genus));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.gameinfoDate);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.gameinfoTime);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(super.getExtra());
		return buf.toString();
	}

	@Override
	public boolean checkBaseParmIsNull() {
		if(StringUtils.isEmpty(this.getUserId()) || StringUtils.isEmpty(gameinfo)){
			return true;
		}
		return false;
	}

	@Override
	public void processDate() {
		this.setGameinfoDate(DateUtils.verifyDate(gameinfoDate));
		this.setGameinfoTime(DateUtils.verifyTime(gameinfoTime));
	}

	@Override
	public String metric() {
		return "gameinfo";
	}
}
