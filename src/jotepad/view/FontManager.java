package jotepad.view;

import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author sirmigui
 */
public class FontManager extends JDialog {
	private static final long serialVersionUID = -8162065733123645031L;

	public static final int MAX_SIZE = 120;
	public static final int MIN_SIZE = 6;
	public static final int STEP_SIZE = 4;
	private static final String TITLE = "Font selector";

	private JComboBox<String> fontsCombo;
	private JSpinner sizesSpinner;
	private SpinnerModel spinnerModel;

	private Container container;
	private JPanel panel;
	private JButton buttonOk;

	private String[] systemFonts;
	private MainWindow view;

	public FontManager(MainWindow view) {
		this.view = view;
		requestFocus();
		setAlwaysOnTop(true);

		Font viewFont = view.getTextArea().getFont();

		systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		fontsCombo = new JComboBox<>(systemFonts);
		fontsCombo.setSelectedItem(viewFont.getFontName());
		fontsCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Font f = new Font((String) e.getItem(), Font.PLAIN, (int) spinnerModel.getValue());
					view.getTextArea().setFont(f);
				}
			}
		});

		sizesSpinner = new JSpinner();
		spinnerModel = new SpinnerNumberModel(Math.clamp(viewFont.getSize(), MIN_SIZE, MAX_SIZE), MIN_SIZE, MAX_SIZE,
				STEP_SIZE);
		spinnerModel.addChangeListener(e -> {
			Font f = new Font(fontsCombo.getItemAt(fontsCombo.getSelectedIndex()), Font.PLAIN,
					(int) spinnerModel.getValue());
			view.getTextArea().setFont(f);
		});
		sizesSpinner.setModel(spinnerModel);

		container = getContentPane();
		container.setLayout(new GridBagLayout());
		panel = (JPanel) container;

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		buttonOk = new JButton("OK");
		buttonOk.addActionListener(e -> {
			changeFontValues();
			dispose();
		});

		container.add(fontsCombo);
		container.add(sizesSpinner);
		container.add(buttonOk);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				updateTheme();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				changeFontValues();
			}
		});

		pack();
		setTitle(TITLE);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void changeFontValues() {
		fontsCombo.setSelectedItem(view.getTextArea().getFont().getFontName());
		spinnerModel.setValue(view.getTextArea().getFont().getSize());
	}

	private void updateTheme() {
		SwingUtilities.updateComponentTreeUI(this);
	}

}
