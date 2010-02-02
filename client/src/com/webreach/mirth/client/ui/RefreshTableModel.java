package com.webreach.mirth.client.ui;

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

    public RefreshTableModel(Vector columnNames, int numRows) {
        super(columnNames, numRows);
    }

    public RefreshTableModel(Vector data, Vector columnNames) {
        super(data, columnNames);
    }

    public void refreshDataVector(Vector data) {
        /*if (dataVector != null && dataVector.size() > 0)
        fireTableRowsDeleted(0, dataVector.size() - 1);
        
        dataVector = data;
        
        if (data.size() > 0)
        fireTableRowsInserted(0, data.size() - 1);
         */
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
