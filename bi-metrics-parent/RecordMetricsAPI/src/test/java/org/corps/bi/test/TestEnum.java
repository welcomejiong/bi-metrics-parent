package org.corps.bi.test;

import org.corps.bi.dao.rocksdb.MetricRocksdbColumnFamilys;
import org.corps.bi.datacenter.core.DataCenterTopics;
import org.junit.Test;

public class TestEnum {
	

	@Test
	public void testDau() throws Exception {
		for (DataCenterTopics topic : DataCenterTopics.values()) {
			MetricRocksdbColumnFamilys family=MetricRocksdbColumnFamilys.parseFromName(topic.getMetric());
			System.out.println(family.getMetric()+":"+family.ordinal());
		}
	}

}
