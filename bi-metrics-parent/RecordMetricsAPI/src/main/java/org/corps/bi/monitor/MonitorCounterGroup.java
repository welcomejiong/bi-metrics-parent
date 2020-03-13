package org.corps.bi.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MonitorCounterGroup {

	private static final Logger logger = LoggerFactory.getLogger(MonitorCounterGroup.class);

	// Key for component's start time in MonitoredCounterGroup.counterMap
	private static final String COUNTER_GROUP_START_TIME = "start.time";

	// key for component's stop time in MonitoredCounterGroup.counterMap
	private static final String COUNTER_GROUP_STOP_TIME = "stop.time";

	private final String type;
	private final String name;
	private final Map<String, MonitorCounterMess> counterMap;

	private AtomicLong startTime;
	private AtomicLong stopTime;

	protected MonitorCounterGroup(String type, String name) {
		this.type = type;
		this.name = name;

		this.counterMap = new ConcurrentHashMap<String, MonitorCounterMess>();

		startTime = new AtomicLong(0L);
		stopTime = new AtomicLong(0L);

	}

	/**
	 * Starts the component
	 *
	 * Initializes the values for the stop time as well as all the keys in the
	 * internal map to zero and sets the start time to the current time in
	 * milliseconds since midnight January 1, 1970 UTC
	 */
	public void start() {

		stopTime.set(0L);
		startTime.set(System.currentTimeMillis());
		logger.info("Component type:{}, name: {} started",type,name);
	}

	/**
	 * Shuts Down the Component
	 *
	 * Used to indicate that the component is shutting down.
	 *
	 * Sets the stop time and then prints out the metrics from the internal map
	 * of keys to values for the following components:
	 *
	 * - ChannelCounter - ChannelProcessorCounter - SinkCounter -
	 * SinkProcessorCounter - SourceCounter
	 */
	public void stop() {

		// Sets the stopTime for the component as the current time in
		// milliseconds
		stopTime.set(System.currentTimeMillis());

		// Prints out a message indicating that this component has been stopped
		logger.info("Component type: " + type + ", name: " + name + " stopped");

		// Retrieve the type for this counter group
		final String typePrefix = type.toLowerCase();

		// Print out the startTime for this component
		logger.info("Shutdown Metric for type: " + type + ", " + "name: " + name + ". " + typePrefix + "." + COUNTER_GROUP_START_TIME + " == " + startTime);

		// Print out the stopTime for this component
		logger.info("Shutdown Metric for type: " + type + ", " + "name: " + name + ". " + typePrefix + "." + COUNTER_GROUP_STOP_TIME + " == " + stopTime);

		// Retrieve and sort counter group map keys
		final List<String> mapKeys = new ArrayList<String>(counterMap.keySet());

		Collections.sort(mapKeys);

		// Cycle through and print out all the key value pairs in counterMap
		for (final String counterMapKey : mapKeys) {

			// Retrieves the value from the original counterMap.
			final long counterMapValue = get(counterMapKey);

			logger.info("Shutdown Metric for type: " + type + ", " + "name: " + name + ". " + counterMapKey + " == " + counterMapValue);
		}
	}

	/**
	 * Returns when this component was first started
	 *
	 * @return
	 */
	public long getStartTime() {
		return startTime.get();
	}

	/**
	 * Returns when this component was stopped
	 *
	 * @return
	 */
	public long getStopTime() {
		return stopTime.get();
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder(type).append(":");
		sb.append(name).append("{");
		boolean first = true;
		Iterator<String> counterIterator = counterMap.keySet().iterator();
		while (counterIterator.hasNext()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			String counterName = counterIterator.next();
			sb.append(counterName).append("=").append(get(counterName));
		}
		sb.append("}");

		return sb.toString();
	}
	
	/**
	 * 这个地方有可能有并发的问题()
	 * eg:
	 * 如果判断的这一刻过期了，正准备关闭序列化流，但是刚好又有数据匹配上
	 * 一般情况下，这种情况比较的少，除非发送的是昨天的数据（为了保证序列化是最新的，在获取序列化的时候，也做了一次校验操作）
	 * 因此需要同步来屏蔽这个问题
	 * @return
	 */
	private synchronized  boolean clearExpire(){
		try {
			List<String> removeWapperKeyList=new ArrayList<String>();
			for (Entry<String, MonitorCounterMess>  entry: this.counterMap.entrySet()) {
				MonitorCounterMess monitorCounterMess=entry.getValue();
				if(!monitorCounterMess.isExpire()){
					continue;
				}
				removeWapperKeyList.add(entry.getKey());
			}
			if(removeWapperKeyList.isEmpty()){
				return true;
			}
			
			for (String key : removeWapperKeyList) {
				this.counterMap.remove(key);
			}
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}
	
	private synchronized MonitorCounterMess getMonitorCounterMess(String counter) {
		MonitorCounterMess monitorCounterMess = null;
		if(this.counterMap.containsKey(counter)){
			monitorCounterMess =  this.counterMap.get(counter);
		}else{
			monitorCounterMess=new MonitorCounterMess();
			this.counterMap.put(counter, monitorCounterMess);
		}
		return monitorCounterMess;
	}
	
	/**
	 * 发送需要的信息
	 * @return
	 */
	public synchronized Map<String,MonitorCounterMess> prepareSendCurrentMonitorCounterMess(){
		Map<String,MonitorCounterMess> ret=new HashMap<String, MonitorCounterMess>();
		for (Entry<String,MonitorCounterMess> entry : this.counterMap.entrySet()) {
			MonitorCounterMess monitorCounterMess=entry.getValue();
			MonitorCounterMess cloneCounterMess=monitorCounterMess.deepClone();
			monitorCounterMess.reset();
			ret.put(entry.getKey(), cloneCounterMess);
		}
		// 清除过期的
		this.clearExpire();
		return ret;
	}

	/**
	 * Retrieves the current value for this key
	 *
	 * @param counter
	 *            The key for this metric
	 * @return The current value for this key
	 */
	public long get(String counter) {
		return this.getMonitorCounterMess(counter).getCount();
	}

	/**
	 * Atomically adds the delta to the current value for this key
	 *
	 * @param counter
	 *            The key for this metric
	 * @param delta
	 * @return The updated value for this key
	 */
	public long addAndGet(String counter, long delta) {
		return this.getMonitorCounterMess(counter).addAndGet(delta);
	}

	/**
	 * Atomically increments the current value for this key by one
	 *
	 * @param counter
	 *            The key for this metric
	 * @return The updated value for this key
	 */
	public long increment(String counter) {
		return this.getMonitorCounterMess(counter).increment();
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return name;
	}

}
