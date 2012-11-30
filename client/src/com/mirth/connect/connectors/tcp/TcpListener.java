/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.util.TcpUtil;

public class TcpListener extends ConnectorSettingsPanel implements DocumentListener, ActionListener {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    private String frameEncodingToolTipText;
    private String messageBytesToolTipText;
    private String startOfMessageAbbreviation;
    private String endOfMessageAbbreviation;

    /** Creates new form TcpListener */
    public TcpListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        reconnectIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        receiveTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        maxConnectionsField.setDocument(new MirthFieldConstraints(0, false, false, true));
        startOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, true, true));
        endOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, true, true));
        startOfMessageBytesField.getDocument().addDocumentListener(this);
        endOfMessageBytesField.getDocument().addDocumentListener(this);

        frameEncodingComboBox.addActionListener(this);
        frameEncodingToolTipText = frameEncodingLabel.getToolTipText();
        messageBytesToolTipText = messageDataLabel.getToolTipText();
        startOfMessageAbbreviation = "";
        endOfMessageAbbreviation = "";

        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    @Override
    public String getConnectorName() {
        return new TcpReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        TcpReceiverProperties properties = new TcpReceiverProperties();

        properties.setServerMode(modeServerRadio.isSelected());
        properties.setReconnectInterval(reconnectIntervalField.getText());
        properties.setReceiveTimeout(receiveTimeoutField.getText());
        properties.setBufferSize(bufferSizeField.getText());
        properties.setMaxConnections(maxConnectionsField.getText());
        properties.setKeepConnectionOpen(keepConnectionOpenYesRadio.isSelected());
        properties.setStartOfMessageBytes(startOfMessageBytesField.getText());
        properties.setEndOfMessageBytes(endOfMessageBytesField.getText());
        properties.setProcessBatch(processBatchYesRadio.isSelected());
        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        properties.setDataTypeBinary(dataTypeBinaryRadio.isSelected());
        
        if (respondOnNewConnectionYesRadio.isSelected()) {
        	properties.setRespondOnNewConnection(TcpReceiverProperties.NEW_CONNECTION);
        } else if (respondOnNewConnectionNoRadio.isSelected()) {
        	properties.setRespondOnNewConnection(TcpReceiverProperties.SAME_CONNECTION);
        } else if (respondOnNewConnectionRecoveryRadio.isSelected()) {
        	properties.setRespondOnNewConnection(TcpReceiverProperties.NEW_CONNECTION_ON_RECOVERY);
        }
        properties.setResponseAddress(responseAddressField.getText());
        properties.setResponsePort(responsePortField.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        TcpReceiverProperties props = (TcpReceiverProperties) properties;

        if (props.isServerMode()) {
            modeServerRadio.setSelected(true);
            modeServerRadioActionPerformed(null);
        } else {
            modeClientRadio.setSelected(true);
            modeClientRadioActionPerformed(null);
        }

        reconnectIntervalField.setText(props.getReconnectInterval());
        receiveTimeoutField.setText(props.getReceiveTimeout());
        bufferSizeField.setText(props.getBufferSize());
        maxConnectionsField.setText(props.getMaxConnections());

        if (props.isKeepConnectionOpen()) {
            keepConnectionOpenYesRadio.setSelected(true);
        } else {
            keepConnectionOpenNoRadio.setSelected(true);
        }

        startOfMessageBytesField.setText(props.getStartOfMessageBytes());
        endOfMessageBytesField.setText(props.getEndOfMessageBytes());

        frameEncodingComboBox.removeActionListener(this);
        if (props.getStartOfMessageBytes().equals(TcpUtil.DEFAULT_LLP_START_BYTES) && props.getEndOfMessageBytes().equals(TcpUtil.DEFAULT_LLP_END_BYTES)) {
            frameEncodingComboBox.setSelectedItem("MLLP");
        } else if (props.getStartOfMessageBytes().equals(TcpUtil.DEFAULT_ASTM_START_BYTES) && props.getEndOfMessageBytes().equals(TcpUtil.DEFAULT_ASTM_END_BYTES)) {
            frameEncodingComboBox.setSelectedItem("ASTM");
        } else {
            frameEncodingComboBox.setSelectedItem("Custom");
        }
        frameEncodingComboBox.addActionListener(this);

        if (props.isProcessBatch()) {
            processBatchYesRadio.setSelected(true);
        } else {
            processBatchNoRadio.setSelected(true);
        }

        if (props.isDataTypeBinary()) {
            dataTypeBinaryRadio.setSelected(true);
            dataTypeBinaryRadioActionPerformed(null);
        } else {
            dataTypeASCIIRadio.setSelected(true);
            dataTypeASCIIRadioActionPerformed(null);
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharsetEncoding());

        switch (props.getRespondOnNewConnection()) {
        	case TcpReceiverProperties.NEW_CONNECTION: 
	            respondOnNewConnectionYesRadio.setSelected(true);
	            respondOnNewConnectionYesRadioActionPerformed(null);
	            break;
        	case TcpReceiverProperties.SAME_CONNECTION:
	            respondOnNewConnectionNoRadio.setSelected(true);
	            respondOnNewConnectionNoRadioActionPerformed(null);
	            break;
        	case TcpReceiverProperties.NEW_CONNECTION_ON_RECOVERY:
	            respondOnNewConnectionRecoveryRadio.setSelected(true);
	            respondOnNewConnectionRecoveryRadioActionPerformed(null);
	            break;
        }

        responseAddressField.setText(props.getResponseAddress());
        responsePortField.setText(props.getResponsePort());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new TcpReceiverProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        TcpReceiverProperties props = (TcpReceiverProperties) properties;

        boolean valid = true;

        if (!props.isServerMode()) {
            if (props.getReconnectInterval().length() == 0) {
                valid = false;
                if (highlight) {
                    reconnectIntervalField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        if (props.getReceiveTimeout().length() == 0) {
            valid = false;
            if (highlight) {
                receiveTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getBufferSize().length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getMaxConnections().length() == 0) {
            valid = false;
            if (highlight) {
                maxConnectionsField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (!TcpUtil.isValidHexString(props.getStartOfMessageBytes())) {
            valid = false;
            if (highlight) {
                startOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (!TcpUtil.isValidHexString(props.getEndOfMessageBytes())) {
            valid = false;
            if (highlight) {
                endOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION || props.getRespondOnNewConnection() == TcpReceiverProperties.NEW_CONNECTION_ON_RECOVERY) {
            if (props.getResponseAddress().length() <= 3) {
                valid = false;
                if (highlight) {
                    responseAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (props.getResponsePort().length() == 0) {
                valid = false;
                if (highlight) {
                    responsePortField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        reconnectIntervalField.setBackground(null);
        receiveTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        maxConnectionsField.setBackground(null);
        startOfMessageBytesField.setBackground(null);
        endOfMessageBytesField.setBackground(null);
        responseAddressField.setBackground(null);
        responsePortField.setBackground(null);
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    private void changeAbbreviation(DocumentEvent evt) {
        String text = "";
        String switchTo = "Custom";

        try {
            text = evt.getDocument().getText(0, evt.getDocument().getLength()).trim();
        } catch (BadLocationException e) {
        }

        if (evt.getDocument().equals(startOfMessageBytesField.getDocument())) {
            startOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(text);

            if (frameEncodingComboBox.getSelectedItem().equals("MLLP") && text.equals(TcpUtil.DEFAULT_LLP_START_BYTES) && endOfMessageBytesField.getText().equals(TcpUtil.DEFAULT_LLP_END_BYTES)) {
                switchTo = "MLLP";
            } else if (frameEncodingComboBox.getSelectedItem().equals("ASTM") && text.equals(TcpUtil.DEFAULT_ASTM_START_BYTES) && endOfMessageBytesField.getText().equals(TcpUtil.DEFAULT_ASTM_END_BYTES)) {
                switchTo = "ASTM";
            }
        } else {
            endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(text);

            if (frameEncodingComboBox.getSelectedItem().equals("MLLP") && startOfMessageBytesField.getText().equals(TcpUtil.DEFAULT_LLP_START_BYTES) && text.equals(TcpUtil.DEFAULT_LLP_END_BYTES)) {
                switchTo = "MLLP";
            } else if (frameEncodingComboBox.getSelectedItem().equals("ASTM") && startOfMessageBytesField.getText().equals(TcpUtil.DEFAULT_ASTM_START_BYTES) && text.equals(TcpUtil.DEFAULT_ASTM_END_BYTES)) {
                switchTo = "ASTM";
            }
        }

        frameEncodingComboBox.removeActionListener(this);
        frameEncodingComboBox.setSelectedItem(switchTo);
        frameEncodingComboBox.addActionListener(this);

        changeAbbreviation();
    }

    private void changeAbbreviation() {
        String startReplaced = startOfMessageAbbreviation.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
        String endReplaced = endOfMessageAbbreviation.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
        String newFrameEncodingToolTipText = frameEncodingToolTipText.replace("SOM", startReplaced).replace("EOM", endReplaced);
        String newMessageBytesToolTipText = messageBytesToolTipText.replace("SOM", startReplaced).replace("EOM", endReplaced);

        frameEncodingLabel.setToolTipText(newFrameEncodingToolTipText);
        frameEncodingComboBox.setToolTipText(newFrameEncodingToolTipText);
        startOfMessageBytes0XLabel.setToolTipText(newMessageBytesToolTipText);
        startOfMessageBytesField.setToolTipText(newMessageBytesToolTipText);
        messageDataLabel.setToolTipText(newMessageBytesToolTipText);
        endOfMessageBytes0XLabel.setToolTipText(newMessageBytesToolTipText);
        endOfMessageBytesField.setToolTipText(newMessageBytesToolTipText);
        sampleMessageLabel.setText(("<html><b>" + startReplaced + "</b> <i>&lt;Message Data&gt;</i> <b>" + endReplaced + "</b></html>").trim());
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource().equals(frameEncodingComboBox)) {
            startOfMessageBytesField.getDocument().removeDocumentListener(this);
            endOfMessageBytesField.getDocument().removeDocumentListener(this);

            if (frameEncodingComboBox.getSelectedItem().equals("MLLP")) {
                startOfMessageBytesField.setText(TcpUtil.DEFAULT_LLP_START_BYTES);
                endOfMessageBytesField.setText(TcpUtil.DEFAULT_LLP_END_BYTES);
            } else if (frameEncodingComboBox.getSelectedItem().equals("ASTM")) {
                startOfMessageBytesField.setText(TcpUtil.DEFAULT_ASTM_START_BYTES);
                endOfMessageBytesField.setText(TcpUtil.DEFAULT_ASTM_END_BYTES);
            } else {
                startOfMessageBytesField.setText("");
                endOfMessageBytesField.setText("");
            }

            startOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(startOfMessageBytesField.getText());
            endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(endOfMessageBytesField.getText());
            changeAbbreviation();

            startOfMessageBytesField.getDocument().addDocumentListener(this);
            endOfMessageBytesField.getDocument().addDocumentListener(this);
        }
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
        dataTypeButtonGroup = new javax.swing.ButtonGroup();
        respondOnNewConnectionButtonGroup = new javax.swing.ButtonGroup();
        modeButtonGroup = new javax.swing.ButtonGroup();
        processBatchButtonGroup = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        bufferSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        ackOnNewConnectionLabel = new javax.swing.JLabel();
        respondOnNewConnectionYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        respondOnNewConnectionNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        respondOnNewConnectionRecoveryRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ackIPLabel = new javax.swing.JLabel();
        ackPortLabel = new javax.swing.JLabel();
        responsePortField = new com.mirth.connect.client.ui.components.MirthTextField();
        responseAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        dataTypeASCIIRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeBinaryRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeLabel = new javax.swing.JLabel();
        keepConnectionOpenLabel = new javax.swing.JLabel();
        keepConnectionOpenYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        startOfMessageBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        endOfMessageBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        startOfMessageBytes0XLabel = new javax.swing.JLabel();
        endOfMessageBytes0XLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        modeServerRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        modeClientRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        reconnectIntervalLabel = new javax.swing.JLabel();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        maxConnectionsLabel = new javax.swing.JLabel();
        maxConnectionsField = new com.mirth.connect.client.ui.components.MirthTextField();
        processBatchLabel = new javax.swing.JLabel();
        processBatchYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        processBatchNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        frameEncodingComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        frameEncodingLabel = new javax.swing.JLabel();
        messageDataLabel = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        sampleMessageLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel3.setText("Receive Timeout (ms):");
        jLabel3.setToolTipText("The amount of time, in milliseconds, to wait without receiving a message before closing a connection.");

        jLabel4.setText("Buffer Size (bytes):");
        jLabel4.setToolTipText("<html>Use larger values for larger messages, and smaller values <br>for smaller messages. Generally, the default value is fine.</html>");

        bufferSizeField.setToolTipText("<html>Use larger values for larger messages, and smaller values <br>for smaller messages. Generally, the default value is fine.</html>");

        receiveTimeoutField.setToolTipText("The amount of time, in milliseconds, to wait without receiving a message before closing a connection.");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding used by the message sender,<br/>or Select Default to use the default character set encoding for the JVM running Mirth.</html>");

        encodingLabel.setText("Encoding:");
        encodingLabel.setToolTipText("<html>Select the character set encoding used by the message sender,<br/>or Select Default to use the default character set encoding for the JVM running Mirth.</html>");

        ackOnNewConnectionLabel.setText("Respond on New Connection:");
        ackOnNewConnectionLabel.setToolTipText("<html>Select No to send the message response on the same connection as the inbound message was received on.<br/>Select Yes to send the response on a new connection.</html>");

        respondOnNewConnectionYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        respondOnNewConnectionYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        respondOnNewConnectionButtonGroup.add(respondOnNewConnectionYesRadio);
        respondOnNewConnectionYesRadio.setText("Yes");
        respondOnNewConnectionYesRadio.setToolTipText("<html>Select No to send the message response on the same connection as the inbound message was received on (Responses will not be sent during message recovery).<br/>Select Yes to send the response on a new connection (during normal processing as well as recovery).<br/>Select Message Recovery to send the message response on the same connection during normal processing, and on a new connection during message recovery.</html>");
        respondOnNewConnectionYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        respondOnNewConnectionYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                respondOnNewConnectionYesRadioActionPerformed(evt);
            }
        });

        respondOnNewConnectionNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        respondOnNewConnectionNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        respondOnNewConnectionButtonGroup.add(respondOnNewConnectionNoRadio);
        respondOnNewConnectionNoRadio.setText("No");
        respondOnNewConnectionNoRadio.setToolTipText("<html>Select No to send the message response on the same connection as the inbound message was received on (Responses will not be sent during message recovery).<br/>Select Yes to send the response on a new connection (during normal processing as well as recovery).<br/>Select Message Recovery to send the message response on the same connection during normal processing, and on a new connection during message recovery.</html>");
        respondOnNewConnectionNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        respondOnNewConnectionNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                respondOnNewConnectionNoRadioActionPerformed(evt);
            }
        });

        respondOnNewConnectionRecoveryRadio.setBackground(new java.awt.Color(255, 255, 255));
        respondOnNewConnectionRecoveryRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        respondOnNewConnectionButtonGroup.add(respondOnNewConnectionRecoveryRadio);
        respondOnNewConnectionRecoveryRadio.setText("Message Recovery");
        respondOnNewConnectionRecoveryRadio.setToolTipText("<html>Select No to send the message response on the same connection as the inbound message was received on (Responses will not be sent during message recovery).<br/>Select Yes to send the response on a new connection (during normal processing as well as recovery).<br/>Select Message Recovery to send the message response on the same connection during normal processing, and on a new connection during message recovery.</html>");
        respondOnNewConnectionRecoveryRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        respondOnNewConnectionRecoveryRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                respondOnNewConnectionRecoveryRadioActionPerformed(evt);
            }
        });

        ackIPLabel.setText("Response Address:");
        ackIPLabel.setToolTipText("<html>Enter the DNS domain name or IP address to send message responses to.</html>");

        ackPortLabel.setText("Response Port:");
        ackPortLabel.setToolTipText("<html>Enter the port to send message responses to.</html>");

        responsePortField.setToolTipText("<html>Enter the port to send message responses to.</html>");

        responseAddressField.setToolTipText("<html>Enter the DNS domain name or IP address to send message responses to.</html>");

        dataTypeASCIIRadio.setBackground(new java.awt.Color(255, 255, 255));
        dataTypeASCIIRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataTypeButtonGroup.add(dataTypeASCIIRadio);
        dataTypeASCIIRadio.setSelected(true);
        dataTypeASCIIRadio.setText("ASCII");
        dataTypeASCIIRadio.setToolTipText("<html>Select Binary if the inbound messages are raw byte streams; the payload will be Base64 encoded.<br>Select ASCII if the inbound messages are text streams; the payload will be encoded with the specified charset.</html>");
        dataTypeASCIIRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dataTypeASCIIRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeASCIIRadioActionPerformed(evt);
            }
        });

        dataTypeBinaryRadio.setBackground(new java.awt.Color(255, 255, 255));
        dataTypeBinaryRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataTypeButtonGroup.add(dataTypeBinaryRadio);
        dataTypeBinaryRadio.setText("Binary");
        dataTypeBinaryRadio.setToolTipText("<html>Select Binary if the inbound messages are raw byte streams; the payload will be Base64 encoded.<br>Select ASCII if the inbound messages are text streams; the payload will be encoded with the specified charset.</html>");
        dataTypeBinaryRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dataTypeBinaryRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeBinaryRadioActionPerformed(evt);
            }
        });

        dataTypeLabel.setText("Data Type:");
        dataTypeLabel.setToolTipText("<html>Select Binary if the inbound messages are raw byte streams; the payload will be Base64 encoded.<br>Select ASCII if the inbound messages are text streams; the payload will be encoded with the specified charset.</html>");

        keepConnectionOpenLabel.setText("Keep Connection Open:");
        keepConnectionOpenLabel.setToolTipText("<html>Select No to close the listening socket after each message is received and the response (if selected) is sent.<br/>Select Yes to always keep the socket open unless the sending system closes it.<br/>If Yes is selected, messages will only be processed if data is received and either the receive timeout is reached,<br/>the sending system closes the socket, or an end of message byte sequence has been detected.<br/>The sending system will also need to use a timeout or delimiter method of processing responses.</html>");

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setToolTipText("<html>Select No to close the listening socket after each message is received and the response (if selected) is sent.<br/>Select Yes to always keep the socket open unless the sending system closes it.<br/>If Yes is selected, messages will only be processed if data is received and either the receive timeout is reached,<br/>the sending system closes the socket, or an end of message byte sequence has been detected.<br/>The sending system will also need to use a timeout or delimiter method of processing responses.</html>");
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setSelected(true);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setToolTipText("<html>Select No to close the listening socket after each message is received and the response (if selected) is sent.<br/>Select Yes to always keep the socket open unless the sending system closes it.<br/>If Yes is selected, messages will only be processed if data is received and either the receive timeout is reached,<br/>the sending system closes the socket, or an end of message byte sequence has been detected.<br/>The sending system will also need to use a timeout or delimiter method of processing responses.</html>");
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        startOfMessageBytesField.setToolTipText("<html>Enter the bytes expected to come before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        startOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startOfMessageBytesFieldActionPerformed(evt);
            }
        });

        endOfMessageBytesField.setToolTipText("<html>Enter the bytes expected to come before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        endOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endOfMessageBytesFieldActionPerformed(evt);
            }
        });

        startOfMessageBytes0XLabel.setText("0x");
        startOfMessageBytes0XLabel.setToolTipText("<html>Enter the bytes expected to come before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");

        endOfMessageBytes0XLabel.setText("0x");
        endOfMessageBytes0XLabel.setToolTipText("<html>Enter the bytes expected to come before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");

        jLabel5.setText("Mode:");
        jLabel5.setToolTipText("<html>Select Server to listen for connections from clients.<br/>Select Client to connect to a TCP Server.</html>");

        modeServerRadio.setBackground(new java.awt.Color(255, 255, 255));
        modeServerRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modeButtonGroup.add(modeServerRadio);
        modeServerRadio.setText("Server");
        modeServerRadio.setToolTipText("<html>Select Server to listen for connections from clients.<br/>Select Client to connect to a TCP Server.</html>");
        modeServerRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modeServerRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modeServerRadioActionPerformed(evt);
            }
        });

        modeClientRadio.setBackground(new java.awt.Color(255, 255, 255));
        modeClientRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modeButtonGroup.add(modeClientRadio);
        modeClientRadio.setText("Client");
        modeClientRadio.setToolTipText("<html>Select Server to listen for connections from clients.<br/>Select Client to connect to a TCP Server.</html>");
        modeClientRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        modeClientRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modeClientRadioActionPerformed(evt);
            }
        });

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");
        reconnectIntervalLabel.setToolTipText("<html>If Client mode is selected, enter the time (in milliseconds) to wait<br/>between disconnecting from the TCP server and connecting to it again.</html>");

        reconnectIntervalField.setToolTipText("<html>If Client mode is selected, enter the time (in milliseconds) to wait<br/>between disconnecting from the TCP server and connecting to it again.</html>");

        maxConnectionsLabel.setText("Max Connections:");
        maxConnectionsLabel.setToolTipText("<html>The maximum number of client connections to accept.<br/>After this number has been reached, subsequent socket requests will result in a rejection.</html>");

        maxConnectionsField.setToolTipText("<html>The maximum number of client connections to accept.<br/>After this number has been reached, subsequent socket requests will result in a rejection.</html>");

        processBatchLabel.setText("Process Batch:");
        processBatchLabel.setToolTipText("<html>Select Yes to allow Mirth to automatically split batched HL7 v2.x messages into discrete messages.<br/>This can be used with batch files containing FHS/BHS/BTS/FTS segments, or it can be used on batch files that just have multiple MSH segments.<br/>The location of each MSH segment signifies the start of a new message to be processed.</html>");

        processBatchYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        processBatchYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processBatchButtonGroup.add(processBatchYesRadio);
        processBatchYesRadio.setText("Yes");
        processBatchYesRadio.setToolTipText("<html>Select Yes to allow Mirth to automatically split batched HL7 v2.x messages into discrete messages.<br/>This can be used with batch files containing FHS/BHS/BTS/FTS segments, or it can be used on batch files that just have multiple MSH segments.<br/>The location of each MSH segment signifies the start of a new message to be processed.</html>");
        processBatchYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processBatchNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        processBatchNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processBatchButtonGroup.add(processBatchNoRadio);
        processBatchNoRadio.setSelected(true);
        processBatchNoRadio.setText("No");
        processBatchNoRadio.setToolTipText("<html>Select Yes to allow Mirth to automatically split batched HL7 v2.x messages into discrete messages.<br/>This can be used with batch files containing FHS/BHS/BTS/FTS segments, or it can be used on batch files that just have multiple MSH segments.<br/>The location of each MSH segment signifies the start of a new message to be processed.</html>");
        processBatchNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        processBatchNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processBatchNoRadioActionPerformed(evt);
            }
        });

        frameEncodingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "MLLP", "ASTM", "Custom" }));
        frameEncodingComboBox.setToolTipText("<html>Select MLLP to use the default frame encoding characters as per the MLLPv2 specifications.<br/>Select ASTM to use the default frame encoding characters as per the ASTM 1381 specifications.<br/>Select Custom to enter user-defined frame encoding characters.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        frameEncodingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frameEncodingComboBoxActionPerformed(evt);
            }
        });

        frameEncodingLabel.setText("Frame Encoding:");
        frameEncodingLabel.setToolTipText("<html>Select MLLP to use the default frame encoding characters as per the MLLPv2 specifications.<br/>Select ASTM to use the default frame encoding characters as per the ASTM 1381 specifications.<br/>Select Custom to enter user-defined frame encoding characters.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");

        messageDataLabel.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        messageDataLabel.setText("<Message Data>");
        messageDataLabel.setToolTipText("<html>Enter the bytes expected to come before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        messageDataLabel.setEnabled(false);

        jLabel19.setText("Sample Frame:");

        sampleMessageLabel.setForeground(new java.awt.Color(153, 153, 153));
        sampleMessageLabel.setText("<html><b>&lt;VT&gt;</b> <i>&lt;Message Data&gt;</i> <b>&lt;FS&gt;&lt;CR&gt;</b></html>");
        sampleMessageLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19)
                    .addComponent(encodingLabel)
                    .addComponent(dataTypeLabel)
                    .addComponent(processBatchLabel)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(maxConnectionsLabel)
                    .addComponent(keepConnectionOpenLabel)
                    .addComponent(frameEncodingLabel)
                    .addComponent(ackOnNewConnectionLabel)
                    .addComponent(ackIPLabel)
                    .addComponent(ackPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(frameEncodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(startOfMessageBytes0XLabel)
                                .addGap(4, 4, 4)
                                .addComponent(startOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(messageDataLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(endOfMessageBytes0XLabel)
                                .addGap(4, 4, 4)
                                .addComponent(endOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(modeServerRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modeClientRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxConnectionsField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(processBatchYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(processBatchNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(dataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dataTypeASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(respondOnNewConnectionYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(respondOnNewConnectionNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(respondOnNewConnectionRecoveryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(responseAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(responsePortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(sampleMessageLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(frameEncodingLabel)
                    .addComponent(frameEncodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageDataLabel)
                    .addComponent(endOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startOfMessageBytes0XLabel)
                    .addComponent(endOfMessageBytes0XLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(sampleMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(modeServerRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modeClientRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxConnectionsLabel)
                    .addComponent(maxConnectionsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(keepConnectionOpenLabel)
                    .addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(processBatchLabel)
                    .addComponent(processBatchYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(processBatchNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataTypeLabel)
                    .addComponent(dataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataTypeASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encodingLabel)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackOnNewConnectionLabel)
                    .addComponent(respondOnNewConnectionYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(respondOnNewConnectionNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(respondOnNewConnectionRecoveryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackIPLabel)
                    .addComponent(responseAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackPortLabel)
                    .addComponent(responsePortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dataTypeBinaryRadioActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataTypeBinaryRadioActionPerformed
    {//GEN-HEADEREND:event_dataTypeBinaryRadioActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }//GEN-LAST:event_dataTypeBinaryRadioActionPerformed

    private void dataTypeASCIIRadioActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dataTypeASCIIRadioActionPerformed
    {//GEN-HEADEREND:event_dataTypeASCIIRadioActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }//GEN-LAST:event_dataTypeASCIIRadioActionPerformed

    private void startOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_startOfMessageBytesFieldActionPerformed

    private void endOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_endOfMessageBytesFieldActionPerformed

    private void modeClientRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeClientRadioActionPerformed
        reconnectIntervalLabel.setEnabled(true);
        reconnectIntervalField.setEnabled(true);
        maxConnectionsLabel.setEnabled(false);
        maxConnectionsField.setEnabled(false);
    }//GEN-LAST:event_modeClientRadioActionPerformed

    private void processBatchNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processBatchNoRadioActionPerformed
    }//GEN-LAST:event_processBatchNoRadioActionPerformed

    private void modeServerRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeServerRadioActionPerformed
        reconnectIntervalLabel.setEnabled(false);
        reconnectIntervalField.setEnabled(false);
        maxConnectionsLabel.setEnabled(true);
        maxConnectionsField.setEnabled(true);
    }//GEN-LAST:event_modeServerRadioActionPerformed

    private void frameEncodingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frameEncodingComboBoxActionPerformed
    }//GEN-LAST:event_frameEncodingComboBoxActionPerformed

    private void respondOnNewConnectionRecoveryRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_respondOnNewConnectionRecoveryRadioActionPerformed
    	responseAddressField.setEnabled(true);
        responsePortField.setEnabled(true);
        ackIPLabel.setEnabled(true);
        ackPortLabel.setEnabled(true);
    }//GEN-LAST:event_respondOnNewConnectionRecoveryRadioActionPerformed

    private void respondOnNewConnectionNoRadioActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionNoActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionNoActionPerformed
        responseAddressField.setEnabled(false);
        responsePortField.setEnabled(false);
        ackIPLabel.setEnabled(false);
        ackPortLabel.setEnabled(false);
    }// GEN-LAST:event_ackOnNewConnectionNoActionPerformed

    private void respondOnNewConnectionYesRadioActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionYesActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionYesActionPerformed
        responseAddressField.setEnabled(true);
        responsePortField.setEnabled(true);
        ackIPLabel.setEnabled(true);
        ackPortLabel.setEnabled(true);
    }// GEN-LAST:event_ackOnNewConnectionYesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ackIPLabel;
    private javax.swing.JLabel ackOnNewConnectionLabel;
    private javax.swing.JLabel ackPortLabel;
    private com.mirth.connect.client.ui.components.MirthTextField bufferSizeField;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeASCIIRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeBinaryRadio;
    private javax.swing.ButtonGroup dataTypeButtonGroup;
    private javax.swing.JLabel dataTypeLabel;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JLabel endOfMessageBytes0XLabel;
    private com.mirth.connect.client.ui.components.MirthTextField endOfMessageBytesField;
    private com.mirth.connect.client.ui.components.MirthComboBox frameEncodingComboBox;
    private javax.swing.JLabel frameEncodingLabel;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private javax.swing.JLabel keepConnectionOpenLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField maxConnectionsField;
    private javax.swing.JLabel maxConnectionsLabel;
    private javax.swing.JLabel messageDataLabel;
    private javax.swing.ButtonGroup modeButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton modeClientRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton modeServerRadio;
    private javax.swing.ButtonGroup processBatchButtonGroup;
    private javax.swing.JLabel processBatchLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton processBatchNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton processBatchYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField receiveTimeoutField;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private javax.swing.ButtonGroup respondOnNewConnectionButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton respondOnNewConnectionNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton respondOnNewConnectionRecoveryRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton respondOnNewConnectionYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField responseAddressField;
    private com.mirth.connect.client.ui.components.MirthTextField responsePortField;
    private javax.swing.JLabel sampleMessageLabel;
    private javax.swing.JLabel startOfMessageBytes0XLabel;
    private com.mirth.connect.client.ui.components.MirthTextField startOfMessageBytesField;
    // End of variables declaration//GEN-END:variables
}
