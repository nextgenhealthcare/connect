/*
 * RefreshTableModel.java
 *
 * Created on February 15, 2007, 12:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui;

import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
 
public class RefreshTableModel extends DefaultTableModel
{
    public RefreshTableModel()
    {
        super();
    }
 
    public RefreshTableModel(int numRows, int numColumns)
    {
        super(numRows, numColumns);
    }
 
    public RefreshTableModel(Object[][] data, Object[] columnNames)
    {
        super(data, columnNames);
    }
 
    public RefreshTableModel(Object[] columnNames, int numRows)
    {
        super(columnNames, numRows);
    }
 
    public RefreshTableModel(Vector columnNames, int numRows)
    {
        super(columnNames, numRows);
    }
 
    public RefreshTableModel(Vector data, Vector columnNames)
    {
        super(data, columnNames);
    }
 
    /*
     *
     */
    public void refreshDataVector(Vector data)
    {
        dataVector = data;
        fireTableRowsInserted(0, getRowCount() - 1);
    }
 
    public void refreshDataVector(Object[][] data)
    {
        refreshDataVector( convertToVector(data) );
    }
    
    // Unit test
    public static void main(String[] args)
    {
        Object[][] data = { {"four", "A"}, {"three", "B"}, {"two", "C"}, {"one", "D"} };
        String[] columnNames = {"Number", "Letter"};
        RefreshTableModel model = new RefreshTableModel(data, columnNames);
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        JScrollPane scrollPane = new JScrollPane( table );
 
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.getContentPane().add( scrollPane );
        frame.pack();
        frame.setVisible(true);
 
        try { Thread.sleep(3000); }
        catch (InterruptedException e) {}
 
        Object[][] refresh = { {"five", "E"}, {"six", "F"} };
        model.refreshDataVector( refresh );
    }
}


