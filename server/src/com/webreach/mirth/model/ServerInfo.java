package com.webreach.mirth.model;

import java.util.Map;

public class ServerInfo {
    private Map<String, String> usageData;
    private Map<String, String> components;
    private String serverId;

    public Map<String, String> getComponents() {
        return components;
    }

    public void setComponents(Map<String, String> components) {
        this.components = components;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Map<String, String> getUsageData() {
        return usageData;
    }

    public void setUsageData(Map<String, String> usageData) {
        this.usageData = usageData;
    }
}