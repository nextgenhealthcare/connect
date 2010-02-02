package com.webreach.mirth.client.ui.components;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import javax.swing.JComboBox;

public class MirthComboBoxCellRenderer extends JComboBox implements TableCellRenderer {

    public MirthComboBoxCellRenderer(String[] items) {
        super(items);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }

        // Select the current value
        setSelectedItem(value);
        return this;
    }
}
