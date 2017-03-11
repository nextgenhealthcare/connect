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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.client.ui.ChannelSetup;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.model.MessageStorageMode;

public class DestinationSettingsPanel extends JPanel {

    private ChannelSetup channelSetup;
    private boolean regenerateTemplate;
    private boolean rotate;
    private boolean includeFilterTransformer;
    private int retryCount;
    private int retryIntervalMillis;
    private int threadCount;
    private String threadAssignmentVariable;
    private int queueBufferSize;

    public DestinationSettingsPanel() {
        initComponents();
        initLayout();
    }

    public void setChannelSetup(ChannelSetup channelSetup) {
        this.channelSetup = channelSetup;
    }

    public void setProperties(DestinationConnectorPropertiesInterface propertiesInterface) {
        DestinationConnectorProperties properties = propertiesInterface.getDestinationConnectorProperties();

        retryCount = properties.getRetryCount();

        if (properties.isQueueEnabled()) {
            if (properties.isSendFirst()) {
                queueMessagesOnFailureRadio.setSelected(true);
            } else {
                queueMessagesAlwaysRadio.setSelected(true);
            }
        } else {
            queueMessagesNeverRadio.setSelected(true);
        }
        updateQueueMessages();

        regenerateTemplate = properties.isRegenerateTemplate();
        rotate = properties.isRotate();
        includeFilterTransformer = properties.isIncludeFilterTransformer();
        retryIntervalMillis = properties.getRetryIntervalMillis();
        threadCount = properties.getThreadCount();
        threadAssignmentVariable = properties.getThreadAssignmentVariable();

        if (properties.getQueueBufferSize() > 0) {
            queueBufferSize = properties.getQueueBufferSize();
        } else {
            queueBufferSize = channelSetup.defaultQueueBufferSize;
        }

        validateResponseLabel.setEnabled(propertiesInterface.canValidateResponse());
        validateResponseYesRadio.setEnabled(propertiesInterface.canValidateResponse());
        validateResponseNoRadio.setEnabled(propertiesInterface.canValidateResponse());
        if (properties.isValidateResponse()) {
            validateResponseYesRadio.setSelected(true);
        } else {
            validateResponseNoRadio.setSelected(true);
        }

        if (properties.isReattachAttachments()) {
            reattachAttachmentsYesRadio.setSelected(true);
        } else {
            reattachAttachmentsNoRadio.setSelected(true);
        }

        updateAdvancedSettingsLabel();
    }

    public void fillProperties(DestinationConnectorPropertiesInterface propertiesInterface) {
        DestinationConnectorProperties properties = propertiesInterface.getDestinationConnectorProperties();

        if (queueMessagesAlwaysRadio.isSelected()) {
            properties.setQueueEnabled(true);
            properties.setSendFirst(false);
        } else if (queueMessagesNeverRadio.isSelected()) {
            properties.setQueueEnabled(false);
            properties.setSendFirst(false);
        } else {
            properties.setQueueEnabled(true);
            properties.setSendFirst(true);
        }

        properties.setRegenerateTemplate(regenerateTemplate);
        properties.setRetryIntervalMillis(retryIntervalMillis);
        properties.setRetryCount(retryCount);
        properties.setRotate(rotate);
        properties.setIncludeFilterTransformer(includeFilterTransformer);
        properties.setThreadCount(threadCount);
        properties.setThreadAssignmentVariable(threadAssignmentVariable);
        properties.setValidateResponse(validateResponseYesRadio.isSelected());
        properties.setQueueBufferSize(queueBufferSize);
        properties.setReattachAttachments(reattachAttachmentsYesRadio.isSelected());
    }

    public boolean checkProperties(DestinationConnectorPropertiesInterface propertiesInterface, boolean highlight) {
        return true;
    }

    public void resetInvalidProperties() {}

    private void updateQueueMessages() {
        channelSetup.saveDestinationPanel();
        MessageStorageMode messageStorageMode = channelSetup.getMessageStorageMode();
        channelSetup.updateQueueWarning(messageStorageMode);
        updateQueueWarning(messageStorageMode);
        updateAdvancedSettingsLabel();
    }

    public void updateQueueWarning(MessageStorageMode messageStorageMode) {
        switch (messageStorageMode) {
            case RAW:
            case METADATA:
            case DISABLED:
                if (queueMessagesAlwaysRadio.isSelected() || queueMessagesOnFailureRadio.isSelected()) {
                    queueMessagesWarningLabel.setText("<html>Queueing is not supported by the current message storage mode</html>");
                } else {
                    queueMessagesWarningLabel.setText("");
                }
                break;

            default:
                queueMessagesWarningLabel.setText("");
                break;
        }
    }

    private void updateAdvancedSettingsLabel() {
        List<String> list = new ArrayList<String>();
        boolean queueEnabled = !queueMessagesNeverRadio.isSelected();
        boolean sendFirst = queueEnabled && queueMessagesOnFailureRadio.isSelected();

        if (!queueEnabled) {
            list.add(String.valueOf(retryCount) + " Retr" + (retryCount == 1 ? "y" : "ies"));
            if (retryCount > 0) {
                list.add("Interval " + String.valueOf(retryIntervalMillis) + " ms");
            }
        } else {
            if (regenerateTemplate) {
                list.add("Regenerate");
            }

            if (rotate) {
                list.add("Rotate");
            }

            if (includeFilterTransformer) {
                list.add("Including Transformer");
            }

            if (sendFirst) {
                list.add(String.valueOf(retryCount) + " Retr" + (retryCount == 1 ? "y" : "ies"));
            }

            list.add("Interval " + String.valueOf(retryIntervalMillis) + " ms");

            if (threadCount > 1) {
                list.add(String.valueOf(threadCount) + " Threads");

                if (StringUtils.isNotBlank(threadAssignmentVariable)) {
                    list.add("Group By " + threadAssignmentVariable);
                }
            }
        }

        advancedQueueSettingsValueLabel.setText(StringUtils.join(list, " / "));
    }

    private void initComponents() {
        setLayout(new MigLayout("insets 4 8 4 4, novisualpadding, hidemode 3, fill", "[]13[grow]"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Destination Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        queueMessagesLabel = new JLabel("Queue Messages:");

        ButtonGroup queueMessagesButtonGroup = new ButtonGroup();
        ActionListener queueMessagesActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateQueueMessages();
            }
        };

        queueMessagesNeverRadio = new MirthRadioButton("Never");
        queueMessagesNeverRadio.setBackground(getBackground());
        queueMessagesNeverRadio.setToolTipText("Disable the destination queue.");
        queueMessagesNeverRadio.addActionListener(queueMessagesActionListener);
        queueMessagesButtonGroup.add(queueMessagesNeverRadio);

        queueMessagesOnFailureRadio = new MirthRadioButton("On Failure");
        queueMessagesOnFailureRadio.setBackground(getBackground());
        queueMessagesOnFailureRadio.setToolTipText("<html>Attempt to send the message first before queueing it. This will allow subsequent<br/>destinations and the Postprocessor to use the response from this destination if it<br/>successfully sends before queueing.</html>");
        queueMessagesOnFailureRadio.addActionListener(queueMessagesActionListener);
        queueMessagesButtonGroup.add(queueMessagesOnFailureRadio);

        queueMessagesAlwaysRadio = new MirthRadioButton("Always");
        queueMessagesAlwaysRadio.setBackground(getBackground());
        queueMessagesAlwaysRadio.setToolTipText("<html>Immediately queue the message. Subsequent destinations and the<br/>Postprocessor will always see this destination's response as QUEUED.</html>");
        queueMessagesAlwaysRadio.addActionListener(queueMessagesActionListener);
        queueMessagesButtonGroup.add(queueMessagesAlwaysRadio);

        queueMessagesWarningLabel = new JLabel();
        queueMessagesWarningLabel.setForeground(Color.RED);

        advancedQueueSettingsLabel = new JLabel("Advanced Queue Settings:");

        advancedQueueSettingsButton = new JButton(new ImageIcon(Frame.class.getResource("images/wrench.png")));
        advancedQueueSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                new AdvancedDialog();
            }
        });

        advancedQueueSettingsValueLabel = new JLabel();

        validateResponseLabel = new JLabel("Validate Response:");

        ButtonGroup validateResponseButtonGroup = new ButtonGroup();
        String toolTipText = "<html>Select Yes to validate the response. Responses can only be validated if the<br>response transformer's inbound properties contains a <b>Response Validation</b><br>section. If validation fails, the message will be marked as queued or errored.</html>";

        validateResponseYesRadio = new MirthRadioButton("Yes");
        validateResponseYesRadio.setBackground(getBackground());
        validateResponseYesRadio.setToolTipText(toolTipText);
        validateResponseButtonGroup.add(validateResponseYesRadio);

        validateResponseNoRadio = new MirthRadioButton("No");
        validateResponseNoRadio.setBackground(getBackground());
        validateResponseNoRadio.setToolTipText(toolTipText);
        validateResponseButtonGroup.add(validateResponseNoRadio);

        reattachAttachmentsLabel = new JLabel("Reattach Attachments:");

        ButtonGroup reattachAttachmentsButtonGroup = new ButtonGroup();
        toolTipText = "<html>If enabled, replacement tokens using the ${ATTACH:...} syntax will<br/>automatically be replaced with the associated attachment content<br/>before the message is sent. If disabled, the tokens will be<br/>expanded to the full ${ATTACH:channelId:messageId:attachmentId}<br/>syntax which can then be reattached in downstream channels.</html>";

        reattachAttachmentsYesRadio = new MirthRadioButton("Yes");
        reattachAttachmentsYesRadio.setBackground(getBackground());
        reattachAttachmentsYesRadio.setToolTipText(toolTipText);
        reattachAttachmentsButtonGroup.add(reattachAttachmentsYesRadio);

        reattachAttachmentsNoRadio = new MirthRadioButton("No");
        reattachAttachmentsNoRadio.setBackground(getBackground());
        reattachAttachmentsNoRadio.setToolTipText(toolTipText);
        reattachAttachmentsButtonGroup.add(reattachAttachmentsNoRadio);
    }

    private void initLayout() {
        add(queueMessagesLabel, "right");
        add(queueMessagesNeverRadio, "split");
        add(queueMessagesOnFailureRadio);
        add(queueMessagesAlwaysRadio);
        add(queueMessagesWarningLabel, "gapbefore 16");
        add(advancedQueueSettingsLabel, "newline, right");
        add(advancedQueueSettingsButton, "h 22!, w 22!, split");
        add(advancedQueueSettingsValueLabel);
        add(validateResponseLabel, "newline, right");
        add(validateResponseYesRadio, "split");
        add(validateResponseNoRadio);
        add(reattachAttachmentsLabel, "newline, right");
        add(reattachAttachmentsYesRadio, "split");
        add(reattachAttachmentsNoRadio);
    }

    private class AdvancedDialog extends MirthDialog {

        public AdvancedDialog() {
            super(PlatformUI.MIRTH_FRAME, true);
            setTitle("Settings");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            initComponents();
            initLayout();
            setProperties();
            pack();
            setLocationRelativeTo(getOwner());
            setVisible(true);
        }

        private void setProperties() {
            if (regenerateTemplate) {
                regenerateTemplateYesRadio.setSelected(true);
            } else {
                regenerateTemplateNoRadio.setSelected(true);
            }

            if (rotate) {
                rotateYesRadio.setSelected(true);
            } else {
                rotateNoRadio.setSelected(true);
            }

            if (includeFilterTransformer) {
                includeFilterTransformerYesRadio.setSelected(true);
            } else {
                includeFilterTransformerNoRadio.setSelected(true);
            }

            retryCountField.setText(String.valueOf(retryCount));
            retryIntervalField.setText(String.valueOf(retryIntervalMillis));
            queueThreadsField.setText(String.valueOf(threadCount));
            threadAssignmentVariableField.setText(StringUtils.defaultString(threadAssignmentVariable));

            boolean queueEnabled = !queueMessagesNeverRadio.isSelected();
            boolean sendFirst = queueMessagesOnFailureRadio.isSelected();

            regenerateTemplateLabel.setEnabled(queueEnabled);
            regenerateTemplateYesRadio.setEnabled(queueEnabled);
            regenerateTemplateNoRadio.setEnabled(queueEnabled);
            rotateLabel.setEnabled(queueEnabled);
            rotateYesRadio.setEnabled(queueEnabled);
            rotateNoRadio.setEnabled(queueEnabled);
            includeFilterTransformerLabel.setEnabled(queueEnabled && regenerateTemplate);
            includeFilterTransformerYesRadio.setEnabled(queueEnabled && regenerateTemplate);
            includeFilterTransformerNoRadio.setEnabled(queueEnabled && regenerateTemplate);
            retryCountLabel.setEnabled(!queueEnabled || sendFirst);
            retryCountField.setEnabled(!queueEnabled || sendFirst);
            retryIntervalLabel.setEnabled(queueEnabled || retryCount > 0);
            retryIntervalField.setEnabled(queueEnabled || retryCount > 0);
            queueThreadsLabel.setEnabled(queueEnabled);
            queueThreadsField.setEnabled(queueEnabled);
            threadAssignmentVariableLabel.setEnabled(queueEnabled && threadCount > 1);
            threadAssignmentVariableField.setEnabled(queueEnabled && threadCount > 1);
            queueBufferSizeLabel.setEnabled(queueEnabled);
            queueBufferSizeField.setEnabled(queueEnabled);
            queueBufferSizeField.setText(String.valueOf(queueBufferSize));
        }

        private boolean saveProperties() {
            retryCountField.setBackground(null);
            retryIntervalField.setBackground(null);
            queueThreadsField.setBackground(null);

            String errors = "";

            if (retryCountField.isEnabled() && StringUtils.isBlank(retryCountField.getText())) {
                errors += "Retry count cannot be blank.\n";
                retryCountField.setBackground(UIConstants.INVALID_COLOR);
            }

            if (retryIntervalField.isEnabled() && NumberUtils.toInt(retryIntervalField.getText(), 0) <= 0) {
                errors += "Retry interval must be greater than zero.\n";
                retryIntervalField.setBackground(UIConstants.INVALID_COLOR);
            }

            if (queueThreadsField.isEnabled() && NumberUtils.toInt(queueThreadsField.getText(), 0) <= 0) {
                errors += "Queue threads must be greater than zero.\n";
                queueThreadsField.setBackground(UIConstants.INVALID_COLOR);
            }

            if (NumberUtils.toInt(queueBufferSizeField.getText()) <= 0) {
                queueBufferSizeField.setBackground(UIConstants.INVALID_COLOR);
                errors += "Queue buffer size must be greater than zero.\n";
            }

            if (StringUtils.isNotBlank(errors)) {
                PlatformUI.MIRTH_FRAME.alertError(this, errors);
                return false;
            }

            regenerateTemplate = regenerateTemplateYesRadio.isSelected();
            rotate = rotateYesRadio.isSelected();
            includeFilterTransformer = includeFilterTransformerYesRadio.isSelected();
            retryCount = NumberUtils.toInt(retryCountField.getText(), 0);
            retryIntervalMillis = NumberUtils.toInt(retryIntervalField.getText(), 0);
            threadCount = NumberUtils.toInt(queueThreadsField.getText(), 1);
            threadAssignmentVariable = threadAssignmentVariableField.getText();
            queueBufferSize = NumberUtils.toInt(queueBufferSizeField.getText());

            updateAdvancedSettingsLabel();
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            return true;
        }

        private void initComponents() {
            setBackground(UIConstants.BACKGROUND_COLOR);
            getContentPane().setBackground(getBackground());

            containerPanel = new JPanel();
            containerPanel.setBackground(getBackground());
            containerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(204, 204, 204)), "Advanced Queue Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

            retryCountLabel = new JLabel("Retry Count Before Queue/Error:");

            retryCountField = new JTextField();
            retryCountField.setToolTipText("<html>The maximum number of times the connector will attempt to send<br/>the message before queueing or erroring.</html>");
            retryCountField.setDocument(new MirthFieldConstraints(0, false, false, true));
            retryCountField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent evt) {
                    retryCountChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent evt) {
                    retryCountChanged();
                }

                @Override
                public void changedUpdate(DocumentEvent evt) {
                    retryCountChanged();
                }
            });

            retryIntervalLabel = new JLabel("Retry Interval (ms):");

            retryIntervalField = new JTextField();
            retryIntervalField.setToolTipText("<html>The amount of time that should elapse between retry attempts to send<br/>messages. This interval applies to both the queue and initial retry attempts.</html>");
            retryIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));

            regenerateTemplateLabel = new JLabel("Regenerate Template:");

            ButtonGroup regenerateTemplateButtonGroup = new ButtonGroup();
            String toolTipText = "<html>Regenerate the template and other connector properties by replacing variables<br/>each time the connector attempts to send the message from the queue. If this is<br/>disabled, the original variable replacements will be used for each attempt.</html>";
            ActionListener regenerateTemplateActionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    regenerateTemplateChanged();
                }
            };

            regenerateTemplateYesRadio = new JRadioButton("Yes");
            regenerateTemplateYesRadio.setBackground(getBackground());
            regenerateTemplateYesRadio.setToolTipText(toolTipText);
            regenerateTemplateYesRadio.addActionListener(regenerateTemplateActionListener);
            regenerateTemplateButtonGroup.add(regenerateTemplateYesRadio);

            regenerateTemplateNoRadio = new JRadioButton("No");
            regenerateTemplateNoRadio.setBackground(getBackground());
            regenerateTemplateNoRadio.setToolTipText(toolTipText);
            regenerateTemplateNoRadio.addActionListener(regenerateTemplateActionListener);
            regenerateTemplateButtonGroup.add(regenerateTemplateNoRadio);

            rotateLabel = new JLabel("Rotate Queue:");

            ButtonGroup rotateButtonGroup = new ButtonGroup();
            toolTipText = "<html>If enabled, when any message fails to be sent from the queue, the connector will<br/>place the message at the end of the queue and attempt to send the next message.<br/>This will prevent a single message from holding up the entire queue. If the order<br/>of messages processed is important, this should be disabled.</html>";

            rotateYesRadio = new JRadioButton("Yes");
            rotateYesRadio.setBackground(getBackground());
            rotateYesRadio.setToolTipText(toolTipText);
            rotateButtonGroup.add(rotateYesRadio);

            rotateNoRadio = new JRadioButton("No");
            rotateNoRadio.setBackground(getBackground());
            rotateNoRadio.setToolTipText(toolTipText);
            rotateButtonGroup.add(rotateNoRadio);

            includeFilterTransformerLabel = new JLabel("Include Filter/Transformer:");

            ButtonGroup includeFilterTransformerButtonGroup = new ButtonGroup();
            toolTipText = "<html>If enabled, the filter and transformer will be re-executed<br/>before every queue send attempt. This is only available<br/>when the Regenerate Template setting is enabled.</html>";

            includeFilterTransformerYesRadio = new JRadioButton("Yes");
            includeFilterTransformerYesRadio.setBackground(getBackground());
            includeFilterTransformerYesRadio.setToolTipText(toolTipText);
            includeFilterTransformerButtonGroup.add(includeFilterTransformerYesRadio);

            includeFilterTransformerNoRadio = new JRadioButton("No");
            includeFilterTransformerNoRadio.setBackground(getBackground());
            includeFilterTransformerNoRadio.setToolTipText(toolTipText);
            includeFilterTransformerButtonGroup.add(includeFilterTransformerNoRadio);

            queueThreadsLabel = new JLabel("Queue Threads:");

            queueThreadsField = new JTextField();
            queueThreadsField.setToolTipText("<html>The number of threads that will read from the queue and dispatch<br/>messages simultaneously. Message order is NOT guaranteed if this<br/>value is greater than one, unless an assignment variable is used below.</html>");
            queueThreadsField.setDocument(new MirthFieldConstraints(0, false, false, true));
            queueThreadsField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent evt) {
                    queueThreadsChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent evt) {
                    queueThreadsChanged();
                }

                @Override
                public void changedUpdate(DocumentEvent evt) {
                    queueThreadsChanged();
                }
            });

            threadAssignmentVariableLabel = new JLabel("Thread Assignment Variable:");

            threadAssignmentVariableField = new JTextField();
            threadAssignmentVariableField.setToolTipText("<html>When using multiple queue threads, this map variable<br/>determines how to assign messages to specific threads.<br/>If rotation is disabled, messages with the same thread<br/>assignment value will always be processed in order.</html>");

            queueBufferSizeLabel = new JLabel("Queue Buffer Size:");
            queueBufferSizeField = new JTextField();
            queueBufferSizeField.setDocument(new MirthFieldConstraints(0, false, false, true));
            queueBufferSizeField.setToolTipText("<html>The buffer size for the destination queue.<br/>Up to this many connector messages may<br/>be held in memory at once when queuing.</html>");

            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (saveProperties()) {
                        dispose();
                    }
                }
            });

            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    dispose();
                }
            });
        }

        private void initLayout() {
            setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill", "", "[grow][][]"));

            containerPanel.setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, fill", "[]13[grow]", "[][][][][][][][][grow]"));
            containerPanel.add(retryCountLabel, "right");
            containerPanel.add(retryCountField, "w 75!");
            containerPanel.add(retryIntervalLabel, "newline, right");
            containerPanel.add(retryIntervalField, "w 75!");
            containerPanel.add(rotateLabel, "newline, right");
            containerPanel.add(rotateYesRadio, "split");
            containerPanel.add(rotateNoRadio);
            containerPanel.add(regenerateTemplateLabel, "newline, right");
            containerPanel.add(regenerateTemplateYesRadio, "split");
            containerPanel.add(regenerateTemplateNoRadio);
            containerPanel.add(includeFilterTransformerLabel, "newline, right");
            containerPanel.add(includeFilterTransformerYesRadio, "split");
            containerPanel.add(includeFilterTransformerNoRadio);
            containerPanel.add(queueThreadsLabel, "newline, right");
            containerPanel.add(queueThreadsField, "w 75!");
            containerPanel.add(threadAssignmentVariableLabel, "newline, right");
            containerPanel.add(threadAssignmentVariableField, "w 75!");
            containerPanel.add(queueBufferSizeLabel, "newline, right");
            containerPanel.add(queueBufferSizeField, "w 75!");
            add(containerPanel, "grow, push");

            add(new JSeparator(), "newline, growx, sx");

            add(okButton, "newline, w 50!, sx, right, split");
            add(cancelButton, "w 50!");
        }

        private void retryCountChanged() {
            if (NumberUtils.toInt(retryCountField.getText()) > 0) {
                retryIntervalField.setEnabled(true);
                retryIntervalLabel.setEnabled(true);
            } else if (queueMessagesNeverRadio.isSelected()) {
                retryIntervalField.setEnabled(false);
                retryIntervalLabel.setEnabled(false);
            }
        }

        private void regenerateTemplateChanged() {
            boolean enabled = !queueMessagesNeverRadio.isSelected() && regenerateTemplateYesRadio.isSelected();
            includeFilterTransformerLabel.setEnabled(enabled);
            includeFilterTransformerYesRadio.setEnabled(enabled);
            includeFilterTransformerNoRadio.setEnabled(enabled);
        }

        private void queueThreadsChanged() {
            int threadCount = NumberUtils.toInt(queueThreadsField.getText(), 0);
            threadAssignmentVariableLabel.setEnabled(threadCount > 1);
            threadAssignmentVariableField.setEnabled(threadCount > 1);
        }

        private JPanel containerPanel;
        private JLabel regenerateTemplateLabel;
        private JRadioButton regenerateTemplateYesRadio;
        private JRadioButton regenerateTemplateNoRadio;
        private JLabel rotateLabel;
        private JRadioButton rotateYesRadio;
        private JRadioButton rotateNoRadio;
        private JLabel includeFilterTransformerLabel;
        private JRadioButton includeFilterTransformerYesRadio;
        private JRadioButton includeFilterTransformerNoRadio;
        private JLabel retryCountLabel;
        private JTextField retryCountField;
        private JLabel retryIntervalLabel;
        private JTextField retryIntervalField;
        private JLabel queueThreadsLabel;
        private JTextField queueThreadsField;
        private JLabel threadAssignmentVariableLabel;
        private JTextField threadAssignmentVariableField;
        private JLabel queueBufferSizeLabel;
        private JTextField queueBufferSizeField;
        private JButton okButton;
        private JButton cancelButton;
    }

    private JLabel queueMessagesLabel;
    private JRadioButton queueMessagesNeverRadio;
    private JRadioButton queueMessagesOnFailureRadio;
    private JRadioButton queueMessagesAlwaysRadio;
    private JLabel queueMessagesWarningLabel;
    private JLabel advancedQueueSettingsLabel;
    private JButton advancedQueueSettingsButton;
    private JLabel advancedQueueSettingsValueLabel;
    private JLabel validateResponseLabel;
    private JRadioButton validateResponseYesRadio;
    private JRadioButton validateResponseNoRadio;
    private JLabel reattachAttachmentsLabel;
    private JRadioButton reattachAttachmentsYesRadio;
    private JRadioButton reattachAttachmentsNoRadio;
}