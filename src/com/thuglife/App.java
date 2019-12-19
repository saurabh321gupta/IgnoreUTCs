package com.thuglife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    private static String DEFAULT_PATH = null;
    private static Integer count = 0;

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame();

        JFileChooser j = new JFileChooser();
        j.setDialogTitle("Choose Repository Folder");
        j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int r = j.showSaveDialog(null);
        if (r == JFileChooser.APPROVE_OPTION) {
            DEFAULT_PATH = j.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        JTextPane codearea = new JTextPane();
        JScrollPane scroll;
        scroll = new JScrollPane(codearea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(300, 300));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        JButton ignoreButton = new JButton("Ignore!!!!!");
        ignoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String input = codearea.getText();
                    if (input == null || input.trim() == "" || DEFAULT_PATH == null)
                        return;
                    doIt(input);
                    showMessageAndExit(count + " UTCs ignored successfully!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        panel.add(ignoreButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void doIt(String input) throws IOException {
        Map<String, List<String>> map = getMap(input);
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String fileName = entry.getKey();
            File f = new File(DEFAULT_PATH + fileName);
            StringBuffer sb = readFile(f);
            updateIfImportIsNotPresent(sb);
            for (String testCase : entry.getValue()) {
                putIgnoreTag(testCase, sb);
            }
            writeFile(f, sb);
        }
    }

    public static StringBuffer readFile(File f) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        } catch (FileNotFoundException e) {
            showMessageAndExit("Invalid Repository folder!");
        }
        String str = br.readLine();
        while (str != null) {
            sb.append(str + "\n");
            str = br.readLine();
        }
        return sb;
    }

    public static void showMessageAndExit(String message) {
        JOptionPane.showMessageDialog(null, message);
        System.exit(0);
    }

    public static Map<String, List<String>> getMap(String input) {
        Map<String, List<String>> map = new HashMap<>();
        String lines[] = input.split("\\r?\\n");
        for (String str : lines) {
            int index = str.indexOf(">");
            if (index != -1) {
                String className = str.substring(0, index - 1);
                int index2 = str.indexOf(" ", index + 2);
                String testCaseName = "";
                if (index2 != -1) {
                    testCaseName = str.substring(index + 2, index2) + "()";
                } else {
                    testCaseName = str.substring(index + 2, str.length()) + "()";
                }
                String testCaseName1 = testCaseName;
                String fileName = prepareFileLocation(className);
                List<String> testCases = map.get(fileName);
                if (testCases != null) {
                    testCases.add(testCaseName);
                } else {
                    map.put(fileName, new ArrayList() {{
                        add(testCaseName1);
                    }});
                }
            }
        }
        return map;
    }

    public static String prepareFileLocation(String str) {
        return "/src/test/java/" + str.replaceAll("\\.", "/") + ".java";
    }

    public static void updateIfImportIsNotPresent(StringBuffer sb) {
        if (sb.indexOf("import org.junit.Ignore;") == -1) {
            int index = sb.indexOf("import");
            sb.replace(index, index + 6, "import org.junit.Ignore;\nimport");
        }
    }

    private static void putIgnoreTag(String testCase, StringBuffer sb) {
        Integer index = findPositionOfTestCase(testCase, sb);
        if (index != null) {
            sb.replace(index, index + 5, "@Ignore\n\t@Test");
            count++;
        }
    }

    public static Integer findPositionOfTestCase(String testCase, StringBuffer sb) {
        int x = sb.indexOf(testCase);
        x = sb.lastIndexOf("@Test", x);
        if (x != -1)
            return x;
        return null;
    }

    private static void writeFile(File f, StringBuffer sb) {
        try {
            FileWriter fstream = new FileWriter(f);
            BufferedWriter outobj = new BufferedWriter(fstream);
            outobj.write(sb.toString());
            outobj.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
