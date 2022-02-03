package com.vlad.file;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Window extends JFrame {
    private JCheckBox domenCheck;
    private JCheckBox withoutCheck;
    private JCheckBox doubleCheck;
    private JCheckBox baseCheck;
    private JButton btnOpenDir;
    private JButton btnRun;
    private JPanel mainFrame;
    private JProgressBar progressBar1;
    private JButton splitBaseBtn;
    private List<Path> files = new ArrayList<>();
    private boolean sign = true;

    public Window() throws IOException {
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("Analyzer by Gaz");
        //File file = new File("./src/main/resources/icons/icon.png");
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream file = classLoader.getResourceAsStream("icon.png");
        this.setIconImage(ImageIO.read(file));
        setContentPane(mainFrame);
        // Вывод окна на экран
        setSize(340, 280);
        setVisible(true);

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
                                FileLogic cracker = new FileLogic(files,
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


    public static void main(String[] args) throws IOException {
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
        mainFrame.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 2, new Insets(10, 20, 10, 20), -1, -1));
        mainFrame.setBackground(new Color(-12303292));
        mainFrame.setForeground(new Color(-13679615));
        mainFrame.setRequestFocusEnabled(true);
        mainFrame.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, mainFrame.getFont()), new Color(-4473925)));
        domenCheck = new JCheckBox();
        domenCheck.setBackground(new Color(-12303292));
        domenCheck.setBorderPainted(false);
        domenCheck.setBorderPaintedFlat(true);
        domenCheck.setFocusPainted(false);
        Font domenCheckFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 12, domenCheck.getFont());
        if (domenCheckFont != null) domenCheck.setFont(domenCheckFont);
        domenCheck.setForeground(new Color(-3355444));
        domenCheck.setHideActionText(false);
        domenCheck.setSelected(true);
        domenCheck.setText("С доменом");
        mainFrame.add(domenCheck, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 21), null, 0, false));
        withoutCheck = new JCheckBox();
        withoutCheck.setBackground(new Color(-12303292));
        withoutCheck.setFocusPainted(false);
        Font withoutCheckFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 12, withoutCheck.getFont());
        if (withoutCheckFont != null) withoutCheck.setFont(withoutCheckFont);
        withoutCheck.setForeground(new Color(-3355444));
        withoutCheck.setSelected(true);
        withoutCheck.setText("Без домена");
        mainFrame.add(withoutCheck, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 21), null, 0, false));
        doubleCheck = new JCheckBox();
        doubleCheck.setBackground(new Color(-12303292));
        doubleCheck.setFocusPainted(false);
        Font doubleCheckFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 12, doubleCheck.getFont());
        if (doubleCheckFont != null) doubleCheck.setFont(doubleCheckFont);
        doubleCheck.setForeground(new Color(-3355444));
        doubleCheck.setSelected(true);
        doubleCheck.setText("Убрать дубли");
        mainFrame.add(doubleCheck, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 21), null, 0, false));
        baseCheck = new JCheckBox();
        baseCheck.setBackground(new Color(-12303292));
        baseCheck.setBorderPainted(false);
        baseCheck.setFocusPainted(false);
        Font baseCheckFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 12, baseCheck.getFont());
        if (baseCheckFont != null) baseCheck.setFont(baseCheckFont);
        baseCheck.setForeground(new Color(-3355444));
        baseCheck.setOpaque(false);
        baseCheck.setSelected(true);
        baseCheck.setText("Сравнить с базой");
        mainFrame.add(baseCheck, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 21), null, 0, false));
        btnOpenDir = new JButton();
        btnOpenDir.setBackground(new Color(-12303292));
        btnOpenDir.setContentAreaFilled(true);
        btnOpenDir.setDefaultCapable(true);
        btnOpenDir.setDoubleBuffered(false);
        btnOpenDir.setFocusCycleRoot(false);
        btnOpenDir.setFocusTraversalPolicyProvider(false);
        Font btnOpenDirFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 12, btnOpenDir.getFont());
        if (btnOpenDirFont != null) btnOpenDir.setFont(btnOpenDirFont);
        btnOpenDir.setForeground(new Color(-3355444));
        btnOpenDir.setText("Добавить файлы");
        mainFrame.add(btnOpenDir, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(196, 30), null, 0, false));
        btnRun = new JButton();
        btnRun.setBackground(new Color(-12303292));
        Font btnRunFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 12, btnRun.getFont());
        if (btnRunFont != null) btnRun.setFont(btnRunFont);
        btnRun.setForeground(new Color(-3355444));
        btnRun.setText("Запустить");
        mainFrame.add(btnRun, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar1 = new JProgressBar();
        progressBar1.setBackground(new Color(-12303292));
        progressBar1.setBorderPainted(true);
        progressBar1.setFocusable(false);
        progressBar1.setOpaque(false);
        progressBar1.setRequestFocusEnabled(false);
        progressBar1.setStringPainted(true);
        mainFrame.add(progressBar1, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        splitBaseBtn = new JButton();
        splitBaseBtn.setBackground(new Color(-12303292));
        Font splitBaseBtnFont = this.$$$getFont$$$("Consolas", Font.PLAIN, 12, splitBaseBtn.getFont());
        if (splitBaseBtnFont != null) splitBaseBtn.setFont(splitBaseBtnFont);
        splitBaseBtn.setForeground(new Color(-3355444));
        splitBaseBtn.setText("Разделить базу");
        mainFrame.add(splitBaseBtn, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainFrame;
    }

}




