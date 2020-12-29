package org.corps.bi.metrics;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.corps.bi.tools.util.JSONUtils;

@SuppressWarnings("serial")
public abstract class AbstractMetric{

	public static final String FIELD_SEPARATOR = "\t";
	
	private String clientId;
	
	private String userId;

	private Map<String, String> extraCache;
	
	/**
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
	private String extra = "";
	
	private int version;
	
	private String udid;
	
	private String roleid;
	
	public AbstractMetric() {
		super();
	}

	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * 获取用户ID 
	 * 
	 * @return userid 联运的游戏即为平台帐号，腾讯的游戏即为openid
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * 设置用户ID 
	 * 
	 * @param userId 联运的游戏即为平台帐号，腾讯的游戏即为openid
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * 
	 * @return 随机的uuid字符串
	 */
	public String sendKey() {
		UUID uuid = UUID.randomUUID();
		String result = uuid.toString();
		return result;
	}

	/**
	 * 通过键值的方式添加扩展分类
	 * 
	 * @param key 扩展分类名称
	 * @param value 扩展分类值
	 */
	public void addExtra(String key, String value) {
		try {
			if(this.extraCache==null){
				this.extraCache = new HashMap<String, String>();
			}
			if(value!=null&&value!=""){
				this.extraCache.put(key, URLEncoder.encode(value, "UTF-8"));
			}else{
				this.extraCache.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取扩展分类
	 * 
	 * @return json格式的extra数据
	 */
	public String getExtra() {
		if (this.extraCache==null || this.extraCache.isEmpty()) {
			return this.extra;
		}
		this.extra=JSONUtils.toJSON(this.extraCache);
		return this.extra;
	}
	
	/**
	 * 获取扩展分类中的某个分类
	 * @param key 分类的名称
	 * @return 如果分类存在，返回此分类的值；不存在，返回null
	 */
	public String findExtraValue(String key){
		try {
			if(this.extraCache != null){
				String v = this.extraCache.get(key);
				if(StringUtils.isEmpty(v)){
					return URLDecoder.decode(v, "UTF-8");
				}
				return v;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 设置扩展分类的值,不建议使用，
	 * 另有更安全的设置扩展分类的方法 {@linkplain org.corps.bi.metrics.AbstractMetric addExtra} 
	 * @param extra 类json格式的扩展分类数据，例如：download_from:qqHall,udid:1300110000,phone_type:XIAOMI 3
	 */
	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public String getRoleid() {
		return roleid;
	}

	public void setRoleid(String roleid) {
		this.roleid = roleid;
	}

	/**
	 * 去除字符串中的空格
	 * @param value
	 * @return 如果传入值为null，返回空字符串。如果不为null，去空格。
	 */
	public String cleanString(String value) {
		return value == null ? "" : value.trim();
	}
	
	/**
	 * 验证指标类中不能为空的字段
	 * @return 必填字段中有字段为null或者"",返回true；反之，返回false。
	 */
	public abstract boolean checkBaseParmIsNull();

	/**
	 * 验证指标类中传入的日期与时间。日期格式(yyyy-MM-dd),时间格式(HH:mm:ss)
	 * 如果传入的日期或者时间为null、""、格式不正确默认当前日期与时间。
	 */
	public abstract void processDate();
	
	
	/**
	 * 持久化指标类，转换成存储格式
	 * @param clientid 服务器ID
	 * @return 存储格式的字符串
	 */
	public abstract String prepareForDB();
	
	public byte[] persistentBody(){
		String body=this.prepareForDB();
		if(body==null){
			return null;
		}
		return body.getBytes(Charset.forName("UTF-8"));
	}

}
