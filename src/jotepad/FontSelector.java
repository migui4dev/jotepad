package jotepad;

import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class FontSelector extends JDialog {

    private static final long serialVersionUID = -8162065733123645031L;
    private static final String TITLE = "Font selector";

    private final JComboBox<String> comboFonts;
    private final JComboBox<Integer> comboSizes;

    private final Container container;
    private final JPanel panel;
    private final Integer[] sizes;
    private final JButton buttonOk;
    private final WindowAdapter wa;
    private final ItemListener actionComboFonts, actionComboSizes;

    private static String[] systemFonts;

    public FontSelector() {
        requestFocus();

        int j = 10;

        sizes = new Integer[30];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = j;
            j += 2;
        }

        systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        comboFonts = new JComboBox<>(systemFonts);
        comboSizes = new JComboBox<>(sizes);

        wa = new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setAlwaysOnTop(true);
            }
        };

        actionComboFonts = (e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                View.setFont((String) e.getItem(), (int) comboSizes.getSelectedItem());
            }
        });

        actionComboSizes = (e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                View.setFont((String) comboFonts.getSelectedItem(), (int) e.getItem());
            }
        });

        comboFonts.addItemListener(actionComboFonts);
        comboSizes.addItemListener(actionComboSizes);

        container = getContentPane();
        container.setLayout(new GridBagLayout());
        panel = (JPanel) container;
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        buttonOk = new JButton("OK");
        buttonOk.addActionListener(e -> {
            dispose();
        });

        container.add(comboFonts);
        container.add(comboSizes);
        container.add(buttonOk);

        pack();
        setTitle(TITLE);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(wa);

    }

}
