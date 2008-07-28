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

package com.webreach.mirth.connectors.mllp;

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

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Step;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class LLPListener extends ConnectorClass
{
    /** Creates new form LLPListener */

    public LLPListener()
    {
        name = LLPListenerProperties.name;
        initComponents();
        reconnectIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        receiveTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(LLPListenerProperties.DATATYPE, name);
        properties.put(LLPListenerProperties.LLP_PROTOCOL_NAME, LLPListenerProperties.LLP_PROTOCOL_NAME_VALUE);

        if (serverRadioButton.isSelected())
            properties.put(LLPListenerProperties.LLP_SERVER_MODE, UIConstants.YES_OPTION);
        else
            properties.put(LLPListenerProperties.LLP_SERVER_MODE, UIConstants.NO_OPTION);

        properties.put(LLPListenerProperties.LLP_ADDRESS, listenerAddressField.getText());
        properties.put(LLPListenerProperties.LLP_PORT, listenerPortField.getText());
        properties.put(LLPListenerProperties.LLP_RECONNECT_INTERVAL, reconnectIntervalField.getText());
        properties.put(LLPListenerProperties.LLP_RECEIVE_TIMEOUT, receiveTimeoutField.getText());
        properties.put(LLPListenerProperties.LLP_BUFFER_SIZE, bufferSizeField.getText());

        properties.put(LLPListenerProperties.LLP_START_OF_MESSAGE_CHARACTER, startOfMessageCharacterField.getText());
        properties.put(LLPListenerProperties.LLP_END_OF_MESSAGE_CHARACTER, endOfMessageCharacterField.getText());

        if (ascii.isSelected())
            properties.put(LLPListenerProperties.LLP_CHAR_ENCODING, "ascii");
        else
            properties.put(LLPListenerProperties.LLP_CHAR_ENCODING, "hex");

        if (processBatchYes.isSelected())
            properties.put(LLPListenerProperties.LLP_PROCESS_BATCH_FILES, UIConstants.YES_OPTION);
        else
            properties.put(LLPListenerProperties.LLP_PROCESS_BATCH_FILES, UIConstants.NO_OPTION);

        properties.put(LLPListenerProperties.LLP_RECORD_SEPARATOR, recordSeparatorField.getText());
        properties.put(LLPListenerProperties.LLP_SEGMENT_END, segmentEnd.getText());

        if (sendACKYes.isSelected())
        {
            properties.put(LLPListenerProperties.LLP_SEND_ACK, UIConstants.YES_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER, UIConstants.NO_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_VALUE, "None");
        }
        else if (sendACKNo.isSelected())
        {
            properties.put(LLPListenerProperties.LLP_SEND_ACK, UIConstants.NO_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER, UIConstants.NO_OPTION);
            properties.put(LLPListenerProperties.LLP_RESPONSE_VALUE, "None");
        }
        else if (sendACKTransformer.isSelected())
        {
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

        if (mshAckAcceptYes.isSelected())
            properties.put(LLPListenerProperties.LLP_ACK_MSH_15, UIConstants.YES_OPTION);
        else
            properties.put(LLPListenerProperties.LLP_ACK_MSH_15, UIConstants.NO_OPTION);

        if (ackOnNewConnectionYes.isSelected())
            properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION, UIConstants.YES_OPTION);
        else
            properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION, UIConstants.NO_OPTION);

        properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP, ackAddressField.getText());
        properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_PORT, ackPortField.getText());

        if (waitForEndOfMessageCharYes.isSelected())
            properties.put(LLPListenerProperties.LLP_WAIT_FOR_END_OF_MESSAGE_CHAR, UIConstants.YES_OPTION);
        else
            properties.put(LLPListenerProperties.LLP_WAIT_FOR_END_OF_MESSAGE_CHAR, UIConstants.NO_OPTION);

        if (useStrictLLPYes.isSelected())
            properties.put(LLPListenerProperties.LLP_USE_STRICT_LLP, UIConstants.YES_OPTION);
        else
            properties.put(LLPListenerProperties.LLP_USE_STRICT_LLP, UIConstants.NO_OPTION);

        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();

        if (((String) props.get(LLPListenerProperties.LLP_SERVER_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            serverRadioButtonActionPerformed(null);
            serverRadioButton.setSelected(true);
        }
        else
        {
            clientRadioButtonActionPerformed(null);
            clientRadioButton.setSelected(true);
        }

        listenerAddressField.setText((String) props.get(LLPListenerProperties.LLP_ADDRESS));
        listenerPortField.setText((String) props.get(LLPListenerProperties.LLP_PORT));
        reconnectIntervalField.setText((String) props.get(LLPListenerProperties.LLP_RECONNECT_INTERVAL));
        receiveTimeoutField.setText((String) props.get(LLPListenerProperties.LLP_RECEIVE_TIMEOUT));
        bufferSizeField.setText((String) props.get(LLPListenerProperties.LLP_BUFFER_SIZE));

        if (((String) props.get(LLPListenerProperties.LLP_CHAR_ENCODING)).equals("ascii"))
            ascii.setSelected(true);
        else
            hex.setSelected(true);

        if (((String) props.get(LLPListenerProperties.LLP_PROCESS_BATCH_FILES)).equals(UIConstants.YES_OPTION))
            processBatchYes.setSelected(true);
        else
            processBatchNo.setSelected(true);

        startOfMessageCharacterField.setText((String) props.get(LLPListenerProperties.LLP_START_OF_MESSAGE_CHARACTER));
        endOfMessageCharacterField.setText((String) props.get(LLPListenerProperties.LLP_END_OF_MESSAGE_CHARACTER));
        recordSeparatorField.setText((String) props.get(LLPListenerProperties.LLP_RECORD_SEPARATOR));
        segmentEnd.setText((String) props.get(LLPListenerProperties.LLP_SEGMENT_END));
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        if (((String) props.get(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER)).equals(UIConstants.YES_OPTION))
        {
            sendACKTransformerActionPerformed(null);
            sendACKTransformer.setSelected(true);
        }
        else
        {
            if (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.YES_OPTION))
            {
                sendACKYesActionPerformed(null);
                sendACKYes.setSelected(true);
            }
            else if (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.NO_OPTION))
            {
                sendACKNoActionPerformed(null);
                sendACKNo.setSelected(true);
            }
        }

        updateResponseDropDown();

        if (parent.channelEditPanel.synchronousCheckBox.isSelected())
            responseFromTransformer.setSelectedItem((String) props.getProperty(LLPListenerProperties.LLP_RESPONSE_VALUE));

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(LLPListenerProperties.CONNECTOR_CHARSET_ENCODING));

        successACKCode.setText((String) props.get(LLPListenerProperties.LLP_ACKCODE_SUCCESSFUL));
        successACKMessage.setText((String) props.get(LLPListenerProperties.LLP_ACKMSG_SUCCESSFUL));
        errorACKCode.setText((String) props.get(LLPListenerProperties.LLP_ACKCODE_ERROR));
        errorACKMessage.setText((String) props.get(LLPListenerProperties.LLP_ACKMSG_ERROR));
        rejectedACKCode.setText((String) props.get(LLPListenerProperties.LLP_ACKCODE_REJECTED));
        rejectedACKMessage.setText((String) props.get(LLPListenerProperties.LLP_ACKMSG_REJECTED));

        if (((String) props.get(LLPListenerProperties.LLP_ACK_MSH_15)).equals(UIConstants.YES_OPTION))
            mshAckAcceptYes.setSelected(true);
        else
            mshAckAcceptNo.setSelected(true);

        if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            ackOnNewConnectionYesActionPerformed(null);
            ackOnNewConnectionYes.setSelected(true);
        }
        else
        {
            ackOnNewConnectionNoActionPerformed(null);
            ackOnNewConnectionNo.setSelected(true);
        }

        ackAddressField.setText((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP));

        ackPortField.setText((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_PORT));

        if (((String) props.get(LLPListenerProperties.LLP_WAIT_FOR_END_OF_MESSAGE_CHAR)).equals(UIConstants.YES_OPTION))
            waitForEndOfMessageCharYes.setSelected(true);
        else
            waitForEndOfMessageCharNo.setSelected(true);

        if (((String) props.get(LLPListenerProperties.LLP_USE_STRICT_LLP)).equals(UIConstants.YES_OPTION))
        {
            useStrictLLPYesActionPerformed(null);
            useStrictLLPYes.setSelected(true);
        }
        else
        {
            useStrictLLPNoActionPerformed(null);
            useStrictLLPNo.setSelected(true);
        }

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        return new LLPListenerProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(LLPListenerProperties.LLP_ADDRESS)).length() == 0)
        {
            valid = false;
            if (highlight)
                listenerAddressField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_PORT)).length() == 0)
        {
            valid = false;
            if (highlight)
                listenerPortField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (clientRadioButton.isSelected() && ((String) props.get(LLPListenerProperties.LLP_RECONNECT_INTERVAL)).length() == 0)
        {
            valid = false;
            if (highlight)
                reconnectIntervalField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_RECEIVE_TIMEOUT)).length() == 0)
        {
            valid = false;
            if (highlight)
                receiveTimeoutField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_BUFFER_SIZE)).length() == 0)
        {
            valid = false;
            if (highlight)
                bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_END_OF_MESSAGE_CHARACTER)).length() == 0)
        {
            valid = false;
            if (highlight)
                endOfMessageCharacterField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_START_OF_MESSAGE_CHARACTER)).length() == 0)
        {
            valid = false;
            if (highlight)
                startOfMessageCharacterField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_RECORD_SEPARATOR)).length() == 0)
        {
            valid = false;
            if (highlight)
                recordSeparatorField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_SEGMENT_END)).length() == 0)
        {
            valid = false;
            if (highlight)
                segmentEnd.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.YES_OPTION))
        {
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_SUCCESSFUL)).length() == 0)
            {
                valid = false;
                if (highlight)
                    successACKCode.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_ERROR)).length() == 0)
            {
                valid = false;
                if (highlight)
                    errorACKCode.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_REJECTED)).length() == 0)
            {
                valid = false;
                if (highlight)
                    rejectedACKCode.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION)).equals(UIConstants.YES_OPTION) && (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.YES_OPTION) || ((String) props.get(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER)).equals(UIConstants.YES_OPTION)))
        {
            if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP)).length() == 0)
            {
                valid = false;
                if (highlight)
                    ackAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_PORT)).length() == 0)
            {
                valid = false;
                if (highlight)
                    ackPortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    private void resetInvalidProperties()
    {
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
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
        bufferSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        recordSeparatorField = new com.webreach.mirth.client.ui.components.MirthTextField();
        startOfMessageCharacterField = new com.webreach.mirth.client.ui.components.MirthTextField();
        endOfMessageCharacterField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        ascii = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        hex = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel37 = new javax.swing.JLabel();
        segmentEnd = new com.webreach.mirth.client.ui.components.MirthTextField();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel39 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        sendACKYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        sendACKNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        successACKCodeLabel = new javax.swing.JLabel();
        successACKCode = new com.webreach.mirth.client.ui.components.MirthTextField();
        successACKMessage = new com.webreach.mirth.client.ui.components.MirthTextField();
        successACKMessageLabel = new javax.swing.JLabel();
        errorACKCode = new com.webreach.mirth.client.ui.components.MirthTextField();
        errorACKCodeLabel = new javax.swing.JLabel();
        rejectedACKCode = new com.webreach.mirth.client.ui.components.MirthTextField();
        rejectedACKCodeLabel = new javax.swing.JLabel();
        rejectedACKMessageLabel = new javax.swing.JLabel();
        errorACKMessageLabel = new javax.swing.JLabel();
        errorACKMessage = new com.webreach.mirth.client.ui.components.MirthTextField();
        rejectedACKMessage = new com.webreach.mirth.client.ui.components.MirthTextField();
        mshAckAcceptLabel = new javax.swing.JLabel();
        mshAckAcceptYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        mshAckAcceptNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        ackOnNewConnectionLabel = new javax.swing.JLabel();
        ackOnNewConnectionYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        ackOnNewConnectionNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        ackIPLabel = new javax.swing.JLabel();
        ackPortLabel = new javax.swing.JLabel();
        ackPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        ackAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        sendACKTransformer = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        responseFromTransformer = new com.webreach.mirth.client.ui.components.MirthComboBox();
        waitForEndOfMessageCharLabel = new javax.swing.JLabel();
        waitForEndOfMessageCharYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        waitForEndOfMessageCharNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel8 = new javax.swing.JLabel();
        useStrictLLPYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        useStrictLLPNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        serverRadioButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        clientRadioButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        llpModeLabel = new javax.swing.JLabel();
        reconnectIntervalField = new com.webreach.mirth.client.ui.components.MirthTextField();
        reconnectIntervalLabel = new javax.swing.JLabel();
        processBatchYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        processBatchNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        addressLabel.setText("Listener Address:");

        portLabel.setText("Listener Port:");

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size (bytes):");

        jLabel34.setText("Start of Message Char:");

        jLabel35.setText("End of Message Char:");

        jLabel36.setText("Record Separator Char:");

        jLabel6.setText("LLP Frame Encoding:");

        ascii.setBackground(new java.awt.Color(255, 255, 255));
        ascii.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(ascii);
        ascii.setText("ASCII");
        ascii.setMargin(new java.awt.Insets(0, 0, 0, 0));

        hex.setBackground(new java.awt.Color(255, 255, 255));
        hex.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(hex);
        hex.setText("Hex");
        hex.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel37.setText("End of Segment Char:");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));

        jLabel39.setText("Encoding:");

        jLabel38.setText("Send ACK:");

        sendACKYes.setBackground(new java.awt.Color(255, 255, 255));
        sendACKYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(sendACKYes);
        sendACKYes.setText("Yes");
        sendACKYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sendACKYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sendACKYesActionPerformed(evt);
            }
        });

        sendACKNo.setBackground(new java.awt.Color(255, 255, 255));
        sendACKNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(sendACKNo);
        sendACKNo.setText("No");
        sendACKNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sendACKNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sendACKNoActionPerformed(evt);
            }
        });

        successACKCodeLabel.setText("Successful ACK Code:");

        successACKMessageLabel.setText("Message:");

        errorACKCodeLabel.setText("Error ACK Code:");

        rejectedACKCodeLabel.setText("Rejected ACK Code:");

        rejectedACKMessageLabel.setText("Message:");

        errorACKMessageLabel.setText("Message:");

        mshAckAcceptLabel.setText("MSH-15 ACK Accept:");

        mshAckAcceptYes.setBackground(new java.awt.Color(255, 255, 255));
        mshAckAcceptYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(mshAckAcceptYes);
        mshAckAcceptYes.setText("Yes");
        mshAckAcceptYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        mshAckAcceptNo.setBackground(new java.awt.Color(255, 255, 255));
        mshAckAcceptNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(mshAckAcceptNo);
        mshAckAcceptNo.setText("No");
        mshAckAcceptNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        ackOnNewConnectionLabel.setText("ACK on New Connection:");

        ackOnNewConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        ackOnNewConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(ackOnNewConnectionYes);
        ackOnNewConnectionYes.setText("Yes");
        ackOnNewConnectionYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ackOnNewConnectionYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ackOnNewConnectionYesActionPerformed(evt);
            }
        });

        ackOnNewConnectionNo.setBackground(new java.awt.Color(255, 255, 255));
        ackOnNewConnectionNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(ackOnNewConnectionNo);
        ackOnNewConnectionNo.setText("No");
        ackOnNewConnectionNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ackOnNewConnectionNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ackOnNewConnectionNoActionPerformed(evt);
            }
        });

        ackIPLabel.setText("ACK Address:");

        ackPortLabel.setText("ACK Port:");

        sendACKTransformer.setBackground(new java.awt.Color(255, 255, 255));
        sendACKTransformer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(sendACKTransformer);
        sendACKTransformer.setText("Respond from:");
        sendACKTransformer.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sendACKTransformer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sendACKTransformerActionPerformed(evt);
            }
        });

        responseFromTransformer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        responseFromTransformer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                responseFromTransformerActionPerformed(evt);
            }
        });

        waitForEndOfMessageCharLabel.setText("Wait for End of Message Char:");

        waitForEndOfMessageCharYes.setBackground(new java.awt.Color(255, 255, 255));
        waitForEndOfMessageCharYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(waitForEndOfMessageCharYes);
        waitForEndOfMessageCharYes.setText("Yes");
        waitForEndOfMessageCharYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        waitForEndOfMessageCharNo.setBackground(new java.awt.Color(255, 255, 255));
        waitForEndOfMessageCharNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(waitForEndOfMessageCharNo);
        waitForEndOfMessageCharNo.setText("No");
        waitForEndOfMessageCharNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel8.setText("Use Strict LLP Validation:");

        useStrictLLPYes.setBackground(new java.awt.Color(255, 255, 255));
        useStrictLLPYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(useStrictLLPYes);
        useStrictLLPYes.setText("Yes");
        useStrictLLPYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useStrictLLPYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                useStrictLLPYesActionPerformed(evt);
            }
        });

        useStrictLLPNo.setBackground(new java.awt.Color(255, 255, 255));
        useStrictLLPNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(useStrictLLPNo);
        useStrictLLPNo.setText("No");
        useStrictLLPNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useStrictLLPNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                useStrictLLPNoActionPerformed(evt);
            }
        });

        serverRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        serverRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serverClientButtonGroup.add(serverRadioButton);
        serverRadioButton.setText("Server");
        serverRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        serverRadioButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                serverRadioButtonActionPerformed(evt);
            }
        });

        clientRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        clientRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serverClientButtonGroup.add(clientRadioButton);
        clientRadioButton.setText("Client");
        clientRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clientRadioButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                clientRadioButtonActionPerformed(evt);
            }
        });

        llpModeLabel.setText("LLP Mode:");

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        processBatchYes.setBackground(new java.awt.Color(255, 255, 255));
        processBatchYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processBatchGroup.add(processBatchYes);
        processBatchYes.setText("Yes");
        processBatchYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processBatchNo.setBackground(new java.awt.Color(255, 255, 255));
        processBatchNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        processBatchGroup.add(processBatchNo);
        processBatchNo.setText("No");
        processBatchNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setText("Process Batch:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(10, 10, 10).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(jLabel4).add(jLabel6).add(jLabel34).add(jLabel36).add(jLabel8).add(waitForEndOfMessageCharLabel).add(jLabel39).add(jLabel38).add(successACKCodeLabel).add(errorACKCodeLabel).add(rejectedACKCodeLabel).add(mshAckAcceptLabel).add(ackOnNewConnectionLabel).add(ackIPLabel).add(ackPortLabel).add(jLabel3).add(portLabel).add(addressLabel).add(llpModeLabel).add(reconnectIntervalLabel).add(jLabel1)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(reconnectIntervalField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(layout.createSequentialGroup().add(serverRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(clientRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(listenerAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(layout.createSequentialGroup().add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel35).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(layout.createSequentialGroup().add(sendACKYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(sendACKNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(sendACKTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(responseFromTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(successACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(successACKMessageLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(successACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(errorACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(errorACKMessageLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(errorACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(rejectedACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(rejectedACKMessageLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(rejectedACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(mshAckAcceptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(mshAckAcceptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(ackPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(ackAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(layout.createSequentialGroup().add(ackOnNewConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(ackOnNewConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel37).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(segmentEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(waitForEndOfMessageCharYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(useStrictLLPYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(useStrictLLPNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(waitForEndOfMessageCharNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).add(layout.createSequentialGroup().add(ascii, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(processBatchYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(processBatchNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(serverRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(clientRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(llpModeLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(listenerAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(addressLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(portLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(reconnectIntervalField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(reconnectIntervalLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel3).add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel4).add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(processBatchYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(processBatchNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel1)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(ascii, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel6)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel34).add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel35)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel36).add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(segmentEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel37)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel8).add(useStrictLLPYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(useStrictLLPNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(waitForEndOfMessageCharLabel).add(waitForEndOfMessageCharYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(waitForEndOfMessageCharNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel39).add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel38).add(sendACKYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(sendACKNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(sendACKTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(responseFromTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(successACKCodeLabel).add(successACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(successACKMessageLabel).add(successACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(errorACKCodeLabel).add(errorACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(errorACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(errorACKMessageLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(rejectedACKCodeLabel).add(rejectedACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(rejectedACKMessageLabel).add(rejectedACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(mshAckAcceptLabel).add(mshAckAcceptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(mshAckAcceptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(ackOnNewConnectionLabel).add(ackOnNewConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(ackOnNewConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(ackAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(ackIPLabel)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(ackPortLabel).add(ackPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap()));
    }// </editor-fold>//GEN-END:initComponents

    private void clientRadioButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_clientRadioButtonActionPerformed
    {// GEN-HEADEREND:event_clientRadioButtonActionPerformed
        addressLabel.setText("Server Address");
        portLabel.setText("Server Port");
        reconnectIntervalField.setEnabled(true);
        reconnectIntervalLabel.setEnabled(true);
    }// GEN-LAST:event_clientRadioButtonActionPerformed

    private void serverRadioButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_serverRadioButtonActionPerformed
    {// GEN-HEADEREND:event_serverRadioButtonActionPerformed
        addressLabel.setText("Listener Address");
        portLabel.setText("Listener Port");
        reconnectIntervalField.setEnabled(false);
        reconnectIntervalLabel.setEnabled(false);
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
        if (responseFromTransformer.getSelectedIndex() != 0 && !parent.channelEditPanel.synchronousCheckBox.isSelected())
        {
            parent.alertInformation(this, "The synchronize source connector setting has been enabled since it is required to use this feature.");
            parent.channelEditPanel.synchronousCheckBox.setSelected(true);
        }
    }// GEN-LAST:event_responseFromTransformerActionPerformed

    public void updateResponseDropDown()
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        String selectedItem = (String) responseFromTransformer.getSelectedItem();

        Channel channel = parent.channelEditPanel.currentChannel;

        Set<String> variables = new LinkedHashSet<String>();

        variables.add("None");

        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getSteps());

        List<String> scripts = new ArrayList<String>();

        for (Connector connector : channel.getDestinationConnectors())
        {
            if (connector.getTransportName().equals("Database Writer"))
            {
                if (connector.getProperties().getProperty("useScript").equals(UIConstants.YES_OPTION))
                {
                    scripts.add(connector.getProperties().getProperty("script"));
                }

            }
            else if (connector.getTransportName().equals("JavaScript Writer"))
            {
                scripts.add(connector.getProperties().getProperty("script"));
            }

            variables.add(connector.getName());
            stepsToCheck.addAll(connector.getTransformer().getSteps());
        }

        Pattern pattern = Pattern.compile(RESULT_PATTERN);

        int i = 0;
        for (Iterator it = stepsToCheck.iterator(); it.hasNext();)
        {
            Step step = (Step) it.next();
            Map data;
            data = (Map) step.getData();

            if (step.getType().equalsIgnoreCase(TransformerPane.JAVASCRIPT_TYPE))
            {
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find())
                {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
            else if (step.getType().equalsIgnoreCase(TransformerPane.MAPPER_TYPE))
            {
                if (data.containsKey(UIConstants.IS_GLOBAL))
                {
                    if (((String) data.get(UIConstants.IS_GLOBAL)).equalsIgnoreCase(UIConstants.IS_GLOBAL_RESPONSE))
                        variables.add((String) data.get("Variable"));
                }
            }
        }

        scripts.add(channel.getPreprocessingScript());
        scripts.add(channel.getPostprocessingScript());

        for (String script : scripts)
        {
            if (script != null && script.length() > 0)
            {
                Matcher matcher = pattern.matcher(script);
                while (matcher.find())
                {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
        }

        responseFromTransformer.setModel(new DefaultComboBoxModel(variables.toArray()));

        if (variables.contains(selectedItem))
            responseFromTransformer.setSelectedItem(selectedItem);
        else
            responseFromTransformer.setSelectedIndex(0);

        if (!parent.channelEditPanel.synchronousCheckBox.isSelected())
        {
            responseFromTransformer.setEnabled(false);
            responseFromTransformer.setSelectedIndex(0);
        }
        else
        {
            responseFromTransformer.setEnabled(true);
        }

        // Reset the proper enabled fields if sendACKNo or sendACKYes were
        // selected.
        if (sendACKYes.isSelected())
            sendACKYesActionPerformed(null);
        else if (sendACKNo.isSelected())
            sendACKNoActionPerformed(null);

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
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

        if (ackOnNewConnectionYes.isSelected())
        {
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

        if (parent.channelEditPanel.synchronousCheckBox.isSelected())
        {
            errorACKCode.setEnabled(true);
            errorACKMessage.setEnabled(true);
            rejectedACKCode.setEnabled(true);
            rejectedACKMessage.setEnabled(true);
        }
        else
        {
            errorACKCode.setEnabled(false);
            errorACKMessage.setEnabled(false);
            rejectedACKCode.setEnabled(false);
            rejectedACKMessage.setEnabled(false);
        }

        successACKCodeLabel.setEnabled(true);
        successACKMessageLabel.setEnabled(true);

        if (parent.channelEditPanel.synchronousCheckBox.isSelected())
        {
            errorACKCodeLabel.setEnabled(true);
            errorACKMessageLabel.setEnabled(true);
            rejectedACKCodeLabel.setEnabled(true);
            rejectedACKMessageLabel.setEnabled(true);
        }
        else
        {
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

        if (ackOnNewConnectionYes.isSelected())
        {
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
    private com.webreach.mirth.client.ui.components.MirthTextField ackAddressField;
    private javax.swing.JLabel ackIPLabel;
    private javax.swing.JLabel ackOnNewConnectionLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton ackOnNewConnectionNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton ackOnNewConnectionYes;
    private com.webreach.mirth.client.ui.components.MirthTextField ackPortField;
    private javax.swing.JLabel ackPortLabel;
    private javax.swing.JLabel addressLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton ascii;
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthRadioButton clientRadioButton;
    private com.webreach.mirth.client.ui.components.MirthTextField endOfMessageCharacterField;
    private com.webreach.mirth.client.ui.components.MirthTextField errorACKCode;
    private javax.swing.JLabel errorACKCodeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField errorACKMessage;
    private javax.swing.JLabel errorACKMessageLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton hex;
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
    private com.webreach.mirth.client.ui.components.MirthTextField listenerAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerPortField;
    private javax.swing.JLabel llpModeLabel;
    private javax.swing.JLabel mshAckAcceptLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton mshAckAcceptNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton mshAckAcceptYes;
    private javax.swing.JLabel portLabel;
    private javax.swing.ButtonGroup processBatchGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton processBatchYes;
    private com.webreach.mirth.client.ui.components.MirthTextField receiveTimeoutField;
    private com.webreach.mirth.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField recordSeparatorField;
    private com.webreach.mirth.client.ui.components.MirthTextField rejectedACKCode;
    private javax.swing.JLabel rejectedACKCodeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField rejectedACKMessage;
    private javax.swing.JLabel rejectedACKMessageLabel;
    private com.webreach.mirth.client.ui.components.MirthComboBox responseFromTransformer;
    private com.webreach.mirth.client.ui.components.MirthTextField segmentEnd;
    private com.webreach.mirth.client.ui.components.MirthRadioButton sendACKNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton sendACKTransformer;
    private com.webreach.mirth.client.ui.components.MirthRadioButton sendACKYes;
    private javax.swing.ButtonGroup serverClientButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton serverRadioButton;
    private com.webreach.mirth.client.ui.components.MirthTextField startOfMessageCharacterField;
    private com.webreach.mirth.client.ui.components.MirthTextField successACKCode;
    private javax.swing.JLabel successACKCodeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField successACKMessage;
    private javax.swing.JLabel successACKMessageLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useStrictLLPNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useStrictLLPYes;
    private javax.swing.JLabel waitForEndOfMessageCharLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton waitForEndOfMessageCharNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton waitForEndOfMessageCharYes;
    // End of variables declaration//GEN-END:variables

}
