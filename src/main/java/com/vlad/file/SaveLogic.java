package com.vlad.file;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaveLogic {
    private JProgressBar progressBar1;

    public SaveLogic(JProgressBar progressBar1) {
        this.progressBar1 = progressBar1;
    }

    private void writeNewBase(String type) throws IOException {
        createPacks();
        int count = countLinesFiles(type);
        int chet = (count / 100);

        File filePath = new File(System.getProperty("user.dir") + "/" + "final");
        if (!filePath.exists()) {
            filePath.mkdir();
        }

        File finalFile = new File(System.getProperty("user.dir") + "/" + "final/" + type +".txt");

        try (Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + "/base" + "/" + type));
             BufferedWriter writer = new BufferedWriter(new FileWriter(finalFile, true))) {
            List<Path> ways = paths
                    .filter(Files::isRegularFile)
                    .filter(e -> !e.getFileName().toString().endsWith("check.txt"))
                    .collect(Collectors.toList());
            if (ways.size() == 0) {
                JOptionPane.showMessageDialog(null, "Нет новых файлов в base/" + type);
                return;
            }
            int counter = 0;
            int percent = 0;
            for (Path way : ways) {
                way = Files.move(way, way.resolveSibling(way.getFileName().toString().split("\\.")[0] + "_check.txt"));
                try (Scanner readerFile = new Scanner(way)) {
                    while (readerFile.hasNextLine()) {
                        writer.write(readerFile.nextLine() + System.lineSeparator());
                        counter += 1;
                        if (counter >= chet) {
                            counter = 0;
                            percent += 1;
                            progressBar1.setValue(percent);
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
        progressBar1.setValue(0);
        System.out.println("Все базы готовы!");
    }

    int countLinesFiles(String type) throws IOException {
        int chet = 0;
        createPacks();

        try (Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir") + "/base" + "/" + type))) {
            List<Path> ways = paths
                    .filter(Files::isRegularFile)
                    .filter(e -> !e.getFileName().toString().endsWith("check.txt"))
                    .collect(Collectors.toList());
            if (ways.size() == 0) {
                return 0;
            }
            for (Path way : ways) {
                try (Scanner readerFile = new Scanner(way)) {
                    while (readerFile.hasNextLine()) {
                        readerFile.nextLine();
                        chet += 1;
                    }
                } catch (Exception ex) {
                }
            }
            return chet;
        }
    }

    public void runSaveBase() {
        saveLogin();
        saveMail();
    }

    private void createPacks() {
       File mail =  new File(System.getProperty("user.dir") + "/base" + "/" + "mail");
       File login =  new File(System.getProperty("user.dir") + "/base" + "/" + "login");

       if (!mail.exists()) {
           mail.mkdirs();
       }
       if (!login.exists()) {
           login.mkdirs();
       }
    }

    private void saveLogin() {
        Runnable runner = new Runnable() {
            public void run() {
                try {
                    writeNewBase("login");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };
        Thread t = new Thread(runner);
        t.start();
    }

    private void saveMail() {
        Runnable runner = new Runnable() {
            public void run() {
                try {
                    writeNewBase("mail");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };
        Thread t = new Thread(runner);
        t.start();
    }
}
