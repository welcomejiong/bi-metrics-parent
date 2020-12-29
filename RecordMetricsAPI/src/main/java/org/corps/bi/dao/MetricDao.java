package org.corps.bi.dao;

import java.util.Map;

public interface MetricDao {
	
	void save(byte[] key,byte[] value);
	
	void saveBatch(Map<byte[],byte[]> batchs);
	
	byte[] get(byte[] key);
	
	void del(byte[] key);
	
}
