/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirth.connect.client.core.ConnectServiceUtil;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.User;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.purged.PurgedDocument;
import com.mirth.connect.plugins.ServerPlugin;
import com.mirth.connect.server.ExtensionLoader;

public class DefaultUsageController extends UsageController {
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private Map<String, Object> lastClientStats = new HashMap<String, Object>();

    private static UsageController instance = null;

    private DefaultUsageController() {}

    public static UsageController create() {
        synchronized (DefaultUsageController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(UsageController.class);

                if (instance == null) {
                    instance = new DefaultUsageController();
                }
            }

            return instance;
        }
    }

    private boolean canSendStats() throws ControllerException {
        long now = System.currentTimeMillis();
        UpdateSettings updateSettings = configurationController.getUpdateSettings();

        if (updateSettings.getStatsEnabled()) {
            Long lastUpdate = updateSettings.getLastStatsTime();
            if (lastUpdate == null || (now - lastUpdate) > ConnectServiceUtil.MILLIS_PER_DAY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates purged data of this server instance and serializes into JSON.
     */
    @Override
    public String createUsageStats(Map<String, Object> clientStats) {
        PurgedDocument purgedDocument = new PurgedDocument();
        String usageData = null;

        if (clientStats != null) {
            lastClientStats = clientStats;
        }

        try {
            if (canSendStats()) {
                getClientSpecs(purgedDocument);
                getServerSpecs(purgedDocument);
                getConfigurationData(purgedDocument);
                getChannelData(purgedDocument);
                getExtensionData(purgedDocument);
                getCodeTemplateData(purgedDocument);
                getScriptData(purgedDocument);
                getAlertData(purgedDocument);
                getUserData(purgedDocument);

                // Convert to JSON
                ObjectMapper mapper = new ObjectMapper();
                mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
                usageData = mapper.writeValueAsString(purgedDocument);
            }
        } catch (Throwable t) {
            // If an exception occurs, we want to fail silently.
        }
        return usageData;
    }

    private void getConfigurationData(PurgedDocument purgedDocument) throws ControllerException {
        purgedDocument.setServerId(configurationController.getServerId());
        purgedDocument.setMirthVersion(configurationController.getServerVersion());
        purgedDocument.setUpdateSettings(configurationController.getUpdateSettings().getPurgedProperties());
        purgedDocument.setServerSettings(configurationController.getServerSettings().getPurgedProperties());
        purgedDocument.setDatabaseType(configurationController.getDatabaseType());
    }

    private void getDatabaseVersion() {
        //TODO: Get database version.
    }

    private void getClientSpecs(PurgedDocument purgedDocument) {
        // The client specs are cached from the most recent user login.
        // If no one has logged in since the last server restart, clientStats will be empty.
        purgedDocument.setClientSpecs(lastClientStats);
    }

    private void getServerSpecs(PurgedDocument purgedDocument) {
        Map<String, Object> serverSpecs = new HashMap<String, Object>();
        serverSpecs.put("osName", System.getProperty("os.name"));
        serverSpecs.put("osVersion", System.getProperty("os.version"));
        serverSpecs.put("javaVersion", System.getProperty("java.version"));
        serverSpecs.put("maxMemory", Runtime.getRuntime().maxMemory());
        serverSpecs.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        purgedDocument.setServerSpecs(serverSpecs);
    }

    private void getChannelData(PurgedDocument purgedDocument) throws ControllerException {
        ChannelController channelController = ControllerFactory.getFactory().createChannelController();
        List<Map<String, Object>> purgedChannels = new ArrayList<Map<String, Object>>();
        int invalidChannels = 0;
        for (Channel channel : channelController.getChannels(null)) {
            if (!(channel instanceof InvalidChannel)) {
                purgedChannels.add(channel.getPurgedProperties());
            } else {
                invalidChannels++;
            }
        }
        purgedDocument.setChannels(purgedChannels);
        purgedDocument.setInvalidChannels(invalidChannels);
    }

    private void getCodeTemplateData(PurgedDocument purgedDocument) throws ControllerException {
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
        List<Map<String, Object>> purgedLibraries = new ArrayList<Map<String, Object>>();

        for (CodeTemplateLibrary library : codeTemplateController.getLibraries(null, true)) {
            purgedLibraries.add(library.getPurgedProperties());
        }
        purgedDocument.setCodeTemplateLibraries(purgedLibraries);
    }

    private void getScriptData(PurgedDocument purgedDocument) throws ControllerException {
        ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
        Map<String, Integer> purgedGlobalScripts = new HashMap<String, Integer>();

        for (Map.Entry<String, String> entry : scriptController.getGlobalScripts().entrySet()) {
            String key = entry.getKey().toLowerCase();
            key += "Lines";
            purgedGlobalScripts.put(key, PurgeUtil.countLines(entry.getValue()));
        }
        purgedDocument.setGlobalScripts(purgedGlobalScripts);
    }

    private void getAlertData(PurgedDocument purgedDocument) throws ControllerException {
        AlertController alertController = ControllerFactory.getFactory().createAlertController();
        List<Map<String, Object>> purgedAlerts = new ArrayList<Map<String, Object>>();
        List<AlertModel> alerts = alertController.getAlerts();

        for (AlertModel alert : alerts) {
            purgedAlerts.add(alert.getPurgedProperties());
        }
        purgedDocument.setAlerts(purgedAlerts);
    }

    private void getUserData(PurgedDocument purgedDocument) throws ControllerException {
        UserController userController = ControllerFactory.getFactory().createUserController();
        List<User> users = userController.getAllUsers();
        purgedDocument.setUsers(users.size());
    }

    private void getExtensionData(PurgedDocument purgedDocument) throws ControllerException {
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        List<Map<String, Object>> purgedPlugins = new ArrayList<Map<String, Object>>();

        for (ServerPlugin plugin : extensionController.getServerPlugins()) {
            Map<String, Object> pluginProperties = new HashMap<String, Object>();
            pluginProperties.put("pluginPoint", plugin.getPluginPointName());
            if (plugin instanceof Purgable) {
                pluginProperties.put("properties", ((Purgable) plugin).getPurgedProperties());
            }
            purgedPlugins.add(pluginProperties);
        }
        purgedDocument.setPlugins(purgedPlugins);

        List<Map<String, Object>> purgedPluginMetaData = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, PluginMetaData> entry : extensionController.getPluginMetaData().entrySet()) {
            Map<String, Object> pluginMetaData = new HashMap<String, Object>();
            pluginMetaData.put("name", entry.getKey());
            if (entry.getValue() instanceof Purgable) {
                pluginMetaData.putAll(((Purgable) entry.getValue()).getPurgedProperties());
                purgedPluginMetaData.add(pluginMetaData);
            }
        }
        purgedDocument.setPluginMetaData(purgedPluginMetaData);

        List<Map<String, Object>> purgedConnectorMetaData = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, ConnectorMetaData> entry : extensionController.getConnectorMetaData().entrySet()) {
            Map<String, Object> connectorMetaData = new HashMap<String, Object>();
            connectorMetaData.put("name", entry.getKey());
            if (entry.getValue() instanceof Purgable) {
                connectorMetaData.putAll(((Purgable) entry.getValue()).getPurgedProperties());
                purgedConnectorMetaData.add(connectorMetaData);
            }
        }
        purgedDocument.setConnectorMetaData(purgedConnectorMetaData);
    }
}
