/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.connectors;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Step;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;

public class ResponseSettingsPanel extends javax.swing.JPanel {

    public final String RESULT_PATTERN = "responseMap.put\\(['|\"]([^'|^\"]*)[\"|']";
    private Frame parent;
    private List<String> queueOnRespondFromNames;
    private List<String> queueOffRespondFromNames;

    public ResponseSettingsPanel() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();
    }

    public void setProperties(ResponseConnectorProperties properties) {
        updateResponseDropDown(properties, true);
        
        // Set the source queue combo box
        if (properties.isRespondAfterProcessing()) {
        	sourceQueueComboBox.setSelectedIndex(0);
        } else {
        	sourceQueueComboBox.setSelectedIndex(1);
        }
        sourceQueueComboBoxActionPerformed(null);

        // TODO: fix?
//        if (!parent.channelEditPanel.synchronousCheckBox.isSelected()) {
//            responseComboBox.setEnabled(false);
//            responseLabel.setEnabled(false);
//            responseComboBox.setSelectedIndex(0);
//        } else {
//            responseComboBox.setEnabled(true);
//            responseLabel.setEnabled(true);
//        }
    }
    
    public void updateResponseDropDown(ResponseConnectorProperties properties, boolean channelLoad) {
    	boolean enabled = parent.isSaveEnabled();
    	
    	String selectedItem;
    	if (channelLoad) {
    		selectedItem = properties.getResponseVariable();
    	} else {
    		selectedItem = (String) responseComboBox.getSelectedItem();
    	}

        Channel channel = parent.channelEditPanel.currentChannel;

        Set<String> variables = new LinkedHashSet<String>();

        variables.addAll(Arrays.asList(properties.getDefaultQueueOffResponses()));

        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getSteps());

        List<String> scripts = new ArrayList<String>();

        for (Connector connector : channel.getDestinationConnectors()) {
            ConnectorSettingsPanel tempConnector = LoadedExtensions.getInstance().getDestinationConnectors().get(connector.getTransportName());;
            scripts.addAll(tempConnector.getScripts(connector.getProperties()));
            
            variables.add(connector.getName());
            stepsToCheck.addAll(connector.getTransformer().getSteps());
        }

        Pattern pattern = Pattern.compile(RESULT_PATTERN);

        for (Iterator it = stepsToCheck.iterator(); it.hasNext();) {
            Step step = (Step) it.next();
            Map data;
            data = (Map) step.getData();

            if (step.getType().equalsIgnoreCase(TransformerPane.JAVASCRIPT_TYPE)) {
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find()) {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            } else if (step.getType().equalsIgnoreCase(TransformerPane.MAPPER_TYPE)) {
                if (data.containsKey(UIConstants.IS_GLOBAL)) {
                    if (((String) data.get(UIConstants.IS_GLOBAL)).equalsIgnoreCase(UIConstants.IS_GLOBAL_RESPONSE)) {
                        variables.add((String) data.get("Variable"));
                    }
                }
            }
        }

        scripts.add(channel.getPreprocessingScript());
        scripts.add(channel.getPostprocessingScript());

        for (String script : scripts) {
            if (script != null && script.length() > 0) {
                Matcher matcher = pattern.matcher(script);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    variables.add(key);
                }
            }
        }

        queueOnRespondFromNames = new ArrayList<String>(Arrays.asList(properties.getDefaultQueueOnResponses()));
        queueOffRespondFromNames = new ArrayList<String>(variables);
        responseComboBox.setModel(new DefaultComboBoxModel(variables.toArray()));

        if (variables.contains(selectedItem)) {
            responseComboBox.setSelectedItem(selectedItem);
        } else {
            responseComboBox.setSelectedIndex(0);
        }
        
        sourceQueueComboBoxActionPerformed(null);
        parent.setSaveEnabled(enabled);
    }

    public void fillProperties(ResponseConnectorProperties properties) {
        properties.setResponseVariable((String) responseComboBox.getSelectedItem());
        properties.setRespondAfterProcessing(sourceQueueComboBox.getSelectedIndex() == 0);
    }

    public boolean checkProperties(ResponseConnectorProperties properties, boolean highlight) {
        boolean valid = true;

        return valid;
    }

    public void resetInvalidProperties() {
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listenerButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        responseLabel = new javax.swing.JLabel();
        responseComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        sourceQueueLabel = new javax.swing.JLabel();
        sourceQueueComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();

        jLabel1.setText("jLabel1");

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Response Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        responseLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        responseLabel.setText("Response:");
        responseLabel.setToolTipText("<html>Select the response to send back to the source system.<br/><br/>Select \"None\" to send no response.<br/>Select a destination of this channel that will supply a return value using the Response Map.<br/>Select a variable that has been added to the Response Map.<br/><br/>Select \"Auto-generate\" to send a response automatically generated by the inbound data type.<br/>\"Before processing\" indicates that the response should be generated before the channel processes the message, with a status of SENT and using the raw inbound message.<br/>\"After source transformer\" indicates that the response should be generated after the channel processes the message, using the status and raw content from the source connector.<br/>\"Destinations completed\" indicates that the response should be generated after the channel processes the message, using the raw content from the source connector,<br/>and a status based on the destination connector messages. The precedence used for the status is as follows:<br/><br/>&nbsp;&nbsp;1) ERROR<br/>&nbsp;&nbsp;2) QUEUED<br/>&nbsp;&nbsp;3) SENT<br/>&nbsp;&nbsp;4) FILTERED<br/><br/>If the inbound data type for the source connector doesn't have an associated auto-responder<br/>then the default auto-responder is used, which returns a response with the selected status, but no response message.<br/><br/></html>");
        responseLabel.setMaximumSize(new java.awt.Dimension(62, 15));
        responseLabel.setMinimumSize(new java.awt.Dimension(62, 15));
        responseLabel.setPreferredSize(new java.awt.Dimension(62, 15));

        responseComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Auto-generate (After source transformer)", "None", "Auto-generate (Before processing)", "Auto-generate (After source transformer)", "Auto-generate (Destinations completed)", "Post-processor", "Destination 1" }));
        responseComboBox.setToolTipText("<html>Select the response to send back to the source system.<br/><br/>Select \"None\" to send no response.<br/>Select a destination of this channel that will supply a return value using the Response Map.<br/>Select a variable that has been added to the Response Map.<br/><br/>Select \"Auto-generate\" to send a response automatically generated by the inbound data type.<br/>\"Before processing\" indicates that the response should be generated before the channel processes the message, with a status of SENT and using the raw inbound message.<br/>\"After source transformer\" indicates that the response should be generated after the channel processes the message, using the status and raw content from the source connector.<br/>\"Destinations completed\" indicates that the response should be generated after the channel processes the message, using the raw content from the source connector,<br/>and a status based on the destination connector messages. The precedence used for the status is as follows:<br/><br/>&nbsp;&nbsp;1) ERROR<br/>&nbsp;&nbsp;2) QUEUED<br/>&nbsp;&nbsp;3) SENT<br/>&nbsp;&nbsp;4) FILTERED<br/><br/>If the inbound data type for the source connector doesn't have an associated auto-responder<br/>then the default auto-responder is used, which returns a response with the selected status, but no response message.<br/><br/></html>");
        responseComboBox.setMinimumSize(new java.awt.Dimension(150, 22));
        responseComboBox.setPreferredSize(new java.awt.Dimension(212, 22));

        sourceQueueLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        sourceQueueLabel.setText("Source Queue:");
        sourceQueueLabel.setToolTipText("<html>Selecting OFF will process the message before sending the response (can use response from destinations)<br>Selecting ON will queue messages and immediately send a response (cannot use response from destinations)</html>");

        sourceQueueComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OFF (Respond after processing)", "ON (Respond before processing)" }));
        sourceQueueComboBox.setToolTipText("<html>Selecting OFF will process the message before sending the response (can use response from destinations)<br>Selecting ON will queue messages and immediately send a response (cannot use response from destinations)</html>");
        sourceQueueComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceQueueComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(responseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceQueueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourceQueueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 8, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceQueueLabel)
                    .addComponent(sourceQueueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sourceQueueComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceQueueComboBoxActionPerformed
        Object selectedItem = responseComboBox.getSelectedItem();
        
        if (sourceQueueComboBox.getSelectedIndex() == 0) {
            responseComboBox.setModel(new DefaultComboBoxModel(queueOffRespondFromNames.toArray()));
        } else {
            responseComboBox.setModel(new DefaultComboBoxModel(queueOnRespondFromNames.toArray()));
        }
        
        if (((DefaultComboBoxModel) responseComboBox.getModel()).getIndexOf(selectedItem) >= 0) {
            responseComboBox.setSelectedItem(selectedItem);
        } else {
            responseComboBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_sourceQueueComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.ButtonGroup listenerButtonGroup;
    private com.mirth.connect.client.ui.components.MirthComboBox responseComboBox;
    private javax.swing.JLabel responseLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox sourceQueueComboBox;
    private javax.swing.JLabel sourceQueueLabel;
    // End of variables declaration//GEN-END:variables
}
