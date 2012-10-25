/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import com.mirth.connect.client.ui.Frame;
import java.awt.Point;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ListenerSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class WebServiceListener extends ConnectorSettingsPanel {

    private final int USERNAME_COLUMN_NUMBER = 0;
    private final int PASSWORD_COLUMN_NUMBER = 1;
    private final String USERNAME_COLUMN_NAME = "Username";
    private final String PASSWORD_COLUMN_NAME = "Password";
    private Frame parent;

    public WebServiceListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        wsdlURLField.setEditable(false);
        methodField.setEditable(false);
    }

    @Override
    public String getConnectorName() {
        return new WebServiceReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        WebServiceReceiverProperties properties = new WebServiceReceiverProperties();
        properties.setClassName(classNameField.getText());
        properties.setServiceName(serviceNameField.getText());
        ArrayList<ArrayList<String>> credentials = getCredentials();
        properties.setUsernames(credentials.get(0));
        properties.setPasswords(credentials.get(1));

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        WebServiceReceiverProperties props = (WebServiceReceiverProperties) properties;

        classNameField.setText(props.getClassName());
        updateClassNameRadio();

        serviceNameField.setText(props.getServiceName());

        updateWSDL();

        List<List<String>> credentials = new ArrayList<List<String>>();

        credentials.add(props.getUsernames());
        credentials.add(props.getPasswords());
        setCredentials(credentials);
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new WebServiceReceiverProperties();
    }

    private void updateClassNameRadio() {
        if (classNameField.getText().equals(new WebServiceReceiverProperties().getClassName())) {
            classNameDefaultRadio.setSelected(true);
            classNameDefaultRadioActionPerformed(null);
        } else {
            classNameCustomRadio.setSelected(true);
            classNameCustomRadioActionPerformed(null);
        }
    }

    public void updateWSDL() {
        String server = "<server ip>";
        try {
            server = new URI(PlatformUI.SERVER_NAME).getHost();
        } catch (Exception e) {
            // ignore exceptions getting the server ip
        }

        wsdlURLField.setText("http://" + server + ":" + ((WebServiceReceiverProperties) getFilledProperties()).getListenerConnectorProperties().getPort() + "/services/" + serviceNameField.getText() + "?wsdl");
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        WebServiceReceiverProperties props = (WebServiceReceiverProperties) properties;

        boolean valid = true;

        if (props.getClassName().length() == 0) {
            valid = false;
            if (highlight) {
                classNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getServiceName().length() == 0) {
            valid = false;
            if (highlight) {
                serviceNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        classNameField.setBackground(null);
        serviceNameField.setBackground(null);
    }

    private void setCredentials(List<List<String>> credentials) {

        List<String> usernames = credentials.get(0);
        List<String> passwords = credentials.get(1);

        Object[][] tableData = new Object[usernames.size()][2];

        credentialsTable = new MirthTable();

        for (int i = 0; i < usernames.size(); i++) {
            tableData[i][USERNAME_COLUMN_NUMBER] = usernames.get(i);
            tableData[i][PASSWORD_COLUMN_NUMBER] = passwords.get(i);
        }

        credentialsTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{USERNAME_COLUMN_NAME, PASSWORD_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        credentialsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (credentialsTable.getSelectedModelIndex() != -1) {
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });

        class AttachmentsTableCellEditor extends TextFieldCellEditor {

            boolean checkUnique;

            public AttachmentsTableCellEditor(boolean checkUnique) {
                super();
                this.checkUnique = checkUnique;
            }

            public boolean checkUnique(String value) {
                boolean exists = false;

                for (int i = 0; i < credentialsTable.getModel().getRowCount(); i++) {
                    if (((String) credentialsTable.getModel().getValueAt(i, USERNAME_COLUMN_NUMBER)).equalsIgnoreCase(value)) {
                        exists = true;
                    }
                }

                return exists;
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    deleteButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                deleteButton.setEnabled(true);

                if (checkUnique && (value.length() == 0 || checkUnique(value))) {
                    return false;
                }

                parent.setSaveEnabled(true);
                return true;
            }
        }

        credentialsTable.getColumnModel().getColumn(credentialsTable.getColumnModelIndex(USERNAME_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(true));
        credentialsTable.getColumnModel().getColumn(credentialsTable.getColumnModelIndex(PASSWORD_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        credentialsTable.setCustomEditorControls(true);

        credentialsTable.setSelectionMode(0);
        credentialsTable.setRowSelectionAllowed(true);
        credentialsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        credentialsTable.setDragEnabled(false);
        credentialsTable.setOpaque(true);
        credentialsTable.setSortable(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            credentialsTable.setHighlighters(highlighter);
        }

        credentialsPane.setViewportView(credentialsTable);
        deleteButton.setEnabled(false);
    }

    private ArrayList<ArrayList<String>> getCredentials() {
        ArrayList<ArrayList<String>> credentials = new ArrayList<ArrayList<String>>();

        ArrayList<String> usernames = new ArrayList<String>();
        ArrayList<String> passwords = new ArrayList<String>();

        for (int i = 0; i < credentialsTable.getModel().getRowCount(); i++) {
            if (((String) credentialsTable.getModel().getValueAt(i, USERNAME_COLUMN_NUMBER)).length() > 0) {
                usernames.add((String) credentialsTable.getModel().getValueAt(i, USERNAME_COLUMN_NUMBER));
                passwords.add((String) credentialsTable.getModel().getValueAt(i, PASSWORD_COLUMN_NUMBER));
            }
        }

        credentials.add(usernames);
        credentials.add(passwords);

        return credentials;
    }

    private void stopCellEditing() {
        if (credentialsTable.isEditing()) {
            credentialsTable.getColumnModel().getColumn(credentialsTable.convertColumnIndexToModel(credentialsTable.getEditingColumn())).getCellEditor().stopCellEditing();
        }
    }

    /**
     * Get the name that should be used for a new user so that it is unique.
     */
    private String getNewUsername(int size) {
        String temp = "user";

        for (int i = 1; i <= size; i++) {
            boolean exists = false;

            for (int j = 0; j < size - 1; j++) {
                if (((String) credentialsTable.getModel().getValueAt(j, credentialsTable.getColumnModelIndex(USERNAME_COLUMN_NAME))).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }

            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    @Override
    public void updatedField(String field) {
        if (ListenerSettingsPanel.FIELD_PORT.equals(field)) {
            updateWSDL();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        classNameButtonGroup = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        serviceNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel2 = new javax.swing.JLabel();
        URL1 = new javax.swing.JLabel();
        methodField = new javax.swing.JTextField();
        wsdlURLField = new javax.swing.JTextField();
        webServiceLabel = new javax.swing.JLabel();
        classNameDefaultRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        classNameCustomRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        classNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        credentialsPane = new javax.swing.JScrollPane();
        credentialsTable = new com.mirth.connect.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        credentialsLabel = new javax.swing.JLabel();
        classNameLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        URL.setText("Service Name:");

        serviceNameField.setToolTipText("The name to give to the web service.");
        serviceNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                serviceNameFieldKeyReleased(evt);
            }
        });

        jLabel2.setText("Method:");

        URL1.setText("WSDL URL:");

        methodField.setText("String acceptMessage(String message)");
        methodField.setToolTipText("Displays the generated web service operation signature the client will call.");

        wsdlURLField.setToolTipText("<html>Displays the generated WSDL URL for the web service.<br>The client that sends messages to the service can download this file to determine how to call the web service.</html>");

        webServiceLabel.setText("Web Service:");

        classNameDefaultRadio.setBackground(new java.awt.Color(255, 255, 255));
        classNameDefaultRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classNameButtonGroup.add(classNameDefaultRadio);
        classNameDefaultRadio.setText("Default service");
        classNameDefaultRadio.setToolTipText("<html>If checked, the connector will use the DefaultAcceptMessage web service.</html>");
        classNameDefaultRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        classNameDefaultRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classNameDefaultRadioActionPerformed(evt);
            }
        });

        classNameCustomRadio.setBackground(new java.awt.Color(255, 255, 255));
        classNameCustomRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        classNameButtonGroup.add(classNameCustomRadio);
        classNameCustomRadio.setSelected(true);
        classNameCustomRadio.setText("Custom service");
        classNameCustomRadio.setToolTipText("<html>If checked, the connector will use a custom web service defined below.</html>");
        classNameCustomRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        classNameCustomRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classNameCustomRadioActionPerformed(evt);
            }
        });

        classNameField.setToolTipText("<html>The fully qualified class name of the web service that should be hosted.<br>If this is a custom class, it should be added in a custom jar so it is loaded with Mirth Connect.</html>");

        credentialsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Username", "Password"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        credentialsTable.setToolTipText("");
        credentialsPane.setViewportView(credentialsTable);

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        credentialsLabel.setText("Basic Authentication:");

        classNameLabel.setText("Service Class Name:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(webServiceLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(URL, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(URL1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(classNameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(credentialsLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(credentialsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteButton)))
                    .addComponent(serviceNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(wsdlURLField, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(methodField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(classNameDefaultRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(classNameCustomRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(classNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classNameDefaultRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(classNameCustomRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(webServiceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(classNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(URL)
                    .addComponent(serviceNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(URL1)
                    .addComponent(wsdlURLField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(methodField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(credentialsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .addComponent(credentialsLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void serviceNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_serviceNameFieldKeyReleased
        updateWSDL();
    }//GEN-LAST:event_serviceNameFieldKeyReleased

    private void classNameDefaultRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classNameDefaultRadioActionPerformed
        classNameField.setText(new WebServiceReceiverProperties().getClassName());
        methodField.setText("String acceptMessage(String message)");
        classNameLabel.setEnabled(false);
        classNameField.setEnabled(false);
    }//GEN-LAST:event_classNameDefaultRadioActionPerformed

    private void classNameCustomRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classNameCustomRadioActionPerformed
        methodField.setText("<Custom Web Service Methods>");
        classNameLabel.setEnabled(true);
        classNameField.setEnabled(true);
    }//GEN-LAST:event_classNameCustomRadioActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        stopCellEditing();
        ((DefaultTableModel) credentialsTable.getModel()).addRow(new Object[]{getNewUsername(credentialsTable.getModel().getRowCount() + 1), ""});
        int newViewIndex = credentialsTable.convertRowIndexToView(credentialsTable.getModel().getRowCount() - 1);
        credentialsTable.setRowSelectionInterval(newViewIndex, newViewIndex);

        credentialsPane.getViewport().setViewPosition(new Point(0, credentialsTable.getRowHeight() * credentialsTable.getModel().getRowCount()));
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        stopCellEditing();

        int selectedModelIndex = credentialsTable.getSelectedModelIndex();
        int newViewIndex = credentialsTable.convertRowIndexToView(selectedModelIndex);
        if (newViewIndex == (credentialsTable.getModel().getRowCount() - 1)) {
            newViewIndex--;
        }

        ((DefaultTableModel) credentialsTable.getModel()).removeRow(selectedModelIndex);

        parent.setSaveEnabled(true);

        if (credentialsTable.getModel().getRowCount() == 0) {
            credentialsTable.clearSelection();
            deleteButton.setEnabled(false);
        } else {
            credentialsTable.setRowSelectionInterval(newViewIndex, newViewIndex);
        }

        parent.setSaveEnabled(true);
    }//GEN-LAST:event_deleteButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private javax.swing.JLabel URL1;
    private javax.swing.ButtonGroup classNameButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton classNameCustomRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton classNameDefaultRadio;
    private com.mirth.connect.client.ui.components.MirthTextField classNameField;
    private javax.swing.JLabel classNameLabel;
    private javax.swing.JLabel credentialsLabel;
    private javax.swing.JScrollPane credentialsPane;
    private com.mirth.connect.client.ui.components.MirthTable credentialsTable;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField methodField;
    private javax.swing.JButton newButton;
    private com.mirth.connect.client.ui.components.MirthTextField serviceNameField;
    private javax.swing.JLabel webServiceLabel;
    private javax.swing.JTextField wsdlURLField;
    // End of variables declaration//GEN-END:variables
}
