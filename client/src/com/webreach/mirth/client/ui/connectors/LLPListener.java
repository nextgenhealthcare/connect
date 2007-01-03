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


package com.webreach.mirth.client.ui.connectors;

import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import java.util.Properties;
import java.util.StringTokenizer;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class LLPListener extends ConnectorClass
{
    Frame parent;
    /** Creates new form LLPListener */
    public final String DATATYPE = "DataType";
    public final String LLP_PROTOCOL_NAME = "tcpProtocolClassName";
    public final String LLP_PROTOCOL_NAME_VALUE = "org.mule.providers.tcp.protocols.LlpProtocol";
    public final String LLP_ADDRESS = "host";
    public final String LLP_PORT = "port";
    public final String LLP_RECEIVE_TIMEOUT = "receiveTimeout";
    public final String LLP_BUFFER_SIZE = "bufferSize";
    public final String LLP_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";
    public final String LLP_CHAR_ENCODING = "charEncoding";
    public final String LLP_START_OF_MESSAGE_CHARACTER = "messageStart";
    public final String LLP_END_OF_MESSAGE_CHARACTER = "messageEnd";
    public final String LLP_RECORD_SEPARATOR = "recordSeparator";
    public final String LLP_SEND_ACK = "sendACK";
    public final String LLP_SEGMENT_END = "segmentEnd";
    public final String LLP_ACKCODE_SUCCESSFUL = "ackCodeSuccessful";
    public final String LLP_ACKMSG_SUCCESSFUL = "ackMsgSuccessful";
    public final String LLP_ACKCODE_ERROR = "ackCodeError";
    public final String LLP_ACKMSG_ERROR = "ackMsgError";
    public final String LLP_ACKCODE_REJECTED = "ackCodeRejected";
    public final String LLP_ACKMSG_REJECTED = "ackMsgRejected";
    public final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    
    public LLPListener()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "LLP Listener";
        initComponents();
        listenerIPAddressField.setDocument(new MirthFieldConstraints(3, false, true));
        listenerIPAddressField1.setDocument(new MirthFieldConstraints(3, false, true));
        listenerIPAddressField2.setDocument(new MirthFieldConstraints(3, false, true));
        listenerPortField.setDocument(new MirthFieldConstraints(5, false, true));
        receiveTimeoutField.setDocument(new MirthFieldConstraints(0, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, true));
        //ast:encoding activation
        parent.setupCharsetEncodingForChannel(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(LLP_PROTOCOL_NAME,LLP_PROTOCOL_NAME_VALUE);
        String listenerIPAddress = listenerIPAddressField.getText() + "." + listenerIPAddressField1.getText() + "." + listenerIPAddressField2.getText() + "." + listenerIPAddressField3.getText();
        properties.put(LLP_ADDRESS, listenerIPAddress);
        properties.put(LLP_PORT, listenerPortField.getText());
        properties.put(LLP_RECEIVE_TIMEOUT, receiveTimeoutField.getText());
        properties.put(LLP_BUFFER_SIZE, bufferSizeField.getText());

        if (keepConnectionOpenYesRadio.isSelected())
            properties.put(LLP_KEEP_CONNECTION_OPEN, UIConstants.YES_OPTION);
        else
            properties.put(LLP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);

        properties.put(LLP_START_OF_MESSAGE_CHARACTER, startOfMessageCharacterField.getText());
        properties.put(LLP_END_OF_MESSAGE_CHARACTER, endOfMessageCharacterField.getText());
        
        if (ascii.isSelected())
            properties.put(LLP_CHAR_ENCODING, "ascii");
        else
            properties.put(LLP_CHAR_ENCODING, "hex");
        
        properties.put(LLP_RECORD_SEPARATOR, recordSeparatorField.getText());
        properties.put(LLP_SEGMENT_END, segmentEnd.getText());
        
        if(sendACKYes.isSelected())
            properties.put(LLP_SEND_ACK, UIConstants.YES_OPTION);
        else
            properties.put(LLP_SEND_ACK, UIConstants.NO_OPTION);
        
        //ast:encoding        
        properties.put(CONNECTOR_CHARSET_ENCODING,parent.getSelectedEncodingForChannel(charsetEncodingCombobox));
        
        properties.put(LLP_ACKCODE_SUCCESSFUL, successACKCode.getText());
        properties.put(LLP_ACKMSG_SUCCESSFUL, successACKMessage.getText());
        properties.put(LLP_ACKCODE_ERROR, errorACKCode.getText());
        properties.put(LLP_ACKMSG_ERROR, errorACKMessage.getText());
        properties.put(LLP_ACKCODE_REJECTED, rejectedACKCode.getText());
        properties.put(LLP_ACKMSG_REJECTED, rejectedACKMessage.getText());
        
        return properties;
    }

    public void setProperties(Properties props)
    {
        String listenerIPAddress = (String)props.get(LLP_ADDRESS);
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

        listenerPortField.setText((String)props.get(LLP_PORT));
        receiveTimeoutField.setText((String)props.get(LLP_RECEIVE_TIMEOUT));
        bufferSizeField.setText((String)props.get(LLP_BUFFER_SIZE));

        if(((String)props.get(LLP_KEEP_CONNECTION_OPEN)).equals(UIConstants.YES_OPTION))
            keepConnectionOpenYesRadio.setSelected(true);
        else
            keepConnectionOpenNoRadio.setSelected(true);
        
        if(((String)props.get(LLP_CHAR_ENCODING)).equals("ascii"))
            ascii.setSelected(true);
        else
            hex.setSelected(true);
        
        startOfMessageCharacterField.setText((String)props.get(LLP_START_OF_MESSAGE_CHARACTER));
        endOfMessageCharacterField.setText((String)props.get(LLP_END_OF_MESSAGE_CHARACTER));
        recordSeparatorField.setText((String)props.get(LLP_RECORD_SEPARATOR));
        segmentEnd.setText((String)props.get(LLP_SEGMENT_END));
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        if(((String)props.get(LLP_SEND_ACK)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            sendACKYesActionPerformed(null);
            sendACKYes.setSelected(true);
        }
        else
        {
            sendACKNoActionPerformed(null);
            sendACKNo.setSelected(true);
        }
        //ast:encoding        
        parent.sePreviousSelectedEncodingForChannel(charsetEncodingCombobox,(String)props.get(CONNECTOR_CHARSET_ENCODING));
        
        successACKCode.setText((String)props.get(LLP_ACKCODE_SUCCESSFUL));
        successACKMessage.setText((String)props.get(LLP_ACKMSG_SUCCESSFUL));
        errorACKCode.setText((String)props.get(LLP_ACKCODE_ERROR));
        errorACKMessage.setText((String)props.get(LLP_ACKMSG_ERROR));
        rejectedACKCode.setText((String)props.get(LLP_ACKCODE_REJECTED));
        rejectedACKMessage.setText((String)props.get(LLP_ACKMSG_REJECTED)); 

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(LLP_PROTOCOL_NAME,LLP_PROTOCOL_NAME_VALUE);
        properties.put(LLP_ADDRESS, "127.0.0.1");
        properties.put(LLP_PORT, "6661");
        properties.put(LLP_RECEIVE_TIMEOUT, "5000");
        properties.put(LLP_BUFFER_SIZE, "65536");
        properties.put(LLP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);
        properties.put(LLP_CHAR_ENCODING, "hex");
        properties.put(LLP_START_OF_MESSAGE_CHARACTER, "0x0B");
        properties.put(LLP_END_OF_MESSAGE_CHARACTER, "0x1C");
        properties.put(LLP_RECORD_SEPARATOR, "0x0D");
        properties.put(LLP_SEGMENT_END, "0x0D");
        properties.put(LLP_SEND_ACK, UIConstants.YES_OPTION);
        properties.put(LLP_ACKCODE_SUCCESSFUL, "AA");
        properties.put(LLP_ACKMSG_SUCCESSFUL, "");
        properties.put(LLP_ACKCODE_ERROR, "AE");
        properties.put(LLP_ACKMSG_ERROR, "An Error Occured Processing Message.");
        properties.put(LLP_ACKCODE_REJECTED, "AR");
        properties.put(LLP_ACKMSG_REJECTED, "Message Rejected.");
        //ast:encoding        
        properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.DEFAULT_ENCODING_OPTION);
        return properties;
    }

    public boolean checkProperties(Properties props)
    {
         if(((String)props.get(LLP_ADDRESS)).length() > 0 && ((String)props.get(LLP_PORT)).length() > 0 && 
        ((String)props.get(LLP_RECEIVE_TIMEOUT)).length() > 0 && ((String)props.get(LLP_BUFFER_SIZE)).length() > 0 &&
        ((String)props.get(LLP_START_OF_MESSAGE_CHARACTER)).length() > 0 && ((String)props.get(LLP_END_OF_MESSAGE_CHARACTER)).length() > 0 &&
        ((String)props.get(LLP_RECORD_SEPARATOR)).length() > 0 && ((String)props.get(LLP_SEGMENT_END)).length() > 0)
            return true;
        return false;       
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        keepConnectionOpenGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        bufferSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        recordSeparatorField = new com.webreach.mirth.client.ui.components.MirthTextField();
        startOfMessageCharacterField = new com.webreach.mirth.client.ui.components.MirthTextField();
        endOfMessageCharacterField = new com.webreach.mirth.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "LLP Listener", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jLabel1.setText("Listener IP Address:");

        jLabel2.setText("Listener Port:");

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size (bytes):");

        jLabel5.setText("Keep Connection Open:");

        jLabel34.setText("Start of Message Char:");

        jLabel35.setText("End of Message Char:");

        jLabel36.setText("Record Separator Char:");

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, bufferSizeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, receiveTimeoutField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
                        .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
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
                        .add(129, 129, 129)
                        .add(segmentEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(ascii, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
                        .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
                        .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
                        .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
                        .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel34)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel6)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, rejectedACKCodeLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, errorACKCodeLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, successACKCodeLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel38)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel39)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel37)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel36)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel35))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rejectedACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(errorACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(successACKCode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(successACKMessageLabel)
                            .add(errorACKMessageLabel)
                            .add(rejectedACKMessageLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(successACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(errorACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(rejectedACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(129, 129, 129)
                        .add(sendACKYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sendACKNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
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
                    .add(jLabel5)
                    .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ascii, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel34)
                    .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel35))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel36))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(segmentEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel37))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel39)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel38)
                    .add(sendACKYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendACKNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                    .add(rejectedACKMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rejectedACKMessageLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sendACKYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sendACKYesActionPerformed
    {//GEN-HEADEREND:event_sendACKYesActionPerformed
        if(evt != null && !PlatformUI.MIRTH_FRAME.channelEditPanel.synchronousCheckBox.isSelected())
        {
            PlatformUI.MIRTH_FRAME.alertInformation("The synchronize source connector setting has been enabled since it is required to use this feature.");
            PlatformUI.MIRTH_FRAME.channelEditPanel.synchronousCheckBox.setSelected(true);
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
        
    }//GEN-LAST:event_sendACKYesActionPerformed

    private void sendACKNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sendACKNoActionPerformed
    {//GEN-HEADEREND:event_sendACKNoActionPerformed
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
    }//GEN-LAST:event_sendACKNoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthRadioButton ascii;
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField endOfMessageCharacterField;
    private com.webreach.mirth.client.ui.components.MirthTextField errorACKCode;
    private javax.swing.JLabel errorACKCodeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField errorACKMessage;
    private javax.swing.JLabel errorACKMessageLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton hex;
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
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField1;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField2;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerIPAddressField3;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerPortField;
    private com.webreach.mirth.client.ui.components.MirthTextField receiveTimeoutField;
    private com.webreach.mirth.client.ui.components.MirthTextField recordSeparatorField;
    private com.webreach.mirth.client.ui.components.MirthTextField rejectedACKCode;
    private javax.swing.JLabel rejectedACKCodeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField rejectedACKMessage;
    private javax.swing.JLabel rejectedACKMessageLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField segmentEnd;
    private com.webreach.mirth.client.ui.components.MirthRadioButton sendACKNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton sendACKYes;
    private com.webreach.mirth.client.ui.components.MirthTextField startOfMessageCharacterField;
    private com.webreach.mirth.client.ui.components.MirthTextField successACKCode;
    private javax.swing.JLabel successACKCodeLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField successACKMessage;
    private javax.swing.JLabel successACKMessageLabel;
    // End of variables declaration//GEN-END:variables

}
