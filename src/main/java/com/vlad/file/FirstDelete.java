package com.vlad.file;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FirstDelete {

    public static void main(String[] args) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir")))) {
            List<Path> ways = paths.filter(e -> e.toString().endsWith(".txt")).collect(Collectors.toList());
            for (Path way : ways) {
                File outputFile = File.createTempFile("text2", ".temp", new File(System.getProperty("user.dir")));
                try (BufferedReader reader = new BufferedReader(new FileReader(way.toFile()));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("$")) {
                            line = line.replaceFirst("\\$", "");
                        }
                        writer.write(line);
                        System.out.println(line);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                way.toFile().delete();
                outputFile.renameTo(way.toFile());
                JOptionPane.showMessageDialog(null, "Работа завершена.");
            }
        }
    }
}
