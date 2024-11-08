package jotepad.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jotepad.view.MainWindow;

/**
 *
 * @author migui
 */
public class FileManager {
	private static final String USER_HOME = System.getProperty("user.home");
	private static final String BACKUP_PATH = String.format("%s/.jotepad/backups/", USER_HOME);

	private File file, backupFile;
	private final Object lock;
	private final MainWindow view;

	public FileManager(MainWindow view) {
		this.file = null;
		this.backupFile = null;
		this.lock = new Object();
		this.view = view;

		File backupFolder = new File(BACKUP_PATH);

		if (!backupFolder.exists()) {
			backupFolder.mkdir();
		}
	}

	public void saveFile() {
		if (file == null) {
			saveFileAs();
		} else {
			byte[] buffer = textToBuffer();
			writeContentInFile(buffer, file);
			writeContentInFile(buffer, backupFile);
		}
	}

	public void saveFileAs() {
		int fileAnswer = view.getFileChooser().showSaveDialog(view);

		if (fileAnswer != JFileChooser.APPROVE_OPTION) {
			return;
		}

		file = new File(view.getFileChooser().getSelectedFile().getAbsolutePath());
		backupFile = new File(createBackupFileName());

		if (!file.exists()) {
			writeContentInFile(textToBuffer(), file);
			writeContentInFile(textToBuffer(), backupFile);
			view.changeTitle(file.getAbsolutePath());

		} else {
			int answer = JOptionPane.showConfirmDialog(view, "This file already exists, do you want overwrite it?",
					"Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

			if (answer == JOptionPane.YES_OPTION) {
				writeContentInFile(textToBuffer(), file);
				writeContentInFile(textToBuffer(), backupFile);
			}

		}
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

		Thread thread1 = new Thread(() -> {
			synchronized (lock) {
				for (int i = 0; i < value.length() / 2; i++) {
					buffer[i] = (byte) value.charAt(i);
				}
				lock.notify();
			}
		});

		Thread thread2 = new Thread(() -> {
			synchronized (lock) {
				for (int i = value.length() / 2; i < value.length(); i++) {
					buffer[i] = (byte) value.charAt(i);
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
			Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
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

}
