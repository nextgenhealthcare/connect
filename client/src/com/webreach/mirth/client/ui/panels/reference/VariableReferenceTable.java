/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui.panels.reference;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.model.CodeTemplate;

public class VariableReferenceTable extends ReferenceTable {

    private Object[] tooltip;
    private ArrayList<CodeTemplate> _listItems;
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

    public VariableReferenceTable(String headerName, ArrayList<CodeTemplate> listItems) {
        this._listItems = listItems;
        makeTable(headerName, listItems);
    }

    private void makeTable(String headerName, ArrayList<CodeTemplate> listItems) {
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

        this.setModel(new DefaultTableModel(d, new Object[]{headerName}) {

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });

        this.getColumnExt(headerName).setPreferredWidth(80);

    }

    public String getToolTipText(MouseEvent event) {
        Point p = event.getPoint();
        int col = convertColumnIndexToModel(columnAtPoint(p));
        int row = convertRowIndexToModel(rowAtPoint(p));
        if (col >= 0 && row >= 0 && tooltip != null) {
            Object o = getValueAt(row, col);
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

        this.setModel(new DefaultTableModel(d, new Object[]{headerName}) {

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });

    }
}
