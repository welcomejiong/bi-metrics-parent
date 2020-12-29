package org.corps.bi.monitor;

import java.util.concurrent.atomic.AtomicLong;

import org.corps.bi.core.Constants;

public class MonitorCounterMess {

	private AtomicLong count;

	private long createTimeMills;

	public MonitorCounterMess() {
		super();
		this.reset();
	}

	public MonitorCounterMess(long count, long createTimeMills) {
		super();
		this.count = new AtomicLong(count);
		this.createTimeMills = createTimeMills;
	}

	public long addAndGet(long delta) {
		return count.addAndGet(delta);
	}

	/**
	 * Atomically increments the current value for this key by one
	 *
	 * @param counter
	 *            The key for this metric
	 * @return The updated value for this key
	 */
	public long increment() {
		return count.incrementAndGet();
	}
	
	public long getCount(){
		return this.count.get();
	}

	public long getCreateTimeMills() {
		return createTimeMills;
	}

	public void setCreateTimeMills(long createTimeMills) {
		this.createTimeMills = createTimeMills;
	}

	public void setCount(AtomicLong count) {
		this.count = count;
	}

	/**
	 * 重置为0
	 */
	public void reset() {
		if (this.count == null) {
			this.count = new AtomicLong(0);
		} else {
			this.count.set(0);
		}
		this.createTimeMills = System.currentTimeMillis();
	}

	/**
	 * 是否过期，默认在内存中存放不超过一天
	 * 
	 * @return
	 */
	public boolean isExpire() {
		return System.currentTimeMillis() - this.createTimeMills >= Constants.ONE_DAY_MILLS;
	}
	
	public MonitorCounterMess deepClone(){
		MonitorCounterMess clone=new MonitorCounterMess(this.count.get(),this.createTimeMills);
		return clone;
	}
}