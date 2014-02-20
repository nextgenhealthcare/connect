/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mllpmode;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.util.TcpUtil;

public class MLLPModeSettingsDialog extends JDialog implements DocumentListener {

    private boolean saved;
    private String startOfMessageAbbreviation;
    private String endOfMessageAbbreviation;
    private String ackAbbreviation;
    private String nackAbbreviation;
    private ActionListener actionListener;

    /**
     * Creates new form MLLPModeSettingsDialog
     */
    public MLLPModeSettingsDialog(ActionListener actionListener) {
        super(PlatformUI.MIRTH_FRAME);
        initComponents();
        this.actionListener = actionListener;

        startOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, false, false));
        endOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, false, false));
        ackBytesField.setDocument(new MirthFieldConstraints(0, true, false, false));
        nackBytesField.setDocument(new MirthFieldConstraints(0, true, false, false));
        maxRetryCountField.setDocument(new MirthFieldConstraints(0, false, false, true));

        startOfMessageBytesField.getDocument().addDocumentListener(this);
        endOfMessageBytesField.getDocument().addDocumentListener(this);
        ackBytesField.getDocument().addDocumentListener(this);
        nackBytesField.getDocument().addDocumentListener(this);

        startOfMessageAbbreviation = "";
        endOfMessageAbbreviation = "";
        ackAbbreviation = "";
        nackAbbreviation = "";
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            Dimension dlgSize = getPreferredSize();
            Dimension frmSize = getParent().getSize();
            Point loc = getParent().getLocation();

            if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
                setLocationRelativeTo(null);
            } else {
                setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
            }

            saved = false;
            resetInvalidProperties();
        }

        super.setVisible(b);
    }

    public TransmissionModeProperties getProperties() {
        MLLPModeProperties props = new MLLPModeProperties();

        props.setStartOfMessageBytes(startOfMessageBytesField.getText());
        props.setEndOfMessageBytes(endOfMessageBytesField.getText());
        props.setUseMLLPv2(useMLLPv2YesRadio.isSelected());
        props.setAckBytes(ackBytesField.getText());
        props.setNackBytes(nackBytesField.getText());
        props.setMaxRetries(maxRetryCountField.getText());

        return props;
    }

    public void setProperties(TransmissionModeProperties properties) {
        MLLPModeProperties props = (MLLPModeProperties) properties;

        startOfMessageBytesField.setText(props.getStartOfMessageBytes());
        endOfMessageBytesField.setText(props.getEndOfMessageBytes());

        if (props.isUseMLLPv2()) {
            useMLLPv2YesRadio.setSelected(true);
            useMLLPv2YesRadioActionPerformed(null);
        } else {
            useMLLPv2NoRadio.setSelected(true);
            useMLLPv2NoRadioActionPerformed(null);
        }

        ackBytesField.setText(props.getAckBytes());
        nackBytesField.setText(props.getNackBytes());
        maxRetryCountField.setText(props.getMaxRetries());

        startOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(startOfMessageBytesField.getText());
        endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(endOfMessageBytesField.getText());
        ackAbbreviation = TcpUtil.convertHexToAbbreviation(ackBytesField.getText());
        nackAbbreviation = TcpUtil.convertHexToAbbreviation(nackBytesField.getText());
    }

    public String checkProperties() {
        resetInvalidProperties();
        String errors = "";

        if (!validBytes(startOfMessageBytesField.getText())) {
            errors += "Invalid start of message bytes.\r\n";
            startOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (!validBytes(endOfMessageBytesField.getText())) {
            errors += "Invalid end of message bytes.\r\n";
            endOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (useMLLPv2YesRadio.isSelected()) {
            if (!validBytes(ackBytesField.getText())) {
                errors += "Invalid affirmative commit acknowledgement bytes.\r\n";
                ackBytesField.setBackground(UIConstants.INVALID_COLOR);
            }
            if (!validBytes(nackBytesField.getText())) {
                errors += "Invalid negative commit acknowledgement bytes.\r\n";
                nackBytesField.setBackground(UIConstants.INVALID_COLOR);
            }
            if (maxRetryCountField.getText().length() == 0) {
                errors += "Invalid maximum retry count.\r\n";
                maxRetryCountField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return errors;
    }

    public void resetInvalidProperties() {
        startOfMessageBytesField.setBackground(null);
        endOfMessageBytesField.setBackground(null);
        ackBytesField.setBackground(null);
        nackBytesField.setBackground(null);
        maxRetryCountField.setBackground(null);
    }

    public boolean isSaved() {
        return saved;
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        changeAbbreviation(evt);
    }

    private void changeAbbreviation(DocumentEvent evt) {
        String text = "";

        try {
            text = evt.getDocument().getText(0, evt.getDocument().getLength()).trim();
        } catch (BadLocationException e) {
        }

        if (evt.getDocument().equals(startOfMessageBytesField.getDocument())) {
            startOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(text);
            actionListener.actionPerformed(new ActionEvent(startOfMessageBytesField, ActionEvent.ACTION_PERFORMED, MLLPModeClientProvider.CHANGE_START_BYTES_COMMAND));
        } else if (evt.getDocument().equals(endOfMessageBytesField.getDocument())) {
            endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(text);
            actionListener.actionPerformed(new ActionEvent(endOfMessageBytesField, ActionEvent.ACTION_PERFORMED, MLLPModeClientProvider.CHANGE_END_BYTES_COMMAND));
        } else if (evt.getDocument().equals(ackBytesField.getDocument())) {
            ackAbbreviation = TcpUtil.convertHexToAbbreviation(text);
        } else {
            nackAbbreviation = TcpUtil.convertHexToAbbreviation(text);
        }

        changeAbbreviation();
    }

    private void changeAbbreviation() {
        startOfMessageBytesAbbrevLabel.setText(startOfMessageAbbreviation);
        endOfMessageBytesAbbrevLabel.setText(endOfMessageAbbreviation);
        ackBytesAbbrevLabel.setText(ackAbbreviation);
        nackBytesAbbrevLabel.setText(nackAbbreviation);
        pack();
    }

    private boolean validBytes(String byteString) {
        return StringUtils.isNotBlank(byteString) && TcpUtil.isValidHexString(byteString);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        useMLLPv2ButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        ackBytesLabel = new javax.swing.JLabel();
        startOfMessageBytesAbbrevLabel = new javax.swing.JLabel();
        maxRetryCountLabel = new javax.swing.JLabel();
        startOfMessageBytesLabel = new javax.swing.JLabel();
        endOfMessageBytesLabel = new javax.swing.JLabel();
        startOfMessageBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        nackBytesLabel = new javax.swing.JLabel();
        startOfMessageBytes0XLabel = new javax.swing.JLabel();
        endOfMessageBytesAbbrevLabel = new javax.swing.JLabel();
        endOfMessageBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        endOfMessageBytes0XLabel = new javax.swing.JLabel();
        ackBytesAbbrevLabel = new javax.swing.JLabel();
        ackBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        ackBytes0XLabel = new javax.swing.JLabel();
        nackBytesAbbrevLabel = new javax.swing.JLabel();
        nackBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        nackBytes0XLabel = new javax.swing.JLabel();
        maxRetryCountField = new com.mirth.connect.client.ui.components.MirthTextField();
        useMLLPv2Label = new javax.swing.JLabel();
        useMLLPv2YesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useMLLPv2NoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        byteAbbreviationList1 = new com.mirth.connect.client.ui.ByteAbbreviationList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Transmission Mode Settings");
        setModal(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("MLLP Settings"));
        jPanel2.setMinimumSize(new java.awt.Dimension(323, 0));

        ackBytesLabel.setText("Commit ACK Bytes:");
        ackBytesLabel.setToolTipText("<html>The MLLPv2 Affirmative Commit Acknowledgement bytes to expect after successfully sending a message,<br/>and to send after successfully receiving a message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        startOfMessageBytesAbbrevLabel.setText("<VT>");
        startOfMessageBytesAbbrevLabel.setToolTipText("<html>The MLLPv2 Start Block bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        maxRetryCountLabel.setText("Max Retry Count:");
        maxRetryCountLabel.setToolTipText("<html>The maximum number of times to retry unsuccessful dispatches before giving up and logging an error.</html>");

        startOfMessageBytesLabel.setText("Start of Message Bytes:");
        startOfMessageBytesLabel.setToolTipText("<html>The MLLPv2 Start Block bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        endOfMessageBytesLabel.setText("End of Message Bytes:");
        endOfMessageBytesLabel.setToolTipText("<html>The MLLPv2 End Data/Block bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        startOfMessageBytesField.setToolTipText("<html>The MLLPv2 Start Block bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");
        startOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startOfMessageBytesFieldActionPerformed(evt);
            }
        });

        nackBytesLabel.setText("Commit NACK Bytes:");
        nackBytesLabel.setToolTipText("<html>The MLLPv2 Negative Commit Acknowledgement bytes to expect after unsuccessfully sending a message,<br/>and to send after unsuccessfully receiving a message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        startOfMessageBytes0XLabel.setText("0x");
        startOfMessageBytes0XLabel.setToolTipText("<html>The MLLPv2 Start Block bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        endOfMessageBytesAbbrevLabel.setText("<FS><CR>");
        endOfMessageBytesAbbrevLabel.setToolTipText("<html>The MLLPv2 End Data/Block bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        endOfMessageBytesField.setToolTipText("<html>The MLLPv2 End Data/Block bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");
        endOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endOfMessageBytesFieldActionPerformed(evt);
            }
        });

        endOfMessageBytes0XLabel.setText("0x");
        endOfMessageBytes0XLabel.setToolTipText("<html>The MLLPv2 End Data/Block bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        ackBytesAbbrevLabel.setText("<ACK>");
        ackBytesAbbrevLabel.setToolTipText("<html>The MLLPv2 Affirmative Commit Acknowledgement bytes to expect after successfully sending a message,<br/>and to send after successfully receiving a message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        ackBytesField.setToolTipText("<html>The MLLPv2 Affirmative Commit Acknowledgement bytes to expect after successfully sending a message,<br/>and to send after successfully receiving a message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");
        ackBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ackBytesFieldActionPerformed(evt);
            }
        });

        ackBytes0XLabel.setText("0x");
        ackBytes0XLabel.setToolTipText("<html>The MLLPv2 Affirmative Commit Acknowledgement bytes to expect after successfully sending a message,<br/>and to send after successfully receiving a message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        nackBytesAbbrevLabel.setText("<NAK>");
        nackBytesAbbrevLabel.setToolTipText("<html>The MLLPv2 Negative Commit Acknowledgement bytes to expect after unsuccessfully sending a message,<br/>and to send after unsuccessfully receiving a message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        nackBytesField.setToolTipText("<html>The MLLPv2 Negative Commit Acknowledgement bytes to expect after unsuccessfully sending a message,<br/>and to send after unsuccessfully receiving a message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");
        nackBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nackBytesFieldActionPerformed(evt);
            }
        });

        nackBytes0XLabel.setText("0x");
        nackBytes0XLabel.setToolTipText("<html>The MLLPv2 Negative Commit Acknowledgement bytes to expect after unsuccessfully sending a message,<br/>and to send after unsuccessfully receiving a message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        maxRetryCountField.setToolTipText("<html>The maximum number of times to retry unsuccessful dispatches before giving up and logging an error.</html>");
        maxRetryCountField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxRetryCountFieldActionPerformed(evt);
            }
        });

        useMLLPv2Label.setText("Use MLLPv2:");

        useMLLPv2YesRadio.setBackground(new java.awt.Color(255, 255, 255));
        useMLLPv2YesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useMLLPv2ButtonGroup.add(useMLLPv2YesRadio);
        useMLLPv2YesRadio.setText("Yes");
        useMLLPv2YesRadio.setToolTipText("<html>Select Yes to use the MLLPv2 bi-directional transport layer,<br/>which includes reliable delivery assurance as per the HL7 specifications.</html>");
        useMLLPv2YesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useMLLPv2YesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useMLLPv2YesRadioActionPerformed(evt);
            }
        });

        useMLLPv2NoRadio.setBackground(new java.awt.Color(255, 255, 255));
        useMLLPv2NoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useMLLPv2ButtonGroup.add(useMLLPv2NoRadio);
        useMLLPv2NoRadio.setSelected(true);
        useMLLPv2NoRadio.setText("No");
        useMLLPv2NoRadio.setToolTipText("<html>Select Yes to use the MLLPv2 bi-directional transport layer,<br/>which includes reliable delivery assurance as per the HL7 specifications.</html>");
        useMLLPv2NoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useMLLPv2NoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useMLLPv2NoRadioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startOfMessageBytesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(endOfMessageBytesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(useMLLPv2Label, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ackBytesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(nackBytesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(maxRetryCountLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(startOfMessageBytes0XLabel)
                        .addGap(3, 3, 3)
                        .addComponent(startOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startOfMessageBytesAbbrevLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(endOfMessageBytes0XLabel)
                        .addGap(3, 3, 3)
                        .addComponent(endOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endOfMessageBytesAbbrevLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(ackBytes0XLabel)
                        .addGap(3, 3, 3)
                        .addComponent(ackBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ackBytesAbbrevLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(useMLLPv2YesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(useMLLPv2NoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(maxRetryCountField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(nackBytes0XLabel)
                                .addGap(3, 3, 3)
                                .addComponent(nackBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nackBytesAbbrevLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startOfMessageBytesLabel)
                    .addComponent(startOfMessageBytes0XLabel)
                    .addComponent(startOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startOfMessageBytesAbbrevLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endOfMessageBytesLabel)
                    .addComponent(endOfMessageBytes0XLabel)
                    .addComponent(endOfMessageBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endOfMessageBytesAbbrevLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useMLLPv2Label)
                    .addComponent(useMLLPv2YesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useMLLPv2NoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ackBytesLabel)
                    .addComponent(ackBytes0XLabel)
                    .addComponent(ackBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ackBytesAbbrevLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nackBytesLabel)
                    .addComponent(nackBytes0XLabel)
                    .addComponent(nackBytesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nackBytesAbbrevLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxRetryCountLabel)
                    .addComponent(maxRetryCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(byteAbbreviationList1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(byteAbbreviationList1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_startOfMessageBytesFieldActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        String errors = checkProperties();
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error validating MLLPv2 transmission mode settings.\r\n\r\n" + errors, "Validation Error", JOptionPane.ERROR_MESSAGE);
        } else {
            saved = true;
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void endOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_endOfMessageBytesFieldActionPerformed

    private void ackBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ackBytesFieldActionPerformed
    }//GEN-LAST:event_ackBytesFieldActionPerformed

    private void nackBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nackBytesFieldActionPerformed
    }//GEN-LAST:event_nackBytesFieldActionPerformed

    private void maxRetryCountFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxRetryCountFieldActionPerformed
    }//GEN-LAST:event_maxRetryCountFieldActionPerformed

    private void useMLLPv2YesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useMLLPv2YesRadioActionPerformed
        ackBytesLabel.setEnabled(true);
        ackBytes0XLabel.setEnabled(true);
        ackBytesField.setEnabled(true);
        ackBytesAbbrevLabel.setEnabled(true);
        
        nackBytesLabel.setEnabled(true);
        nackBytes0XLabel.setEnabled(true);
        nackBytesField.setEnabled(true);
        nackBytesAbbrevLabel.setEnabled(true);
        
        maxRetryCountLabel.setEnabled(true);
        maxRetryCountField.setEnabled(true);
    }//GEN-LAST:event_useMLLPv2YesRadioActionPerformed

    private void useMLLPv2NoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useMLLPv2NoRadioActionPerformed
        ackBytesLabel.setEnabled(false);
        ackBytes0XLabel.setEnabled(false);
        ackBytesField.setEnabled(false);
        ackBytesAbbrevLabel.setEnabled(false);
        
        nackBytesLabel.setEnabled(false);
        nackBytes0XLabel.setEnabled(false);
        nackBytesField.setEnabled(false);
        nackBytesAbbrevLabel.setEnabled(false);
        
        maxRetryCountLabel.setEnabled(false);
        maxRetryCountField.setEnabled(false);
    }//GEN-LAST:event_useMLLPv2NoRadioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ackBytes0XLabel;
    private javax.swing.JLabel ackBytesAbbrevLabel;
    private com.mirth.connect.client.ui.components.MirthTextField ackBytesField;
    private javax.swing.JLabel ackBytesLabel;
    private com.mirth.connect.client.ui.ByteAbbreviationList byteAbbreviationList1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel endOfMessageBytes0XLabel;
    private javax.swing.JLabel endOfMessageBytesAbbrevLabel;
    private com.mirth.connect.client.ui.components.MirthTextField endOfMessageBytesField;
    private javax.swing.JLabel endOfMessageBytesLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private com.mirth.connect.client.ui.components.MirthTextField maxRetryCountField;
    private javax.swing.JLabel maxRetryCountLabel;
    private javax.swing.JLabel nackBytes0XLabel;
    private javax.swing.JLabel nackBytesAbbrevLabel;
    private com.mirth.connect.client.ui.components.MirthTextField nackBytesField;
    private javax.swing.JLabel nackBytesLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel startOfMessageBytes0XLabel;
    private javax.swing.JLabel startOfMessageBytesAbbrevLabel;
    private com.mirth.connect.client.ui.components.MirthTextField startOfMessageBytesField;
    private javax.swing.JLabel startOfMessageBytesLabel;
    private javax.swing.ButtonGroup useMLLPv2ButtonGroup;
    private javax.swing.JLabel useMLLPv2Label;
    private com.mirth.connect.client.ui.components.MirthRadioButton useMLLPv2NoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton useMLLPv2YesRadio;
    // End of variables declaration//GEN-END:variables
}
