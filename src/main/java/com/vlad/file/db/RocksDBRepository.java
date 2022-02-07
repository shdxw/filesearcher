package com.vlad.file.db;

import org.apache.commons.io.FileUtils;
import org.rocksdb.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;


public class RocksDBRepository {

    private RocksDB db;
    WriteOptions writeOptions;
    Options options;


    public RocksDBRepository(String dbname, boolean open) {
        writeOptions = new WriteOptions()
                .setSync(false)
                .setDisableWAL(true);

        Statistics stat = new Statistics();
        options = new Options();
        options.setCreateIfMissing(true) //если базы нет, создается новая
                .setCompactionStyle(CompactionStyle.LEVEL)
                .setCompressionType(CompressionType.SNAPPY_COMPRESSION) //обычная компрессия
                .setAllowMmapReads(false)//хуета из бенча
                .setMaxWriteBufferNumber(2)
                .setUseFsync(false)
                .setIncreaseParallelism(12)//разрешаем паралельки
                .setMaxBackgroundJobs(12)
                .setEnablePipelinedWrite(true)
                .setWriteBufferSize(640 * 1024 * 1024)
                .setStatistics(stat);
//                .prepareForBulkLoad()
//                .setStatsDumpPeriodSec(10);



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
        db.write(writeOptions, batch);
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

    public Options getOptions() {
        return options;
    }

    private static long saveFiles(File tempfile, String database) {

        long detectLogin = 0;

        RocksDBRepository db = new RocksDBRepository(database, true);//TODO: improve

//        long time = System.nanoTime();

        Options options = db.getOptions();
        try (Scanner reader = new Scanner(tempfile)) {

            //SstFileWriter sstFileWriter = new SstFileWriter(new EnvOptions(), options);
            //String way = System.getProperty("user.dir") + "\\temp.sst";
            //File te = new File(way);
            //te.createNewFile();
            long count = 0;
            while (reader.hasNextLine()) {
                WriteBatch batch = new WriteBatch();
//                Map<String, String> data = new TreeMap<>();
//                sstFileWriter.open(te.getAbsolutePath());
                while (reader.hasNextLine() && count <= 25_000_000) {
                    String[] line = reader.nextLine().split(":");
//                    data.put(line[0], line[1]);
                    //sstFileWriter.put(new Slice(line[0].getBytes()), new Slice(line[1].getBytes()));
                    batch.put(line[0].getBytes(), line[1].getBytes());
                    detectLogin++;
                    count++;
                }
//                for (String key : data.keySet()) {
//                    sstFileWriter.put(new Slice(key.getBytes()), new Slice(data.get(key).getBytes()));
//                }
                count = 0;
//                sstFileWriter.finish();
//                te.delete();
//                db.writeBatch(batch);
            }
        } catch (IOException | RocksDBException e) {
            e.printStackTrace();
        }
        db.close();
//        time = System.nanoTime() - time;

//        System.out.printf("Выполнено за %.2f sec\n%n", time / 1_000_000.0 / 1000);
        return detectLogin;
    }

    public static void main(String[] args) throws IOException {
        RocksDB.loadLibrary();
        RocksDBRepository.saveFiles(new File("C:\\Users\\egork\\Desktop\\bases\\m.txt"),
                "mail");
    }
}
