package jotepad;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

public class View extends JFrame {

    private static final long serialVersionUID = -81894675312554367L;
    private static final String VERSION = "0.131";
    private static final String TITLE = "Jotepad";
    private static final String LOOK_AND_FEEL = "Windows";
    private static final int WINDOW_WIDHT = 800, WINDOW_HEIGHT = WINDOW_WIDHT - 300;

    private final Font defaultFont = new Font("Liberation Mono", Font.PLAIN, 16);
    private final JFileChooser fileChooser;
    private final Container container;
    private final JMenuBar menuBar;
    private final JMenu menuFile, menuFormat, menuHelp;
    private final JMenuItem fileOpen, fileSave, fileSaveAs, fileClose;
    private final JCheckBoxMenuItem formatWordWrap;
    private final JMenuItem formatFont;
    private final JMenuItem helpAbout, helpGitRepo;
    private final JScrollPane scrollBar;

    private final Object lock;
    private final ArrayList<String> history;

    private static JTextArea textArea;
    private static File savedFile, openedFile;

    private int currentHistoryIndex;
    private FontSelector fontSelector;

    public View() {
        lock = new Object();
        history = new ArrayList<>();
        currentHistoryIndex = -1;

        setLookAndFeel();

        container = getContentPane();

        fileChooser = new JFileChooser();

        menuBar = new JMenuBar();

        menuFile = new JMenu("File");
        menuFormat = new JMenu("Format");
        menuHelp = new JMenu("Help");

        fileOpen = new JMenuItem("Open... [Ctrl+O]");
        fileOpen.addActionListener((e -> {
            openFile();
        }));

        fileSave = new JMenuItem("Save [Ctrl+S]");
        fileSave.addActionListener((e -> {
            saveFile();
        }));

        fileSaveAs = new JMenuItem("Save as... [Ctrl+Shift+S]");
        fileSaveAs.addActionListener((e -> {
            saveFileAs();
        }));

        fileClose = new JMenuItem("Close");
        fileClose.addActionListener((e -> {
            System.exit(0);
        }));

        menuFile.add(fileOpen);
        menuFile.add(fileSave);
        menuFile.add(fileSaveAs);
        menuFile.add(new JSeparator());
        menuFile.add(fileClose);

        formatWordWrap = new JCheckBoxMenuItem("Word wrap", true);
        formatWordWrap.addActionListener(e -> {
            toggleLineWrap();
        });

        formatFont = new JMenuItem("Font...");
        formatFont.addActionListener((e -> {
            if (fontSelector == null) {
                fontSelector = new FontSelector();
            }
            fontSelector.setVisible(true);
        }));

        menuFormat.add(formatFont);
        menuFormat.add(formatWordWrap);

        helpAbout = new JMenuItem("About Jotepad");
        helpAbout.addActionListener((e -> {
            JOptionPane.showMessageDialog(container,
                    "Jotepad it's a text editor programmed in Java.", "About Jotepad", JOptionPane.INFORMATION_MESSAGE);
        }));

        helpGitRepo = new JMenuItem("Our GitHub's repo");
        helpGitRepo.addActionListener((e) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/sirmigui/jotepad"));
            } catch (URISyntaxException | IOException ex) {
                Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        menuHelp.add(helpAbout);
        menuHelp.add(helpGitRepo);

        menuBar.add(menuFile);
        menuBar.add(menuFormat);
        menuBar.add(menuHelp);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setFont(defaultFont);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String key = String.format("%s", (char) e.getKeyCode());
                if (e.isControlDown()) {
                    switch (key.toLowerCase()) {
                        case "o" -> {
                            openFile();
                        }
                        case "s" -> {
                            if (e.isShiftDown()) {
                                saveFileAs();
                            } else {
                                saveFile();
                            }
                        }
                        case "z" -> {
                            undoChanges();
                            return;
                        }
                    }

                }

                if (!textArea.getText().isBlank() && textArea.getText().length() % 10 == 0) {
                    history.add(textArea.getText());
                    currentHistoryIndex++;
                }
            }
        }
        );

        scrollBar = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        container.add(scrollBar);

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setTitle(String.format("%s v%s", TITLE, VERSION));

        setSize(WINDOW_WIDHT, WINDOW_HEIGHT);

        setJMenuBar(menuBar);
    }

    private void setLookAndFeel() {
        List<LookAndFeelInfo> lookAndFeelList = Arrays.asList(UIManager.getInstalledLookAndFeels());

        lookAndFeelList.forEach((e) -> {
            if (e.getName().equals(LOOK_AND_FEEL)) {
                try {
                    UIManager.setLookAndFeel(e.getClassName());
                    return;
                } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException ex) {
                    Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void toggleLineWrap() {
        textArea.setLineWrap(!textArea.getLineWrap());
    }

    private void changeTitle() {
        setTitle(String.format("%s v%s | %s", TITLE, VERSION, savedFile.toString()));
    }

    private void undoChanges() {
        if (currentHistoryIndex > 0) {
            history.remove(history.size() - 1);
            currentHistoryIndex--;
            textArea.setText(history.get(currentHistoryIndex));
        }
    }

    private void saveFile() {
        textArea.setEditable(false);
        if (savedFile == null) {
            saveFileAs();
        } else {
            writeContentInFile();
        }
        textArea.setEditable(true);
    }

    private void saveFileAs() {
        textArea.setEditable(false);
        int fileAnswer = fileChooser.showSaveDialog(container);

        if (fileAnswer == JFileChooser.APPROVE_OPTION) {
            String name = fileChooser.getSelectedFile().getAbsolutePath();

            if (!name.endsWith(".txt")) {
                name = name.concat(".txt");
            }

            savedFile = new File(name);

            if (!savedFile.exists()) {
                writeContentInFile();
                changeTitle();
            } else {
                int answer = JOptionPane.showConfirmDialog(container,
                        "This file already exists, do you want overwrite it?", "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (answer == JOptionPane.YES_OPTION) {
                    writeContentInFile();
                }

            }

        }
        textArea.setEditable(true);
    }

    private byte[] textToBuffer() {
        String valorTextArea = textArea.getText();
        byte[] buffer = new byte[valorTextArea.length()];

        Thread thread1 = new Thread(() -> {
            synchronized (lock) {
                for (int i = 0; i < valorTextArea.length() / 2; i++) {
                    buffer[i] = (byte) valorTextArea.charAt(i);
                }
                lock.notify();
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock) {
                for (int i = valorTextArea.length() / 2; i < valorTextArea.length(); i++) {
                    buffer[i] = (byte) valorTextArea.charAt(i);
                }
                lock.notify();
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
        }

        return buffer;
    }

    private void writeContentInFile() {
        try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(savedFile))) {
            byte[] buffer = textToBuffer();

            if (buffer == null) {
                return;
            }

            writer.write(buffer, 0, buffer.length);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        textArea.setEditable(true);
    }

    protected static void setFont(String nombreFuente, int tamañoFuente) {
        textArea.setFont(new Font(nombreFuente, 0, tamañoFuente));
    }

    private void openFile() {
        int answer;
        if (fileChooser.showOpenDialog(container) != JFileChooser.CANCEL_OPTION) {
            openedFile = savedFile = new File(fileChooser.getSelectedFile().getAbsolutePath());
            changeTitle();

            if (!textArea.getText().isEmpty()) {
                if (!fileContentEqualsInstanceContent()) {
                    answer = JOptionPane.showConfirmDialog(container, "Do you want to save this document?", "Save file", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        saveFileAs();
                    }
                }
            }
            setReadedContent(readFileContent());
        }
    }

    private String readFileContent() {
        StringBuilder fileContent = new StringBuilder();
        long fileSize = openedFile.length();

        if (openedFile != null) {
            try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(openedFile))) {
                int readedValue = reader.read();

                while (readedValue != -1) {
                    fileContent.append((char) readedValue);
                    readedValue = reader.read();
                }

                reader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileContent.toString();
    }

    private void setReadedContent(String fileContent) {
        textArea.setText("");
        textArea.setText(fileContent);
    }

    private boolean fileContentEqualsInstanceContent() {
        String fileContent = readFileContent();
        String instanceContent = textArea.getText();

        return fileContent.toLowerCase().equals(instanceContent.toLowerCase());
    }

    public static void main(String[] args) {
        View view = new View();
        view.setVisible(true);
    }
}
