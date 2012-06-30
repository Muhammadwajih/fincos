package pt.uc.dei.fincos.controller.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Customized renderer that adds an icon for the items in a
 * combo-box.
 */
class ComboBoxRenderer extends JLabel implements ListCellRenderer {

    /** Serial id. */
    private static final long serialVersionUID = -2207858845314916020L;

    /** List of items. */
    private String[] texts;

    /** List of icons. */
    private ImageIcon[] images;

    /** Font of the combo-box list.*/
    private static final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    public ComboBoxRenderer(String[] texts, ImageIcon[] images) {
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
        this.images = images;
        this.texts = texts;
    }

    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        if (value != null) {
            int selectedIndex = ((Integer) value).intValue();
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(Color.WHITE);
                setForeground(list.getForeground());
            }

            setIcon(images[selectedIndex]);
            setText(texts[selectedIndex]);
            setFont(font);
        }

        return this;
    }
}
