/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mirth.connect.connectors.jms;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class JmsListener extends ConnectorSettingsPanel {
    public JmsListener() {
        initComponents();
        connectionPropertiesTable.setNewButton(newButton);
        connectionPropertiesTable.setDeleteButton(deleteButton);
//        initJmsProviderComboBox();
    }

//    private void initJmsProviderComboBox() {
//        try {
//            jmsDrivers = parent.mirthClient.getJmsDrivers();
//        } catch (ClientException e) {
//            parent.alertException(this, e.getStackTrace(), e.getMessage());
//        }
//
//        DefaultComboBoxModel model = new DefaultComboBoxModel();
//
//        for (JmsDriverInfo driver : jmsDrivers) {
//            model.addElement(driver);
//        }
//
//        jmsProviderComboBox.setModel(model);
//    }

    @Override
    public String getConnectorName() {
        return new JmsReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        JmsReceiverProperties properties = new JmsReceiverProperties();

        properties.setUseJndi(useJndiYes.isSelected());
        properties.setJndiProviderUrl(providerUrlField.getText());
        properties.setJndiInitialContextFactory(initialContextFactoryField.getText());
        properties.setJndiConnectionFactoryName(connectionFactoryNameField.getText());
//        properties.setConnectionFactoryClass(((JmsDriverInfo) jmsProviderComboBox.getSelectedItem()).getConnectionFactoryClass());
        properties.setConnectionFactoryClass(connectionFactoryClassField.getText());
        properties.setUsername(usernameField.getText());
        properties.setPassword(passwordField.getText());
        properties.setDestinationName(destinationNameField.getText());
        properties.setTopic(destinationTypeTopic.isSelected());
        properties.setDurableTopic(durableTopicCheckbox.isSelected());
        properties.setClientId(clientIdField.getText());
        properties.setSelector(selectorField.getText());
        properties.setReconnectIntervalMillis(reconnectIntervalField.getText());
        properties.setConnectionProperties(connectionPropertiesTable.getProperties());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        JmsReceiverProperties jmsReceiverProperties = (JmsReceiverProperties) properties;

        if (jmsReceiverProperties.isUseJndi()) {
            useJndiYes.setSelected(true);
            useJndiNo.setSelected(false);
            useJndiYesActionPerformed(null);
        } else {
            useJndiYes.setSelected(false);
            useJndiNo.setSelected(true);
            useJndiNoActionPerformed(null);
        }

        providerUrlField.setText(jmsReceiverProperties.getJndiProviderUrl());
        initialContextFactoryField.setText(jmsReceiverProperties.getJndiInitialContextFactory());
        connectionFactoryNameField.setText(jmsReceiverProperties.getJndiConnectionFactoryName());

//        for (JmsDriverInfo driver : jmsDrivers) {
//            if (jmsReceiverProperties.getConnectionFactoryClass().equals(driver.getConnectionFactoryClass())) {
//                jmsProviderComboBox.setSelectedItem(driver);
//            }
//        }

        connectionFactoryClassField.setText(jmsReceiverProperties.getConnectionFactoryClass());
        usernameField.setText(jmsReceiverProperties.getUsername());
        passwordField.setText(jmsReceiverProperties.getPassword());
        destinationNameField.setText(jmsReceiverProperties.getDestinationName());

        if (jmsReceiverProperties.isTopic()) {
            destinationTypeQueue.setSelected(false);
            destinationTypeTopic.setSelected(true);
            destinationTypeTopicActionPerformed(null);
        } else {
            destinationTypeQueue.setSelected(true);
            destinationTypeTopic.setSelected(false);
            destinationTypeQueueActionPerformed(null);
        }

        durableTopicCheckbox.setSelected(jmsReceiverProperties.isDurableTopic());
        clientIdField.setText(jmsReceiverProperties.getClientId());
        selectorField.setText(jmsReceiverProperties.getSelector());
        reconnectIntervalField.setText(jmsReceiverProperties.getReconnectIntervalMillis());
        connectionPropertiesTable.setProperties(jmsReceiverProperties.getConnectionProperties());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new JmsReceiverProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        JmsReceiverProperties jmsReceiverProperties = (JmsReceiverProperties) properties;
        boolean valid = true;

        if (jmsReceiverProperties.isUseJndi()) {
            if (jmsReceiverProperties.getJndiProviderUrl().length() == 0) {
                valid = false;

                if (highlight) {
                    providerUrlField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (jmsReceiverProperties.getJndiInitialContextFactory().length() == 0) {
                valid = false;

                if (highlight) {
                    initialContextFactoryField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (jmsReceiverProperties.getJndiConnectionFactoryName().length() == 0) {
                valid = false;

                if (highlight) {
                    connectionFactoryNameField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        } else if (jmsReceiverProperties.getConnectionFactoryClass() == null) {
            valid = false;

            if (highlight) {
//                jmsProviderComboBox.setBackground(UIConstants.INVALID_COLOR);
                connectionFactoryClassField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (jmsReceiverProperties.getDestinationName().length() == 0) {
            valid = false;

            if (highlight) {
                destinationNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (jmsReceiverProperties.isTopic() && jmsReceiverProperties.isDurableTopic() && jmsReceiverProperties.getClientId().length() == 0) {
            valid = false;

            if (highlight) {
                clientIdField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        providerUrlField.setBackground(null);
        initialContextFactoryField.setBackground(null);
        connectionFactoryNameField.setBackground(null);
//        jmsProviderComboBox.setBackground(null);
        connectionFactoryClassField.setBackground(null);
        destinationNameField.setBackground(null);
        clientIdField.setBackground(null);
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

        useJndiButtonGroup = new javax.swing.ButtonGroup();
        durableButtonGroup = new javax.swing.ButtonGroup();
        destinationTypeButtonGroup = new javax.swing.ButtonGroup();
        useJndiLabel = new javax.swing.JLabel();
        providerUrlLabel = new javax.swing.JLabel();
        initialContextFactoryLabel = new javax.swing.JLabel();
        connectionFactoryNameLabel = new javax.swing.JLabel();
        connectionFactoryClassLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        clientIdLabel = new javax.swing.JLabel();
        destinationNameLabel = new javax.swing.JLabel();
        selectorLabel = new javax.swing.JLabel();
        providerUrlField = new com.mirth.connect.client.ui.components.MirthTextField();
        initialContextFactoryField = new com.mirth.connect.client.ui.components.MirthTextField();
        connectionFactoryNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        passwordField = new com.mirth.connect.client.ui.components.MirthTextField();
        clientIdField = new com.mirth.connect.client.ui.components.MirthTextField();
        destinationNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        selectorField = new com.mirth.connect.client.ui.components.MirthTextField();
        destinationTypeLabel = new javax.swing.JLabel();
        connectionPropertiesLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        connectionPropertiesTable = new com.mirth.connect.client.ui.components.MirthPropertiesTable();
        newButton = new com.mirth.connect.client.ui.components.MirthButton();
        deleteButton = new com.mirth.connect.client.ui.components.MirthButton();
        connectionFactoryClassField = new com.mirth.connect.client.ui.components.MirthTextField();
        reconnectIntervalLabel = new javax.swing.JLabel();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        useJndiYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useJndiNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        destinationTypeQueue = new com.mirth.connect.client.ui.components.MirthRadioButton();
        destinationTypeTopic = new com.mirth.connect.client.ui.components.MirthRadioButton();
        durableTopicCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();

        setBackground(new java.awt.Color(255, 255, 255));

        useJndiLabel.setText("Use JNDI:");

        providerUrlLabel.setText("Provider URL:");

        initialContextFactoryLabel.setText("Initial Context Factory:");

        connectionFactoryNameLabel.setText("Connection Factory Name:");

        connectionFactoryClassLabel.setText("Connection Factory Class:");

        usernameLabel.setText("Username:");

        passwordLabel.setText("Password:");

        clientIdLabel.setText("Client ID:");

        destinationNameLabel.setText("Destination Name:");

        selectorLabel.setText("Selector Expression:");

        providerUrlField.setToolTipText("If using JNDI, enter the URL of the JNDI provider here.");

        initialContextFactoryField.setToolTipText("If using JNDI, enter the full Java classname of the JNDI Initial Context Factory class here.");

        connectionFactoryNameField.setToolTipText("If using JNDI, enter the JNDI name of the connection factory here.");

        usernameField.setToolTipText("The username for accessing the queue or topic.");

        passwordField.setToolTipText("The password for accessing the queue or topic.");

        clientIdField.setToolTipText("A unique identifier for the source connector when connecting to the JMS broker (typically used when accessing a durable queue/topic).");

        destinationNameField.setToolTipText("The name of the queue or topic.");

        selectorField.setToolTipText("<html>Enter the selector expression to select which topic or queue messages the source connector is interested in,<br> or leave blank to read all messages.</html>");

        destinationTypeLabel.setText("Destination Type:");

        connectionPropertiesLabel.setText("Connection Properties:");

        jScrollPane2.setViewportView(connectionPropertiesTable);

        newButton.setText("New");
        newButton.setToolTipText("<html>Adds a new row to end of the list.<br>Double click the Property and Value cells to enter their values.</html>");

        deleteButton.setText("Delete");
        deleteButton.setToolTipText("Deletes the currently selected row from the list.");

        connectionFactoryClassField.setToolTipText("If not using JNDI, enter the full Java classname of the JMS connection factory here.");

        reconnectIntervalLabel.setText("Reconnect Interval (milliseconds):");

        reconnectIntervalField.setToolTipText("The number of milliseconds between reconnect attempts when a connection error occurs.");

        useJndiYes.setBackground(new java.awt.Color(255, 255, 255));
        useJndiButtonGroup.add(useJndiYes);
        useJndiYes.setText("Yes");
        useJndiYes.setToolTipText("<html>Select Yes to use JNDI to look up a connection factory to connect to the queue or topic.<br/>Select No to specify a connection factory class without using JNDI.</html>");
        useJndiYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJndiYesActionPerformed(evt);
            }
        });

        useJndiNo.setBackground(new java.awt.Color(255, 255, 255));
        useJndiButtonGroup.add(useJndiNo);
        useJndiNo.setText("No");
        useJndiNo.setToolTipText("<html>Select Yes to use JNDI to look up a connection factory to connect to the queue or topic.<br/>Select No to specify a connection factory class without using JNDI.</html>");
        useJndiNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJndiNoActionPerformed(evt);
            }
        });

        destinationTypeQueue.setBackground(new java.awt.Color(255, 255, 255));
        destinationTypeButtonGroup.add(destinationTypeQueue);
        destinationTypeQueue.setText("Queue");
        destinationTypeQueue.setToolTipText("If not using JNDI, specify whether the destination is a queue or a topic.");
        destinationTypeQueue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationTypeQueueActionPerformed(evt);
            }
        });

        destinationTypeTopic.setBackground(new java.awt.Color(255, 255, 255));
        destinationTypeButtonGroup.add(destinationTypeTopic);
        destinationTypeTopic.setText("Topic");
        destinationTypeTopic.setToolTipText("If not using JNDI, specify whether the destination is a queue or a topic.");
        destinationTypeTopic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationTypeTopicActionPerformed(evt);
            }
        });

        durableTopicCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        durableTopicCheckbox.setText("Durable");
        durableTopicCheckbox.setToolTipText("<html>When connecting to a topic, if this box is checked, all messages published to the topic will be read,<br/>regardless of whether or not a connection to the broker is active.<br/>If not checked, only messages published while a connection is active will be read.</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connectionPropertiesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(selectorLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(reconnectIntervalLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(clientIdLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(destinationTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(destinationNameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(connectionFactoryClassLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(connectionFactoryNameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(initialContextFactoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(providerUrlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(useJndiLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selectorField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(destinationTypeQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(destinationTypeTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(durableTopicCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(destinationNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(connectionFactoryClassField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(connectionFactoryNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(initialContextFactoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(providerUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(useJndiYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useJndiNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(clientIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationTypeLabel)
                    .addComponent(destinationTypeQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destinationTypeTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(durableTopicCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clientIdLabel)
                    .addComponent(clientIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectorLabel)
                    .addComponent(selectorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(connectionPropertiesLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 56, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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

    private void destinationTypeQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationTypeQueueActionPerformed
        durableTopicCheckbox.setEnabled(false);
    }//GEN-LAST:event_destinationTypeQueueActionPerformed

    private void destinationTypeTopicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationTypeTopicActionPerformed
        durableTopicCheckbox.setEnabled(true);
    }//GEN-LAST:event_destinationTypeTopicActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTextField clientIdField;
    private javax.swing.JLabel clientIdLabel;
    private com.mirth.connect.client.ui.components.MirthTextField connectionFactoryClassField;
    private javax.swing.JLabel connectionFactoryClassLabel;
    private com.mirth.connect.client.ui.components.MirthTextField connectionFactoryNameField;
    private javax.swing.JLabel connectionFactoryNameLabel;
    private javax.swing.JLabel connectionPropertiesLabel;
    private com.mirth.connect.client.ui.components.MirthPropertiesTable connectionPropertiesTable;
    private com.mirth.connect.client.ui.components.MirthButton deleteButton;
    private com.mirth.connect.client.ui.components.MirthTextField destinationNameField;
    private javax.swing.JLabel destinationNameLabel;
    private javax.swing.ButtonGroup destinationTypeButtonGroup;
    private javax.swing.JLabel destinationTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton destinationTypeQueue;
    private com.mirth.connect.client.ui.components.MirthRadioButton destinationTypeTopic;
    private javax.swing.ButtonGroup durableButtonGroup;
    private com.mirth.connect.client.ui.components.MirthCheckBox durableTopicCheckbox;
    private com.mirth.connect.client.ui.components.MirthTextField initialContextFactoryField;
    private javax.swing.JLabel initialContextFactoryLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private com.mirth.connect.client.ui.components.MirthButton newButton;
    private com.mirth.connect.client.ui.components.MirthTextField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField providerUrlField;
    private javax.swing.JLabel providerUrlLabel;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthTextField selectorField;
    private javax.swing.JLabel selectorLabel;
    private javax.swing.ButtonGroup useJndiButtonGroup;
    private javax.swing.JLabel useJndiLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJndiNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJndiYes;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
    // @formatter:on
}
