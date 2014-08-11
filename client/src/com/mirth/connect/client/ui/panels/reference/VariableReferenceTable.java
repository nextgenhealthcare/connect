/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.reference;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.mirth.connect.model.CodeTemplate;

public class VariableReferenceTable extends ReferenceTable {

    private Object[] tooltip;
    private String headerName = "blank";

    public VariableReferenceTable() {
        super();
        makeTable(null, null);
    }

    public VariableReferenceTable(String headerName, Object[] data) {
        super();
        makeTable(headerName, data, null);
    }

    public VariableReferenceTable(String headerName, Object[] data, Object[] tooltip) {
        super();
        makeTable(headerName, data, tooltip);
    }

    public VariableReferenceTable(String headerName, List<CodeTemplate> listItems) {
        makeTable(headerName, listItems);
    }

    private void makeTable(String headerName, List<CodeTemplate> listItems) {
        if (listItems == null) {
            return;
        }
        Object[] tooltips = new String[listItems.size()];
        Object[] names = new String[listItems.size()];
        Iterator<CodeTemplate> listItemIterator = listItems.iterator();
        int i = 0;
        while (listItemIterator.hasNext()) {
            CodeTemplate listItem = listItemIterator.next();
            names[i] = listItem.getName();
            tooltips[i] = listItem.getTooltip();
            i++;
        }
        makeTable(headerName, names, tooltips);
    }

    private void makeTable(String headerName, Object[] data, Object[] tooltip) {
        if (data == null) {
            return;
        }

        this.headerName = headerName;
        this.tooltip = tooltip;

        Object[][] d = new String[data.length][2];
        for (int i = 0; i < data.length; i++) {
            d[i][0] = data[i];
            d[i][1] = null;
        }

        this.setModel(new FilterTableModel(d, new Object[] { headerName }) {

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });

        this.getColumnExt(headerName).setPreferredWidth(80);

    }

    public String getToolTipText(MouseEvent event) {
        Point p = event.getPoint();

        int col = columnAtPoint(p);
        if (col != -1) {
            col = convertColumnIndexToModel(col);
        }

        int row = rowAtPoint(p);
        if (row != -1) {
            row = convertRowIndexToModel(row);
        }

        if (col >= 0 && row >= 0 && tooltip != null) {
            Object o = ((FilterTableModel) getModel()).getValueAt(row, col);
            if (o != null) {
                return "<html><body style=\"width:150px\"><p>" + tooltip[row] + "</p></body></html>";
            }
        }
        return null;
    }

    public void updateVariables(Set<String> variables) {
        Object[][] d = new String[variables.toArray().length][2];
        for (int j = 0; j < variables.toArray().length; j++) {
            d[j][0] = variables.toArray()[j];
            d[j][1] = null;
        }

        this.setModel(new FilterTableModel(d, new Object[] { headerName }) {

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });

    }

    /**
     * Execute to filter the table items based on the filter string
     * 
     * @param filterString
     *            The filtering string
     */
    public void performFilter(final String filterString) {
        TableModel tableModel = getModel();
        if (tableModel instanceof FilterTableModel) {
            FilterTableModel filterTableModel = (FilterTableModel) tableModel;
            filterTableModel.performFilter(filterString);
        }
    }

    /**
     * Filter table model that will manage the filtering of the items to be shown on the table item
     * list.
     * 
     */
    public class FilterTableModel extends DefaultTableModel {

        public FilterTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        /**
         * Performs the filtering of the table items based on the filter string
         * 
         * @param filterString
         *            The filter string
         */
        public void performFilter(final String filterString) {
            // cannot handle null filter strings
            if (filterString == null)
                return;

            RowFilter<Object, Object> rowFilter = new RowFilter<Object, Object>() {

                @Override
                public boolean include(RowFilter.Entry<? extends Object, ? extends Object> entry) {
                    try {
                        String name = entry.getStringValue(0);
                        if (name == null)
                            return false;

                        return filterString.trim().isEmpty() || name.trim().toLowerCase().contains(filterString.trim().toLowerCase());
                    } catch (IndexOutOfBoundsException e) {
                        // eat up the exception	, and it'll return false				
                    }
                    return false;
                }
            };
            setRowFilter(rowFilter);
        }
    }
}
