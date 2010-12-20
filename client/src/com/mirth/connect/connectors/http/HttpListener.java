/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.mirth.connect.connectors.http;

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

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Step;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class HttpListener extends ConnectorClass {

    /** Creates new form HTTPListener */
    public HttpListener() {
        name = HttpListenerProperties.name;
        initComponents();
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(HttpListenerProperties.DATATYPE, name);
        properties.put(HttpListenerProperties.HTTP_HOST, listenerAddressField.getText());
        properties.put(HttpListenerProperties.HTTP_PORT, listenerPortField.getText());

        if (messageContentBodyOnlyRadio.isSelected()) {
            properties.put(HttpListenerProperties.HTTP_BODY_ONLY, UIConstants.YES_OPTION);
        } else {
            properties.put(HttpListenerProperties.HTTP_BODY_ONLY, UIConstants.NO_OPTION);
        }

        properties.put(HttpListenerProperties.HTTP_RESPONSE, (String) responseFromTransformer.getSelectedItem());
        properties.put(HttpListenerProperties.HTTP_RESPONSE_CONTENT_TYPE, responseContentTypeField.getText());

        properties.put(HttpListenerProperties.HTTP_CHARSET, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        listenerAddressField.setText((String) props.get(HttpListenerProperties.HTTP_HOST));
        updateListenerAddressRadio();

        listenerPortField.setText((String) props.get(HttpListenerProperties.HTTP_PORT));

        if (((String) props.get(HttpListenerProperties.HTTP_BODY_ONLY)).equals(UIConstants.YES_OPTION)) {
            messageContentBodyOnlyRadio.setSelected(true);
        } else {
            messageContentHeadersQueryAndBodyRadio.setSelected(true);
        }

        updateResponseDropDown();

        if (parent.channelEditPanel.synchronousCheckBox.isSelected()) {
            // Setting the selected item also enables/disables the response
            // content type field and sets the default if it is disabled
            responseFromTransformer.setSelectedItem((String) props.getProperty(HttpListenerProperties.HTTP_RESPONSE));
        }

        responseContentTypeField.setText((String) props.get(HttpListenerProperties.HTTP_RESPONSE_CONTENT_TYPE));

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(HttpListenerProperties.HTTP_CHARSET));
    }

    public Properties getDefaults() {
        return new HttpListenerProperties().getDefaults();
    }

    private void updateListenerAddressRadio() {
        if (listenerAddressField.getText().equals(getDefaults().getProperty(HttpListenerProperties.HTTP_HOST))) {
            listenerAllRadio.setSelected(true);
            listenerAllRadioActionPerformed(null);
        } else {
            listenerSpecificRadio.setSelected(true);
            listenerSpecificRadioActionPerformed(null);
        }
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(HttpListenerProperties.HTTP_HOST)).length() == 0) {
            valid = false;
            if (highlight) {
                listenerAddressField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(HttpListenerProperties.HTTP_PORT)).length() == 0) {
            valid = false;
            if (highlight) {
                listenerPortField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (!((String) props.get(HttpListenerProperties.HTTP_RESPONSE)).equalsIgnoreCase("None")) {
            if (((String) props.get(HttpListenerProperties.HTTP_RESPONSE_CONTENT_TYPE)).length() == 0) {
                valid = false;
                if (highlight) {
                    responseContentTypeField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

        }

        return valid;
    }

    private void resetInvalidProperties() {
        listenerAddressField.setBackground(null);
        listenerPortField.setBackground(null);
        responseContentTypeField.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

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

        listenerAddressButtonGroup = new javax.swing.ButtonGroup();
        includeHeadersGroup = new javax.swing.ButtonGroup();
        responseFromLabel = new javax.swing.JLabel();
        responseFromTransformer = new com.mirth.connect.client.ui.components.MirthComboBox();
        messageContentBodyOnlyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        messageContentHeadersQueryAndBodyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        messageContentLabel = new javax.swing.JLabel();
        listenerAddressLabel = new javax.swing.JLabel();
        listenerAllRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        listenerSpecificRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        listenerAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        listenerPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        listenerPortLabel = new javax.swing.JLabel();
        responseContentTypeField = new com.mirth.connect.client.ui.components.MirthTextField();
        responseContentTypeLabel = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        charsetEncodingLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        responseFromLabel.setText("Respond from:");

        responseFromTransformer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        responseFromTransformer.setToolTipText("<html>Select None or the name of a destination of this channel that will generate the response to the request.<br>If None is selected, the response will always be \"200 OK\" if the message is successfully processed <br>or \"500 Server Error\" if there is an error processing the message.</html>");
        responseFromTransformer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responseFromTransformerActionPerformed(evt);
            }
        });

        messageContentBodyOnlyRadio.setBackground(new java.awt.Color(255, 255, 255));
        messageContentBodyOnlyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        includeHeadersGroup.add(messageContentBodyOnlyRadio);
        messageContentBodyOnlyRadio.setText("Body Only");
        messageContentBodyOnlyRadio.setToolTipText("<html>If selected, the message content will only include the body as a string.</html>");
        messageContentBodyOnlyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        messageContentBodyOnlyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageContentBodyOnlyRadioActionPerformed(evt);
            }
        });

        messageContentHeadersQueryAndBodyRadio.setBackground(new java.awt.Color(255, 255, 255));
        messageContentHeadersQueryAndBodyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        includeHeadersGroup.add(messageContentHeadersQueryAndBodyRadio);
        messageContentHeadersQueryAndBodyRadio.setText("Headers, Query, and Body");
        messageContentHeadersQueryAndBodyRadio.setToolTipText("<html>If selected, the message content will include the request headers, query parameters, and body as XML.</html>");
        messageContentHeadersQueryAndBodyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        messageContentHeadersQueryAndBodyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageContentHeadersQueryAndBodyRadioActionPerformed(evt);
            }
        });

        messageContentLabel.setText("Message Content:");

        listenerAddressLabel.setText("Listener Address:");

        listenerAllRadio.setBackground(new java.awt.Color(255, 255, 255));
        listenerAllRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listenerAddressButtonGroup.add(listenerAllRadio);
        listenerAllRadio.setText("Listen on all interfaces");
        listenerAllRadio.setToolTipText("<html>If checked, the connector will listen on all interfaces, using address 0.0.0.0.</html>");
        listenerAllRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        listenerAllRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenerAllRadioActionPerformed(evt);
            }
        });

        listenerSpecificRadio.setBackground(new java.awt.Color(255, 255, 255));
        listenerSpecificRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listenerAddressButtonGroup.add(listenerSpecificRadio);
        listenerSpecificRadio.setText("Specific interface:");
        listenerSpecificRadio.setToolTipText("<html>If checked, the connector will listen on the specific interface address defined.</html>");
        listenerSpecificRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        listenerSpecificRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenerSpecificRadioActionPerformed(evt);
            }
        });

        listenerAddressField.setToolTipText("The DNS domain name or IP address on which the server should listen for connections.");

        listenerPortField.setToolTipText("The port on which the server should listen for connections.");

        listenerPortLabel.setText("Port:");

        responseContentTypeField.setToolTipText("The MIME type to be used for the response.");

        responseContentTypeLabel.setText("Response Content Type:");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding to be used for the response to the sending system.<br>Set to Default to assume the default character set encoding for the JVM running Mirth.</html>");

        charsetEncodingLabel.setText("Charset Encoding:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(listenerAddressLabel)
                    .addComponent(listenerPortLabel)
                    .addComponent(messageContentLabel)
                    .addComponent(responseFromLabel)
                    .addComponent(responseContentTypeLabel)
                    .addComponent(charsetEncodingLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(listenerPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listenerAllRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listenerSpecificRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listenerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(responseFromTransformer, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseContentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(messageContentBodyOnlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(messageContentHeadersQueryAndBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listenerAllRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(listenerSpecificRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(listenerAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(listenerAddressLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listenerPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(listenerPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageContentLabel)
                    .addComponent(messageContentHeadersQueryAndBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageContentBodyOnlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseFromTransformer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseFromLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseContentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseContentTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void listenerAllRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenerAllRadioActionPerformed
        listenerAddressField.setText(getDefaults().getProperty(HttpListenerProperties.HTTP_HOST));
        listenerAddressField.setEnabled(false);
}//GEN-LAST:event_listenerAllRadioActionPerformed

    private void listenerSpecificRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenerSpecificRadioActionPerformed
        listenerAddressField.setEnabled(true);
}//GEN-LAST:event_listenerSpecificRadioActionPerformed

    private void responseFromTransformerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responseFromTransformerActionPerformed
        if (((String) responseFromTransformer.getSelectedItem()).equalsIgnoreCase("None")) {
            responseContentTypeLabel.setEnabled(false);
            responseContentTypeField.setEnabled(false);
            responseContentTypeField.setText(getDefaults().getProperty(HttpListenerProperties.HTTP_RESPONSE_CONTENT_TYPE));
        } else {
            responseContentTypeLabel.setEnabled(true);
            responseContentTypeField.setEnabled(true);
        }
    }//GEN-LAST:event_responseFromTransformerActionPerformed

    private void messageContentHeadersQueryAndBodyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageContentHeadersQueryAndBodyRadioActionPerformed
        parent.channelEditPanel.checkAndSetXmlDataType();
    }//GEN-LAST:event_messageContentHeadersQueryAndBodyRadioActionPerformed

    private void messageContentBodyOnlyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageContentBodyOnlyRadioActionPerformed
        parent.channelEditPanel.checkAndSetXmlDataType();
    }//GEN-LAST:event_messageContentBodyOnlyRadioActionPerformed

    public void updateResponseDropDown() {
        boolean enabled = parent.isSaveEnabled();

        String selectedItem = (String) responseFromTransformer.getSelectedItem();

        Channel channel = parent.channelEditPanel.currentChannel;

        Set<String> variables = new LinkedHashSet<String>();

        variables.add("None");

        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getSteps());

        List<String> scripts = new ArrayList<String>();

        for (Connector connector : channel.getDestinationConnectors()) {
            if (connector.getTransportName().equals("Database Writer")) {
                if (connector.getProperties().getProperty("useScript").equals(UIConstants.YES_OPTION)) {
                    scripts.add(connector.getProperties().getProperty("script"));
                }

            } else if (connector.getTransportName().equals("JavaScript Writer")) {
                scripts.add(connector.getProperties().getProperty("script"));
            }

            variables.add(connector.getName());
            stepsToCheck.addAll(connector.getTransformer().getSteps());
        }

        Pattern pattern = Pattern.compile(RESULT_PATTERN);

        int i = 0;
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

        responseFromTransformer.setModel(new DefaultComboBoxModel(variables.toArray()));

        if (variables.contains(selectedItem)) {
            responseFromTransformer.setSelectedItem(selectedItem);
        } else {
            responseFromTransformer.setSelectedIndex(0);
        }

        if (!parent.channelEditPanel.synchronousCheckBox.isSelected()) {
            responseFromTransformer.setEnabled(false);
            responseFromLabel.setEnabled(false);
            responseFromTransformer.setSelectedIndex(0);
        } else {
            responseFromTransformer.setEnabled(true);
            responseFromLabel.setEnabled(true);
        }

        parent.setSaveEnabled(enabled);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel charsetEncodingLabel;
    private javax.swing.ButtonGroup includeHeadersGroup;
    private javax.swing.ButtonGroup listenerAddressButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField listenerAddressField;
    private javax.swing.JLabel listenerAddressLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton listenerAllRadio;
    private com.mirth.connect.client.ui.components.MirthTextField listenerPortField;
    private javax.swing.JLabel listenerPortLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton listenerSpecificRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton messageContentBodyOnlyRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton messageContentHeadersQueryAndBodyRadio;
    private javax.swing.JLabel messageContentLabel;
    private com.mirth.connect.client.ui.components.MirthTextField responseContentTypeField;
    private javax.swing.JLabel responseContentTypeLabel;
    private javax.swing.JLabel responseFromLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox responseFromTransformer;
    // End of variables declaration//GEN-END:variables
}
