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

import org.apache.commons.lang.StringUtils;

import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.ExtensionPoint;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.plugins.AttachmentViewer;
import com.mirth.connect.plugins.ChannelWizardPlugin;
import com.mirth.connect.plugins.ClientPanelPlugin;
import com.mirth.connect.plugins.ClientPlugin;
import com.mirth.connect.plugins.DashboardColumnPlugin;
import com.mirth.connect.plugins.DashboardPanelPlugin;
import com.mirth.connect.plugins.FilterRulePlugin;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class LoadedExtensions {
    private Map<String, ClientPlugin> clientPlugins = new HashMap<String, ClientPlugin>();

    private Map<String, ClientPanelPlugin> clientPanelPlugins = new HashMap<String, ClientPanelPlugin>();
    private Map<String, DashboardPanelPlugin> dashboardPanelPlugins = new HashMap<String, DashboardPanelPlugin>();
    private Map<String, ChannelWizardPlugin> channelWizardPlugins = new HashMap<String, ChannelWizardPlugin>();
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
        for (PluginMetaData metaData : parent.getPluginMetaData().values()) {
            try {
                if (metaData.isEnabled()) {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
                        if (extensionPoint.getMode() == ExtensionPoint.Mode.CLIENT && StringUtils.isNotBlank(extensionPoint.getClassName())) {
                            String pluginName = extensionPoint.getName();

                            /*
                             * Load all client plugins besides client vocabulary
                             * plugins, since those are loaded in the model by
                             * MessageVocabularyFactory
                             */
                            if (extensionPoint.getType() != ExtensionPoint.Type.CLIENT_VOCABULARY) {
                                Class<?> clazz = Class.forName(extensionPoint.getClassName());
                                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                                for (int i = 0; i < constructors.length; i++) {
                                    Class<?> parameters[];
                                    parameters = constructors[i].getParameterTypes();
                                    // load plugin if the number of parameters
                                    // in the constructor is 1.
                                    if (parameters.length == 1) {
                                        ClientPlugin clientPlugin = (ClientPlugin) constructors[i].newInstance(new Object[] { pluginName });
                                        addPlugin(clientPlugin);
                                        i = constructors.length;
                                    }
                                }
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

    private void addPlugin(ClientPlugin plugin) throws Exception {
        clientPlugins.put(plugin.getName(), plugin);

        if (plugin instanceof ClientPanelPlugin) {
            clientPanelPlugins.put(plugin.getName(), (ClientPanelPlugin) plugin);
        } else if (plugin instanceof DashboardPanelPlugin) {
            dashboardPanelPlugins.put(plugin.getName(), (DashboardPanelPlugin) plugin);
        } else if (plugin instanceof ChannelWizardPlugin) {
            channelWizardPlugins.put(plugin.getName(), (ChannelWizardPlugin) plugin);
        } else if (plugin instanceof DashboardColumnPlugin) {
            dashboardColumnPlugins.put(plugin.getName(), (DashboardColumnPlugin) plugin);
        } else if (plugin instanceof AttachmentViewer) {
            attachmentViewerPlugins.put(plugin.getName(), (AttachmentViewer) plugin);
        } else if (plugin instanceof FilterRulePlugin) {
            filterRulePlugins.put(plugin.getName(), (FilterRulePlugin) plugin);
        } else if (plugin instanceof TransformerStepPlugin) {
            transformerStepPlugins.put(plugin.getName(), (TransformerStepPlugin) plugin);
        } else {
            throw new Exception("Client plugin is not a recognized plugin class.");
        }
    }

    public Map<String, ClientPlugin> getClientPlugins() {
        return clientPlugins;
    }

    public Map<String, ClientPanelPlugin> getClientPanelPlugins() {
        return clientPanelPlugins;
    }

    public Map<String, DashboardPanelPlugin> getDashboardPanelPlugins() {
        return dashboardPanelPlugins;
    }

    public Map<String, ChannelWizardPlugin> getChannelWizardPlugins() {
        return channelWizardPlugins;
    }

    public Map<String, DashboardColumnPlugin> getDashboardColumnPlugins() {
        return dashboardColumnPlugins;
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
