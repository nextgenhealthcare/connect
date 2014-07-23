/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.connectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.client.ui.ChannelSetup;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.model.MessageStorageMode;

public class DestinationSettingsPanel extends javax.swing.JPanel {
    private ChannelSetup channelSetup;

    public DestinationSettingsPanel() {
        initComponents();
        retryIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        retryCountField.setDocument(new MirthFieldConstraints(0, false, false, true));
        queueThreadsField.setDocument(new MirthFieldConstraints(0, false, false, true));
    }

    public void setChannelSetup(ChannelSetup channelSetup) {
        this.channelSetup = channelSetup;
    }

    public void setProperties(DestinationConnectorPropertiesInterface propertiesInterface) {
        DestinationConnectorProperties properties = propertiesInterface.getDestinationConnectorProperties();

        // Set the retry count first because it may be used by the radio action event
        retryCountField.setText(Integer.toString(properties.getRetryCount()));

        if (properties.isQueueEnabled()) {
            if (properties.isSendFirst()) {
                queueAttemptFirstRadio.setSelected(true);
                queueAttemptFirstRadioActionPerformed(null);
            } else {
                queueAlwaysRadio.setSelected(true);
                queueAlwaysRadioActionPerformed(null);
            }
        } else {
            queueNeverRadio.setSelected(true);
            queueNeverRadioActionPerformed(null);
        }

        regenerateTemplateCheckbox.setSelected(properties.isRegenerateTemplate());
        rotateCheckbox.setSelected(properties.isRotate());

        retryIntervalField.setText(String.valueOf(properties.getRetryIntervalMillis()));

        queueThreadsField.setText(String.valueOf(properties.getThreadCount()));

        validateResponseLabel.setEnabled(propertiesInterface.canValidateResponse());
        validateResponseYesRadio.setEnabled(propertiesInterface.canValidateResponse());
        validateResponseNoRadio.setEnabled(propertiesInterface.canValidateResponse());
        if (properties.isValidateResponse()) {
            validateResponseYesRadio.setSelected(true);
        } else {
            validateResponseNoRadio.setSelected(true);
        }
    }

    public void fillProperties(DestinationConnectorPropertiesInterface propertiesInterface) {
        DestinationConnectorProperties properties = propertiesInterface.getDestinationConnectorProperties();

        if (queueAlwaysRadio.isSelected()) {
            properties.setQueueEnabled(true);
            properties.setSendFirst(false);
        } else if (queueNeverRadio.isSelected()) {
            properties.setQueueEnabled(false);
            properties.setSendFirst(false);
        } else {
            properties.setQueueEnabled(true);
            properties.setSendFirst(true);
        }

        properties.setRegenerateTemplate(regenerateTemplateCheckbox.isSelected());

        properties.setRetryIntervalMillis(NumberUtils.toInt(retryIntervalField.getText(), -1));

        properties.setRetryCount(NumberUtils.toInt(retryCountField.getText(), -1));

        properties.setRotate(rotateCheckbox.isSelected());

        properties.setThreadCount(NumberUtils.toInt(queueThreadsField.getText(), -1));

        properties.setValidateResponse(validateResponseYesRadio.isSelected());
    }

    public boolean checkProperties(DestinationConnectorPropertiesInterface propertiesInterface, boolean highlight) {
        DestinationConnectorProperties properties = propertiesInterface.getDestinationConnectorProperties();

        boolean valid = true;

        // TODO: Queue properties checks don't work properly with ints
        if (properties.isQueueEnabled() || properties.getRetryCount() > 0) {
            if (properties.getRetryIntervalMillis() <= 0) {
                valid = false;
                if (highlight) {
                    retryIntervalField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        if (properties.getRetryCount() < 0) {
            valid = false;

            if (highlight) {
                retryCountField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (properties.getThreadCount() < 1) {
            valid = false;

            if (highlight) {
                queueThreadsField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    public void resetInvalidProperties() {
        retryIntervalField.setBackground(null);
        retryCountField.setBackground(null);
        queueThreadsField.setBackground(null);
    }

    public void updateQueueWarning(MessageStorageMode messageStorageMode) {
        switch (messageStorageMode) {
            case RAW:
            case METADATA:
            case DISABLED:
                if (queueAlwaysRadio.isSelected() || queueAttemptFirstRadio.isSelected()) {
                    queueWarningLabel.setText("<html>Queueing is not supported by the current message storage mode</html>");
                } else {
                    queueWarningLabel.setText("");
                }
                break;

            default:
                queueWarningLabel.setText("");
                break;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        queueButtonGroup = new javax.swing.ButtonGroup();
        validateResponseButtonGroup = new javax.swing.ButtonGroup();
        queueMessagesLabel = new javax.swing.JLabel();
        queueAlwaysRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        retryIntervalLabel = new javax.swing.JLabel();
        retryIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        queueNeverRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        queueAttemptFirstRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        retryCountLabel = new javax.swing.JLabel();
        retryCountField = new com.mirth.connect.client.ui.components.MirthTextField();
        regenerateTemplateCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        queueWarningLabel = new javax.swing.JLabel();
        rotateCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        queueThreadsField = new com.mirth.connect.client.ui.components.MirthTextField();
        queueThreadsLabel = new javax.swing.JLabel();
        validateResponseLabel = new javax.swing.JLabel();
        validateResponseYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        validateResponseNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Destination Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        queueMessagesLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        queueMessagesLabel.setText("Queue Messages:");

        queueAlwaysRadio.setBackground(new java.awt.Color(255, 255, 255));
        queueAlwaysRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        queueButtonGroup.add(queueAlwaysRadio);
        queueAlwaysRadio.setText("Always");
        queueAlwaysRadio.setToolTipText("<html>\nImmediately queue the message. Subsequent destinations and the<br/>\nPostprocessor will always see this destination's response as QUEUED.\n</html>");
        queueAlwaysRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        queueAlwaysRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queueAlwaysRadioActionPerformed(evt);
            }
        });

        retryIntervalLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        retryIntervalLabel.setText("Retry Interval (ms):");

        retryIntervalField.setToolTipText("<html>\nThe amount of time that should elapse between retry attempts to send<br/>\nmessages. This interval applies to both the queue and initial retry attempts.\n</html>");

        queueNeverRadio.setBackground(new java.awt.Color(255, 255, 255));
        queueNeverRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        queueButtonGroup.add(queueNeverRadio);
        queueNeverRadio.setText("Never");
        queueNeverRadio.setToolTipText("Disable the destination queue.");
        queueNeverRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        queueNeverRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queueNeverRadioActionPerformed(evt);
            }
        });

        queueAttemptFirstRadio.setBackground(new java.awt.Color(255, 255, 255));
        queueAttemptFirstRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        queueButtonGroup.add(queueAttemptFirstRadio);
        queueAttemptFirstRadio.setText("Attempt First");
        queueAttemptFirstRadio.setToolTipText("<html>\nAttempt to send the message first before queueing it. This will allow subsequent<br/>\ndestinations and the Postprocessor to use the response from this destination if it<br/>\nsuccessfully sends before queueing.\n</html>");
        queueAttemptFirstRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        queueAttemptFirstRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queueAttemptFirstRadioActionPerformed(evt);
            }
        });

        retryCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        retryCountLabel.setText("Retry Count Before Queue/Error:");

        retryCountField.setToolTipText("<html>\nThe maximum number of times the connector will attempt to send<br/>\nthe message before queueing or erroring.\n</html>");
        retryCountField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                retryCountFieldKeyReleased(evt);
            }
        });

        regenerateTemplateCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        regenerateTemplateCheckbox.setText("Regenerate Template");
        regenerateTemplateCheckbox.setToolTipText("<html>\nRegenerate the template and other connector properties by replacing variables<br/>\neach time the connector attempts to send the message from the queue. If this is<br/>\ndisabled, the original variable replacements will be used for each attempt.\n</html>");

        queueWarningLabel.setForeground(new java.awt.Color(255, 0, 0));
        queueWarningLabel.setText("<html>test text</html>");
        queueWarningLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        rotateCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        rotateCheckbox.setText("Rotate");
        rotateCheckbox.setToolTipText("<html>\nIf checked, when any message fails to be sent from the queue, the connector will<br/>\nplace the message at the end of the queue and attempt to send the next message.<br/>\nThis will prevent a single message from holding up the entire queue. If the order<br/>\nof messages processed is important, this should be unchecked.</html>");

        queueThreadsField.setToolTipText("<html>\nThe number of threads that will read from the queue and dispatch<br/>\nmessages simultaneously. Message order is NOT guaranteed if this<br/>\nvalue is greater than one.\n</html>");

        queueThreadsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        queueThreadsLabel.setText("Queue Threads:");

        validateResponseLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        validateResponseLabel.setText("Validate Response:");

        validateResponseYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        validateResponseYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validateResponseButtonGroup.add(validateResponseYesRadio);
        validateResponseYesRadio.setText("Yes");
        validateResponseYesRadio.setToolTipText("<html>Select Yes to validate the response. Responses can only be validated if the<br>response transformer's inbound properties contains a <b>Response Validation</b><br>section. If validation fails, the message will be marked as queued or errored. </html>");
        validateResponseYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        validateResponseNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        validateResponseNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validateResponseButtonGroup.add(validateResponseNoRadio);
        validateResponseNoRadio.setSelected(true);
        validateResponseNoRadio.setText("No");
        validateResponseNoRadio.setToolTipText("<html>Select Yes to validate the response. Responses can only be validated if the<br>response transformer's inbound properties contains a <b>Response Validation</b><br>section. If validation fails, the message will be marked as queued or errored. </html>");
        validateResponseNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(validateResponseLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(queueThreadsLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(retryIntervalLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(retryCountLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(queueMessagesLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(retryCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(retryIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(queueThreadsField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(queueWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(queueNeverRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(queueAttemptFirstRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(queueAlwaysRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(regenerateTemplateCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rotateCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(validateResponseYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(validateResponseNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(rotateCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(regenerateTemplateCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queueAlwaysRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queueAttemptFirstRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queueNeverRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queueMessagesLabel))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(retryCountLabel)
                            .addComponent(retryCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(retryIntervalLabel)
                            .addComponent(retryIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(queueWarningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(queueThreadsLabel)
                    .addComponent(queueThreadsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(validateResponseNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(validateResponseYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(validateResponseLabel)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void queueAlwaysRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queueAlwaysRadioActionPerformed
        regenerateTemplateCheckbox.setEnabled(true);
        rotateCheckbox.setEnabled(true);
        retryIntervalField.setEnabled(true);
        retryIntervalLabel.setEnabled(true);
        retryCountLabel.setEnabled(false);
        retryCountField.setEnabled(false);
        queueThreadsLabel.setEnabled(true);
        queueThreadsField.setEnabled(true);
        channelSetup.saveDestinationPanel();

        MessageStorageMode messageStorageMode = channelSetup.getMessageStorageMode();
        channelSetup.updateQueueWarning(messageStorageMode);
        updateQueueWarning(messageStorageMode);
    }//GEN-LAST:event_queueAlwaysRadioActionPerformed

    private void queueNeverRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queueNeverRadioActionPerformed
        regenerateTemplateCheckbox.setEnabled(false);
        rotateCheckbox.setEnabled(false);

        if (NumberUtils.toInt(retryCountField.getText()) == 0) {
            retryIntervalField.setEnabled(false);
            retryIntervalLabel.setEnabled(false);
        } else {
            retryIntervalField.setEnabled(true);
            retryIntervalLabel.setEnabled(true);
        }

        retryCountLabel.setEnabled(true);
        retryCountField.setEnabled(true);

        queueThreadsLabel.setEnabled(false);
        queueThreadsField.setEnabled(false);

        channelSetup.saveDestinationPanel();

        MessageStorageMode messageStorageMode = channelSetup.getMessageStorageMode();
        channelSetup.updateQueueWarning(messageStorageMode);
        updateQueueWarning(messageStorageMode);
    }//GEN-LAST:event_queueNeverRadioActionPerformed

    private void queueAttemptFirstRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queueAttemptFirstRadioActionPerformed
        regenerateTemplateCheckbox.setEnabled(true);
        rotateCheckbox.setEnabled(true);
        retryIntervalField.setEnabled(true);
        retryIntervalLabel.setEnabled(true);
        retryCountLabel.setEnabled(true);
        retryCountField.setEnabled(true);
        queueThreadsLabel.setEnabled(true);
        queueThreadsField.setEnabled(true);
        channelSetup.saveDestinationPanel();

        MessageStorageMode messageStorageMode = channelSetup.getMessageStorageMode();
        channelSetup.updateQueueWarning(messageStorageMode);
        updateQueueWarning(messageStorageMode);
    }//GEN-LAST:event_queueAttemptFirstRadioActionPerformed

    private void retryCountFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_retryCountFieldKeyReleased
        if (NumberUtils.toInt(retryCountField.getText()) > 0) {
            retryIntervalField.setEnabled(true);
            retryIntervalLabel.setEnabled(true);
        } else if (queueNeverRadio.isSelected()) {
            retryIntervalField.setEnabled(false);
            retryIntervalLabel.setEnabled(false);
        }
    }//GEN-LAST:event_retryCountFieldKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthRadioButton queueAlwaysRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton queueAttemptFirstRadio;
    private javax.swing.ButtonGroup queueButtonGroup;
    private javax.swing.JLabel queueMessagesLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton queueNeverRadio;
    private com.mirth.connect.client.ui.components.MirthTextField queueThreadsField;
    private javax.swing.JLabel queueThreadsLabel;
    private javax.swing.JLabel queueWarningLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox regenerateTemplateCheckbox;
    private com.mirth.connect.client.ui.components.MirthTextField retryCountField;
    private javax.swing.JLabel retryCountLabel;
    private com.mirth.connect.client.ui.components.MirthTextField retryIntervalField;
    private javax.swing.JLabel retryIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox rotateCheckbox;
    private javax.swing.ButtonGroup validateResponseButtonGroup;
    private javax.swing.JLabel validateResponseLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton validateResponseNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton validateResponseYesRadio;
    // End of variables declaration//GEN-END:variables
}
