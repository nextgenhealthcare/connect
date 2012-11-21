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

import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.util.ConnectionTestResponse;
import com.mirth.connect.util.TcpUtil;

public class TcpSender extends ConnectorSettingsPanel implements DocumentListener, ActionListener {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    private String frameEncodingToolTipText;
    private String messageBytesToolTipText;
    private String startOfMessageAbbreviation;
    private String endOfMessageAbbreviation;

    public TcpSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        sendTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        responseTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        startOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, true, true));
        endOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, true, true));
        startOfMessageBytesField.getDocument().addDocumentListener(this);
        endOfMessageBytesField.getDocument().addDocumentListener(this);

        frameEncodingToolTipText = frameEncodingLabel.getToolTipText();
        messageBytesToolTipText = messageDataLabel.getToolTipText();
        startOfMessageAbbreviation = "";
        endOfMessageAbbreviation = "";

        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    @Override
    public String getConnectorName() {
        return new TcpDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        TcpDispatcherProperties properties = new TcpDispatcherProperties();

        properties.setHost(hostField.getText());
        properties.setPort(portField.getText());
        properties.setSendTimeout(sendTimeoutField.getText());
        properties.setBufferSize(bufferSizeField.getText());
        properties.setKeepConnectionOpen(keepConnectionOpenYesRadio.isSelected());
        properties.setStartOfMessageBytes(startOfMessageBytesField.getText());
        properties.setEndOfMessageBytes(endOfMessageBytesField.getText());
        properties.setResponseTimeout(responseTimeoutField.getText());
        properties.setIgnoreResponse(ignoreResponseCheckBox.isSelected());
        properties.setProcessHL7ACK(processHL7ACKYesRadio.isSelected());
        properties.setDataTypeBinary(dataTypeBinaryRadio.isSelected());
        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        properties.setTemplate(templateTextArea.getText());

        logger.debug("getProperties: properties=" + properties);

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        logger.debug("setProperties: properties=" + properties);
        TcpDispatcherProperties props = (TcpDispatcherProperties) properties;

        hostField.setText(props.getHost());
        portField.setText(props.getPort());
        sendTimeoutField.setText(props.getSendTimeout());
        bufferSizeField.setText(props.getBufferSize());

        if (props.isKeepConnectionOpen()) {
            keepConnectionOpenYesRadio.setSelected(true);
            keepConnectionOpenYesRadioActionPerformed(null);
        } else {
            keepConnectionOpenNoRadio.setSelected(true);
            keepConnectionOpenNoRadioActionPerformed(null);
        }

        startOfMessageBytesField.setText(props.getStartOfMessageBytes());
        endOfMessageBytesField.setText(props.getEndOfMessageBytes());

        if (props.getStartOfMessageBytes().equals(TcpUtil.DEFAULT_LLP_START_BYTES) && props.getEndOfMessageBytes().equals(TcpUtil.DEFAULT_LLP_END_BYTES)) {
            frameEncodingComboBox.setSelectedItem("MLLP");
        } else if (props.getStartOfMessageBytes().equals(TcpUtil.DEFAULT_ASTM_START_BYTES) && props.getEndOfMessageBytes().equals(TcpUtil.DEFAULT_ASTM_END_BYTES)) {
            frameEncodingComboBox.setSelectedItem("ASTM");
        } else {
            frameEncodingComboBox.setSelectedItem("Custom");
        }

        responseTimeoutField.setText(String.valueOf(props.getResponseTimeout()));

        ignoreResponseCheckBox.setSelected(props.isIgnoreResponse());
        ignoreResponseCheckBoxActionPerformed(null);

        if (props.isProcessHL7ACK()) {
            processHL7ACKYesRadio.setSelected(true);
        } else {
            processHL7ACKNoRadio.setSelected(true);
        }

        if (props.isDataTypeBinary()) {
            dataTypeBinaryRadio.setSelected(true);
            dataTypeBinaryRadioActionPerformed(null);
        } else {
            dataTypeASCIIRadio.setSelected(true);
            dataTypeASCIIRadioActionPerformed(null);
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharsetEncoding());

        templateTextArea.setText(props.getTemplate());
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
                hostField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getPort().length() == 0) {
            valid = false;
            if (highlight) {
                portField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.isKeepConnectionOpen()) {
            if (props.getSendTimeout().length() == 0) {
                valid = false;
                if (highlight) {
                    sendTimeoutField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        if (props.getBufferSize().length() == 0) {
            valid = false;
            if (highlight) {
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
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
        if (!props.isIgnoreResponse()) {
            if (props.getResponseTimeout().length() == 0) {
                valid = false;
                if (highlight) {
                    responseTimeoutField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        if (props.getTemplate().length() == 0) {
            valid = false;
            if (highlight) {
                templateTextArea.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        hostField.setBackground(null);
        portField.setBackground(null);
        sendTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        startOfMessageBytesField.setBackground(null);
        endOfMessageBytesField.setBackground(null);
        responseTimeoutField.setBackground(null);
        templateTextArea.setBackground(null);
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
        processHL7ACKButtonGroup = new javax.swing.ButtonGroup();
        keepConnectionOpenLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        sendTimeoutLabel = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        portField = new com.mirth.connect.client.ui.components.MirthTextField();
        sendTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        bufferSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        hostField = new com.mirth.connect.client.ui.components.MirthTextField();
        responseTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        responseTimeoutLabel = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        templateTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        ignoreResponseCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        dataTypeASCIIRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeBinaryRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeLabel = new javax.swing.JLabel();
        testConnection = new javax.swing.JButton();
        startOfMessageBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        endOfMessageBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        processHL7ACKLabel = new javax.swing.JLabel();
        processHL7ACKYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        processHL7ACKNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        startOfMessageBytes0XLabel = new javax.swing.JLabel();
        frameEncodingComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        frameEncodingLabel = new javax.swing.JLabel();
        messageDataLabel = new javax.swing.JLabel();
        endOfMessageBytes0XLabel = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        sampleMessageLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        keepConnectionOpenLabel.setText("Keep Connection Open:");
        keepConnectionOpenLabel.setToolTipText("<html>Select Yes to keep the connection to the host open across multiple messages.<br>Select No to immediately the close the connection to the host after sending each message.</html>");

        jLabel15.setText("Buffer Size (bytes):");
        jLabel15.setToolTipText("<html>The size, in bytes, of the buffer to be used to hold messages waiting to be sent. Generally, the default value is fine.<html>");

        sendTimeoutLabel.setText("Send Timeout (ms):");
        sendTimeoutLabel.setToolTipText("<html>The number of milliseconds to keep the connection to the host open,<br/>if Keep Connection Open is enabled.</html>");

        jLabel17.setText("Host Port:");
        jLabel17.setToolTipText("<html>The port on which to connect.</html>");

        jLabel18.setText("Host Address:");
        jLabel18.setToolTipText("<html>The DNS domain name or IP address on which to connect.</html>");

        portField.setToolTipText("<html>The port on which to connect.</html>");

        sendTimeoutField.setToolTipText("<html>The number of milliseconds to keep the connection to the host open,<br/>if Keep Connection Open is enabled.</html>");

        bufferSizeField.setToolTipText("<html>The size, in bytes, of the buffer to be used to hold messages waiting to be sent. Generally, the default value is fine.<html>");

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setToolTipText("<html>Select Yes to keep the connection to the host open across multiple messages.<br>Select No to immediately the close the connection to the host after sending each message.</html>");
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keepConnectionOpenYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepConnectionOpenYesRadioActionPerformed(evt);
            }
        });

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setToolTipText("<html>Select Yes to keep the connection to the host open across multiple messages.<br>Select No to immediately the close the connection to the host after sending each message.</html>");
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keepConnectionOpenNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepConnectionOpenNoRadioActionPerformed(evt);
            }
        });

        hostField.setToolTipText("<html>The DNS domain name or IP address on which to connect.</html>");

        responseTimeoutField.setToolTipText("<html>The number of milliseconds the connector should wait for a response from the host after sending a message.<br/>If the End of Message byte sequence is defined, a response will be received when it is detected.<br/>If the timeout elapses, all data received at that point will be stored as the response data.</html>");

        responseTimeoutLabel.setText("Response Timeout (ms):");
        responseTimeoutLabel.setToolTipText("<html>The number of milliseconds the connector should wait for a response from the host after sending a message.<br/>If the End of Message byte sequence is defined, a response will be received when it is detected.<br/>If the timeout elapses, all data received at that point will be stored as the response data.</html>");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("<html>The character set encoding to use when converting the outbound message to a byte stream if Data Type ASCII is selected.<br>Select Default to use the default character set encoding for the JVM running the Mirth server.</html>");
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        encodingLabel.setText("Encoding:");
        encodingLabel.setToolTipText("<html>The character set encoding to use when converting the outbound message to a byte stream if Data Type ASCII is selected.<br>Select Default to use the default character set encoding for the JVM running the Mirth server.</html>");

        jLabel7.setText("Template:");

        templateTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        ignoreResponseCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        ignoreResponseCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreResponseCheckBox.setText("Ignore Response");
        ignoreResponseCheckBox.setToolTipText("<html>If checked, the connector will not wait for a response after sending a message.<br>If unchecked, the connector will wait for a response from the host after each message is sent.</html>");
        ignoreResponseCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ignoreResponseCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreResponseCheckBoxActionPerformed(evt);
            }
        });

        dataTypeASCIIRadio.setBackground(new java.awt.Color(255, 255, 255));
        dataTypeASCIIRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dataTypeButtonGroup.add(dataTypeASCIIRadio);
        dataTypeASCIIRadio.setSelected(true);
        dataTypeASCIIRadio.setText("ASCII");
        dataTypeASCIIRadio.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select ASCII if the outbound message is text (will be encoded with the specified charset).</html>");
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
        dataTypeBinaryRadio.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select ASCII if the outbound message is text (will be encoded with the specified charset).</html>");
        dataTypeBinaryRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dataTypeBinaryRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeBinaryRadioActionPerformed(evt);
            }
        });

        dataTypeLabel.setText("Data Type:");
        dataTypeLabel.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select ASCII if the outbound message is text (will be encoded with the specified charset).</html>");

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        startOfMessageBytesField.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        startOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startOfMessageBytesFieldActionPerformed(evt);
            }
        });

        endOfMessageBytesField.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        endOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endOfMessageBytesFieldActionPerformed(evt);
            }
        });

        processHL7ACKLabel.setText("Process HL7 ACK:");
        processHL7ACKLabel.setToolTipText("<html>If enabled, only successful HL7 v2.x ACK codes will allow a message to be marked as successful.<br/>If disabled, the response will not be parsed and the message will always be marked as successful.</html>");

        processHL7ACKYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        processHL7ACKYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processHL7ACKButtonGroup.add(processHL7ACKYesRadio);
        processHL7ACKYesRadio.setText("Yes");
        processHL7ACKYesRadio.setToolTipText("<html>If enabled, only successful HL7 v2.x ACK codes will allow a message to be marked as successful.<br/>If disabled, the response will not be parsed and the message will always be marked as successful.</html>");
        processHL7ACKYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processHL7ACKNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        processHL7ACKNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processHL7ACKButtonGroup.add(processHL7ACKNoRadio);
        processHL7ACKNoRadio.setText("No");
        processHL7ACKNoRadio.setToolTipText("<html>If enabled, only successful HL7 v2.x ACK codes will allow a message to be marked as successful.<br/>If disabled, the response will not be parsed and the message will always be marked as successful.</html>");
        processHL7ACKNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        startOfMessageBytes0XLabel.setText("0x");
        startOfMessageBytes0XLabel.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");

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
        messageDataLabel.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        messageDataLabel.setEnabled(false);

        endOfMessageBytes0XLabel.setText("0x");
        endOfMessageBytes0XLabel.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>An even number of characters must be entered, and only the characters 0-9 and A-F are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");

        jLabel19.setText("Sample Frame:");

        sampleMessageLabel.setForeground(new java.awt.Color(153, 153, 153));
        sampleMessageLabel.setText("<html><b>&lt;VT&gt;</b> <i>&lt;Message Data&gt;</i> <b>&lt;FS&gt;&lt;CR&gt;</b></html>");
        sampleMessageLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabel19).addComponent(jLabel18).addComponent(frameEncodingLabel).addComponent(jLabel17).addComponent(keepConnectionOpenLabel).addComponent(sendTimeoutLabel).addComponent(jLabel15).addComponent(responseTimeoutLabel).addComponent(processHL7ACKLabel).addComponent(dataTypeLabel).addComponent(encodingLabel).addComponent(jLabel7)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(templateTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(responseTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(ignoreResponseCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(processHL7ACKYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(processHL7ACKNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(dataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(dataTypeASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addGroup(layout.createSequentialGroup().addComponent(hostField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(testConnection)).addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup().addComponent(frameEncodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(startOfMessageBytes0XLabel).addGap(3, 3, 3).addComponent(startOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(4, 4, 4).addComponent(messageDataLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(endOfMessageBytes0XLabel).addGap(3, 3, 3).addComponent(endOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))).addGap(0, 0, Short.MAX_VALUE)).addComponent(sampleMessageLabel)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(frameEncodingLabel).addComponent(frameEncodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(startOfMessageBytes0XLabel).addComponent(startOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(messageDataLabel).addComponent(endOfMessageBytes0XLabel).addComponent(endOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel19).addComponent(sampleMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel18).addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(testConnection)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel17).addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(keepConnectionOpenLabel).addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(sendTimeoutLabel).addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel15).addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(responseTimeoutLabel).addComponent(responseTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(ignoreResponseCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(processHL7ACKLabel).addComponent(processHL7ACKYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(processHL7ACKNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(dataTypeLabel).addComponent(dataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(dataTypeASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(encodingLabel).addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel7).addComponent(templateTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)).addContainerGap()));
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

    private void ignoreResponseCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ignoreResponseCheckBoxActionPerformed
    {//GEN-HEADEREND:event_ignoreResponseCheckBoxActionPerformed
        boolean selected = ignoreResponseCheckBox.isSelected();
        responseTimeoutLabel.setEnabled(!selected);
        responseTimeoutField.setEnabled(!selected);
        processHL7ACKLabel.setEnabled(!selected);
        processHL7ACKYesRadio.setEnabled(!selected);
        processHL7ACKNoRadio.setEnabled(!selected);
        if (selected) {
            processHL7ACKNoRadio.setSelected(true);
        }
    }//GEN-LAST:event_ignoreResponseCheckBoxActionPerformed

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

    private void startOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_startOfMessageBytesFieldActionPerformed

    private void endOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_endOfMessageBytesFieldActionPerformed

    private void keepConnectionOpenYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepConnectionOpenYesRadioActionPerformed
        sendTimeoutLabel.setEnabled(true);
        sendTimeoutField.setEnabled(true);
    }//GEN-LAST:event_keepConnectionOpenYesRadioActionPerformed

    private void keepConnectionOpenNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepConnectionOpenNoRadioActionPerformed
        sendTimeoutLabel.setEnabled(false);
        sendTimeoutField.setEnabled(false);
    }//GEN-LAST:event_keepConnectionOpenNoRadioActionPerformed

    private void frameEncodingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frameEncodingComboBoxActionPerformed
    }//GEN-LAST:event_frameEncodingComboBoxActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
    }// GEN-LAST:event_charsetEncodingComboboxActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables

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
    private com.mirth.connect.client.ui.components.MirthTextField hostField;
    private com.mirth.connect.client.ui.components.MirthCheckBox ignoreResponseCheckBox;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel7;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private javax.swing.JLabel keepConnectionOpenLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private javax.swing.JLabel messageDataLabel;
    private com.mirth.connect.client.ui.components.MirthTextField portField;
    private javax.swing.ButtonGroup processHL7ACKButtonGroup;
    private javax.swing.JLabel processHL7ACKLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton processHL7ACKNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton processHL7ACKYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField responseTimeoutField;
    private javax.swing.JLabel responseTimeoutLabel;
    private javax.swing.JLabel sampleMessageLabel;
    private com.mirth.connect.client.ui.components.MirthTextField sendTimeoutField;
    private javax.swing.JLabel sendTimeoutLabel;
    private javax.swing.JLabel startOfMessageBytes0XLabel;
    private com.mirth.connect.client.ui.components.MirthTextField startOfMessageBytesField;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea templateTextArea;
    private javax.swing.JButton testConnection;
    // End of variables declaration//GEN-END:variables
}
