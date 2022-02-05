package com.vlad.file.db;

import org.apache.commons.io.FileUtils;
import org.rocksdb.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class RocksDBRepository {

    private RocksDB db;

    public RocksDBRepository(String dbname, boolean open) {
        Statistics stat = new Statistics();
        final Options options = new Options();
        options.setCreateIfMissing(true) //если базы нет, создается новая
                .setCompressionType(CompressionType.SNAPPY_COMPRESSION) //обычная компрессия
                .setIncreaseParallelism(12)
                .setMaxBackgroundJobs(12)
                .setEnablePipelinedWrite(true)
                .setWriteBufferSize(1000 * 1024 * 1024)
                .setStatistics(stat); //разрешаем паралельки


        Filter bloomFilter = new BloomFilter(15);
        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();

        tableConfig.setFilter(bloomFilter)//настройки для таблицы
                .optimizeFiltersForMemory();
        options.setTableFormatConfig(tableConfig);

        File baseDir = new File(System.getProperty("user.dir") + "/databases", dbname);
        try {
            Files.createDirectories(baseDir.getParentFile().toPath());
            Files.createDirectories(baseDir.getAbsoluteFile().toPath());
            if (open) {
                db = RocksDB.open(options, baseDir.getAbsolutePath());
            } else {
                db = RocksDB.openReadOnly(options, baseDir.getAbsolutePath());
            }

            System.out.println("db initialized " + dbname);
        } catch(IOException | RocksDBException e) {
            System.out.printf("Error initializng. Exception: '%s', message: '%s', %s", e.getCause(), e.getMessage(), e);
        }
    }

    public synchronized boolean save(String key, String value) {
        try {
            db.put(key.getBytes(), value.getBytes());
        } catch (RocksDBException e) {
            System.out.printf("Error saving entry. Cause: '%s', message: '%s'", e.getCause(), e.getMessage());
            return false;
        }
        return true;
    }

    public synchronized String find(String key) {
        String value = null;
        try {
            byte[] bytes = db.get(key.getBytes());
            if (bytes != null) value = new String(bytes);
        } catch (RocksDBException e) {
            System.out.printf(
                    "Error retrieving the entry with key: %s, cause: %s, message: %s",
                    key,
                    e.getCause(),
                    e.getMessage()
            );
        }
        return value;
    }

    public boolean isInBase(String key) {
        byte[] bytes = null;
        try {
            bytes = db.get(key.getBytes());
        } catch (RocksDBException e) {
            System.out.printf(
                    "Error retrieving the entry with key: %s, cause: %s, message: %s",
                    key,
                    e.getCause(),
                    e.getMessage()
            );
        }
        return bytes != null;
    }

    public void writeBatch(WriteBatch batch) throws RocksDBException {
        WriteOptions writeOpt = new WriteOptions();
        db.write(writeOpt, batch);
    }

    public synchronized boolean delete(String key) {
        try {
            db.delete(key.getBytes());
        } catch (RocksDBException e) {
            System.out.println("Пиздец, нихуя не удалилось");
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public RocksIterator print() {
        return db.newIterator();
    }

    public long countLines() {
        RocksIterator iter = db.newIterator();
        long count = 0;
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
           count++;
        }
        return count;
    }

    public void close() {
//        try {
//            System.out.println(db.getProperty("rocksdb.stats"));
//        } catch (RocksDBException e) {
//            e.printStackTrace();
//        } finally {
//            db.close();
//        }
        db.close();
    }

    public static void main(String[] args) throws IOException {

        FileUtils.forceDelete(new File("C:\\projects\\filecrack\\TEMP\\text16183689624325815467.temp"));
    }
}
