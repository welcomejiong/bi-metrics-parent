package org.corps.bi.dao.rocksdb;

import java.util.Map;
import java.util.Map.Entry;

import org.corps.bi.dao.MetricDao;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public  class DefaultMetricRocksdbImpl implements MetricDao{
	
	public static Logger LOGGER =LoggerFactory.getLogger(DefaultMetricRocksdbImpl.class.getName());
	
	private final String metric;
	
	private final RocksDB rocksdb;
	
	private final ColumnFamilyHandle columnFamilyHandle;
	
	public DefaultMetricRocksdbImpl(String metric,RocksDB rocksdb,ColumnFamilyHandle columnFamilyHandle) {
		super();
		this.metric=metric;
		this.rocksdb = rocksdb;
		this.columnFamilyHandle=columnFamilyHandle;
	}

	@Override
	public void save(byte[] key, byte[] value) {
		try {
			this.rocksdb.put(this.columnFamilyHandle, key, value);
		} catch (RocksDBException e) {
			LOGGER.error(e.getMessage(),e);
		}
		
	}

	@Override
	public byte[] get(byte[] key) {
		try {
			return this.rocksdb.get(this.columnFamilyHandle, key);
		} catch (RocksDBException e) {
			LOGGER.error(e.getMessage(),e);
		}
		return null;
	}

	@Override
	public void del(byte[] key) {
		try {
			 this.rocksdb.delete(this.columnFamilyHandle, key);
		} catch (RocksDBException e) {
			LOGGER.error(e.getMessage(),e);
		}
		
	}


	@Override
	public void saveBatch(Map<byte[], byte[]> batchs) {
		if(batchs==null||batchs.isEmpty()) {
			return ;
		}
		final WriteBatch wb = new WriteBatch();
		try {
			for (Entry<byte[],byte[]> entry : batchs.entrySet()) {
				wb.put(this.columnFamilyHandle,entry.getKey(), entry.getValue());
			}
			this.rocksdb.write(new WriteOptions(), wb);
			// flush 会主动从memtable to sst ,这样会造成大量的小的sst和wal的log文件，因此关闭掉
			//this.rocksdb.flush(new FlushOptions(), this.columnFamilyHandle);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}finally {
			wb.close();
		}
		
	}

	

	
	
	

}
