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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
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
        listenerIPAddressField.setDocument(new MirthFieldConstraints(3, false, false, true));
        listenerIPAddressField1.setDocument(new MirthFieldConstraints(3, false, false, true));
        listenerIPAddressField2.setDocument(new MirthFieldConstraints(3, false, false, true));
        listenerPortField.setDocument(new MirthFieldConstraints(5, false, false, true));
        receiveTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        parent.setupCharsetEncodingForChannel(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(LLPListenerProperties.DATATYPE, name);
        properties.put(LLPListenerProperties.LLP_PROTOCOL_NAME, LLPListenerProperties.LLP_PROTOCOL_NAME_VALUE);
        String listenerIPAddress = listenerIPAddressField.getText() + "." + listenerIPAddressField1.getText() + "." + listenerIPAddressField2.getText() + "." + listenerIPAddressField3.getText();
        properties.put(LLPListenerProperties.LLP_ADDRESS, listenerIPAddress);
        properties.put(LLPListenerProperties.LLP_PORT, listenerPortField.getText());
        properties.put(LLPListenerProperties.LLP_RECEIVE_TIMEOUT, receiveTimeoutField.getText());
        properties.put(LLPListenerProperties.LLP_BUFFER_SIZE, bufferSizeField.getText());

        properties.put(LLPListenerProperties.LLP_START_OF_MESSAGE_CHARACTER, startOfMessageCharacterField.getText());
        properties.put(LLPListenerProperties.LLP_END_OF_MESSAGE_CHARACTER, endOfMessageCharacterField.getText());

        if (ascii.isSelected())
            properties.put(LLPListenerProperties.LLP_CHAR_ENCODING, "ascii");
        else
            properties.put(LLPListenerProperties.LLP_CHAR_ENCODING, "hex");

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
            properties.put(LLPListenerProperties.LLP_RESPONSE_VALUE, (String)responseFromTransformer.getSelectedItem());
        }


        properties.put(LLPListenerProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForChannel(charsetEncodingCombobox));
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

        String ackIPAddress = ackIPAddressField.getText() + "." + ackIPAddressField1.getText() + "." + ackIPAddressField2.getText() + "." + ackIPAddressField3.getText();
        properties.put(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP, ackIPAddress);
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
        
        String listenerIPAddress = (String) props.get(LLPListenerProperties.LLP_ADDRESS);
        StringTokenizer IP = new StringTokenizer(listenerIPAddress, ".");
        if (IP.hasMoreTokens())
            listenerIPAddressField.setText(IP.nextToken());
        else
            listenerIPAddressField.setText("");
        if (IP.hasMoreTokens())
            listenerIPAddressField1.setText(IP.nextToken());
        else
            listenerIPAddressField1.setText("");
        if (IP.hasMoreTokens())
            listenerIPAddressField2.setText(IP.nextToken());
        else
            listenerIPAddressField2.setText("");
        if (IP.hasMoreTokens())
            listenerIPAddressField3.setText(IP.nextToken());
        else
            listenerIPAddressField3.setText("");

        listenerPortField.setText((String) props.get(LLPListenerProperties.LLP_PORT));
        receiveTimeoutField.setText((String) props.get(LLPListenerProperties.LLP_RECEIVE_TIMEOUT));
        bufferSizeField.setText((String) props.get(LLPListenerProperties.LLP_BUFFER_SIZE));

        if (((String) props.get(LLPListenerProperties.LLP_CHAR_ENCODING)).equals("ascii"))
            ascii.setSelected(true);
        else
            hex.setSelected(true);

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
        responseFromTransformer.setSelectedItem((String) props.getProperty(LLPListenerProperties.LLP_RESPONSE_VALUE));
        
        parent.sePreviousSelectedEncodingForChannel(charsetEncodingCombobox, (String) props.get(LLPListenerProperties.CONNECTOR_CHARSET_ENCODING));

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

        String ackIPAddress = (String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP);
        StringTokenizer ackIP = new StringTokenizer(ackIPAddress, ".");
        if (ackIP.hasMoreTokens())
            ackIPAddressField.setText(ackIP.nextToken());
        else
            ackIPAddressField.setText("");
        if (ackIP.hasMoreTokens())
            ackIPAddressField1.setText(ackIP.nextToken());
        else
            ackIPAddressField1.setText("");
        if (ackIP.hasMoreTokens())
            ackIPAddressField2.setText(ackIP.nextToken());
        else
            ackIPAddressField2.setText("");
        if (ackIP.hasMoreTokens())
            ackIPAddressField3.setText(ackIP.nextToken());
        else
            ackIPAddressField3.setText("");

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

    public boolean checkProperties(Properties props)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(LLPListenerProperties.LLP_ADDRESS)).length() <= 3)
        {
            valid = false;
            listenerIPAddressField.setBackground(UIConstants.INVALID_COLOR);
            listenerIPAddressField1.setBackground(UIConstants.INVALID_COLOR);
            listenerIPAddressField2.setBackground(UIConstants.INVALID_COLOR);
            listenerIPAddressField3.setBackground(UIConstants.INVALID_COLOR);            
        }
        if (((String) props.get(LLPListenerProperties.LLP_PORT)).length() == 0)
        {
            valid = false;
            listenerPortField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_RECEIVE_TIMEOUT)).length() == 0)
        {
            valid = false;
            receiveTimeoutField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_BUFFER_SIZE)).length() == 0)
        {
            valid = false;
            bufferSizeField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_END_OF_MESSAGE_CHARACTER)).length() == 0)
        {
            valid = false;
            endOfMessageCharacterField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_START_OF_MESSAGE_CHARACTER)).length() == 0)
        {
            valid = false;
            startOfMessageCharacterField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_RECORD_SEPARATOR)).length() == 0)
        {
            valid = false;
            recordSeparatorField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_SEGMENT_END)).length() == 0)
        {
            valid = false;
            segmentEnd.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.YES_OPTION))
        {
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_SUCCESSFUL)).length() == 0)
            {
                valid = false;
                successACKCode.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_ERROR)).length() == 0)
            {
                valid = false;
                errorACKCode.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACKCODE_REJECTED)).length() == 0)
            {
                valid = false;
                rejectedACKCode.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION)).equals(UIConstants.YES_OPTION) && (((String) props.get(LLPListenerProperties.LLP_SEND_ACK)).equals(UIConstants.YES_OPTION) || ((String) props.get(LLPListenerProperties.LLP_RESPONSE_FROM_TRANSFORMER)).equals(UIConstants.YES_OPTION)))
        {
            if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_IP)).length() <= 3)
            {
                valid = false;
                ackIPAddressField.setBackground(UIConstants.INVALID_COLOR);
                ackIPAddressField1.setBackground(UIConstants.INVALID_COLOR);
                ackIPAddressField2.setBackground(UIConstants.INVALID_COLOR);
                ackIPAddressField3.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(LLPListenerProperties.LLP_ACK_NEW_CONNECTION_PORT)).length() == 0)
            {
                valid = false;
                ackPortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        listenerIPAddressField.setBackground(null);
        listenerIPAddressField1.setBackground(null);
        listenerIPAddressField2.setBackground(null);
        listenerIPAddressField3.setBackground(null);
        listenerPortField.setBackground(null);
        receiveTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
        endOfMessageCharacterField.setBackground(null);
        startOfMessageCharacterField.setBackground(null);
        recordSeparatorField.setBackground(null);
        segmentEnd.setBackground(null);
        successACKCode.setBackground(null);
        errorACKCode.setBackground(null);
        rejectedACKCode.setBackground(null);
        ackIPAddressField.setBackground(null);
        ackIPAddressField1.setBackground(null);
        ackIPAddressField2.setBackground(null);
        ackIPAddressField3.setBackground(null);
        ackPortField.setBackground(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
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
        listenerIPAddressField3 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel25 = new javax.swing.JLabel();
        listenerIPAddressField2 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel26 = new javax.swing.JLabel();
        listenerIPAddressField1 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel9 = new javax.swing.JLabel();
        listenerIPAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
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
        ackIPAddressField1 = new com.webreach.mirth.client.ui.components.MirthTextField();
        ackPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        ipDot1 = new javax.swing.JLabel();
        ackIPAddressField3 = new com.webreach.mirth.client.ui.components.MirthTextField();
        ipDot2 = new javax.swing.JLabel();
        ackIPAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        ackIPAddressField2 = new com.webreach.mirth.client.ui.components.MirthTextField();
        ipDot = new javax.swing.JLabel();
        sendACKTransformer = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        responseFromTransformer = new com.webreach.mirth.client.ui.components.MirthComboBox();
        waitForEndOfMessageCharLabel = new javax.swing.JLabel();
        waitForEndOfMessageCharYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        waitForEndOfMessageCharNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel8 = new javax.swing.JLabel();
        useStrictLLPYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        useStrictLLPNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("Listener IP Address:");

        jLabel2.setText("Listener Port:");

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size (bytes):");

        jLabel34.setText("Start of Message Char:");

        jLabel35.setText("End of Message Char:");

        jLabel36.setText("Record Separator Char:");

        jLabel25.setText(".");

        jLabel26.setText(".");

        jLabel9.setText(".");

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

        ackIPLabel.setText("ACK IP Address:");

        ackPortLabel.setText("ACK Port:");

        ipDot1.setText(".");

        ipDot2.setText(".");

        ipDot.setText(".");

        sendACKTransformer.setBackground(new java.awt.Color(255, 255, 255));
        sendACKTransformer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(sendACKTransformer);
        sendACKTransformer.setText("Response from");
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel6)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel34)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel36)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, waitForEndOfMessageCharLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel39)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel38)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, successACKCodeLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, errorACKCodeLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, rejectedACKCodeLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mshAckAcceptLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, ackOnNewConnectionLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, ackIPLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, ackPortLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(listenerIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel9)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listenerIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel26)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listenerIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel25)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listenerIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(ascii, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel35)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(sendACKYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sendACKNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sendACKTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(responseFromTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(successACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(successACKMessageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(successACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(errorACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(errorACKMessageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(errorACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(rejectedACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rejectedACKMessageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rejectedACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(mshAckAcceptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mshAckAcceptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(ackPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(ackIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(ipDot)
                        .add(4, 4, 4)
                        .add(ackIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(ipDot1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ackIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ipDot2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ackIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(ackOnNewConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ackOnNewConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel37)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(segmentEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(waitForEndOfMessageCharYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(useStrictLLPYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(useStrictLLPNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(waitForEndOfMessageCharNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(listenerIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(listenerIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(listenerIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel26)
                    .add(jLabel25)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(listenerIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel1))
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ascii, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel34)
                    .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel35))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel36)
                    .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(segmentEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel37))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(useStrictLLPYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(useStrictLLPNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(waitForEndOfMessageCharLabel)
                    .add(waitForEndOfMessageCharYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(waitForEndOfMessageCharNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel39)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel38)
                    .add(sendACKYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendACKNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendACKTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(responseFromTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(successACKCodeLabel)
                    .add(successACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(successACKMessageLabel)
                    .add(successACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(errorACKCodeLabel)
                    .add(errorACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(errorACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(errorACKMessageLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rejectedACKCodeLabel)
                    .add(rejectedACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rejectedACKMessageLabel)
                    .add(rejectedACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mshAckAcceptLabel)
                    .add(mshAckAcceptYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mshAckAcceptNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ackOnNewConnectionLabel)
                    .add(ackOnNewConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ackOnNewConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(ackIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(ackIPLabel))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(ipDot))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, ackIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(ipDot1)
                    .add(ackIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ackIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ipDot2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ackPortLabel)
                    .add(ackPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void useStrictLLPNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useStrictLLPNoActionPerformed
    {//GEN-HEADEREND:event_useStrictLLPNoActionPerformed
        waitForEndOfMessageCharLabel.setEnabled(true);
        waitForEndOfMessageCharYes.setEnabled(true);
        waitForEndOfMessageCharNo.setEnabled(true);
    }//GEN-LAST:event_useStrictLLPNoActionPerformed

    private void useStrictLLPYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_useStrictLLPYesActionPerformed
    {//GEN-HEADEREND:event_useStrictLLPYesActionPerformed
        waitForEndOfMessageCharLabel.setEnabled(false);
        waitForEndOfMessageCharYes.setEnabled(false);
        waitForEndOfMessageCharNo.setEnabled(false);
        waitForEndOfMessageCharNo.setSelected(true);
    }//GEN-LAST:event_useStrictLLPYesActionPerformed

    private void responseFromTransformerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_responseFromTransformerActionPerformed
    {//GEN-HEADEREND:event_responseFromTransformerActionPerformed
        if (responseFromTransformer.getSelectedIndex() != 0 && !parent.channelEditPanel.synchronousCheckBox.isSelected())
        {
            parent.alertInformation("The synchronize source connector setting has been enabled since it is required to use this feature.");
            parent.channelEditPanel.synchronousCheckBox.setSelected(true);
        }
    }//GEN-LAST:event_responseFromTransformerActionPerformed
    
    public void updateResponseDropDown()
    {
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();
        
        String selectedItem = (String) responseFromTransformer.getSelectedItem();
        
        Channel channel = parent.channelEditPanel.currentChannel;
        
        ArrayList<String> variables = new ArrayList<String>();
        
        variables.add("None");
        
        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getSteps());      
        
        for(Connector connector : channel.getDestinationConnectors())
        {
            variables.add(connector.getName());
            stepsToCheck.addAll(connector.getTransformer().getSteps());
        }       
               
        int i = 0;
        for (Iterator it = stepsToCheck.iterator(); it.hasNext();)
        {
            Step step = (Step) it.next();
            Map data;
            data = (Map) step.getData();
            
            if (step.getType().equalsIgnoreCase(TransformerPane.JAVASCRIPT_TYPE))
            {
                Pattern pattern = Pattern.compile(RESULT_PATTERN);
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find())
                {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
            else if (step.getType().equalsIgnoreCase(TransformerPane.MAPPER_TYPE))
            {
                if(data.containsKey(UIConstants.IS_GLOBAL))
                {
                    if (((String) data.get(UIConstants.IS_GLOBAL)).equalsIgnoreCase(UIConstants.IS_GLOBAL_RESPONSE))
                        variables.add((String)data.get("Variable"));
                }
            }
        }
        
        responseFromTransformer.setModel(new DefaultComboBoxModel(variables.toArray()));
        
        if(variables.contains(selectedItem))
            responseFromTransformer.setSelectedItem(selectedItem);
        else
            responseFromTransformer.setSelectedIndex(0);
        
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
            ackIPAddressField.setEnabled(true);
            ackIPAddressField1.setEnabled(true);
            ackIPAddressField2.setEnabled(true);
            ackIPAddressField3.setEnabled(true);
            ackPortField.setEnabled(true);

            ipDot.setEnabled(true);
            ipDot1.setEnabled(true);
            ipDot2.setEnabled(true);
            ackIPLabel.setEnabled(true);
            ackPortLabel.setEnabled(true);
        }
        responseFromTransformer.setEnabled(true);
        updateResponseDropDown();
    }// GEN-LAST:event_sendACKTransformerActionPerformed

    private void ackOnNewConnectionNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionNoActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionNoActionPerformed
        ackIPAddressField.setEnabled(false);
        ackIPAddressField1.setEnabled(false);
        ackIPAddressField2.setEnabled(false);
        ackIPAddressField3.setEnabled(false);
        ackPortField.setEnabled(false);

        ipDot.setEnabled(false);
        ipDot1.setEnabled(false);
        ipDot2.setEnabled(false);
        ackIPLabel.setEnabled(false);
        ackPortLabel.setEnabled(false);
    }// GEN-LAST:event_ackOnNewConnectionNoActionPerformed

    private void ackOnNewConnectionYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_ackOnNewConnectionYesActionPerformed
    {// GEN-HEADEREND:event_ackOnNewConnectionYesActionPerformed
        ackIPAddressField.setEnabled(true);
        ackIPAddressField1.setEnabled(true);
        ackIPAddressField2.setEnabled(true);
        ackIPAddressField3.setEnabled(true);
        ackPortField.setEnabled(true);

        ipDot.setEnabled(true);
        ipDot1.setEnabled(true);
        ipDot2.setEnabled(true);
        ackIPLabel.setEnabled(true);
        ackPortLabel.setEnabled(true);
    }// GEN-LAST:event_ackOnNewConnectionYesActionPerformed

    private void sendACKYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_sendACKYesActionPerformed
    {// GEN-HEADEREND:event_sendACKYesActionPerformed
        if (evt != null && !parent.channelEditPanel.synchronousCheckBox.isSelected())
        {
            parent.alertInformation("The synchronize source connector setting has been enabled since it is required to use this feature.");
            parent.channelEditPanel.synchronousCheckBox.setSelected(true);
        }

        successACKCode.setEnabled(true);
        successACKMessage.setEnabled(true);
        errorACKCode.setEnabled(true);
        errorACKMessage.setEnabled(true);
        rejectedACKCode.setEnabled(true);
        rejectedACKMessage.setEnabled(true);

        successACKCodeLabel.setEnabled(true);
        successACKMessageLabel.setEnabled(true);
        errorACKCodeLabel.setEnabled(true);
        errorACKMessageLabel.setEnabled(true);
        rejectedACKCodeLabel.setEnabled(true);
        rejectedACKMessageLabel.setEnabled(true);

        ackOnNewConnectionNo.setEnabled(true);
        ackOnNewConnectionYes.setEnabled(true);
        ackOnNewConnectionLabel.setEnabled(true);
        mshAckAcceptNo.setEnabled(true);
        mshAckAcceptYes.setEnabled(true);
        mshAckAcceptLabel.setEnabled(true);

        if (ackOnNewConnectionYes.isSelected())
        {
            ackIPAddressField.setEnabled(true);
            ackIPAddressField1.setEnabled(true);
            ackIPAddressField2.setEnabled(true);
            ackIPAddressField3.setEnabled(true);
            ackPortField.setEnabled(true);

            ipDot.setEnabled(true);
            ipDot1.setEnabled(true);
            ipDot2.setEnabled(true);
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

        ackIPAddressField.setEnabled(false);
        ackIPAddressField1.setEnabled(false);
        ackIPAddressField2.setEnabled(false);
        ackIPAddressField3.setEnabled(false);
        ackPortField.setEnabled(false);

        ipDot.setEnabled(false);
        ipDot1.setEnabled(false);
        ipDot2.setEnabled(false);
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
    private com.webreach.mirth.client.ui.components.MirthTextField ackIPAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField ackIPAddressField1;
    private com.webreach.mirth.client.ui.components.MirthTextField ackIPAddressField2;
    private com.webreach.mirth.client.ui.components.MirthTextField ackIPAddressField3;
    private javax.swing.JLabel ackIPLabel;
    private javax.swing.JLabel ackOnNewConnectionLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton ackOnNewConnectionNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton ackOnNewConnectionYes;
    private com.webreach.mirth.client.ui.components.MirthTextField ackPortField;
    private javax.swing.JLabel ackPortLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton ascii;
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField endOfMessageCharacterField;
    private com.webreach.mirth.client.ui.components.MirthTextField errorACKCode;
    private javax.swing.JLabel errorACKCodeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField errorACKMessage;
    private javax.swing.JLabel errorACKMessageLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton hex;
    private javax.swing.JLabel ipDot;
    private javax.swing.JLabel ipDot1;
    private javax.swing.JLabel ipDot2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
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
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField1;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField2;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField3;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerPortField;
    private javax.swing.JLabel mshAckAcceptLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton mshAckAcceptNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton mshAckAcceptYes;
    private com.webreach.mirth.client.ui.components.MirthTextField receiveTimeoutField;
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
