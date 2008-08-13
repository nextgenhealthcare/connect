package com.webreach.mirth.client.core;

import java.util.Map;

public class ServerInfo {
    private Map<String, String> components;
    private Map<String, String> contacts;
    private String serverId;

    public Map<String, String> getComponents() {
        return components;
    }

    public void setComponents(Map<String, String> components) {
        this.components = components;
    }

    public Map<String, String> getContacts() {
        return contacts;
    }

    public void setContacts(Map<String, String> contacts) {
        this.contacts = contacts;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

}
