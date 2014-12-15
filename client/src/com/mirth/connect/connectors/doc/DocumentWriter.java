/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.util.ConnectionTestResponse;

public class DocumentWriter extends ConnectorSettingsPanel {

    private Frame parent;

    public DocumentWriter() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
    }
    
    private void updateFileEnabled(boolean enable) {
        fileNameField.setEnabled(enable);
        jLabel2.setEnabled(enable);
        directoryField.setEnabled(enable);
        jLabel1.setEnabled(enable);
        testConnection.setEnabled(enable);
    }

    @Override
    public String getConnectorName() {
        return new DocumentDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DocumentDispatcherProperties properties = new DocumentDispatcherProperties();

        properties.setHost(directoryField.getText().replace('\\', '/'));
        properties.setOutputPattern(fileNameField.getText());

        if (pdf.isSelected()) {
            properties.setDocumentType(DocumentDispatcherProperties.DOCUMENT_TYPE_PDF);
        } else {
            properties.setDocumentType(DocumentDispatcherProperties.DOCUMENT_TYPE_RTF);
        }

        properties.setEncrypt(passwordYes.isSelected());

        String writeToOption = "FILE";
        if (attachmentRadioButton.isSelected()) {
            writeToOption = "ATTACHMENT";
        } else if(bothRadioButton.isSelected()) {
            writeToOption = "BOTH";
        }

        properties.setOutput(writeToOption);

        properties.setPassword(new String(passwordField.getPassword()));
        properties.setTemplate(fileContentsTextPane.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DocumentDispatcherProperties props = (DocumentDispatcherProperties) properties;

        directoryField.setText(props.getHost());
        fileNameField.setText(props.getOutputPattern());

        if (props.isEncrypt()) {
            passwordYes.setSelected(true);
            passwordYesActionPerformed(null);
        } else {
            passwordNo.setSelected(true);
            passwordNoActionPerformed(null);
        }

        fileRadioButton.setSelected(true);

        String writeToOptions = props.getOutput();
        if (StringUtils.isNotBlank(writeToOptions)) {
            if (writeToOptions.equalsIgnoreCase("BOTH")) {
                bothRadioButton.setSelected(true);
            } else if (writeToOptions.equalsIgnoreCase("ATTACHMENT")) {
                attachmentRadioButton.setSelected(true);
            }

            updateFileEnabled(!writeToOptions.equalsIgnoreCase("ATTACHMENT"));
        }
        
        if (props.getDocumentType().equals(DocumentDispatcherProperties.DOCUMENT_TYPE_PDF)) {
            pdf.setSelected(true);
            pdfActionPerformed(null);
        } else {
            rtf.setSelected(true);
            rtfActionPerformed(null);
        }

        passwordField.setText(props.getPassword());

        fileContentsTextPane.setText(props.getTemplate());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new DocumentDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        DocumentDispatcherProperties props = (DocumentDispatcherProperties) properties;

        boolean valid = true;

        if (props.getHost().length() == 0) {
            valid = false;
            if (highlight) {
                directoryField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getOutputPattern().length() == 0) {
            valid = false;
            if (highlight) {
                fileNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getTemplate().length() == 0) {
            valid = false;
            if (highlight) {
                fileContentsTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.isEncrypt()) {
            if (props.getPassword().length() == 0) {
                valid = false;
                if (highlight) {
                    passwordField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        directoryField.setBackground(null);
        fileNameField.setBackground(null);
        fileContentsTextPane.setBackground(null);
        passwordField.setBackground(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        directoryField = new com.mirth.connect.client.ui.components.MirthTextField();
        fileNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        passwordYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        passwordNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        encryptedLabel = new javax.swing.JLabel();
        passwordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        passwordLabel = new javax.swing.JLabel();
        fileContentsTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(false,false);
        jLabel5 = new javax.swing.JLabel();
        pdf = new com.mirth.connect.client.ui.components.MirthRadioButton();
        rtf = new com.mirth.connect.client.ui.components.MirthRadioButton();
        testConnection = new javax.swing.JButton();
        outputLabel = new javax.swing.JLabel();
        fileRadioButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        attachmentRadioButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        bothRadioButton = new com.mirth.connect.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setText("Directory:");

        jLabel2.setText("File Name:");

        jLabel3.setText("Template:");

        directoryField.setToolTipText("The directory (folder) where the generated file should be written.");

        fileNameField.setToolTipText("The file name to give to the generated file.");

        passwordYes.setBackground(new java.awt.Color(255, 255, 255));
        passwordYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passwordYes);
        passwordYes.setText("Yes");
        passwordYes.setToolTipText("If Document Type PDF is selected, generated documents can optionally be encrypted.");
        passwordYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        passwordYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordYesActionPerformed(evt);
            }
        });

        passwordNo.setBackground(new java.awt.Color(255, 255, 255));
        passwordNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passwordNo);
        passwordNo.setText("No");
        passwordNo.setToolTipText("If Document Type PDF is selected, generated documents can optionally be encrypted.");
        passwordNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        passwordNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordNoActionPerformed(evt);
            }
        });

        encryptedLabel.setText("Encrypted:");

        passwordField.setToolTipText("If Encrypted Yes is selected, enter the password to be used to later view the document here.");

        passwordLabel.setText("Password:");

        fileContentsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setText("Document Type:");

        pdf.setBackground(new java.awt.Color(255, 255, 255));
        pdf.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(pdf);
        pdf.setText("PDF");
        pdf.setToolTipText("The type of document to be created for each message.");
        pdf.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pdfActionPerformed(evt);
            }
        });

        rtf.setBackground(new java.awt.Color(255, 255, 255));
        rtf.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(rtf);
        rtf.setText("RTF");
        rtf.setToolTipText("The type of document to be created for each message.");
        rtf.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rtf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rtfActionPerformed(evt);
            }
        });

        testConnection.setText("Test Write");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        outputLabel.setBackground(new java.awt.Color(255, 255, 255));
        outputLabel.setText("Output:");

        fileRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup3.add(fileRadioButton);
        fileRadioButton.setText("File");
        fileRadioButton.setToolTipText("Write the contents to a file.");
        fileRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileRadioButtonActionPerformed(evt);
            }
        });

        attachmentRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup3.add(attachmentRadioButton);
        attachmentRadioButton.setText("Attachment");
        attachmentRadioButton.setToolTipText("Write the contents to an attachment.");
        attachmentRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attachmentRadioButtonActionPerformed(evt);
            }
        });

        bothRadioButton.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup3.add(bothRadioButton);
        bothRadioButton.setText("Both");
        bothRadioButton.setToolTipText("Write the content to a file and an attachment.");
        bothRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bothRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel1)
                    .addComponent(encryptedLabel)
                    .addComponent(passwordLabel)
                    .addComponent(jLabel3)
                    .addComponent(outputLabel)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileContentsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(passwordYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(passwordNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pdf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rtf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testConnection))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fileRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(attachmentRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bothRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 92, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputLabel)
                    .addComponent(fileRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attachmentRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bothRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testConnection)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(fileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(pdf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rtf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encryptedLabel)
                    .addComponent(passwordYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileContentsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addComponent(jLabel3)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        final String workingId = parent.startWorking("Testing connection...");

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {

                try {
                    ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(parent.channelEditPanel.currentChannel.getId(), getConnectorName(), "testWrite", getProperties());

                    if (response == null) {
                        throw new ClientException("Failed to invoke service.");
                    } else if (response.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                        parent.alertInformation(parent, response.getMessage());
                    } else {
                        parent.alertWarning(parent, response.getMessage());
                    }

                    return null;
                } catch (ClientException e) {
                    parent.alertError(parent, e.getMessage());
                    return null;
                }
            }

            public void done() {
                parent.stopWorking(workingId);
            }
        };

        worker.execute();
    }//GEN-LAST:event_testConnectionActionPerformed

    private void fileRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileRadioButtonActionPerformed
        updateFileEnabled(true);
    }//GEN-LAST:event_fileRadioButtonActionPerformed

    private void attachmentRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachmentRadioButtonActionPerformed
        updateFileEnabled(false);
    }//GEN-LAST:event_attachmentRadioButtonActionPerformed

    private void bothRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bothRadioButtonActionPerformed
        updateFileEnabled(true);
    }//GEN-LAST:event_bothRadioButtonActionPerformed

    private void pdfActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_pdfActionPerformed
    {// GEN-HEADEREND:event_pdfActionPerformed
        if (passwordYes.isSelected()) {
            passwordYesActionPerformed(null);
        } else {
            passwordNoActionPerformed(null);
        }

        encryptedLabel.setEnabled(true);
        passwordYes.setEnabled(true);
        passwordNo.setEnabled(true);
    }// GEN-LAST:event_pdfActionPerformed

    private void rtfActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_rtfActionPerformed
    {// GEN-HEADEREND:event_rtfActionPerformed
        encryptedLabel.setEnabled(false);
        passwordYes.setEnabled(false);
        passwordNo.setEnabled(false);
        passwordNoActionPerformed(null);
    }// GEN-LAST:event_rtfActionPerformed

    private void passwordNoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_passwordNoActionPerformed
        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
    }// GEN-LAST:event_passwordNoActionPerformed

    private void passwordYesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_passwordYesActionPerformed
        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }// GEN-LAST:event_passwordYesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthRadioButton attachmentRadioButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton bothRadioButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private com.mirth.connect.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel encryptedLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea fileContentsTextPane;
    private com.mirth.connect.client.ui.components.MirthTextField fileNameField;
    private com.mirth.connect.client.ui.components.MirthRadioButton fileRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel outputLabel;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton passwordNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton passwordYes;
    private com.mirth.connect.client.ui.components.MirthRadioButton pdf;
    private com.mirth.connect.client.ui.components.MirthRadioButton rtf;
    private javax.swing.JButton testConnection;
    // End of variables declaration//GEN-END:variables
}
