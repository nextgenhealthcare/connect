/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class TCPSender extends ConnectorSettingsPanel {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    public TCPSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        sendTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        reconnectIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        maximumRetryCountField.setDocument(new MirthFieldConstraints(2, false, false, true));
        // ast: Acktimeout constrain
        responseTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    @Override
    public String getConnectorName() {
        return new TcpDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        TcpDispatcherProperties properties = new TcpDispatcherProperties();

        properties.setHost(hostAddressField.getText());
        properties.setPort(hostPortField.getText());
        properties.setSendTimeout(sendTimeoutField.getText());
        properties.setBufferSize(bufferSizeField.getText());
        properties.setKeepConnectionOpen(keepConnectionOpenYesRadio.isSelected());
        properties.setFrameEncodingIsHex(frameEncodingHexRadio.isSelected());
        properties.setBeginBytes(beginBytesField.getText());
        properties.setEndBytes(endBytesField.getText());
        properties.setMaxRetryCount(maximumRetryCountField.getText());
        properties.setReconnectInterval(reconnectIntervalField.getText());
        properties.setResponseTimeout(responseTimeoutField.getText());
        properties.setIgnoreResponse(ignoreACKCheckBox.isSelected());
        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        properties.setDataTypeIsBase64(dataTypeBinary.isSelected());
        properties.setTemplate(template.getText());

        logger.debug("getProperties: properties=" + properties);

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        logger.debug("setProperties: properties=" + properties);
        TcpDispatcherProperties props = (TcpDispatcherProperties) properties;

        hostAddressField.setText(props.getHost());
        hostPortField.setText(props.getPort());
        sendTimeoutField.setText(props.getSendTimeout());
        bufferSizeField.setText(props.getBufferSize());

        if (props.isKeepConnectionOpen()) {
            keepConnectionOpenYesRadio.setSelected(true);
        } else {
            keepConnectionOpenNoRadio.setSelected(true);
        }

        if (props.isFrameEncodingHex()) {
            frameEncodingHexRadio.setSelected(true);
        } else {
            frameEncodingASCIIRadio.setSelected(true);
        }

        beginBytesField.setText(props.getBeginBytes());
        endBytesField.setText(props.getEndBytes());

        maximumRetryCountField.setText(props.getMaxRetryCount());
        reconnectIntervalField.setText(props.getReconnectInterval());

        ignoreACKCheckBox.setSelected(props.isIgnoreResponse());
        ignoreACKCheckBoxActionPerformed(null);

        responseTimeoutField.setText(String.valueOf(props.getResponseTimeout()));

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharsetEncoding());

        if (props.isDataTypeBase64()) {
            dataTypeBinary.setSelected(true);
            dataTypeBinaryActionPerformed(null);
        } else {
            dataTypeASCII.setSelected(true);
            dataTypeASCIIActionPerformed(null);
        }

        template.setText(props.getTemplate());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new TcpDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        logger.debug("checkProperties: properties=" + properties);
        TcpDispatcherProperties props = (TcpDispatcherProperties) properties;

        boolean valid = true;

        if (props.getHost().length() <= 3) {
            valid = false;
            if (highlight) {
                hostAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getPort().length() == 0) {
            valid = false;
            if (highlight) {
                hostPortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getSendTimeout().length() == 0) {
            valid = false;
            if (highlight) {
                sendTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.isFrameEncodingHex()) {
            if (props.getBeginBytes().length() > 0) {
                if (!isValidHexString(props.getBeginBytes())) {
                    valid = false;
                    if (highlight) {
                        beginBytesField.setBackground(UIConstants.INVALID_COLOR);
                    }
                }
            }
            if (props.getEndBytes().length() > 0) {
                if (!isValidHexString(props.getEndBytes())) {
                    valid = false;
                    if (highlight) {
                        endBytesField.setBackground(UIConstants.INVALID_COLOR);
                    }
                }
            }
        }
        if (props.getReconnectInterval().length() == 0) {
            valid = false;
            if (highlight) {
                reconnectIntervalField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getBufferSize().length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getMaxRetryCount().length() == 0) {
            valid = false;
            if (highlight) {
                maximumRetryCountField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getTemplate().length() == 0) {
            valid = false;
            if (highlight) {
                template.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (!props.isIgnoreResponse()) {
            if (props.getResponseTimeout().length() == 0) {
                valid = false;
                if (highlight) {
                    responseTimeoutField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        hostAddressField.setBackground(null);
        hostPortField.setBackground(null);
        sendTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        beginBytesField.setBackground(null);
        endBytesField.setBackground(null);
        maximumRetryCountField.setBackground(null);
        template.setBackground(null);
        responseTimeoutField.setBackground(null);
        reconnectIntervalField.setBackground(null);
    }

    private boolean isValidHexString(String str) {
        Pattern pattern = Pattern.compile("^(0x)?([0-9a-fA-F]{2})+$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
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
        frameEncodingButtonGroup = new javax.swing.ButtonGroup();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        hostPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        sendTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        bufferSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        maximumRetryCountField = new com.mirth.connect.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        hostAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        responseTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel19 = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        template = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        reconnectIntervalLabel = new javax.swing.JLabel();
        ignoreACKCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        dataTypeASCII = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeBinary = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeLabel = new javax.swing.JLabel();
        testConnection = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        frameEncodingASCIIRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        frameEncodingHexRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        beginBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        endBytesField = new com.mirth.connect.client.ui.components.MirthTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel13.setText("Keep Connection Open:");

        jLabel15.setText("Buffer Size (bytes):");

        jLabel16.setText("Send Timeout (ms):");

        jLabel17.setText("Host Port:");

        jLabel18.setText("Host Address:");

        jLabel8.setText("Maximum Retry Count:");

        hostPortField.setToolTipText("The port on which to connect.");

        sendTimeoutField.setToolTipText("The number of milliseconds to keep the connection to the host open.");

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

        responseTimeoutField.setToolTipText("The number of milliseconds the connector should wait for a response from the host after sending a message.");

        jLabel19.setText("Response Timeout (ms):");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("<html>The character set encoding to use when converting the outbound message to a byte stream if Data Type ASCII is selected below.<br>Select Default to use the default character set encoding for the JVM running the Mirth server.</html>");
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        encodingLabel.setText("Encoding:");

        jLabel7.setText("Template:");

        template.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        jLabel14.setText("Frame Encoding:");

        frameEncodingASCIIRadio.setBackground(new java.awt.Color(255, 255, 255));
        frameEncodingASCIIRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        frameEncodingButtonGroup.add(frameEncodingASCIIRadio);
        frameEncodingASCIIRadio.setText("ASCII");
        frameEncodingASCIIRadio.setToolTipText("<html>Select ASCII to encode the beginning and ending frame bytes as US-ASCII. <br>Select Hex to use the literal hexadecimal representation for the beginning and ending frame bytes. <br>Only the characters 0-9, a-f, and A-F are allowed, and the string may optionally be prefixed with \"0x\".</html>");
        frameEncodingASCIIRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        frameEncodingASCIIRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frameEncodingASCIIRadioActionPerformed(evt);
            }
        });

        frameEncodingHexRadio.setBackground(new java.awt.Color(255, 255, 255));
        frameEncodingHexRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        frameEncodingButtonGroup.add(frameEncodingHexRadio);
        frameEncodingHexRadio.setText("Hex");
        frameEncodingHexRadio.setToolTipText("<html>Select ASCII to encode the beginning and ending frame bytes as US-ASCII. <br>Select Hex to use the literal hexadecimal representation for the beginning and ending frame bytes. <br>Only the characters 0-9, a-f, and A-F are allowed, and the string may optionally be prefixed with \"0x\".</html>");
        frameEncodingHexRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        frameEncodingHexRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frameEncodingHexRadioActionPerformed(evt);
            }
        });

        jLabel20.setText("Beginning Bytes:");

        jLabel21.setText("Ending Bytes:");

        beginBytesField.setToolTipText("<html>Enter the bytes expected to send before the beginning of the actual message. <br>If using hex frame encoding, then only the characters 0-9, a-f, and A-F are allowed, <br>and the string my optionally be prefixed with \"0x\".</html>");

        endBytesField.setToolTipText("<html>Enter the bytes expected to send after the end of the actual message. <br>If using hex frame encoding, then only the characters 0-9, a-f, and A-F are allowed, <br>and the string my optionally be prefixed with \"0x\".</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dataTypeLabel)
                    .addComponent(encodingLabel)
                    .addComponent(jLabel19)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel18)
                                .addComponent(jLabel17)
                                .addComponent(jLabel16)
                                .addComponent(jLabel15)
                                .addComponent(jLabel13)
                                .addComponent(jLabel8)
                                .addComponent(jLabel14)
                                .addComponent(jLabel20)))
                        .addComponent(reconnectIntervalLabel))
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(maximumRetryCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(hostAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testConnection))
                            .addComponent(hostPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(dataTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dataTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(frameEncodingASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(frameEncodingHexRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(1, 1, 1)
                                .addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(beginBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(endBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(responseTimeoutField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ignoreACKCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(16, 16, 16))
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
                    .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(frameEncodingASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(frameEncodingHexRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(beginBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(endBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(responseTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(template, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            responseTimeoutField.setText("0");
            responseTimeoutField.setEnabled(false);
        } else {
            responseTimeoutField.setEnabled(true);
        }
    }//GEN-LAST:event_ignoreACKCheckBoxActionPerformed

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        final String workingId = parent.startWorking("Testing connection...");

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {

                try {
                    ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(getConnectorName(), "testConnection", getProperties());

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

    private void frameEncodingASCIIRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frameEncodingASCIIRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_frameEncodingASCIIRadioActionPerformed

    private void frameEncodingHexRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frameEncodingHexRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_frameEncodingHexRadioActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_charsetEncodingComboboxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTextField beginBytesField;
    private com.mirth.connect.client.ui.components.MirthTextField bufferSizeField;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeASCII;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeBinary;
    private javax.swing.ButtonGroup dataTypeButtonGroup;
    private javax.swing.JLabel dataTypeLabel;
    private javax.swing.JLabel encodingLabel;
    private com.mirth.connect.client.ui.components.MirthTextField endBytesField;
    private com.mirth.connect.client.ui.components.MirthRadioButton frameEncodingASCIIRadio;
    private javax.swing.ButtonGroup frameEncodingButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton frameEncodingHexRadio;
    private com.mirth.connect.client.ui.components.MirthTextField hostAddressField;
    private com.mirth.connect.client.ui.components.MirthTextField hostPortField;
    private com.mirth.connect.client.ui.components.MirthCheckBox ignoreACKCheckBox;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField maximumRetryCountField;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthTextField responseTimeoutField;
    private com.mirth.connect.client.ui.components.MirthTextField sendTimeoutField;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea template;
    private javax.swing.JButton testConnection;
    private javax.swing.ButtonGroup usePersistenceQueuesGroup;
    // End of variables declaration//GEN-END:variables
}
