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
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SortOrder;

import org.apache.commons.lang3.StringUtils;

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

    public MirthTreeTable() {
        this(null);
    }

    public MirthTreeTable(String prefix) {
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

        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        this.prefix = prefix;
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

    public void restoreDefaults() {
        if (StringUtils.isNotEmpty(prefix)) {
            resetSortOrder();
            userPreferences.put(prefix + "SortOrder", "");
            userPreferences.putInt(prefix + "SortOrderColumn", -1);
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
             * Don't edit cells on any keystroke. Let the toggleEditing action
             * handle it for 'Enter' only. Also surrender focus to any activated
             * editor.
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
      
       for (int i = 0; i < selectedRows.length; i++)
       {
           nodes.add((AbstractDashboardTableNode) this.getPathForRow(selectedRows[i]).getLastPathComponent());
       }
      
       return nodes;
   }
}
