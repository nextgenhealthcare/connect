package com.mirth.connect.client.ui;

import javax.swing.table.AbstractTableModel;

public class DeployInDebugEntity extends AbstractTableModel
{
    Object rowData[][] = { { Boolean.FALSE,"Deploy/Undeploy" }, { Boolean.FALSE,"Batch"}, {  Boolean.FALSE ,"Source Connector"},
            { Boolean.FALSE  ,"Destination Filter"}, {  Boolean.FALSE ,"Destination Connector"},};

        String columnNames[] = { "",""};

        public int getColumnCount() {
            return columnNames.length;
          }

          public String getColumnName(int column) {
            return columnNames[column];
          }

          public int getRowCount() {
            return rowData.length;
          }

          public Object getValueAt(int row, int column) {
            return rowData[row][column];
          }

          public Class getColumnClass(int column) {
            return (getValueAt(0, column).getClass());
          }

          public void setValueAt(Object value, int row, int column) {
            rowData[row][column] = value;
          }

          public boolean isCellEditable(int row, int column) {
            return (column != 0);
          }



}
