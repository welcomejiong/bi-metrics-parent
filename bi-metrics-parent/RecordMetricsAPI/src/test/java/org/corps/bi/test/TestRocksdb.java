package org.corps.bi.test;
import java.util.List;
import java.util.Map;

import org.corps.bi.core.Constants;
import org.corps.bi.dao.rocksdb.CatMetricsInRocksdb;
import org.corps.bi.tools.util.JSONUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRocksdb {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(TestRocksdb.class);
	
	
	
	public TestRocksdb() {
		super();
		Constants.init("recording.properties");
	}



	@Test
	public void testMetric() {
		CatMetricsInRocksdb cat=CatMetricsInRocksdb.getInstance();
		Map<String,Object> mess=cat.cat("dau");
		System.out.println(JSONUtils.toJSON(mess));
	}
	
	@Test
	public void testAllMetric() {
		CatMetricsInRocksdb cat=CatMetricsInRocksdb.getInstance();
		List<Map<String,Object>> mess=cat.catAll();
		System.out.println(JSONUtils.toJSON(mess));
	}
	
	
}
