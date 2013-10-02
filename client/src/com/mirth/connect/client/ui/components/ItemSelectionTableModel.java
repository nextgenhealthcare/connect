/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

public class ItemSelectionTableModel<K, V> extends AbstractTableModel {
    public final static int NUM_COLUMNS = 3;
    public final static int VALUE_COLUMN = 1;
    public final static int CHECKBOX_COLUMN = 2;
    public final static int KEY_COLUMN = 0;

    private Object[][] tableData = null;
    private String[] columnNames = new String[NUM_COLUMNS];
    private boolean[] canEdit = new boolean[] { false, false, true };

    /**
     * Table model that represents a list of key/value pairs, with values shown in the first column
     * and checkboxes in the second
     * 
     * @param items
     *            A list of key/value pairs
     * @param selectedKeys
     *            A list of the keys that should be initially selected
     */
    public ItemSelectionTableModel(Map<K, V> items, List<K> selectedKeys, String valueColumnName, String checkboxColumnName, String keyColumnName) {
        tableData = new Object[items.size()][NUM_COLUMNS];
        int i = 0;

        for (Entry<K, V> entry : items.entrySet()) {
            tableData[i][VALUE_COLUMN] = entry.getValue();
            tableData[i][CHECKBOX_COLUMN] = (selectedKeys == null || selectedKeys.contains(entry.getKey())) ? Boolean.TRUE : Boolean.FALSE;
            tableData[i][KEY_COLUMN] = entry.getKey();
            i++;
        }

        columnNames[KEY_COLUMN] = keyColumnName;
        columnNames[VALUE_COLUMN] = valueColumnName;
        columnNames[CHECKBOX_COLUMN] = checkboxColumnName;
    }

    public ItemSelectionTableModel(Map<K, V> items, List<K> selectedKeys, String valueColumnName, String checkboxColumnName) {
        this(items, selectedKeys, valueColumnName, checkboxColumnName, null);
    }

    public List<K> getKeys(boolean selected) {
        List<K> selectedKeys = new ArrayList<K>();
        int rowCount = getRowCount();

        for (int i = 0; i < rowCount; i++) {
            if ((Boolean) getValueAt(i, CHECKBOX_COLUMN) == selected) {
                selectedKeys.add((K) getValueAt(i, KEY_COLUMN));
            }
        }

        return selectedKeys;
    }

    public void unselectAllKeys() {
        for (int i = 0; i < tableData.length; i++) {
            tableData[i][CHECKBOX_COLUMN] = Boolean.FALSE;
        }

        fireTableDataChanged();
    }

    public void selectKey(K key) {
        int rowCount = getRowCount();

        for (int i = 0; i < rowCount; i++) {
            if (key.equals((K) getValueAt(i, KEY_COLUMN))) {
                tableData[i][CHECKBOX_COLUMN] = Boolean.TRUE;
            }
        }

        fireTableDataChanged();
    }

    public void selectAllKeys() {
        for (int i = 0; i < tableData.length; i++) {
            tableData[i][CHECKBOX_COLUMN] = Boolean.TRUE;
        }

        fireTableDataChanged();
    }

    public void invertSelection() {
        for (int i = 0; i < tableData.length; i++) {
            tableData[i][CHECKBOX_COLUMN] = ((Boolean) tableData[i][CHECKBOX_COLUMN]) ? Boolean.FALSE : Boolean.TRUE;
        }

        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return tableData.length;
    }

    @Override
    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int i, int j) {
        return tableData[i][j];
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        tableData[rowIndex][columnIndex] = value;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit[columnIndex];
    }
}
