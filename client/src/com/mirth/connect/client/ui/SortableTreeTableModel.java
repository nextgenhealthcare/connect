/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

/** 
 * http://svn.chorem.org/svn/jtimer/tags/jtimer-1.0-beta4/src/java/org/codelutin/jtimer/ui/treetable/sorting/
 * 
 * SortableTreeTableModel.java
 *
 * Created 19/10/2007 11:54:13 AM
 * 
 * @author Ray Turnbull
 */
import java.util.ArrayList;
import java.util.List;

import javax.swing.SortOrder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.JTableHeader;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

/**
 * This is designed to be used with AbstractSortableTreeTableNode.<br>
 * For user interaction by clicking column headers, SortableTreeTable (extends JXTreeTable) should be used also.<br>
 * The model must be set up using string Column Identifiers.
 * <p>
 * It controls the sorting of the children of each node.<br>
 * <br>
 * Note that this does not support sorting the underlying table, as that is meaningless for a TreeTable.<br>
 * All nodes wiil have their immediate children sorted seperately, and the process will continue down through each generation, starting from the specified node.<br>
 * If any of the sort options are set or changed, sorting will start from the root node. If a node has a child added or removed, sorting will start from that node. The sorts may also be called manually, e.g if sort parameters are loaded before the model is attached to the TreeTable
 * <p>
 * This class does not handle changes to the values of fields being sorted. This must be done manually by calling sort.
 * 
 * @see #sort(TreeTableNode)
 * @see #sort()
 * 
 */
public class SortableTreeTableModel extends DefaultTreeTableModel implements TreeExpansionListener {
    private String sortColumn = null;
    private SortOrder sortOrder = SortOrder.UNSORTED;
    private int columnIndex = -1;
    private List<TreePath> expanded;
    private SortableTreeTable treeTable = null;
    private JTableHeader header = null;
    boolean expanding = false;
    boolean sortChildNodes = false;

    public SortableTreeTableModel() {}

    public SortableTreeTableModel(TreeTableNode root) {
        super(root);
    }

    public SortableTreeTableModel(TreeTableNode root, List<?> columnNames) {
        super(root, columnNames);
    }

    // =========================================================== public api

    /**
     * Set name of column to sort. If null, will reset to unsorted. Sort Order will be set ASCENDING<br>
     * Name must exist in the Column Identifiers.
     * 
     * @param column
     *            - name of column to sort on
     */
    public void setSortColumn(String column) {
        if (column == null) {
            sortColumn = null;
            if (sortOrder != SortOrder.UNSORTED) {
                sortOrder = SortOrder.UNSORTED;
                reset();
            }
        } else {
            setKeys(column, SortOrder.ASCENDING);
        }
    }

    /**
     * Set column to sort by column index. If -1 or greater than number of columns - 1, will be set Unsorted, otherwise sort order will be set Ascending.
     * 
     * @param column
     *            - index of column name in columnNames List
     */
    public void setSortColumn(int column) {
        if (column == -1 || column > columnIdentifiers.size() - 1) {
            sortColumn = null;
            if (sortOrder != SortOrder.UNSORTED) {
                sortOrder = SortOrder.UNSORTED;
                reset();
            }
        } else {
            setKeys(columnIdentifiers.get(column).toString(), SortOrder.ASCENDING);
        }
    }

    /**
     * Set Sort Order. If null, order will be Unsorted.<br>
     * If no sort column has been set has no effect.
     * 
     * @param order
     */
    public void setSortOrder(SortOrder order) {
        if (sortColumn == null) {
            return;
        }
        if (order == null) {
            order = SortOrder.UNSORTED;
        }
        setKeys(sortColumn, order);
    }

    /**
     * Toggle sort order. Unsorted will be sorted ascending, and sorted columns order will be reversed. If sort column not set, has no effect.
     */
    public void toggleSortOrder() {
        if (sortOrder == SortOrder.ASCENDING) {
            setSortOrder(SortOrder.DESCENDING);
        } else {
            setSortOrder(SortOrder.ASCENDING);
        }
    }

    /**
     * Set sort options. If either option is null, will be set Unsorted
     * 
     * @param column
     *            - name of column to sort. Must be a column in column names list.
     * @param order
     *            - see Swingx org.jdesktop.swingx.decorator.SortOrder
     */
    public void setSortOptions(String column, SortOrder order) {
        if (column == null) {
            setSortColumn(null);
            return;
        }
        if (order == null) {
            order = SortOrder.UNSORTED;
        }
        setKeys(column, order);
    }

    /**
     * Sorts complete TreeTable. This will be called automatically if any of the sort options (column or order) are changed. It is not usually neccessary to called this directly unless TreeTable is changed external to model, or TreeTable data changed.
     */
    public void sort() {
        if (sortOrder == SortOrder.UNSORTED) {
            reset();
        } else {
            doFullSort(false);
        }
    }

    /**
     * Sorts children of node, and all their children. Node need not be sortable. (Although if it is not and neither are any descendants, nothing will happen.) Called automatically if a child is added to or removed from a node. Usually not necessary to call this directly unless node added outside model or data changed.
     * 
     * @param parent
     *            - first node to be sorted.
     */
    public void sort(TreeTableNode parent) {
        boolean reset;
        if (sortOrder == SortOrder.UNSORTED) {
            reset = true;
        } else {
            reset = false;
        }
        doSort(parent, reset);
        TreePath path = new TreePath(getPathToRoot(parent));
        modelSupport.fireTreeStructureChanged(path);
        reExpand();
    }

    public int getSortColumnIndex() {
        return columnIndex;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void insertNodeInto(MutableTreeTableNode newChild, MutableTreeTableNode parent) {
        int index = getChildCount(parent);
        insertNodeInto(newChild, parent, index);
    }

    public void setSortable(MutableTreeTableNode node, boolean sortable) {
        if (!(node instanceof AbstractSortableTreeTableNode))
            return;
        AbstractSortableTreeTableNode n = (AbstractSortableTreeTableNode) node;
        n.setSortable(sortable);
        if (n.isSorted() && !sortable) {
            n.reset();
            TreePath path = new TreePath(getPathToRoot(n));
            modelSupport.firePathChanged(path);
        }
    }

    // ==================================================== overridden methods

    public void insertNodeInto(MutableTreeTableNode newChild, MutableTreeTableNode parent, int index) {
        parent.insert(newChild, index);
        if (sortOrder != SortOrder.UNSORTED) {
            sort(parent);
        } else {
            modelSupport.fireChildAdded(new TreePath(getPathToRoot(parent)), index, newChild);
        }
    }

    public void removeNodeFromParent(MutableTreeTableNode node) {
        MutableTreeTableNode parent = (MutableTreeTableNode) node.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("node does not have a parent.");
        }

        int index = getIndexOfChild(parent, node);
        expanded.remove(new TreePath(getPathToRoot(node)));
        node.removeFromParent();
        if (sortOrder != SortOrder.UNSORTED) {
            sort(parent);
        } else {
            modelSupport.fireChildRemoved(new TreePath(getPathToRoot(parent)), index, node);
        }
    }

    @Override
    public void setRoot(TreeTableNode root) {
        expanded = new ArrayList<TreePath>();
        super.setRoot(root);
    }

    // ======================================================= private methods

    /**
     * this is a hack to enable expanded nodes to be re-expanded after structure change. It is called from SortableTreeTable<br>
     * It is also used to retrieve the table header to repaint if sort options changed programatically
     * <p>
     * It means model can only be used in one treetable.
     * 
     */
    void setTreeTable(SortableTreeTable treeTable) {
        this.treeTable = treeTable;
        expanded = new ArrayList<TreePath>();
        header = treeTable.getTableHeader();
    }

    private void setKeys(String column, SortOrder order) {
        if (sortColumn != null && sortColumn.equals(column)) {
            if (sortOrder.equals(order)) {
                return;
            }
        } else {
            if (column != null) {
                int x = columnIdentifiers.indexOf(column);
                if (x == -1) {
                    throw new IllegalArgumentException("Column " + column + " not in Column Identifiers");
                } else {
                    columnIndex = x;
                }
            }
        }
        sortColumn = column;
        sortOrder = order;
        sort();
        if (header != null) {
            header.repaint();
        }
    }

    private void reset() {
        doFullSort(true);
    }

    private void doFullSort(boolean reset) {
        TreeTableNode root = getRoot();
        if (root == null) {
            return;
        }
        doSort(root, reset);
        modelSupport.fireTreeStructureChanged(new TreePath(root));
        reExpand();
    }

    /*
     * Start from node and drill down all nodes looking to sort.
     */
    private void doSort(TreeTableNode parent, boolean reset) {
        boolean canSort;
        AbstractSortableTreeTableNode node;
        if (parent instanceof AbstractSortableTreeTableNode) {
            node = (AbstractSortableTreeTableNode) parent;
            canSort = true;
        } else {
            node = null;
            canSort = false;
        }
        if (canSort) {
            canSort = node.canSort();
        }
        if (canSort) {
            if (reset) {
                canSort = node.isSorted();
            } else {
                canSort = node.canSort(sortColumn);
            }
        }
        if (canSort) {
            if (reset) {
                node.reset();
            } else {
                node.sort(columnIndex, sortOrder);
            }
        } else {
            node.reset();
        }
        
        // MIRTH: Modified Sorting Code

        // Sort Child Nodes
        if (isSortChildNodes()) {
            doSortChildNodes(parent, reset);
        }
    }
    
    private void reExpand() {
        if (treeTable == null)
            return;
        expanding = true;
        for (TreePath path : expanded) {
            treeTable.expandPath(path);
        }
        expanding = false;
    }

    /*
     * Inherited
     */
    @Override
    public void treeCollapsed(TreeExpansionEvent arg0) {
        TreePath p = arg0.getPath();
        expanded.remove(p);
    }

    /*
     * Inherited
     */
    @Override
    public void treeExpanded(TreeExpansionEvent arg0) {
        if (expanding)
            return;
        TreePath p = arg0.getPath();
        if (!expanded.contains(p)) {
            expanded.add(p);
        }
    }

    // MIRTH: Modified Sorting Code
    
    // Hack to allow column header clicks to set the column and toggle the sort order
    public void setColumnAndToggleSortOrder(int column) {
        if (column == -1 || column > columnIdentifiers.size() - 1) {
            sortColumn = null;
            if (sortOrder != SortOrder.UNSORTED) {
                sortOrder = SortOrder.UNSORTED;
                reset();
            }
        } else {
            if (sortOrder == SortOrder.ASCENDING) {
                setKeys(columnIdentifiers.get(column).toString(), SortOrder.DESCENDING);
            } else {
                setKeys(columnIdentifiers.get(column).toString(), SortOrder.ASCENDING);
            }
        }
    }
    
    private void doSortChildNodes(TreeTableNode parent, boolean reset) {
        for (int i = 0; i < getChildCount(parent); ++i) {
            TreeTableNode child = (TreeTableNode) getChild(parent, i);
            doSort(child, reset);
        }
    }

    public boolean isSortChildNodes() {
        return sortChildNodes;
    }

    public void setSortChildNodes(boolean sortChildNodes) {
        this.sortChildNodes = sortChildNodes;
    }
}