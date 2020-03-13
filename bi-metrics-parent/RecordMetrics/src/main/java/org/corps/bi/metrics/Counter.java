package org.corps.bi.metrics;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.utils.DateUtils;
import org.corps.bi.utils.Utils;

/**
 * Counter是针对游戏中用户实际操作的记录，是万能的记录工具。
 * 通过Counter打点记录,BI可以获取在此打点步骤的人数以及用户行为，且通过人数增减与其他类的合并分析，可以分析出用户行为方向。
 * 
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class Counter extends AbstractMetric implements IMetric{

	private String userLevel;
	private String counter;
	private String value;
	private String kingdom;
	private String phylum;
	private String classfield;
	private String family;
	private String genus;
	private String counterDate;
	private String counterTime;
	
	public Counter() {
		super();
	}
	
	private void init(){
		this.value = "";
		this.kingdom = "";
		this.phylum = "";
		this.classfield = "";
		this.family = "";
		this.genus = "";
	}

	/**
	 * 获取用户被记录时的等级
	 * @return 用户等级
	 */
	public String getUserLevel() {
		return userLevel;
	}

	/**
	 * 设置用户被记录时的等级
	 * @param userLevel 用户等级
	 */
	public void setUserLevel(String userLevel) {
		this.userLevel = userLevel;
	}

	/**
	 * 获取记录的日期，格式(yyyy-MM-dd)
	 * @return 日期字符串
	 */
	public String getCounterDate() {
		return counterDate;
	}

	/**
	 * 设置记录的日期，格式(yyyy-MM-dd)
	 * @param counterDate 日期字符串
	 */
	public void setCounterDate(String counterDate) {
		this.counterDate = counterDate;
	}

	/**
	 * 获取记录的时间，格式(yyyy-MM-dd)
	 * @return 时间字符串
	 */
	public String getCounterTime() {
		return counterTime;
	}

	/**
	 * 设置记录的时间，格式(yyyy-MM-dd)
	 * @param counterDate 时间字符串
	 */
	public void setCounterTime(String counterTime) {
		this.counterTime = counterTime;
	}

	/**
	 * 获取counter类目相对应的值，示例：<BR/>
	 * 1.手游<BR/>
	 * 1.1 counter-任务：可为null<BR/>
	 * 1.2 counter-其他：物品产出和消耗的数量<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：角色ID<BR/>
	 * 2.2 counter-其他：任务、副本等可为null或者是物品产出和消耗的数量<BR/>
	 * 
	 * @return counter类目相对应的值
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 设置counter类目相对应的值，示例：<BR/>
	 * 1.手游<BR/>
	 * 1.1 counter-任务：可为null<BR/>
	 * 1.2 counter-其他：物品产出和消耗的数量<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：角色ID<BR/>
	 * 2.2 counter-其他：任务、副本等可为null或者是物品产出和消耗的数量<BR/>
	 * 
	 * @return counter类目相对应的值
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 获取counter类目：示例：<BR/>
	 * 1.手游<BR/>
	 * 1.1 counter-任务：mission等<BR/>
	 * 1.2 counter-其他：item等<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：role_choice等<BR/>
	 * 2.2 counter-其他：mission(任务)、fuben(副本)、item(物品)等<BR/>
	 * @return counter类目
	 */
	public String getCounter() {
		return counter;
	}

	/**
	 * 设置counter类目：示例：<BR/>
	 * 1.手游<BR/>
	 * 1.1 counter-任务：mission等<BR/>
	 * 1.2 counter-其他：item等<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：role_choice等<BR/>
	 * 2.2 counter-其他：mission(任务)、fuben(副本)、item(物品)等<BR/>
	 * @param counter  counter类目
	 */
	public void setCounter(String counter) {
		this.counter = counter;
	}

	/**
	 * 获取一级分类，示例：<BR/>
	 * 1.手游<BR/>
	 * 1.1 counter-任务：任务类型(日常任务daily/主线任务main等)<BR/>
	 * 1.2 counter-其他：inflow/outflow等<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：null<BR/>
	 * 2.2 counter-其他：main/sub/daily_mission(任务)、pvp/pve(副本)、inflow/outflow(物品)等<BR/>
	 * @return 一级分类
	 */
	public String getKingdom() {
		return kingdom;
	}

	/**
	 * 设置一级分类，示例：<BR/>
	 * 1.手游<BR/>
	 * 1.1 counter-任务：任务类型(日常任务daily/主线任务main等)<BR/>
	 * 1.2 counter-其他：inflow/outflow等<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：null<BR/>
	 * 2.2 counter-其他：main/sub/daily_mission(任务)、pvp/pve(副本)、inflow/outflow(物品)等<BR/>
	 * @param kingdom 一级分类
	 */
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	/**
	 * 获取二级分类，示例：<BR/>
	 * * 1.手游<BR/>
	 * 1.1 counter-任务：start/end等<BR/>
	 * 1.2 counter-其他：产出/消耗途径(最细的途径)<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：null<BR/>
	 * 2.2 counter-其他：start/end(任务)、start/end(副本)、产出/消耗途径(最细的途径)(物品)等<BR/>
	 * @return 二级分类
	 */
	public String getPhylum() {
		return phylum;
	}

	/**
	 * 设置二级分类，示例：<BR/>
	 * * 1.手游<BR/>
	 * 1.1 counter-任务：start/end等<BR/>
	 * 1.2 counter-其他：产出/消耗途径(最细的途径)<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：null<BR/>
	 * 2.2 counter-其他：start/end(任务)、start/end(副本)、产出/消耗途径(最细的途径)(物品)等<BR/>
	 * @param phylum 二级分类
	 */
	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}

	/**
	 * 获取三级分类，示例：null<BR/>
	 * @return 三级分类
	 */
	public String getClassfield() {
		return classfield;
	}

	/**
	 * 设置三级分类，示例：null<BR/>
	 * @param classfield 三级分类
	 */
	public void setClassfield(String classfield) {
		this.classfield = classfield;
	}

	/**
	 * 获取四级分类，示例：<BR/>
	 * * 1.手游<BR/>
	 * 1.1 counter-任务：任务名等<BR/>
	 * 1.2 counter-其他：道具名等<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：游戏角色名<BR/>
	 * 2.2 counter-其他：任务名(任务)、副本名(副本)、道具名(物品)等<BR/>
	 * @return 四级分类
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * 设置四级分类，示例：<BR/>
	 * * 1.手游<BR/>
	 * 1.1 counter-任务：任务名等<BR/>
	 * 1.2 counter-其他：道具名等<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：游戏角色名<BR/>
	 * 2.2 counter-其他：任务名(任务)、副本名(副本)、道具名(物品)等<BR/>
	 * @param family 四级分类
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * 获取五级分类，示例：<BR/>
	 * * 1.手游<BR/>
	 * 1.1 counter-任务：任务ID等<BR/>
	 * 1.2 counter-其他：道具ID等<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：null<BR/>
	 * 2.2 counter-其他：任务ID(任务)、副本ID(副本)、道具ID(物品)等<BR/>
	 * @return 五级分类
	 */
	public String getGenus() {
		return genus;
	}

	/**
	 * 设置五级分类，示例：<BR/>
	 * * 1.手游<BR/>
	 * 1.1 counter-任务：任务ID等<BR/>
	 * 1.2 counter-其他：道具ID等<BR/>
	 * 2.页游<BR/>
	 * 2.1 counter-角色报送：null<BR/>
	 * 2.2 counter-其他：任务ID(任务)、副本ID(副本)、道具ID(物品)等<BR/>
	 * @param genus 五级分类
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
		buf.append(this.userLevel);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.counter));
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
		buf.append(this.counterDate);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.counterTime);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(super.getExtra());
		return buf.toString();
	}

	@Override
	public boolean checkBaseParmIsNull() {
		if(StringUtils.isEmpty(this.getUserId()) || StringUtils.isEmpty(counter)){
			return true;
		}
		return false;
	}

	@Override
	public void processDate() {
			this.setCounterDate(DateUtils.verifyDate(counterDate));
			this.setCounterTime(DateUtils.verifyTime(counterTime));
	}

	@Override
	public String metric() {
		return "counter";
	}
	
}
