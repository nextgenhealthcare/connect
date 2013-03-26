/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.connectors;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.mirth.connect.client.ui.ChannelSetup;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.model.MessageStorageMode;

public class ConnectorPanel extends JPanel {
    private ConnectorSettingsPanel currentPanel;

    public ConnectorPanel() {
        initComponents();
    }

    public void setChannelSetup(ChannelSetup channelSetup) {
        queueSettingsPanel.setChannelSetup(channelSetup);
    }

    public void setConnectorSettingsPanel(ConnectorSettingsPanel panel) {
        if (currentPanel != null) {
            connectorSettingsContainer.remove(currentPanel);
        }

        connectorSettingsContainer.add(panel);
        connectorSettingsContainer.revalidate();
        currentPanel = panel;

        ((TitledBorder) connectorSettingsContainer.getBorder()).setTitle(panel.getConnectorName() + " Settings");

        ConnectorProperties connectorProperties = getConnectorSettingsPanel().getDefaults();
        pollingSettingsPanel.setVisible(connectorProperties instanceof PollConnectorPropertiesInterface);
        listenerSettingsPanel.setVisible(connectorProperties instanceof ListenerConnectorPropertiesInterface);
        responseSettingsPanel.setVisible(connectorProperties instanceof ResponseConnectorPropertiesInterface);
        queueSettingsPanel.setVisible(connectorProperties instanceof QueueConnectorPropertiesInterface);
    }

    private ConnectorSettingsPanel getConnectorSettingsPanel() {
        for (Component component : connectorSettingsContainer.getComponents()) {
            if (component.isVisible()) {
                return (ConnectorSettingsPanel) component;
            }
        }

        return null;
    }

    public ConnectorProperties getProperties() {
        ConnectorSettingsPanel connectorSettingsPanel = getConnectorSettingsPanel();

        if (connectorSettingsPanel == null) {
            return null;
        }

        ConnectorProperties connectorProperties = connectorSettingsPanel.getProperties();

        if (connectorProperties instanceof PollConnectorPropertiesInterface) {
            pollingSettingsPanel.fillProperties(((PollConnectorPropertiesInterface) connectorProperties).getPollConnectorProperties());
        }

        if (connectorProperties instanceof ListenerConnectorPropertiesInterface) {
            listenerSettingsPanel.fillProperties(((ListenerConnectorPropertiesInterface) connectorProperties).getListenerConnectorProperties());
        }

        if (connectorProperties instanceof ResponseConnectorPropertiesInterface) {
            responseSettingsPanel.fillProperties(((ResponseConnectorPropertiesInterface) connectorProperties).getResponseConnectorProperties());
        }

        if (connectorProperties instanceof QueueConnectorPropertiesInterface) {
            queueSettingsPanel.fillProperties(((QueueConnectorPropertiesInterface) connectorProperties).getQueueConnectorProperties());
        }

        return connectorProperties;
    }

    public void setProperties(ConnectorProperties properties) {
        if (properties instanceof PollConnectorPropertiesInterface) {
            pollingSettingsPanel.resetInvalidProperties();
            pollingSettingsPanel.setProperties(((PollConnectorPropertiesInterface) properties).getPollConnectorProperties());
        }

        if (properties instanceof ListenerConnectorPropertiesInterface) {
            listenerSettingsPanel.resetInvalidProperties();
            listenerSettingsPanel.setProperties(((ListenerConnectorPropertiesInterface) properties).getListenerConnectorProperties());
        }

        if (properties instanceof ResponseConnectorPropertiesInterface) {
            responseSettingsPanel.resetInvalidProperties();
            responseSettingsPanel.setProperties(((ResponseConnectorPropertiesInterface) properties).getResponseConnectorProperties());
        }

        if (properties instanceof QueueConnectorPropertiesInterface) {
            queueSettingsPanel.resetInvalidProperties();
            queueSettingsPanel.setProperties(((QueueConnectorPropertiesInterface) properties).getQueueConnectorProperties());
        }

        getConnectorSettingsPanel().resetInvalidProperties();
        getConnectorSettingsPanel().setProperties(properties);
    }

    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        return checkProperties(getConnectorSettingsPanel(), properties, highlight);
    }

    public boolean checkProperties(ConnectorSettingsPanel connectorSettingsPanel, ConnectorProperties properties, boolean highlight) {
        boolean polling = true;

        if (properties instanceof PollConnectorPropertiesInterface) {
            pollingSettingsPanel.resetInvalidProperties();
            polling = pollingSettingsPanel.checkProperties(((PollConnectorPropertiesInterface) properties).getPollConnectorProperties(), highlight);
        }

        boolean listener = true;

        if (properties instanceof ListenerConnectorPropertiesInterface) {
            listenerSettingsPanel.resetInvalidProperties();
            listener = listenerSettingsPanel.checkProperties(((ListenerConnectorPropertiesInterface) properties).getListenerConnectorProperties(), highlight);
        }

        boolean response = true;

        if (properties instanceof ResponseConnectorPropertiesInterface) {
            responseSettingsPanel.resetInvalidProperties();
            listener = responseSettingsPanel.checkProperties(((ResponseConnectorPropertiesInterface) properties).getResponseConnectorProperties(), highlight);
        }

        boolean queue = true;

        if (properties instanceof QueueConnectorPropertiesInterface) {
            queueSettingsPanel.resetInvalidProperties();
            queue = queueSettingsPanel.checkProperties(((QueueConnectorPropertiesInterface) properties).getQueueConnectorProperties(), highlight);
        }

        connectorSettingsPanel.resetInvalidProperties();
        boolean connector = connectorSettingsPanel.checkProperties(properties, highlight);

        return (connector && polling && listener && response && queue);
    }

    public String doValidate(ConnectorProperties properties, boolean highlight) {
        return doValidate(getConnectorSettingsPanel(), properties, highlight);
    }

    public String doValidate(ConnectorSettingsPanel connectorSettingsPanel, ConnectorProperties properties, boolean highlight) {
        String error = null;

        if (!checkProperties(connectorSettingsPanel, properties, highlight)) {
            error = "Error in the form for connector \"" + connectorSettingsPanel.getConnectorName() + "\".\n\n";
        }

        String connectorError = connectorSettingsPanel.doValidate(properties, highlight);

        if (connectorError != null) {
            if (error == null) {
                error = "";
            }

            error += connectorError;
        }

        return error;
    }

    public void updateResponseDropDown() {
        if (getConnectorSettingsPanel().getProperties() instanceof ResponseConnectorPropertiesInterface) {
            responseSettingsPanel.updateResponseDropDown(((ResponseConnectorPropertiesInterface) getConnectorSettingsPanel().getProperties()).getResponseConnectorProperties(), false);
        }
    }

    public ConnectorProperties getDefaults() {
        return getConnectorSettingsPanel().getDefaults();
    }

    public TransferMode getTransferMode() {
        return getConnectorSettingsPanel().getTransferMode();
    }

    public boolean requiresXmlDataType() {
        return getConnectorSettingsPanel().requiresXmlDataType();
    }

    public void updatedField(String field) {
        getConnectorSettingsPanel().updatedField(field);
    }

    public void updateQueueWarning(MessageStorageMode messageStorageMode) {
        queueSettingsPanel.updateQueueWarning(messageStorageMode);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pollingSettingsPanel = new com.mirth.connect.client.ui.panels.connectors.PollingSettingsPanel();
        connectorSettingsContainer = new javax.swing.JPanel();
        queueSettingsPanel = new com.mirth.connect.client.ui.panels.connectors.QueueSettingsPanel();
        listenerSettingsPanel = new com.mirth.connect.client.ui.panels.connectors.ListenerSettingsPanel();
        responseSettingsPanel = new com.mirth.connect.client.ui.panels.connectors.ResponseSettingsPanel();

        setBackground(new java.awt.Color(255, 255, 255));

        connectorSettingsContainer.setBackground(new java.awt.Color(255, 255, 255));
        connectorSettingsContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Connector Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N
        connectorSettingsContainer.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pollingSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(connectorSettingsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(queueSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(listenerSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(responseSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pollingSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listenerSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(responseSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queueSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectorSettingsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel connectorSettingsContainer;
    private com.mirth.connect.client.ui.panels.connectors.ListenerSettingsPanel listenerSettingsPanel;
    private com.mirth.connect.client.ui.panels.connectors.PollingSettingsPanel pollingSettingsPanel;
    private com.mirth.connect.client.ui.panels.connectors.QueueSettingsPanel queueSettingsPanel;
    private com.mirth.connect.client.ui.panels.connectors.ResponseSettingsPanel responseSettingsPanel;
    // End of variables declaration//GEN-END:variables
}
