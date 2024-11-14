package jotepad.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jotepad.model.Config;
import jotepad.view.MainWindow;

/**
 *
 * @author migui
 */
public class FileManager {
	private static final String USER_HOME = System.getProperty("user.home");
	private static final String BACKUP_PATH = String.format("%s/.jotepad/backups/", USER_HOME);

	private static final File CONFIG_FILE = new File("config");

	private File file, backupFile;
	private final MainWindow view;

	public FileManager(MainWindow view) {
		this.file = null;
		this.backupFile = null;
		this.view = view;

		File backupFolder = new File(BACKUP_PATH);

		if (!backupFolder.exists()) {
			backupFolder.mkdir();
		}
	}

	public boolean saveFile() {
		if (file == null) {
			return saveFileAs();
		} else {
			byte[] buffer = textToBuffer();
			writeContentInFile(buffer, file);
			writeContentInFile(buffer, backupFile);
			return true;
		}
	}

	public boolean saveFileAs() {
		int fileAnswer = view.getFileChooser().showSaveDialog(view);
		int answer = JOptionPane.NO_OPTION;

		if (fileAnswer != JFileChooser.APPROVE_OPTION) {
			return false;
		}

		file = new File(view.getFileChooser().getSelectedFile().getAbsolutePath());
		backupFile = new File(createBackupFileName());

		if (file.exists()) {
			answer = JOptionPane.showConfirmDialog(view, "This file already exists, do you want overwrite it?",
					"Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		}

		if (answer == JOptionPane.YES_OPTION || !file.exists()) {
			writeContentInFile(textToBuffer(), file);
			writeContentInFile(textToBuffer(), backupFile);
			view.changeTitle(file.getAbsolutePath());
			return true;
		}

		return false;

	}

	public void openFile() {
		if (view.getFileChooser().showOpenDialog(view) == JFileChooser.CANCEL_OPTION) {
			return;
		}

		if (file != null) {
			int answer = JOptionPane.showConfirmDialog(view, "Do you want to save this document?", "Save file",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (answer == JOptionPane.YES_OPTION) {
				saveFile();
			}
		}

		file = new File(view.getFileChooser().getSelectedFile().getAbsolutePath());
		backupFile = new File(createBackupFileName());

		view.changeTitle(file.getAbsolutePath());
		view.getTextArea().setText(readFileContent());
	}

	private byte[] textToBuffer() {
		String value = view.getTextArea().getText();
		byte[] buffer = new byte[value.length()];

		for (int i = 0, len = value.length(); i < len; i++) {
			buffer[i] = (byte) value.charAt(i);
		}

		return buffer;
	}

	private void writeContentInFile(byte[] buffer, File f) {
		try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(f))) {
			if (!f.exists()) {
				f.createNewFile();
			}

			writer.write(buffer, 0, buffer.length);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readFileContent() {
		StringBuilder fileContent = new StringBuilder();

		try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file))) {
			int readedValue = reader.read();

			while (readedValue != -1) {
				fileContent.append((char) readedValue);
				readedValue = reader.read();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileContent.toString();
	}

	private String createBackupFileName() {
		return String.format("%s%s", BACKUP_PATH, file.getName());
	}

	public static Config readConfigFile() {
		if (!CONFIG_FILE.exists()) {
			return null;
		}

		Config c = null;

		try (ObjectInputStream reader = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(CONFIG_FILE)))) {
			c = (Config) reader.readObject();

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return c;
	}

	public static void saveConfigFile(Config c) {
		try (ObjectOutputStream writer = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(CONFIG_FILE)))) {
			if (!CONFIG_FILE.exists()) {
				CONFIG_FILE.createNewFile();
			}

			writer.writeObject(c);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
