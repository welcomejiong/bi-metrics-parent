package org.corps.bi.datacenter.combine;

import java.util.List;
import java.util.Map;

import org.corps.bi.dao.rocksdb.CatMetricsInRocksdb;
import org.corps.bi.tools.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatRocksdbService {
	
	public static Logger LOGGER =LoggerFactory.getLogger(CatRocksdbService.class.getName());
	
	public void cat(String metric,String day) {
		
		Map<String,Object> messAll=CatMetricsInRocksdb.getInstance().cat(metric, day);
		LOGGER.info("cat metric:{} mess:{}",metric,JSONUtils.toJSON(messAll));
	}
	
	public void cat(String day) {
		List<Map<String,Object>> messAll=CatMetricsInRocksdb.getInstance().catAll(day);
		LOGGER.info("cat metric:{} mess:{}","all",JSONUtils.toJSON(messAll));
	}
	

}
