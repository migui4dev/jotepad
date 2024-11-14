package jotepad.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import jotepad.controller.FileManager;
import jotepad.model.Config;

/**
 *
 * @author sirmigui
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = -81894675312554367L;

	private static final LookAndFeel[] THEMES = { new FlatDarkLaf(), new FlatLightLaf(), new FlatMacDarkLaf(),
			new FlatMacLightLaf(), new MetalLookAndFeel(), new NimbusLookAndFeel() };

	private LookAndFeel currentTheme;

	private static final Font DEFAULT_FONT = new Font("Liberation Mono", Font.PLAIN, 16);

	private static final int WINDOW_WIDHT = 800, WINDOW_HEIGHT = WINDOW_WIDHT - 300;
	private static final String VERSION = "0.135";
	private static final String TITLE = "Jotepad";
	private static final String TITLE_FORMAT = "%s v%s | %s";

	private final Container container;
	private final JFileChooser fileChooser;

	// Frame elements:
	private final JMenuBar menuBar;
	private final JMenu menuFile, menuView, menuFormat, menuHelp;
	private final JMenuItem fileOpen, fileSave, fileSaveAs, fileClose;
	private final JMenuItem viewChangeTheme;
	private final JMenuItem formatFont;
	private final JMenuItem helpAbout, helpGitRepo;
	private final JCheckBoxMenuItem formatWordWrap;
	private final JScrollPane scrollBar;
	private final JTextArea textArea;

	private final ArrayList<String> history;
	private final FileManager fileManager;
	private int currentHistoryIndex;
	private FontManager fontSelector;

	private Config config;

	public MainWindow(Config config) {
		this.config = config;

		fileManager = new FileManager(this);
		history = new ArrayList<>();
		currentHistoryIndex = -1;

		fileChooser = new JFileChooser();
		container = getContentPane();

		// MenuBar y sus elementos declarados
		menuBar = new JMenuBar();
		menuFile = new JMenu("File");
		menuView = new JMenu("View");
		menuFormat = new JMenu("Format");
		menuHelp = new JMenu("Help");

		fileOpen = new JMenuItem("Open...");
		fileOpen.addActionListener(e -> {
			fileManager.openFile();
		});

		fileSave = new JMenuItem("Save");
		fileSave.addActionListener(e -> {
			fileManager.saveFile();
		});

		fileSaveAs = new JMenuItem("Save as...");
		fileSaveAs.addActionListener(e -> {
			fileManager.saveFileAs();
		});

		fileClose = new JMenuItem("Close");
		fileClose.addActionListener(e -> {
			System.exit(0);
		});

		menuFile.add(fileOpen);
		menuFile.add(fileSave);
		menuFile.add(fileSaveAs);
		menuFile.add(new JSeparator());
		menuFile.add(fileClose);
		// Termina la declaración de MenuBar y sus elementos

		viewChangeTheme = new JMenu("Change Theme");

		for (int i = 0; i < THEMES.length; i++) {
			JMenuItem menuItemTheme = new JMenuItem(THEMES[i].getName());
			int index = i;

			menuItemTheme.addActionListener(e -> {
				setTheme(index);
			});

			viewChangeTheme.add(menuItemTheme);
		}

		menuView.add(viewChangeTheme);

		formatFont = new JMenuItem("Font...");
		formatFont.addActionListener(e -> {
			if (fontSelector == null) {
				fontSelector = new FontManager(this);
			}
			fontSelector.setVisible(true);
		});

		formatWordWrap = new JCheckBoxMenuItem("Word wrap", true);
		formatWordWrap.addActionListener(e -> {
			toggleLineWrap();
		});

		menuFormat.add(formatFont);
		menuFormat.add(formatWordWrap);

		helpAbout = new JMenuItem("About Jotepad");
		helpAbout.addActionListener(e -> {
			JOptionPane.showMessageDialog(container, "Jotepad it's a text editor programmed in Java.", "About Jotepad",
					JOptionPane.INFORMATION_MESSAGE);
		});

		helpGitRepo = new JMenuItem("GitHub repository");
		helpGitRepo.addActionListener(e -> {
			try {
				Desktop d = Desktop.getDesktop();

				if (!Desktop.isDesktopSupported() || !d.isSupported(Desktop.Action.BROWSE)) {
					JOptionPane.showMessageDialog(this, "The link could not be opened.", "Action not supported",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				d.browse(new URI("https://github.com/sirmigui/jotepad"));
			} catch (URISyntaxException | IOException ex) {
				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
			}
		});

		menuHelp.add(helpAbout);
		menuHelp.add(helpGitRepo);

		menuBar.add(menuFile);
		menuBar.add(menuView);
		menuBar.add(menuFormat);
		menuBar.add(menuHelp);

		textArea = new JTextArea();
		textArea.setBorder(null);
		textArea.setFont(DEFAULT_FONT);
		textArea.setLineWrap(formatWordWrap.isEnabled());

		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();

				if (e.isControlDown()) {
					switch (keyCode) {
					case KeyEvent.VK_PLUS -> {
						zoomText(FontManager.STEP_SIZE);
						return;
					}
					case KeyEvent.VK_MINUS -> {
						zoomText(-FontManager.STEP_SIZE);
						return;
					}
					case KeyEvent.VK_O -> {
						fileManager.openFile();
						return;
					}
					case KeyEvent.VK_S -> {
						if (e.isShiftDown()) {
							fileManager.saveFileAs();
							return;
						} else {
							fileManager.saveFile();
							return;
						}
					}
					case KeyEvent.VK_Z -> {
						undoChanges();
						return;
					}
					}
				}

				if (textArea.getText().length() % 3 == 0) {
					currentHistoryIndex++;
					history.add(textArea.getText());
				}

			}
		});

		scrollBar = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollBar.validate();

		container.setLayout(new BorderLayout());
		container.add(scrollBar, BorderLayout.CENTER);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Font f = textArea.getFont();
				Config c = new Config(currentTheme.getName(), f.getFontName(), f.getSize());

				FileManager.saveConfigFile(c);
			}
		});

		setJMenuBar(menuBar);
		setSize(WINDOW_WIDHT, WINDOW_HEIGHT);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(String.format("%s v%s", TITLE, VERSION));
		setConfig();
	}

	public JTextArea getTextArea() {
		return textArea;
	}

	public JFileChooser getFileChooser() {
		return fileChooser;
	}

	public void changeTitle(String filePath) {
		setTitle(String.format(TITLE_FORMAT, TITLE, VERSION, filePath));
	}

	private void setTheme(int themeIndex) {
		try {
			currentTheme = THEMES[themeIndex];
			UIManager.setLookAndFeel(currentTheme);
			SwingUtilities.updateComponentTreeUI(this);
			fileChooser.updateUI();
		} catch (UnsupportedLookAndFeelException | NullPointerException ex) {
			Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void zoomText(int zoom) {
		Font font = textArea.getFont();
		int size = Math.clamp(font.getSize() + zoom, FontManager.MIN_SIZE, FontManager.MAX_SIZE);
		textArea.setFont(font.deriveFont(Font.PLAIN, size));

	}

	private void toggleLineWrap() {
		textArea.setLineWrap(!textArea.getLineWrap());
	}

	private void undoChanges() {
		if (currentHistoryIndex == 0) {
			return;
		}

		history.remove(history.size() - 1);
		currentHistoryIndex--;
		textArea.setText(history.get(currentHistoryIndex));
	}

	private void setConfig() {
		if (config == null) {
			setTheme(1);
			return;
		}

		for (int i = 0, len = THEMES.length; i < len; i++) {
			if (THEMES[i].getName().equals(config.getThemeName())) {
				setTheme(i);
				break;
			}
		}

		textArea.setFont(new Font(config.getFontName(), Font.PLAIN, config.getFontSize()));
	}

	public static void main(String[] args) {
		Config c = FileManager.readConfigFile();

		java.awt.EventQueue.invokeLater(() -> {
			new MainWindow(c);
		});
	}

}
