package org.corps.bi.metrics;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.utils.DateUtils;

/**
 * Economy记录了游戏中用户所有涉及虚拟货币的行为。
 * Economy可以为数据分析提供很好的数据支持，用于分析用户的经济行为和游戏的经济系统是否合理。
 * 
 * <br/>
 *  手游扩展分类extra: <br/>
 *  页游："user_level:用户级别,actorid：角色ID" <br/>
 *  手游："download_from:渠道ID,actorid:角色id,user_level:当前等级"
 *
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class Economy extends AbstractMetric implements IMetric{

	/**
	 * 游戏中的币种。
	 * 例如coin、gold、honor、qpoint等虚拟或可获取或可花费的货币或者类货币。
	 */
	private String currency;

	/**
	 * 用户花费的总数
	 */
	private String amount; 

	/**
	 * 用户购买物品的数量
	 */
	private String value;

	private String kingdom;

	private String phylum;

	private String classfield;

	private String family;

	private String genus;

	private String economyDate;

	private String economyTime;
	
	public Economy() {
		super();
	}

	private void init() {
		this.currency = "";
		this.amount = "";
		this.value = "";
		this.kingdom = "";
		this.phylum = "";
		this.classfield = "";
		this.family = "";
		this.genus = "";
	}

	/**
	 * 获取花费的货币数量
	 * @return
	 */
	public String getAmount() {
		return amount;
	}

	/**
	 * 设置花费的货币数量
	 * @param amount
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}

	/**
	 * 获取3级分类，系统或玩法次级分类
	 * @return
	 */
	public String getClassfield() {
		return classfield;
	}

	/**
	 * 设置3级分类，系统或玩法次级分类。示例：商城的限时抢购等
	 * @param classfield
	 */
	public void setClassfield(String classfield) {
		this.classfield = classfield;
	}

	/**
	 * 获取游戏中的币种，虚拟的或可获取或可花费的货币或者类货币。
	 * 例如：与RMB等价货币使用“yb”，
	 * 其他货币使用其他缩写：coin、gold、honor、qpoint等。
	 * @return
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * 设置游戏中的币种，虚拟的或可获取或可花费的货币或者类货币。
	 * 例如：与RMB等价货币使用“yb”，
	 * 其他货币使用其他缩写：coin、gold、honor、qpoint等。
	 * @param currency
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * 获取1级分类，示例：earning/expenditure
	 * @return
	 */
	public String getKingdom() {
		return kingdom;
	}

	/**
	 * 1级分类，示例：earning/expenditure
	 * @param kingdom
	 */
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	/**
	 * 获取2级分类，所在系统或玩法名称，比如商城、技能升级等。
	 * @return
	 */
	public String getPhylum() {
		return phylum;
	}

	/**
	 * 设置2级分类，所在系统或玩法名称，比如商城、技能升级等。
	 * @param phylum
	 */
	public void setPhylum(String phylum) {
		this.phylum = phylum;
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
	 * 获取5级分类，购买的道具ID
	 * @return
	 */
	public String getGenus() {
		return genus;
	}

	/**
	 * 设置5级分类，购买的道具ID
	 * @param genus
	 */
	public void setGenus(String genus) {
		this.genus = genus;
	}

	/**
	 * 获取货币兑换的道具数量
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 设置货币兑换的道具数量
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 获取购买日期，日期格式(yyyy-MM-dd)
	 * @return
	 */
	public String getEconomyDate() {
		return economyDate;
	}

	/**
	 * 设置购买日期，日期格式(yyyy-MM-dd)
	 * @param economyDate
	 */
	public void setEconomyDate(String economyDate) {
		this.economyDate = economyDate;
	}

	/**
	 * 获取购买时间，时间格式(HH:mm:ss)
	 * @return
	 */
	public String getEconomyTime() {
		return economyTime;
	}

	/**
	 * 设置购买时间，时间格式(HH:mm:ss)
	 * @param economyTime
	 */
	public void setEconomyTime(String economyTime) {
		this.economyTime = economyTime;
	}

	@Override
	public String prepareForDB() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getUserId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.getClientId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.currency);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.amount);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.value);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.kingdom);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.phylum);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.classfield);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.family);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.genus);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.economyDate);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.economyTime);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(super.getExtra());
		return buf.toString();
	}

	@Override
	public boolean checkBaseParmIsNull() {
		if(StringUtils.isEmpty(this.getUserId()) || StringUtils.isEmpty(amount)){
			return true;
		}
		return false;
	}

	@Override
	public void processDate() {
		this.setEconomyDate(DateUtils.verifyDate(economyDate));
		this.setEconomyTime(DateUtils.verifyTime(economyTime));
	}

	@Override
	public String metric() {
		return "economy";
	}

}
