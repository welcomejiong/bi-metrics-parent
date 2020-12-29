package org.corps.bi.dao.rocksdb;

import org.rocksdb.DBOptions;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Logger;
import org.slf4j.LoggerFactory;

public class RocksdbLogger extends Logger {
	
	public static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RocksdbLogger.class.getName());

	public RocksdbLogger(DBOptions dboptions) {
		super(dboptions);
	}

	@Override
	protected void log(InfoLogLevel infoLogLevel, String logMsg) {
		
		if(InfoLogLevel.ERROR_LEVEL.ordinal()==infoLogLevel.ordinal()) {
			LOGGER.error(logMsg);
		}else if(InfoLogLevel.WARN_LEVEL.ordinal()==infoLogLevel.ordinal()) {
			LOGGER.warn(logMsg);
		}else if(InfoLogLevel.INFO_LEVEL.ordinal()==infoLogLevel.ordinal()) {
			LOGGER.info(logMsg);
		}else if(InfoLogLevel.DEBUG_LEVEL.ordinal()==infoLogLevel.ordinal()) {
			LOGGER.debug(logMsg);
		}else{
			LOGGER.info(logMsg);
		}
	}

}
