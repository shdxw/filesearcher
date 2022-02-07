package com.vlad.file;

import com.vlad.file.db.RocksDBRepository;
import org.apache.commons.io.FileUtils;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileLogic {
    private int lines;
    private List<Path> files;
    private boolean doubles;
    private boolean baseDoubles;
    private boolean domen;
    private boolean withoutDomen;
    private JProgressBar progressBar;
    // private final Charset CODE = StandardCharsets.UTF_8;
    private final Charset CODE = StandardCharsets.ISO_8859_1;
    private final int BUFFERSIZE = 1310720 * 2 * 2;
    private final File PLACE = new File(System.getProperty("user.dir") + "/TEMP");

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_STRING =
            Pattern.compile("^[A-Z0-9._%@$!#()*&^~/+-]+:[A-Z0-9._%@$!#()*&^~+/-]+$", Pattern.CASE_INSENSITIVE);

    public FileLogic(List<Path> files, boolean doubles, boolean baseDoubles, boolean domen, boolean withoutDomen) {
        this.files = files;
        this.doubles = doubles;
        this.baseDoubles = baseDoubles;
        this.domen = domen;
        this.withoutDomen = withoutDomen;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public long deleteBase(File tempfile, String pack, int init, int sum) throws IOException {
        File outputFile = File.createTempFile("text2", ".temp", PLACE);
        outputFile.createNewFile();
        RocksDBRepository db = new RocksDBRepository(pack, false);

        long detect = db.countLines(); // всего в базе

        AtomicInteger count = new AtomicInteger();

        try (BufferedReader reader = new BufferedReader(new FileReader(tempfile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                count.getAndIncrement();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int newCount = count.get() / sum; //сумма за процент
        count.set(0);

        try (Stream<String> stream = Files.newBufferedReader(Paths.get(String.valueOf(tempfile)), CODE).lines().parallel();
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile), BUFFERSIZE)) {
            stream
                    .forEach(
                            e -> {
                                if (!db.isInBase(e.split(":")[0])) {
                                    try {
                                        writer.write(e + System.lineSeparator());
                                        count.getAndIncrement();
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                }
                            }
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Счетчик: " + count.toString());
        db.close();
        System.out.println(outputFile.length() / (1024 * 1024) + " mb - в методе");

        Files.move(outputFile.toPath(), tempfile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return detect;
    }

    private void writeFile(File tempfile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempfile, CODE), BUFFERSIZE)) {
            for (int i = 0; i < files.size(); i++) {
                try (Stream<String> stream = Files.newBufferedReader(Paths.get(String.valueOf(files.get(i))), CODE).lines().parallel()) {
                    stream
                            .forEach(a -> {
                                try {
                                    writer.write(a + System.lineSeparator());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanFile(File tempfile) throws IOException { //
        File outputFile = File.createTempFile("text2", ".temp", PLACE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile), BUFFERSIZE)) {
            try (Stream<String> stream = Files.newBufferedReader(Paths.get(String.valueOf(tempfile)), CODE).lines().parallel()) {
                stream
                        .filter(line -> {
                            line = line.trim();
                            line = line.replace(';', ':');
                            line = line.replaceAll("\"", "");
                            return VALID_STRING.matcher(line).matches();
                        }).forEach(line -> {
                    try {
                        writer.write(line + System.lineSeparator());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        Files.move(outputFile.toPath(), tempfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private int deleteDoubles(File tempfile) throws IOException {
        int detected = 0;

        Set<String> lines = new HashSet<>();
        try (Scanner reader = new Scanner(tempfile, CODE)) {
            while (reader.hasNextLine()) {
                if (!lines.add(reader.nextLine())) {
                    detected++;
                }
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(tempfile));
        for (String unique : lines) {
            writer.write(unique);
            writer.newLine();
        }
        writer.close();
        lines = null;
        return detected;
    }

    private void normalizeDomen(File tempfile) throws IOException {
        File outputFile = File.createTempFile("text2", ".temp", PLACE);

        try (Stream<String> stream = Files.newBufferedReader(Paths.get(String.valueOf(tempfile)), CODE).lines().parallel();
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile), BUFFERSIZE)) {
            stream
                    .forEach(line -> {
                        try {
                            String[] data = line.split(":");
                            if (data.length > 1) {
                                line = data[0].trim();
                                String pass = data[1].trim();
                                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(line);
                                if (matcher.find() && !pass.equals(" ")) {
                                    writer.write(line + ":" + pass + System.lineSeparator());
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        Files.move(outputFile.toPath(), tempfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void normalizeLogin(File tempfile) throws IOException {
        File outputFile = File.createTempFile("text2", ".temp", PLACE);

        try (Stream<String> stream = Files.newBufferedReader(Paths.get(String.valueOf(tempfile)), CODE).lines().parallel();
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile), BUFFERSIZE)) {
            stream
                    .forEach(line -> {
                        String[] data = line.split(":");
                        if (data.length > 1) {
                            line = data[0].trim();
                            String pass = data[1].trim();
                            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(line);
                            if (matcher.find()) {
                                line = line.split("@")[0];
                            }
                            if (!pass.equals(" ")) {
                                try {
                                    writer.write(line + ":" + pass + System.lineSeparator());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        Files.move(outputFile.toPath(), tempfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private long saveFiles(File tempfile, String database) {

        long detectLogin = 0;

        RocksDBRepository db = new RocksDBRepository(database, true);//TODO: improve

        try (Scanner reader = new Scanner(tempfile)) {
            WriteBatch batch = new WriteBatch();
            long count = 0;
            while (reader.hasNextLine()) {
                String key = reader.nextLine();
                while (reader.hasNextLine() && count <= 10_000_000) {
                    String[] line = reader.nextLine().split(":");
                    batch.put(line[0].getBytes(), line[1].getBytes());
                    detectLogin++;
                    count++;
                }
                count = 0;
                db.writeBatch(batch);
                batch.clear();
            }
        } catch (IOException | RocksDBException e) {
            e.printStackTrace();
        }
        db.close();
        return detectLogin;
    }

    private List<Long> createBases() {
        List<Long> res = new ArrayList<>();
        RocksDBRepository mails = new RocksDBRepository("mail", true);
        RocksDBRepository logins = new RocksDBRepository("login", true);
        res.add(mails.countLines());
        res.add(logins.countLines());
        mails.close();
        logins.close();
        return res;
    }

    private List<Long> saveExtraFiles(File tempfile, File tempfile2) {

        List<Long> list = new ArrayList<>();

        long detectLogin = 0;
        long detectMail = 0;

        //сохраняем почты
        detectMail = saveFiles(tempfile, "mail");
        //сохраняем логины
        detectLogin = saveFiles(tempfile2, "login");

        list.add(detectMail);
        list.add(detectLogin);
        return list;
    }

    private void setProgress(int value) {
        progressBar.setValue(value);
    }

    public void runProgram() {
        try {
            long detectDoubles = 0; //есть

            long detectBeforeMail = 0;//+
            long detectBeforeLogin = 0;//+

            long detectValidMail = 0;//
            long detectValidLogin = 0;//

            long detectAfterMail = 0;
            long detectAfterLogin = 0;

            long time = System.nanoTime();

            PLACE.mkdirs();
            File newFile = File.createTempFile("text", ".temp", PLACE);
            File newFile2 = File.createTempFile("text23", ".temp", PLACE);

            createBases();

            System.out.println(5);
            setProgress(5);
            writeFile(newFile); //+
            System.out.println(10);
            setProgress(10);
            cleanFile(newFile); //+
            System.out.println(20);
            setProgress(20);
            if (domen && withoutDomen) { //+
                Files.copy(newFile.toPath(), newFile2.toPath(), StandardCopyOption.REPLACE_EXISTING);
                normalizeDomen(newFile);
                normalizeLogin(newFile2);
            } else {
                if (domen) {
                    normalizeDomen(newFile);
                }
                if (withoutDomen) {
                    normalizeLogin(newFile);
                }
            }

            setProgress(30);
            System.out.println(30);
//            if (doubles) {
//                if (domen && withoutDomen) {
//                    detectDoubles += deleteDoubles(newFile);
//                    detectDoubles += deleteDoubles(newFile2);
//                } else {
//                    detectDoubles += deleteDoubles(newFile);
//                }
//            }
            setProgress(50);
            System.out.println(50);
            if (baseDoubles) {
                if (domen && withoutDomen) {
                    detectBeforeMail = deleteBase(newFile, "mail", 50, 20);
                    detectBeforeLogin = deleteBase(newFile2, "login", 70, 90);
                } else if (domen) {
                    detectBeforeMail = deleteBase(newFile, "mail", 50, 90);
                } else {
                    detectBeforeLogin = deleteBase(newFile, "login", 50, 90);
                }
            }
            setProgress(90);
            System.out.println(90);
            System.out.println(newFile.length() / (1024 * 1024) + " mb");
            System.out.println(newFile2.length() / (1024 * 1024) + " mb");

            //-------------------------------
            if (domen && withoutDomen) {
                var detectValid = saveExtraFiles(newFile, newFile2);
                detectValidLogin = detectValid.get(1);
                detectValidMail = detectValid.get(0);
            } else {
                if (domen) {
                    detectValidMail = saveFiles(newFile, "mail");
                } else {
                    detectValidLogin = saveFiles(newFile, "login");
                }
            }
            setProgress(100);

            detectAfterLogin = detectBeforeLogin + detectValidLogin;
            detectAfterMail = detectBeforeMail + detectValidMail;


            time = System.nanoTime() - time;

            newFile2.deleteOnExit();
            newFile.deleteOnExit();

            MessageFinal dialog = new MessageFinal();
            dialog.setL1(String.format("Почт в базе: %d", detectBeforeMail));
            dialog.setL2(String.format("Логинов в базе: %d", detectBeforeLogin));
            dialog.setL3(String.format("Дублей удалено: %d", detectDoubles));
            dialog.setL4(String.format("Внесено логинов: %d", detectValidLogin));
            dialog.setL5(String.format("Внесено почт: %d", detectValidMail));
            dialog.setL6(String.format("Почт после обработки: %d", detectAfterMail));
            dialog.setL7(String.format("Логинов после обработки: %d", detectAfterLogin));
            dialog.setL8(String.format("Выполнено за %.2f sec\n", time / 1_000_000.0 / 1000));
            dialog.setLocationRelativeTo(null);
            dialog.pack();
            dialog.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        setProgress(0);
    }
}
