/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JScrollPane;

import com.mirth.connect.client.ui.components.DataTypesCellEditor;
import com.mirth.connect.client.ui.components.DataTypesCellRenderer;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;

public class DataTypesDialog extends javax.swing.JDialog {

    private Frame parent;
    private final String[] columnNames = {"Connector", "Inbound", "Outbound"};

    public DataTypesDialog() {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        makeTables();

        setVisible(true);
    }

    public void makeTables() {
        updateSourceTable();
        updateDestinationsTable();

        makeTable(sourceConnectorTable, sourceConnectorTablePane, true);
        makeTable(destinationConnectorTable, destinationConnectorTablePane, false);
    }

    public void makeTable(MirthTable table, JScrollPane scrollPane, boolean source) {
        int dataTypeColumnWidth = 125;
        
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        String[] dataTypes = new String[parent.dataTypeToDisplayName.values().size()];
        parent.dataTypeToDisplayName.values().toArray(dataTypes);

        table.getColumnModel().getColumn(1).setCellEditor(new DataTypesCellEditor(table, dataTypes, 1, false, source, true, this));
        table.getColumnModel().getColumn(1).setCellRenderer(new DataTypesCellRenderer(dataTypes, source, true));
        
        table.getColumnModel().getColumn(2).setCellEditor(new DataTypesCellEditor(table, dataTypes, 1, false, source, false, this));
        table.getColumnModel().getColumn(2).setCellRenderer(new DataTypesCellRenderer(dataTypes, source, false));
        
        table.setRowSelectionAllowed(false);
        table.setRowHeight(UIConstants.ROW_HEIGHT);
        table.setSortable(false);
        table.setOpaque(true);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFocusable(false);

        table.getColumnExt(0).setMinWidth(UIConstants.MIN_WIDTH);
        table.getColumnExt(0).setResizable(false);

        table.getColumnExt(1).setMaxWidth(dataTypeColumnWidth);
        table.getColumnExt(1).setMinWidth(dataTypeColumnWidth);
        table.getColumnExt(1).setResizable(false);
        
        table.getColumnExt(2).setMaxWidth(dataTypeColumnWidth);
        table.getColumnExt(2).setMinWidth(dataTypeColumnWidth);
        table.getColumnExt(2).setResizable(false);

        scrollPane.setViewportView(table);
    }

    public void updateSourceTable() {
        Object[][] tableData = null;

        Channel currentChannel = parent.channelEditPanel.currentChannel;

        tableData = new Object[1][3];

        Connector sourceConnector = currentChannel.getSourceConnector();

        tableData[0][0] = "Source Connector";
        tableData[0][1] = parent.dataTypeToDisplayName.get(sourceConnector.getTransformer().getInboundDataType());
        tableData[0][2] = parent.dataTypeToDisplayName.get(sourceConnector.getTransformer().getOutboundDataType());

        sourceConnectorTable.setModel(new RefreshTableModel(tableData, columnNames) {

            boolean[] canEdit = new boolean[]{false, true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
    }

    public void updateDestinationsTable() {
        Object[][] tableData = null;

        Channel currentChannel = parent.channelEditPanel.currentChannel;

        tableData = new Object[currentChannel.getDestinationConnectors().size()][3];

        int tableRow = 0;

        for (Connector destinationConnector : currentChannel.getDestinationConnectors()) {
            tableData[tableRow][0] = destinationConnector.getName();
            tableData[tableRow][1] = parent.dataTypeToDisplayName.get(currentChannel.getSourceConnector().getTransformer().getOutboundDataType());
            tableData[tableRow][2] = parent.dataTypeToDisplayName.get(destinationConnector.getTransformer().getOutboundDataType());
            tableRow++;
        }
        
        destinationConnectorTable.setModel(new RefreshTableModel(tableData, columnNames) {

            boolean[] canEdit = new boolean[]{false, true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
    }
    
    public void updateDestinationInboundDataType(String dataType) {
        for (int row = 0; row < destinationConnectorTable.getRowCount(); row++) {
            destinationConnectorTable.setValueAt(dataType, row, 1);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        sourceConnectorTablePane = new javax.swing.JScrollPane();
        sourceConnectorTable = new com.mirth.connect.client.ui.components.MirthTable();
        jPanel3 = new javax.swing.JPanel();
        destinationConnectorTablePane = new javax.swing.JScrollPane();
        destinationConnectorTable = new com.mirth.connect.client.ui.components.MirthTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Set Data Types");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Source Connector"));

        sourceConnectorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Connector", "Inbound", "Properties", "Outbound", "Properties"
            }
        ));
        sourceConnectorTablePane.setViewportView(sourceConnectorTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceConnectorTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceConnectorTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Destination Connectors"));

        destinationConnectorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Connector", "Inbound", "Properties", "Outbound", "Properties"
            }
        ));
        destinationConnectorTablePane.setViewportView(destinationConnectorTable);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(destinationConnectorTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(destinationConnectorTablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(closeButton)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
    this.dispose();
}//GEN-LAST:event_closeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private com.mirth.connect.client.ui.components.MirthTable destinationConnectorTable;
    private javax.swing.JScrollPane destinationConnectorTablePane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private com.mirth.connect.client.ui.components.MirthTable sourceConnectorTable;
    private javax.swing.JScrollPane sourceConnectorTablePane;
    // End of variables declaration//GEN-END:variables
}
