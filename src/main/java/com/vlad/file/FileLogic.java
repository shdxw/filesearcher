package com.vlad.file;

import com.vlad.file.db.RocksDBRepository;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileLogic {
    private int lines;
    private List<Path> files;
    private boolean doubles;
    private boolean baseDoubles;
    private boolean domen;
    private boolean withoutDomen;
    private JProgressBar progressBar;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_STRING =
            Pattern.compile("^[A-Z0-9._%@$!#()*&^~/+-]+:[A-Z0-9._%@$!#()*&^~+/-]+$", Pattern.CASE_INSENSITIVE);

    public FileLogic(int lines, List<Path> files, boolean doubles, boolean baseDoubles, boolean domen, boolean withoutDomen) {
        this.lines = lines;
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
        File outputFile = File.createTempFile("text2", ".temp", null);
        RocksDBRepository db = new RocksDBRepository(pack);

        long detect = db.countLines(); // всего в базе

        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(tempfile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int newCount = count/sum; //сумма за процент
        count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(tempfile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (db.find(line) == null) {
                    writer.write(line + System.lineSeparator());
                    count++;
                    if (count >= newCount) {
                        init++;
                        setProgress(init);
                        count = 0;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        tempfile.delete();
        outputFile.renameTo(tempfile);

        return detect;
    }

    private void writeFile(File tempfile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempfile))) {
            for (int i = 0; i < files.size(); i++) {
                try (Scanner reader = new Scanner(files.get(i))) {
                    while (reader.hasNextLine()) {
                        writer.write(reader.nextLine() + System.getProperty("line.separator"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanFile(File tempfile) throws IOException { //
        File outputFile = File.createTempFile("text2", ".temp", null);

        try (Stream<String> stream = Files.lines(Paths.get(String.valueOf(tempfile)), StandardCharsets.ISO_8859_1);
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                    stream.forEach(line -> {
                        line = line.trim();
                        line = line.replace(';', ':');
                        line = line.replaceAll("\"", "");
                        if (VALID_STRING.matcher(line).matches()) {
                            try {
                                writer.write(line);
                                writer.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
        tempfile.delete();
        outputFile.renameTo(tempfile);
    }

    private int deleteDoubles(File tempfile) throws IOException {
        int detected = 0;
        BufferedReader reader = new BufferedReader(new FileReader(tempfile));
        Set<String> lines = new HashSet<String>(50000); // maybe should be bigger
        String line;
        while ((line = reader.readLine()) != null) {
            if (!lines.add(line)) {
               detected++;
            }
        }
        reader.close();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempfile));
        for (String unique : lines) {
            writer.write(unique);
            writer.newLine();
        }
        writer.close();
        return detected;
    }

    private void normalizeDomen(File tempfile) throws IOException {
        File outputFile = File.createTempFile("text2", ".temp", null);

        try (BufferedReader reader = new BufferedReader(new FileReader(tempfile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String line;
            String pass;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(":");
                line = data[0].trim();
                pass = data[1].trim();
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(line);
                if (matcher.find() && !pass.equals(" ")) {
                    writer.write(line + ":" + pass);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tempfile.delete();
        outputFile.renameTo(tempfile);
    }

    private void normalizeLogin(File tempfile) throws IOException {
        File outputFile = File.createTempFile("text2", ".temp", null);

        try (BufferedReader reader = new BufferedReader(new FileReader(tempfile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String line;
            String pass;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(":");
                line = data[0].trim();
                pass = data[1].trim();
                Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(line);
                if (matcher.find()) {
                    line = line.split("@")[0];
                }
                if (!pass.equals(" ")) {
                    writer.write(line + ":" + pass);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tempfile.delete();
        outputFile.renameTo(tempfile);
    }

    private void makeBasePack() {
        String path = System.getProperty("user.dir");
        File filePath = new File(path + "/" + "base" + "/" + "login");
        File filePath2 = new File(path + "/" + "base" + "/" + "mail");
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        if (!filePath2.exists()) {
            filePath2.mkdirs();
        }
    }

    private String makeFileName(int num, String date, int length, String pack) {
        String path = System.getProperty("user.dir");
        return path + "/" + "base" + "/" + pack + "/" + num + "_" + date + "_" + length + ".txt";
    }

    private File rename(int count, File filePath) throws IOException {
        String[] filename = filePath
                .toPath()
                .getFileName()
                .toString()
                .split("_");

        String name = "";
        for (int i = 0; i < filename.length - 1; i++) {
            name = name + filename[i] + "_";
        }
        return Files.move(filePath.toPath(),
                filePath
                        .toPath()
                        .resolveSibling(name + count + ".txt")).toFile();
    }

    private void setMessage(List<String> list) {
//        Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuilder builder = new StringBuilder();
        for (String s : list) {
            builder.append(s).append(System.lineSeparator());
        }
//        StringSelection select = new StringSelection(builder.toString());
//        cp.setContents(select, null);
        JList jlist = new JList(list.toArray());
        JScrollPane sp = new JScrollPane(jlist);
        sp.setPreferredSize(new Dimension(500, 200));
        JOptionPane.showMessageDialog(null, sp);
    }

    private List<String> saveFiles(File tempfile, int count) {
        makeBasePack();
        Date date = new Date();
        String[] dateS = date.toString().split(" ");
        String trueDate = dateS[1] + "_" + dateS[2] + "_" + dateS[3].replace(":", "-");
        List<String> list = new ArrayList<>();
        int detectLogin = 0;

        try (Scanner reader = new Scanner(tempfile)) {
            int chet = 0;
            int num = 1;
            while (reader.hasNextLine()) {
                String extraName = domen ? "mail" : "login";
                File filePath = new File(makeFileName(countFiles(extraName), trueDate, count, extraName));
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filePath)))) {
                    while (reader.hasNextLine()) {
                        writer.write(reader.nextLine() + System.getProperty("line.separator"));
                        detectLogin++;
                        chet += 1;
                        if (chet == count) {
                            chet = 0;
                            num += 1;
                            break;
                        }
                    }
                }
                if (chet != 0) {
                    filePath = rename(chet, filePath);
                }
                list.add(filePath.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        list.add(String.valueOf(detectLogin));
        return list;
    }

    private List<String> saveExtraFiles(File tempfile, File tempfile2, int count) {
        makeBasePack();
        Date date = new Date();
        String[] dateS = date.toString().split(" ");
        String trueDate = dateS[1] + "_" + dateS[2] + "_" + dateS[3].replace(":", "-");
        List<String> list = new ArrayList<>();

        int detectLogin = 0;
        int detectMail = 0;

        //сохраняем почты
        try (Scanner reader = new Scanner(tempfile)) {
            int chet = 0;
            int num = 1;
            while (reader.hasNextLine()) {
                File filePath = new File(makeFileName(countFiles("mail"), trueDate, count, "mail"));
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filePath)))) {
                    while (reader.hasNextLine()) {
                        writer.write(reader.nextLine() + System.getProperty("line.separator"));
                        detectMail++;
                        chet += 1;
                        if (chet == count) {
                            chet = 0;
                            num += 1;
                            break;
                        }
                    }
                }
                if (chet != 0) {
                    filePath = rename(chet, filePath);
                }
                list.add(filePath.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //сохраняем логины
        try (Scanner reader = new Scanner(tempfile2)) {
            int chet = 0;
            int num = 1;
            while (reader.hasNextLine()) {
                File filePath = new File(makeFileName(countFiles("login"), trueDate, count, "login"));
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filePath)))) {
                    while (reader.hasNextLine()) {
                        writer.write(reader.nextLine() + System.getProperty("line.separator"));
                        detectLogin++;
                        chet += 1;
                        if (chet == count) {
                            chet = 0;
                            num += 1;
                            break;
                        }
                    }
                }
                if (chet != 0) {
                    filePath = rename(chet, filePath);
                }
                list.add(filePath.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        list.add(String.valueOf(detectMail));
        list.add(String.valueOf(detectLogin));
        return list;
    }

    private int countFiles(String pack) {
        String path = System.getProperty("user.dir");
        return Objects.requireNonNull(new File(path + "/" + "base" + "/" + pack).list()).length + 1;
    }

    private void setProgress(int value) {
        progressBar.setValue(value);
    }

    public void runProgram() {
        try {
            int detectDoubles = 0; //есть

            int detectBeforeMail = 0;//+
            int detectBeforeLogin = 0;//+

            int detectValidMail = 0;//
            int detectValidLogin = 0;//

            int detectAfterMail = 0;
            int detectAfterLogin = 0;

            long time = System.nanoTime();

            File newFile = File.createTempFile("text", ".temp", null);
            File newFile2 = File.createTempFile("text23", ".temp", null);
            makeBasePack();
            System.out.println(5);
            setProgress(5);
            writeFile(newFile);
            System.out.println(10);
            setProgress(10);
            cleanFile(newFile);
            System.out.println(20);
            setProgress(20);
            if (domen && withoutDomen) {
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
            if (doubles) {
                if (domen && withoutDomen) {
                    detectDoubles += deleteDoubles(newFile);
                    detectDoubles += deleteDoubles(newFile2);
                } else {
                    detectDoubles += deleteDoubles(newFile);
                }
            }
            setProgress(50);
            System.out.println(50);
            if (baseDoubles) {
                if (domen && withoutDomen) {
                    detectBeforeMail += deleteBase(newFile, "mail", 50, 20);
                    detectBeforeLogin += deleteBase(newFile2, "login", 70, 90);
                } else if (domen){
                    detectBeforeMail += deleteBase(newFile, "mail", 50, 90);
                } else {
                    detectBeforeLogin += deleteBase(newFile, "login", 50, 90);
                }
            }
            setProgress(90);
            System.out.println(90);

            //-------------------------------
            List<String> files = null;
            if (domen && withoutDomen) {
            files = saveExtraFiles(newFile, newFile2, lines);
            detectValidLogin = Integer.parseInt(files.get(files.size() - 1));
            files.remove(files.size() - 1);
            detectValidMail = Integer.parseInt(files.get(files.size() - 1));
            files.remove(files.size() - 1);
            } else {
            files =  saveFiles(newFile, lines);
            if (domen) {
                detectValidMail = Integer.parseInt(files.get(files.size() - 1));
                files.remove(files.size() - 1);
            } else {
                detectValidLogin = Integer.parseInt(files.get(files.size() - 1));
                files.remove(files.size() - 1);
            }
            }
            setProgress(100);

            detectAfterLogin = detectBeforeLogin + detectValidLogin;
            detectAfterMail = detectBeforeMail + detectValidMail;


            time = System.nanoTime() - time;
            //String timeInString = String.format("Выполнено за %,9.3f ms\n", time/1_000_000.0);
            newFile2.deleteOnExit();
            newFile.deleteOnExit();
            //-------------------------------

            //StringBuilder builder = new StringBuilder();
//            for (String s : list) {
//                builder.append(s).append(System.lineSeparator());
//            }
//        StringSelection select = new StringSelection(builder.toString());
//        cp.setContents(select, null);

            MessageFinal dialog = new MessageFinal();
            JList listik = new JList(files.toArray());
            dialog.setPane(listik);
            dialog.setL1(String.format("Почт в базе: %d", detectBeforeMail));
            dialog.setL2(String.format("Логинов в базе: %d", detectBeforeLogin));
            dialog.setL3(String.format("Дублей удалено: %d", detectDoubles));
            dialog.setL4(String.format("Внесено логинов: %d", detectValidLogin));
            dialog.setL5(String.format("Внесено почт: %d", detectValidMail));
            dialog.setL6(String.format("Почт после обработки: %d", detectAfterMail));
            dialog.setL7(String.format("Логинов после обработки: %d", detectAfterLogin));
            dialog.setL8(String.format("Выполнено за %.2f sec\n", time/1_000_000.0/1000));
            dialog.pack();
            dialog.setVisible(true);


            //setMessage(files);
            //-------------------------------

        } catch (IOException e) {
            e.printStackTrace();
        }
        setProgress(0);
    }
}
