/* FINCoS Framework
 * Copyright (C) 2013 CISUC, University of Coimbra
 *
 * Licensed under the terms of The GNU General Public License, Version 2.
 * A copy of the License has been included with this distribution in the
 * fincos-license.txt file.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 */


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
 *
 * @author  Marcelo R.N. Mendes
 *
 */
@SuppressWarnings("rawtypes")
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
