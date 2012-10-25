/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public abstract class TextFieldCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JTextField textField = new JTextField();
    private String originalValue;
    private Frame parent;

    /**
     * Checks whether or not the value change is valid.
     * 
     * @param value
     * @return
     */
    protected abstract boolean valueChanged(String value);

    public TextFieldCellEditor() {
        super();

        this.parent = PlatformUI.MIRTH_FRAME;

        textField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                textField.setCaretPosition(textField.getText().length());
            }

            @Override
            public void focusLost(FocusEvent arg0) {}

        });
    }

    /**
     * This method is called just before the cell value is saved. If the value
     * is not valid, false should be returned.
     */
    public boolean stopCellEditing() {
        String s = (String) getCellEditorValue();

        if (!valueChanged(s)) {
            super.cancelCellEditing();
        }
        return super.stopCellEditing();
    }

    /**
     * This method is called when editing is completed. It must return the new
     * value to be stored in the cell.
     */
    public Object getCellEditorValue() {
        return getTextField().getText();
    }

    /**
     * This method is called when a cell value is edited by the user.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // 'value' is value contained in the cell located at (rowIndex,
        // vColIndex)
        setOriginalValue((String) value);

        // Configure the component with the specified value
        getTextField().setText((String) value);

        // Return the configured component
        return getTextField();
    }

    /**
     * Enables the editor only for double-clicks.
     */
    public boolean isCellEditable(EventObject evt) {
        if (evt == null) {
            return false;
        }

        if (evt instanceof MouseEvent) {
            return ((MouseEvent) evt).getClickCount() >= 2;
        }
        return true;
    }

    public JTextField getTextField() {
        return textField;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public Frame getParent() {
        return parent;
    }
}
