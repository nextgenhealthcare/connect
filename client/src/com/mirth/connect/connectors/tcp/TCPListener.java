/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Step;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class TCPListener extends ConnectorClass {

    /** Creates new form TCPListener */
    public TCPListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = TCPListenerProperties.name;
        initComponents();
        receiveTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(TCPListenerProperties.DATATYPE, name);
        properties.put(TCPListenerProperties.TCP_ADDRESS, listenerAddressField.getText());
        properties.put(TCPListenerProperties.TCP_PORT, listenerPortField.getText());
        properties.put(TCPListenerProperties.TCP_RECEIVE_TIMEOUT, receiveTimeoutField.getText());
        properties.put(TCPListenerProperties.TCP_BUFFER_SIZE, bufferSizeField.getText());

        properties.put(TCPListenerProperties.TCP_RESPONSE_VALUE, (String) responseFromTransformer.getSelectedItem());

        // ast:encoding
        properties.put(TCPListenerProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        if (dataTypeBinary.isSelected()) {
            properties.put(TCPListenerProperties.TCP_TYPE, UIConstants.YES_OPTION);
        } else {
            properties.put(TCPListenerProperties.TCP_TYPE, UIConstants.NO_OPTION);
        }

        if (ackOnNewConnectionYes.isSelected()) {
            properties.put(TCPListenerProperties.TCP_ACK_NEW_CONNECTION, UIConstants.YES_OPTION);
        } else {
            properties.put(TCPListenerProperties.TCP_ACK_NEW_CONNECTION, UIConstants.NO_OPTION);
        }

        properties.put(TCPListenerProperties.TCP_ACK_NEW_CONNECTION_IP, ackAddressField.getText());
        properties.put(TCPListenerProperties.TCP_ACK_NEW_CONNECTION_PORT, ackPortField.getText());
        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        listenerAddressField.setText((String) props.get(TCPListenerProperties.TCP_ADDRESS));
        listenerPortField.setText((String) props.get(TCPListenerProperties.TCP_PORT));
        receiveTimeoutField.setText((String) props.get(TCPListenerProperties.TCP_RECEIVE_TIMEOUT));
        bufferSizeField.setText((String) props.get(TCPListenerProperties.TCP_BUFFER_SIZE));

        boolean enabled = parent.isSaveEnabled();

        updateResponseDropDown();

        if (parent.channelEditPanel.synchronousCheckBox.isSelected()) {
            responseFromTransformer.setSelectedItem((String) props.getProperty(TCPListenerProperties.TCP_RESPONSE_VALUE));
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(TCPListenerProperties.CONNECTOR_CHARSET_ENCODING));

        if (((String) props.get(TCPListenerProperties.TCP_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            dataTypeBinary.setSelected(true);
            dataTypeBinaryActionPerformed(null);
        } else {
            dataTypeASCII.setSelected(true);
            dataTypeASCIIActionPerformed(null);
        }

        if (((String) props.get(TCPListenerProperties.TCP_ACK_NEW_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            ackOnNewConnectionYesActionPerformed(null);
            ackOnNewConnectionYes.setSelected(true);
        } else {
            ackOnNewConnectionNoActionPerformed(null);
            ackOnNewConnectionNo.setSelected(true);
        }

        ackAddressField.setText((String) props.get(TCPListenerProperties.TCP_ACK_NEW_CONNECTION_IP));
        ackPortField.setText((String) props.get(TCPListenerProperties.TCP_ACK_NEW_CONNECTION_PORT));

        parent.setSaveEnabled(enabled);
    }

    public Properties getDefaults() {
        return new TCPListenerProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(TCPListenerProperties.TCP_ADDRESS)).length() <= 3) {
            valid = false;
            if (highlight) {
                listenerAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPListenerProperties.TCP_PORT)).length() == 0) {
            valid = false;
            if (highlight) {
                listenerPortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPListenerProperties.TCP_RECEIVE_TIMEOUT)).length() == 0) {
            valid = false;
            if (highlight) {
                receiveTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPListenerProperties.TCP_BUFFER_SIZE)).length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPListenerProperties.TCP_ACK_NEW_CONNECTION)).equals(UIConstants.YES_OPTION)) {
            if (((String) props.get(TCPListenerProperties.TCP_ACK_NEW_CONNECTION_IP)).length() <= 3) {
                valid = false;
                if (highlight) {
                    ackAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (((String) props.get(TCPListenerProperties.TCP_ACK_NEW_CONNECTION_PORT)).length() == 0) {
                valid = false;
                if (highlight) {
                    ackPortField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    private void resetInvalidProperties() {
        listenerAddressField.setBackground(null);
        listenerPortField.setBackground(null);
        receiveTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        ackAddressField.setBackground(null);
        ackPortField.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

        return error;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        keepConnectionOpenGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        dataTypeButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        bufferSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        listenerPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        listenerAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        ackOnNewConnectionLabel = new javax.swing.JLabel();
        ackOnNewConnectionYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ackOnNewConnectionNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ackIPLabel = new javax.swing.JLabel();
        ackPortLabel = new javax.swing.JLabel();
        ackPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        ackAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        responseFromLabel = new javax.swing.JLabel();
        responseFromTransformer = new com.mirth.connect.client.ui.components.MirthComboBox();
        dataTypeASCII = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeBinary = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setText("Listener Address:");

        jLabel2.setText("Listener Port:");

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size (bytes):");

        bufferSizeField.setToolTipText("<html>Use larger values for larger messages, and smaller values <br>for smaller messages. Generally, the default value is fine.</html>");

        receiveTimeoutField.setToolTipText("The amount of time, in milliseconds, to wait without receiving a message before closing a connection.");

        listenerPortField.setToolTipText("The port to listen for connections on.");

        listenerAddressField.setToolTipText("The DNS domain name or IP address to listen for connections on.");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding used by the message sender,<br> or Select Default to use the default character set encoding for the JVM running Mirth.</html>");

        encodingLabel.setText("Encoding:");

        ackOnNewConnectionLabel.setText("Response on New Connection:");

        ackOnNewConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        ackOnNewConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(ackOnNewConnectionYes);
        ackOnNewConnectionYes.setText("Yes");
        ackOnNewConnectionYes.setToolTipText("<html>Select No to send the message response on the same connection as the inbound message was received on.<br>Select Yes to send the response on a new connection.</html>");
        ackOnNewConnectionYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ackOnNewConnectionYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ackOnNewConnectionYesActionPerformed(evt);
            }
        });

        ackOnNewConnectionNo.setBackground(new java.awt.Color(255, 255, 255));
        ackOnNewConnectionNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(ackOnNewConnectionNo);
        ackOnNewConnectionNo.setText("No");
        ackOnNewConnectionNo.setToolTipText("<html>Select No to send the message response on the same connection as the inbound message was received on.<br>Select Yes to send the response on a new connection.</html>");
        ackOnNewConnectionNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ackOnNewConnectionNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ackOnNewConnectionNoActionPerformed(evt);
            }
        });

        ackIPLabel.setText("Response Address:");

        ackPortLabel.setText("Response Port:");

        ackPortField.setToolTipText("Enter the port to send message responses to.");

        ackAddressField.setToolTipText("Enter the DNS domain name or IP address to send message responses to.");

        responseFromLabel.setText("Respond from:");

        responseFromTransformer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        responseFromTransformer.setToolTipText("<html>Select \"None\" to send no response.<br>Select a destination of this channel that will supply a return value using the Response Map.<br>Select a variable that has been added to the Response Map.</html>");

        dataTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        dataTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataTypeButtonGroup.add(dataTypeASCII);
        dataTypeASCII.setSelected(true);
        dataTypeASCII.setText("ASCII");
        dataTypeASCII.setToolTipText("<html>Select Binary if the inbound messages are raw byte streams.<br>Select ASCII if the inbound messages are text streams.</html>");
        dataTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dataTypeASCII.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeASCIIActionPerformed(evt);
            }
        });

        dataTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        dataTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataTypeButtonGroup.add(dataTypeBinary);
        dataTypeBinary.setText("Binary");
        dataTypeBinary.setToolTipText("<html>Select Binary if the inbound messages are raw byte streams.<br>Select ASCII if the inbound messages are text streams.</html>");
        dataTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dataTypeBinary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeBinaryActionPerformed(evt);
            }
        });

        dataTypeLabel.setText("Data Type:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dataTypeLabel)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(encodingLabel)
                    .addComponent(responseFromLabel)
                    .addComponent(ackOnNewConnectionLabel)
                    .addComponent(ackIPLabel)
                    .addComponent(ackPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(listenerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(listenerPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseFromTransformer, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ackAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ackPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ackOnNewConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ackOnNewConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dataTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dataTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(88, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listenerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listenerPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encodingLabel)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataTypeLabel)
                    .addComponent(dataTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseFromLabel)
                    .addComponent(responseFromTransformer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackOnNewConnectionLabel)
                    .addComponent(ackOnNewConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ackOnNewConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ackIPLabel)
                    .addComponent(ackAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackPortLabel)
                    .addComponent(ackPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(108, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dataTypeBinaryActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataTypeBinaryActionPerformed
    {//GEN-HEADEREND:event_dataTypeBinaryActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }//GEN-LAST:event_dataTypeBinaryActionPerformed

    private void dataTypeASCIIActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataTypeASCIIActionPerformed
    {//GEN-HEADEREND:event_dataTypeASCIIActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }//GEN-LAST:event_dataTypeASCIIActionPerformed

    public void updateResponseDropDown() {
        boolean enabled = parent.isSaveEnabled();

        String selectedItem = (String) responseFromTransformer.getSelectedItem();

        Channel channel = parent.channelEditPanel.currentChannel;

        Set<String> variables = new LinkedHashSet<String>();

        variables.add("None");

        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getSteps());

        List<String> scripts = new ArrayList<String>();

        for (Connector connector : channel.getDestinationConnectors()) {
            if (connector.getTransportName().equals("Database Writer")) {
                if (connector.getProperties().getProperty("useScript").equals(UIConstants.YES_OPTION)) {
                    scripts.add(connector.getProperties().getProperty("script"));
                }

            } else if (connector.getTransportName().equals("JavaScript Writer")) {
                scripts.add(connector.getProperties().getProperty("script"));
            }

            variables.add(connector.getName());
            stepsToCheck.addAll(connector.getTransformer().getSteps());
        }

        Pattern pattern = Pattern.compile(RESULT_PATTERN);

        int i = 0;
        for (Iterator it = stepsToCheck.iterator(); it.hasNext();) {
            Step step = (Step) it.next();
            Map data;
            data = (Map) step.getData();

            if (step.getType().equalsIgnoreCase(TransformerPane.JAVASCRIPT_TYPE)) {
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find()) {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            } else if (step.getType().equalsIgnoreCase(TransformerPane.MAPPER_TYPE)) {
                if (data.containsKey(UIConstants.IS_GLOBAL)) {
                    if (((String) data.get(UIConstants.IS_GLOBAL)).equalsIgnoreCase(UIConstants.IS_GLOBAL_RESPONSE)) {
                        variables.add((String) data.get("Variable"));
                    }
                }
            }
        }

        scripts.add(channel.getPreprocessingScript());
        scripts.add(channel.getPostprocessingScript());

        for (String script : scripts) {
            if (script != null && script.length() > 0) {
                Matcher matcher = pattern.matcher(script);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
        }

        responseFromTransformer.setModel(new DefaultComboBoxModel(variables.toArray()));

        if (variables.contains(selectedItem)) {
            responseFromTransformer.setSelectedItem(selectedItem);
        } else {
            responseFromTransformer.setSelectedIndex(0);
        }

        if (!parent.channelEditPanel.synchronousCheckBox.isSelected()) {
            responseFromTransformer.setEnabled(false);
            responseFromLabel.setEnabled(false);
            responseFromTransformer.setSelectedIndex(0);
        } else {
            responseFromTransformer.setEnabled(true);
            responseFromLabel.setEnabled(true);
        }

        parent.setSaveEnabled(enabled);
    }

    private void ackOnNewConnectionNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionNoActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionNoActionPerformed
        ackAddressField.setEnabled(false);
        ackPortField.setEnabled(false);
        ackIPLabel.setEnabled(false);
        ackPortLabel.setEnabled(false);
    }// GEN-LAST:event_ackOnNewConnectionNoActionPerformed

    private void ackOnNewConnectionYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionYesActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionYesActionPerformed
        ackAddressField.setEnabled(true);
        ackPortField.setEnabled(true);
        ackIPLabel.setEnabled(true);
        ackPortLabel.setEnabled(true);
    }// GEN-LAST:event_ackOnNewConnectionYesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTextField ackAddressField;
    private javax.swing.JLabel ackIPLabel;
    private javax.swing.JLabel ackOnNewConnectionLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton ackOnNewConnectionNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton ackOnNewConnectionYes;
    private com.mirth.connect.client.ui.components.MirthTextField ackPortField;
    private javax.swing.JLabel ackPortLabel;
    private com.mirth.connect.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeASCII;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeBinary;
    private javax.swing.ButtonGroup dataTypeButtonGroup;
    private javax.swing.JLabel dataTypeLabel;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private com.mirth.connect.client.ui.components.MirthTextField listenerAddressField;
    private com.mirth.connect.client.ui.components.MirthTextField listenerPortField;
    private com.mirth.connect.client.ui.components.MirthTextField receiveTimeoutField;
    private javax.swing.JLabel responseFromLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox responseFromTransformer;
    // End of variables declaration//GEN-END:variables
}
