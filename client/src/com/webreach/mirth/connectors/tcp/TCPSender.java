/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.connectors.tcp;

import com.webreach.mirth.client.core.ClientException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.QueuedSenderProperties;
import java.util.Map;
import org.jdesktop.swingworker.SwingWorker;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class TCPSender extends ConnectorClass
{
    private HashMap channelList;

    public TCPSender()
    {
        name = TCPSenderProperties.name;
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

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(TCPSenderProperties.DATATYPE, name);
        properties.put(TCPSenderProperties.TCP_ADDRESS, hostAddressField.getText());
        properties.put(TCPSenderProperties.TCP_PORT, hostPortField.getText());
        properties.put(TCPSenderProperties.TCP_SERVER_TIMEOUT, serverTimeoutField.getText());
        properties.put(TCPSenderProperties.TCP_BUFFER_SIZE, bufferSizeField.getText());

        if (keepConnectionOpenYesRadio.isSelected())
            properties.put(TCPSenderProperties.TCP_KEEP_CONNECTION_OPEN, UIConstants.YES_OPTION);
        else
            properties.put(TCPSenderProperties.TCP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);

        properties.put(TCPSenderProperties.TCP_MAX_RETRY_COUNT, maximumRetryCountField.getText());

        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectInterval.getText());
        
        if (usePersistentQueuesYesRadio.isSelected())
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        else
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);

        if (rotateMessages.isSelected())
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        else
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        
        properties.put(TCPSenderProperties.TCP_ACK_TIMEOUT, ackTimeoutField.getText());
        properties.put(TCPSenderProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        
        if (dataTypeBinary.isSelected())
            properties.put(TCPSenderProperties.TCP_TYPE, UIConstants.YES_OPTION);
        else
            properties.put(TCPSenderProperties.TCP_TYPE, UIConstants.NO_OPTION);
        
        properties.put(TCPSenderProperties.TCP_TEMPLATE, template.getText());
        properties.put(TCPSenderProperties.CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));
        
        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        hostAddressField.setText((String) props.get(TCPSenderProperties.TCP_ADDRESS));
        hostPortField.setText((String) props.get(TCPSenderProperties.TCP_PORT));
        serverTimeoutField.setText((String) props.get(TCPSenderProperties.TCP_SERVER_TIMEOUT));
        bufferSizeField.setText((String) props.get(TCPSenderProperties.TCP_BUFFER_SIZE));

        if (((String) props.get(TCPSenderProperties.TCP_KEEP_CONNECTION_OPEN)).equals(UIConstants.YES_OPTION))
            keepConnectionOpenYesRadio.setSelected(true);
        else
            keepConnectionOpenNoRadio.setSelected(true);

        maximumRetryCountField.setText((String) props.get(TCPSenderProperties.TCP_MAX_RETRY_COUNT));

        reconnectInterval.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));
        
        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION))
        {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        }
        else
        {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }
        
        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION))
            rotateMessages.setSelected(true);
        else
            rotateMessages.setSelected(false);
        
        if (((String) props.get(TCPSenderProperties.TCP_ACK_TIMEOUT)).equals("0"))
            ignoreACKCheckBox.setSelected(true);
        else
            ignoreACKCheckBox.setSelected(false);
        
        ignoreACKCheckBoxActionPerformed(null);
        
        ackTimeoutField.setText((String) props.get(TCPSenderProperties.TCP_ACK_TIMEOUT));

        template.setText((String) props.get(TCPSenderProperties.TCP_TEMPLATE));
        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(TCPSenderProperties.CONNECTOR_CHARSET_ENCODING));
        
        if (((String) props.get(TCPSenderProperties.TCP_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            dataTypeBinary.setSelected(true);
            dataTypeBinaryActionPerformed(null);
        }
        else
        {
            dataTypeASCII.setSelected(true);
            dataTypeASCIIActionPerformed(null);
        }

        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");
        
        String selectedChannelName = "None";
        
        for (Channel channel : parent.channels.values())
        {
        	if (((String) props.get(TCPSenderProperties.CHANNEL_ID)).equalsIgnoreCase(channel.getId()))
        		selectedChannelName = channel.getName();
        	
            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        channelNames.setSelectedItem(selectedChannelName);

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        return new TCPSenderProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(TCPSenderProperties.TCP_ADDRESS)).length() <= 3)
        {
            valid = false;
            if (highlight)
            	hostAddressField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(TCPSenderProperties.TCP_PORT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	hostPortField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(TCPSenderProperties.TCP_SERVER_TIMEOUT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	serverTimeoutField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0)
        {
            valid = false;
            if (highlight)
            	reconnectInterval.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(TCPSenderProperties.TCP_BUFFER_SIZE)).length() == 0)
        {
            valid = false;
            if (highlight)
            	bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(TCPSenderProperties.TCP_MAX_RETRY_COUNT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	maximumRetryCountField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(TCPSenderProperties.TCP_TEMPLATE)).length() == 0)
        {
            valid = false;
            if (highlight)
            	template.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(TCPSenderProperties.TCP_ACK_TIMEOUT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	ackTimeoutField.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        hostAddressField.setBackground(null);
        hostPortField.setBackground(null);
        serverTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        maximumRetryCountField.setBackground(null);
        template.setBackground(null);
        ackTimeoutField.setBackground(null);
        reconnectInterval.setBackground(null);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
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
        dataTypeButtonGroup = new javax.swing.ButtonGroup();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        hostPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        serverTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        bufferSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        maximumRetryCountField = new com.webreach.mirth.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        hostAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        ackTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel19 = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
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
        dataTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        dataTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        dataTypeLabel = new javax.swing.JLabel();
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
        usePersistentQueuesYesRadio.setToolTipText("<html>If checked, the connector will store any messages that are unable to be successfully processed in a file-based queue.<br>Messages will be automatically resent until the queue is manually cleared or the message is successfully sent.<br>The default queue location is <Mirth Directory>/.mule/queuestore/<ChannelID>,<br> where <Mirth Directory> is the main Mirth install root and <ChannelID> is the unique id of the current channel.</html>");
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

        reconnectInterval.setToolTipText("<html>The number of milliseconds to wait after closing a connection to the host<br> before opening a new connection, even if there are messages to send.</html>");

        jLabel1.setText("Reconnect Interval (ms):");

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

        rotateMessages.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessages.setText("Rotate Messages in Queue");

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(dataTypeLabel)
                    .add(jLabel18)
                    .add(jLabel17)
                    .add(jLabel16)
                    .add(jLabel15)
                    .add(jLabel13)
                    .add(jLabel8)
                    .add(jLabel36)
                    .add(jLabel19)
                    .add(encodingLabel)
                    .add(URL)
                    .add(jLabel7)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(reconnectInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(rotateMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(ackTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(ignoreACKCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(dataTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dataTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(serverTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, maximumRetryCountField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(hostAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(testConnection))
                    .add(hostPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(template, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel18)
                    .add(testConnection))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel17))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel16)
                    .add(serverTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(reconnectInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel15))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel13)
                    .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(maximumRetryCountField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel36)
                    .add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rotateMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel19)
                    .add(ackTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ignoreACKCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(encodingLabel)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dataTypeLabel)
                    .add(dataTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(dataTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(URL)
                    .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(template, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
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
        if (ignoreACKCheckBox.isSelected())
        {
            ackTimeoutField.setText("0");
            ackTimeoutField.setEnabled(false);
        }
        else
        {
            ackTimeoutField.setEnabled(true);
        }
    }//GEN-LAST:event_ignoreACKCheckBoxActionPerformed

private void usePersistentQueuesNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesNoRadioActionPerformed
rotateMessages.setEnabled(false);
}//GEN-LAST:event_usePersistentQueuesNoRadioActionPerformed

private void usePersistentQueuesYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesYesRadioActionPerformed
rotateMessages.setEnabled(true);
}//GEN-LAST:event_usePersistentQueuesYesRadioActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
parent.setWorking("Testing connection...", true);

    SwingWorker worker = new SwingWorker<Void, Void>() {

        public Void doInBackground() {
            
            try {
                Map<String, String> props = new HashMap<String, String>();
                
                props.put(TCPSenderProperties.TCP_ADDRESS, TCPSender.this.hostAddressField.getText());
                props.put(TCPSenderProperties.TCP_PORT, TCPSender.this.hostPortField.getText());
                props.put(TCPSenderProperties.TCP_SERVER_TIMEOUT, TCPSender.this.serverTimeoutField.getText());
                String response = (String) parent.mirthClient.invokeConnectorService(name, "testConnection", props);

                if (response == null) {
                    throw new ClientException("Failed to invoke service.");
                } else { 
                    parent.alertInformation(parent, response);
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

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_charsetEncodingComboboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private com.webreach.mirth.client.ui.components.MirthTextField ackTimeoutField;
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.webreach.mirth.client.ui.components.MirthComboBox channelNames;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthRadioButton dataTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton dataTypeBinary;
    private javax.swing.ButtonGroup dataTypeButtonGroup;
    private javax.swing.JLabel dataTypeLabel;
    private javax.swing.JLabel encodingLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField hostAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField hostPortField;
    private com.webreach.mirth.client.ui.components.MirthCheckBox ignoreACKCheckBox;
    private javax.swing.JLabel jLabel1;
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
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.webreach.mirth.client.ui.components.MirthTextField maximumRetryCountField;
    private com.webreach.mirth.client.ui.components.MirthTextField reconnectInterval;
    private com.webreach.mirth.client.ui.components.MirthCheckBox rotateMessages;
    private com.webreach.mirth.client.ui.components.MirthTextField serverTimeoutField;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea template;
    private javax.swing.JButton testConnection;
    private javax.swing.ButtonGroup usePersistenceQueuesGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    // End of variables declaration//GEN-END:variables

}
