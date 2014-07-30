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
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        URL.setText("Channel Id:");

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(URL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(channelIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 13, Short.MAX_VALUE)))
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
                        .addComponent(jLabel7)
                        .addGap(0, 133, Short.MAX_VALUE))
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addContainerGap())
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField channelIdField;
    private com.mirth.connect.client.ui.components.MirthComboBox channelNames;
    private javax.swing.JLabel jLabel7;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea template;
    // End of variables declaration//GEN-END:variables
}
