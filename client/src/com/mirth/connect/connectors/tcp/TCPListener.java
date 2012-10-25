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

import org.apache.log4j.Logger;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class TcpListener extends ConnectorSettingsPanel {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    /** Creates new form TCPListener */
    public TcpListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        receiveTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    @Override
    public String getConnectorName() {
        return new TcpReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        TcpReceiverProperties properties = new TcpReceiverProperties();

        properties.setTimeout(receiveTimeoutField.getText());
        properties.setBufferSize(bufferSizeField.getText());
        properties.setKeepConnectionOpen(keepConnectionOpenYesRadio.isSelected());
        properties.setFrameEncodingIsHex(frameEncodingHexRadio.isSelected());
        properties.setBeginBytes(beginBytesField.getText());
        properties.setEndBytes(endBytesField.getText());
        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        properties.setDataTypeIsBase64(dataTypeBinary.isSelected());
        properties.setAckOnNewConnection(ackOnNewConnectionYes.isSelected());
        properties.setAckIP(ackAddressField.getText());
        properties.setAckPort(ackPortField.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        TcpReceiverProperties props = (TcpReceiverProperties) properties;

        receiveTimeoutField.setText(props.getTimeout());
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

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharsetEncoding());

        if (props.isDataTypeBase64()) {
            dataTypeBinary.setSelected(true);
            dataTypeBinaryActionPerformed(null);
        } else {
            dataTypeASCII.setSelected(true);
            dataTypeASCIIActionPerformed(null);
        }

        if (props.isAckOnNewConnection()) {
            ackOnNewConnectionYesActionPerformed(null);
            ackOnNewConnectionYes.setSelected(true);
        } else {
            ackOnNewConnectionNoActionPerformed(null);
            ackOnNewConnectionNo.setSelected(true);
        }

        ackAddressField.setText(props.getAckIP());
        ackPortField.setText(props.getAckPort());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new TcpReceiverProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        TcpReceiverProperties props = (TcpReceiverProperties) properties;

        boolean valid = true;

        if (props.getTimeout().length() == 0) {
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
        if (props.isAckOnNewConnection()) {
            if (props.getAckIP().length() <= 3) {
                valid = false;
                if (highlight) {
                    ackAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (props.getAckPort().length() == 0) {
                valid = false;
                if (highlight) {
                    ackPortField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        receiveTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        beginBytesField.setBackground(null);
        endBytesField.setBackground(null);
        ackAddressField.setBackground(null);
        ackPortField.setBackground(null);
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
        dataTypeButtonGroup = new javax.swing.ButtonGroup();
        ackOnNewConnectionButtonGroup = new javax.swing.ButtonGroup();
        frameEncodingButtonGroup = new javax.swing.ButtonGroup();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        bufferSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        ackOnNewConnectionLabel = new javax.swing.JLabel();
        ackOnNewConnectionYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ackOnNewConnectionNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ackIPLabel = new javax.swing.JLabel();
        ackPortLabel = new javax.swing.JLabel();
        ackPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        ackAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        dataTypeASCII = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeBinary = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeLabel = new javax.swing.JLabel();
        keepConnectionOpenLabel = new javax.swing.JLabel();
        keepConnectionOpenYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        encodingLabel1 = new javax.swing.JLabel();
        encodingLabel2 = new javax.swing.JLabel();
        beginBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        endBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        encodingLabel3 = new javax.swing.JLabel();
        frameEncodingASCIIRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        frameEncodingHexRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size (bytes):");

        bufferSizeField.setToolTipText("<html>Use larger values for larger messages, and smaller values <br>for smaller messages. Generally, the default value is fine.</html>");

        receiveTimeoutField.setToolTipText("The amount of time, in milliseconds, to wait without receiving a message before closing a connection.");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding used by the message sender,<br> or Select Default to use the default character set encoding for the JVM running Mirth.</html>");

        encodingLabel.setText("Encoding:");

        ackOnNewConnectionLabel.setText("Response on New Connection:");

        ackOnNewConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        ackOnNewConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ackOnNewConnectionButtonGroup.add(ackOnNewConnectionYes);
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
        ackOnNewConnectionButtonGroup.add(ackOnNewConnectionNo);
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

        keepConnectionOpenLabel.setText("Keep Connection Open:");

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setToolTipText("<html>Select No to close the listening socket after each message is received and the response (if selected) is sent. <br>Select Yes to always keep the socket open unless the sending system closes it.  If Yes is selected, messages <br>will only be processed if data is received and either the receive timeout is reached or the sending system closes <br>the socket. The sending system will also need to use a timeout or delimiter method of processing responses.</html>");
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setToolTipText("<html>Select No to close the listening socket after each message is received and the response (if selected) is sent. <br>Select Yes to always keep the socket open unless the sending system closes it.  If Yes is selected, messages <br>will only be processed if data is received and either the receive timeout is reached or the sending system closes <br>the socket. The sending system will also need to use a timeout or delimiter method of processing responses.</html>");
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        encodingLabel1.setText("Beginning Bytes:");

        encodingLabel2.setText("Ending Bytes:");

        beginBytesField.setToolTipText("<html>Enter the bytes expected to come before the beginning of the actual message. <br>If using hex frame encoding, then only the characters 0-9, a-f, and A-F are allowed, <br>and the string my optionally be prefixed with \"0x\".</html>");
        beginBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                beginBytesFieldActionPerformed(evt);
            }
        });

        endBytesField.setToolTipText("<html>Enter the bytes expected to come after the end of the actual message. <br>If using hex frame encoding, then only the characters 0-9, a-f, and A-F are allowed, <br>and the string my optionally be prefixed with \"0x\".</html>");
        endBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endBytesFieldActionPerformed(evt);
            }
        });

        encodingLabel3.setText("Frame Encoding:");

        frameEncodingASCIIRadio.setBackground(new java.awt.Color(255, 255, 255));
        frameEncodingASCIIRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        frameEncodingButtonGroup.add(frameEncodingASCIIRadio);
        frameEncodingASCIIRadio.setText("ASCII");
        frameEncodingASCIIRadio.setToolTipText("<html>Select ASCII to encode the beginning and ending frame bytes as US-ASCII. <br>Select Hex to use the literal hexadecimal representation for the beginning and ending frame bytes. <br>Only the characters 0-9, a-f, and A-F are allowed, and the string may optionally be prefixed with \"0x\".</html>");
        frameEncodingASCIIRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(38, 38, 38).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(encodingLabel3).addComponent(jLabel3).addComponent(jLabel4).addComponent(keepConnectionOpenLabel))).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(dataTypeLabel).addComponent(encodingLabel).addComponent(ackOnNewConnectionLabel).addComponent(ackIPLabel).addComponent(ackPortLabel))).addComponent(encodingLabel1)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(beginBytesField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(frameEncodingASCIIRadio, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(frameEncodingHexRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(encodingLabel2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(endBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))).addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(ackAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(ackPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(ackOnNewConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(ackOnNewConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(dataTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(dataTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel3).addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel4).addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(keepConnectionOpenLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(encodingLabel3).addComponent(frameEncodingASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(frameEncodingHexRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(encodingLabel1).addComponent(beginBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(encodingLabel2).addComponent(endBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(encodingLabel).addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(dataTypeLabel).addComponent(dataTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(dataTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(ackOnNewConnectionLabel).addComponent(ackOnNewConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(ackOnNewConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(ackIPLabel).addComponent(ackAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(ackPortLabel).addComponent(ackPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
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

    private void beginBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_beginBytesFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_beginBytesFieldActionPerformed

    private void endBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endBytesFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_endBytesFieldActionPerformed

    private void frameEncodingHexRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frameEncodingHexRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_frameEncodingHexRadioActionPerformed

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
    private javax.swing.ButtonGroup ackOnNewConnectionButtonGroup;
    private javax.swing.JLabel ackOnNewConnectionLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton ackOnNewConnectionNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton ackOnNewConnectionYes;
    private com.mirth.connect.client.ui.components.MirthTextField ackPortField;
    private javax.swing.JLabel ackPortLabel;
    private com.mirth.connect.client.ui.components.MirthTextField beginBytesField;
    private com.mirth.connect.client.ui.components.MirthTextField bufferSizeField;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeASCII;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeBinary;
    private javax.swing.ButtonGroup dataTypeButtonGroup;
    private javax.swing.JLabel dataTypeLabel;
    private javax.swing.JLabel encodingLabel;
    private javax.swing.JLabel encodingLabel1;
    private javax.swing.JLabel encodingLabel2;
    private javax.swing.JLabel encodingLabel3;
    private com.mirth.connect.client.ui.components.MirthTextField endBytesField;
    private com.mirth.connect.client.ui.components.MirthRadioButton frameEncodingASCIIRadio;
    private javax.swing.ButtonGroup frameEncodingButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton frameEncodingHexRadio;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private javax.swing.JLabel keepConnectionOpenLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField receiveTimeoutField;
    // End of variables declaration//GEN-END:variables
}
