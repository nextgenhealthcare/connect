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
    //ast: encodign
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
        if(((String)sendACKCombobox.getSelectedItem()).equalsIgnoreCase("YES"))
            properties.put(LLP_SEND_ACK, UIConstants.YES_OPTION);
        else
            properties.put(LLP_SEND_ACK, UIConstants.NO_OPTION);
        //ast:encoding        
        properties.put(CONNECTOR_CHARSET_ENCODING,parent.
                        getSelectedEncodingForChannel(charsetEncodingCombobox));        
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
            sendACKCombobox.setSelectedItem("Yes");
        else
            sendACKCombobox.setSelectedItem("No");
        //ast:encoding        
        parent.sePreviousSelectedEncodingForChannel(charsetEncodingCombobox,
                        (String)props.get(CONNECTOR_CHARSET_ENCODING));

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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        sendACKCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
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

        jLabel38.setText("Send ACK:");

        sendACKCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Yes", "No" }));

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

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));

        jLabel39.setText("Encoding:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel3)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel5)
                    .add(jLabel34)
                    .add(jLabel35)
                    .add(jLabel36)
                    .add(jLabel4)
                    .add(jLabel6)
                    .add(jLabel38)
                    .add(jLabel37)
                    .add(jLabel39))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(recordSeparatorField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(endOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(startOfMessageCharacterField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(ascii, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(hex, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(sendACKCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(segmentEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, bufferSizeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, receiveTimeoutField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(listenerIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel9)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(listenerIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel26)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listenerIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel25)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(listenerIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(104, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(listenerIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(listenerIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel1))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(listenerIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(listenerIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel9)
                    .add(jLabel26)
                    .add(jLabel25))
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
                    .add(sendACKCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel38))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel39))
                .addContainerGap(67, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthRadioButton ascii;
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField endOfMessageCharacterField;
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
    private com.webreach.mirth.client.ui.components.MirthTextField segmentEnd;
    private com.webreach.mirth.client.ui.components.MirthComboBox sendACKCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField startOfMessageCharacterField;
    // End of variables declaration//GEN-END:variables

}
