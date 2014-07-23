/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.connectors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.mirth.connect.client.ui.AbstractConnectorPropertiesPanel;
import com.mirth.connect.client.ui.ChannelSetup;
import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.InvalidConnectorPluginProperties;
import com.mirth.connect.model.MessageStorageMode;
import com.mirth.connect.plugins.ConnectorPropertiesPlugin;

public class ConnectorPanel extends JPanel {
    private ChannelSetup channelSetup;
    private ConnectorSettingsPanel currentPanel;
    private JPanel connectorSettingsContainer;
    private ListenerSettingsPanel listenerSettingsPanel;
    private PollingSettingsPanel pollingSettingsPanel;
    private ResponseSettingsPanel responseSettingsPanel;
    private DestinationSettingsPanel destinationSettingsPanel;
    private Map<String, AbstractConnectorPropertiesPanel> connectorPropertiesPanels = new HashMap<String, AbstractConnectorPropertiesPanel>();

    public ConnectorPanel() {
        super(new MigLayout("insets 0, novisualpadding, hidemode 3, fill", "[grow]"));
        setBackground(UIConstants.BACKGROUND_COLOR);
        initComponents();

        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            connectorPropertiesPanels.get(connectorPropertiesPlugin.getPluginPointName()).setConnectorPanel(this);
        }
    }

    public void setChannelSetup(ChannelSetup channelSetup) {
        this.channelSetup = channelSetup;
        destinationSettingsPanel.setChannelSetup(channelSetup);
        responseSettingsPanel.setChannelSetup(channelSetup);
    }

    public void setConnectorSettingsPanel(ConnectorSettingsPanel panel) {
        if (currentPanel != null) {
            connectorSettingsContainer.remove(currentPanel);
        }

        connectorSettingsContainer.add(panel);
        connectorSettingsContainer.revalidate();
        currentPanel = panel;
        currentPanel.setConnectorPanel(this);

        ((TitledBorder) connectorSettingsContainer.getBorder()).setTitle(panel.getConnectorName() + " Settings");

        ConnectorProperties connectorProperties = getConnectorSettingsPanel().getDefaults();
        pollingSettingsPanel.setVisible(connectorProperties instanceof PollConnectorPropertiesInterface);
        listenerSettingsPanel.setVisible(connectorProperties instanceof ListenerConnectorPropertiesInterface);
        responseSettingsPanel.setVisible(connectorProperties instanceof ResponseConnectorPropertiesInterface);
        destinationSettingsPanel.setVisible(connectorProperties instanceof DestinationConnectorPropertiesInterface);

        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            connectorPropertiesPanels.get(connectorPropertiesPlugin.getPluginPointName()).setVisible(connectorPropertiesPlugin.isSupported(connectorProperties.getName()));
        }
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
            pollingSettingsPanel.fillProperties((PollConnectorPropertiesInterface) connectorProperties);
        }

        if (connectorProperties instanceof ListenerConnectorPropertiesInterface) {
            listenerSettingsPanel.fillProperties((ListenerConnectorPropertiesInterface) connectorProperties);
        }

        if (connectorProperties instanceof ResponseConnectorPropertiesInterface) {
            responseSettingsPanel.fillProperties((ResponseConnectorPropertiesInterface) connectorProperties);
        }

        if (connectorProperties instanceof DestinationConnectorPropertiesInterface) {
            destinationSettingsPanel.fillProperties((DestinationConnectorPropertiesInterface) connectorProperties);
        }

        if (connectorProperties != null) {
            Set<ConnectorPluginProperties> connectorPluginProperties = new HashSet<ConnectorPluginProperties>();

            for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
                if (connectorPropertiesPlugin.isSupported(connectorProperties.getName())) {
                    connectorPluginProperties.add(connectorPropertiesPanels.get(connectorPropertiesPlugin.getPluginPointName()).getProperties());
                }
            }

            connectorProperties.setPluginProperties(connectorPluginProperties);
        }

        return connectorProperties;
    }

    public void setProperties(ConnectorProperties properties) {
        Mode mode = Mode.SOURCE;

        if (properties instanceof PollConnectorPropertiesInterface) {
            pollingSettingsPanel.resetInvalidProperties();
            pollingSettingsPanel.setProperties((PollConnectorPropertiesInterface) properties);
        }

        if (properties instanceof ListenerConnectorPropertiesInterface) {
            listenerSettingsPanel.resetInvalidProperties();
            listenerSettingsPanel.setProperties((ListenerConnectorPropertiesInterface) properties);
        }

        if (properties instanceof ResponseConnectorPropertiesInterface) {
            responseSettingsPanel.resetInvalidProperties();
            responseSettingsPanel.setProperties((ResponseConnectorPropertiesInterface) properties);
        }

        if (properties instanceof DestinationConnectorPropertiesInterface) {
            mode = Mode.DESTINATION;
            destinationSettingsPanel.resetInvalidProperties();
            destinationSettingsPanel.setProperties((DestinationConnectorPropertiesInterface) properties);
        }

        Set<String> addedPluginProperties = new HashSet<String>();

        // Set all properties existing in the model
        if (properties.getPluginProperties() != null) {
            for (ConnectorPluginProperties connectorPluginProperties : properties.getPluginProperties()) {
                if (!(connectorPluginProperties instanceof InvalidConnectorPluginProperties)) {
                    AbstractConnectorPropertiesPanel connectorPluginPropertiesPanel = connectorPropertiesPanels.get(connectorPluginProperties.getName());

                    if (connectorPluginPropertiesPanel != null) {
                        connectorPluginPropertiesPanel.resetInvalidProperties();
                        connectorPluginPropertiesPanel.setProperties(connectorPluginProperties, mode, properties.getName());
                        addedPluginProperties.add(connectorPluginProperties.getName());
                    }
                }
            }
        }

        // For any supported plugin properties that weren't in the model, set the defaults
        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            String pluginPropertiesName = connectorPropertiesPlugin.getPluginPointName();

            if (connectorPropertiesPlugin.isSupported(properties.getName()) && !addedPluginProperties.contains(pluginPropertiesName)) {
                AbstractConnectorPropertiesPanel connectorPluginPropertiesPanel = connectorPropertiesPanels.get(pluginPropertiesName);
                connectorPluginPropertiesPanel.resetInvalidProperties();
                connectorPluginPropertiesPanel.setProperties(connectorPluginPropertiesPanel.getDefaults(), mode, properties.getName());
            }
        }

        getConnectorSettingsPanel().resetInvalidProperties();
        getConnectorSettingsPanel().setProperties(properties);
    }

    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        return checkProperties(getConnectorSettingsPanel(), properties, highlight);
    }

    public boolean checkProperties(ConnectorSettingsPanel connectorSettingsPanel, ConnectorProperties properties, boolean highlight) {
        boolean polling = true;
        Mode mode = Mode.SOURCE;

        if (properties instanceof PollConnectorPropertiesInterface) {
            pollingSettingsPanel.resetInvalidProperties();
            polling = pollingSettingsPanel.checkProperties((PollConnectorPropertiesInterface) properties, highlight);
        }

        boolean listener = true;

        if (properties instanceof ListenerConnectorPropertiesInterface) {
            listenerSettingsPanel.resetInvalidProperties();
            listener = listenerSettingsPanel.checkProperties((ListenerConnectorPropertiesInterface) properties, highlight);
        }

        boolean response = true;

        if (properties instanceof ResponseConnectorPropertiesInterface) {
            responseSettingsPanel.resetInvalidProperties();
            listener = responseSettingsPanel.checkProperties((ResponseConnectorPropertiesInterface) properties, highlight);
        }

        boolean destination = true;

        if (properties instanceof DestinationConnectorPropertiesInterface) {
            mode = Mode.DESTINATION;
            destinationSettingsPanel.resetInvalidProperties();
            destination = destinationSettingsPanel.checkProperties((DestinationConnectorPropertiesInterface) properties, highlight);
        }

        boolean pluginProperties = true;

        if (properties.getPluginProperties() != null) {
            for (ConnectorPluginProperties connectorPluginProperties : properties.getPluginProperties()) {
                if (!(connectorPluginProperties instanceof InvalidConnectorPluginProperties)) {
                    AbstractConnectorPropertiesPanel connectorPluginPropertiesPanel = connectorPropertiesPanels.get(connectorPluginProperties.getName());

                    if (connectorPluginPropertiesPanel != null) {
                        connectorPluginPropertiesPanel.resetInvalidProperties();
                        if (!connectorPluginPropertiesPanel.checkProperties(connectorPluginProperties, mode, properties.getName(), highlight)) {
                            pluginProperties = false;
                        }
                    }
                }
            }
        }

        connectorSettingsPanel.resetInvalidProperties();
        boolean connector = connectorSettingsPanel.checkProperties(properties, highlight);

        return (connector && polling && listener && response && destination && pluginProperties);
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
            responseSettingsPanel.updateResponseDropDown((ResponseConnectorPropertiesInterface) getConnectorSettingsPanel().getProperties(), false);
        }
    }

    public ConnectorProperties getDefaults() {
        ConnectorProperties properties = getConnectorSettingsPanel().getDefaults();

        Set<ConnectorPluginProperties> connectorPluginProperties = new HashSet<ConnectorPluginProperties>();

        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            if (connectorPropertiesPlugin.isSupported(properties.getName())) {
                connectorPluginProperties.add(connectorPropertiesPanels.get(connectorPropertiesPlugin.getPluginPointName()).getDefaults());
            }
        }

        properties.setPluginProperties(connectorPluginProperties);

        return properties;
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
        destinationSettingsPanel.updateQueueWarning(messageStorageMode);
        responseSettingsPanel.updateQueueWarning(messageStorageMode);
    }

    public void decorateConnectorType() {
        ConnectorTypeDecoration connectorTypeDecoration = currentPanel.getConnectorTypeDecoration();

        // Give priority to connector property plugins for decoration
        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            if (connectorPropertiesPlugin.isSupported(currentPanel.getConnectorName())) {
                ConnectorTypeDecoration tempConnectorTypeDecoration = connectorPropertiesPanels.get(connectorPropertiesPlugin.getPluginPointName()).getConnectorTypeDecoration();
                if (tempConnectorTypeDecoration != null) {
                    connectorTypeDecoration = tempConnectorTypeDecoration;
                }
            }
        }

        channelSetup.decorateConnectorType(connectorTypeDecoration);

        currentPanel.doLocalDecoration(connectorTypeDecoration);
        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            if (connectorPropertiesPlugin.isSupported(currentPanel.getConnectorName())) {
                connectorPropertiesPanels.get(connectorPropertiesPlugin.getPluginPointName()).doLocalDecoration(connectorTypeDecoration);
            }
        }
    }

    void handlePluginConnectorServiceResponse(String method, Object response) {
        boolean handleResponse = true;

        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            if (connectorPropertiesPlugin.isSupported(currentPanel.getConnectorName())) {
                if (!connectorPropertiesPanels.get(connectorPropertiesPlugin.getPluginPointName()).handleConnectorServiceResponse(currentPanel, method, response)) {
                    handleResponse = false;
                }
            }
        }

        if (handleResponse) {
            currentPanel.handleConnectorServiceResponse(method, response);
        }
    }

    private void initComponents() {
        listenerSettingsPanel = new ListenerSettingsPanel();
        add(listenerSettingsPanel, "growx, wrap");

        pollingSettingsPanel = new PollingSettingsPanel();
        add(pollingSettingsPanel, "growx, wrap");

        responseSettingsPanel = new ResponseSettingsPanel();
        add(responseSettingsPanel, "growx, wrap");

        destinationSettingsPanel = new DestinationSettingsPanel();
        add(destinationSettingsPanel, "growx, wrap");

        for (ConnectorPropertiesPlugin connectorPropertiesPlugin : LoadedExtensions.getInstance().getConnectorPropertiesPlugins().values()) {
            AbstractConnectorPropertiesPanel connectorPropertiesPanel = connectorPropertiesPlugin.getConnectorPropertiesPanel();
            connectorPropertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), connectorPropertiesPlugin.getSettingsTitle(), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));
            connectorPropertiesPanels.put(connectorPropertiesPlugin.getPluginPointName(), connectorPropertiesPanel);
            add(connectorPropertiesPanel, "growx, wrap");
        }

        connectorSettingsContainer = new JPanel(new BorderLayout());
        connectorSettingsContainer.setBackground(new Color(255, 255, 255));
        connectorSettingsContainer.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Connector Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));
        add(connectorSettingsContainer, "grow, pushy");
    }
}