/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;

public class DebugTableModel extends DefaultTableModel {

    public static Object data[][] = { { Boolean.FALSE,"Deploy/Undeploy" }, { Boolean.FALSE,"Batch"}, {  Boolean.FALSE ,"Source Connector"},
            { Boolean.FALSE  ,"Destination Filter"}, {  Boolean.FALSE ,"Destination Connector"}};
    public static String[] columnNames = { "", "" };
    
    public DebugTableModel() {
        super();
    }

    public DebugTableModel(int numRows, int numColumns) {
        super(numRows, numColumns);
    }

    public DebugTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public DebugTableModel(Object[] columnNames, int numRows) {
        super(columnNames, numRows);
    }

    public DebugTableModel(Vector<Object> columnNames, int numRows) {
        super(columnNames, numRows);
    }

    @SuppressWarnings("rawtypes")
    public DebugTableModel(Vector<Vector> data, Vector<?> columnNames) {
        super(data, columnNames);
    }

    @SuppressWarnings("rawtypes")
    public void refreshDataVector(Vector<Vector> data) {
        dataVector = data;
        fireTableDataChanged();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void refreshDataVector(Object[][] data) {
        refreshDataVector((Vector) convertToVector(data));
    }
    public Class getColumnClass(int column) {
        return (getValueAt(0, column).getClass());
      }
    // Unit test
    public static void main(String[] args) {
        
        DebugTableModel model = new DebugTableModel(data, columnNames);
        JXTable table = new JXTable(model);
        table.setSortable(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(10);
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

        Object[][] refresh = { { "five", "E" }, { "six", "F" } };
        model.refreshDataVector(refresh);
    }
}
