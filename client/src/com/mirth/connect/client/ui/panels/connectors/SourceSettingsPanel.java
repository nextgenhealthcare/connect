/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.connectors;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.client.ui.ChannelSetup;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.donkey.model.channel.SourceConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Step;
import com.mirth.connect.util.JavaScriptSharedUtil;

public class SourceSettingsPanel extends JPanel {

    private Frame parent;
    private ChannelSetup channelSetup;
    private List<String> queueOnRespondFromNames;
    private List<Object> queueOffRespondFromNames;

    public SourceSettingsPanel() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initLayout();
        queueWarningLabel.setVisible(false);
    }

    public void setChannelSetup(ChannelSetup channelSetup) {
        this.channelSetup = channelSetup;
    }

    public void setProperties(SourceConnectorPropertiesInterface propertiesInterface) {
        SourceConnectorProperties properties = propertiesInterface.getSourceConnectorProperties();

        updateResponseDropDown(propertiesInterface, true);

        // Set the source queue combo box
        if (properties.isRespondAfterProcessing()) {
            sourceQueueComboBox.setSelectedIndex(0);
        } else {
            sourceQueueComboBox.setSelectedIndex(1);
        }

        if (properties.getQueueBufferSize() > 0) {
            queueBufferSizeField.setText(String.valueOf(properties.getQueueBufferSize()));
        } else {
            queueBufferSizeField.setText(String.valueOf(channelSetup.defaultQueueBufferSize));
        }

        processBatchLabel.setEnabled(propertiesInterface.canBatch());
        processBatchYesRadio.setEnabled(propertiesInterface.canBatch());
        processBatchNoRadio.setEnabled(propertiesInterface.canBatch());

        if (properties.isProcessBatch()) {
            processBatchYesRadio.setSelected(true);
        } else {
            processBatchNoRadio.setSelected(true);
        }

        batchResponseLabel.setEnabled(propertiesInterface.canBatch() && properties.isProcessBatch());
        batchResponseFirstRadio.setEnabled(propertiesInterface.canBatch() && properties.isProcessBatch());
        batchResponseLastRadio.setEnabled(propertiesInterface.canBatch() && properties.isProcessBatch());

        if (properties.isFirstResponse()) {
            batchResponseFirstRadio.setSelected(true);
        } else {
            batchResponseLastRadio.setSelected(true);
        }

        processingThreadsField.setText(String.valueOf(properties.getProcessingThreads()));
    }

    public void updateResponseDropDown(SourceConnectorPropertiesInterface propertiesInterface, boolean channelLoad) {
        SourceConnectorProperties properties = propertiesInterface.getSourceConnectorProperties();

        boolean enabled = parent.isSaveEnabled();
        Channel channel = parent.channelEditPanel.currentChannel;

        Set<Object> variables = new LinkedHashSet<Object>();

        variables.addAll(Arrays.asList(SourceConnectorProperties.QUEUE_OFF_RESPONSES));

        List<Rule> rulesToCheck = new ArrayList<Rule>();
        rulesToCheck.addAll(channel.getSourceConnector().getFilter().getElements());

        List<Step> stepsToCheck = new ArrayList<Step>();
        stepsToCheck.addAll(channel.getSourceConnector().getTransformer().getElements());

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
            rulesToCheck.addAll(connector.getFilter().getElements());
            stepsToCheck.addAll(connector.getTransformer().getElements());
            stepsToCheck.addAll(connector.getResponseTransformer().getElements());
        }

        for (Rule rule : rulesToCheck) {
            Collection<String> vars = rule.getResponseVariables();
            if (vars != null) {
                variables.addAll(vars);
            }
        }

        for (Iterator it = stepsToCheck.iterator(); it.hasNext();) {
            Step step = (Step) it.next();

            Collection<String> vars = step.getResponseVariables();
            if (vars != null) {
                variables.addAll(vars);
            }

            scripts.add(channel.getPreprocessingScript());
            scripts.add(channel.getPostprocessingScript());

            for (String script : scripts) {
                variables.addAll(JavaScriptSharedUtil.getResponseVariables(script));
            }
        }

        queueOnRespondFromNames = new ArrayList<String>(Arrays.asList(SourceConnectorProperties.QUEUE_ON_RESPONSES));
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
            queueBufferSizeLabel.setEnabled(false);
            queueBufferSizeField.setEnabled(false);
        } else {
            responseComboBox.setModel(new DefaultComboBoxModel(queueOnRespondFromNames.toArray()));
            queueBufferSizeLabel.setEnabled(true);
            queueBufferSizeField.setEnabled(true);
        }

        setSelectedItem(selectedItem);

        channelSetup.saveSourcePanel();
        MessageStorageMode messageStorageMode = channelSetup.getMessageStorageMode();
        channelSetup.updateQueueWarning(messageStorageMode);
        updateQueueWarning(messageStorageMode);
    }

    public void fillProperties(SourceConnectorPropertiesInterface propertiesInterface) {
        SourceConnectorProperties properties = propertiesInterface.getSourceConnectorProperties();

        properties.setQueueBufferSize(NumberUtils.toInt(queueBufferSizeField.getText()));

        if (responseComboBox.getSelectedItem() instanceof Entry) {
            properties.setResponseVariable(((Entry<String, String>) responseComboBox.getSelectedItem()).getKey());
        } else {
            properties.setResponseVariable((String) responseComboBox.getSelectedItem());
        }
        properties.setRespondAfterProcessing(sourceQueueComboBox.getSelectedIndex() == 0);

        properties.setProcessBatch(processBatchYesRadio.isSelected());

        properties.setFirstResponse(batchResponseFirstRadio.isSelected());

        properties.setProcessingThreads(NumberUtils.toInt(processingThreadsField.getText(), 0));
    }

    public boolean checkProperties(SourceConnectorPropertiesInterface propertiesInterface, boolean highlight) {
        SourceConnectorProperties properties = propertiesInterface.getSourceConnectorProperties();

        boolean valid = true;

        if (highlight && properties.getQueueBufferSize() <= 0) {
            queueBufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            valid = false;
        }

        if (properties.getProcessingThreads() <= 0) {
            processingThreadsField.setBackground(UIConstants.INVALID_COLOR);
            valid = false;
        }

        return valid;
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

    public void resetInvalidProperties() {
        queueBufferSizeField.setBackground(null);
        processingThreadsField.setBackground(null);
    }

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

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Source Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        sourceQueueLabel = new JLabel("Source Queue:");

        sourceQueueComboBox = new MirthComboBox();
        sourceQueueComboBox.setModel(new DefaultComboBoxModel(new String[] {
                "OFF (Respond after processing)", "ON (Respond before processing)" }));
        sourceQueueComboBox.setToolTipText("<html>Selecting OFF will process the message before sending the response (can use response from destinations)<br>Selecting ON will queue messages and immediately send a response (cannot use response from destinations)</html>");
        sourceQueueComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sourceQueueComboBoxActionPerformed(evt);
            }
        });

        queueWarningLabel = new JLabel("Queuing is not supported by the current message storage mode.");
        queueWarningLabel.setForeground(Color.RED);

        queueBufferSizeLabel = new JLabel("Queue Buffer Size:");
        queueBufferSizeField = new JTextField();
        queueBufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        queueBufferSizeField.setToolTipText("<html>The buffer size for the source queue.<br/>Up to this many connector messages may<br/>be held in memory at once when queuing.</html>");

        responseLabel = new JLabel("Response:");

        responseComboBox = new MirthComboBox();
        responseComboBox.setModel(new DefaultComboBoxModel(new String[] {
                "Auto-generate (After source transformer)", "None",
                "Auto-generate (Before processing)", "Auto-generate (After source transformer)",
                "Auto-generate (Destinations completed)", "Post-processor", "Destination 1" }));
        responseComboBox.setToolTipText("<html>Select a destination's response, the postprocessor return value, or a response map variable.<br/>Select <b>\"Auto-generate\"</b> to send a response generated by the inbound data type using the raw message:<br/>&nbsp;- <b>Before processing:</b> Response generated before the channel processes the message (SENT status)<br/>&nbsp;- <b>After source transformer:</b> Response generated after the channel processes the message (source status)<br/>&nbsp;- <b>Destinations completed:</b> Response generated after the channel processes the message, with a status<br/>&nbsp;&nbsp;&nbsp;&nbsp;based on the destination statuses, using a precedence of ERROR, QUEUED, SENT, FILTERED<br/></html>");

        processBatchLabel = new JLabel("Process Batch:");
        ButtonGroup processBatchButtonGroup = new ButtonGroup();

        processBatchYesRadio = new MirthRadioButton("Yes");
        processBatchYesRadio.setBackground(getBackground());
        processBatchYesRadio.setToolTipText("<html>Select Yes to enable batch processing. Batch messages are only supported if<br>the source connector's inbound properties contains a <b>Batch</b> section.</html>");
        processBatchYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                processBatchYesRadioActionPerformed(evt);
            }
        });
        processBatchButtonGroup.add(processBatchYesRadio);

        processBatchNoRadio = new MirthRadioButton("No");
        processBatchNoRadio.setBackground(getBackground());
        processBatchNoRadio.setToolTipText("<html>Select Yes to enable batch processing. Batch messages are only supported if<br>the source connector's inbound properties contains a <b>Batch</b> section.</html>");
        processBatchNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                processBatchNoRadioActionPerformed(evt);
            }
        });
        processBatchButtonGroup.add(processBatchNoRadio);

        batchResponseLabel = new JLabel("Batch Response:");
        ButtonGroup batchResponseButtonGroup = new ButtonGroup();

        batchResponseFirstRadio = new MirthRadioButton("First");
        batchResponseFirstRadio.setBackground(getBackground());
        batchResponseFirstRadio.setToolTipText("<html>Each message in the batch contains its own response that is generated via the method selected above.<br> Select either the response from the first or last message in the batch to be sent back to the originating system.</html>");
        batchResponseButtonGroup.add(batchResponseFirstRadio);

        batchResponseLastRadio = new MirthRadioButton("Last");
        batchResponseLastRadio.setBackground(getBackground());
        batchResponseLastRadio.setToolTipText("<html>Each message in the batch contains its own response that is generated via the method selected above.<br> Select either the response from the first or last message in the batch to be sent back to the originating system.</html>");
        batchResponseButtonGroup.add(batchResponseLastRadio);

        processingThreadsLabel = new JLabel("Max Processing Threads:");

        processingThreadsField = new MirthTextField();
        processingThreadsField.setDocument(new MirthFieldConstraints(0, false, false, true));
        processingThreadsField.setToolTipText("<html>The maximum number of messages that can process through<br/>the channel simultaneously. Note that when this value<br/>is greater than 1, message order is NOT guaranteed.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0, gap 6 4", "[]12[]"));

        add(sourceQueueLabel, "right");
        add(sourceQueueComboBox, "split");
        add(queueWarningLabel, "gapbefore 12");
        add(queueBufferSizeLabel, "newline, right");
        add(queueBufferSizeField, "w 50!");
        add(responseLabel, "newline, right");
        add(responseComboBox, "w 226:");
        add(processBatchLabel, "newline, right");
        add(processBatchYesRadio, "split");
        add(processBatchNoRadio);
        add(batchResponseLabel, "newline, right");
        add(batchResponseFirstRadio, "split");
        add(batchResponseLastRadio);
        add(processingThreadsLabel, "newline, right");
        add(processingThreadsField, "w 50!");
    }

    private void sourceQueueComboBoxActionPerformed(ActionEvent evt) {
        updateSelectedResponseItem();
    }

    private void processBatchYesRadioActionPerformed(ActionEvent evt) {
        batchResponseLabel.setEnabled(true);
        batchResponseFirstRadio.setEnabled(true);
        batchResponseLastRadio.setEnabled(true);
    }

    private void processBatchNoRadioActionPerformed(ActionEvent evt) {
        batchResponseLabel.setEnabled(false);
        batchResponseFirstRadio.setEnabled(false);
        batchResponseLastRadio.setEnabled(false);
    }

    private JLabel sourceQueueLabel;
    private MirthComboBox sourceQueueComboBox;
    private JLabel queueWarningLabel;
    private JLabel queueBufferSizeLabel;
    private JTextField queueBufferSizeField;
    private JLabel responseLabel;
    private MirthComboBox responseComboBox;
    private JLabel processBatchLabel;
    private MirthRadioButton processBatchYesRadio;
    private MirthRadioButton processBatchNoRadio;
    private JLabel batchResponseLabel;
    private MirthRadioButton batchResponseFirstRadio;
    private MirthRadioButton batchResponseLastRadio;
    private JLabel processingThreadsLabel;
    private MirthTextField processingThreadsField;
}