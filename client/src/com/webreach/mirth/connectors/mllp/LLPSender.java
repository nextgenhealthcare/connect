/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.mllp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.SwingWorker;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.QueuedSenderProperties;
import com.webreach.mirth.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class LLPSender extends ConnectorClass {

    /** Creates new form LLPSender */
    private HashMap channelList;

    public LLPSender() {
        name = LLPSenderProperties.name;
        initComponents();
        serverTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        reconnectInterval.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        maximumRetryCountField.setDocument(new MirthFieldConstraints(2, false, false, true));
        // ast: Acktimeout constrain
        ackTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(LLPSenderProperties.DATATYPE, name);
        properties.put(LLPSenderProperties.LLP_PROTOCOL_NAME, LLPSenderProperties.LLP_PROTOCOL_NAME_VALUE);
        properties.put(LLPSenderProperties.LLP_ADDRESS, hostAddressField.getText());
        properties.put(LLPSenderProperties.LLP_PORT, hostPortField.getText());
        properties.put(LLPSenderProperties.LLP_SERVER_TIMEOUT, serverTimeoutField.getText());
        properties.put(LLPSenderProperties.LLP_BUFFER_SIZE, bufferSizeField.getText());

        if (keepConnectionOpenYesRadio.isSelected()) {
            properties.put(LLPSenderProperties.LLP_KEEP_CONNECTION_OPEN, UIConstants.YES_OPTION);
        } else {
            properties.put(LLPSenderProperties.LLP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);
        }

        properties.put(LLPSenderProperties.LLP_MAX_RETRY_COUNT, maximumRetryCountField.getText());
        properties.put(LLPSenderProperties.LLP_START_OF_MESSAGE_CHARACTER, startOfMessageCharacterField.getText());
        properties.put(LLPSenderProperties.LLP_END_OF_MESSAGE_CHARACTER, endOfMessageCharacterField.getText());

        if (ascii.isSelected()) {
            properties.put(LLPSenderProperties.LLP_CHAR_ENCODING, "ascii");
        } else {
            properties.put(LLPSenderProperties.LLP_CHAR_ENCODING, "hex");
        }

        properties.put(LLPSenderProperties.LLP_RECORD_SEPARATOR, recordSeparatorField.getText());

        properties.put(LLPSenderProperties.LLP_SEGMENT_END, segmentEnd.getText());

        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectInterval.getText());

        if (usePersistentQueuesYesRadio.isSelected()) {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);
        }

        if (rotateMessages.isSelected()) {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        }

        properties.put(LLPSenderProperties.LLP_ACK_TIMEOUT, ackTimeoutField.getText());

        if (processHL7AckYesRadio.isSelected()) {
            properties.put(LLPSenderProperties.LLP_HL7_ACK_RESPONSE, UIConstants.YES_OPTION);
        } else {
            properties.put(LLPSenderProperties.LLP_HL7_ACK_RESPONSE, UIConstants.NO_OPTION);
        }

        properties.put(LLPSenderProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        properties.put(LLPSenderProperties.LLP_TEMPLATE, template.getText());
        properties.put(LLPSenderProperties.CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        hostAddressField.setText((String) props.get(LLPSenderProperties.LLP_ADDRESS));
        hostPortField.setText((String) props.get(LLPSenderProperties.LLP_PORT));
        serverTimeoutField.setText((String) props.get(LLPSenderProperties.LLP_SERVER_TIMEOUT));
        bufferSizeField.setText((String) props.get(LLPSenderProperties.LLP_BUFFER_SIZE));

        if (((String) props.get(LLPSenderProperties.LLP_KEEP_CONNECTION_OPEN)).equals(UIConstants.YES_OPTION)) {
            keepConnectionOpenYesRadio.setSelected(true);
        } else {
            keepConnectionOpenNoRadio.setSelected(true);
        }

        maximumRetryCountField.setText((String) props.get(LLPSenderProperties.LLP_MAX_RETRY_COUNT));

        if (((String) props.get(LLPSenderProperties.LLP_CHAR_ENCODING)).equals("ascii")) {
            ascii.setSelected(true);
        } else {
            hex.setSelected(true);
        }

        startOfMessageCharacterField.setText((String) props.get(LLPSenderProperties.LLP_START_OF_MESSAGE_CHARACTER));
        endOfMessageCharacterField.setText((String) props.get(LLPSenderProperties.LLP_END_OF_MESSAGE_CHARACTER));
        recordSeparatorField.setText((String) props.get(LLPSenderProperties.LLP_RECORD_SEPARATOR));
        segmentEnd.setText((String) props.get(LLPSenderProperties.LLP_SEGMENT_END));

        reconnectInterval.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));

        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION)) {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        } else {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }

        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION)) {
            rotateMessages.setSelected(true);
        } else {
            rotateMessages.setSelected(false);
        }

        if (((String) props.get(LLPSenderProperties.LLP_ACK_TIMEOUT)).equals("0")) {
            ignoreACKCheckBox.setSelected(true);
        } else {
            ignoreACKCheckBox.setSelected(false);
        }

        ignoreACKCheckBoxActionPerformed(null);

        ackTimeoutField.setText((String) props.get(LLPSenderProperties.LLP_ACK_TIMEOUT));

        if (((String) props.get(LLPSenderProperties.LLP_HL7_ACK_RESPONSE)).equals(UIConstants.YES_OPTION)) {
            processHL7AckYesRadio.setSelected(true);
        } else {
            processHL7AckNoRadio.setSelected(true);
        }

        template.setText((String) props.get(LLPSenderProperties.LLP_TEMPLATE));
        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(LLPSenderProperties.CONNECTOR_CHARSET_ENCODING));

        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");

        String selectedChannelName = "None";

        for (Channel channel : parent.channels.values()) {
            if (((String) props.get(LLPSenderProperties.CHANNEL_ID)).equalsIgnoreCase(channel.getId())) {
                selectedChannelName = channel.getName();
            }

            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        channelNames.setSelectedItem(selectedChannelName);

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults() {
        return new LLPSenderProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(LLPSenderProperties.LLP_ADDRESS)).length() == 0) {
            valid = false;
            if (highlight) {
                hostAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_PORT)).length() == 0) {
            valid = false;
            if (highlight) {
                hostPortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_SERVER_TIMEOUT)).length() == 0) {
            valid = false;
            if (highlight) {
                serverTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0) {
            valid = false;
            if (highlight) {
                reconnectInterval.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_BUFFER_SIZE)).length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_MAX_RETRY_COUNT)).length() == 0) {
            valid = false;
            if (highlight) {
                maximumRetryCountField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_TEMPLATE)).length() == 0) {
            valid = false;
            if (highlight) {
                template.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_ACK_TIMEOUT)).length() == 0) {
            valid = false;
            if (highlight) {
                ackTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_END_OF_MESSAGE_CHARACTER)).length() == 0) {
            valid = false;
            if (highlight) {
                endOfMessageCharacterField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_START_OF_MESSAGE_CHARACTER)).length() == 0) {
            valid = false;
            if (highlight) {
                startOfMessageCharacterField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_RECORD_SEPARATOR)).length() == 0) {
            valid = false;
            if (highlight) {
                recordSeparatorField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPSenderProperties.LLP_SEGMENT_END)).length() == 0) {
            valid = false;
            if (highlight) {
                segmentEnd.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    private void resetInvalidProperties() {
        hostAddressField.setBackground(null);
        hostPortField.setBackground(null);
        serverTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        maximumRetryCountField.setBackground(null);
        template.setBackground(null);
        ackTimeoutField.setBackground(null);
        endOfMessageCharacterField.setBackground(null);
        startOfMessageCharacterField.setBackground(null);
        recordSeparatorField.setBackground(null);
        segmentEnd.setBackground(null);
        reconnectInterval.setBackground(null);
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
        usePersistenceQueuesGroup = new javax.swing.ButtonGroup();
        processHL7AckGroup = new javax.swing.ButtonGroup();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        hostPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        serverTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        bufferSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        maximumRetryCountField = new com.webreach.mirth.client.ui.components.MirthTextField();
        startOfMessageCharacterField = new com.webreach.mirth.client.ui.components.MirthTextField();
        endOfMessageCharacterField = new com.webreach.mirth.client.ui.components.MirthTextField();
        recordSeparatorField = new com.webreach.mirth.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        hostAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel14 = new javax.swing.JLabel();
        ascii = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        hex = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        segmentEnd = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel37 = new javax.swing.JLabel();
        ackTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel19 = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel20 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        usePersistentQueuesYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usePersistentQueuesNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel7 = new javax.swing.JLabel();
        template = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        channelNames = new com.webreach.mirth.client.ui.components.MirthComboBox();
        URL = new javax.swing.JLabel();
        reconnectInterval = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel1 = new javax.swing.JLabel();
        ignoreACKCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        jLabel2 = new javax.swing.JLabel();
        processHL7AckYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        processHL7AckNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        rotateMessages = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        testConnection = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel13.setText("Keep Connection Open:");

        jLabel15.setText("Buffer Size (bytes):");

        jLabel16.setText("Send Timeout (ms):");

        jLabel17.setText("Host Port:");

        jLabel18.setText("Host Address:");

        jLabel8.setText("Maximum Retry Count:");

        jLabel10.setText("Start of Message Char:");

        jLabel11.setText("End of Message Char:");

        jLabel12.setText("Record Separator Char:");

        hostPortField.setToolTipText("Enter the port number to connect to (LLP Mode Client).");

        serverTimeoutField.setToolTipText("<html>Enter the number of milliseconds that the connector should keep its host connection open even if it has no messages to send.<br>If it has no messages to send after this much time,<br> it will close the connection and reopen it later when messages need to be sent.</html>");

        bufferSizeField.setToolTipText("<html>Enter at least the size, in bytes, of the largest response message expected.<br>Entering too small a value will cause larger messages to be rejected.<br>Entering too large a value wastes memory. Generally, the default value is fine.</html>");

        maximumRetryCountField.setToolTipText("The maximum number of times the connector should retry a failed attempt to connect to the host before giving up and logging an error.");

        startOfMessageCharacterField.setToolTipText("Enter the hexadecimal character (byte) value to send as the start of message character.");

        endOfMessageCharacterField.setToolTipText("Enter the hexadecimal character (byte) value to send as the end of message character.");

        recordSeparatorField.setToolTipText("Enter the hexadecimal character (byte) value to send as the record separator character.");

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setToolTipText("<html>the connector will keep its connection to the host open,<br> subject to the limitations of Send Timeout, across multiple messages.</html>");
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setToolTipText("the connector will open a connection for each message sent and close it immediately after sending.");
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        hostAddressField.setToolTipText("Enter the DNS domain name or IP address to connect to (LLP Mode Client).");

        jLabel14.setText("LLP Frame Encoding:");

        ascii.setBackground(new java.awt.Color(255, 255, 255));
        ascii.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(ascii);
        ascii.setText("ASCII");
        ascii.setToolTipText("Select ASCII to send messages unencoded.");
        ascii.setMargin(new java.awt.Insets(0, 0, 0, 0));

        hex.setBackground(new java.awt.Color(255, 255, 255));
        hex.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(hex);
        hex.setText("Hex");
        hex.setToolTipText("Select Hex to sends messages encoded in hexadecimal.");
        hex.setMargin(new java.awt.Insets(0, 0, 0, 0));

        segmentEnd.setToolTipText("Enter the hexadecimal character (byte) value to send as the end of segment character.");

        jLabel37.setText("End of Segment Char:");

        ackTimeoutField.setToolTipText("<html>If Ignore ACK is not checked, the number of milliseconds the connector should wait for an ACK response after sending a message.<br>If an ACK is not received in this time, the message's state is set to error.</html>");

        jLabel19.setText("ACK Timeout (ms):");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding to apply to the message sent to the Host,<br> or Default to use the default character set encoding for the JVM running the Mirth Server.</html>");
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        jLabel20.setText("Encoding:");

        jLabel36.setText("Use Persistent Queues:");

        usePersistentQueuesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usePersistenceQueuesGroup.add(usePersistentQueuesYesRadio);
        usePersistentQueuesYesRadio.setText("Yes");
        usePersistentQueuesYesRadio.setToolTipText("<html>If checked, the connector will store any messages that are unable to be successfully processed in a file-based queue.<br>Messages will be automatically resent until the queue is manually cleared or the message is successfully sent.<br>The default queue location is (Mirth Directory)/.mule/queuestore/(ChannelID),<br> where (Mirth Directory) is the main Mirth install root and (ChannelID) is the unique id of the current channel.</html>");
        usePersistentQueuesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesYesRadioActionPerformed(evt);
            }
        });

        usePersistentQueuesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usePersistenceQueuesGroup.add(usePersistentQueuesNoRadio);
        usePersistentQueuesNoRadio.setSelected(true);
        usePersistentQueuesNoRadio.setText("No");
        usePersistentQueuesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesNoRadioActionPerformed(evt);
            }
        });

        jLabel7.setText("Template:");

        template.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        channelNames.setToolTipText("<html>If None is selected, responses sent by the Host are discarded.<br>Otherwise, they are sent to the selected channel as a new inbound message for processing.</html>");

        URL.setText("Send Response to:");

        reconnectInterval.setToolTipText("<html>The reconnect interval determines the amount of time, in milliseconds,<br> Mirth will wait before resending a failed message.</html>");

        jLabel1.setText("Reconnect Interval (ms):");

        ignoreACKCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        ignoreACKCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreACKCheckBox.setText("Ignore ACK");
        ignoreACKCheckBox.setToolTipText("<html>If checked, the connector does not wait for an ACK response after sending a message.<br>If not checked, the connector waits for an ACK for the time specified in ACK Timeout.</html>");
        ignoreACKCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ignoreACKCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreACKCheckBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Process HL7 ACK:");

        processHL7AckYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        processHL7AckYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processHL7AckGroup.add(processHL7AckYesRadio);
        processHL7AckYesRadio.setText("Yes");
        processHL7AckYesRadio.setToolTipText("<html>This setting configures the behavior of Mirth's ACK processing.<br>When this setting is on, only successful ACK codes will allow a message to be marked as processed.<br>When this setting is turned off, Mirth will not attempt to parse the ACK response.</html>");
        processHL7AckYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processHL7AckNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        processHL7AckNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processHL7AckGroup.add(processHL7AckNoRadio);
        processHL7AckNoRadio.setText("No");
        processHL7AckNoRadio.setToolTipText("<html>This setting configures the behavior of Mirth's ACK processing.<br>When this setting is on, only successful ACK codes will allow a message to be marked as processed.<br>When this setting is turned off, Mirth will not attempt to parse the ACK response.</html>");
        processHL7AckNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        rotateMessages.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessages.setText("Rotate Messages in Queue");
        rotateMessages.setToolTipText("<html>If checked, upon unsuccessful re-try, it will rotate and put the queued message to the back of the queue<br> in order to prevent it from clogging the queue and to let the other subsequent messages in queue be processed.<br>If the order of messages processed is important, this should be unchecked.</html>");

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel18)
                    .addComponent(jLabel17)
                    .addComponent(jLabel16)
                    .addComponent(jLabel15)
                    .addComponent(jLabel13)
                    .addComponent(jLabel8)
                    .addComponent(jLabel14)
                    .addComponent(jLabel10)
                    .addComponent(jLabel12)
                    .addComponent(jLabel36)
                    .addComponent(jLabel19)
                    .addComponent(jLabel20)
                    .addComponent(URL)
                    .addComponent(jLabel7)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reconnectInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rotateMessages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ackTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoreACKCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(processHL7AckYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(processHL7AckNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startOfMessageCharacterField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endOfMessageCharacterField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ascii, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(maximumRetryCountField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hostAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testConnection))
                    .addComponent(hostPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(recordSeparatorField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel37)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(segmentEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hostPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(serverTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(reconnectInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(maximumRetryCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(ascii, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(testConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startOfMessageCharacterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(endOfMessageCharacterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(recordSeparatorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37)
                    .addComponent(segmentEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotateMessages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(ackTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ignoreACKCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(processHL7AckYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(processHL7AckNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(URL)
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ignoreACKCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ignoreACKCheckBoxActionPerformed
    {//GEN-HEADEREND:event_ignoreACKCheckBoxActionPerformed
        if (ignoreACKCheckBox.isSelected()) {
            ackTimeoutField.setText("0");
            ackTimeoutField.setEnabled(false);
        } else {
            ackTimeoutField.setEnabled(true);
        }
    }//GEN-LAST:event_ignoreACKCheckBoxActionPerformed

private void usePersistentQueuesYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesYesRadioActionPerformed
    rotateMessages.setEnabled(true);
}//GEN-LAST:event_usePersistentQueuesYesRadioActionPerformed

private void usePersistentQueuesNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesNoRadioActionPerformed
    rotateMessages.setEnabled(false);
}//GEN-LAST:event_usePersistentQueuesNoRadioActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
    parent.setWorking("Testing connection...", true);

    SwingWorker worker = new SwingWorker<Void, Void>() {

        public Void doInBackground() {

            try {
                ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(name, "testConnection", getProperties());

                if (response == null) {
                    throw new ClientException("Failed to invoke service.");
                } else if (response.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                    parent.alertInformation(parent, response.getMessage());
                } else {
                    parent.alertWarning(parent, response.getMessage());
                }

                return null;
            } catch (ClientException e) {
                parent.alertError(parent, e.getMessage());
                return null;
            }
        }

        public void done() {
            parent.setWorking("", false);
        }
    };

    worker.execute();
}//GEN-LAST:event_testConnectionActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_charsetEncodingComboboxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private com.webreach.mirth.client.ui.components.MirthTextField ackTimeoutField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton ascii;
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.webreach.mirth.client.ui.components.MirthComboBox channelNames;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField endOfMessageCharacterField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton hex;
    private com.webreach.mirth.client.ui.components.MirthTextField hostAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField hostPortField;
    private com.webreach.mirth.client.ui.components.MirthCheckBox ignoreACKCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.webreach.mirth.client.ui.components.MirthTextField maximumRetryCountField;
    private javax.swing.ButtonGroup processHL7AckGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processHL7AckNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processHL7AckYesRadio;
    private com.webreach.mirth.client.ui.components.MirthTextField reconnectInterval;
    private com.webreach.mirth.client.ui.components.MirthTextField recordSeparatorField;
    private com.webreach.mirth.client.ui.components.MirthCheckBox rotateMessages;
    private com.webreach.mirth.client.ui.components.MirthTextField segmentEnd;
    private com.webreach.mirth.client.ui.components.MirthTextField serverTimeoutField;
    private com.webreach.mirth.client.ui.components.MirthTextField startOfMessageCharacterField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea template;
    private javax.swing.JButton testConnection;
    private javax.swing.ButtonGroup usePersistenceQueuesGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    // End of variables declaration//GEN-END:variables
}
