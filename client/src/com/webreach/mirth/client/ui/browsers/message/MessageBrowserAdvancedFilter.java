/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui.browsers.message;

import java.awt.*;

import com.webreach.mirth.model.MessageObject;

public class MessageBrowserAdvancedFilter extends javax.swing.JDialog {

    private String connector = "";
    private String messageSource = "";
    private String messageType = "";
    private String containingKeyword = "";
    private boolean includeRawMessage = false;
    private boolean includeTransformedMessage = false;
    private boolean includeEncodedMessage = false;
    private String protocol = "ALL";

    /** Creates new form MessageBrowserAdvancedFilter */
    public MessageBrowserAdvancedFilter(com.webreach.mirth.client.ui.Frame parent, String title, boolean modal) {
        super(parent, title, modal);

        initComponents();
        getContentPane().setBackground(new java.awt.Color(255, 255, 255));

        pack();
        Dimension dlgSize = getPreferredSize();
        Dimension frmSize = parent.getSize();
        Point loc = parent.getLocation();

        if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
            setLocationRelativeTo(null);
        } else {
            setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        }

        setResizable(false);

        String[] protocolValues = new String[MessageObject.Protocol.values().length + 1];
        protocolValues[0] = "ALL";
        for (int i = 1; i < protocolValues.length; i++) {
            protocolValues[i] = MessageObject.Protocol.values()[i - 1].toString();
        }

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(protocolValues));

        reset();
    }

    public void reset() {
        connector = "";
        messageSource = "";
        messageType = "";
        containingKeyword = "";
        includeRawMessage = false;
        includeTransformedMessage = false;
        includeEncodedMessage = false;
        protocol = "ALL";

        connectorField.setText(connector);
        messageSourceField.setText(messageSource);
        messageTypeField.setText(messageType);
        containing.setText(containingKeyword);
        rawMessageCheckBox.setSelected(includeRawMessage);
        transformedMessageCheckBox.setSelected(includeTransformedMessage);
        encodedMessageCheckBox.setSelected(includeEncodedMessage);
        protocolComboBox.setSelectedIndex(0);
    }

    public void setFieldValues(String connector, String messageSource, String messageType, String containingKeyword,
            boolean includeRawMessage, boolean includeTransformedMessage, boolean includeEncodedMessage,
            String protocol) {

        this.connector = connector;
        this.messageSource = messageSource;
        this.messageType = messageType;
        this.containingKeyword = containingKeyword;
        this.includeRawMessage = includeRawMessage;
        this.includeTransformedMessage = includeTransformedMessage;
        this.includeEncodedMessage = includeEncodedMessage;
        this.protocol = protocol;

        connectorField.setText(this.connector);
        messageSourceField.setText(this.messageSource);
        messageTypeField.setText(this.messageType);
        containing.setText(this.containingKeyword);
        rawMessageCheckBox.setSelected(this.includeRawMessage);
        transformedMessageCheckBox.setSelected(this.includeTransformedMessage);
        encodedMessageCheckBox.setSelected(this.includeEncodedMessage);

        if (this.protocol.equals("ALL")) {
            protocolComboBox.setSelectedIndex(0);
        } else if (this.protocol.equals("HL7V2")) {
            protocolComboBox.setSelectedIndex(1);
        } else if (this.protocol.equals("X12")) {
            protocolComboBox.setSelectedIndex(2);
        } else if (this.protocol.equals("XML")) {
            protocolComboBox.setSelectedIndex(3);
        } else if (this.protocol.equals("HL7V3")) {
            protocolComboBox.setSelectedIndex(4);
        } else if (this.protocol.equals("EDI")) {
            protocolComboBox.setSelectedIndex(5);
        } else if (this.protocol.equals("NCPDP")) {
            protocolComboBox.setSelectedIndex(6);
        } else if (this.protocol.equals("DICOM")) {
            protocolComboBox.setSelectedIndex(7);
        }

    }

    public String getConnector() {
        return connector;
    }

    public String getMessageSource() {
        return messageSource;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getContainingKeyword() {
        return containingKeyword;
    }

    public boolean isIncludeRawMessage() {
        return includeRawMessage;
    }

    public boolean isIncludeTransformedMessage() {
        return includeTransformedMessage;
    }

    public boolean isIncludeEncodedMessage() {
        return includeEncodedMessage;
    }

    public String getProtocol() {
        return protocol;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        connectorField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        messageTypeField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel7 = new javax.swing.JLabel();
        messageSourceField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel10 = new javax.swing.JLabel();
        containing = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel9 = new javax.swing.JLabel();
        rawMessageCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        transformedMessageCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        encodedMessageCheckBox = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        jLabel8 = new javax.swing.JLabel();
        protocolComboBox = new javax.swing.JComboBox();
        advSearchOKButton = new javax.swing.JButton();
        advSearchCancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Connector:");

        jLabel4.setText("Message Type:");

        jLabel7.setText("Source:");

        jLabel10.setText("Containing:");

        jLabel9.setText(" in ");

        rawMessageCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        rawMessageCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rawMessageCheckBox.setText("Raw");
        rawMessageCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        transformedMessageCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        transformedMessageCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        transformedMessageCheckBox.setText("Transformed");
        transformedMessageCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        encodedMessageCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        encodedMessageCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encodedMessageCheckBox.setText("Encoded");
        encodedMessageCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel8.setText("Protocol:");

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        advSearchOKButton.setText("OK");
        advSearchOKButton.setMaximumSize(new java.awt.Dimension(65, 23));
        advSearchOKButton.setMinimumSize(new java.awt.Dimension(65, 23));
        advSearchOKButton.setPreferredSize(new java.awt.Dimension(65, 23));
        advSearchOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advSearchOKButtonActionPerformed(evt);
            }
        });

        advSearchCancelButton.setText("Cancel");
        advSearchCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advSearchCancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(connectorField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(messageSourceField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(protocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(advSearchOKButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(containing, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rawMessageCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(transformedMessageCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(encodedMessageCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(advSearchCancelButton))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {advSearchCancelButton, advSearchOKButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(connectorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(protocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(messageSourceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(containing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9)
                    .addComponent(rawMessageCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transformedMessageCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(encodedMessageCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(advSearchOKButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(advSearchCancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {advSearchCancelButton, advSearchOKButton});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void advSearchOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advSearchOKButtonActionPerformed

        // "OK" button clicked.  save settings, and exit.        
        connector = connectorField.getText();
        messageSource = messageSourceField.getText();
        messageType = messageTypeField.getText();
        containingKeyword = containing.getText();
        includeRawMessage = rawMessageCheckBox.isSelected();
        includeTransformedMessage = transformedMessageCheckBox.isSelected();
        includeEncodedMessage = encodedMessageCheckBox.isSelected();
        protocol = (String) protocolComboBox.getSelectedItem();

        setVisible(false);

    }//GEN-LAST:event_advSearchOKButtonActionPerformed

    private void advSearchCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advSearchCancelButtonActionPerformed

        // "Cancel" button clicked.  Just exit.
        setVisible(false);

    }//GEN-LAST:event_advSearchCancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton advSearchCancelButton;
    private javax.swing.JButton advSearchOKButton;
    private com.webreach.mirth.client.ui.components.MirthTextField connectorField;
    private com.webreach.mirth.client.ui.components.MirthTextField containing;
    private com.webreach.mirth.client.ui.components.MirthCheckBox encodedMessageCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.webreach.mirth.client.ui.components.MirthTextField messageSourceField;
    private com.webreach.mirth.client.ui.components.MirthTextField messageTypeField;
    private javax.swing.JComboBox protocolComboBox;
    private com.webreach.mirth.client.ui.components.MirthCheckBox rawMessageCheckBox;
    private com.webreach.mirth.client.ui.components.MirthCheckBox transformedMessageCheckBox;
    // End of variables declaration//GEN-END:variables
}
