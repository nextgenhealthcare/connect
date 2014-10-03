/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.purged;

import java.util.List;
import java.util.Map;

public class PurgedDocument {
    private String mirthVersion;
    private String serverId;
    private String databaseType;
    private Map<String, Object> serverSpecs;
    private Map<String, Integer> globalScripts;
    private List<Map<String, Object>> channels;
    private int invalidChannels;
    private List<Map<String, Object>> codeTemplates;
    private List<Map<String, Object>> alerts;
    private List<Map<String, Object>> plugins;
    private List<Map<String, Object>> pluginMetaData;
    private List<Map<String, Object>> connectorMetaData;
    private Map<String, Object> serverSettings;
    private Map<String, Object> updateSettings;
    private int users;

    public int getInvalidChannels() {
        return invalidChannels;
    }

    public void setInvalidChannels(int invalidChannels) {
        this.invalidChannels = invalidChannels;
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(int users) {
        this.users = users;
    }

    public Map<String, Object> getUpdateSettings() {
        return updateSettings;
    }

    public void setUpdateSettings(Map<String, Object> updateSettings) {
        this.updateSettings = updateSettings;
    }

    public Map<String, Object> getServerSettings() {
        return serverSettings;
    }

    public void setServerSettings(Map<String, Object> serverSettings) {
        this.serverSettings = serverSettings;
    }

    public List<Map<String, Object>> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Map<String, Object>> alerts) {
        this.alerts = alerts;
    }

    public Map<String, Integer> getGlobalScripts() {
        return globalScripts;
    }

    public void setGlobalScripts(Map<String, Integer> globalScripts) {
        this.globalScripts = globalScripts;
    }

    public List<Map<String, Object>> getPluginMetaData() {
        return pluginMetaData;
    }

    public void setPluginMetaData(List<Map<String, Object>> pluginMetaData) {
        this.pluginMetaData = pluginMetaData;
    }

    public String getMirthVersion() {
        return mirthVersion;
    }

    public void setMirthVersion(String mirthVersion) {
        this.mirthVersion = mirthVersion;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public List<Map<String, Object>> getChannels() {
        return channels;
    }

    public void setChannels(List<Map<String, Object>> channels) {
        this.channels = channels;
    }

    public List<Map<String, Object>> getCodeTemplates() {
        return codeTemplates;
    }

    public void setCodeTemplates(List<Map<String, Object>> codeTemplates) {
        this.codeTemplates = codeTemplates;
    }

    public List<Map<String, Object>> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<Map<String, Object>> plugins) {
        this.plugins = plugins;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public Map<String, Object> getServerSpecs() {
        return serverSpecs;
    }

    public void setServerSpecs(Map<String, Object> serverSpecs) {
        this.serverSpecs = serverSpecs;
    }

    public List<Map<String, Object>> getConnectorMetaData() {
        return connectorMetaData;
    }

    public void setConnectorMetaData(List<Map<String, Object>> connectorMetaData) {
        this.connectorMetaData = connectorMetaData;
    }
}
