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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class DataTypesDialog extends javax.swing.JDialog {

    private Frame parent;
    private final String[] columnNames = {"Connector", "Inbound", "Outbound"};
    private Map<Integer, Transformer> transformers;

    public DataTypesDialog() {
        super(PlatformUI.MIRTH_FRAME);
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        
        inboundPropertiesPanel.setInbound(true);
        outboundPropertiesPanel.setInbound(false);

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
        
        transformers = new HashMap<Integer, Transformer>();
        
        makeTables();
        connectorTable.getSelectionModel().setSelectionInterval(0, 0);
        
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                inboundPropertiesPanel.save();
                outboundPropertiesPanel.save();
            }
            
        });
        
        setVisible(true);
    }

    public void makeTables() {
        updateConnectorTable();

        makeTable(connectorTable, connectorTablePane);
    }

    public void makeTable(MirthTable table, JScrollPane scrollPane) {
        int dataTypeColumnWidth = 100;
        
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        String[] dataTypes = new String[parent.dataTypeToDisplayName.values().size()];
        parent.dataTypeToDisplayName.values().toArray(dataTypes);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Transformer transformer = transformers.get(connectorTable.getSelectedRow());
                    // Model row should always be the same as table row for this table.
                    int modelRow = connectorTable.convertRowIndexToModel(connectorTable.getSelectedRow());
                    String transformerName = (String) connectorTable.getModel().getValueAt(modelRow, 0);
                    String inboundDataType = (String) connectorTable.getModel().getValueAt(modelRow, 1);
                    String outboundDataType = (String) connectorTable.getModel().getValueAt(modelRow, 2);
                    
                    inboundPropertiesPanel.setTitle(transformerName);
                    inboundPropertiesPanel.setDataTypeProperties(inboundDataType, transformer.getInboundProperties());
                    
                    outboundPropertiesPanel.setTitle(transformerName);
                    outboundPropertiesPanel.setDataTypeProperties(outboundDataType, transformer.getOutboundProperties());
                }
            }
            
        });

        table.getColumnModel().getColumn(1).setCellEditor(new MirthComboBoxTableCellEditor(table, dataTypes, 1, false, new DataTypeComboBoxActionListener(true)));
        table.getColumnModel().getColumn(1).setCellRenderer(new MirthComboBoxTableCellRenderer(dataTypes));
        
        table.getColumnModel().getColumn(2).setCellEditor(new MirthComboBoxTableCellEditor(table, dataTypes, 1, false, new DataTypeComboBoxActionListener(false)));
        table.getColumnModel().getColumn(2).setCellRenderer(new MirthComboBoxTableCellRenderer(dataTypes));
        
        table.setRowHeight(UIConstants.ROW_HEIGHT);
        table.setSortable(false);
        table.setOpaque(true);
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnExt(0).setMinWidth(UIConstants.MIN_WIDTH);
        table.getColumnExt(0).setResizable(false);

        table.getColumnExt(1).setMaxWidth(dataTypeColumnWidth);
        table.getColumnExt(1).setMinWidth(dataTypeColumnWidth);
        table.getColumnExt(1).setResizable(false);
        
        table.getColumnExt(2).setMaxWidth(dataTypeColumnWidth);
        table.getColumnExt(2).setMinWidth(dataTypeColumnWidth);
        table.getColumnExt(2).setResizable(false);
        
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            table.setHighlighters(highlighter);
        }

        scrollPane.setViewportView(table);
    }
    
    private class DataTypeComboBoxActionListener implements ActionListener {
        
        private boolean inbound;
        
        public DataTypeComboBoxActionListener(boolean inbound) {
            this.inbound = inbound;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (connectorTable.getEditingRow() != -1) {
                int selectedRow = connectorTable.convertRowIndexToModel(connectorTable.getEditingRow());
                String dataTypeDisplayName = (String)((JComboBox)e.getSource()).getSelectedItem();
                String dataType = PlatformUI.MIRTH_FRAME.displayNameToDataType.get(dataTypeDisplayName);
                DataTypeProperties defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
                Transformer transformer = transformers.get(selectedRow);

                if (inbound) {
                    if (!transformer.getInboundDataType().equals(dataType)) {
                        transformer.setInboundDataType(dataType);
                        transformer.setInboundProperties(defaultProperties);
                        inboundPropertiesPanel.setDataTypeProperties(dataTypeDisplayName, transformer.getInboundProperties());
                    }
                } else {
                    if (!transformer.getOutboundDataType().equals(dataType)) {
                        transformer.setOutboundDataType(dataType);
                        transformer.setOutboundProperties(defaultProperties);
                        outboundPropertiesPanel.setDataTypeProperties(dataTypeDisplayName, transformer.getOutboundProperties());
                    }
                    
                    if (selectedRow == 0) {
                        // Also set the inbound data type and properties for all destinations
                        for (int row = 1; row < connectorTable.getRowCount(); row++) {
                            // Get a new properties object so they aren't all using the same one
                            defaultProperties = LoadedExtensions.getInstance().getDataTypePlugins().get(dataType).getDefaultProperties();
                            
                            connectorTable.getModel().setValueAt(dataTypeDisplayName, row, 1);
                            transformer = transformers.get(row);
                            transformer.setInboundDataType(dataType);
                            transformer.setInboundProperties(defaultProperties);
                        }
                    }
                }
            }
            
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        }
        
    }

    public void updateConnectorTable() {
        Object[][] tableData = null;

        Channel currentChannel = parent.channelEditPanel.currentChannel;

        tableData = new Object[currentChannel.getDestinationConnectors().size() + 1][3];

        int tableRow = 0;
        
        Connector sourceConnector = currentChannel.getSourceConnector();
        transformers.put(tableRow, sourceConnector.getTransformer());
        
        tableData[tableRow][0] = "Source Connector";
        tableData[tableRow][1] = parent.dataTypeToDisplayName.get(sourceConnector.getTransformer().getInboundDataType());
        tableData[tableRow][2] = parent.dataTypeToDisplayName.get(sourceConnector.getTransformer().getOutboundDataType());
        
        tableRow++;

        for (Connector destinationConnector : currentChannel.getDestinationConnectors()) {
            transformers.put(tableRow, destinationConnector.getTransformer());
            
            tableData[tableRow][0] = destinationConnector.getName();
            tableData[tableRow][1] = parent.dataTypeToDisplayName.get(currentChannel.getSourceConnector().getTransformer().getOutboundDataType());
            tableData[tableRow][2] = parent.dataTypeToDisplayName.get(destinationConnector.getTransformer().getOutboundDataType());
            tableRow++;
        }
        
        connectorTable.setModel(new RefreshTableModel(tableData, columnNames) {

            boolean[] canEdit = new boolean[]{false, true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                boolean editable;
                
                if (rowIndex == 0) {
                    if (columnIndex == 1 && PlatformUI.MIRTH_FRAME.channelEditPanel.requiresXmlDataType()) {
                        editable = false;
                    } else {
                        editable = canEdit[columnIndex];
                    }
                } else {
                    editable = (columnIndex == 2);
                }
                
                return editable;
            }
        });
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
        configurationPane = new javax.swing.JPanel();
        connectorTablePane = new javax.swing.JScrollPane();
        connectorTable = new com.mirth.connect.client.ui.components.MirthTable();
        jPanel5 = new javax.swing.JPanel();
        inboundPropertiesPanel = new com.mirth.connect.client.ui.DataTypePropertiesPanel();
        outboundPropertiesPanel = new com.mirth.connect.client.ui.DataTypePropertiesPanel();

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

        configurationPane.setBackground(new java.awt.Color(255, 255, 255));
        configurationPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuration"));

        connectorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Connector", "Inbound", "Outbound"
            }
        ));
        connectorTablePane.setViewportView(connectorTable);

        javax.swing.GroupLayout configurationPaneLayout = new javax.swing.GroupLayout(configurationPane);
        configurationPane.setLayout(configurationPaneLayout);
        configurationPaneLayout.setHorizontalGroup(
            configurationPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(connectorTablePane)
        );
        configurationPaneLayout.setVerticalGroup(
            configurationPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationPaneLayout.createSequentialGroup()
                .addComponent(connectorTablePane, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 114, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new java.awt.GridLayout(0, 2));
        jPanel5.add(inboundPropertiesPanel);
        jPanel5.add(outboundPropertiesPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(closeButton))
                    .addComponent(configurationPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configurationPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
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
    private javax.swing.JPanel configurationPane;
    private com.mirth.connect.client.ui.components.MirthTable connectorTable;
    private javax.swing.JScrollPane connectorTablePane;
    private com.mirth.connect.client.ui.DataTypePropertiesPanel inboundPropertiesPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private com.mirth.connect.client.ui.DataTypePropertiesPanel outboundPropertiesPanel;
    // End of variables declaration//GEN-END:variables
}
