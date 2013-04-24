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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.mirth.connect.client.ui.UIConstants;

public class MirthComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    protected JComboBox comboBox;
    private int clickCount;

    public MirthComboBoxTableCellEditor(JTable table, Object[] items, int clickCount, boolean focusable, final ActionListener actionListener) {
        this.clickCount = clickCount;

        comboBox = new JComboBox(items);
        comboBox.setFocusable(focusable);
        comboBox.setMaximumRowCount(20);
        comboBox.setForeground(table.getForeground());
        comboBox.setBackground(table.getBackground());
        comboBox.setRenderer(new DataTypeListCellRenderer());

        // Allow an action listener to be passed in. However, we need to fireEditingStopped after the action has finished so we wrap it in another listener
        comboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (actionListener != null) {
                    actionListener.actionPerformed(e);
                }
                fireEditingStopped();
            }

        });

        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4515838
        // Workaround to remove the border around the comboBox
        for (int i = 0; i < comboBox.getComponentCount(); i++) {
            if (comboBox.getComponent(i) instanceof AbstractButton) {
                ((AbstractButton) comboBox.getComponent(i)).setBorderPainted(false);
            }
        }
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= clickCount;
        }

        return false;
    }

    @Override
    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Only the selected component will be edited, so always set the colors to the table's selection colors.
        comboBox.setForeground(table.getSelectionForeground());
        comboBox.setBackground(table.getSelectionBackground());

        for (int i = 0; i < comboBox.getComponentCount(); i++) {
            if (comboBox.getComponent(i) instanceof AbstractButton) {
                comboBox.getComponent(i).setBackground(comboBox.getBackground());
            }
        }

        comboBox.setSelectedItem(value);
        return comboBox;
    }

    @SuppressWarnings("serial")
    private class DataTypeListCellRenderer extends DefaultListCellRenderer {

        public DataTypeListCellRenderer() {}

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (index >= 0) {
                if (!isSelected) {
                    component.setBackground(UIConstants.BACKGROUND_COLOR);
                }
            }

            return component;
        }
    }

}
