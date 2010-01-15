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

package com.webreach.mirth.connectors.http;

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
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Step;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class HTTPListener extends ConnectorClass
{
    /** Creates new form HTTPListener */

    public HTTPListener()
    {
        name = HTTPListenerProperties.name;
        initComponents();
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(HTTPListenerProperties.DATATYPE, name);
        properties.put(HTTPListenerProperties.HTTP_ADDRESS, listenerAddressField.getText());
        properties.put(HTTPListenerProperties.HTTP_PORT, listenerPortField.getText());
        properties.put(HTTPListenerProperties.HTTP_RECEIVE_TIMEOUT, receiveTimeoutField.getText());
        properties.put(HTTPListenerProperties.HTTP_BUFFER_SIZE, bufferSizeField.getText());
        properties.put(HTTPListenerProperties.HTTP_EXTENDED_PAYLOAD, UIConstants.YES_OPTION);
        
        if (keepConnectionOpenYesRadio.isSelected())
            properties.put(HTTPListenerProperties.HTTP_KEEP_CONNECTION_OPEN, UIConstants.YES_OPTION);
        else
            properties.put(HTTPListenerProperties.HTTP_KEEP_CONNECTION_OPEN, UIConstants.NO_OPTION);

        properties.put(HTTPListenerProperties.HTTP_RESPONSE_VALUE, (String)responseFromTransformer.getSelectedItem());
        
        if (appendPayloadYesRadio.isSelected())
            properties.put(HTTPListenerProperties.HTTP_APPEND_PAYLOAD, UIConstants.YES_OPTION);
        else
            properties.put(HTTPListenerProperties.HTTP_APPEND_PAYLOAD, UIConstants.NO_OPTION);
        
        if (((String) payloadURLEncodingComboBox.getSelectedItem()).equals(HTTPListenerProperties.PAYLOAD_ENCODING_NONE))
            properties.put(HTTPListenerProperties.HTTP_PAYLOAD_ENCODING, HTTPListenerProperties.PAYLOAD_ENCODING_NONE);
        else if (((String) payloadURLEncodingComboBox.getSelectedItem()).equals(HTTPListenerProperties.PAYLOAD_ENCODING_ENCODE))
            properties.put(HTTPListenerProperties.HTTP_PAYLOAD_ENCODING, HTTPListenerProperties.PAYLOAD_ENCODING_ENCODE);
        else if (((String) payloadURLEncodingComboBox.getSelectedItem()).equals(HTTPListenerProperties.PAYLOAD_ENCODING_DECODE))
            properties.put(HTTPListenerProperties.HTTP_PAYLOAD_ENCODING, HTTPListenerProperties.PAYLOAD_ENCODING_DECODE);

        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        listenerAddressField.setText((String) props.get(HTTPListenerProperties.HTTP_ADDRESS));
        listenerPortField.setText((String) props.get(HTTPListenerProperties.HTTP_PORT));
        receiveTimeoutField.setText((String) props.get(HTTPListenerProperties.HTTP_RECEIVE_TIMEOUT));
        bufferSizeField.setText((String) props.get(HTTPListenerProperties.HTTP_BUFFER_SIZE));

        if (((String) props.get(HTTPListenerProperties.HTTP_KEEP_CONNECTION_OPEN)).equals(UIConstants.YES_OPTION))
            keepConnectionOpenYesRadio.setSelected(true);
        else
            keepConnectionOpenNoRadio.setSelected(true);
        
        updateResponseDropDown();
        
        if (parent.channelEditPanel.synchronousCheckBox.isSelected())
            responseFromTransformer.setSelectedItem((String) props.getProperty(HTTPListenerProperties.HTTP_RESPONSE_VALUE));
        
        if (((String) props.get(HTTPListenerProperties.HTTP_APPEND_PAYLOAD)).equals(UIConstants.YES_OPTION))
            appendPayloadYesRadio.setSelected(true);
        else
            appendPayloadNoRadio.setSelected(true);
        
        if (props.get(HTTPListenerProperties.HTTP_PAYLOAD_ENCODING).equals(HTTPListenerProperties.PAYLOAD_ENCODING_NONE))
            payloadURLEncodingComboBox.setSelectedItem(HTTPListenerProperties.PAYLOAD_ENCODING_NONE);
        else if (props.get(HTTPListenerProperties.HTTP_PAYLOAD_ENCODING).equals(HTTPListenerProperties.PAYLOAD_ENCODING_ENCODE))
            payloadURLEncodingComboBox.setSelectedItem(HTTPListenerProperties.PAYLOAD_ENCODING_ENCODE);
        else if (props.get(HTTPListenerProperties.HTTP_PAYLOAD_ENCODING).equals(HTTPListenerProperties.PAYLOAD_ENCODING_DECODE))
            payloadURLEncodingComboBox.setSelectedItem(HTTPListenerProperties.PAYLOAD_ENCODING_DECODE);
    }

    public Properties getDefaults()
    {
        return new HTTPListenerProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(HTTPListenerProperties.HTTP_ADDRESS)).length() == 0)
        {
            valid = false;
            if (highlight)
            	listenerAddressField.setBackground(UIConstants.INVALID_COLOR);   
        }
        if (((String) props.get(HTTPListenerProperties.HTTP_PORT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	listenerPortField.setBackground(UIConstants.INVALID_COLOR);            
        }
        if (((String) props.get(HTTPListenerProperties.HTTP_RECEIVE_TIMEOUT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	receiveTimeoutField.setBackground(UIConstants.INVALID_COLOR);            
        }
        if (((String) props.get(HTTPListenerProperties.HTTP_BUFFER_SIZE)).length() == 0)
        {
            valid = false;
            if (highlight)
            	bufferSizeField.setBackground(UIConstants.INVALID_COLOR);            
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        listenerAddressField.setBackground(null);
        listenerPortField.setBackground(null);
        receiveTimeoutField.setBackground(null);
        bufferSizeField.setBackground(null);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        keepConnectionOpenGroup = new javax.swing.ButtonGroup();
        appendPayloadGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        bufferSizeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        receiveTimeoutField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerAddressField = new com.webreach.mirth.client.ui.components.MirthTextField();
        listenerPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        keepConnectionOpenYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        keepConnectionOpenNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        responseFromLabel = new javax.swing.JLabel();
        responseFromTransformer = new com.webreach.mirth.client.ui.components.MirthComboBox();
        appendPayloadYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        appendPayloadNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel6 = new javax.swing.JLabel();
        payloadURLEncodingComboBox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel7 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setText("Listener Address:");

        jLabel2.setText("Listener Port:");

        jLabel3.setText("Receive Timeout (ms):");

        jLabel4.setText("Buffer Size:");

        jLabel5.setText("Keep Connection Open:");

        bufferSizeField.setToolTipText("<html>Enter at least the size of the largest HTTP request that will be sent to the connector.<br>Entering too small a value will cause larger requests to take more time to process.<br>Entering too large a value wastes memory. Generally, the default value is fine.</html>");

        receiveTimeoutField.setToolTipText("Enter the number of milliseconds that the connector should keep a connection open without receiving a request.");

        listenerAddressField.setToolTipText("Enter the DNS domain name or IP address on which the connector should listen for HTTP operations.");

        listenerPortField.setToolTipText("Enter the port number (0-65535) on which the connector should listen.");

        keepConnectionOpenYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenYesRadio);
        keepConnectionOpenYesRadio.setText("Yes");
        keepConnectionOpenYesRadio.setToolTipText("Select Yes to keep the connection to the client open after a request is received to allow multiple requests on one connection.");
        keepConnectionOpenYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        keepConnectionOpenNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        keepConnectionOpenNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepConnectionOpenGroup.add(keepConnectionOpenNoRadio);
        keepConnectionOpenNoRadio.setText("No");
        keepConnectionOpenNoRadio.setToolTipText("Select No to close the connection after each request.");
        keepConnectionOpenNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        responseFromLabel.setText("Respond from:");

        responseFromTransformer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        responseFromTransformer.setToolTipText("<html>Select None or the name of a destination of this channel that will generate the response to the request.<br>If None is selected, the response will always be \"200 OK\" if the message is successfully processed <br>or \"500 Server Error\" if there is an error processing the message.</html>");

        appendPayloadYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        appendPayloadYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        appendPayloadGroup.add(appendPayloadYesRadio);
        appendPayloadYesRadio.setText("Yes");
        appendPayloadYesRadio.setToolTipText("<html>If Yes is selected, the \"Content\" part of the HTTP request will be interpreted as a separate XML node in the incoming data.<br>If the payload variable is not appended Mirth will automatically convert the HTTP \"Content\" data to the appropriate XML representation.</html>");
        appendPayloadYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        appendPayloadNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        appendPayloadNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        appendPayloadGroup.add(appendPayloadNoRadio);
        appendPayloadNoRadio.setText("No");
        appendPayloadNoRadio.setToolTipText("<html>If Yes is selected, the \"Content\" part of the HTTP request will be interpreted as a separate XML node in the incoming data.<br>If the payload variable is not appended Mirth will automatically convert the HTTP \"Content\" data to the appropriate XML representation.</html>");
        appendPayloadNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel6.setText("Append Payload Variable:");

        payloadURLEncodingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Encode", "Decode" }));
        payloadURLEncodingComboBox.setToolTipText("<html>Select None to store the Content part of the HTTP request in payload without translation.<br>Select Encode to store the Content part after performing URL encoding.<br>Select Decode to store the Content part after performing URL decoding.</html>");
        payloadURLEncodingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                payloadURLEncodingComboBoxActionPerformed(evt);
            }
        });

        jLabel7.setText("Payload URL Encoding:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel6)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, responseFromLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(payloadURLEncodingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(appendPayloadYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(appendPayloadNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(responseFromTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 150, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenerAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(listenerAddressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(listenerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(receiveTimeoutField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bufferSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keepConnectionOpenYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keepConnectionOpenNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(responseFromLabel)
                    .add(responseFromTransformer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(appendPayloadYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(appendPayloadNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(payloadURLEncodingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void payloadURLEncodingComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_payloadURLEncodingComboBoxActionPerformed
    {//GEN-HEADEREND:event_payloadURLEncodingComboBoxActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_payloadURLEncodingComboBoxActionPerformed
    
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
        
        if(variables.contains(selectedItem))
            responseFromTransformer.setSelectedItem(selectedItem);
        else
            responseFromTransformer.setSelectedIndex(0);
        
        if (!parent.channelEditPanel.synchronousCheckBox.isSelected())
        {
            responseFromTransformer.setEnabled(false);
            responseFromLabel.setEnabled(false);
            responseFromTransformer.setSelectedIndex(0);
        }
        else
        {
            responseFromTransformer.setEnabled(true);
            responseFromLabel.setEnabled(true);
        }
        
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup appendPayloadGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendPayloadNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendPayloadYesRadio;
    private com.webreach.mirth.client.ui.components.MirthTextField bufferSizeField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.ButtonGroup keepConnectionOpenGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton keepConnectionOpenYesRadio;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerAddressField;
    private com.webreach.mirth.client.ui.components.MirthTextField listenerPortField;
    private com.webreach.mirth.client.ui.components.MirthComboBox payloadURLEncodingComboBox;
    private com.webreach.mirth.client.ui.components.MirthTextField receiveTimeoutField;
    private javax.swing.JLabel responseFromLabel;
    private com.webreach.mirth.client.ui.components.MirthComboBox responseFromTransformer;
    // End of variables declaration//GEN-END:variables

}
