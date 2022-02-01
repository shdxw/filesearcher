package com.vlad.file.extra;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class Csv {

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

    private int countFiles(String packname) {
        String path = System.getProperty("user.dir");
        File dir = new File(path + "/" + "results" + "/" + packname);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return Objects.requireNonNull(new File(path + "/" + "results" + "/" + packname).list()).length + 1;
    }

    private String makeFileName(int num, String date, int length, String packname) {
        String path = System.getProperty("user.dir");
        File dir = new File(path + "/" + "results" + "/" + packname);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return path + "/" + "results" + "/" + packname + "/" + num + "_" + date + "_" + length + ".txt";
    }

    private void saveFiles(int count, String packname, File tempfile) throws IOException {

        float lines = (int) Files.lines(tempfile.toPath()).count();
        Date date = new Date();

        String[] dateS = date.toString().split(" ");
        String trueDate = dateS[1] + "_" + dateS[2] + "_" + dateS[3].replace(":", "-");

        try (Scanner reader = new Scanner(tempfile)) {
            int chet = 0;
            int num = 1;
            while (reader.hasNextLine()) {
                File filePath = new File(makeFileName(countFiles(packname), trueDate, count, packname));
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filePath)))) {
                    while (reader.hasNextLine()) {
                        writer.write(reader.nextLine() + System.getProperty("line.separator"));
                        chet += 1;
                        lines -= 1;
                        System.out.println("Осталось: " + lines);
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
            }
            System.out.println("zaebis");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(System.getProperty("user.dir")))) {
            List<Path> ways = paths.filter(e -> e.toString().endsWith(".csv")).collect(Collectors.toList());

            File outputFile = File.createTempFile("text2", ".temp", new File(System.getProperty("user.dir")));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                for (Path way : ways) {
                    boolean first = true;
                    try (CSVReader reader = new CSVReader(new FileReader(String.valueOf(way)))) {
                        String[] lineInArray;
                        while ((lineInArray = reader.readNext()) != null) {
                            if (first) {
                                first = false;
                                continue;
                            }
                            writer.write(String.format("%s:%s", lineInArray[7], lineInArray[11]));
                            writer.newLine();
                        }
                    } catch (CsvValidationException e) {
                        e.printStackTrace();
                    }
                }
            }
            Csv csv = new Csv();
            csv.saveFiles(400_000, "csv", outputFile);
            outputFile.delete();
        }
    }

}
