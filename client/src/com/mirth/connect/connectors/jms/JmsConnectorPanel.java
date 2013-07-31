/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class JmsConnectorPanel extends ConnectorSettingsPanel {
    protected final static int TYPE_LISTENER = 1;
    protected final static int TYPE_SENDER = 2;
    private final static int PROPERTY_COLUMN_WIDTH = 135;

    private String connectorName;
    private int connectorType;
    private Frame parent;
    private JmsTemplateListModel listModel;

    public JmsConnectorPanel() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        connectionPropertiesTable.setNewButton(newButton);
        connectionPropertiesTable.setDeleteButton(deleteButton);
        connectionPropertiesTable.getPropertyColumn().setMinWidth(PROPERTY_COLUMN_WIDTH);
        connectionPropertiesTable.getPropertyColumn().setMaxWidth(PROPERTY_COLUMN_WIDTH);

        templateList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object templateName = templateList.getSelectedValue();

                if (templateName == null) {
                    loadTemplateButton.setEnabled(false);
                    deleteTemplateButton.setEnabled(false);
                } else {
                    loadTemplateButton.setEnabled(true);
                    deleteTemplateButton.setEnabled(!listModel.isPredefinedTemplate(templateName.toString()));
                }
            }
        });

        templateList.setCellRenderer(new TemplateListCellRenderer());
    }

    @SuppressWarnings("unchecked")
    public synchronized void init(int connectorType, String connectorName) {
        this.connectorType = connectorType;
        this.connectorName = connectorName;
        this.listModel = JmsTemplateListModel.getInstance();
    }

    @Override
    public String getConnectorName() {
        return connectorName;
    }

    @Override
    public ConnectorProperties getDefaults() {
        return null;
    }

    @Override
    public ConnectorProperties getProperties() {
        JmsConnectorProperties properties;

        if (connectorType == TYPE_LISTENER) {
            properties = new JmsReceiverProperties();
        } else {
            properties = new JmsDispatcherProperties();
        }

        properties.setUseJndi(useJndiYes.isSelected());
        properties.setJndiProviderUrl(providerUrlField.getText());
        properties.setJndiInitialContextFactory(initialContextFactoryField.getText());
        properties.setJndiConnectionFactoryName(connectionFactoryNameField.getText());
        properties.setConnectionFactoryClass(connectionFactoryClassField.getText());
        properties.setConnectionProperties(connectionPropertiesTable.getProperties());
        properties.setTopic(destinationTypeTopic.isSelected());

        if (connectorType == TYPE_LISTENER) {
            ((JmsReceiverProperties) properties).setDurableTopic(durableTopicCheckbox.isSelected());
        }

        properties.setDestinationName(destinationNameField.getText());
        properties.setClientId(clientIdField.getText());
        properties.setUsername(usernameField.getText());
        properties.setPassword(passwordField.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        refreshTemplates();
        JmsConnectorProperties jmsConnectorProperties = (JmsConnectorProperties) properties;

        if (jmsConnectorProperties.isUseJndi()) {
            useJndiYes.setSelected(true);
            useJndiNo.setSelected(false);
            useJndiYesActionPerformed(null);
        } else {
            useJndiYes.setSelected(false);
            useJndiNo.setSelected(true);
            useJndiNoActionPerformed(null);
        }

        providerUrlField.setText(jmsConnectorProperties.getJndiProviderUrl());
        initialContextFactoryField.setText(jmsConnectorProperties.getJndiInitialContextFactory());
        connectionFactoryNameField.setText(jmsConnectorProperties.getJndiConnectionFactoryName());
        connectionFactoryClassField.setText(jmsConnectorProperties.getConnectionFactoryClass());
        connectionPropertiesTable.setProperties(jmsConnectorProperties.getConnectionProperties());

        if (jmsConnectorProperties.isTopic()) {
            destinationTypeQueue.setSelected(false);
            destinationTypeTopic.setSelected(true);
            destinationTypeTopicActionPerformed(null);
        } else {
            destinationTypeQueue.setSelected(true);
            destinationTypeTopic.setSelected(false);
            destinationTypeQueueActionPerformed(null);
        }

        if (connectorType == TYPE_LISTENER) {
            durableTopicCheckbox.setSelected(((JmsReceiverProperties) jmsConnectorProperties).isDurableTopic());
        } else {
            durableTopicCheckbox.setVisible(false);
        }

        destinationNameField.setText(jmsConnectorProperties.getDestinationName());
        clientIdField.setText(jmsConnectorProperties.getClientId());
        usernameField.setText(jmsConnectorProperties.getUsername());
        passwordField.setText(jmsConnectorProperties.getPassword());
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        boolean valid = true;
        JmsConnectorProperties jmsConnectorProperties = (JmsConnectorProperties) properties;

        if (jmsConnectorProperties.isUseJndi()) {
            if (jmsConnectorProperties.getJndiProviderUrl().length() == 0) {
                valid = false;

                if (highlight) {
                    providerUrlField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (jmsConnectorProperties.getJndiInitialContextFactory().length() == 0) {
                valid = false;

                if (highlight) {
                    initialContextFactoryField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (jmsConnectorProperties.getJndiConnectionFactoryName().length() == 0) {
                valid = false;

                if (highlight) {
                    connectionFactoryNameField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        } else {
            if (jmsConnectorProperties.getConnectionFactoryClass().length() == 0) {
                valid = false;

                if (highlight) {
                    connectionFactoryClassField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (connectorType == TYPE_LISTENER && jmsConnectorProperties.isTopic() && ((JmsReceiverProperties) jmsConnectorProperties).isDurableTopic() && jmsConnectorProperties.getClientId().isEmpty()) {
                valid = false;

                if (highlight) {
                    clientIdField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        if (jmsConnectorProperties.getDestinationName().length() == 0) {
            valid = false;

            if (highlight) {
                destinationNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    public void resetInvalidProperties() {
        providerUrlField.setBackground(null);
        initialContextFactoryField.setBackground(null);
        connectionFactoryNameField.setBackground(null);
        connectionFactoryClassField.setBackground(null);
        clientIdField.setBackground(null);
        destinationNameField.setBackground(null);
    }

    private void refreshTemplates() {
        Object result = null;

        try {
            result = invokeRemoteMethod("getTemplates", null);
        } catch (Exception e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
        }

        if (result != null && result instanceof Map) {
            Map<String, JmsConnectorProperties> templates = (Map<String, JmsConnectorProperties>) result;

            // load templates from the server into the listModel
            for (Entry<String, JmsConnectorProperties> templateEntry : templates.entrySet()) {
                listModel.putTemplate(templateEntry.getKey(), templateEntry.getValue());
            }

            // remove any entries from the listModel that are not on the server (except for pre-defined templates)
            for (int i = 0; i < listModel.getSize(); i++) {
                String templateName = listModel.getElementAt(i).toString();

                if (!templates.containsKey(templateName) && !listModel.isPredefinedTemplate(templateName)) {
                    listModel.deleteTemplate(templateName);
                }
            }
        }

        templateList.setModel(listModel);
    }

    private class TemplateListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // make the read-only templates appear italic and grey
            if (value != null && listModel.isPredefinedTemplate(value.toString())) {
                Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
                attributes.putAll(getFont().getAttributes());
                attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
                attributes.put(TextAttribute.FOREGROUND, new Color(64, 64, 64));
                setFont(new Font(attributes));
            }

            return component;
        }
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
        destinationTypeButtonGroup = new javax.swing.ButtonGroup();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        destinationNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        useJndiYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        providerUrlField = new com.mirth.connect.client.ui.components.MirthTextField();
        useJndiNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        initialContextFactoryField = new com.mirth.connect.client.ui.components.MirthTextField();
        connectionFactoryNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        durableTopicCheckbox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        passwordLabel = new javax.swing.JLabel();
        destinationTypeLabel = new javax.swing.JLabel();
        destinationTypeQueue = new com.mirth.connect.client.ui.components.MirthRadioButton();
        destinationNameLabel = new javax.swing.JLabel();
        destinationTypeTopic = new com.mirth.connect.client.ui.components.MirthRadioButton();
        connectionFactoryClassField = new com.mirth.connect.client.ui.components.MirthTextField();
        usernameLabel = new javax.swing.JLabel();
        connectionFactoryClassLabel = new javax.swing.JLabel();
        connectionFactoryNameLabel = new javax.swing.JLabel();
        providerUrlLabel = new javax.swing.JLabel();
        initialContextFactoryLabel = new javax.swing.JLabel();
        useJndiLabel = new javax.swing.JLabel();
        connectionPropertiesLabel = new javax.swing.JLabel();
        connectionPropertiesScrollPane = new javax.swing.JScrollPane();
        connectionPropertiesTable = new com.mirth.connect.client.ui.components.MirthPropertiesTable();
        newButton = new com.mirth.connect.client.ui.components.MirthButton();
        deleteButton = new com.mirth.connect.client.ui.components.MirthButton();
        passwordField = new javax.swing.JPasswordField();
        clientIdLabel = new javax.swing.JLabel();
        clientIdField = new com.mirth.connect.client.ui.components.MirthTextField();
        templateScrollPane = new javax.swing.JScrollPane();
        templateList = new javax.swing.JList();
        loadTemplateButton = new com.mirth.connect.client.ui.components.MirthButton();
        saveTemplateButton = new com.mirth.connect.client.ui.components.MirthButton();
        deleteTemplateButton = new com.mirth.connect.client.ui.components.MirthButton();

        setBackground(new java.awt.Color(255, 255, 255));

        usernameField.setToolTipText("The username for accessing the queue or topic.");

        destinationNameField.setToolTipText("The name of the queue or topic.");

        useJndiYes.setBackground(new java.awt.Color(255, 255, 255));
        useJndiButtonGroup.add(useJndiYes);
        useJndiYes.setText("Yes");
        useJndiYes.setToolTipText("<html>Select Yes to use JNDI to look up a connection factory to connect to the queue or topic.<br/>Select No to specify a connection factory class without using JNDI.</html>");
        useJndiYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJndiYesActionPerformed(evt);
            }
        });

        providerUrlField.setToolTipText("If using JNDI, enter the URL of the JNDI provider here.");

        useJndiNo.setBackground(new java.awt.Color(255, 255, 255));
        useJndiButtonGroup.add(useJndiNo);
        useJndiNo.setText("No");
        useJndiNo.setToolTipText("<html>Select Yes to use JNDI to look up a connection factory to connect to the queue or topic.<br/>Select No to specify a connection factory class without using JNDI.</html>");
        useJndiNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJndiNoActionPerformed(evt);
            }
        });

        initialContextFactoryField.setToolTipText("If using JNDI, enter the full Java classname of the JNDI Initial Context Factory class here.");

        connectionFactoryNameField.setToolTipText("If using JNDI, enter the JNDI name of the connection factory here.");

        durableTopicCheckbox.setBackground(new java.awt.Color(255, 255, 255));
        durableTopicCheckbox.setText("Durable");
        durableTopicCheckbox.setToolTipText("<html>When connecting to a topic, if this box is checked, all messages published to the topic will be read,<br/>regardless of whether or not a connection to the broker is active.<br/>If not checked, only messages published while a connection is active will be read.</html>");

        passwordLabel.setText("Password:");

        destinationTypeLabel.setText("Destination Type:");

        destinationTypeQueue.setBackground(new java.awt.Color(255, 255, 255));
        destinationTypeButtonGroup.add(destinationTypeQueue);
        destinationTypeQueue.setText("Queue");
        destinationTypeQueue.setToolTipText("If not using JNDI, specify whether the destination is a queue or a topic.");
        destinationTypeQueue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationTypeQueueActionPerformed(evt);
            }
        });

        destinationNameLabel.setText("Destination Name:");

        destinationTypeTopic.setBackground(new java.awt.Color(255, 255, 255));
        destinationTypeButtonGroup.add(destinationTypeTopic);
        destinationTypeTopic.setText("Topic");
        destinationTypeTopic.setToolTipText("If not using JNDI, specify whether the destination is a queue or a topic.");
        destinationTypeTopic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationTypeTopicActionPerformed(evt);
            }
        });

        connectionFactoryClassField.setToolTipText("If using the generic JMS provider and not using JNDI, enter the full Java classname of the JMS connection factory here.");

        usernameLabel.setText("Username:");

        connectionFactoryClassLabel.setText("Connection Factory Class:");

        connectionFactoryNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        connectionFactoryNameLabel.setText("Connection Factory Name:");

        providerUrlLabel.setText("Provider URL:");

        initialContextFactoryLabel.setText("Initial Context Factory:");

        useJndiLabel.setText("Use JNDI:");

        connectionPropertiesLabel.setText("Connection Properties:");

        connectionPropertiesScrollPane.setViewportView(connectionPropertiesTable);

        newButton.setText("New");
        newButton.setToolTipText("<html>Adds a new row to end of the list.<br>Double click the Property and Value cells to enter their values.</html>");

        deleteButton.setText("Delete");
        deleteButton.setToolTipText("Deletes the currently selected row from the list.");

        passwordField.setToolTipText("The password for accessing the queue or topic.");

        clientIdLabel.setText("Client ID:");

        clientIdField.setToolTipText("The JMS client ID to use when connecting to the JMS broker.");

        templateScrollPane.setBorder(null);

        templateList.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Connection Templates", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N
        templateList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        templateScrollPane.setViewportView(templateList);

        loadTemplateButton.setText("Load");
        loadTemplateButton.setToolTipText("<html>Populates connection information using the selected connection template.</html>");
        loadTemplateButton.setEnabled(false);
        loadTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTemplateButtonActionPerformed(evt);
            }
        });

        saveTemplateButton.setText("Save");
        saveTemplateButton.setToolTipText("<html>Saves the current connection information as a new connection template.</html>");
        saveTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTemplateButtonActionPerformed(evt);
            }
        });

        deleteTemplateButton.setText("Delete");
        deleteTemplateButton.setToolTipText("<html>Deletes the selected connection template.</html>");
        deleteTemplateButton.setEnabled(false);
        deleteTemplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTemplateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connectionFactoryNameLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(providerUrlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(connectionFactoryClassLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(connectionPropertiesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(initialContextFactoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(useJndiLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(destinationTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(clientIdLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(destinationNameLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(destinationTypeQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationTypeTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(durableTopicCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(useJndiYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useJndiNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(connectionPropertiesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(connectionFactoryClassField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(connectionFactoryNameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(initialContextFactoryField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(providerUrlField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(templateScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(loadTemplateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveTemplateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteTemplateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(clientIdField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(usernameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(passwordField, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(destinationNameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(connectionPropertiesLabel)
                            .addComponent(connectionPropertiesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(templateScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(loadTemplateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(saveTemplateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deleteTemplateButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationTypeQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destinationTypeTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(durableTopicCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destinationTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(destinationNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clientIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clientIdLabel)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // @formatter:on

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
        durableTopicCheckbox.setEnabled(false);
    }//GEN-LAST:event_useJndiYesActionPerformed

    private void useJndiNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useJndiNoActionPerformed
        providerUrlLabel.setEnabled(false);
        providerUrlField.setEnabled(false);
        initialContextFactoryLabel.setEnabled(false);
        initialContextFactoryField.setEnabled(false);
        connectionFactoryNameLabel.setEnabled(false);
        connectionFactoryNameField.setEnabled(false);
        destinationTypeLabel.setEnabled(true);
        destinationTypeQueue.setEnabled(true);
        destinationTypeTopic.setEnabled(true);

        if (destinationTypeTopic.isSelected()) {
            durableTopicCheckbox.setEnabled(true);
        }

        connectionFactoryClassLabel.setEnabled(true);
        connectionFactoryClassField.setEnabled(true);
    }//GEN-LAST:event_useJndiNoActionPerformed

    private void destinationTypeQueueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationTypeQueueActionPerformed
        durableTopicCheckbox.setEnabled(false);
    }//GEN-LAST:event_destinationTypeQueueActionPerformed

    private void destinationTypeTopicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationTypeTopicActionPerformed
        durableTopicCheckbox.setEnabled(true);
    }//GEN-LAST:event_destinationTypeTopicActionPerformed

    private void loadTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadTemplateButtonActionPerformed
        String templateName = templateList.getSelectedValue().toString();

        if (confirmDialog("Are you sure you want to overwrite the current connection settings with the template: \"" + templateName + "\"?")) {
            JmsConnectorProperties template = listModel.getTemplate(templateName);

            if (template == null) {
                parent.alertError(parent, "The template \"" + templateName + "\" no longer exists on the server.");
            } else {
                if (template.isUseJndi()) {
                    useJndiYes.setSelected(true);
                    useJndiNo.setSelected(false);
                    useJndiYesActionPerformed(null);
                } else {
                    useJndiYes.setSelected(false);
                    useJndiNo.setSelected(true);
                    useJndiNoActionPerformed(null);
                }

                providerUrlField.setText(template.getJndiProviderUrl());
                initialContextFactoryField.setText(template.getJndiInitialContextFactory());
                connectionFactoryNameField.setText(template.getJndiConnectionFactoryName());
                connectionFactoryClassField.setText(template.getConnectionFactoryClass());
                connectionPropertiesTable.setProperties(template.getConnectionProperties());
            }
        }
    }//GEN-LAST:event_loadTemplateButtonActionPerformed

    private void saveTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTemplateButtonActionPerformed
        String templateName = null;
        Object defaultValue = (templateList.getSelectedValue() == null || listModel.isPredefinedTemplate(templateList.getSelectedValue().toString())) ? "" : templateList.getSelectedValue();

        do {
            Object response = JOptionPane.showInputDialog(parent, "Enter a name for the connection template:", "Save", JOptionPane.QUESTION_MESSAGE, null, null, defaultValue);

            if (response == null) {
                return;
            }

            templateName = StringUtils.trim(response.toString());

            if (templateName.isEmpty()) {
                return;
            }

            if (listModel.isPredefinedTemplate(templateName)) {
                parent.alertWarning(parent, "\"" + templateName + "\" is a reserved template and cannot be overwritten. Please enter a different template name.");
                defaultValue = "";
            }
        } while (listModel.isPredefinedTemplate(templateName));

        if (listModel.containsTemplate(templateName) && !confirmDialog("Are you sure you want to overwrite the existing template named \"" + templateName + "\"?")) {
            return;
        }

        JmsConnectorProperties template = new JmsConnectorProperties();
        template.setUseJndi(useJndiYes.isSelected());
        template.setJndiProviderUrl(providerUrlField.getText());
        template.setJndiInitialContextFactory(initialContextFactoryField.getText());
        template.setJndiConnectionFactoryName(connectionFactoryNameField.getText());
        template.setConnectionFactoryClass(connectionFactoryClassField.getText());
        template.setConnectionProperties(connectionPropertiesTable.getProperties());

        try {
            invokeRemoteMethod("saveTemplate", new Object[] { templateName, template });
        } catch (Exception e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
            return;
        }

        listModel.putTemplate(templateName, template);
        templateList.setSelectedValue(templateName, true);
    }//GEN-LAST:event_saveTemplateButtonActionPerformed

    private void deleteTemplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteTemplateButtonActionPerformed
        String templateName = templateList.getSelectedValue().toString();

        if (listModel.isPredefinedTemplate(templateName) || !confirmDialog("Are you sure you want to delete the template \"" + templateName + "\"?")) {
            return;
        }

        try {
            invokeRemoteMethod("deleteTemplate", templateName);
        } catch (Exception e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
            return;
        }

        int selectedIndex = templateList.getSelectedIndex();
        listModel.deleteTemplate(templateName);

        if (selectedIndex >= listModel.getSize()) {
            selectedIndex = listModel.getSize() - 1;
        }

        templateList.setSelectedIndex(selectedIndex);
    }//GEN-LAST:event_deleteTemplateButtonActionPerformed

    private Object invokeRemoteMethod(String method, Object arg) throws Exception {
        return parent.mirthClient.invokeConnectorService(parent.channelEditPanel.currentChannel.getId(), connectorName, method, arg);
    }

    private boolean confirmDialog(String message) {
        return (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTextField clientIdField;
    private javax.swing.JLabel clientIdLabel;
    private com.mirth.connect.client.ui.components.MirthTextField connectionFactoryClassField;
    private javax.swing.JLabel connectionFactoryClassLabel;
    private com.mirth.connect.client.ui.components.MirthTextField connectionFactoryNameField;
    private javax.swing.JLabel connectionFactoryNameLabel;
    private javax.swing.JLabel connectionPropertiesLabel;
    private javax.swing.JScrollPane connectionPropertiesScrollPane;
    private com.mirth.connect.client.ui.components.MirthPropertiesTable connectionPropertiesTable;
    private com.mirth.connect.client.ui.components.MirthButton deleteButton;
    private com.mirth.connect.client.ui.components.MirthButton deleteTemplateButton;
    private com.mirth.connect.client.ui.components.MirthTextField destinationNameField;
    private javax.swing.JLabel destinationNameLabel;
    private javax.swing.ButtonGroup destinationTypeButtonGroup;
    private javax.swing.JLabel destinationTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton destinationTypeQueue;
    private com.mirth.connect.client.ui.components.MirthRadioButton destinationTypeTopic;
    private com.mirth.connect.client.ui.components.MirthCheckBox durableTopicCheckbox;
    private com.mirth.connect.client.ui.components.MirthTextField initialContextFactoryField;
    private javax.swing.JLabel initialContextFactoryLabel;
    private com.mirth.connect.client.ui.components.MirthButton loadTemplateButton;
    private com.mirth.connect.client.ui.components.MirthButton newButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField providerUrlField;
    private javax.swing.JLabel providerUrlLabel;
    private com.mirth.connect.client.ui.components.MirthButton saveTemplateButton;
    private javax.swing.JList templateList;
    private javax.swing.JScrollPane templateScrollPane;
    private javax.swing.ButtonGroup useJndiButtonGroup;
    private javax.swing.JLabel useJndiLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJndiNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJndiYes;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
