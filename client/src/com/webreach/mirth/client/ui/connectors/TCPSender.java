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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;
import com.webreach.mirth.model.Channel;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class TCPSender extends ConnectorClass
{
    Frame parent;

    /** Creates new form TCPSender */
    private final String DATATYPE = "DataType";

    private final String TCP_ADDRESS = "host";

    private final String TCP_PORT = "port";

    private final String TCP_SERVER_TIMEOUT = "sendTimeout";

    private final String TCP_BUFFER_SIZE = "bufferSize";

    private final String TCP_KEEP_CONNECTION_OPEN = "keepSendSocketOpen";

    private final String TCP_MAX_RETRY_COUNT = "maxRetryCount";

    private final String TCP_CHAR_ENCODING = "charEncoding";

    private final String TCP_TEMPLATE = "template";

    private final String TCP_USE_PERSISTENT_QUEUES = "usePersistentQueues";

    private final String TCP_ACK_TIMEOUT = "ackTimeout";

    private final String CONNECTOR_CHARSET_ENCODING = "charsetEncoding";

    private final String CHANNEL_ID = "replyChannelId";

    private final String CHANNEL_NAME = "channelName";

    private HashMap channelList;

    public TCPSender()
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        name = "TCP Sender";
        initComponents();
        hostIPAddressField.setDocument(new MirthFieldConstraints(3, false, true));
        hostIPAddressField1.setDocument(new MirthFieldConstraints(3, false, true));
        hostIPAddressField2.setDocument(new MirthFieldConstraints(3, false, true));
        hostPortField.setDocument(new MirthFieldConstraints(5, false, true));
        serverTimeoutField.setDocument(new MirthFieldConstraints(0, false, true));
        bufferSizeField.setDocument(new MirthFieldConstraints(0, false, true));
        maximumRetryCountField.setDocument(new MirthFieldConstraints(2, false, true));
        // ast: Acktimeout constrain
        ackTimeoutField.setDocument(new MirthFieldConstraints(0, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForChannel(charsetEncodingCombobox);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        String hostIPAddress = hostIPAddressField.getText() + "." + hostIPAddressField1.getText() + "." + hostIPAddressField2.getText() + "." + hostIPAddressField3.getText();
        properties.put(TCP_ADDRESS, hostIPAddress);
        properties.put(TCP_PORT, hostPortField.getText());
        properties.put(TCP_SERVER_TIMEOUT, serverTimeoutField.getText());
        properties.put(TCP_BUFFER_SIZE, bufferSizeField.getText());

        if (keepConnectionOpenYesRadio.isSelected())
            properties.put(TCP_KEEP_CONNECTION_OPEN, UIConstants.YES_OPTION);
        else
            properties.put(TCP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);

        properties.put(TCP_MAX_RETRY_COUNT, maximumRetryCountField.getText());

        // ast: queues
        if (usePersistentQueuesYesRadio.isSelected())
            properties.put(TCP_USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        else
            properties.put(TCP_USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);

        properties.put(TCP_ACK_TIMEOUT, ackTimeoutField.getText());
        properties.put(CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForChannel(charsetEncodingCombobox));
        properties.put(TCP_TEMPLATE, template.getText());
        properties.put(CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));
        properties.put(CHANNEL_NAME, (String) channelNames.getSelectedItem());
        return properties;
    }

    public void setProperties(Properties props)
    {
        String hostIPAddress = (String) props.get(TCP_ADDRESS);
        StringTokenizer IP = new StringTokenizer(hostIPAddress, ".");
        if (IP.hasMoreTokens())
            hostIPAddressField.setText(IP.nextToken());
        else
            hostIPAddressField.setText("");
        if (IP.hasMoreTokens())
            hostIPAddressField1.setText(IP.nextToken());
        else
            hostIPAddressField1.setText("");
        if (IP.hasMoreTokens())
            hostIPAddressField2.setText(IP.nextToken());
        else
            hostIPAddressField2.setText("");
        if (IP.hasMoreTokens())
            hostIPAddressField3.setText(IP.nextToken());
        else
            hostIPAddressField3.setText("");

        hostPortField.setText((String) props.get(TCP_PORT));
        serverTimeoutField.setText((String) props.get(TCP_SERVER_TIMEOUT));
        bufferSizeField.setText((String) props.get(TCP_BUFFER_SIZE));

        if (((String) props.get(TCP_KEEP_CONNECTION_OPEN)).equals(UIConstants.YES_OPTION))
            keepConnectionOpenYesRadio.setSelected(true);
        else
            keepConnectionOpenNoRadio.setSelected(true);

        maximumRetryCountField.setText((String) props.get(TCP_MAX_RETRY_COUNT));

        // ast:queued
        if (((String) props.get(TCP_USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION))
            usePersistentQueuesYesRadio.setSelected(true);
        else
            usePersistentQueuesNoRadio.setSelected(true);

        ackTimeoutField.setText((String) props.get(TCP_ACK_TIMEOUT));

        template.setText((String) props.get(TCP_TEMPLATE));
        parent.sePreviousSelectedEncodingForChannel(charsetEncodingCombobox, (String) props.get(CONNECTOR_CHARSET_ENCODING));

        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");
        for (Channel channel : parent.channels.values())
        {
            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        if (props.get(CHANNEL_NAME) != null)
            channelNames.setSelectedItem((String) props.get(CHANNEL_NAME));

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(TCP_ADDRESS, "127.0.0.1");
        properties.put(TCP_PORT, "6660");
        properties.put(TCP_SERVER_TIMEOUT, "5000");
        properties.put(TCP_BUFFER_SIZE, "65536");
        properties.put(TCP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);
        properties.put(TCP_MAX_RETRY_COUNT, "50");
        properties.put(TCP_CHAR_ENCODING, "hex");
        properties.put(TCP_USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        properties.put(TCP_ACK_TIMEOUT, "5000");
        properties.put(CONNECTOR_CHARSET_ENCODING, UIConstants.DEFAULT_ENCODING_OPTION);
        properties.put(TCP_TEMPLATE, "${message.encodedData}");
        properties.put(CHANNEL_ID, "sink");
        properties.put(CHANNEL_NAME, "None");
        return properties;
    }

    public boolean checkProperties(Properties props)
    {
        if (((String) props.get(TCP_ADDRESS)).length() > 0 && ((String) props.get(TCP_PORT)).length() > 0 && ((String) props.get(TCP_SERVER_TIMEOUT)).length() > 0 && ((String) props.get(TCP_BUFFER_SIZE)).length() > 0 && ((String) props.get(TCP_MAX_RETRY_COUNT)).length() > 0 && ((String) props.get(TCP_TEMPLATE)).length() > 0)
            return true;
        return false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        keepConnectionOpenGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        usePersistenceQueuesGroup = new javax.swing.ButtonGroup();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        hostPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        serverTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        bufferSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        maximumRetryCountField = new com.webreach.mirth.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        hostIPAddressField3 = new com.webreach.mirth.client.ui.components.MirthTextField();
        hostIPAddressField2 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        hostIPAddressField1 = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel9 = new javax.swing.JLabel();
        hostIPAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        ackTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel19 = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel20 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        usePersistentQueuesYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usePersistentQueuesNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel7 = new javax.swing.JLabel();
        template = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        channelNames = new com.webreach.mirth.client.ui.components.MirthComboBox();
        URL = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel13.setText("Keep Connection Open:");

        jLabel15.setText("Buffer Size (bytes):");

        jLabel16.setText("Send Timeout (ms):");

        jLabel17.setText("Host Port:");

        jLabel18.setText("Host IP Address:");

        jLabel8.setText("Maximum Retry Count:");

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

        jLabel19.setText("Response Timeout (ms):");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        jLabel20.setText("Encoding:");

        jLabel36.setText("Use Persistent Queues:");

        usePersistentQueuesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usePersistenceQueuesGroup.add(usePersistentQueuesYesRadio);
        usePersistentQueuesYesRadio.setText("Yes");
        usePersistentQueuesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        usePersistentQueuesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usePersistenceQueuesGroup.add(usePersistentQueuesNoRadio);
        usePersistentQueuesNoRadio.setSelected(true);
        usePersistentQueuesNoRadio.setText("No");
        usePersistentQueuesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel7.setText("Template:");

        template.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        URL.setText("Send Response to:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel18).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel17).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel16).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel15).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel13).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel36).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel19).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel20).add(org.jdesktop.layout.GroupLayout.TRAILING, URL).add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(layout.createSequentialGroup().add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(ackTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(serverTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(org.jdesktop.layout.GroupLayout.LEADING, maximumRetryCountField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.LEADING, keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(hostIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel9).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(hostIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel26).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(hostIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel25).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(hostIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(hostPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(template, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(hostIPAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(hostIPAddressField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel18)).add(jLabel9)).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(jLabel26).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(hostIPAddressField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel25)).add(hostIPAddressField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))).add(6, 6, 6).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(hostPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel17)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel16).add(serverTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel15)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel13).add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel8).add(maximumRetryCountField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel36).add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel19).add(ackTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel20).add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(URL).add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel7).add(template, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }// </editor-fold>//GEN-END:initComponents

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt)
    {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_charsetEncodingComboboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;

    private com.webreach.mirth.client.ui.components.MirthTextField ackTimeoutField;

    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;

    private javax.swing.ButtonGroup buttonGroup1;

    private com.webreach.mirth.client.ui.components.MirthComboBox channelNames;

    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;

    private com.webreach.mirth.client.ui.components.MirthTextField hostIPAddressField;

    private com.webreach.mirth.client.ui.components.MirthTextField hostIPAddressField1;

    private com.webreach.mirth.client.ui.components.MirthTextField hostIPAddressField2;

    private com.webreach.mirth.client.ui.components.MirthTextField hostIPAddressField3;

    private com.webreach.mirth.client.ui.components.MirthTextField hostPortField;

    private javax.swing.JLabel jLabel13;

    private javax.swing.JLabel jLabel15;

    private javax.swing.JLabel jLabel16;

    private javax.swing.JLabel jLabel17;

    private javax.swing.JLabel jLabel18;

    private javax.swing.JLabel jLabel19;

    private javax.swing.JLabel jLabel20;

    private javax.swing.JLabel jLabel25;

    private javax.swing.JLabel jLabel26;

    private javax.swing.JLabel jLabel36;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JLabel jLabel8;

    private javax.swing.JLabel jLabel9;

    private javax.swing.ButtonGroup keepConnectionOpenGroup;

    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;

    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;

    private com.webreach.mirth.client.ui.components.MirthTextField maximumRetryCountField;

    private com.webreach.mirth.client.ui.components.MirthTextField serverTimeoutField;

    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea template;

    private javax.swing.ButtonGroup usePersistenceQueuesGroup;

    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;

    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    // End of variables declaration//GEN-END:variables

}
