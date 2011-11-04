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
import java.util.HashMap;
import java.util.Properties;

import javax.swing.SwingWorker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class TCPSender extends ConnectorClass {

    private HashMap channelList;

    public TCPSender() {
        name = TCPSenderProperties.name;
        initComponents();
        serverTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        reconnectIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        queuePollIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        maximumRetryCountField.setDocument(new MirthFieldConstraints(2, false, false, true));
        // ast: Acktimeout constrain
        ackTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(TCPSenderProperties.DATATYPE, name);
        properties.put(TCPSenderProperties.TCP_ADDRESS, hostAddressField.getText());
        properties.put(TCPSenderProperties.TCP_PORT, hostPortField.getText());
        properties.put(TCPSenderProperties.TCP_SERVER_TIMEOUT, serverTimeoutField.getText());
        properties.put(TCPSenderProperties.TCP_BUFFER_SIZE, bufferSizeField.getText());

        if (keepConnectionOpenYesRadio.isSelected()) {
            properties.put(TCPSenderProperties.TCP_KEEP_CONNECTION_OPEN, UIConstants.YES_OPTION);
        } else {
            properties.put(TCPSenderProperties.TCP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);
        }

        properties.put(TCPSenderProperties.TCP_MAX_RETRY_COUNT, maximumRetryCountField.getText());

        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectIntervalField.getText());

        if (usePersistentQueuesYesRadio.isSelected()) {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);
        }

        properties.put(QueuedSenderProperties.QUEUE_POLL_INTERVAL, queuePollIntervalField.getText());

        if (rotateMessagesCheckBox.isSelected()) {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        }

        properties.put(TCPSenderProperties.TCP_ACK_TIMEOUT, ackTimeoutField.getText());
        properties.put(TCPSenderProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        if (dataTypeBinary.isSelected()) {
            properties.put(TCPSenderProperties.TCP_TYPE, UIConstants.YES_OPTION);
        } else {
            properties.put(TCPSenderProperties.TCP_TYPE, UIConstants.NO_OPTION);
        }

        properties.put(TCPSenderProperties.TCP_TEMPLATE, template.getText());
        properties.put(TCPSenderProperties.CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        hostAddressField.setText((String) props.get(TCPSenderProperties.TCP_ADDRESS));
        hostPortField.setText((String) props.get(TCPSenderProperties.TCP_PORT));
        serverTimeoutField.setText((String) props.get(TCPSenderProperties.TCP_SERVER_TIMEOUT));
        bufferSizeField.setText((String) props.get(TCPSenderProperties.TCP_BUFFER_SIZE));

        if (((String) props.get(TCPSenderProperties.TCP_KEEP_CONNECTION_OPEN)).equals(UIConstants.YES_OPTION)) {
            keepConnectionOpenYesRadio.setSelected(true);
        } else {
            keepConnectionOpenNoRadio.setSelected(true);
        }

        maximumRetryCountField.setText((String) props.get(TCPSenderProperties.TCP_MAX_RETRY_COUNT));

        reconnectIntervalField.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));

        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION)) {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        } else {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }

        queuePollIntervalField.setText((String) props.get(QueuedSenderProperties.QUEUE_POLL_INTERVAL));

        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION)) {
            rotateMessagesCheckBox.setSelected(true);
        } else {
            rotateMessagesCheckBox.setSelected(false);
        }

        if (((String) props.get(TCPSenderProperties.TCP_ACK_TIMEOUT)).equals("0")) {
            ignoreACKCheckBox.setSelected(true);
        } else {
            ignoreACKCheckBox.setSelected(false);
        }

        ignoreACKCheckBoxActionPerformed(null);

        ackTimeoutField.setText((String) props.get(TCPSenderProperties.TCP_ACK_TIMEOUT));

        template.setText((String) props.get(TCPSenderProperties.TCP_TEMPLATE));
        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(TCPSenderProperties.CONNECTOR_CHARSET_ENCODING));

        if (((String) props.get(TCPSenderProperties.TCP_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            dataTypeBinary.setSelected(true);
            dataTypeBinaryActionPerformed(null);
        } else {
            dataTypeASCII.setSelected(true);
            dataTypeASCIIActionPerformed(null);
        }

        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");

        String selectedChannelName = "None";

        for (Channel channel : parent.channels.values()) {
            if (((String) props.get(TCPSenderProperties.CHANNEL_ID)).equalsIgnoreCase(channel.getId())) {
                selectedChannelName = channel.getName();
            }

            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean enabled = parent.isSaveEnabled();

        channelNames.setSelectedItem(selectedChannelName);

        parent.setSaveEnabled(enabled);
    }

    public Properties getDefaults() {
        return new TCPSenderProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(TCPSenderProperties.TCP_ADDRESS)).length() <= 3) {
            valid = false;
            if (highlight) {
                hostAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPSenderProperties.TCP_PORT)).length() == 0) {
            valid = false;
            if (highlight) {
                hostPortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPSenderProperties.TCP_SERVER_TIMEOUT)).length() == 0) {
            valid = false;
            if (highlight) {
                serverTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0) {
            valid = false;
            if (highlight) {
                reconnectIntervalField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPSenderProperties.TCP_BUFFER_SIZE)).length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPSenderProperties.TCP_MAX_RETRY_COUNT)).length() == 0) {
            valid = false;
            if (highlight) {
                maximumRetryCountField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPSenderProperties.TCP_TEMPLATE)).length() == 0) {
            valid = false;
            if (highlight) {
                template.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(TCPSenderProperties.TCP_ACK_TIMEOUT)).length() == 0) {
            valid = false;
            if (highlight) {
                ackTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION)) {

            if (((String) props.get(QueuedSenderProperties.QUEUE_POLL_INTERVAL)).length() == 0) {
                valid = false;
                if (highlight) {
                    queuePollIntervalField.setBackground(UIConstants.INVALID_COLOR);
                }
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
        reconnectIntervalField.setBackground(null);
        queuePollIntervalField.setBackground(null);
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
        usePersistenceQueuesGroup = new javax.swing.ButtonGroup();
        dataTypeButtonGroup = new javax.swing.ButtonGroup();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        hostPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        serverTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        bufferSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        maximumRetryCountField = new com.mirth.connect.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        hostAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        ackTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel19 = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        usePersistentQueuesYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usePersistentQueuesNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel7 = new javax.swing.JLabel();
        template = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        channelNames = new com.mirth.connect.client.ui.components.MirthComboBox();
        URL = new javax.swing.JLabel();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        reconnectIntervalLabel = new javax.swing.JLabel();
        ignoreACKCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        dataTypeASCII = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeBinary = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeLabel = new javax.swing.JLabel();
        rotateMessagesCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        testConnection = new javax.swing.JButton();
        queuePollIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        queuePollIntervalLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel13.setText("Keep Connection Open:");

        jLabel15.setText("Buffer Size (bytes):");

        jLabel16.setText("Send Timeout (ms):");

        jLabel17.setText("Host Port:");

        jLabel18.setText("Host Address:");

        jLabel8.setText("Maximum Retry Count:");

        hostPortField.setToolTipText("The port on which to connect.");

        serverTimeoutField.setToolTipText("The number of milliseconds to keep the connection to the host open.");

        bufferSizeField.setToolTipText("The size, in bytes, of the buffer to be used to hold messages waiting to be sent. Generally, the default value is fine.");

        maximumRetryCountField.setToolTipText("The maximum number of times to retry an attempt to connect to the host before logging an error.");

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setToolTipText("<html>Select Yes to keep the connection to the host open across multiple messages.<br>Select No to immediately the close the connection to the host after sending each message.</html>");
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setToolTipText("<html>Select Yes to keep the connection to the host open across multiple messages.<br>Select No to immediately the close the connection to the host after sending each message.</html>");
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        hostAddressField.setToolTipText("The DNS domain name or IP address on which to connect.");

        ackTimeoutField.setToolTipText("The number of milliseconds the connector should wait for a response from the host after sending a message.");

        jLabel19.setText("Response Timeout (ms):");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("<html>The character set encoding to use when converting the outbound message to a byte stream if Data Type ASCII is selected below.<br>Select Default to use the default character set encoding for the JVM running the Mirth server.</html>");
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        encodingLabel.setText("Encoding:");

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
        channelNames.setToolTipText("<html>Select None to not process any response received from the host,<br> or select the channel to send the response to as a new inbound message.</html>");

        URL.setText("Send Response to:");

        reconnectIntervalField.setToolTipText("<html>The number of milliseconds to wait after closing a connection to the host <br>before opening a new connection, even if there are messages to send.<br>This is used for both the retry count and time to wait between queue retries, <br>if queuing is enabled.</html>");

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        ignoreACKCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        ignoreACKCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreACKCheckBox.setText("Ignore Response");
        ignoreACKCheckBox.setToolTipText("<html>If checked, the connector will not wait for a response after sending a message.<br>If unchecked, the connector will wait for a response from the host after each message is sent<br> and optionally pass the response received to a Mirth channel.</html>");
        ignoreACKCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ignoreACKCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreACKCheckBoxActionPerformed(evt);
            }
        });

        dataTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        dataTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataTypeButtonGroup.add(dataTypeASCII);
        dataTypeASCII.setSelected(true);
        dataTypeASCII.setText("ASCII");
        dataTypeASCII.setToolTipText("Select ASCII if the outbound message is text (will undergo character set encoding).");
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
        dataTypeBinary.setToolTipText("Select Binary if the outbound message is a byte stream (will not undergo character set encoding).");
        dataTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dataTypeBinary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeBinaryActionPerformed(evt);
            }
        });

        dataTypeLabel.setText("Data Type:");

        rotateMessagesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessagesCheckBox.setText("Rotate Messages in Queue");
        rotateMessagesCheckBox.setToolTipText("<html>If checked, upon unsuccessful re-try, it will rotate and put the queued message to the back of the queue<br> in order to prevent it from clogging the queue and to let the other subsequent messages in queue be processed.<br>If the order of messages processed is important, this should be unchecked.</html>");

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        queuePollIntervalField.setToolTipText("<html>The amount of time that should elapse between polls of an empty queue to check for queued messages.</html>");

        queuePollIntervalLabel.setText("Queue Poll Interval (ms):");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(URL)
                    .addComponent(dataTypeLabel)
                    .addComponent(encodingLabel)
                    .addComponent(jLabel19)
                    .addComponent(queuePollIntervalLabel)
                    .addComponent(jLabel36)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel18)
                                .addComponent(jLabel17)
                                .addComponent(jLabel16)
                                .addComponent(jLabel15)
                                .addComponent(jLabel13)
                                .addComponent(jLabel8)))
                        .addComponent(reconnectIntervalLabel))
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maximumRetryCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hostAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testConnection))
                    .addComponent(hostPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(rotateMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(queuePollIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ackTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoreACKCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dataTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dataTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(testConnection)
                    .addComponent(hostAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maximumRetryCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reconnectIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotateMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(queuePollIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queuePollIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ignoreACKCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(encodingLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(URL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                .addContainerGap())
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

    private void ignoreACKCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ignoreACKCheckBoxActionPerformed
    {//GEN-HEADEREND:event_ignoreACKCheckBoxActionPerformed
        if (ignoreACKCheckBox.isSelected()) {
            ackTimeoutField.setText("0");
            ackTimeoutField.setEnabled(false);
        } else {
            ackTimeoutField.setEnabled(true);
        }
    }//GEN-LAST:event_ignoreACKCheckBoxActionPerformed

private void usePersistentQueuesNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesNoRadioActionPerformed
    rotateMessagesCheckBox.setEnabled(false);
    queuePollIntervalLabel.setEnabled(false);
    queuePollIntervalField.setEnabled(false);
}//GEN-LAST:event_usePersistentQueuesNoRadioActionPerformed

private void usePersistentQueuesYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesYesRadioActionPerformed
    rotateMessagesCheckBox.setEnabled(true);
    queuePollIntervalLabel.setEnabled(true);
    queuePollIntervalField.setEnabled(true);
}//GEN-LAST:event_usePersistentQueuesYesRadioActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
    final String workingId = parent.startWorking("Testing connection...");

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
            parent.stopWorking(workingId);
        }
    };

    worker.execute();
}//GEN-LAST:event_testConnectionActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_charsetEncodingComboboxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private com.mirth.connect.client.ui.components.MirthTextField ackTimeoutField;
    private com.mirth.connect.client.ui.components.MirthTextField bufferSizeField;
    private com.mirth.connect.client.ui.components.MirthComboBox channelNames;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeASCII;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeBinary;
    private javax.swing.ButtonGroup dataTypeButtonGroup;
    private javax.swing.JLabel dataTypeLabel;
    private javax.swing.JLabel encodingLabel;
    private com.mirth.connect.client.ui.components.MirthTextField hostAddressField;
    private com.mirth.connect.client.ui.components.MirthTextField hostPortField;
    private com.mirth.connect.client.ui.components.MirthCheckBox ignoreACKCheckBox;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField maximumRetryCountField;
    private com.mirth.connect.client.ui.components.MirthTextField queuePollIntervalField;
    private javax.swing.JLabel queuePollIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox rotateMessagesCheckBox;
    private com.mirth.connect.client.ui.components.MirthTextField serverTimeoutField;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea template;
    private javax.swing.JButton testConnection;
    private javax.swing.ButtonGroup usePersistenceQueuesGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    // End of variables declaration//GEN-END:variables
}
