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
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.mirth.connect.model.Connector;

/**
 * A table cell editor for the destination table.
 */
public class DestinationTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    // This is the component that will handle the editing of the cell value

    JComponent component = new JTextField();
    Frame parent;
    Object originalValue;

    public DestinationTableCellEditor() {
        super();
        this.parent = PlatformUI.MIRTH_FRAME;
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
        return ((JTextField) component).getText();
    }

    /**
     * This method is called when a cell value is edited by the user.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // 'value' is value contained in the cell located at (rowIndex,
        // vColIndex)
        originalValue = value;

        if (isSelected) {
            // cell (and perhaps other cells) are selected
        }

        // Configure the component with the specified value
        ((JTextField) component).setText((String) value);

        // Return the configured component
        return component;
    }

    /**
     * Checks whether or not the value change is valid.
     */
    private boolean valueChanged(String s) {
        List<Connector> destinationConnectors = parent.channelEditPanel.currentChannel.getDestinationConnectors();

        // make sure the name doesn't already exist
        for (int i = 0; i < destinationConnectors.size(); i++) {
            if (destinationConnectors.get(i).getName().equalsIgnoreCase(s)) {
                return false;
            }
        }

        parent.setSaveEnabled(true);
        // set the name to the new name.
        for (int i = 0; i < destinationConnectors.size(); i++) {
            if (destinationConnectors.get(i).getName().equalsIgnoreCase((String) originalValue)) {
                destinationConnectors.get(i).setName(s);
            }
        }

        return true;
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
}
