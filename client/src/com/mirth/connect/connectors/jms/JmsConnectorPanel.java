/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mirth.connect.connectors.jms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class JmsConnectorPanel extends ConnectorSettingsPanel {
    protected final static int TYPE_LISTENER = 1;
    protected final static int TYPE_SENDER = 2;

    private final static String DEFAULT_PRESET = "<html><font color=\"#333333\">(custom)</font></html>";

    private int connectorType;
    private String connectorName;
    private Frame parent;
    private Object lastPreset = DEFAULT_PRESET;
    private JmsPresetsDialog jmsPresetsDialog;
    private boolean selectingPreset;
    private boolean settingProperties;
    private Logger logger = Logger.getLogger(getClass());

    public JmsConnectorPanel() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        invalidProviderLabel.setVisible(false);
        connectionPropertiesTable.setNewButton(newButton);
        connectionPropertiesTable.setDeleteButton(deleteButton);
        initModifiedListeners();
        jmsPresetsDialog = new JmsPresetsDialog(parent, true, parent, this);
        jmsPresetsDialog.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
    }

    public void init(int connectorType, String connectorName) {
        this.connectorType = connectorType;
        this.connectorName = connectorName;
        refreshPresetList();
    }

    @SuppressWarnings("unchecked")
    private void refreshPresetList() {
        Object selectedPreset = null;

        if (presetComboBox.getModel().getSize() > 0) {
            selectedPreset = presetComboBox.getModel().getSelectedItem();
        } else {
            selectedPreset = DEFAULT_PRESET;
        }

        Set<String> presets;

        try {
            presets = (Set<String>) parent.mirthClient.invokeConnectorService(connectorName, "getPresets", null);
        } catch (ClientException e) {
            logger.error("An error occurred when attempting to retrieve the list of presets", e);
            return;
        }

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        for (String preset : presets) {
            model.addElement(preset);
        }

        if (model.getIndexOf(selectedPreset) == -1) {
            selectedPreset = DEFAULT_PRESET;
        }

        presetComboBox.setModel(model);

        // manually re-select the selected menu item, do not call selectPreset() since we don't want it to update the lastPreset variable
        try {
            selectingPreset = true;
            presetComboBox.getModel().setSelectedItem(selectedPreset.toString());
        } finally {
            selectingPreset = false;
        }

        jmsPresetsDialog.init(connectorName, presets);
    }

    private void initModifiedListeners() {
        DocumentListener documentListener = new DocumentListener() {// @formatter:off
            @Override public void removeUpdate(DocumentEvent e) { setModified(); }
            @Override public void insertUpdate(DocumentEvent e) { setModified(); }
            @Override public void changedUpdate(DocumentEvent e) { setModified(); }
        }; 

        ActionListener actionListener = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { setModified(); }
        };
        
        TableModelListener tableModelListener = new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) { setModified(); }
        }; // @formatter:on

        useJndiYes.addActionListener(actionListener);
        useJndiNo.addActionListener(actionListener);
        providerUrlField.getDocument().addDocumentListener(documentListener);
        initialContextFactoryField.getDocument().addDocumentListener(documentListener);
        connectionFactoryNameField.getDocument().addDocumentListener(documentListener);
        connectionFactoryClassField.getDocument().addDocumentListener(documentListener);
        usernameField.getDocument().addDocumentListener(documentListener);
        passwordField.getDocument().addDocumentListener(documentListener);
        destinationNameField.getDocument().addDocumentListener(documentListener);
        destinationTypeQueue.addActionListener(actionListener);
        destinationTypeTopic.addActionListener(actionListener);
        durableTopicCheckbox.addActionListener(actionListener);
        clientIdField.getDocument().addDocumentListener(documentListener);
        reconnectIntervalField.getDocument().addDocumentListener(documentListener);
        connectionPropertiesTable.getModel().addTableModelListener(tableModelListener);
        newButton.addActionListener(actionListener);
        deleteButton.addActionListener(actionListener);
    }

    private void setModified() {
        if (!savePresetButton.isEnabled() && !settingProperties) {
            savePresetButton.setEnabled(true);
            selectPreset(DEFAULT_PRESET);
        }
    }

    private void selectPreset(String presetName) {
        selectingPreset = true;

        try {
            presetComboBox.getModel().setSelectedItem(presetName);
        } finally {
            selectingPreset = false;
            lastPreset = presetComboBox.getModel().getSelectedItem();
        }
    }

    protected void removePreset(String presetName) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) presetComboBox.getModel();

        if (model.getSelectedItem().equals(presetName)) {
            savePresetButton.setEnabled(true);
            selectPreset(DEFAULT_PRESET);
        }

        model.removeElement(presetName);
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
        properties.setUsername(usernameField.getText());
        properties.setPassword(passwordField.getText());
        properties.setDestinationName(destinationNameField.getText());
        properties.setTopic(destinationTypeTopic.isSelected());

        if (connectorType == TYPE_LISTENER) {
            ((JmsReceiverProperties) properties).setDurableTopic(durableTopicCheckbox.isSelected());
        }

        properties.setClientId(clientIdField.getText());
        properties.setReconnectIntervalMillis(reconnectIntervalField.getText());
        properties.setConnectionProperties(connectionPropertiesTable.getProperties());
        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        JmsConnectorProperties jmsConnectorProperties = (JmsConnectorProperties) properties;
        setPropertiesInternal(jmsConnectorProperties);

        try {
            String presetName = (String) parent.mirthClient.invokeConnectorService(connectorName, "getPresetName", jmsConnectorProperties);

            if (presetName != null) {
                selectPreset(presetName);
                savePresetButton.setEnabled(false);
            } else {
                selectPreset(DEFAULT_PRESET);
                savePresetButton.setEnabled(true);
            }
        } catch (ClientException e) {
            logger.error("Failed to lookup preset information", e);
        }
    }

    private void setPropertiesInternal(JmsConnectorProperties jmsConnectorProperties) {
        settingProperties = true;

        try {
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
            usernameField.setText(jmsConnectorProperties.getUsername());
            passwordField.setText(jmsConnectorProperties.getPassword());
            destinationNameField.setText(jmsConnectorProperties.getDestinationName());

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
            }

            clientIdField.setText(jmsConnectorProperties.getClientId());
            reconnectIntervalField.setText(jmsConnectorProperties.getReconnectIntervalMillis());
            connectionPropertiesTable.setProperties(jmsConnectorProperties.getConnectionProperties());

            if (connectorType == TYPE_SENDER) {
                durableTopicCheckbox.setVisible(false);
            }
        } finally {
            settingProperties = false;
        }
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
        presetsLabel = new javax.swing.JLabel();
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
        presetComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        connectionPropertiesLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        connectionPropertiesTable = new com.mirth.connect.client.ui.components.MirthPropertiesTable();
        newButton = new com.mirth.connect.client.ui.components.MirthButton();
        deleteButton = new com.mirth.connect.client.ui.components.MirthButton();
        reconnectIntervalLabel = new javax.swing.JLabel();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        passwordField = new javax.swing.JPasswordField();
        invalidProviderLabel = new javax.swing.JLabel();
        savePresetButton = new com.mirth.connect.client.ui.components.MirthButton();
        managePresetsButton = new com.mirth.connect.client.ui.components.IconButton();
        clientIdLabel = new javax.swing.JLabel();
        clientIdField = new com.mirth.connect.client.ui.components.MirthTextField();

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

        presetsLabel.setText("Preset:");

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

        connectionFactoryNameLabel.setText("Connection Factory Name:");

        providerUrlLabel.setText("Provider URL:");

        initialContextFactoryLabel.setText("Initial Context Factory:");

        useJndiLabel.setText("Use JNDI:");

        presetComboBox.setToolTipText("<html>The JMS provider type. If 'Custom' is selected, then a JMS provider class must be specified.</html>");
        presetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetComboBoxActionPerformed(evt);
            }
        });

        connectionPropertiesLabel.setText("Properties:");

        jScrollPane2.setViewportView(connectionPropertiesTable);

        newButton.setText("New");
        newButton.setToolTipText("<html>Adds a new row to end of the list.<br>Double click the Property and Value cells to enter their values.</html>");

        deleteButton.setText("Delete");
        deleteButton.setToolTipText("Deletes the currently selected row from the list.");

        reconnectIntervalLabel.setText("Reconnect Interval (milliseconds):");

        reconnectIntervalField.setToolTipText("The number of milliseconds between reconnect attempts when a connection error occurs.");

        passwordField.setToolTipText("The password for accessing the queue or topic.");

        invalidProviderLabel.setForeground(new java.awt.Color(255, 0, 0));

        savePresetButton.setText("Save");
        savePresetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePresetButtonActionPerformed(evt);
            }
        });

        managePresetsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mirth/connect/client/ui/images/wrench.png"))); // NOI18N
        managePresetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePresetsButtonActionPerformed(evt);
            }
        });

        clientIdLabel.setText("Client ID:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(clientIdLabel)
                    .addComponent(destinationTypeLabel)
                    .addComponent(destinationNameLabel)
                    .addComponent(passwordLabel)
                    .addComponent(usernameLabel)
                    .addComponent(connectionFactoryClassLabel)
                    .addComponent(connectionFactoryNameLabel)
                    .addComponent(initialContextFactoryLabel)
                    .addComponent(providerUrlLabel)
                    .addComponent(useJndiLabel)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(connectionPropertiesLabel)
                    .addComponent(presetsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connectionFactoryClassField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectionFactoryNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(initialContextFactoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(destinationTypeQueue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(destinationTypeTopic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(durableTopicCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(passwordField, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(usernameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(useJndiYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(useJndiNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(presetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(savePresetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(managePresetsButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(81, 81, 81)
                        .addComponent(invalidProviderLabel))
                    .addComponent(providerUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(clientIdField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(destinationNameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(presetsLabel)
                        .addComponent(presetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(savePresetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(managePresetsButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(invalidProviderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(connectionPropertiesLabel)
                            .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(77, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
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

    private void savePresetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePresetButtonActionPerformed
        Object presetName;
        
        do {
            presetName = JOptionPane.showInputDialog(this, "Enter a name for the new preset:", "Save Preset", JOptionPane.QUESTION_MESSAGE, UIConstants.ICON_INFORMATION, null, null);
            
            if (presetName == null) {
                return;
            }

            if (presetName.toString().trim().length() == 0) {
                JOptionPane.showMessageDialog(this, "Preset name cannot be blank", "Error", JOptionPane.ERROR_MESSAGE, UIConstants.ICON_ERROR);
                presetName = null;
            }
        } while (presetName == null);

        DefaultComboBoxModel model = (DefaultComboBoxModel) presetComboBox.getModel();
        
        if (model.getIndexOf(presetName) != -1) {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to overwrite the preset '" + presetName + "'?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, UIConstants.ICON_WARNING);
            
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        JmsConnectorProperties connectorProperties = (JmsConnectorProperties) getProperties();
        connectorProperties.setPassword(""); // for security reasons, we don't include the password in the preset
        
        try {
            parent.mirthClient.invokeConnectorService(connectorName, "savePreset", new Object[] { presetName, connectorProperties });
            
            if (model.getIndexOf(presetName) == -1) {
                model.addElement(presetName);
                jmsPresetsDialog.addPreset(presetName.toString());
            }
            
            selectPreset(presetName.toString());
            savePresetButton.setEnabled(false);
        } catch (ClientException e) {
            logger.error("Failed to save preset", e);
            parent.alertException(this, e.getStackTrace(), "Failed to save preset");
        }
        
        refreshPresetList();
    }//GEN-LAST:event_savePresetButtonActionPerformed

    private void presetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetComboBoxActionPerformed
        if (selectingPreset) {
            return;
        }

        Object presetName = presetComboBox.getSelectedItem();
        
        if (presetName.equals(DEFAULT_PRESET) || presetName.equals(lastPreset)) {
            return;
        }
        
        refreshPresetList();
        
        if (((DefaultComboBoxModel) presetComboBox.getModel()).getIndexOf(presetName) == -1) {
            parent.alertError(this, "The preset \"" + presetName + "\" no longer exists on the server");
            selectPreset(lastPreset.toString());
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, "Warning: this will replace the current settings with preset '" + presetName + "', continue?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, UIConstants.ICON_WARNING);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                JmsConnectorProperties connectorProperties = (connectorType == TYPE_LISTENER) ? new JmsReceiverProperties() : new JmsDispatcherProperties();
                Object object = parent.mirthClient.invokeConnectorService(this.connectorName, "getPreset", presetName);
                
                if (object == null) {
                    parent.alertError(this, "Failed to load the preset \"" + presetName + "\" from the server");
                } else {
                    connectorProperties.setProperties((JmsConnectorProperties) object);
                    setPropertiesInternal(connectorProperties);
                    savePresetButton.setEnabled(false);
                }
                
                lastPreset = presetName;
            } catch (ClientException e) {
                logger.error("Failed to load preset", e);
                parent.alertException(this, e.getStackTrace(), "Failed to load preset");
                selectPreset(lastPreset.toString());
            }
        } else {
            selectPreset(lastPreset.toString());
        }
    }//GEN-LAST:event_presetComboBoxActionPerformed

    private void managePresetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePresetsButtonActionPerformed
        refreshPresetList();
        jmsPresetsDialog.setLocationRelativeTo(parent);
        jmsPresetsDialog.setVisible(true);
    }//GEN-LAST:event_managePresetsButtonActionPerformed
        
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
    private com.mirth.connect.client.ui.components.MirthCheckBox durableTopicCheckbox;
    private com.mirth.connect.client.ui.components.MirthTextField initialContextFactoryField;
    private javax.swing.JLabel initialContextFactoryLabel;
    private javax.swing.JLabel invalidProviderLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private com.mirth.connect.client.ui.components.IconButton managePresetsButton;
    private com.mirth.connect.client.ui.components.MirthButton newButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox presetComboBox;
    private javax.swing.JLabel presetsLabel;
    private com.mirth.connect.client.ui.components.MirthTextField providerUrlField;
    private javax.swing.JLabel providerUrlLabel;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthButton savePresetButton;
    private javax.swing.ButtonGroup useJndiButtonGroup;
    private javax.swing.JLabel useJndiLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJndiNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useJndiYes;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
    // @formatter:on
}
