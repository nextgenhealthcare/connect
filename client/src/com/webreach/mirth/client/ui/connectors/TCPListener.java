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

import java.util.Properties;
import java.util.StringTokenizer;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;

/**
 * A form that extends from ConnectorClass.  All methods implemented
 * are described in ConnectorClass.
 */
public class TCPListener extends ConnectorClass
{
    Frame parent;
    /** Creates new form TCPListener */
    private final String DATATYPE = "DataType";
    private final String TCP_ADDRESS = "host";
    private final String TCP_PORT = "port";
    private final String TCP_RECEIVE_TIMEOUT = "receiveTimeout";
    private final String TCP_BUFFER_SIZE = "bufferSize";
    private final String TCP_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";
    private final String TCP_CHAR_ENCODING = "charEncoding";
    private final String TCP_SEND_ACK = "sendACK";
    private final String TCP_ACK_NEW_CONNECTION = "ackOnNewConnection";
    private final String TCP_ACK_NEW_CONNECTION_IP = "ackIP";
    private final String TCP_ACK_NEW_CONNECTION_PORT = "ackPort";
    private final String TCP_RESPONSE_FROM_TRANSFORMER = "responseFromTransformer";
    private final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";
    
    public TCPListener()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "TCP Listener";
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
        String listenerIPAddress = listenerIPAddressField.getText() + "." + listenerIPAddressField1.getText() + "." + listenerIPAddressField2.getText() + "." + listenerIPAddressField3.getText();
        properties.put(TCP_ADDRESS, listenerIPAddress);
        properties.put(TCP_PORT, listenerPortField.getText());
        properties.put(TCP_RECEIVE_TIMEOUT, receiveTimeoutField.getText());
        properties.put(TCP_BUFFER_SIZE, bufferSizeField.getText());

        if (keepConnectionOpenYesRadio.isSelected())
            properties.put(TCP_KEEP_CONNECTION_OPEN, UIConstants.YES_OPTION);
        else
            properties.put(TCP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);
      
        if(sendACKYes.isSelected())
        {
            properties.put(TCP_SEND_ACK, UIConstants.YES_OPTION);
            properties.put(TCP_RESPONSE_FROM_TRANSFORMER, UIConstants.NO_OPTION);
        }
        else if(sendACKNo.isSelected())
        {
            properties.put(TCP_SEND_ACK, UIConstants.NO_OPTION);
            properties.put(TCP_RESPONSE_FROM_TRANSFORMER, UIConstants.NO_OPTION);
        }
        else if(sendACKTransformer.isSelected())
        {
            properties.put(TCP_RESPONSE_FROM_TRANSFORMER, UIConstants.YES_OPTION);
            properties.put(TCP_SEND_ACK, UIConstants.NO_OPTION);
        }
        
        //ast:encoding        
        properties.put(CONNECTOR_CHARSET_ENCODING,parent.getSelectedEncodingForChannel(charsetEncodingCombobox));
              
        if(ackOnNewConnectionYes.isSelected())
            properties.put(TCP_ACK_NEW_CONNECTION, UIConstants.YES_OPTION);
        else
            properties.put(TCP_ACK_NEW_CONNECTION, UIConstants.NO_OPTION);
        
        String ackIPAddress = ackIPAddressField.getText() + "." + ackIPAddressField1.getText() + "." + ackIPAddressField2.getText() + "." + ackIPAddressField3.getText();
        properties.put(TCP_ACK_NEW_CONNECTION_IP, ackIPAddress);
        properties.put(TCP_ACK_NEW_CONNECTION_PORT, ackPortField.getText());
        return properties;
    }

    public void setProperties(Properties props)
    {
        String listenerIPAddress = (String)props.get(TCP_ADDRESS);
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

        listenerPortField.setText((String)props.get(TCP_PORT));
        receiveTimeoutField.setText((String)props.get(TCP_RECEIVE_TIMEOUT));
        bufferSizeField.setText((String)props.get(TCP_BUFFER_SIZE));

        if(((String)props.get(TCP_KEEP_CONNECTION_OPEN)).equals(UIConstants.YES_OPTION))
            keepConnectionOpenYesRadio.setSelected(true);
        else
            keepConnectionOpenNoRadio.setSelected(true);
        
        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        if(((String)props.get(TCP_SEND_ACK)).equals(UIConstants.YES_OPTION))
        {
            sendACKYesActionPerformed(null);
            sendACKYes.setSelected(true);
        }
        else if(((String)props.get(TCP_SEND_ACK)).equals(UIConstants.NO_OPTION))
        {
            sendACKNoActionPerformed(null);
            sendACKNo.setSelected(true);
        }
        
        if(((String)props.get(TCP_RESPONSE_FROM_TRANSFORMER)).equals(UIConstants.NO_OPTION))
        {
            sendACKTransformerActionPerformed(null);
            sendACKTransformer.setSelected(true);
        }
        
        //ast:encoding        
        parent.sePreviousSelectedEncodingForChannel(charsetEncodingCombobox,(String)props.get(CONNECTOR_CHARSET_ENCODING));
        
        if(((String)props.get(TCP_ACK_NEW_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            ackOnNewConnectionYesActionPerformed(null);
            ackOnNewConnectionYes.setSelected(true);
        }
        else
        {
            ackOnNewConnectionNoActionPerformed(null);
            ackOnNewConnectionNo.setSelected(true);
        }
        
        String ackIPAddress = (String)props.get(TCP_ACK_NEW_CONNECTION_IP);
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
        
        ackPortField.setText((String)props.get(TCP_ACK_NEW_CONNECTION_PORT)); 
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(TCP_ADDRESS, "127.0.0.1");
        properties.put(TCP_PORT, "6661");
        properties.put(TCP_RECEIVE_TIMEOUT, "5000");
        properties.put(TCP_BUFFER_SIZE, "65536");
        properties.put(TCP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);
        properties.put(TCP_ACK_NEW_CONNECTION, UIConstants.NO_OPTION);
        properties.put(TCP_ACK_NEW_CONNECTION_IP, "..."); // hack to get around defaults
        properties.put(TCP_ACK_NEW_CONNECTION_PORT, "");
        properties.put(TCP_RESPONSE_FROM_TRANSFORMER, UIConstants.NO_OPTION);
        //ast:encoding        
        properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.DEFAULT_ENCODING_OPTION);
        return properties;
    }

    public boolean checkProperties(Properties props)
    {
        if(((String)props.get(TCP_ACK_NEW_CONNECTION)).equals(UIConstants.YES_OPTION) && (((String)props.get(TCP_ACK_NEW_CONNECTION_IP)).length() == 0 || ((String)props.get(TCP_ACK_NEW_CONNECTION_PORT)).length() == 0))
            return false;
        
        if(((String)props.get(TCP_ADDRESS)).length() > 0 && ((String)props.get(TCP_PORT)).length() > 0 && 
        ((String)props.get(TCP_RECEIVE_TIMEOUT)).length() > 0 && ((String)props.get(TCP_BUFFER_SIZE)).length() > 0)
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
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        bufferSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        listenerIPAddressField3 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel25 = new javax.swing.JLabel();
        listenerIPAddressField2 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel26 = new javax.swing.JLabel();
        listenerIPAddressField1 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel9 = new javax.swing.JLabel();
        listenerIPAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel39 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        sendACKYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        sendACKNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("Listener IP Address:");

        jLabel2.setText("Listener Port:");

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size (bytes):");

        jLabel5.setText("Keep Connection Open:");

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
        sendACKTransformer.setText("Response from Transformer");
        sendACKTransformer.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sendACKTransformer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sendACKTransformerActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(ackPortLabel)
                    .add(ackIPLabel)
                    .add(ackOnNewConnectionLabel)
                    .add(jLabel38)
                    .add(jLabel39)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel3)
                    .add(jLabel5)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 160, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 160, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(sendACKYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sendACKNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sendACKTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(ackOnNewConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ackOnNewConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                    .add(ackPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                    .add(jLabel39)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel38)
                    .add(sendACKYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendACKNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendACKTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ackOnNewConnectionLabel)
                    .add(ackOnNewConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ackOnNewConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(ackIPLabel)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(ackIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sendACKTransformerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sendACKTransformerActionPerformed
    {//GEN-HEADEREND:event_sendACKTransformerActionPerformed
        ackOnNewConnectionNo.setEnabled(true);
        ackOnNewConnectionYes.setEnabled(true);
        ackOnNewConnectionLabel.setEnabled(true);
        
        if(ackOnNewConnectionYes.isSelected())
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
    }//GEN-LAST:event_sendACKTransformerActionPerformed

    private void ackOnNewConnectionNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ackOnNewConnectionNoActionPerformed
    {//GEN-HEADEREND:event_ackOnNewConnectionNoActionPerformed
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
    }//GEN-LAST:event_ackOnNewConnectionNoActionPerformed

    private void ackOnNewConnectionYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ackOnNewConnectionYesActionPerformed
    {//GEN-HEADEREND:event_ackOnNewConnectionYesActionPerformed
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
    }//GEN-LAST:event_ackOnNewConnectionYesActionPerformed

    private void sendACKYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sendACKYesActionPerformed
    {//GEN-HEADEREND:event_sendACKYesActionPerformed
        if(evt != null && !PlatformUI.MIRTH_FRAME.channelEditPanel.synchronousCheckBox.isSelected())
        {
            PlatformUI.MIRTH_FRAME.alertInformation("The synchronize source connector setting has been enabled since it is required to use this feature.");
            PlatformUI.MIRTH_FRAME.channelEditPanel.synchronousCheckBox.setSelected(true);
        }
        
        ackOnNewConnectionNo.setEnabled(true);
        ackOnNewConnectionYes.setEnabled(true);
        ackOnNewConnectionLabel.setEnabled(true);

        if(ackOnNewConnectionYes.isSelected())
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
    }//GEN-LAST:event_sendACKYesActionPerformed

    private void sendACKNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sendACKNoActionPerformed
    {//GEN-HEADEREND:event_sendACKNoActionPerformed
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
    }//GEN-LAST:event_sendACKNoActionPerformed


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
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel ipDot;
    private javax.swing.JLabel ipDot1;
    private javax.swing.JLabel ipDot2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
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
    private com.webreach.mirth.client.ui.components.MirthRadioButton sendACKNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton sendACKTransformer;
    private com.webreach.mirth.client.ui.components.MirthRadioButton sendACKYes;
    // End of variables declaration//GEN-END:variables

}
