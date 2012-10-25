/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

public class RefreshTableModel extends DefaultTableModel {

    public RefreshTableModel() {
        super();
    }

    public RefreshTableModel(int numRows, int numColumns) {
        super(numRows, numColumns);
    }

    public RefreshTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public RefreshTableModel(Object[] columnNames, int numRows) {
        super(columnNames, numRows);
    }

    public RefreshTableModel(Vector<Object> columnNames, int numRows) {
        super(columnNames, numRows);
    }

    public RefreshTableModel(Vector<Object> data, Vector<?> columnNames) {
        super(data, columnNames);
    }

    public void refreshDataVector(Vector<Object> data) {
        dataVector = data;
        fireTableDataChanged();
    }

    public void refreshDataVector(Object[][] data) {
        refreshDataVector(convertToVector(data));
    }

    // Unit test
    public static void main(String[] args) {
        Object[][] data = {{"four", "A"}, {"three", "B"}, {"two", "C"}, {"one", "D"}};
        String[] columnNames = {"Number", "Letter"};
        RefreshTableModel model = new RefreshTableModel(data, columnNames);
        JXTable table = new JXTable(model);
        table.setSortable(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(scrollPane);
        frame.pack();
        frame.setVisible(true);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        Object[][] refresh = {{"five", "E"}, {"six", "F"}};
        model.refreshDataVector(refresh);
    }
}
