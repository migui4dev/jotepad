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
    private final Container container;
    private final JPanel panel;
    private static String[] systemFonts;
    private JComboBox<String> comboFonts;
    private JComboBox<Integer> comboSizes;
    private final Integer[] sizes;
    private final JButton buttonOk;
    private final ActionListener actionOk;
    private final WindowAdapter wa;
    private JTextArea demonstrationText;
    private final ItemListener actionComboFonts;

    public FontSelector() {
        requestFocus();

        actionOk = (e -> {
            View.changeFont(View.getTextArea(), (String) comboFonts.getSelectedItem(),
                    (int) comboSizes.getSelectedItem());
            dispose();
        });

        wa = new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setAlwaysOnTop(true);
            }
        };

        actionComboFonts = (e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                View.changeFont(demonstrationText, (String) e.getItem(), 48);
            }

        });

        container = getContentPane();
        container.setLayout(new GridBagLayout());
        panel = (JPanel) container;
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        demonstrationText = new JTextArea("Aa");
        demonstrationText.setEditable(false);
        demonstrationText.setFont(new Font("Liberation Sans", 0, 48));

        int j = 10;

        sizes = new Integer[30];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = j;
            j += 2;
        }

        systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        comboFonts = new JComboBox<>(systemFonts);
        comboFonts.addItemListener(actionComboFonts);
        comboSizes = new JComboBox<>(sizes);
        buttonOk = new JButton("OK");
        buttonOk.addActionListener(actionOk);

        container.add(comboFonts);
        container.add(comboSizes);
        container.add(buttonOk);
        container.add(demonstrationText);

        pack();
        setTitle(TITLE);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(wa);

    }

}
