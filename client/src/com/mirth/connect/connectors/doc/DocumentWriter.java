/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import java.util.Properties;

import javax.swing.SwingWorker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class DocumentWriter extends ConnectorClass {

    /**
     * Creates new form DocumentWriter
     */
    public DocumentWriter() {
        name = DocumentWriterProperties.name;
        initComponents();
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(DocumentWriterProperties.DATATYPE, name);
        properties.put(DocumentWriterProperties.FILE_DIRECTORY, directoryField.getText().replace('\\', '/'));
        properties.put(DocumentWriterProperties.FILE_NAME, fileNameField.getText());

        if (pdf.isSelected()) {
            properties.put(DocumentWriterProperties.DOCUMENT_TYPE, "pdf");
        } else {
            properties.put(DocumentWriterProperties.DOCUMENT_TYPE, "rtf");
        }

        if (passwordYes.isSelected()) {
            properties.put(DocumentWriterProperties.DOCUMENT_PASSWORD_PROTECTED, UIConstants.YES_OPTION);
        } else {
            properties.put(DocumentWriterProperties.DOCUMENT_PASSWORD_PROTECTED, UIConstants.NO_OPTION);
        }

        properties.put(DocumentWriterProperties.DOCUMENT_PASSWORD, new String(passwordField.getPassword()));
        properties.put(DocumentWriterProperties.FILE_CONTENTS, fileContentsTextPane.getText());

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        directoryField.setText((String) props.get(DocumentWriterProperties.FILE_DIRECTORY));
        fileNameField.setText((String) props.get(DocumentWriterProperties.FILE_NAME));

        if (((String) props.get(DocumentWriterProperties.DOCUMENT_PASSWORD_PROTECTED)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            passwordYes.setSelected(true);
            passwordYesActionPerformed(null);
        } else {
            passwordNo.setSelected(true);
            passwordNoActionPerformed(null);
        }

        if (((String) props.get(DocumentWriterProperties.DOCUMENT_TYPE)).equals("pdf")) {
            pdf.setSelected(true);
        } else {
            rtf.setSelected(true);
        }

        passwordField.setText((String) props.get(DocumentWriterProperties.DOCUMENT_PASSWORD));

        fileContentsTextPane.setText((String) props.get(DocumentWriterProperties.FILE_CONTENTS));
    }

    public Properties getDefaults() {
        return new DocumentWriterProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(DocumentWriterProperties.FILE_DIRECTORY)).length() == 0) {
            valid = false;
            if (highlight) {
                directoryField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(DocumentWriterProperties.FILE_NAME)).length() == 0) {
            valid = false;
            if (highlight) {
                fileNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(DocumentWriterProperties.FILE_CONTENTS)).length() == 0) {
            valid = false;
            if (highlight) {
                fileContentsTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(DocumentWriterProperties.DOCUMENT_PASSWORD_PROTECTED)).equals(UIConstants.YES_OPTION)) {
            if (((String) props.get(DocumentWriterProperties.DOCUMENT_PASSWORD)).length() == 0) {
                valid = false;
                if (highlight) {
                    passwordField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    private void resetInvalidProperties() {
        directoryField.setBackground(null);
        fileNameField.setBackground(null);
        fileContentsTextPane.setBackground(null);
        passwordField.setBackground(null);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(encryptedLabel)
                    .addComponent(passwordLabel)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testConnection))
                    .addComponent(fileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(passwordYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileContentsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pdf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rtf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testConnection))
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
                    .addComponent(passwordYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(encryptedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(fileContentsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        parent.setWorking("Testing connection...", true);

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {

                try {
                    ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(name, "testWrite", getProperties());

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
                parent.setWorking("", false);
            }
        };

        worker.execute();
}//GEN-LAST:event_testConnectionActionPerformed

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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private com.mirth.connect.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel encryptedLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea fileContentsTextPane;
    private com.mirth.connect.client.ui.components.MirthTextField fileNameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton passwordNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton passwordYes;
    private com.mirth.connect.client.ui.components.MirthRadioButton pdf;
    private com.mirth.connect.client.ui.components.MirthRadioButton rtf;
    private javax.swing.JButton testConnection;
    // End of variables declaration//GEN-END:variables
}
