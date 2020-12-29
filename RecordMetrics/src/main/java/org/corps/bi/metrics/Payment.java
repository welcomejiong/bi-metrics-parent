package org.corps.bi.metrics;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.utils.DateUtils;
import org.corps.bi.utils.Utils;

/**
 * 
 * Payment区别于economy,是记录游戏中用户花费真实货币的购买行为的类。
 * <br/>
 * 手游扩展分类extra:<br/>
 * 页游："user_level:用户充值时等级,actorid:充值的角色id"<br/>
 * 手游："udid:安卓IMEI码/IOS IDFA(与gameinfo中userid对应）,
		user_level:用户等级,
		phone_type:手机型号,
		phone_version:操作系统版本号(安卓为内核版本),
		ratio:分辨率,
		download_from:渠道来源,
		actorid:角色id"

 */
@SuppressWarnings({ "rawtypes", "serial" })
public class Payment extends AbstractMetric implements IMetric{

	/**
	 * 支付的金额
	 */
	private String amount;

	/**
	 * 支付的货币类型
	 */
	private String currency;

	/**
	 * 交易平台
	 */
	private String provider;

	/**
	 * ip地址
	 */
	private String ip;

	/**
	 * 交易流水号
	 */
	private String transactionid;

	/**
	 * 支付的状态
	 */
	private String status;

	private String kingdom;

	private String phylum;

	private String value2;

	private String paymentDate;

	private String paymentTime;
	
	public Payment() {
		super();
	}

	private void init() {
		this.provider = "";
		this.ip = "";
		this.kingdom = "";
		this.phylum = "";
		this.value2 = "";
	}
	
	/**
	 * 获取付费的日期，格式(yyyy-MM-dd)
	 * @return
	 */
	public String getPaymentDate() {
		return paymentDate;
	}

	/**
	 * 设置付费的日期，格式(yyyy-MM-dd)
	 * @param paymentDate
	 */
	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	/**
	 * 获取付费的时间，格式(HH:mm:ss)
	 * @return
	 */
	public String getPaymentTime() {
		return paymentTime;
	}

	/**
	 * 设置付费的时间，格式(HH:mm:ss)
	 * @param paymentTime
	 */
	public void setPaymentTime(String paymentTime) {
		this.paymentTime = paymentTime;
	}

	/**
	 * 获取付费金额
	 * 付费金额（包含抵扣券玩家实际支付金额，单位统一）
	 * @return 金额
	 */
	public String getAmount() {
		return amount;
	}

	/**
	 * 设置付费金额
	 * 付费金额（包含抵扣券玩家实际支付金额，单位统一）
	 * @param amount
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}

	/**
	 * 获取支付的货币类型
	 * 货币名如：RMB、USD等
	 * @return
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * 设置支付的货币类型
	 * 货币名如：RMB、USD等
	 * @param currency
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * 获取用户ip
	 * @return ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * 设置用户ip
	 * @param ip
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * 获取1级分类
	 * 页游：buy_yb，手游：null
	 * @return
	 */
	public String getKingdom() {
		return kingdom;
	}

	/**
	 * 设置1级分类
	 * 页游：buy_yb，手游：null
	 * @param kingdom
	 */
	public void setKingdom(String kingdom) {
		this.kingdom = kingdom;
	}

	/**
	 * 获取2级分类
	 * 页游：null，手游：抵扣券消耗金额，没有可不报
	 * @return
	 */
	public String getPhylum() {
		return phylum;
	}

	/**
	 * 设置2级分类
	 * 页游：null，手游：抵扣券消耗金额，没有可不报
	 * @param phylum
	 */
	public void setPhylum(String phylum) {
		this.phylum = phylum;
	}

	/**
	 * 获取交易提供商(支付宝、微信支付等)
	 * @return
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * 设置交易提供商(支付宝、微信支付等)
	 * @param provider
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	/**
	 * 获取支付的状态
	 * success/fail(一般只报送成功充值数据，失败的数据项目组可选是否报送)
	 * @return
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * 设置支付的状态
	 * success/fail(一般只报送成功充值数据，失败的数据项目组可选是否报送)
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 获取交易流水号
	 * @return
	 */
	public String getTransactionid() {
		return transactionid;
	}

	/**
	 * 设置交易流水号
	 * @param transactionid
	 */
	public void setTransactionid(String transactionid) {
		this.transactionid = transactionid;
	}

	/**
	 * 获取购买元宝的数量
	 * @return
	 */
	public String getValue2() {
		return value2;
	}

	/**
	 * 设置购买元宝的数量
	 * @param value2
	 */
	public void setValue2(String value2) {
		this.value2 = value2;
	}

	@Override
	public String prepareForDB() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getUserId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.getClientId());
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.amount);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.currency);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.provider);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.ip);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(Utils.char_replace(this.transactionid));
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.status);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.kingdom);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.phylum);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.value2);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.paymentDate);
		buf.append(AbstractMetric.FIELD_SEPARATOR);
		buf.append(this.paymentTime);
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
		this.paymentDate = (DateUtils.verifyDate(paymentDate));
		this.paymentTime = (DateUtils.verifyTime(paymentTime));
	}

	@Override
	public String metric() {
		return "payment";
	}
}
