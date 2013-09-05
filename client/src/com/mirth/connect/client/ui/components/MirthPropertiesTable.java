/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;

public class MirthPropertiesTable extends MirthTable {
    private final static String PROPERTY_TITLE = "Property";
    private final static String VALUE_TITLE = "Value";

    private Frame parent;
    private DefaultTableModel model;
    private int lastIndex;

    public MirthPropertiesTable() {
        this.parent = PlatformUI.MIRTH_FRAME;

        model = new javax.swing.table.DefaultTableModel(null, new String[] { PROPERTY_TITLE,
                VALUE_TITLE }) {
            boolean[] canEdit = new boolean[] { true, true };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

        setModel(model);
        getPropertyColumn().setCellEditor(new CellEditor(true, null));
        getValueColumn().setCellEditor(new CellEditor(false, null));
        setCustomEditorControls(true);
        setSelectionMode(0);
        setRowSelectionAllowed(true);
        setRowHeight(UIConstants.ROW_HEIGHT);
        setDragEnabled(false);
        setOpaque(true);
        setSortable(false);
        getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            setHighlighters(highlighter);
        }
    }
    
    public TableColumn getPropertyColumn() {
        return getColumnModel().getColumn(getColumnModel().getColumnIndex(PROPERTY_TITLE));
    }

    public TableColumn getValueColumn() {
        return getColumnModel().getColumn(getColumnModel().getColumnIndex(VALUE_TITLE));
    }
    
    public void setNewButton(final JButton newButton) {
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.addRow(new String[] { getNewPropertyName(), "" });
                setRowSelectionInterval(getRowCount() - 1, getRowCount() - 1);
                parent.setSaveEnabled(true);
            }
        });
    }

    public void setDeleteButton(final JButton deleteButton) {
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = (isEditing()) ? getEditingRow() : getSelectedRow();

                if (selectedRow != -1 && !isEditing()) {
                    model.removeRow(selectedRow);

                    if (getRowCount() != 0) {
                        if (lastIndex == 0) {
                            setRowSelectionInterval(0, 0);
                        } else if (lastIndex == getRowCount()) {
                            setRowSelectionInterval(lastIndex - 1, lastIndex - 1);
                        } else {
                            setRowSelectionInterval(lastIndex, lastIndex);
                        }
                    }

                    parent.setSaveEnabled(true);
                }
            }
        });

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                int selectedRow = (isEditing()) ? getEditingRow() : getSelectedRow();

                if (selectedRow != -1) {
                    lastIndex = selectedRow;
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });

        getColumnModel().getColumn(getColumnModel().getColumnIndex(PROPERTY_TITLE)).setCellEditor(new CellEditor(true, deleteButton));
        getColumnModel().getColumn(getColumnModel().getColumnIndex(VALUE_TITLE)).setCellEditor(new CellEditor(false, deleteButton));
    }

    public void setProperties(Map<String, String> properties) {
        model.setRowCount(0);

        for (Entry<String, String> entry : properties.entrySet()) {
            model.addRow(new String[] { entry.getKey(), entry.getValue() });
        }
    }

    public Map<String, String> getProperties() {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        int rowCount = model.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            properties.put(model.getValueAt(i, 0).toString(), model.getValueAt(i, 1).toString());
        }

        return properties;
    }

    /**
     * Get the name that should be used for a new property so that it is unique.
     */
    private String getNewPropertyName() {
        String temp = "Property ";

        for (int i = 1; i <= getRowCount() + 1; i++) {
            boolean exists = false;

            for (int j = 0; j < getRowCount(); j++) {
                if (((String) getValueAt(j, 0)).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }

            if (!exists) {
                return temp + i;
            }
        }

        return "";
    }

    private class CellEditor extends TextFieldCellEditor {
        private boolean checkProperties;
        private JButton deleteButton;

        CellEditor(boolean checkProperties, JButton deleteButton) {
            super();
            this.checkProperties = checkProperties;
            this.deleteButton = deleteButton;
        }

        boolean checkUniqueProperty(String property) {
            boolean exists = false;

            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, 0) != null && ((String) getValueAt(i, 0)).equalsIgnoreCase(property)) {
                    exists = true;
                }
            }

            return exists;
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            boolean editable = super.isCellEditable(evt);

            if (editable && deleteButton != null) {
                deleteButton.setEnabled(false);
            }

            return editable;
        }

        @Override
        protected boolean valueChanged(String value) {
            if (deleteButton != null) {
                deleteButton.setEnabled(true);
            }

            if (checkProperties && (value.length() == 0 || checkUniqueProperty(value))) {
                return false;
            }

            parent.setSaveEnabled(true);
            return true;
        }
    }
}
