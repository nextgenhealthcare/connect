/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mirth.connect.connectors.jms;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class JmsSender extends ConnectorSettingsPanel {
    public JmsSender() {
        initComponents();
        connectionPropertiesTable.setNewButton(newButton);
        connectionPropertiesTable.setDeleteButton(deleteButton);
    }

    @Override
    public String getConnectorName() {
        return new JmsDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        JmsDispatcherProperties properties = new JmsDispatcherProperties();

        properties.setUseJndi(useJndiYes.isSelected());
        properties.setJndiProviderUrl(providerUrlField.getText());
        properties.setJndiInitialContextFactory(initialContextFactoryField.getText());
        properties.setJndiConnectionFactoryName(connectionFactoryNameField.getText());
        properties.setConnectionFactoryClass(connectionFactoryClassField.getText());
        properties.setUsername(usernameField.getText());
        properties.setPassword(passwordField.getText());
        properties.setDestinationName(destinationNameField.getText());
        properties.setTopic(destinationTypeTopic.isSelected());
        properties.setReconnectIntervalMillis(reconnectIntervalField.getText());
        properties.setConnectionProperties(connectionPropertiesTable.getProperties());
        properties.setTemplate(templateTextArea.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) properties;

        if (jmsDispatcherProperties.isUseJndi()) {
            useJndiYes.setSelected(true);
            useJndiNo.setSelected(false);
            useJndiYesActionPerformed(null);
        } else {
            useJndiYes.setSelected(false);
            useJndiNo.setSelected(true);
            useJndiNoActionPerformed(null);
        }

        providerUrlField.setText(jmsDispatcherProperties.getJndiProviderUrl());
        initialContextFactoryField.setText(jmsDispatcherProperties.getJndiInitialContextFactory());
        connectionFactoryNameField.setText(jmsDispatcherProperties.getJndiConnectionFactoryName());
        connectionFactoryClassField.setText(jmsDispatcherProperties.getConnectionFactoryClass());
        usernameField.setText(jmsDispatcherProperties.getUsername());
        passwordField.setText(jmsDispatcherProperties.getPassword());
        destinationNameField.setText(jmsDispatcherProperties.getDestinationName());

        if (jmsDispatcherProperties.isTopic()) {
            destinationTypeQueue.setSelected(false);
            destinationTypeTopic.setSelected(true);
        } else {
            destinationTypeQueue.setSelected(true);
            destinationTypeTopic.setSelected(false);
        }

        reconnectIntervalField.setText(jmsDispatcherProperties.getReconnectIntervalMillis());
        connectionPropertiesTable.setProperties(jmsDispatcherProperties.getConnectionProperties());
        templateTextArea.setText(jmsDispatcherProperties.getTemplate());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new JmsDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) properties;
        boolean valid = true;

        if (jmsDispatcherProperties.isUseJndi()) {
            if (jmsDispatcherProperties.getJndiProviderUrl().length() == 0) {
                valid = false;

                if (highlight) {
                    providerUrlField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (jmsDispatcherProperties.getJndiInitialContextFactory().length() == 0) {
                valid = false;

                if (highlight) {
                    initialContextFactoryField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (jmsDispatcherProperties.getJndiConnectionFactoryName().length() == 0) {
                valid = false;

                if (highlight) {
                    connectionFactoryNameField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        } else if (jmsDispatcherProperties.getConnectionFactoryClass() == null) {
            valid = false;

            if (highlight) {
                connectionFactoryClassField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (jmsDispatcherProperties.getDestinationName().length() == 0) {
            valid = false;

            if (highlight) {
                destinationNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (jmsDispatcherProperties.getTemplate().length() == 0) {
            valid = false;

            if (highlight) {
                templateTextArea.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        providerUrlField.setBackground(null);
        initialContextFactoryField.setBackground(null);
        connectionFactoryNameField.setBackground(null);
        connectionFactoryClassField.setBackground(null);
        destinationNameField.setBackground(null);
        templateTextArea.setBackground(null);
    }

    // @formatter:off
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        destinationTypeButtonGroup = new javax.swing.ButtonGroup();
        jndiButtonGroup = new javax.swing.ButtonGroup();
        useJndiLabel = new javax.swing.JLabel();
        initialContextFactoryField = new com.mirth.connect.client.ui.components.MirthTextField();
        connectionFactoryNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        providerUrlField = new com.mirth.connect.client.ui.components.MirthTextField();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        passwordField = new com.mirth.connect.client.ui.components.MirthTextField();
        initialContextFactoryLabel = new javax.swing.JLabel();
        providerUrlLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        connectionFactoryClassLabel = new javax.swing.JLabel();
        connectionFactoryClassField = new com.mirth.connect.client.ui.components.MirthTextField();
        connectionFactoryNameLabel = new javax.swing.JLabel();
        destinationTypeLabel = new javax.swing.JLabel();
        destinationNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        destinationNameLabel = new javax.swing.JLabel();
        connectionPropertiesLabel = new javax.swing.JLabel();
        connectorPropertiesScrollPane = new javax.swing.JScrollPane();
        connectionPropertiesTable = new com.mirth.connect.client.ui.components.MirthPropertiesTable();
        newButton = new com.mirth.connect.client.ui.components.MirthButton();
        deleteButton = new com.mirth.connect.client.ui.components.MirthButton();
        templateLabel = new javax.swing.JLabel();
        templateTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        reconnectIntervalLabel = new javax.swing.JLabel();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        useJndiYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useJndiNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        destinationTypeTopic = new com.mirth.connect.client.ui.components.MirthRadioButton();
        destinationTypeQueue = new com.mirth.connect.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));

        useJndiLabel.setText("Use JNDI:");

        initialContextFactoryField.setToolTipText("If using JNDI, enter the full Java classname of the JNDI Initial Context Factory class here.");

        connectionFactoryNameField.setToolTipText("If using JNDI, enter the JNDI name of the connection factory here.");

        providerUrlField.setToolTipText("If using JNDI, enter the URL of the JNDI provider here.");

        usernameField.setToolTipText("The username for accessing the queue or topic.");

        passwordField.setToolTipText("The password for accessing the queue or topic.");

        initialContextFactoryLabel.setText("Initial Context Factory:");

        providerUrlLabel.setText("Provider URL:");

        passwordLabel.setText("Password:");

        usernameLabel.setText("Username:");

        connectionFactoryClassLabel.setText("Connection Factory Class:");

        connectionFactoryClassField.setToolTipText("If not using JNDI, enter the full Java classname of the JMS connection factory here.");

        connectionFactoryNameLabel.setText("Connection Factory Name:");

        destinationTypeLabel.setText("Destination Type:");

        destinationNameField.setToolTipText("The name of the queue or topic.");

        destinationNameLabel.setText("Destination Name:");

        connectionPropertiesLabel.setText("Connection Factory Properties:");

        connectorPropertiesScrollPane.setViewportView(connectionPropertiesTable);

        newButton.setText("New");
        newButton.setToolTipText("<html>Adds a new row to end of the list.<br>Double click the Property and Value cells to enter their values.</html>");

        deleteButton.setText("Delete");
        deleteButton.setToolTipText("Deletes the currently selected row from the list.");

        templateLabel.setText("Template:");

        templateTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        templateTextArea.setToolTipText("The JMS message body.");

        reconnectIntervalLabel.setText("Reconnect Interval (milliseconds):");

        reconnectIntervalField.setToolTipText("The number of milliseconds between reconnect attempts when a connection error occurs.");

        useJndiYes.setBackground(new java.awt.Color(255, 255, 255));
        jndiButtonGroup.add(useJndiYes);
        useJndiYes.setText("Yes");
        useJndiYes.setToolTipText("<html>Select Yes to use JNDI to look up a connection factory to connect to the queue or topic.<br/>Select No to specify a connection factory class without using JNDI.</html>");
        useJndiYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJndiYesActionPerformed(evt);
            }
        });

        useJndiNo.setBackground(new java.awt.Color(255, 255, 255));
        jndiButtonGroup.add(useJndiNo);
        useJndiNo.setText("No");
        useJndiNo.setToolTipText("<html>Select Yes to use JNDI to look up a connection factory to connect to the queue or topic.<br/>Select No to specify a connection factory class without using JNDI.</html>");
        useJndiNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJndiNoActionPerformed(evt);
            }
        });

        destinationTypeTopic.setBackground(new java.awt.Color(255, 255, 255));
        destinationTypeButtonGroup.add(destinationTypeTopic);
        destinationTypeTopic.setText("Topic");
        destinationTypeTopic.setToolTipText("If not using JNDI, specify whether the destination is a queue or a topic.");

        destinationTypeQueue.setBackground(new java.awt.Color(255, 255, 255));
        destinationTypeButtonGroup.add(destinationTypeQueue);
        destinationTypeQueue.setText("Queue");
        destinationTypeQueue.setToolTipText("If not using JNDI, specify whether the destination is a queue or a topic.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(templateLabel)
                    .addComponent(passwordLabel)
                    .addComponent(usernameLabel)
                    .addComponent(connectionFactoryClassLabel)
                    .addComponent(initialContextFactoryLabel)
                    .addComponent(providerUrlLabel)
                    .addComponent(connectionFactoryNameLabel)
                    .addComponent(useJndiLabel)
                    .addComponent(destinationNameLabel)
                    .addComponent(destinationTypeLabel)
                    .addComponent(connectionPropertiesLabel)
                    .addComponent(reconnectIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(templateTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(connectorPropertiesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(providerUrlField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(initialContextFactoryField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(connectionFactoryNameField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(connectionFactoryClassField, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                                .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                                .addComponent(usernameField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(useJndiYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(useJndiNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(destinationTypeQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(destinationTypeTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(destinationNameField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useJndiLabel)
                    .addComponent(useJndiYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useJndiNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(providerUrlLabel)
                    .addComponent(providerUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initialContextFactoryLabel)
                    .addComponent(initialContextFactoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectionFactoryNameLabel)
                    .addComponent(connectionFactoryNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectionFactoryClassLabel)
                    .addComponent(connectionFactoryClassField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationNameLabel)
                    .addComponent(destinationNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(destinationTypeQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(destinationTypeTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(destinationTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reconnectIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connectorPropertiesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectionPropertiesLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(templateTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(templateLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void useJndiNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useJndiNoActionPerformed
        providerUrlLabel.setEnabled(false);
        providerUrlField.setEnabled(false);
        initialContextFactoryLabel.setEnabled(false);
        initialContextFactoryField.setEnabled(false);
        connectionFactoryNameLabel.setEnabled(false);
        connectionFactoryNameField.setEnabled(false);
        connectionFactoryClassLabel.setEnabled(true);
        connectionFactoryClassField.setEnabled(true);
        destinationTypeLabel.setEnabled(true);
        destinationTypeQueue.setEnabled(true);
        destinationTypeTopic.setEnabled(true);
    }//GEN-LAST:event_useJndiNoActionPerformed

    private void useJndiYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useJndiYesActionPerformed
        providerUrlLabel.setEnabled(true);
        providerUrlField.setEnabled(true);
        initialContextFactoryLabel.setEnabled(true);
        initialContextFactoryField.setEnabled(true);
        connectionFactoryNameLabel.setEnabled(true);
        connectionFactoryNameField.setEnabled(true);
        connectionFactoryClassLabel.setEnabled(false);
        connectionFactoryClassField.setEnabled(false);
        destinationTypeLabel.setEnabled(false);
        destinationTypeQueue.setEnabled(false);
        destinationTypeTopic.setEnabled(false);
    }//GEN-LAST:event_useJndiYesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTextField connectionFactoryClassField;
    private javax.swing.JLabel connectionFactoryClassLabel;
    private com.mirth.connect.client.ui.components.MirthTextField connectionFactoryNameField;
    private javax.swing.JLabel connectionFactoryNameLabel;
    private javax.swing.JLabel connectionPropertiesLabel;
    private com.mirth.connect.client.ui.components.MirthPropertiesTable connectionPropertiesTable;
    private javax.swing.JScrollPane connectorPropertiesScrollPane;
    private com.mirth.connect.client.ui.components.MirthButton deleteButton;
    private com.mirth.connect.client.ui.components.MirthTextField destinationNameField;
    private javax.swing.JLabel destinationNameLabel;
    private javax.swing.ButtonGroup destinationTypeButtonGroup;
    private javax.swing.JLabel destinationTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton destinationTypeQueue;
    private com.mirth.connect.client.ui.components.MirthRadioButton destinationTypeTopic;
    private com.mirth.connect.client.ui.components.MirthTextField initialContextFactoryField;
    private javax.swing.JLabel initialContextFactoryLabel;
    private javax.swing.ButtonGroup jndiButtonGroup;
    private com.mirth.connect.client.ui.components.MirthButton newButton;
    private com.mirth.connect.client.ui.components.MirthTextField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField providerUrlField;
    private javax.swing.JLabel providerUrlLabel;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private javax.swing.JLabel templateLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea templateTextArea;
    private javax.swing.JLabel useJndiLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJndiNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJndiYes;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
    // @formatter:on
}
