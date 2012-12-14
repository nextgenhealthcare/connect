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

        frameEncodingToolTipText = frameEncodingComboBox.getToolTipText();
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

        properties.setRemoteAddress(remoteAddressField.getText());
        properties.setRemotePort(remotePortField.getText());
        properties.setOverrideLocalBinding(overrideLocalBindingYesRadio.isSelected());
        properties.setLocalAddress(localAddressField.getText());
        properties.setLocalPort(localPortField.getText());
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

        remoteAddressField.setText(props.getRemoteAddress());
        remotePortField.setText(props.getRemotePort());

        if (props.isOverrideLocalBinding()) {
            overrideLocalBindingYesRadio.setSelected(true);
            overrideLocalBindingYesRadioActionPerformed(null);
        } else {
            overrideLocalBindingNoRadio.setSelected(true);
            overrideLocalBindingNoRadioActionPerformed(null);
        }

        localAddressField.setText(props.getLocalAddress());
        localPortField.setText(props.getLocalPort());
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

        frameEncodingComboBox.removeActionListener(this);
        if (props.getStartOfMessageBytes().equals(TcpUtil.DEFAULT_LLP_START_BYTES) && props.getEndOfMessageBytes().equals(TcpUtil.DEFAULT_LLP_END_BYTES)) {
            frameEncodingComboBox.setSelectedItem("MLLP");
        } else {
            frameEncodingComboBox.setSelectedItem("Custom");
        }
        frameEncodingComboBox.addActionListener(this);

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

        if (props.getRemoteAddress().length() <= 3) {
            valid = false;
            if (highlight) {
                remoteAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getRemotePort().length() == 0) {
            valid = false;
            if (highlight) {
                remotePortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.isOverrideLocalBinding()) {
            if (props.getLocalAddress().length() <= 3) {
                valid = false;
                if (highlight) {
                    localAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (props.getLocalPort().length() == 0) {
                valid = false;
                if (highlight) {
                    localPortField.setBackground(UIConstants.INVALID_COLOR);
                }
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
        remoteAddressField.setBackground(null);
        remotePortField.setBackground(null);
        localAddressField.setBackground(null);
        localPortField.setBackground(null);
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
            }
        } else {
            endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(text);

            if (frameEncodingComboBox.getSelectedItem().equals("MLLP") && startOfMessageBytesField.getText().equals(TcpUtil.DEFAULT_LLP_START_BYTES) && text.equals(TcpUtil.DEFAULT_LLP_END_BYTES)) {
                switchTo = "MLLP";
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
        overrideLocalBindingButtonGroup = new javax.swing.ButtonGroup();
        keepConnectionOpenLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        sendTimeoutLabel = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        remotePortField = new com.mirth.connect.client.ui.components.MirthTextField();
        sendTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        bufferSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        remoteAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
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
        localAddressLabel = new javax.swing.JLabel();
        localAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        localPortLabel = new javax.swing.JLabel();
        localPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        keepConnectionOpenLabel1 = new javax.swing.JLabel();
        overrideLocalBindingYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        overrideLocalBindingNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        keepConnectionOpenLabel.setText("Keep Connection Open:");

        jLabel15.setText("Buffer Size (bytes):");

        sendTimeoutLabel.setText("Send Timeout (ms):");

        jLabel17.setText("Remote Port:");

        jLabel18.setText("Remote Address:");

        remotePortField.setToolTipText("<html>The port on which to connect.</html>");

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

        remoteAddressField.setToolTipText("<html>The DNS domain name or IP address on which to connect.</html>");

        responseTimeoutField.setToolTipText("<html>The number of milliseconds the connector should wait for a response from the host after sending a message.<br/>If the End of Message byte sequence is defined, a response will be received when it is detected.<br/>If the timeout elapses, all data received at that point will be stored as the response data.</html>");

        responseTimeoutLabel.setText("Response Timeout (ms):");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
                "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)",
                "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("<html>The character set encoding to use when converting the outbound message to a byte stream if Data Type ASCII is selected.<br>Select Default to use the default character set encoding for the JVM running the Mirth server.</html>");
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        encodingLabel.setText("Encoding:");

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

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        startOfMessageBytesField.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        startOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startOfMessageBytesFieldActionPerformed(evt);
            }
        });

        endOfMessageBytesField.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        endOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endOfMessageBytesFieldActionPerformed(evt);
            }
        });

        processHL7ACKLabel.setText("Process HL7 ACK:");

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
        startOfMessageBytes0XLabel.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");

        frameEncodingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "MLLP",
                "Custom" }));
        frameEncodingComboBox.setToolTipText("<html>Select MLLP to use the default frame encoding characters as per the MLLPv1 specifications.<br/>Select Custom to enter user-defined frame encoding characters.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        frameEncodingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frameEncodingComboBoxActionPerformed(evt);
            }
        });

        frameEncodingLabel.setText("Frame Encoding:");

        messageDataLabel.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        messageDataLabel.setText("<Message Data>");
        messageDataLabel.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");
        messageDataLabel.setEnabled(false);

        endOfMessageBytes0XLabel.setText("0x");
        endOfMessageBytes0XLabel.setToolTipText("<html>Enter the bytes to send before the beginning and after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.<br/><br/><b>Sample Frame: SOM <i>&lt;Message Data&gt;</i> EOM</b></html>");

        jLabel19.setText("Sample Frame:");

        sampleMessageLabel.setForeground(new java.awt.Color(153, 153, 153));
        sampleMessageLabel.setText("<html><b>&lt;VT&gt;</b> <i>&lt;Message Data&gt;</i> <b>&lt;FS&gt;&lt;CR&gt;</b></html>");
        sampleMessageLabel.setEnabled(false);

        localAddressLabel.setText("Local Address:");

        localAddressField.setToolTipText("<html>The local address that the client socket will be bound to, if Override Local Binding is set to Yes.<br/></html>");

        localPortLabel.setText("Local Port:");

        localPortField.setToolTipText("<html>The local port that the client socket will be bound to, if Override Local Binding is set to Yes.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>");

        keepConnectionOpenLabel1.setText("Override Local Binding:");

        overrideLocalBindingYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        overrideLocalBindingYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        overrideLocalBindingButtonGroup.add(overrideLocalBindingYesRadio);
        overrideLocalBindingYesRadio.setText("Yes");
        overrideLocalBindingYesRadio.setToolTipText("<html>Select Yes to override the local address and port that the client socket will be bound to.<br/>Select No to use the default values of 0.0.0.0:0.<br/>A local port of zero (0) indicates that the OS should assign an ephemeral port automatically.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>");
        overrideLocalBindingYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        overrideLocalBindingYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overrideLocalBindingYesRadioActionPerformed(evt);
            }
        });

        overrideLocalBindingNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        overrideLocalBindingNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        overrideLocalBindingButtonGroup.add(overrideLocalBindingNoRadio);
        overrideLocalBindingNoRadio.setText("No");
        overrideLocalBindingNoRadio.setToolTipText("<html>Select Yes to override the local address and port that the client socket will be bound to.<br/>Select No to use the default values of 0.0.0.0:0.<br/>A local port of zero (0) indicates that the OS should assign an ephemeral port automatically.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>");
        overrideLocalBindingNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        overrideLocalBindingNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overrideLocalBindingNoRadioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(13, 13, 13).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabel19).addComponent(frameEncodingLabel).addComponent(jLabel18).addComponent(jLabel17).addComponent(keepConnectionOpenLabel1).addComponent(localAddressLabel).addComponent(localPortLabel).addComponent(keepConnectionOpenLabel).addComponent(sendTimeoutLabel).addComponent(jLabel15).addComponent(responseTimeoutLabel).addComponent(processHL7ACKLabel).addComponent(dataTypeLabel).addComponent(encodingLabel).addComponent(jLabel7)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(sampleMessageLabel).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(frameEncodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(startOfMessageBytes0XLabel).addGap(3, 3, 3).addComponent(startOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(4, 4, 4).addComponent(messageDataLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(endOfMessageBytes0XLabel).addGap(3, 3, 3).addComponent(endOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(remoteAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(testConnection)).addComponent(remotePortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(overrideLocalBindingYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(overrideLocalBindingNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(localAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(localPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(responseTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(ignoreResponseCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(processHL7ACKYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(processHL7ACKNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(dataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(dataTypeASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(0, 9, Short.MAX_VALUE)).addComponent(templateTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(frameEncodingLabel).addComponent(frameEncodingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(startOfMessageBytes0XLabel).addComponent(startOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(messageDataLabel).addComponent(endOfMessageBytes0XLabel).addComponent(endOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel19).addComponent(sampleMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel18).addComponent(remoteAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(testConnection)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel17).addComponent(remotePortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(keepConnectionOpenLabel1).addComponent(overrideLocalBindingYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(overrideLocalBindingNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(localAddressLabel).addComponent(localAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(localPortLabel).addComponent(localPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(keepConnectionOpenLabel).addComponent(keepConnectionOpenYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(keepConnectionOpenNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(sendTimeoutLabel).addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel15).addComponent(bufferSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(responseTimeoutLabel).addComponent(responseTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(ignoreResponseCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(processHL7ACKLabel).addComponent(processHL7ACKYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(processHL7ACKNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(dataTypeLabel).addComponent(dataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(dataTypeASCIIRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(encodingLabel).addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jLabel7).addGap(0, 0, Short.MAX_VALUE)).addComponent(templateTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)).addContainerGap()));
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

    private void overrideLocalBindingYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overrideLocalBindingYesRadioActionPerformed
        localAddressField.setEnabled(true);
        localAddressLabel.setEnabled(true);
        localPortField.setEnabled(true);
        localPortLabel.setEnabled(true);
    }//GEN-LAST:event_overrideLocalBindingYesRadioActionPerformed

    private void overrideLocalBindingNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overrideLocalBindingNoRadioActionPerformed
        localAddressField.setEnabled(false);
        localAddressLabel.setEnabled(false);
        localPortField.setEnabled(false);
        localPortLabel.setEnabled(false);
    }//GEN-LAST:event_overrideLocalBindingNoRadioActionPerformed

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
    private com.mirth.connect.client.ui.components.MirthCheckBox ignoreResponseCheckBox;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel7;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private javax.swing.JLabel keepConnectionOpenLabel;
    private javax.swing.JLabel keepConnectionOpenLabel1;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField localAddressField;
    private javax.swing.JLabel localAddressLabel;
    private com.mirth.connect.client.ui.components.MirthTextField localPortField;
    private javax.swing.JLabel localPortLabel;
    private javax.swing.JLabel messageDataLabel;
    private javax.swing.ButtonGroup overrideLocalBindingButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton overrideLocalBindingNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton overrideLocalBindingYesRadio;
    private javax.swing.ButtonGroup processHL7ACKButtonGroup;
    private javax.swing.JLabel processHL7ACKLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton processHL7ACKNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton processHL7ACKYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField remoteAddressField;
    private com.mirth.connect.client.ui.components.MirthTextField remotePortField;
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
