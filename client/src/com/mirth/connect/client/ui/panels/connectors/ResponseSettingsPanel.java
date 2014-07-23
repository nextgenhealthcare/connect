/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.connectors;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;

import org.apache.commons.lang3.StringEscapeUtils;

import com.mirth.connect.client.ui.ChannelSetup;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.Step;

public class ResponseSettingsPanel extends javax.swing.JPanel {

    /*
     * This regular expression uses alternation to capture either the
     * "responseMap.put" syntax, or the "$r('key'," syntax. Kleene closures for
     * whitespace are used in between every method token since it is legal
     * JavaScript. Instead of checking ['"] once at the beginning and end, it
     * checks once and then uses a backreference later on. That way you can
     * capture keys like "Foo's Bar". It also accounts for backslashes before
     * any subsequent backreferences so that "Foo\"s Bar" would still be
     * captured. In the "$r" case, the regular expression also performs a
     * lookahead to ensure that there is a comma after the first argument,
     * indicating that it is the "put" version of the method, not the "get"
     * version.
     */
    private final String RESULT_PATTERN = "responseMap\\s*\\.\\s*put\\s*\\(\\s*(['\"])(((?!(?<!\\\\)\\1).)*)(?<!\\\\)\\1|\\$r\\s*\\(\\s*(['\"])(((?!(?<!\\\\)\\4).)*)(?<!\\\\)\\4(?=\\s*,)";
    private final static int FULL_NAME_MATCHER_INDEX = 2;
    private final static int SHORT_NAME_MATCHER_INDEX = 5;

    private Frame parent;
    private ChannelSetup channelSetup;
    private List<String> queueOnRespondFromNames;
    private List<Object> queueOffRespondFromNames;

    public ResponseSettingsPanel() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        queueWarningLabel.setVisible(false);
    }

    public void setChannelSetup(ChannelSetup channelSetup) {
        this.channelSetup = channelSetup;
    }

    public void setProperties(ResponseConnectorPropertiesInterface propertiesInterface) {
        ResponseConnectorProperties properties = propertiesInterface.getResponseConnectorProperties();

        updateResponseDropDown(propertiesInterface, true);

        // Set the source queue combo box
        if (properties.isRespondAfterProcessing()) {
            sourceQueueComboBox.setSelectedIndex(0);
        } else {
            sourceQueueComboBox.setSelectedIndex(1);
        }
    }

    public void updateResponseDropDown(ResponseConnectorPropertiesInterface propertiesInterface, boolean channelLoad) {
        ResponseConnectorProperties properties = propertiesInterface.getResponseConnectorProperties();

        boolean enabled = parent.isSaveEnabled();
        Channel channel = parent.channelEditPanel.currentChannel;

        Set<Object> variables = new LinkedHashSet<Object>();

        variables.addAll(Arrays.asList(properties.getDefaultQueueOffResponses()));

        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getSteps());

        List<String> scripts = new ArrayList<String>();

        for (Connector connector : channel.getDestinationConnectors()) {
            ConnectorSettingsPanel tempConnector = LoadedExtensions.getInstance().getDestinationConnectors().get(connector.getTransportName());
            scripts.addAll(tempConnector.getScripts(connector.getProperties()));

            /*
             * We add an Entry object instead of just the connector name, so that the back-end
             * response variable is the "d#" key, while the front-end combo box display is the full
             * connector name.
             */
            variables.add(new SimpleEntry<String, String>("d" + String.valueOf(connector.getMetaDataId()), connector.getName()) {
                @Override
                public String toString() {
                    return getValue();
                }
            });
            stepsToCheck.addAll(connector.getTransformer().getSteps());
            stepsToCheck.addAll(connector.getResponseTransformer().getSteps());
        }

        Pattern pattern = Pattern.compile(RESULT_PATTERN);

        for (Iterator it = stepsToCheck.iterator(); it.hasNext();) {
            Step step = (Step) it.next();
            Map data;
            data = (Map) step.getData();

            if (step.getType().equalsIgnoreCase(TransformerPane.JAVASCRIPT_TYPE)) {
                Matcher matcher = pattern.matcher(step.getScript());
                while (matcher.find()) {
                    variables.add(getMapKey(matcher));
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
                    variables.add(getMapKey(matcher));
                }
            }
        }

        queueOnRespondFromNames = new ArrayList<String>(Arrays.asList(properties.getDefaultQueueOnResponses()));
        queueOffRespondFromNames = new ArrayList<Object>(variables);

        if (channelLoad) {
            /*
             * The response variable is the response map key. To ensure that the destination name
             * still shows up in the combo box, we use an Entry object for the selected item, rather
             * than a single String.
             */
            Object selectedItem = properties.getResponseVariable();
            for (Connector connector : channel.getDestinationConnectors()) {
                if (selectedItem.equals("d" + String.valueOf(connector.getMetaDataId()))) {
                    selectedItem = new SimpleEntry<String, String>("d" + String.valueOf(connector.getMetaDataId()), connector.getName()) {
                        @Override
                        public String toString() {
                            return getValue();
                        }
                    };
                    break;
                }
            }

            responseComboBox.setModel(new DefaultComboBoxModel(variables.toArray()));
            setSelectedItem(selectedItem);
        } else {
            updateSelectedResponseItem();
        }

        parent.setSaveEnabled(enabled);
    }

    private void updateSelectedResponseItem() {
        Object selectedItem = responseComboBox.getSelectedItem();

        if (sourceQueueComboBox.getSelectedIndex() == 0) {
            responseComboBox.setModel(new DefaultComboBoxModel(queueOffRespondFromNames.toArray()));
        } else {
            responseComboBox.setModel(new DefaultComboBoxModel(queueOnRespondFromNames.toArray()));
        }

        setSelectedItem(selectedItem);

        channelSetup.saveSourcePanel();
        MessageStorageMode messageStorageMode = channelSetup.getMessageStorageMode();
        channelSetup.updateQueueWarning(messageStorageMode);
        updateQueueWarning(messageStorageMode);
    }

    public void fillProperties(ResponseConnectorPropertiesInterface propertiesInterface) {
        ResponseConnectorProperties properties = propertiesInterface.getResponseConnectorProperties();

        if (responseComboBox.getSelectedItem() instanceof Entry) {
            properties.setResponseVariable(((Entry<String, String>) responseComboBox.getSelectedItem()).getKey());
        } else {
            properties.setResponseVariable((String) responseComboBox.getSelectedItem());
        }
        properties.setRespondAfterProcessing(sourceQueueComboBox.getSelectedIndex() == 0);
    }

    public boolean checkProperties(ResponseConnectorPropertiesInterface propertiesInterface, boolean highlight) {
        ResponseConnectorProperties properties = propertiesInterface.getResponseConnectorProperties();

        boolean valid = true;

        return valid;
    }

    private String getMapKey(Matcher matcher) {
        /*
         * Since multiple capturing groups are used and the final key could reside on either side of
         * the alternation, we use two specific group indices (2 and 5), one for the full
         * "responseMap" case and one for the short "$r" case. We also replace JavaScript-specific
         * escape sequences like \', \", etc.
         */
        String key = matcher.group(FULL_NAME_MATCHER_INDEX);
        if (key == null) {
            key = matcher.group(SHORT_NAME_MATCHER_INDEX);
        }
        return StringEscapeUtils.unescapeEcmaScript(key);
    }

    private void setSelectedItem(Object selectedItem) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) responseComboBox.getModel();

        if (selectedItem instanceof Entry) {
            /*
             * If the selected item is an Entry and the key ("d#") is the same as an Entry in the
             * model, then they're "the same", and that entry is selected.
             */
            for (int i = 0; i <= model.getSize() - 1; i++) {
                if (model.getElementAt(i) instanceof Entry && ((Entry<String, String>) selectedItem).getKey().equals(((Entry<String, String>) model.getElementAt(i)).getKey())) {
                    responseComboBox.setSelectedIndex(i);
                    return;
                }
            }
        } else if (model.getIndexOf(selectedItem) >= 0) {
            responseComboBox.setSelectedItem(selectedItem);
            return;
        }
        responseComboBox.setSelectedIndex(0);
    }

    public void resetInvalidProperties() {}

    public void updateQueueWarning(MessageStorageMode messageStorageMode) {
        switch (messageStorageMode) {
            case METADATA:
            case DISABLED:
                queueWarningLabel.setVisible(sourceQueueComboBox.getSelectedIndex() == 1);
                break;

            default:
                queueWarningLabel.setVisible(false);
                break;
        }
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
        queueWarningLabel = new javax.swing.JLabel();

        jLabel1.setText("jLabel1");

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Response Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        responseLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        responseLabel.setText("Response:");
        responseLabel.setMaximumSize(new java.awt.Dimension(62, 15));
        responseLabel.setMinimumSize(new java.awt.Dimension(62, 15));
        responseLabel.setPreferredSize(new java.awt.Dimension(62, 15));

        responseComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Auto-generate (After source transformer)", "None", "Auto-generate (Before processing)", "Auto-generate (After source transformer)", "Auto-generate (Destinations completed)", "Post-processor", "Destination 1" }));
        responseComboBox.setToolTipText("<html>Select a destination's response, the postprocessor return value, or a response map variable.<br/>Select <b>\"Auto-generate\"</b> to send a response generated by the inbound data type using the raw message:<br/>&nbsp;- <b>Before processing:</b> Response generated before the channel processes the message (SENT status)<br/>&nbsp;- <b>After source transformer:</b> Response generated after the channel processes the message (source status)<br/>&nbsp;- <b>Destinations completed:</b> Response generated after the channel processes the message, with a status<br/>&nbsp;&nbsp;&nbsp;&nbsp;based on the destination statuses, using a precedence of ERROR, QUEUED, SENT, FILTERED<br/></html>");
        responseComboBox.setMinimumSize(new java.awt.Dimension(150, 22));
        responseComboBox.setPreferredSize(new java.awt.Dimension(212, 22));

        sourceQueueLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        sourceQueueLabel.setText("Source Queue:");

        sourceQueueComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OFF (Respond after processing)", "ON (Respond before processing)" }));
        sourceQueueComboBox.setToolTipText("<html>Selecting OFF will process the message before sending the response (can use response from destinations)<br>Selecting ON will queue messages and immediately send a response (cannot use response from destinations)</html>");
        sourceQueueComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceQueueComboBoxActionPerformed(evt);
            }
        });

        queueWarningLabel.setForeground(new java.awt.Color(255, 0, 0));
        queueWarningLabel.setText("Queueing is not supported by the current message storage mode.");

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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sourceQueueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(queueWarningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(responseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 12, 12))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceQueueLabel)
                    .addComponent(sourceQueueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queueWarningLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sourceQueueComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceQueueComboBoxActionPerformed
        updateSelectedResponseItem();
    }//GEN-LAST:event_sourceQueueComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.ButtonGroup listenerButtonGroup;
    private javax.swing.JLabel queueWarningLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox responseComboBox;
    private javax.swing.JLabel responseLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox sourceQueueComboBox;
    private javax.swing.JLabel sourceQueueLabel;
    // End of variables declaration//GEN-END:variables
}
