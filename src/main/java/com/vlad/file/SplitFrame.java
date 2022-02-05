package com.vlad.file;

import com.vlad.file.db.RocksDBRepository;
import org.rocksdb.RocksIterator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class SplitFrame extends JFrame {

    private JButton weightBtn;
    private JButton sumBtn;
    private JTextField textField1;
    private JTextField textField2;
    private JPanel splitFrame;
    private JComboBox<String> comboBox1;
    private JProgressBar progressBar1;

    public SplitFrame() {
        setVisible(true);
        setTitle("Разделение базы");
        setSize(420, 100);
        setContentPane(splitFrame);

        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        textField1.setColumns(6);
        textField2.setColumns(6);
        comboBox1.addItem("mb");
        comboBox1.addItem("kb");
        pack();
        weightBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getWeightBases("mail");
                getWeightBases("login");
            }
        });

        sumBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getLinesBases("mail");
                getLinesBases("login");
            }
        });
    }

    private void getLinesBases(String packname) {
        int x = Integer.parseInt(textField2.getText());
        Runnable runner = new Runnable() {
            public void run() {
                if (!checkFile(packname)) {
                    JOptionPane.showMessageDialog(splitFrame, "Базы final/" + packname + " не существует");
                    return;
                }
                try {
                    try {
                        saveFiles(x, packname);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(splitFrame, "Введите кол-во строк!");
                    return;
                }
            }
        };
        Thread t = new Thread(runner, "sum runner");
        t.start();
    }

    private void getWeightBases(String packname) {
        Runnable runner = new Runnable() {
            public void run() {
                if (!checkFile(packname)) {
                    JOptionPane.showMessageDialog(splitFrame, "Базы final/" + packname + " не существует");
                    return;
                }
                try {
                    int x = Integer.parseInt(textField1.getText());
                    String weight = (String) comboBox1.getSelectedItem();
//                    JOptionPane.showMessageDialog(splitFrame, x + weight);

                    try {
                        saveFiles(x, weight, packname);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(splitFrame, "Введите вес файла!");
                    return;
                }
            }
        };
        Thread t = new Thread(runner);
        t.start();
    }


    //подсчет файлов в папке results/packname
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

    //сохраняем базы по кол-ву строк
    private void saveFiles(int count, String packname) throws IOException {
        System.out.println(count + " кол-во строк");

        RocksDBRepository db = new RocksDBRepository(packname, false);

        long lines = db.countLines();
        long oldLines = lines;
        Date date = new Date();

        String[] dateS = date.toString().split(" ");
        String trueDate = dateS[1] + "_" + dateS[2] + "_" + dateS[3].replace(":", "-");

        List<String> list = new ArrayList<>();

        RocksIterator iter = db.print();

        int chet = 0;
        int num = 1;
        iter.seekToFirst();
        while (iter.isValid()) {
//
            File filePath = new File(makeFileName(countFiles(packname), trueDate, count, packname));
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath)))) {

                while (iter.isValid()) {
                    writer.write(new String(iter.value()) + System.getProperty("line.separator"));
                    chet += 1;
                    lines -= 1;
                    if (chet == count) {
                        double val = (((oldLines - lines) / (double) oldLines) * 100.0);
                        System.out.println(oldLines + lines);
                        System.out.println(val);
                        progressBar1.setValue((int) val);
                        chet = 0;
                        num += 1;
                        break;
                    }
                    iter.next();
                }
            }
            if (chet != 0) {
                filePath = rename(chet, filePath);
            }
            list.add(filePath.getAbsolutePath());
            iter.next();
        }
        setMessage(list);
        progressBar1.setValue(0);
    }

    //сохраняем базы по размеру
    private void saveFiles(int count, String size, String packname) throws IOException {

        if (size.equals("mb")) {
            count = count * 1024 * 1024;
        } else {
            count = count * 1024;
        }

        File tempfile = null;
        try {
            tempfile = takeFile(packname);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(splitFrame, "Базы не существует");
            e.printStackTrace();
            return;
        }

        float lines = (float) countLines(packname);
        float oldLines = lines;
        Date date = new Date();

        String[] dateS = date.toString().split(" ");
        String trueDate = dateS[1] + "_" + dateS[2] + "_" + dateS[3].replace(":", "-");

        List<String> list = new ArrayList<>();
        try (Scanner reader = new Scanner(tempfile)) {
            int chet = 0;
            int chet2 = 0;
            while (reader.hasNextLine()) {
//                String extraName = domen ? "mail" : "login";
                File filePath = new File(makeFileName(countFiles(packname), trueDate, count, packname));
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filePath)))) {
                    while (reader.hasNextLine()) {
                        String name = reader.nextLine() + System.getProperty("line.separator");
                        writer.write(name);
                        chet += name.getBytes("UTF-8").length;
                        System.out.println(chet);
                        chet2 += 1;
                        lines -= 1;
                        if (chet >= count) {
                            float val = (float) (((oldLines - lines) / oldLines) * 100.0);
                            System.out.println(val);
                            progressBar1.setValue((int) val);
//                            filePath = rename(chet2, filePath);
                            chet = 0;
                            break;
                        }
                    }
                }
                filePath = rename(chet2, filePath);
                chet2 = 0;
                list.add(filePath.getAbsolutePath());
            }
            setMessage(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        progressBar1.setValue(0);
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

    private int countLines(String filename) throws IOException {
        String path = System.getProperty("user.dir");
        File filePath = new File(path + "/" + "final" + "/" + filename + ".txt");
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }

    private File takeFile(String filename) throws IOException {
        String path = System.getProperty("user.dir");
        return new File(path + "/" + "final" + "/" + filename + ".txt");
    }

    private boolean checkFile(String filename) {
        String path = System.getProperty("user.dir");
        File filePath = new File(path + "/" + "databases" + "/" + filename);
        return filePath.exists();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        splitFrame = new JPanel();
        splitFrame.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        weightBtn = new JButton();
        weightBtn.setText("Разбить по размеру");
        splitFrame.add(weightBtn, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sumBtn = new JButton();
        sumBtn.setText("Разбить по строкам ");
        splitFrame.add(sumBtn, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        textField1.setText("");
        splitFrame.add(textField1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textField2 = new JTextField();
        splitFrame.add(textField2, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        comboBox1 = new JComboBox();
        splitFrame.add(comboBox1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(75, -1), null, 0, false));
        progressBar1 = new JProgressBar();
        progressBar1.setStringPainted(true);
        splitFrame.add(progressBar1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return splitFrame;
    }

}