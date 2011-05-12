/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.plugins.AttachmentViewer;
import com.mirth.connect.plugins.ChannelColumnPlugin;
import com.mirth.connect.plugins.ChannelPanelPlugin;
import com.mirth.connect.plugins.ChannelWizardPlugin;
import com.mirth.connect.plugins.ClientPlugin;
import com.mirth.connect.plugins.DashboardColumnPlugin;
import com.mirth.connect.plugins.DashboardPanelPlugin;
import com.mirth.connect.plugins.FilterRulePlugin;
import com.mirth.connect.plugins.SettingsPanelPlugin;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class LoadedExtensions {
    private Map<String, ClientPlugin> clientPlugins = new HashMap<String, ClientPlugin>();

    private Map<String, SettingsPanelPlugin> settingsPanelPlugins = new HashMap<String, SettingsPanelPlugin>();
    private Map<String, ChannelPanelPlugin> channelPanelPlugins = new HashMap<String, ChannelPanelPlugin>();
    private Map<String, DashboardPanelPlugin> dashboardPanelPlugins = new HashMap<String, DashboardPanelPlugin>();
    private Map<String, ChannelWizardPlugin> channelWizardPlugins = new HashMap<String, ChannelWizardPlugin>();
    private Map<String, ChannelColumnPlugin> channelColumnPlugins = new HashMap<String, ChannelColumnPlugin>();
    private Map<String, DashboardColumnPlugin> dashboardColumnPlugins = new HashMap<String, DashboardColumnPlugin>();
    private Map<String, AttachmentViewer> attachmentViewerPlugins = new HashMap<String, AttachmentViewer>();
    private Map<String, FilterRulePlugin> filterRulePlugins = new HashMap<String, FilterRulePlugin>();
    private Map<String, TransformerStepPlugin> transformerStepPlugins = new HashMap<String, TransformerStepPlugin>();

    private Map<String, ConnectorClass> connectors = new TreeMap<String, ConnectorClass>();
    private Map<String, ConnectorClass> sourceConnectors = new TreeMap<String, ConnectorClass>();
    private Map<String, ConnectorClass> destinationConnectors = new TreeMap<String, ConnectorClass>();

    private static LoadedExtensions instance = null;

    private Frame parent = PlatformUI.MIRTH_FRAME;

    private LoadedExtensions() {
        // private
    }

    public static LoadedExtensions getInstance() {
        synchronized (LoadedExtensions.class) {
            if (instance == null) {
                instance = new LoadedExtensions();
            }

            return instance;
        }
    }

    public void initialize() {
        // Remove all existing extensions from the maps in case they are being
        // initialized again
        clearExtensionMaps();

        for (PluginMetaData metaData : parent.getPluginMetaData().values()) {
            try {
                if (metaData.isEnabled()) {
                    for (String clazzName : metaData.getClientClasses()) {
                            Class<?> clazz = Class.forName(clazzName);
                            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                            
                            for (int i = 0; i < constructors.length; i++) {
                                Class<?> parameters[];
                                parameters = constructors[i].getParameterTypes();
                                // load plugin if the number of parameters
                                // in the constructor is 1.
                                if (parameters.length == 1) {
                                    ClientPlugin clientPlugin = (ClientPlugin) constructors[i].newInstance(new Object[] { metaData.getName() });
                                    addPlugin(clientPlugin);
                                    i = constructors.length;
                                }
                            }
                    }
                }
            } catch (Exception e) {
                parent.alertException(parent, e.getStackTrace(), e.getMessage());
            }
        }

        for (ConnectorMetaData metaData : parent.getConnectorMetaData().values()) {
            if (metaData.isEnabled()) {
                try {
                    String connectorName = metaData.getName();
                    ConnectorClass connectorClass = (ConnectorClass) Class.forName(metaData.getClientClassName()).newInstance();

                    if (metaData.getType() == ConnectorMetaData.Type.SOURCE) {
                        connectors.put(connectorName, connectorClass);
                        sourceConnectors.put(connectorName, connectorClass);
                    } else if (metaData.getType() == ConnectorMetaData.Type.DESTINATION) {
                        connectors.put(connectorName, connectorClass);
                        destinationConnectors.put(connectorName, connectorClass);
                    } else {
                        // type must be SOURCE or DESTINATION
                        throw new Exception();
                    }
                } catch (Exception e) {
                    parent.alertError(this.parent, "Could not load connector class: " + metaData.getClientClassName());
                }
            }
        }
    }

    public void startPlugins() {
        for (ClientPlugin clientPlugin : clientPlugins.values()) {
            clientPlugin.start();
        }
    }

    public void stopPlugins() {
        for (ClientPlugin clientPlugin : clientPlugins.values()) {
            clientPlugin.stop();
        }
    }

    public void resetPlugins() {
        for (ClientPlugin clientPlugin : clientPlugins.values()) {
            clientPlugin.reset();
        }
    }

    private void addPlugin(ClientPlugin plugin) {
        clientPlugins.put(plugin.getName(), plugin);

        if (plugin instanceof SettingsPanelPlugin) {
            settingsPanelPlugins.put(plugin.getName(), (SettingsPanelPlugin) plugin);
        }

        if (plugin instanceof DashboardPanelPlugin) {
            dashboardPanelPlugins.put(plugin.getName(), (DashboardPanelPlugin) plugin);
        }

        if (plugin instanceof ChannelPanelPlugin) {
            channelPanelPlugins.put(plugin.getName(), (ChannelPanelPlugin) plugin);
        }

        if (plugin instanceof ChannelWizardPlugin) {
            channelWizardPlugins.put(plugin.getName(), (ChannelWizardPlugin) plugin);
        }

        if (plugin instanceof DashboardColumnPlugin) {
            dashboardColumnPlugins.put(plugin.getName(), (DashboardColumnPlugin) plugin);
        }

        if (plugin instanceof ChannelColumnPlugin) {
            channelColumnPlugins.put(plugin.getName(), (ChannelColumnPlugin) plugin);
        }

        if (plugin instanceof AttachmentViewer) {
            attachmentViewerPlugins.put(plugin.getName(), (AttachmentViewer) plugin);
        }

        if (plugin instanceof FilterRulePlugin) {
            filterRulePlugins.put(plugin.getName(), (FilterRulePlugin) plugin);
        }

        if (plugin instanceof TransformerStepPlugin) {
            transformerStepPlugins.put(plugin.getName(), (TransformerStepPlugin) plugin);
        }
    }

    private void clearExtensionMaps() {
        clientPlugins.clear();

        settingsPanelPlugins.clear();
        dashboardPanelPlugins.clear();
        channelPanelPlugins.clear();
        channelWizardPlugins.clear();
        dashboardColumnPlugins.clear();
        channelColumnPlugins.clear();
        attachmentViewerPlugins.clear();
        filterRulePlugins.clear();
        transformerStepPlugins.clear();

        connectors.clear();
        sourceConnectors.clear();
        destinationConnectors.clear();
    }

    public Map<String, ClientPlugin> getClientPlugins() {
        return clientPlugins;
    }

    public Map<String, SettingsPanelPlugin> getSettingsPanelPlugins() {
        return settingsPanelPlugins;
    }

    public Map<String, DashboardPanelPlugin> getDashboardPanelPlugins() {
        return dashboardPanelPlugins;
    }

    public Map<String, ChannelPanelPlugin> getChannelPanelPlugins() {
        return channelPanelPlugins;
    }

    public Map<String, ChannelWizardPlugin> getChannelWizardPlugins() {
        return channelWizardPlugins;
    }

    public Map<String, DashboardColumnPlugin> getDashboardColumnPlugins() {
        return dashboardColumnPlugins;
    }

    public Map<String, ChannelColumnPlugin> getChannelColumnPlugins() {
        return channelColumnPlugins;
    }

    public Map<String, AttachmentViewer> getAttachmentViewerPlugins() {
        return attachmentViewerPlugins;
    }

    public Map<String, FilterRulePlugin> getFilterRulePlugins() {
        return filterRulePlugins;
    }

    public Map<String, TransformerStepPlugin> getTransformerStepPlugins() {
        return transformerStepPlugins;
    }

    public Map<String, ConnectorClass> getConnectors() {
        return connectors;
    }

    public Map<String, ConnectorClass> getSourceConnectors() {
        return sourceConnectors;
    }

    public Map<String, ConnectorClass> getDestinationConnectors() {
        return destinationConnectors;
    }
}
