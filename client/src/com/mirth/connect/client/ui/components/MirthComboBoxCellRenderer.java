/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import javax.swing.JComboBox;

public class MirthComboBoxCellRenderer implements TableCellRenderer {

    JComboBox comboBox;

    public MirthComboBoxCellRenderer(Object[] items) {
        comboBox = new JComboBox(items);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (isSelected) {
            comboBox.setForeground(table.getSelectionForeground());
            comboBox.setBackground(table.getSelectionBackground());
        } else {
            comboBox.setForeground(table.getForeground());
            comboBox.setBackground(table.getBackground());
        }

        comboBox.setSelectedItem(value);
        return comboBox;
    }
}
