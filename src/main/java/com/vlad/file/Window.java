package com.vlad.file;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Window extends JFrame {
    private JCheckBox domenCheck;
    private JCheckBox withoutCheck;
    private JCheckBox doubleCheck;
    private JCheckBox baseCheck;
    private JButton btnOpenDir;
    private JButton btnRun;
    private JTextField textField;
    private JPanel mainFrame;
    private JProgressBar progressBar1;
    private JButton saveBaseButton;
    private JProgressBar progressBar2;
    private JButton splitBaseBtn;
    private List<Path> files = new ArrayList<>();
    private boolean sign = true;

    public Window() {
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("Yoba analyzer");
        setContentPane(mainFrame);
        // Вывод окна на экран
        setSize(300, 280);
        setVisible(true);

        textField.setEditable(true);

//        btnOpenDir.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//
//                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
//                jfc.setDialogTitle("Выберите файлы:");
//                jfc.setAcceptAllFileFilterUsed(false);
//                FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT", "txt");
//                jfc.addChoosableFileFilter(filter);
//                jfc.setMultiSelectionEnabled(true);
//                int returnValue = jfc.showOpenDialog(null);
//                if (returnValue == JFileChooser.APPROVE_OPTION) {
//                    files = jfc.getSelectedFiles();
//                    System.out.println(files);
//                }
//            }
//        });

        btnOpenDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                chooser.setDialogTitle("Выберите папку с файлами:");

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //
                // disable the "All files" option.
                //
                chooser.setAcceptAllFileFilterUsed(false);
                int returnValue = chooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    try (Stream<Path> paths = Files.walk(Paths.get(chooser.getSelectedFile().toString()))) {
                        files = paths.filter(Files::isRegularFile)
                                .filter(a -> a
                                        .toString()
                                        .endsWith(".txt"))
                                .collect(Collectors.toList());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    if (files.size() == 0) {
                        JOptionPane.showMessageDialog(mainFrame, "В выбранной папке нет .txt файлов");
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "Выбраны " + files.size() + " файла");
                    }
                }
            }
        });

        splitBaseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SplitFrame();
            }
        });

        saveBaseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SaveLogic logic = new SaveLogic(progressBar2);
                logic.runSaveBase();
            }
        });

        domenCheck.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    doubleCheck.setSelected(true);
                    baseCheck.setSelected(true);
                }
            }
        });

        withoutCheck.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    doubleCheck.setSelected(true);
                    baseCheck.setSelected(true);
                }
            }
        });


        btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (sign) {
                    sign = false;
                    Runnable runner = new Runnable() {
                        public void run() {
                            //Your original code with the loop here.
                            if (files.size() == 0) {
                                JOptionPane.showMessageDialog(mainFrame, "Выберите файлы");
                                return;
                            }
                            if (!domenCheck.isSelected() && !withoutCheck.isSelected()) {
                                JOptionPane.showMessageDialog(mainFrame, "Выберите с доменом или без домена (либо оба)");
                                return;
                            }
                            try {
                                int x = Integer.parseInt(textField.getText());
                                FileLogic cracker = new FileLogic(x,
                                        files,
                                        doubleCheck.isSelected(),
                                        baseCheck.isSelected(),
                                        domenCheck.isSelected(),
                                        withoutCheck.isSelected()
                                );
                                cracker.setProgressBar(progressBar1);
                                cracker.runProgram();
                                files.clear();
                                sign = true;
                            } catch (NumberFormatException e1) {
                                JOptionPane.showMessageDialog(mainFrame, "Некорректный ввод числа!");
                                return;
                            }
                        }
                    };
                    Thread t = new Thread(runner, "Code Executer");
                    t.start();
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Идет анализ...");
                }
            }
        });
    }


    public static void main(String[] args) {
        UIManager.getInstalledLookAndFeels();
        new Window();
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
        mainFrame = new JPanel();
        mainFrame.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(8, 2, new Insets(0, 0, 0, 0), -1, -1));
        domenCheck = new JCheckBox();
        domenCheck.setText("С доменом");
        mainFrame.add(domenCheck, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 21), null, 0, false));
        withoutCheck = new JCheckBox();
        withoutCheck.setText("Без домена");
        mainFrame.add(withoutCheck, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 21), null, 0, false));
        doubleCheck = new JCheckBox();
        doubleCheck.setText("Убрать дубли");
        mainFrame.add(doubleCheck, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 21), null, 0, false));
        baseCheck = new JCheckBox();
        baseCheck.setText("Сравнить с базой");
        mainFrame.add(baseCheck, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 21), null, 0, false));
        btnOpenDir = new JButton();
        btnOpenDir.setText("Добавить файлы");
        mainFrame.add(btnOpenDir, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 30), null, 0, false));
        btnRun = new JButton();
        btnRun.setText("Запустить");
        mainFrame.add(btnRun, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField = new JTextField();
        textField.setColumns(6);
        textField.setEditable(true);
        textField.setEnabled(true);
        textField.setHorizontalAlignment(0);
        textField.setText("400000");
        textField.setToolTipText("число строк в файле");
        mainFrame.add(textField, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        progressBar1 = new JProgressBar();
        progressBar1.setStringPainted(true);
        mainFrame.add(progressBar1, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveBaseButton = new JButton();
        saveBaseButton.setText("Сохранить базу");
        mainFrame.add(saveBaseButton, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar2 = new JProgressBar();
        progressBar2.setStringPainted(true);
        mainFrame.add(progressBar2, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        splitBaseBtn = new JButton();
        splitBaseBtn.setText("Разделить базу");
        mainFrame.add(splitBaseBtn, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainFrame;
    }

}




