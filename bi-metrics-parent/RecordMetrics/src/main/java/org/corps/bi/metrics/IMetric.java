package org.corps.bi.metrics;

public interface IMetric {
	
	/**
	 * 获取指标英文代称
	 * @return 指标名称
	 */
	public String metric();
	
	/**
	 * 持久化指标类，转换成存储格式
	 * @param clientid 服务器ID
	 * @return 存储格式的字符串
	 */
	public byte[] persistentBody();

}
