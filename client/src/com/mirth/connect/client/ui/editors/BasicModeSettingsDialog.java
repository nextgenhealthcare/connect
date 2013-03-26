/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

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
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.plugins.BasicModePlugin;
import com.mirth.connect.util.TcpUtil;

public class BasicModeSettingsDialog extends JDialog implements DocumentListener {

    private boolean saved;
    private String startOfMessageAbbreviation;
    private String endOfMessageAbbreviation;
    private ActionListener actionListener;

    /**
     * Creates new form BasicModeSettingsDialog
     */
    public BasicModeSettingsDialog(ActionListener actionListener) {
        super(PlatformUI.MIRTH_FRAME);
        initComponents();
        this.actionListener = actionListener;

        startOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, false, false));
        endOfMessageBytesField.setDocument(new MirthFieldConstraints(0, true, false, false));

        startOfMessageBytesField.getDocument().addDocumentListener(this);
        endOfMessageBytesField.getDocument().addDocumentListener(this);

        startOfMessageAbbreviation = "";
        endOfMessageAbbreviation = "";

        changeAbbreviation();
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
        FrameModeProperties props = new FrameModeProperties("Basic");

        props.setStartOfMessageBytes(startOfMessageBytesField.getText());
        props.setEndOfMessageBytes(endOfMessageBytesField.getText());

        return props;
    }

    public void setProperties(TransmissionModeProperties properties) {
        FrameModeProperties props = (FrameModeProperties) properties;

        startOfMessageBytesField.setText(props.getStartOfMessageBytes());
        endOfMessageBytesField.setText(props.getEndOfMessageBytes());

        startOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(startOfMessageBytesField.getText());
        endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(endOfMessageBytesField.getText());
    }

    public String checkProperties() {
        resetInvalidProperties();
        String errors = "";

        if (!TcpUtil.isValidHexString(startOfMessageBytesField.getText())) {
            errors += "Invalid start of message bytes.\r\n";
            startOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (!TcpUtil.isValidHexString(endOfMessageBytesField.getText())) {
            errors += "Invalid end of message bytes.\r\n";
            endOfMessageBytesField.setBackground(UIConstants.INVALID_COLOR);
        }

        return errors;
    }

    public void resetInvalidProperties() {
        startOfMessageBytesField.setBackground(null);
        endOfMessageBytesField.setBackground(null);
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
            actionListener.actionPerformed(new ActionEvent(startOfMessageBytesField, ActionEvent.ACTION_PERFORMED, BasicModePlugin.CHANGE_START_BYTES_COMMAND));
        } else if (evt.getDocument().equals(endOfMessageBytesField.getDocument())) {
            endOfMessageAbbreviation = TcpUtil.convertHexToAbbreviation(text);
            actionListener.actionPerformed(new ActionEvent(endOfMessageBytesField, ActionEvent.ACTION_PERFORMED, BasicModePlugin.CHANGE_END_BYTES_COMMAND));
        }

        changeAbbreviation();
    }

    private void changeAbbreviation() {
        startOfMessageBytesAbbrevLabel.setText(startOfMessageAbbreviation);
        endOfMessageBytesAbbrevLabel.setText(endOfMessageAbbreviation);
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
        startOfMessageBytesAbbrevLabel = new javax.swing.JLabel();
        startOfMessageBytesLabel = new javax.swing.JLabel();
        endOfMessageBytesLabel = new javax.swing.JLabel();
        startOfMessageBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        startOfMessageBytes0XLabel = new javax.swing.JLabel();
        endOfMessageBytesAbbrevLabel = new javax.swing.JLabel();
        endOfMessageBytesField = new com.mirth.connect.client.ui.components.MirthTextField();
        endOfMessageBytes0XLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        byteAbbreviationList1 = new com.mirth.connect.client.ui.ByteAbbreviationList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Transmission Mode Settings");
        setModal(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Basic Settings"));
        jPanel2.setMinimumSize(new java.awt.Dimension(323, 0));

        startOfMessageBytesAbbrevLabel.setText("<VT>");
        startOfMessageBytesAbbrevLabel.setToolTipText("<html>The bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        startOfMessageBytesLabel.setText("Start of Message Bytes:");
        startOfMessageBytesLabel.setToolTipText("<html>The bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        endOfMessageBytesLabel.setText("End of Message Bytes:");
        endOfMessageBytesLabel.setToolTipText("<html>The bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        startOfMessageBytesField.setToolTipText("<html>The bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");
        startOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startOfMessageBytesFieldActionPerformed(evt);
            }
        });

        startOfMessageBytes0XLabel.setText("0x");
        startOfMessageBytes0XLabel.setToolTipText("<html>The bytes before the beginning of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        endOfMessageBytesAbbrevLabel.setText("<FS><CR>");
        endOfMessageBytesAbbrevLabel.setToolTipText("<html>The bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        endOfMessageBytesField.setToolTipText("<html>The bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");
        endOfMessageBytesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endOfMessageBytesFieldActionPerformed(evt);
            }
        });

        endOfMessageBytes0XLabel.setText("0x");
        endOfMessageBytes0XLabel.setToolTipText("<html>The bytes after the end of the actual message.<br/>Only valid hexidecimal characters (0-9, A-F) are allowed.</html>");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startOfMessageBytesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(endOfMessageBytesLabel, javax.swing.GroupLayout.Alignment.TRAILING))
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
                        .addComponent(endOfMessageBytesAbbrevLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
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
                .addContainerGap(117, Short.MAX_VALUE))
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
            JOptionPane.showMessageDialog(this, "Error validating transmission mode settings.\r\n\r\n" + errors, "Validation Error", JOptionPane.ERROR_MESSAGE);
        } else {
            saved = true;
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void endOfMessageBytesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endOfMessageBytesFieldActionPerformed
    }//GEN-LAST:event_endOfMessageBytesFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.ByteAbbreviationList byteAbbreviationList1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel endOfMessageBytes0XLabel;
    private javax.swing.JLabel endOfMessageBytesAbbrevLabel;
    private com.mirth.connect.client.ui.components.MirthTextField endOfMessageBytesField;
    private javax.swing.JLabel endOfMessageBytesLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel startOfMessageBytes0XLabel;
    private javax.swing.JLabel startOfMessageBytesAbbrevLabel;
    private com.mirth.connect.client.ui.components.MirthTextField startOfMessageBytesField;
    private javax.swing.JLabel startOfMessageBytesLabel;
    private javax.swing.ButtonGroup useMLLPv2ButtonGroup;
    // End of variables declaration//GEN-END:variables
}
