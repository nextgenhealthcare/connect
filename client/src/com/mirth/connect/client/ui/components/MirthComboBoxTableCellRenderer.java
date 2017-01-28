/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class MirthComboBoxTableCellRenderer implements TableCellRenderer {

    protected JComboBox comboBox;

    public MirthComboBoxTableCellRenderer(Object[] items) {
        comboBox = new JComboBox(items);

        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4515838
        // Workaround to remove the border around the comboBox
        for (int i = 0; i < comboBox.getComponentCount(); i++) {
            if (comboBox.getComponent(i) instanceof AbstractButton) {
                ((AbstractButton) comboBox.getComponent(i)).setBorderPainted(false);
            }
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            comboBox.setForeground(table.getSelectionForeground());
            comboBox.setBackground(table.getSelectionBackground());
        } else {
            comboBox.setForeground(table.getForeground());
            comboBox.setBackground(table.getBackground());
        }

        for (int i = 0; i < comboBox.getComponentCount(); i++) {
            Component component = comboBox.getComponent(i);
            component.setBackground(comboBox.getBackground());

            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
                button.setVisible(table.isCellEditable(row, column));
            } else if (component instanceof JComponent) {
                ((JComponent) component).setBorder(new EmptyBorder(0, 3, 0, 0));

                if (isSelected) {
                    ((JComponent) component).setBackground(table.getSelectionBackground());
                } else if (row % 2 == 0) {
                    ((JComponent) component).setBackground(new Color(242, 242, 242));
                }
            }
        }

        if (value != null) {
            comboBox.setSelectedItem(value);
        } else {
            comboBox.setSelectedIndex(-1);
        }

        return comboBox;
    }

    public JComboBox getComboBox() {
        return comboBox;
    }

    public void setEditable(boolean editable) {
        comboBox.setEditable(editable);
    }
}
