package org.corps.bi.dao.rocksdb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.corps.bi.core.Constants;
import org.corps.bi.datacenter.core.DataCenterTopics;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.util.SizeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksdbManager {
	
	public static Logger LOGGER =LoggerFactory.getLogger(RocksdbManager.class.getName());
	
	private static final RocksdbManager INSTANCE=new RocksdbManager();
	
	private final RocksDB rocksdb;
	
	private final Map<String,ColumnFamilyHandle> columnFamilyHandleMap=new HashMap<String,ColumnFamilyHandle>();

	private RocksdbManager() {
		super();
		this.rocksdb=this.initRocksDB();
	}
	
	private RocksDB initRocksDB() {
		try {
			
			this.checkRocksdbDataDir(Constants.ROCKSDB_DB_PATH);
			
			this.checkRocksdbDataDir(Constants.ROCKSDB_DB_WALDIR);
			
			//this.checkRocksdbDataDir(Constants.ROCKSDB_DB_LOGDIR);
			
			final List<ColumnFamilyDescriptor> columnFamilyDescriptors =
			        new ArrayList<ColumnFamilyDescriptor>();
			columnFamilyDescriptors.add(new ColumnFamilyDescriptor(
			        RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
			
			for (DataCenterTopics dataCenterTopic : DataCenterTopics.values()) {
				String cfName=dataCenterTopic.getMetric();
				columnFamilyDescriptors.add(new ColumnFamilyDescriptor(
						cfName.getBytes(), new ColumnFamilyOptions()));
			}
			
			final List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<ColumnFamilyHandle>();
			final DBOptions options = new DBOptions();
			options.setCreateIfMissing(true);
			options.setCreateMissingColumnFamilies(true);
			// 触发落盘
			options.setMaxTotalWalSize(2*SizeUnit.GB);
			// 触发清除落盘的key
			options.setWalSizeLimitMB(6000);
			options.setMaxLogFileSize(200*SizeUnit.MB);
			options.setKeepLogFileNum(10);
			options.setDbLogDir(Constants.ROCKSDB_DB_LOGDIR);
			options.setWalDir(Constants.ROCKSDB_DB_WALDIR);
			//options.setAvoidFlushDuringRecovery(true);
			RocksDB db = null;
			if(Constants.ROCKSDB_DB_IS_READONLY) {
				db = RocksDB.openReadOnly(options, Constants.ROCKSDB_DB_PATH, columnFamilyDescriptors, columnFamilyHandles);
			}else{
				db = RocksDB.open(options, Constants.ROCKSDB_DB_PATH,columnFamilyDescriptors, columnFamilyHandles);
			}
			//RocksdbLogger rocksdbLogger=new RocksdbLogger(options);
			
			for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
				columnFamilyHandleMap.put(new String(columnFamilyHandle.getName()), columnFamilyHandle);
			}
			return db;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}
	
	private void checkRocksdbDataDir(String path) {
		File tmpFile=new File(path);
		if(tmpFile.exists()) {
			return ;
		}
		tmpFile.mkdirs();
	}
	
	private void createNewColumnFamily(List<String> cfNameList) {
		try {
			if(cfNameList==null||cfNameList.isEmpty()) {
				return ;
			}
			RocksDB db =null;
			try {
				final Options options = new Options().setCreateIfMissing(true);
				db = RocksDB.open(options, Constants.ROCKSDB_DB_PATH);
				for (String cfName : cfNameList) {
					ColumnFamilyHandle columnFamilyHandle = db
							.createColumnFamily(new ColumnFamilyDescriptor(cfName.getBytes(), new ColumnFamilyOptions()));
					columnFamilyHandle.close();
				} 
			} finally {
				if(db!=null) {
					db.close();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	public RocksDB getRocksdb() {
		return rocksdb;
	}
	
	public ColumnFamilyHandle getColumnFamilyHandle(String  cfName) {
		return this.columnFamilyHandleMap.get(cfName);
	}
	
	public void close() {
		try {
			for (Entry<String,ColumnFamilyHandle> entry : this.columnFamilyHandleMap.entrySet()) {
				this.closeColumnFamilyHandle(entry.getValue());
			}
			this.rocksdb.syncWal();
			this.rocksdb.close();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	public void closeColumnFamilyHandle(ColumnFamilyHandle columnFamilyHandle) {
		try {
			columnFamilyHandle.close();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.close();
	}

	public static RocksdbManager getInstance() {
		return INSTANCE;
	}
	
	
	

}
