/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MirthTable extends JXTable {
    private Preferences userPreferences;
    private String prefix;
    private Set<String> defaultVisibleColumns;
    private Map<String, Integer> columnOrderMap;
    private List<SortKey> sortKeys;

    private MouseAdapter tableSortAdapter;

    public MirthTable() {
        this(null, null);
    }

    public MirthTable(String prefix, Set<String> defaultVisibleColumns) {
        super();

        this.prefix = prefix;
        this.defaultVisibleColumns = defaultVisibleColumns;

        userPreferences = Preferences.userNodeForPackage(Mirth.class);
        columnOrderMap = new HashMap<String, Integer>();
        if (StringUtils.isNotEmpty(prefix)) {
            try {
                userPreferences = Preferences.userNodeForPackage(Mirth.class);
                String columns = userPreferences.get(prefix + "ColumnOrderMap", "");

                if (StringUtils.isNotEmpty(columns)) {
                    columnOrderMap = (Map<String, Integer>) ObjectXMLSerializer.getInstance().deserialize(columns, Map.class);
                }
            } catch (Exception e) {
            }

            sortKeys = new ArrayList<SortKey>();
            try {
                String sortOrder = userPreferences.get(prefix + "SortOrder", "");

                if (StringUtils.isNotEmpty(sortOrder)) {
                    sortKeys = ObjectXMLSerializer.getInstance().deserialize(sortOrder, List.class);
                }
            } catch (Exception e) {
            }
        }

        setDragEnabled(true);
        addKeyListener(new KeyListener() {

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
        putClientProperty("terminateEditOnFocusLost", Boolean.FALSE);

        getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                saveColumnOrder();
            }
        });

        final JButton columnControlButton = new JButton(new ColumnControlButton(this).getIcon());

        columnControlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu columnMenu = getColumnMenu();
                Dimension buttonSize = columnControlButton.getSize();
                int xPos = columnControlButton.getComponentOrientation().isLeftToRight() ? buttonSize.width - columnMenu.getPreferredSize().width : 0;
                columnMenu.show(columnControlButton, xPos, columnControlButton.getHeight());
            }
        });

        setColumnControl(columnControlButton);
    }

    public void setMirthColumnControlEnabled(boolean enabled) {
        if (enabled) {
            if (tableSortAdapter == null) {
                tableSortAdapter = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            getColumnMenu().show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                };

                getTableHeader().addMouseListener(tableSortAdapter);
            }
        } else {
            if (tableSortAdapter != null) {
                getTableHeader().removeMouseListener(tableSortAdapter);
                tableSortAdapter = null;
            }
        }

        setColumnControlVisible(enabled);
    }

    private JPopupMenu getColumnMenu() {
        JPopupMenu columnMenu = new JPopupMenu();
        DefaultTableModel model = (DefaultTableModel) getModel();

        for (int i = 0; i < model.getColumnCount(); i++) {
            final String columnName = model.getColumnName(i);
            // Get the column object by name. Using an index may not return the column object if the column is hidden
            TableColumnExt column = getColumnExt(columnName);

            // Create the menu item
            final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(columnName);
            // Show or hide the checkbox
            menuItem.setSelected(column.isVisible());

            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    TableColumnExt column = getColumnExt(menuItem.getText());
                    // Determine whether to show or hide the selected column
                    boolean enable = !column.isVisible();
                    // Do not hide a column if it is the last remaining visible column              
                    if (enable || getColumnCount() > 1) {
                        column.setVisible(enable);
                        saveColumnOrder();
                    }
                }
            });

            columnMenu.add(menuItem);
        }

        columnMenu.addSeparator();

        JMenuItem menuItem = new JMenuItem("Restore Default");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                restoreDefaultColumnPreferences();

            }

        });
        columnMenu.add(menuItem);

        return columnMenu;
    }

    @Override
    public void sorterChanged(RowSorterEvent event) {
        super.sorterChanged(event);
        try {
            if (StringUtils.isNotEmpty(prefix)) {
                sortKeys = new ArrayList<SortKey>(getRowSorter().getSortKeys());
                userPreferences.put(prefix + "SortOrder", ObjectXMLSerializer.getInstance().serialize(sortKeys));
            }
        } catch (Exception e) {
        }
    }

    public void restoreColumnPreferences() {
        try {
            if (StringUtils.isNotEmpty(prefix)) {
                TableColumnModelExt columnModel = (TableColumnModelExt) getColumnModel();
                TreeMap<Integer, Integer> columnOrder = new TreeMap<Integer, Integer>();

                int columnIndex = 0;
                for (TableColumn column : columnModel.getColumns(true)) {
                    String columnName = (String) column.getIdentifier();
                    Integer viewIndex = columnOrderMap.get(columnName);
                    TableColumnExt columnExt = getColumnExt(columnName);

                    boolean visible = false;
                    if (viewIndex == null) {
                        visible = defaultVisibleColumns == null || defaultVisibleColumns.contains(columnName);
                    } else {
                        visible = viewIndex > -1;
                    }

                    columnExt.setVisible(visible);
                    if (viewIndex != null && viewIndex > -1) {
                        columnOrder.put(viewIndex, columnIndex);
                    }

                    columnIndex++;
                }

                int viewIndex = 0;
                for (int index : columnOrder.values()) {
                    columnModel.moveColumn(convertColumnIndexToView(index), viewIndex++);
                }

                if (sortKeys != null && !sortKeys.isEmpty()) {
                    getRowSorter().setSortKeys(sortKeys);
                }
            }
        } catch (Exception e) {
            restoreDefaultColumnPreferences();
        }
    }

    private void restoreDefaultColumnPreferences() {
        try {
            userPreferences.put(prefix + "ColumnOrderMap", "");
            userPreferences.put(prefix + "SortOrder", "");

            if (StringUtils.isNotEmpty(prefix)) {
                columnOrderMap.clear();
                /*
                 * To preserve the order of all columns, including those that should be hidden, we
                 * must perform the following loops: First, we need to make all columns visible.
                 * Then, all the columns are arranged into their default ordering. Finally, all
                 * columns that should be hidden have their visibility set to false.
                 */
                for (TableColumn column : getColumns(true)) {
                    TableColumnExt columnExt = (TableColumnExt) column;
                    columnExt.setVisible(true);
                }

                int index = 0;
                for (TableColumn column : getColumns(true)) {
                    TableColumnExt columnExt = (TableColumnExt) column;
                    columnModel.moveColumn(columnModel.getColumnIndex(columnExt.getTitle()), index++);
                }

                for (TableColumn column : getColumns(true)) {
                    TableColumnExt columnExt = (TableColumnExt) column;
                    columnExt.setVisible(defaultVisibleColumns.contains(columnExt.getTitle()));
                }

                sortKeys.clear();
                getRowSorter().setSortKeys(null);
            }
        } catch (Exception e) {
        }
    }

    private void saveColumnOrder() {
        try {
            if (StringUtils.isNotEmpty(prefix)) {
                columnOrderMap.clear();

                for (TableColumn column : getColumns(true)) {
                    columnOrderMap.put((String) column.getHeaderValue(), convertColumnIndexToView(column.getModelIndex()));
                }

                userPreferences.put(prefix + "ColumnOrderMap", ObjectXMLSerializer.getInstance().serialize(columnOrderMap));
            }
        } catch (Exception e) {
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
}
