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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
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
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.client.ui.AbstractDashboardTableNode;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.SortableHeaderCellRenderer;
import com.mirth.connect.client.ui.SortableTreeTable;
import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class MirthTreeTable extends SortableTreeTable {

    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
    private static String PREFERENCE_COLUMN_ORDER_MAP = "TreeColumnOrderMap";
    private static String PREFERENCE_SORT_ORDER = "TreeSortOrder";
    private static String PREFERENCE_SORT_ORDER_COLUMN = "TreeSortOrderColumn";

    private Map<String, Set<String>> customHiddenColumnMap;
    private String channelId;

    private Preferences userPreferences;
    private String prefix;
    private Map<String, Integer> columnOrderMap;
    private int sortOrderColumn;
    private SortOrder sortOrder;

    private Set<String> defaultVisibleColumns;
    private Set<String> metaDataColumns;

    private MouseAdapter treeTableSortAdapter;
    private MouseAdapter rightClickMouseAdapter;

    private TransferHandler transferHandler;

    public MirthTreeTable() {
        this(null, null);
    }

    public MirthTreeTable(String prefix, Set<String> defaultVisibleColumns) {
        customHiddenColumnMap = new HashMap<String, Set<String>>();

        this.prefix = prefix;
        this.defaultVisibleColumns = defaultVisibleColumns;

        columnOrderMap = new HashMap<String, Integer>();
        sortOrderColumn = -1;
        sortOrder = null;

        if (StringUtils.isNotEmpty(prefix)) {
            try {
                userPreferences = Preferences.userNodeForPackage(Mirth.class);
                String columns = userPreferences.get(prefix + PREFERENCE_COLUMN_ORDER_MAP, "");

                if (StringUtils.isNotEmpty(columns)) {
                    columnOrderMap = (Map<String, Integer>) ObjectXMLSerializer.getInstance().deserialize(columns, Map.class);
                }
            } catch (Exception e) {
            }

            try {
                String order = userPreferences.get(prefix + PREFERENCE_SORT_ORDER, "");

                if (StringUtils.isNotEmpty(order)) {
                    sortOrder = ObjectXMLSerializer.getInstance().deserialize(order, SortOrder.class);
                    sortOrderColumn = userPreferences.getInt(prefix + PREFERENCE_SORT_ORDER_COLUMN, -1);
                }
            } catch (Exception e) {
            }
        }

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

        JTableHeader header = getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                saveColumnOrder();
            }
        });
        header.setDefaultRenderer(new SortableHeaderCellRenderer(header.getDefaultRenderer()));

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

    protected void beforeSort() {}

    protected void afterSort() {}

    @Override
    public void setSortable(boolean enable) {
        super.setSortable(enable);

        JTableHeader header = getTableHeader();
        if (enable) {
            if (treeTableSortAdapter == null) {
                treeTableSortAdapter = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            JTableHeader h = (JTableHeader) e.getSource();
                            TableColumnModel columnModel = getColumnModel();

                            int viewColumn = h.columnAtPoint(e.getPoint());
                            int column = columnModel.getColumn(viewColumn).getModelIndex();

                            if (column != -1) {
                                beforeSort();

                                // Toggle sort order (ascending <-> descending)
                                SortableTreeTableModel model = (SortableTreeTableModel) getTreeTableModel();
                                model.setColumnAndToggleSortOrder(column);

                                // Set sorting icon and current column index
                                ((SortableHeaderCellRenderer) getTableHeader().getDefaultRenderer()).setSortingIcon(model.getSortOrder());
                                ((SortableHeaderCellRenderer) getTableHeader().getDefaultRenderer()).setColumnIndex(column);

                                saveSortPreferences(column);

                                afterSort();
                            }
                        }
                    }
                };
                header.addMouseListener(treeTableSortAdapter);
            }
        } else {
            if (treeTableSortAdapter != null) {
                header.removeMouseListener(treeTableSortAdapter);
                treeTableSortAdapter = null;
            }
        }
    }

    public void setMirthColumnControlEnabled(boolean enable) {
        if (enable) {
            if (rightClickMouseAdapter == null) {
                rightClickMouseAdapter = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            getColumnMenu().show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                };

                getTableHeader().addMouseListener(rightClickMouseAdapter);
            }
        } else {
            if (rightClickMouseAdapter != null) {
                getTableHeader().removeMouseListener(rightClickMouseAdapter);
                rightClickMouseAdapter = null;
            }
        }

        setColumnControlVisible(enable);
    }

    public void setMirthTransferHandlerEnabled(boolean enable) {
        setDragEnabled(enable);

        if (transferHandler == null) {
            transferHandler = new TransferHandler() {
                @Override
                public int getSourceActions(JComponent c) {
                    return COPY_OR_MOVE;
                }

                @Override
                protected Transferable createTransferable(JComponent c) {
                    int row = getSelectedRow();

                    List columnValuesList = new ArrayList<Object>();
                    for (TableColumn column : getColumns()) {
                        Object value = getValueAt(row, convertColumnIndexToView(column.getModelIndex()));
                        if (value != null) {
                            if (value instanceof Calendar) {
                                Calendar calendar = (GregorianCalendar) value;
                                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                                dateFormat.setCalendar(calendar);
                                value = (String) dateFormat.format(calendar.getTime());
                            } else if (value instanceof Long) {
                                value = String.valueOf(value);
                            } else if (value instanceof Status) {
                                value = Status.fromChar(((Status) value).getStatusCode());
                            } else {
                                value = String.valueOf(value);
                            }
                        } else {
                            value = "-";
                        }

                        columnValuesList.add(value);
                    }

                    return new StringSelection(StringUtils.join(columnValuesList, " "));
                }
            };

            setTransferHandler(transferHandler);
        }
    }

    private JPopupMenu getColumnMenu() {
        SortableTreeTableModel model = (SortableTreeTableModel) getTreeTableModel();
        JPopupMenu columnMenu = new JPopupMenu();

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
                public void actionPerformed(ActionEvent arg0) {
                    TableColumnExt column = getColumnExt(menuItem.getText());
                    // Determine whether to show or hide the selected column
                    boolean enable = !column.isVisible();
                    // Do not hide a column if it is the last remaining visible column              
                    if (enable || getColumnCount() > 1) {
                        column.setVisible(enable);

                        Set<String> customHiddenColumns = customHiddenColumnMap.get(channelId);

                        if (customHiddenColumns != null) {
                            if (enable) {
                                customHiddenColumns.remove(columnName);
                            } else {
                                customHiddenColumns.add(columnName);
                            }
                        }
                    }
                    saveColumnOrder();
                }
            });

            columnMenu.add(menuItem);
        }

        columnMenu.addSeparator();

        JMenuItem menuItem = new JMenuItem("Collapse All");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                collapseAll();
            }

        });
        columnMenu.add(menuItem);

        menuItem = new JMenuItem("Expand All");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                expandAll();
            }

        });
        columnMenu.add(menuItem);

        columnMenu.addSeparator();

        menuItem = new JMenuItem("Restore Default");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                if (metaDataColumns != null) {
                    defaultVisibleColumns.addAll(metaDataColumns);
                }
                restoreDefaultColumnPreferences();
            }

        });
        columnMenu.add(menuItem);

        return columnMenu;
    }

    public void setMetaDataColumns(Set<String> metaDataColumns, String channelId) {
        this.channelId = channelId;
        this.metaDataColumns = metaDataColumns;
    }

    public Map<String, Set<String>> getCustomHiddenColumnMap() {
        return customHiddenColumnMap;
    }

    public void saveColumnOrder() {
        try {
            if (StringUtils.isNotEmpty(prefix)) {
                int metaDataColumnOffset = 0;
                columnOrderMap.clear();

                for (TableColumn column : getColumns()) {
                    String columnName = (String) column.getHeaderValue();

                    if (metaDataColumns != null && metaDataColumns.contains(columnName)) {
                        metaDataColumnOffset++;
                    }

                    if (metaDataColumns == null || !metaDataColumns.contains(columnName)) {
                        columnOrderMap.put(columnName, convertColumnIndexToView(column.getModelIndex()) - metaDataColumnOffset);
                    }
                }

                // Finally, loop again to store the hidden columns in the map
                for (TableColumn column : getColumns(true)) {
                    String columnName = (String) column.getHeaderValue();

                    if (!columnOrderMap.keySet().contains(columnName) && (metaDataColumns == null || !metaDataColumns.contains(columnName))) {
                        columnOrderMap.put(columnName, -1);
                    }
                }

                userPreferences.put(prefix + PREFERENCE_COLUMN_ORDER_MAP, ObjectXMLSerializer.getInstance().serialize(new HashMap<String, Integer>(columnOrderMap)));
            }
        } catch (Exception e) {
        }
    }

    public void restoreColumnPreferences() {
        if (StringUtils.isNotEmpty(prefix)) {
            try {
                TableColumnModelExt columnModel = (TableColumnModelExt) getTableHeader().getColumnModel();
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

                // MetaDataColumns are restored here by comparing them against the customHiddenColumnMap which contains a list of all columns that should be hidden
                if (CollectionUtils.isNotEmpty(metaDataColumns)) {
                    Set<String> cachedColumns = customHiddenColumnMap.get(channelId);

                    if (cachedColumns != null) {
                        for (String column : metaDataColumns) {
                            getColumnExt(column).setVisible(!cachedColumns.contains(column));
                        }
                    }
                }

                // After restoring column order, restore sort order
                SortableTreeTableModel model = (SortableTreeTableModel) getTreeTableModel();
                if (sortOrderColumn > -1 && sortOrderColumn < model.getColumnCount()) {
                    model.setColumnAndToggleSortOrder(sortOrderColumn);
                    model.setSortOrder(sortOrder);

                    // Update sorting Icons
                    ((SortableHeaderCellRenderer) getTableHeader().getDefaultRenderer()).setSortingIcon(sortOrder);
                    ((SortableHeaderCellRenderer) getTableHeader().getDefaultRenderer()).setColumnIndex(sortOrderColumn);
                }
            } catch (Exception e) {
                restoreDefaultColumnPreferences();
            }
        }
    }

    public void saveSortPreferences(int column) {
        try {
            if (StringUtils.isNotEmpty(prefix)) {
                SortOrder order = getSortOrder(column);

                userPreferences.put(prefix + PREFERENCE_SORT_ORDER, ObjectXMLSerializer.getInstance().serialize(order));
                userPreferences.putInt(prefix + PREFERENCE_SORT_ORDER_COLUMN, column);

                sortOrder = order;
                sortOrderColumn = column;
            }
        } catch (Exception e) {
        }
    }

    public void restoreDefaultColumnPreferences() {
        try {
            userPreferences.put(prefix + PREFERENCE_COLUMN_ORDER_MAP, "");
            userPreferences.put(prefix + PREFERENCE_SORT_ORDER, "");
            userPreferences.putInt(prefix + PREFERENCE_SORT_ORDER_COLUMN, -1);

            if (StringUtils.isNotEmpty(prefix)) {
                columnOrderMap.clear();
                if (customHiddenColumnMap.get(channelId) != null) {
                    customHiddenColumnMap.get(channelId).clear();
                }
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

                sortOrder = null;
                sortOrderColumn = -1;
                resetSortOrder();

                // Reset the icons
                ((SortableHeaderCellRenderer) getTableHeader().getDefaultRenderer()).setSortingIcon(sortOrder);
                ((SortableHeaderCellRenderer) getTableHeader().getDefaultRenderer()).setColumnIndex(sortOrderColumn);
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
        int viewIndex = -1;

        for (TableColumn column : getColumns()) {
            if (columnName.equals(column.getIdentifier())) {
                viewIndex = this.getColumnModel().getColumnIndex(columnName);
            }
        }

        return viewIndex;
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
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) this.getPathForRow(selectedRows[i]).getLastPathComponent();
            if (node.isGroupNode()) {
                for (Enumeration<? extends MutableTreeTableNode> channelNodes = node.children(); channelNodes.hasMoreElements();) {
                    nodes.add((AbstractDashboardTableNode) channelNodes.nextElement());
                }
            } else {
                nodes.add(node);
            }
        }

        return nodes;
    }
}