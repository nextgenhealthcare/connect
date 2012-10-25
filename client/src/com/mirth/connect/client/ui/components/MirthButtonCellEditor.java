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
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class MirthButtonCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    JTable table;
    JButton button = new JButton();
    int clickCount = 1;

    public MirthButtonCellEditor(JTable table) {
        this.table = table;
        button.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        button.setText(value.toString());
        button.setFocusable(false);
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= clickCount;
        }
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        super.cancelCellEditing();
    }
}
