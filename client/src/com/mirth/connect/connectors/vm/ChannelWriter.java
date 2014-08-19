/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatus;

public class ChannelWriter extends ConnectorSettingsPanel {

    private Frame parent;
    private Map<String, String> channelList;
    private ArrayList<String> channelNameArray;
    private Boolean channelIdModified = false;
    private Boolean comboBoxModified = false;

    public ChannelWriter() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        channelIdField.setToolTipText("<html>The destination channel's unique global id.</html>");
        channelIdField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateField();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateField();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateField();
            }

        });
        
        class CustomTableCellEditor extends TextFieldCellEditor {

            @Override
            protected boolean valueChanged(String value) {
                if ((value.length() == 0 || checkUniqueProperty(value))) {
                    return false;
                }
                
                parent.setSaveEnabled(true);
                return true;
            }
            
            protected boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int rowIndex = 0; rowIndex < mapVariablesTable.getRowCount(); rowIndex++) {
                    if (mapVariablesTable.getValueAt(rowIndex, 0) != null && ((String) mapVariablesTable.getValueAt(rowIndex, 0)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
            }
        }

        mapVariablesTable.getColumnModel().getColumn(0).setCellEditor(new CustomTableCellEditor());
        mapVariablesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mapVariablesTable.setToolTipText("The following map variables will be included in the source map of the destination channel's message.");
        
        mapVariablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (mapVariablesTable.getRowCount() > 0) {
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });
    }

    private void updateField() {
        try {
            channelIdModified = true;
            if (!comboBoxModified) {
                String fieldEntry = channelIdField.getText();
                String selection = "";

                if (StringUtils.isBlank(fieldEntry)) {
                    selection = "<None>";
                } else if (channelList.containsValue(fieldEntry)) {
                    for (Entry<String, String> entry : channelList.entrySet()) {
                        if (entry.getValue().equals(fieldEntry)) {
                            fieldEntry = entry.getKey();
                        }
                    }
                    selection = fieldEntry;
                } else if (fieldEntry.contains("$")) {
                    selection = "<Map Variable>";
                } else {
                    selection = "<Channel Not Found>";
                }

                channelNames.getModel().setSelectedItem(selection);
            }
        } finally {
            channelIdModified = false;
        }
    }

    @Override
    public String getConnectorName() {
        return new VmDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        if (channelList == null) {
            return null;
        }

        VmDispatcherProperties properties = new VmDispatcherProperties();

        properties.setChannelId(StringUtils.isBlank(channelIdField.getText()) ? "none" : channelIdField.getText());
        properties.setChannelTemplate(template.getText());
        properties.setMapVariables(getMapVariableTableValues());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        VmDispatcherProperties props = (VmDispatcherProperties) properties;

        channelNameArray = new ArrayList<String>();
        channelList = new HashMap<String, String>();
        channelList.put("<None>", "none");

        String selectedChannelName = "None";

        for (ChannelStatus channelStatus : parent.channelStatuses.values()) {
            Channel channel = channelStatus.getChannel();
            if (props.getChannelId().equalsIgnoreCase(channel.getId())) {
                selectedChannelName = channel.getName();
            }

            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }

        // sort the channels in alpha-numeric order.
        Collections.sort(channelNameArray);

        // add "None" to the very top of the list.
        channelNameArray.add(0, "<None>");

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean enabled = parent.isSaveEnabled();

        String channelId = props.getChannelId();
        channelIdField.setText((channelId.equals("none")) ? "" : channelId);
        channelNames.setSelectedItem(selectedChannelName);
        template.setText(props.getChannelTemplate());
        
        setMapVariableTableValues(props.getMapVariables());

        parent.setSaveEnabled(enabled);
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new VmDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        return true;
    }

    @Override
    public void resetInvalidProperties() {}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        channelNames = new com.mirth.connect.client.ui.components.MirthComboBox();
        jLabel7 = new javax.swing.JLabel();
        template = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        channelIdField = new javax.swing.JTextField();
        mapVariablesPane = new javax.swing.JScrollPane();
        mapVariablesTable = new com.mirth.connect.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        URL1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        URL.setText("Channel Name:");

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        channelNames.setToolTipText("<html>Select the channel to which messages accepted by this destination's filter should be written,<br> or none to not write the message at all.</html>");
        channelNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                channelNamesActionPerformed(evt);
            }
        });

        jLabel7.setText("Template:");

        template.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        template.setToolTipText("<html>A Velocity enabled template for the actual message to be written to the channel.<br>In many cases, the default value of \"${message.encodedData}\" is sufficient.</html>");

        mapVariablesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Map Variable"
            }
        ));
        mapVariablesTable.setToolTipText("Query parameters are encoded as x=y pairs as part of the request URL, separated from it by a '?' and from each other by an '&'.");
        mapVariablesTable.setDragEnabled(false);
        mapVariablesTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        mapVariablesTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                mapVariablesTableKeyTyped(evt);
            }
        });
        mapVariablesPane.setViewportView(mapVariablesTable);

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        URL1.setText("Message Metadata:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(URL, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(URL1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(mapVariablesPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(channelIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(URL)
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(channelIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addComponent(URL1)
                    .addComponent(mapVariablesPane, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void channelNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_channelNamesActionPerformed
        try {
            comboBoxModified = true;
            if (!channelIdModified) {
                String selectedChannelName = channelNames.getSelectedItem().toString();
                String channelId = null;

                if (selectedChannelName.equals("<None>")) {
                    channelId = "";
                } else if (channelNameArray.contains(selectedChannelName)) {
                    channelId = channelList.get(selectedChannelName);
                }

                if (channelId != null) {
                    channelIdField.setText(channelId);
                }
            }
        } finally {
            comboBoxModified = false;
        }
    }//GEN-LAST:event_channelNamesActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) mapVariablesTable.getModel();

        Vector<String> row = new Vector<String>();
        String name = "Variable ";

        for (int i = 1; i <= mapVariablesTable.getRowCount() + 1; i++) {
            boolean exists = false;
            for (int index = 0; index < mapVariablesTable.getRowCount(); index++) {
                if (((String) mapVariablesTable.getValueAt(index, 0)).equalsIgnoreCase(name + i)) {
                    exists = true;
                }
            }

            if (!exists) {
                row.add(name + i);
                break;
            }
        }

        model.addRow(row);

        int rowSelectionNumber = mapVariablesTable.getRowCount() - 1;
        mapVariablesTable.setRowSelectionInterval(rowSelectionNumber, rowSelectionNumber);
        
        Boolean enabled = deleteButton.isEnabled();
        if (!enabled) {
            deleteButton.setEnabled(true);
        }
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int rowSelectionNumber = mapVariablesTable.getSelectedModelIndex();

        if (rowSelectionNumber > -1) {
            DefaultTableModel model = (DefaultTableModel) mapVariablesTable.getModel();
            int viewSelectionRow = mapVariablesTable.convertRowIndexToView(rowSelectionNumber);
            model.removeRow(rowSelectionNumber);

            if (mapVariablesTable.getRowCount() != 0) {
                if (viewSelectionRow == 0) {
                    mapVariablesTable.setRowSelectionInterval(0, 0);
                } else if (viewSelectionRow == mapVariablesTable.getRowCount()) {
                    viewSelectionRow--;
                    mapVariablesTable.setRowSelectionInterval(viewSelectionRow, viewSelectionRow);
                } else {
                    mapVariablesTable.setRowSelectionInterval(viewSelectionRow, viewSelectionRow);
                }
            }

            deleteButton.setEnabled((mapVariablesTable.getRowCount() != 0));
            parent.setSaveEnabled(true);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void mapVariablesTableKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mapVariablesTableKeyTyped
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_mapVariablesTableKeyTyped

    private List<String> getMapVariableTableValues() {
        List<String> sourceMap = new ArrayList<String>();
        for (int rowIndex = 0; rowIndex < mapVariablesTable.getRowCount(); rowIndex++) {
            String key = mapVariablesTable.getValueAt(rowIndex, 0).toString();

            if (!StringUtils.isBlank(key)) {
                sourceMap.add(key);
            }
        }

        return sourceMap;
    }

    private void setMapVariableTableValues(List<String> sourceMap) {
        ((DefaultTableModel) mapVariablesTable.getModel()).setRowCount(0);
        DefaultTableModel tableModel = (DefaultTableModel) mapVariablesTable.getModel();
        for (String entry : sourceMap) {
            tableModel.addRow(new Object[] { entry});
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private javax.swing.JLabel URL1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField channelIdField;
    private com.mirth.connect.client.ui.components.MirthComboBox channelNames;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel7;
    private com.mirth.connect.client.ui.components.MirthTable mapVariablesTable;
    private javax.swing.JButton newButton;
    private javax.swing.JScrollPane mapVariablesPane;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea template;
    // End of variables declaration//GEN-END:variables
}
