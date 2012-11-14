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

    public ResponseSettingsPanel() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();
    }

    public void setProperties(ResponseConnectorProperties properties) {
        updateResponseDropDown(properties);
        
        // Set the source queue combo box
        if (properties.isRespondAfterProcessing()) {
        	sourceQueueComboBox.setSelectedIndex(0);
        } else {
        	sourceQueueComboBox.setSelectedIndex(1);
        }

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
    
    public void updateResponseDropDown(ResponseConnectorProperties properties) {
    	boolean enabled = parent.isSaveEnabled();
    	
    	String selectedItem = (String) responseComboBox.getSelectedItem();

        Channel channel = parent.channelEditPanel.currentChannel;

        Set<String> variables = new LinkedHashSet<String>();

        variables.addAll(Arrays.asList(properties.getDefaultResponses()));

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

        responseComboBox.setModel(new DefaultComboBoxModel(variables.toArray()));

        if (variables.contains(selectedItem)) {
            responseComboBox.setSelectedItem(selectedItem);
        } else {
            responseComboBox.setSelectedIndex(0);
        }
        
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
        responseLabel.setMaximumSize(new java.awt.Dimension(62, 15));
        responseLabel.setMinimumSize(new java.awt.Dimension(62, 15));
        responseLabel.setPreferredSize(new java.awt.Dimension(62, 15));

        responseComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        responseComboBox.setToolTipText("<html>Select \"None\" to send no response.<br>Select a destination of this channel that will supply a return value using the Response Map.<br>Select a variable that has been added to the Response Map.</html>");
        responseComboBox.setMinimumSize(new java.awt.Dimension(150, 22));
        responseComboBox.setPreferredSize(new java.awt.Dimension(212, 22));

        sourceQueueLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        sourceQueueLabel.setText("Source Queue:");

        sourceQueueComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OFF (Respond after processing)", "ON (Respond before processing)" }));
        sourceQueueComboBox.setToolTipText("<html>Selecting OFF will process the message before sending the response (can use response from destinations)<br>Selecting ON will queue messages and immediately send a response (cannot use response from destinations)</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(responseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(sourceQueueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(responseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceQueueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 48, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceQueueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceQueueLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.ButtonGroup listenerButtonGroup;
    private com.mirth.connect.client.ui.components.MirthComboBox responseComboBox;
    private javax.swing.JLabel responseLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox sourceQueueComboBox;
    private javax.swing.JLabel sourceQueueLabel;
    // End of variables declaration//GEN-END:variables
}
