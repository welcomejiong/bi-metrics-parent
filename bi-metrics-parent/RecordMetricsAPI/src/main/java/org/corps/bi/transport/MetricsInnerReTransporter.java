package org.corps.bi.transport;

/**
 * 重新发送一遍数据
 */
public interface MetricsInnerReTransporter {
	
	public boolean reTransport();
	
	public boolean shutdown();
	
	boolean isTransporting();
	
	boolean isTransportEnd();
	
}
