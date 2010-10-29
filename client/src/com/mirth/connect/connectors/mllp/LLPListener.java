/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mllp;

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
import javax.swing.SwingWorker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Step;
import com.mirth.connect.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class LLPListener extends ConnectorClass {

    /** Creates new form LLPListener */
    public LLPListener() {
        name = LLPListenerProperties.name;
        initComponents();
        reconnectIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        receiveTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(LLPListenerProperties.DATATYPE, name);
        properties.put(LLPListenerProperties.LLP_PROTOCOL_NAME, LLPListenerProperties.LLP_PROTOCOL_NAME_VALUE);

        if (serverRadioButton.isSelected()) {
            properties.put(LLPListenerProperties.LLP_SERVER_MODE, UIConstants.YES_OPTION);
        } else {
            properties.put(LLPListenerProperties.LLP_SERVER_MODE, UIConstants.NO_OPTION);
        }

        properties.put(LLPListenerProperties.LLP_ADDRESS, listenerAddressField.getText());
        properties.put(LLPListenerProperties.LLP_PORT, listenerPortField.getText());
        properties.put(LLPListenerProperties.LLP_RECONNECT_INTERVAL, reconnectIntervalField.getText());
        properties.put(LLPListenerProperties.LLP_RECEIVE_TIMEOUT, receiveTimeoutField.getText());
        properties.put(LLPListenerProperties.LLP_BUFFER_SIZE, bufferSizeField.getText());

        properties.put(LLPListenerProperties.LLP_START_OF_MESSAGE_CHARACTER, startOfMessageCharacterField.getText());
        properties.put(LLPListenerProperties.LLP_END_OF_MESSAGE_CHARACTER, endOfMessageCharacterField.getText());

        if (ascii.isSelected()) {
            properties.put(LLPListenerProperties.LLP_CHAR_ENCODING, "ascii");
        } else {
            properties.put(LLPListenerProperties.LLP_CHAR_ENCODING, "hex");
        }

        if (processBatchYes.isSelected()) {
            properties.put(LLPListenerProperties.LLP_PROCESS_BATCH_FILES, UIConstants.YES_OPTION);
        } else {
            properties.put(LLPListenerProperties.LLP_PROCESS_BATCH_FILES, UIConstants.NO_OPTION);
        }

        properties.put(LLPListenerProperties.LLP_RECORD_SEPARATOR, recordSeparatorField.getText());
        properties.put(LLPListenerProperties.LLP_SEGMENT_END, segmentEnd.getText());

        if (sendACKYes.isSelected()) {
            properties.put(LLPListenerProperties.LLP_SEND_ACK, UIConstants.YES_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER, UIConstants.NO_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_VALUE, "None");
        } else if (sendACKNo.isSelected()) {
            properties.put(LLPListenerProperties.LLP_SEND_ACK, UIConstants.NO_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER, UIConstants.NO_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_VALUE, "None");
        } else if (sendACKTransformer.isSelected()) {
            properties.put(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER, UIConstants.YES_OPTION);
            properties.put(LLPListenerProperties.LLP_SEND_ACK, UIConstants.NO_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_VALUE, (String) responseFromTransformer.getSelectedItem());
        }

        properties.put(LLPListenerProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        properties.put(LLPListenerProperties.LLP_ACKCODE_SUCCESSFUL, successACKCode.getText());
        properties.put(LLPListenerProperties.LLP_ACKMSG_SUCCESSFUL, successACKMessage.getText());
        properties.put(LLPListenerProperties.LLP_ACKCODE_ERROR, errorACKCode.getText());
        properties.put(LLPListenerProperties.LLP_ACKMSG_ERROR, errorACKMessage.getText());
        properties.put(LLPListenerProperties.LLP_ACKCODE_REJECTED, rejectedACKCode.getText());
        properties.put(LLPListenerProperties.LLP_ACKMSG_REJECTED, rejectedACKMessage.getText());

        if (mshAckAcceptYes.isSelected()) {
            properties.put(LLPListenerProperties.LLP_ACK_MSH_15, UIConstants.YES_OPTION);
        } else {
            properties.put(LLPListenerProperties.LLP_ACK_MSH_15, UIConstants.NO_OPTION);
        }

        if (ackOnNewConnectionYes.isSelected()) {
            properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION, UIConstants.YES_OPTION);
        } else {
            properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION, UIConstants.NO_OPTION);
        }

        properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP, ackAddressField.getText());
        properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_PORT, ackPortField.getText());

        if (waitForEndOfMessageCharYes.isSelected()) {
            properties.put(LLPListenerProperties.LLP_WAIT_FOR_END_OF_MESSAGE_CHAR, UIConstants.YES_OPTION);
        } else {
            properties.put(LLPListenerProperties.LLP_WAIT_FOR_END_OF_MESSAGE_CHAR, UIConstants.NO_OPTION);
        }

        if (useStrictLLPYes.isSelected()) {
            properties.put(LLPListenerProperties.LLP_USE_STRICT_LLP, UIConstants.YES_OPTION);
        } else {
            properties.put(LLPListenerProperties.LLP_USE_STRICT_LLP, UIConstants.NO_OPTION);
        }

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        if (((String) props.get(LLPListenerProperties.LLP_SERVER_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            serverRadioButtonActionPerformed(null);
            serverRadioButton.setSelected(true);
        } else {
            clientRadioButtonActionPerformed(null);
            clientRadioButton.setSelected(true);
        }

        listenerAddressField.setText((String) props.get(LLPListenerProperties.LLP_ADDRESS));
        listenerPortField.setText((String) props.get(LLPListenerProperties.LLP_PORT));
        reconnectIntervalField.setText((String) props.get(LLPListenerProperties.LLP_RECONNECT_INTERVAL));
        receiveTimeoutField.setText((String) props.get(LLPListenerProperties.LLP_RECEIVE_TIMEOUT));
        bufferSizeField.setText((String) props.get(LLPListenerProperties.LLP_BUFFER_SIZE));

        if (((String) props.get(LLPListenerProperties.LLP_CHAR_ENCODING)).equals("ascii")) {
            ascii.setSelected(true);
        } else {
            hex.setSelected(true);
        }

        if (((String) props.get(LLPListenerProperties.LLP_PROCESS_BATCH_FILES)).equals(UIConstants.YES_OPTION)) {
            processBatchYes.setSelected(true);
        } else {
            processBatchNo.setSelected(true);
        }

        startOfMessageCharacterField.setText((String) props.get(LLPListenerProperties.LLP_START_OF_MESSAGE_CHARACTER));
        endOfMessageCharacterField.setText((String) props.get(LLPListenerProperties.LLP_END_OF_MESSAGE_CHARACTER));
        recordSeparatorField.setText((String) props.get(LLPListenerProperties.LLP_RECORD_SEPARATOR));
        segmentEnd.setText((String) props.get(LLPListenerProperties.LLP_SEGMENT_END));
        boolean enabled = parent.isSaveEnabled();

        if (((String) props.get(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER)).equals(UIConstants.YES_OPTION)) {
            sendACKTransformerActionPerformed(null);
            sendACKTransformer.setSelected(true);
        } else {
            if (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.YES_OPTION)) {
                sendACKYesActionPerformed(null);
                sendACKYes.setSelected(true);
            } else if (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.NO_OPTION)) {
                sendACKNoActionPerformed(null);
                sendACKNo.setSelected(true);
            }
        }

        updateResponseDropDown();

        if (parent.channelEditPanel.synchronousCheckBox.isSelected()) {
            responseFromTransformer.setSelectedItem((String) props.getProperty(LLPListenerProperties.LLP_RESPONSE_VALUE));
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(LLPListenerProperties.CONNECTOR_CHARSET_ENCODING));

        successACKCode.setText((String) props.get(LLPListenerProperties.LLP_ACKCODE_SUCCESSFUL));
        successACKMessage.setText((String) props.get(LLPListenerProperties.LLP_ACKMSG_SUCCESSFUL));
        errorACKCode.setText((String) props.get(LLPListenerProperties.LLP_ACKCODE_ERROR));
        errorACKMessage.setText((String) props.get(LLPListenerProperties.LLP_ACKMSG_ERROR));
        rejectedACKCode.setText((String) props.get(LLPListenerProperties.LLP_ACKCODE_REJECTED));
        rejectedACKMessage.setText((String) props.get(LLPListenerProperties.LLP_ACKMSG_REJECTED));

        if (((String) props.get(LLPListenerProperties.LLP_ACK_MSH_15)).equals(UIConstants.YES_OPTION)) {
            mshAckAcceptYes.setSelected(true);
        } else {
            mshAckAcceptNo.setSelected(true);
        }

        if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            ackOnNewConnectionYesActionPerformed(null);
            ackOnNewConnectionYes.setSelected(true);
        } else {
            ackOnNewConnectionNoActionPerformed(null);
            ackOnNewConnectionNo.setSelected(true);
        }

        ackAddressField.setText((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP));

        ackPortField.setText((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_PORT));

        if (((String) props.get(LLPListenerProperties.LLP_WAIT_FOR_END_OF_MESSAGE_CHAR)).equals(UIConstants.YES_OPTION)) {
            waitForEndOfMessageCharYes.setSelected(true);
        } else {
            waitForEndOfMessageCharNo.setSelected(true);
        }

        if (((String) props.get(LLPListenerProperties.LLP_USE_STRICT_LLP)).equals(UIConstants.YES_OPTION)) {
            useStrictLLPYesActionPerformed(null);
            useStrictLLPYes.setSelected(true);
        } else {
            useStrictLLPNoActionPerformed(null);
            useStrictLLPNo.setSelected(true);
        }

        parent.setSaveEnabled(enabled);
    }

    public Properties getDefaults() {
        return new LLPListenerProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(LLPListenerProperties.LLP_ADDRESS)).length() == 0) {
            valid = false;
            if (highlight) {
                listenerAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_PORT)).length() == 0) {
            valid = false;
            if (highlight) {
                listenerPortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (clientRadioButton.isSelected() && ((String) props.get(LLPListenerProperties.LLP_RECONNECT_INTERVAL)).length() == 0) {
            valid = false;
            if (highlight) {
                reconnectIntervalField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_RECEIVE_TIMEOUT)).length() == 0) {
            valid = false;
            if (highlight) {
                receiveTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_BUFFER_SIZE)).length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_END_OF_MESSAGE_CHARACTER)).length() == 0) {
            valid = false;
            if (highlight) {
                endOfMessageCharacterField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_START_OF_MESSAGE_CHARACTER)).length() == 0) {
            valid = false;
            if (highlight) {
                startOfMessageCharacterField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_RECORD_SEPARATOR)).length() == 0) {
            valid = false;
            if (highlight) {
                recordSeparatorField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_SEGMENT_END)).length() == 0) {
            valid = false;
            if (highlight) {
                segmentEnd.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.YES_OPTION)) {
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_SUCCESSFUL)).length() == 0) {
                valid = false;
                if (highlight) {
                    successACKCode.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_ERROR)).length() == 0) {
                valid = false;
                if (highlight) {
                    errorACKCode.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_REJECTED)).length() == 0) {
                valid = false;
                if (highlight) {
                    rejectedACKCode.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION)).equals(UIConstants.YES_OPTION) && (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.YES_OPTION) || ((String) props.get(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER)).equals(UIConstants.YES_OPTION))) {
            if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP)).length() == 0) {
                valid = false;
                if (highlight) {
                    ackAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_PORT)).length() == 0) {
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
        reconnectIntervalField.setBackground(null);
        receiveTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        endOfMessageCharacterField.setBackground(null);
        startOfMessageCharacterField.setBackground(null);
        recordSeparatorField.setBackground(null);
        segmentEnd.setBackground(null);
        successACKCode.setBackground(null);
        errorACKCode.setBackground(null);
        rejectedACKCode.setBackground(null);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        serverClientButtonGroup = new javax.swing.ButtonGroup();
        processBatchGroup = new javax.swing.ButtonGroup();
        addressLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        bufferSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        listenerPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        recordSeparatorField = new com.mirth.connect.client.ui.components.MirthTextField();
        startOfMessageCharacterField = new com.mirth.connect.client.ui.components.MirthTextField();
        endOfMessageCharacterField = new com.mirth.connect.client.ui.components.MirthTextField();
        listenerAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        ascii = new com.mirth.connect.client.ui.components.MirthRadioButton();
        hex = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel37 = new javax.swing.JLabel();
        segmentEnd = new com.mirth.connect.client.ui.components.MirthTextField();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        jLabel39 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        sendACKYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        sendACKNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        successACKCodeLabel = new javax.swing.JLabel();
        successACKCode = new com.mirth.connect.client.ui.components.MirthTextField();
        successACKMessage = new com.mirth.connect.client.ui.components.MirthTextField();
        successACKMessageLabel = new javax.swing.JLabel();
        errorACKCode = new com.mirth.connect.client.ui.components.MirthTextField();
        errorACKCodeLabel = new javax.swing.JLabel();
        rejectedACKCode = new com.mirth.connect.client.ui.components.MirthTextField();
        rejectedACKCodeLabel = new javax.swing.JLabel();
        rejectedACKMessageLabel = new javax.swing.JLabel();
        errorACKMessageLabel = new javax.swing.JLabel();
        errorACKMessage = new com.mirth.connect.client.ui.components.MirthTextField();
        rejectedACKMessage = new com.mirth.connect.client.ui.components.MirthTextField();
        mshAckAcceptLabel = new javax.swing.JLabel();
        mshAckAcceptYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        mshAckAcceptNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ackOnNewConnectionLabel = new javax.swing.JLabel();
        ackOnNewConnectionYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ackOnNewConnectionNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ackIPLabel = new javax.swing.JLabel();
        ackPortLabel = new javax.swing.JLabel();
        ackPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        ackAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        sendACKTransformer = new com.mirth.connect.client.ui.components.MirthRadioButton();
        responseFromTransformer = new com.mirth.connect.client.ui.components.MirthComboBox();
        waitForEndOfMessageCharLabel = new javax.swing.JLabel();
        waitForEndOfMessageCharYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        waitForEndOfMessageCharNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel8 = new javax.swing.JLabel();
        useStrictLLPYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useStrictLLPNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        serverRadioButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        clientRadioButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        llpModeLabel = new javax.swing.JLabel();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        reconnectIntervalLabel = new javax.swing.JLabel();
        processBatchYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        processBatchNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel1 = new javax.swing.JLabel();
        testConnection = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        addressLabel.setText("Listener Address:");

        portLabel.setText("Listener Port:");

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size (bytes):");

        jLabel34.setText("Start of Message Char:");

        jLabel35.setText("End of Message Char:");

        jLabel36.setText("Record Separator Char:");

        bufferSizeField.setToolTipText("<html>Use larger values for larger messages, and smaller values <br>for smaller messages. Generally, the default value is fine.</html>");

        receiveTimeoutField.setToolTipText("Enter the time, in milliseconds, to wait without receiving a message before disconnecting.");

        listenerPortField.setToolTipText("Enter the port number to listen for connections on (LLP Mode Server) or to connect to (LLP Mode Client).");

        recordSeparatorField.setToolTipText("Enter the hexadecimal character (byte) value to be received as the record separator character.");

        startOfMessageCharacterField.setToolTipText("Enter the hexadecimal character (byte) value to be received as the start of message character.");

        endOfMessageCharacterField.setToolTipText("Enter the hexadecimal character (byte) value to be received as the end of message character.");

        listenerAddressField.setToolTipText("Enter the domain name or IP address to listen for connections on (LLP Mode Server) or to connect to (LLP Mode Client).");

        jLabel6.setText("LLP Frame Encoding:");

        ascii.setBackground(new java.awt.Color(255, 255, 255));
        ascii.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(ascii);
        ascii.setText("ASCII");
        ascii.setToolTipText("<html>LLP can transmit HL7 messages either unencoded, or encoded in hexadecimal.<br>Select ASCII to expect messages to be unencoded.<br>Select Hex to expect messages to be encoded in hexadecimal.</html>");
        ascii.setMargin(new java.awt.Insets(0, 0, 0, 0));

        hex.setBackground(new java.awt.Color(255, 255, 255));
        hex.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(hex);
        hex.setText("Hex");
        hex.setToolTipText("<html>LLP can transmit HL7 messages either unencoded, or encoded in hexadecimal.<br>Select ASCII to expect messages to be unencoded.<br>Select Hex to expect messages to be encoded in hexadecimal.</html>");
        hex.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel37.setText("End of Segment Char:");

        segmentEnd.setToolTipText("Enter the hexadecimal character (byte) value to be received as the end of segment character.");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding used by the sender of the message,<br> or Default to assume the default character set encoding for the JVM running Mirth.</html>");

        jLabel39.setText("Encoding:");

        jLabel38.setText("Send ACK:");

        sendACKYes.setBackground(new java.awt.Color(255, 255, 255));
        sendACKYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(sendACKYes);
        sendACKYes.setText("Yes");
        sendACKYes.setToolTipText("Select Yes to send an ACK message back for every message received.");
        sendACKYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sendACKYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendACKYesActionPerformed(evt);
            }
        });

        sendACKNo.setBackground(new java.awt.Color(255, 255, 255));
        sendACKNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(sendACKNo);
        sendACKNo.setText("No");
        sendACKNo.setToolTipText("Select No to not send a response.");
        sendACKNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sendACKNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendACKNoActionPerformed(evt);
            }
        });

        successACKCodeLabel.setText("Successful ACK Code:");
        successACKCodeLabel.setToolTipText("<html>If Send Ack Yes is selected,<br> these controls specify the contents of the response sent to valid messages.</html>");

        successACKMessageLabel.setText("Message:");

        errorACKCodeLabel.setText("Error ACK Code:");
        errorACKCodeLabel.setToolTipText("<html>If Send Ack Yes is selected,<br> these controls specify the contents of the response sent to messages which cause processing errors.</html>");

        rejectedACKCodeLabel.setText("Rejected ACK Code:");
        rejectedACKCodeLabel.setToolTipText("<html>If Send Ack Yes is selected,<br> these controls specify the contents of the response sent to messages which are rejected by the source filter.</html>");

        rejectedACKMessageLabel.setText("Message:");

        errorACKMessageLabel.setText("Message:");

        mshAckAcceptLabel.setText("MSH-15 ACK Accept:");

        mshAckAcceptYes.setBackground(new java.awt.Color(255, 255, 255));
        mshAckAcceptYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(mshAckAcceptYes);
        mshAckAcceptYes.setText("Yes");
        mshAckAcceptYes.setToolTipText("<html>This setting determines if Mirth should check the MSH-15 field of an incoming message to control the acknowledgment conditions.<br>The MSH-15 field specifies if a message should be always acknowledged, never acknowledged, or only acknowledged on error.</html>");
        mshAckAcceptYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mshAckAcceptNo.setBackground(new java.awt.Color(255, 255, 255));
        mshAckAcceptNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(mshAckAcceptNo);
        mshAckAcceptNo.setText("No");
        mshAckAcceptNo.setToolTipText("<html>This setting determines if Mirth should check the MSH-15 field of an incoming message to control the acknowledgment conditions.<br>The MSH-15 field specifies if a message should be always acknowledged, never acknowledged, or only acknowledged on error.</html>");
        mshAckAcceptNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        ackOnNewConnectionLabel.setText("ACK on New Connection:");

        ackOnNewConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        ackOnNewConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(ackOnNewConnectionYes);
        ackOnNewConnectionYes.setText("Yes");
        ackOnNewConnectionYes.setToolTipText("Select Yes to create a new network connection to send ACK messages (responses).");
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
        ackOnNewConnectionNo.setToolTipText("Select Yes to create a new network connection to send ACK messages (responses).");
        ackOnNewConnectionNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ackOnNewConnectionNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ackOnNewConnectionNoActionPerformed(evt);
            }
        });

        ackIPLabel.setText("ACK Address:");

        ackPortLabel.setText("ACK Port:");

        ackPortField.setToolTipText("If \"Yes\" is selected for \"ACK on New Connection,\" the port to send the ACK message to.");

        ackAddressField.setToolTipText("<html>If \"Yes\" is selected for \"ACK on New Connection,\"<br> the DNS domain name or IP address of the server to send the ACK message to.</html>");

        sendACKTransformer.setBackground(new java.awt.Color(255, 255, 255));
        sendACKTransformer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(sendACKTransformer);
        sendACKTransformer.setText("Respond from:");
        sendACKTransformer.setToolTipText("<html>Select Respond from to use a destination of this channel<br> or a variable from the Response Map to generate the response to received messages.<br>If the channel is not synchronized, an ACK will be sent back based upon whether or not the source connector<br> successfully received/parsed the message.</html>");
        sendACKTransformer.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sendACKTransformer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendACKTransformerActionPerformed(evt);
            }
        });

        responseFromTransformer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        responseFromTransformer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responseFromTransformerActionPerformed(evt);
            }
        });

        waitForEndOfMessageCharLabel.setText("Wait for End of Message Char:");

        waitForEndOfMessageCharYes.setBackground(new java.awt.Color(255, 255, 255));
        waitForEndOfMessageCharYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(waitForEndOfMessageCharYes);
        waitForEndOfMessageCharYes.setText("Yes");
        waitForEndOfMessageCharYes.setToolTipText("<html>Select Yes to wait for an end of message character to be received.<br>This is useful if messages are split over multiple connections,<br> and pieced together based on when start and end of message characters are received.</html>");
        waitForEndOfMessageCharYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        waitForEndOfMessageCharNo.setBackground(new java.awt.Color(255, 255, 255));
        waitForEndOfMessageCharNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(waitForEndOfMessageCharNo);
        waitForEndOfMessageCharNo.setText("No");
        waitForEndOfMessageCharNo.setToolTipText("<html>Select Yes to wait for an end of message character to be received.<br>This is useful if messages are split over multiple connections,<br> and pieced together based on when start and end of message characters are received.</html>");
        waitForEndOfMessageCharNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel8.setText("Use Strict LLP Validation:");

        useStrictLLPYes.setBackground(new java.awt.Color(255, 255, 255));
        useStrictLLPYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(useStrictLLPYes);
        useStrictLLPYes.setText("Yes");
        useStrictLLPYes.setToolTipText("<html>Select Yes use the standard LLP receiver.<br>Select No to use a less strict receiver that will also allow receiving messages<br> even if they are split over multiple connections based on \"Wait for End of Message Char.\"<br>Generally, Yes should be selected.</html>");
        useStrictLLPYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useStrictLLPYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useStrictLLPYesActionPerformed(evt);
            }
        });

        useStrictLLPNo.setBackground(new java.awt.Color(255, 255, 255));
        useStrictLLPNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(useStrictLLPNo);
        useStrictLLPNo.setText("No");
        useStrictLLPNo.setToolTipText("<html>Select Yes use the standard LLP receiver.<br>Select No to use a less strict receiver that will also allow receiving messages<br> even if they are split over multiple connections based on \"Wait for End of Message Char.\"<br>Generally, Yes should be selected.</html>");
        useStrictLLPNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useStrictLLPNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useStrictLLPNoActionPerformed(evt);
            }
        });

        serverRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        serverRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serverClientButtonGroup.add(serverRadioButton);
        serverRadioButton.setText("Server");
        serverRadioButton.setToolTipText("<html>Select Server to listen for connections from clients.<br>Select Client to connect to an LLP Server.</html>");
        serverRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        serverRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverRadioButtonActionPerformed(evt);
            }
        });

        clientRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        clientRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serverClientButtonGroup.add(clientRadioButton);
        clientRadioButton.setText("Client");
        clientRadioButton.setToolTipText("<html>Select Server to listen for connections from clients.<br>Select Client to connect to an LLP Server.</html>");
        clientRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clientRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientRadioButtonActionPerformed(evt);
            }
        });

        llpModeLabel.setText("LLP Mode:");

        reconnectIntervalField.setToolTipText("<html>If LLP Mode Client is selected, enter the time, in milliseconds,<br> to wait between disconnecting from the LLP server and connecting to it again.</html>");

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        processBatchYes.setBackground(new java.awt.Color(255, 255, 255));
        processBatchYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processBatchGroup.add(processBatchYes);
        processBatchYes.setText("Yes");
        processBatchYes.setToolTipText("<html>Select process batch to allow Mirth to automatically split batched messages into discrete messages.<br>This can be used with batch files containing an FSH or BSH header, or it can be used on batch files that just have multiple MSH segments.<br>The location of each MSH segment signifies the start of a new message to be processed.</html>");
        processBatchYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processBatchNo.setBackground(new java.awt.Color(255, 255, 255));
        processBatchNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processBatchGroup.add(processBatchNo);
        processBatchNo.setText("No");
        processBatchNo.setToolTipText("<html>Select process batch to allow Mirth to automatically split batched messages into discrete messages.<br>This can be used with batch files containing an FSH or BSH header, or it can be used on batch files that just have multiple MSH segments.<br>The location of each MSH segment signifies the start of a new message to be processed.</html>");
        processBatchNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setText("Process Batch:");

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
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel34)
                    .addComponent(jLabel36)
                    .addComponent(jLabel8)
                    .addComponent(waitForEndOfMessageCharLabel)
                    .addComponent(jLabel39)
                    .addComponent(jLabel38)
                    .addComponent(successACKCodeLabel)
                    .addComponent(errorACKCodeLabel)
                    .addComponent(rejectedACKCodeLabel)
                    .addComponent(mshAckAcceptLabel)
                    .addComponent(ackOnNewConnectionLabel)
                    .addComponent(ackIPLabel)
                    .addComponent(ackPortLabel)
                    .addComponent(jLabel3)
                    .addComponent(portLabel)
                    .addComponent(addressLabel)
                    .addComponent(llpModeLabel)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serverRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clientRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(listenerPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listenerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testConnection))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startOfMessageCharacterField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel35)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endOfMessageCharacterField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sendACKYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sendACKNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sendACKTransformer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(responseFromTransformer, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(successACKCode, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(successACKMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(successACKMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(errorACKCode, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(errorACKMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(errorACKMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rejectedACKCode, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rejectedACKMessageLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rejectedACKMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mshAckAcceptYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mshAckAcceptNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ackPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ackAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ackOnNewConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ackOnNewConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(recordSeparatorField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel37)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(segmentEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(waitForEndOfMessageCharYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(useStrictLLPYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(useStrictLLPNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(waitForEndOfMessageCharNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ascii, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(processBatchYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(processBatchNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clientRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(llpModeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listenerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addressLabel)
                    .addComponent(testConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listenerPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reconnectIntervalLabel))
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
                    .addComponent(processBatchYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(processBatchNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ascii, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(startOfMessageCharacterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endOfMessageCharacterField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(recordSeparatorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(segmentEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(useStrictLLPYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useStrictLLPNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(waitForEndOfMessageCharLabel)
                    .addComponent(waitForEndOfMessageCharYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(waitForEndOfMessageCharNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(sendACKYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendACKNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendACKTransformer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseFromTransformer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(successACKCodeLabel)
                    .addComponent(successACKCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(successACKMessageLabel)
                    .addComponent(successACKMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(errorACKCodeLabel)
                    .addComponent(errorACKCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(errorACKMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(errorACKMessageLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rejectedACKCodeLabel)
                    .addComponent(rejectedACKCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rejectedACKMessageLabel)
                    .addComponent(rejectedACKMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mshAckAcceptLabel)
                    .addComponent(mshAckAcceptYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mshAckAcceptNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackOnNewConnectionLabel)
                    .addComponent(ackOnNewConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ackOnNewConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ackIPLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackPortLabel)
                    .addComponent(ackPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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

    private void clientRadioButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_clientRadioButtonActionPerformed
    {// GEN-HEADEREND:event_clientRadioButtonActionPerformed
        addressLabel.setText("Server Address");
        portLabel.setText("Server Port");
        reconnectIntervalField.setEnabled(true);
        reconnectIntervalLabel.setEnabled(true);
        testConnection.setEnabled(true);
    }// GEN-LAST:event_clientRadioButtonActionPerformed

    private void serverRadioButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_serverRadioButtonActionPerformed
    {// GEN-HEADEREND:event_serverRadioButtonActionPerformed
        addressLabel.setText("Listener Address");
        portLabel.setText("Listener Port");
        reconnectIntervalField.setEnabled(false);
        reconnectIntervalLabel.setEnabled(false);
        testConnection.setEnabled(false);
    }// GEN-LAST:event_serverRadioButtonActionPerformed

    private void useStrictLLPNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useStrictLLPNoActionPerformed
    {// GEN-HEADEREND:event_useStrictLLPNoActionPerformed
        waitForEndOfMessageCharLabel.setEnabled(true);
        waitForEndOfMessageCharYes.setEnabled(true);
        waitForEndOfMessageCharNo.setEnabled(true);
    }// GEN-LAST:event_useStrictLLPNoActionPerformed

    private void useStrictLLPYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useStrictLLPYesActionPerformed
    {// GEN-HEADEREND:event_useStrictLLPYesActionPerformed
        waitForEndOfMessageCharLabel.setEnabled(false);
        waitForEndOfMessageCharYes.setEnabled(false);
        waitForEndOfMessageCharNo.setEnabled(false);
        waitForEndOfMessageCharNo.setSelected(true);
    }// GEN-LAST:event_useStrictLLPYesActionPerformed

    private void responseFromTransformerActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_responseFromTransformerActionPerformed
    {// GEN-HEADEREND:event_responseFromTransformerActionPerformed
        if (responseFromTransformer.getSelectedIndex() != 0 && !parent.channelEditPanel.synchronousCheckBox.isSelected()) {
            parent.alertInformation(this, "The synchronize source connector setting has been enabled since it is required to use this feature.");
            parent.channelEditPanel.synchronousCheckBox.setSelected(true);
        }
    }// GEN-LAST:event_responseFromTransformerActionPerformed

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
            responseFromTransformer.setSelectedIndex(0);
        } else {
            responseFromTransformer.setEnabled(true);
        }

        // Reset the proper enabled fields if sendACKNo or sendACKYes were
        // selected.
        if (sendACKYes.isSelected()) {
            sendACKYesActionPerformed(null);
        } else if (sendACKNo.isSelected()) {
            sendACKNoActionPerformed(null);
        }

        parent.setSaveEnabled(enabled);
    }

    private void sendACKTransformerActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_sendACKTransformerActionPerformed
    {// GEN-HEADEREND:event_sendACKTransformerActionPerformed
        successACKCode.setEnabled(false);
        successACKMessage.setEnabled(false);
        errorACKCode.setEnabled(false);
        errorACKMessage.setEnabled(false);
        rejectedACKCode.setEnabled(false);
        rejectedACKMessage.setEnabled(false);

        successACKCodeLabel.setEnabled(false);
        successACKMessageLabel.setEnabled(false);
        errorACKCodeLabel.setEnabled(false);
        errorACKMessageLabel.setEnabled(false);
        rejectedACKCodeLabel.setEnabled(false);
        rejectedACKMessageLabel.setEnabled(false);

        ackOnNewConnectionNo.setEnabled(true);
        ackOnNewConnectionYes.setEnabled(true);
        ackOnNewConnectionLabel.setEnabled(true);
        mshAckAcceptNo.setEnabled(false);
        mshAckAcceptYes.setEnabled(false);
        mshAckAcceptLabel.setEnabled(false);

        if (ackOnNewConnectionYes.isSelected()) {
            ackAddressField.setEnabled(true);
            ackPortField.setEnabled(true);
            ackIPLabel.setEnabled(true);
            ackPortLabel.setEnabled(true);
        }
        responseFromTransformer.setEnabled(true);
        updateResponseDropDown();
    }// GEN-LAST:event_sendACKTransformerActionPerformed

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

    private void sendACKYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_sendACKYesActionPerformed
    {// GEN-HEADEREND:event_sendACKYesActionPerformed

        successACKCode.setEnabled(true);
        successACKMessage.setEnabled(true);

        if (parent.channelEditPanel.synchronousCheckBox.isSelected()) {
            errorACKCode.setEnabled(true);
            errorACKMessage.setEnabled(true);
            rejectedACKCode.setEnabled(true);
            rejectedACKMessage.setEnabled(true);
        } else {
            errorACKCode.setEnabled(false);
            errorACKMessage.setEnabled(false);
            rejectedACKCode.setEnabled(false);
            rejectedACKMessage.setEnabled(false);
        }

        successACKCodeLabel.setEnabled(true);
        successACKMessageLabel.setEnabled(true);

        if (parent.channelEditPanel.synchronousCheckBox.isSelected()) {
            errorACKCodeLabel.setEnabled(true);
            errorACKMessageLabel.setEnabled(true);
            rejectedACKCodeLabel.setEnabled(true);
            rejectedACKMessageLabel.setEnabled(true);
        } else {
            errorACKCodeLabel.setEnabled(false);
            errorACKMessageLabel.setEnabled(false);
            rejectedACKCodeLabel.setEnabled(false);
            rejectedACKMessageLabel.setEnabled(false);
        }

        ackOnNewConnectionNo.setEnabled(true);
        ackOnNewConnectionYes.setEnabled(true);
        ackOnNewConnectionLabel.setEnabled(true);
        mshAckAcceptNo.setEnabled(true);
        mshAckAcceptYes.setEnabled(true);
        mshAckAcceptLabel.setEnabled(true);

        if (ackOnNewConnectionYes.isSelected()) {
            ackAddressField.setEnabled(true);
            ackPortField.setEnabled(true);
            ackIPLabel.setEnabled(true);
            ackPortLabel.setEnabled(true);
        }

        responseFromTransformer.setEnabled(false);
    }// GEN-LAST:event_sendACKYesActionPerformed

    private void sendACKNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_sendACKNoActionPerformed
    {// GEN-HEADEREND:event_sendACKNoActionPerformed
        successACKCode.setEnabled(false);
        successACKMessage.setEnabled(false);
        errorACKCode.setEnabled(false);
        errorACKMessage.setEnabled(false);
        rejectedACKCode.setEnabled(false);
        rejectedACKMessage.setEnabled(false);

        successACKCodeLabel.setEnabled(false);
        successACKMessageLabel.setEnabled(false);
        errorACKCodeLabel.setEnabled(false);
        errorACKMessageLabel.setEnabled(false);
        rejectedACKCodeLabel.setEnabled(false);
        rejectedACKMessageLabel.setEnabled(false);

        ackAddressField.setEnabled(false);
        ackPortField.setEnabled(false);
        ackIPLabel.setEnabled(false);
        ackPortLabel.setEnabled(false);

        ackOnNewConnectionNo.setEnabled(false);
        ackOnNewConnectionYes.setEnabled(false);
        ackOnNewConnectionLabel.setEnabled(false);
        mshAckAcceptNo.setEnabled(false);
        mshAckAcceptYes.setEnabled(false);
        mshAckAcceptLabel.setEnabled(false);

        responseFromTransformer.setEnabled(false);
    }// GEN-LAST:event_sendACKNoActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTextField ackAddressField;
    private javax.swing.JLabel ackIPLabel;
    private javax.swing.JLabel ackOnNewConnectionLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton ackOnNewConnectionNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton ackOnNewConnectionYes;
    private com.mirth.connect.client.ui.components.MirthTextField ackPortField;
    private javax.swing.JLabel ackPortLabel;
    private javax.swing.JLabel addressLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton ascii;
    private com.mirth.connect.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.mirth.connect.client.ui.components.MirthRadioButton clientRadioButton;
    private com.mirth.connect.client.ui.components.MirthTextField endOfMessageCharacterField;
    private com.mirth.connect.client.ui.components.MirthTextField errorACKCode;
    private javax.swing.JLabel errorACKCodeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField errorACKMessage;
    private javax.swing.JLabel errorACKMessageLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton hex;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private com.mirth.connect.client.ui.components.MirthTextField listenerAddressField;
    private com.mirth.connect.client.ui.components.MirthTextField listenerPortField;
    private javax.swing.JLabel llpModeLabel;
    private javax.swing.JLabel mshAckAcceptLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton mshAckAcceptNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton mshAckAcceptYes;
    private javax.swing.JLabel portLabel;
    private javax.swing.ButtonGroup processBatchGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton processBatchNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton processBatchYes;
    private com.mirth.connect.client.ui.components.MirthTextField receiveTimeoutField;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthTextField recordSeparatorField;
    private com.mirth.connect.client.ui.components.MirthTextField rejectedACKCode;
    private javax.swing.JLabel rejectedACKCodeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField rejectedACKMessage;
    private javax.swing.JLabel rejectedACKMessageLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox responseFromTransformer;
    private com.mirth.connect.client.ui.components.MirthTextField segmentEnd;
    private com.mirth.connect.client.ui.components.MirthRadioButton sendACKNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton sendACKTransformer;
    private com.mirth.connect.client.ui.components.MirthRadioButton sendACKYes;
    private javax.swing.ButtonGroup serverClientButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton serverRadioButton;
    private com.mirth.connect.client.ui.components.MirthTextField startOfMessageCharacterField;
    private com.mirth.connect.client.ui.components.MirthTextField successACKCode;
    private javax.swing.JLabel successACKCodeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField successACKMessage;
    private javax.swing.JLabel successACKMessageLabel;
    private javax.swing.JButton testConnection;
    private com.mirth.connect.client.ui.components.MirthRadioButton useStrictLLPNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useStrictLLPYes;
    private javax.swing.JLabel waitForEndOfMessageCharLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton waitForEndOfMessageCharNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton waitForEndOfMessageCharYes;
    // End of variables declaration//GEN-END:variables
}
