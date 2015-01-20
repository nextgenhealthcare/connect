/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.mirth.connect.client.ui.AbstractDashboardTableNode;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.SortableTreeTable;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MirthTreeTable extends SortableTreeTable {
    private Preferences userPreferences;
    private String prefix;

    private Set<String> defaultVisibleColumns;
    private Set<String> metaDataColumns;

    public MirthTreeTable() {
        this(null, null);
    }

    public MirthTreeTable(String prefix, Set<String> defaultVisibleColumns) {
        this.setDragEnabled(true);
        this.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                boolean isAccelerated = (((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) > 0) || ((e.getModifiers() & InputEvent.CTRL_MASK) > 0));
                if ((e.getKeyCode() == KeyEvent.VK_S) && isAccelerated) {
                    PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                }
            }

            public void keyReleased(KeyEvent e) {}

            public void keyTyped(KeyEvent e) {}
        });

        /*
         * Swingx 1.0 has this set to true by default, which doesn't allow dragging and dropping
         * into tables. Swingx 0.8 had this set to false. Tables that want it set to true can
         * override it.
         */
        this.putClientProperty("terminateEditOnFocusLost", Boolean.FALSE);
        this.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                saveColumnOrder();
            }
        });

        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        this.prefix = prefix;
        this.defaultVisibleColumns = defaultVisibleColumns;
    }

    public void setDefaultVisibleColumns(Set<String> defaultVisibleColumns) {
        this.defaultVisibleColumns = defaultVisibleColumns;
    }

    public void setMetaDataColumns(Set<String> metaDataColumns) {
        this.metaDataColumns = metaDataColumns;
    }

    public void restoreColumnOrder(TreeTableModel model) {
        if (StringUtils.isNotEmpty(prefix) && model.getColumnCount() > 0) {
            try {
                String columns = userPreferences.get(prefix + "ColumnOrderNames", "");
                if (StringUtils.isNotEmpty(columns)) {
                    Map<String, Integer> columnOrder = (HashMap<String, Integer>) ObjectXMLSerializer.getInstance().deserialize(columns, Map.class);

                    if (defaultVisibleColumns != null && !CollectionUtils.subtract(defaultVisibleColumns, columnOrder.keySet()).isEmpty()) {
                        restoreDefaults(defaultVisibleColumns);
                    }

                    TableColumnModelExt columnModel = (TableColumnModelExt) getTableHeader().getColumnModel();
                    if (columnModel.getColumnCount() > 0) {
                        for (Map.Entry<String, Integer> entry : columnOrder.entrySet()) {
                            String columnName = entry.getKey();
                            int index = entry.getValue();

                            if (index > -1) {
                                columnModel.moveColumn(columnModel.getColumnIndex(columnName), index);
                            }

                            TableColumnExt columnExt = getColumnExt(columnName);
                            if (columnExt != null) {
                                getColumnExt(columnName).setVisible(index > -1);
                            }
                        }
                    }
                } else {
                    for (int modelIndex = 0; modelIndex < columnModel.getColumnCount(); modelIndex++) {
                        TableColumnExt column = getColumnExt(modelIndex);
                        String columnName = column.getTitle();

                        boolean defaultVisible = false;
                        if (defaultVisibleColumns.contains(columnName)) {
                            defaultVisible = true;
                        }

                        column.setVisible(defaultVisible);
                    }
                }
            } catch (Exception e) {
                userPreferences.put(prefix + "DefaultVisibleColumns", "");
                userPreferences.put(prefix + "ColumnOrderNames", "");
            }
        }
    }

    public void saveSortOrder(int column) {
        if (StringUtils.isNotEmpty(prefix)) {
            userPreferences.put(prefix + "SortOrder", ObjectXMLSerializer.getInstance().serialize(getSortOrder(column)));
            userPreferences.putInt(prefix + "SortOrderColumn", column);
        }
    }

    public int restoreSortOrder() {
        int sortOrderColumn = -1;
        if (StringUtils.isNotEmpty(prefix)) {
            sortOrderColumn = userPreferences.getInt(prefix + "SortOrderColumn", -1);
            if (sortOrderColumn > -1) {
                SortableTreeTableModel model = (SortableTreeTableModel) getTreeTableModel();

                model.setColumnAndToggleSortOrder(sortOrderColumn);
                model.setSortOrder(ObjectXMLSerializer.getInstance().deserialize(userPreferences.get(prefix + "SortOrder", ""), SortOrder.class));
            }
        }
        return sortOrderColumn;
    }

    public void restoreDefaults(Set<String> defaultColumnOrder) {
        if (StringUtils.isNotEmpty(prefix)) {
            Map<String, Integer> columnOrder = new HashMap<String, Integer>();

            setColumnSequence(defaultColumnOrder.toArray());

            int index = 0;
            for (TableColumn column : getColumns(true)) {
                TableColumnExt columnExt = (TableColumnExt) column;
                String columnName = columnExt.getTitle();

                boolean enable = defaultColumnOrder.contains(columnExt.getTitle());
                columnExt.setVisible(enable);

                if (enable && (metaDataColumns == null || !metaDataColumns.contains(columnName))) {
                    columnOrder.put(columnExt.getTitle(), index);
                    index++;
                } else {
                    columnOrder.put(columnExt.getTitle(), -1);
                }
            }

            savePreferences(columnOrder);

            resetSortOrder();
            userPreferences.put(prefix + "SortOrder", "");
            userPreferences.putInt(prefix + "SortOrderColumn", -1);
        }
    }

    public void saveColumnOrder() {
        if (StringUtils.isNotEmpty(prefix)) {
            Map<String, Integer> columnOrder = new HashMap<String, Integer>();
            Map<String, Integer> offset = new HashMap<String, Integer>();

            if (metaDataColumns != null) {
                for (TableColumn column : getColumns(true)) {
                    String columnName = (String) column.getHeaderValue();
                    TableColumnExt columnExt = (TableColumnExt) column;
                    if (metaDataColumns.contains(columnName)) {
                        offset.put(columnName, convertColumnIndexToView(columnExt.getModelIndex()));
                    }
                }
            }

            for (TableColumn column : getColumns(true)) {
                String columnName = (String) column.getHeaderValue();
                TableColumnExt columnExt = (TableColumnExt) column;
                
                if (columnExt.isVisible() && (metaDataColumns == null || !metaDataColumns.contains(columnName))) {
                    int columnIndex = this.convertColumnIndexToView(columnExt.getModelIndex());
                    if (offset.size() > 0) {
                        for (Map.Entry<String, Integer> entry : offset.entrySet()) {
                            int metaDataIndex = entry.getValue();
                            if (metaDataIndex != -1 && metaDataIndex < columnIndex) {
                                columnIndex--;
                            }
                        }
                    }

                    columnOrder.put(columnName, columnIndex);
                } else {
                    columnOrder.put(columnName, -1);
                }
            }

            savePreferences(columnOrder);
        }
    }

    private void savePreferences(Map<String, Integer> columnOrder) {
        try {
            userPreferences.put(prefix + "ColumnOrderNames", ObjectXMLSerializer.getInstance().serialize(new HashMap<String, Integer>(columnOrder)));
        } catch (Exception e) {
            userPreferences.put(prefix + "ColumnOrderNames", "");
        }
    }

    public void setCustomEditorControls(boolean enabled) {
        if (enabled) {
            // An action to toggle cell editing with the 'Enter' key.
            Action toggleEditing = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (isEditing()) {
                        getCellEditor().stopCellEditing();
                    } else {
                        boolean success = editCellAt(getSelectedRow(), getSelectedColumn(), e);

                        if (success) {
                            // Request focus for TextFieldCellEditors
                            if (getCellEditor() instanceof TextFieldCellEditor) {
                                ((TextFieldCellEditor) getCellEditor()).getTextField().requestFocusInWindow();
                            }
                        }
                    }
                }
            };

            /*
             * Don't edit cells on any keystroke. Let the toggleEditing action handle it for 'Enter'
             * only. Also surrender focus to any activated editor.
             */
            setAutoStartEditOnKeyStroke(false);
            setSurrendersFocusOnKeystroke(true);
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "toggleEditing");
            getActionMap().put("toggleEditing", toggleEditing);
        } else {
            setAutoStartEditOnKeyStroke(true);
            setSurrendersFocusOnKeystroke(false);
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "selectNextRowCell");
        }
    }

    @Override
    public Class getColumnClass(int column) {
        try {
            if (getRowCount() >= 0 && column >= 0 && column < getColumnCount() && getValueAt(0, column) != null) {
                return getValueAt(0, column).getClass();
            } else {
                return Object.class;
            }
        } catch (Exception e) {
            return Object.class;
        }
    }

    public int getColumnViewIndex(String columnName) {
        return this.getColumnModel().getColumnIndex(columnName);
    }

    public int getColumnModelIndex(String columnName) {
        return this.convertColumnIndexToModel(this.getColumnModel().getColumnIndex(columnName));
    }

    public int getSelectedModelIndex() {
        int index = -1;

        if (this.isEditing()) {
            index = this.getEditingRow();
        } else {
            index = this.getSelectedRow();
        }

        if (index == -1) {
            return index;
        }

        return this.convertRowIndexToModel(index);
    }

    public int[] getSelectedModelRows() {
        int[] views = this.getSelectedRows();

        for (int i = 0; i < views.length; i++) {
            views[i] = this.convertRowIndexToModel(views[i]);
        }

        return views;
    }

    public List<AbstractDashboardTableNode> getSelectedNodes() {
        List<AbstractDashboardTableNode> nodes = new ArrayList<AbstractDashboardTableNode>();

        int[] selectedRows = this.getSelectedModelRows();

        for (int i = 0; i < selectedRows.length; i++) {
            nodes.add((AbstractDashboardTableNode) this.getPathForRow(selectedRows[i]).getLastPathComponent());
        }

        return nodes;
    }
}