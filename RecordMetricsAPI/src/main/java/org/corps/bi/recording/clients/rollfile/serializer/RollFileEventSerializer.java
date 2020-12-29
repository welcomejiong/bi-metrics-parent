package org.corps.bi.recording.clients.rollfile.serializer;

import org.corps.bi.recording.clients.rollfile.RollFileEvent;

public interface RollFileEventSerializer {
	
	/**
	 * 500毫秒执行一次，如果上次500毫秒没有执行完毕，下次500毫秒又开始执行，会造成写入文件混乱的情况
	 * @param batchSize
	 */
	public void persistent(int batchSize);
	
	public boolean send(RollFileEvent rollFileEvent);
	
	public void flush();
	
	public void destory();
	
	/**
	 * 通知需要切换文件
	 * @return
	 */
	public boolean rotate();
	/**
	 * 修改不需奥切换文件
	 * @return
	 */
	public boolean unRotate();
	
	/**
	 * 是否过期，默认在内存中存放不超过一天
	 * @return
	 */
	public boolean isExpire();
	
	
	/**
	 * 是否正在持久化
	 * @return
	 */
	public boolean isPersistenting();

}
