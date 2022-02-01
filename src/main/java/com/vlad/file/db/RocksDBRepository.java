package com.vlad.file.db;

import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Optional;


public class RocksDBRepository{

    private RocksDB db;

    public RocksDBRepository(String dbname) {
        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true) //если базы нет, создается новая
                .setCompressionType(CompressionType.SNAPPY_COMPRESSION) //обычная компрессия
                .setIncreaseParallelism(8)
                .setEnablePipelinedWrite(true)
                .setWriteBufferSize(64 * 1024 * 1024 * 10);

       // .setAllowConcurrentMemtableWrite(true); //азрешаем паралельки

        File baseDir = new File(System.getProperty("user.dir") + "/databases", dbname);
        try {
            Files.createDirectories(baseDir.getParentFile().toPath());
            Files.createDirectories(baseDir.getAbsoluteFile().toPath());
            db = RocksDB.open(options, baseDir.getAbsolutePath());
            System.out.println("db initialized");
        } catch(IOException | RocksDBException e) {
            System.out.printf("Error initializng. Exception: '%s', message: '%s', %s", e.getCause(), e.getMessage(), e);
        }
    }

    public synchronized boolean save(String key, String value) {
        System.out.println("Save files");
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
        System.out.printf("По ключу %s нашли %s%n", key, value);
        return value;
    }

    public synchronized boolean delete(String key) {
        System.out.println("Удаляем ключ с базы");
        try {
            db.delete(key.getBytes());
        } catch (RocksDBException e) {
            System.out.println("Пиздец, нихуя не удалилось");
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public void print() {
        RocksIterator iter = db.newIterator();
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println("iterator key:" + new String(iter.key()) + ", iter value:" + new String(iter.value()));
        }
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
        db.close();
    }

    public static void main(String[] args) throws RocksDBException {
        RocksDBRepository database = new RocksDBRepository("gnomik");

        database.save("kaka", "baka1");
        database.save("kaka22", "baka1");
        database.save("kaka23", "baka1");
        System.out.println(database.find("kak"));
        database.print();
        System.out.println(database.countLines());
    }
}
