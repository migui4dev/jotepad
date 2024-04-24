package jotepad;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javax.swing.UnsupportedLookAndFeelException;

public class View extends JFrame {

    private static final long serialVersionUID = -81894675312554367L;
    private static final String VERSION = "0.12";
    private static final String TITLE = "Jotepad";
    private static final String LOOK_AND_FEEL = "Windows";
    private static final int WINDOW_WIDHT = 800, WINDOW_HEIGHT = WINDOW_WIDHT - 300;

    private final Font defaultFont = new Font("Liberation Mono", 0, 16);

    private JFileChooser fileChooser;
    private Container container;
    private JMenuBar menuBar;
    private JMenu menuFile, menuFormat, menuHelp;
    private JMenuItem fileOpen, fileSave, fileSaveAs, fileClose;
    private JCheckBoxMenuItem formatWordWrap;
    private JMenuItem formatFont;
    private JMenuItem viewAbout;
    private static JTextArea textArea;
    private ActionListener actionFileOpen, actionFileSave, actionFileSaveAs, actionFileClose;
    private ActionListener actionFormatWordWrap;
    private ActionListener actionFormatFont, actionHelpAbout;
    private JScrollPane scrollBar;
    private static File savedFile, openedFile;

    private final Object lock;

    public View() {
        lock = new Object();
        this.initComponents();
    }

    private void setLookAndFeel() {
        for (UIManager.LookAndFeelInfo info
                : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equals(LOOK_AND_FEEL)) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void initComponents() {

        setLookAndFeel();

        fileChooser = new JFileChooser();
        actionFileOpen = (e -> {
            openFile();
        });

        actionFileSave = (e -> {
            saveFile();
        });

        actionFileSaveAs = (e -> {
            saveFileAs();
        });

        actionFileClose = (e -> {
            System.exit(0);
        });

        actionFormatWordWrap = (e -> {
            toggleLineWrap();
        });

        actionFormatFont = (e -> {

            new FontSelector().setVisible(true);
        });

        actionHelpAbout = (e -> {
            JOptionPane.showMessageDialog(container,
                    "Jotepad it's a text editor programmed in Java.", "About Jotepad", JOptionPane.INFORMATION_MESSAGE);
        });

        container = getContentPane();

        menuBar = new JMenuBar();

        menuFile = new JMenu("File");
        menuFormat = new JMenu("Format");
        menuHelp = new JMenu("Help");

        fileOpen = new JMenuItem("Open... [Ctrl+O]");
        fileOpen.addActionListener(actionFileOpen);
        fileSave = new JMenuItem("Save [Ctrl+S]");
        fileSave.addActionListener(actionFileSave);
        fileSaveAs = new JMenuItem("Save as... [Ctrl+Shift+S]");
        fileSaveAs.addActionListener(actionFileSaveAs);
        fileClose = new JMenuItem("Close");
        fileClose.addActionListener(actionFileClose);

        menuFile.add(fileOpen);
        menuFile.add(fileSave);
        menuFile.add(fileSaveAs);
        menuFile.add(new JSeparator());
        menuFile.add(fileClose);

        formatWordWrap = new JCheckBoxMenuItem("Word wrap", true);
        formatWordWrap.addActionListener(actionFormatWordWrap);

        formatFont = new JMenuItem("Font...");
        formatFont.addActionListener(actionFormatFont);

        menuFormat.add(formatFont);
        menuFormat.add(formatWordWrap);

        viewAbout = new JMenuItem("About Jotepad");
        viewAbout.addActionListener(actionHelpAbout);

        menuHelp.add(viewAbout);

        menuBar.add(menuFile);
        menuBar.add(menuFormat);
        menuBar.add(menuHelp);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setFont(defaultFont);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    if (String.format("%s", (char) e.getKeyCode()).equals("O")) {
                        openFile();
                    } else if (e.isShiftDown() && String.format("%s", (char) e.getKeyCode()).equals("S")) {
                        saveFileAs();
                    } else if (String.format("%s", (char) e.getKeyCode()).equals("S")) {
                        saveFile();
                    }
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

    protected static JTextArea getTextArea() {
        return textArea;
    }

    private void toggleLineWrap() {
        textArea.setLineWrap(!textArea.getLineWrap());
    }

    private void saveFile() {
        textArea.setEditable(false);
        if (savedFile == null) {
            saveFileAs();
        } else {
            writeContent();
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
                writeContent();
                this.setTitle(String.format("%s v%s | %s", TITLE, VERSION, savedFile.toString()));
            } else {
                int answer = JOptionPane.showConfirmDialog(container,
                        "This file already exists, do you want overwrite it?", "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (answer == JOptionPane.YES_OPTION) {
                    writeContent();
                }

            }

        }
        textArea.setEditable(true);
    }

    private byte[] putDataInBuffer() {
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

    private void writeContent() {
        try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(savedFile))) {
            byte[] buffer = putDataInBuffer();

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

    protected static void changeFont(JTextArea areaDeTexto, String nombreFuente, int tamañoFuente) {
        areaDeTexto.setFont(new Font(nombreFuente, 0, tamañoFuente));
    }

    private void openFile() {
        int answer;
        if (fileChooser.showOpenDialog(container) != JFileChooser.CANCEL_OPTION) {
            openedFile = savedFile = new File(fileChooser.getSelectedFile().getAbsolutePath());
            this.setTitle(String.format("%s v%s | %s", TITLE, VERSION, savedFile.toString()));

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
        new View();
    }
}
