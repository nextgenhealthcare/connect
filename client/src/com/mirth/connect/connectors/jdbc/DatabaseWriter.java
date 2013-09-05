/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;
import org.syntax.jedit.tokenmarker.TSQLTokenMarker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.connectors.jdbc.DatabaseMetadataDialog.STATEMENT_TYPE;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.DriverInfo;

public class DatabaseWriter extends ConnectorSettingsPanel {

    private static SyntaxDocument sqlMappingDoc;
    private static SyntaxDocument jsMappingDoc;
    private List<DriverInfo> drivers;
    private Frame parent;

    public DatabaseWriter() {
        this.parent = PlatformUI.MIRTH_FRAME;

        try {
            drivers = this.parent.mirthClient.getDatabaseDrivers();
        } catch (ClientException e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }

        initComponents();

        drivers.add(0, new DriverInfo(DatabaseDispatcherProperties.DRIVER_DEFAULT, DatabaseDispatcherProperties.DRIVER_DEFAULT, "", ""));
        String[] driverNames = new String[drivers.size()];

        for (int i = 0; i < drivers.size(); i++) {
            driverNames[i] = drivers.get(i).getName();
        }

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(driverNames));

        sqlMappingDoc = new SyntaxDocument();
        sqlMappingDoc.setTokenMarker(new TSQLTokenMarker());
        jsMappingDoc = new SyntaxDocument();
        jsMappingDoc.setTokenMarker(new JavaScriptTokenMarker());
    }

    @Override
    public String getConnectorName() {
        return new DatabaseDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        DatabaseDispatcherProperties properties = new DatabaseDispatcherProperties();

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driver = drivers.get(i);
            if (driver.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                properties.setDriver(driver.getClassName());
            }
        }

        properties.setUrl(databaseURLField.getText());
        properties.setUsername(databaseUsernameField.getText());
        properties.setPassword(new String(databasePasswordField.getPassword()));

        properties.setUseScript(useJavaScriptYes.isSelected());
        properties.setQuery(databaseSQLTextPane.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) properties;

        boolean enabled = parent.isSaveEnabled();

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driver = drivers.get(i);
            if (driver.getClassName().equalsIgnoreCase(props.getDriver())) {
                databaseDriverCombobox.setSelectedItem(driver.getName());
            }
        }

        parent.setSaveEnabled(enabled);

        databaseURLField.setText(props.getUrl());
        databaseUsernameField.setText(props.getUsername());
        databasePasswordField.setText(props.getPassword());

        if (props.isUseScript()) {
            useJavaScriptYes.setSelected(true);
            useJavaScriptYesActionPerformed(null);

        } else {
            useJavaScriptNo.setSelected(true);
            useJavaScriptNoActionPerformed(null);
        }

        databaseSQLTextPane.setText(props.getQuery());

    }

    @Override
    public ConnectorProperties getDefaults() {
        return new DatabaseDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) properties;

        boolean valid = true;

        if (!props.isUseScript() && props.getUrl().length() == 0) {
            valid = false;
            if (highlight) {
                databaseURLField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getQuery().length() == 0) {
            valid = false;
            if (highlight) {
                databaseSQLTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getDriver().equals(DatabaseDispatcherProperties.DRIVER_DEFAULT)) {
            valid = false;
            if (highlight) {
                databaseDriverCombobox.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public TransferMode getTransferMode() {
        return ((DatabaseDispatcherProperties) getProperties()).isUseScript() ? TransferMode.JAVASCRIPT : TransferMode.VELOCITY;
    }

    @Override
    public void resetInvalidProperties() {
        databaseURLField.setBackground(null);
        databaseSQLTextPane.setBackground(null);
        databaseDriverCombobox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }

    @Override
    public String doValidate(ConnectorProperties properties, boolean highlight) {
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) properties;

        String error = null;

        if (props.isUseScript()) {
            String script = props.getQuery();

            if (script.length() != 0) {
                Context context = Context.enter();
                try {
                    context.compileString("function rhinoWrapper() {" + script + "\n}", UUID.randomUUID().toString(), 1, null);
                } catch (EvaluatorException e) {
                    if (error == null) {
                        error = "";
                    }
                    error += "Error in connector \"" + getConnectorName() + "\" at Javascript:\nError on line " + e.lineNumber() + ": " + e.getMessage() + ".\n\n";
                } catch (Exception e) {
                    if (error == null) {
                        error = "";
                    }
                    error += "Error in connector \"" + getConnectorName() + "\" at Javascript:\nUnknown error occurred during validation.";
                }

                Context.exit();
            }
        }

        return error;
    }

    @Override
    public List<String> getScripts(ConnectorProperties properties) {
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) properties;

        List<String> scripts = new ArrayList<String>();

        if (props.isUseScript()) {
            scripts.add(props.getQuery());
        }

        return scripts;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        sqlLabel = new javax.swing.JLabel();
        databaseDriverCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        databaseURLField = new com.mirth.connect.client.ui.components.MirthTextField();
        databaseUsernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        databasePasswordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        databaseSQLTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,true);
        useJavaScriptYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useJavaScriptNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel6 = new javax.swing.JLabel();
        generateConnection = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        generateInsert = new javax.swing.JButton();
        insertURLTemplateButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setText("Driver:");

        jLabel2.setText("URL:");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        sqlLabel.setText("SQL:");

        databaseDriverCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sun JDBC-ODBC Bridge", "ODBC - MySQL", "ODBC - PostgreSQL", "ODBC - SQL Server/Sybase", "ODBC - Oracle 10g Release 2" }));
        databaseDriverCombobox.setToolTipText("Specifies the type of database driver to use to connect to the database.");

        databaseURLField.setToolTipText("<html>The JDBC URL to connect to the database.<br>This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databaseUsernameField.setToolTipText("<html>The username to connect to the database.<br>This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databasePasswordField.setToolTipText("<html>The password to connect to the database.<br>This is not used when \"Use JavaScript\" is checked.<br>However, it is used when the Insert Connection feature is used to generate code.</html>");

        databaseSQLTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        useJavaScriptYes.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(useJavaScriptYes);
        useJavaScriptYes.setText("Yes");
        useJavaScriptYes.setToolTipText("Implement JavaScript code using JDBC to insert a message into the database.");
        useJavaScriptYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJavaScriptYesActionPerformed(evt);
            }
        });

        useJavaScriptNo.setBackground(new java.awt.Color(255, 255, 255));
        useJavaScriptNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(useJavaScriptNo);
        useJavaScriptNo.setText("No");
        useJavaScriptNo.setToolTipText("Specify the SQL statements to insert a message into the database.");
        useJavaScriptNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaScriptNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJavaScriptNoActionPerformed(evt);
            }
        });

        jLabel6.setText("Use JavaScript:");

        generateConnection.setText("Connection");
        generateConnection.setToolTipText("<html>If \"Yes\" is selected for Use JavaScript, this button is enabled.<br>When clicked, it inserts boilerplate Connection construction code into the JavaScript control at the current caret location.</html>");
        generateConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateConnectionActionPerformed(evt);
            }
        });

        jLabel7.setText("Generate:");

        generateInsert.setText("Insert");
        generateInsert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateInsertActionPerformed(evt);
            }
        });

        insertURLTemplateButton.setText("Insert URL Template");
        insertURLTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertURLTemplateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(sqlLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(databaseURLField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(databaseUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(databaseDriverCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(insertURLTemplateButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(useJavaScriptYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(useJavaScriptNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(databasePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(generateConnection)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(generateInsert))
                            .addComponent(databaseSQLTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE))
                        .addContainerGap())))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {generateConnection, generateInsert});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(databaseDriverCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(insertURLTemplateButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(databaseURLField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(databaseUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(databasePasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(useJavaScriptYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(useJavaScriptNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(generateConnection)
                        .addComponent(jLabel7)
                        .addComponent(generateInsert)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sqlLabel)
                    .addComponent(databaseSQLTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void generateInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateInsertActionPerformed
        showDatabaseMetaData(STATEMENT_TYPE.INSERT_TYPE);
    }//GEN-LAST:event_generateInsertActionPerformed

    private void insertURLTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertURLTemplateButtonActionPerformed

        if (!databaseURLField.getText().equals("")) {
            if (!parent.alertOption(parent, "Are you sure you would like to replace your current connection URL with the template URL?")) {
                return;
            }
        }

        String template = "";

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driverInfo = drivers.get(i);
            if (driverInfo.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                template = driverInfo.getTemplate();
            }
        }

        databaseURLField.setText(template);
        databaseURLField.grabFocus();
        parent.setSaveEnabled(true);

    }//GEN-LAST:event_insertURLTemplateButtonActionPerformed

    public void showDatabaseMetaData(STATEMENT_TYPE type) {
        DatabaseDispatcherProperties properties = (DatabaseDispatcherProperties) getProperties();

        if (properties.getUrl().length() == 0 || properties.getDriver().equals(DatabaseReceiverProperties.DRIVER_DEFAULT)) {
            parent.alertError(parent, "A valid Driver and URL are required to perform this operation.");
        } else {
            String selectLimit = null;

            for (int i = 0; i < drivers.size(); i++) {
                DriverInfo driver = drivers.get(i);
                if (driver.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                    selectLimit = driver.getSelectLimit();
                }
            }

            new DatabaseMetadataDialog(this, type, new DatabaseConnectionInfo(properties.getDriver(), properties.getUrl(), properties.getUsername(), properties.getPassword(), "", selectLimit));
        }
    }

    public void setInsertText(List<String> statements) {
        if (!useJavaScriptYes.isSelected()) {
            for (String statement : statements) {
                databaseSQLTextPane.setText(statement.replaceAll("\\?", "") + "\n\n" + databaseSQLTextPane.getText());
            }
        } else {
            StringBuilder connectionString = new StringBuilder();
            for (String statement : statements) {
                connectionString.append("var result = dbConn.executeUpdate(\"");
                connectionString.append(statement.replaceAll("\\n", " "));
                connectionString.append("\");\n");
            }
            databaseSQLTextPane.setSelectedText("\n" + connectionString.toString());
        }

        parent.setSaveEnabled(true);
    }

    private String generateConnectionString() {
        String driver = "";

        for (int i = 0; i < drivers.size(); i++) {
            DriverInfo driverInfo = drivers.get(i);
            if (driverInfo.getName().equalsIgnoreCase(((String) databaseDriverCombobox.getSelectedItem()))) {
                driver = driverInfo.getClassName();
            }
        }

        StringBuilder connectionString = new StringBuilder();
        connectionString.append("var dbConn = DatabaseConnectionFactory.createDatabaseConnection('");
        connectionString.append(driver + "','" + databaseURLField.getText() + "','");
        connectionString.append(databaseUsernameField.getText() + "','" + new String(databasePasswordField.getPassword()) + "\');\n");
        connectionString.append("\ndbConn.close();");

        return connectionString.toString();
    }

    private void generateConnectionActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_generateConnectionActionPerformed
    {// GEN-HEADEREND:event_generateConnectionActionPerformed
        databaseSQLTextPane.setText(generateConnectionString() + "\n\n" + databaseSQLTextPane.getText());
        databaseSQLTextPane.requestFocus();
        databaseSQLTextPane.setCaretPosition(databaseSQLTextPane.getText().indexOf("\n\n") + 1);
        parent.setSaveEnabled(true);
    }// GEN-LAST:event_generateConnectionActionPerformed

    private void useJavaScriptYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useJavaScriptYesActionPerformed
    {// GEN-HEADEREND:event_useJavaScriptYesActionPerformed
        sqlLabel.setText("JavaScript:");
        databaseSQLTextPane.setDocument(jsMappingDoc);
        databaseSQLTextPane.setText(generateConnectionString());
        generateConnection.setEnabled(true);
        parent.channelEditPanel.destinationVariableList.setTransferMode(TransferMode.JAVASCRIPT);
    }// GEN-LAST:event_useJavaScriptYesActionPerformed

    private void useJavaScriptNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_useJavaScriptNoActionPerformed
    {// GEN-HEADEREND:event_useJavaScriptNoActionPerformed
        sqlLabel.setText("SQL:");
        databaseSQLTextPane.setDocument(sqlMappingDoc);
        databaseSQLTextPane.setText("");
        generateConnection.setEnabled(false);
        parent.channelEditPanel.destinationVariableList.setTransferMode(TransferMode.VELOCITY);
    }// GEN-LAST:event_useJavaScriptNoActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private com.mirth.connect.client.ui.components.MirthComboBox databaseDriverCombobox;
    private com.mirth.connect.client.ui.components.MirthPasswordField databasePasswordField;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea databaseSQLTextPane;
    private com.mirth.connect.client.ui.components.MirthTextField databaseURLField;
    private com.mirth.connect.client.ui.components.MirthTextField databaseUsernameField;
    private javax.swing.JButton generateConnection;
    private javax.swing.JButton generateInsert;
    private javax.swing.JButton insertURLTemplateButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel sqlLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJavaScriptNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJavaScriptYes;
    // End of variables declaration//GEN-END:variables
}
