/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.directoryresource.DirectoryResourceProperties;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("serverConfiguration")
public class ServerConfiguration implements Serializable, Migratable {
    private String date;
    private List<Channel> channels = null;
    private List<User> users = null;
    private List<AlertModel> alerts = null;
    private List<CodeTemplateLibrary> codeTemplateLibraries = null;
    private ServerSettings serverSettings = null;
    private UpdateSettings updateSettings = null;
    private Map<String, String> globalScripts = null;
    private Map<String, Properties> pluginProperties = null;
    private ResourcePropertiesList resourceProperties = null;

    public List<AlertModel> getAlerts() {
        return this.alerts;
    }

    public void setAlerts(List<AlertModel> alerts) {
        this.alerts = alerts;
    }

    public List<Channel> getChannels() {
        return this.channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public ServerSettings getServerSettings() {
        return serverSettings;
    }

    public void setServerSettings(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    public UpdateSettings getUpdateSettings() {
        return updateSettings;
    }

    public void setUpdateSettings(UpdateSettings updateSettings) {
        this.updateSettings = updateSettings;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CodeTemplateLibrary> getCodeTemplateLibraries() {
        return codeTemplateLibraries;
    }

    public void setCodeTemplateLibraries(List<CodeTemplateLibrary> codeTemplateLibraries) {
        this.codeTemplateLibraries = codeTemplateLibraries;
    }

    public Map<String, String> getGlobalScripts() {
        return globalScripts;
    }

    public void setGlobalScripts(Map<String, String> globalScripts) {
        this.globalScripts = globalScripts;
    }

    public Map<String, Properties> getPluginProperties() {
        return pluginProperties;
    }

    public void setPluginProperties(Map<String, Properties> pluginProperties) {
        this.pluginProperties = pluginProperties;
    }

    public ResourcePropertiesList getResourceProperties() {
        return resourceProperties;
    }

    public void setResourceProperties(ResourcePropertiesList resourceProperties) {
        this.resourceProperties = resourceProperties;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        DonkeyElement globalScripts = element.getChildElement("globalScripts");

        if (globalScripts != null) {
            for (DonkeyElement entry : globalScripts.getChildElements()) {
                DonkeyElement keyString = entry.getChildElement("string");
                if (keyString.getTextContent().equals("Shutdown")) {
                    keyString.setTextContent("Undeploy");
                }
            }
        }
    }

    @Override
    public void migrate3_2_0(DonkeyElement element) {
        ResourcePropertiesList list = new ResourcePropertiesList();
        DirectoryResourceProperties defaultResource = new DirectoryResourceProperties();
        defaultResource.setId(ResourceProperties.DEFAULT_RESOURCE_ID);
        defaultResource.setName(ResourceProperties.DEFAULT_RESOURCE_NAME);
        defaultResource.setDescription("Loads libraries from the custom-lib folder in the Mirth Connect home directory.");
        defaultResource.setIncludeWithGlobalScripts(true);
        defaultResource.setDirectory("custom-lib");
        list.getList().add(defaultResource);

        try {
            DonkeyElement resourcePropertiesElement = element.addChildElementFromXml(ObjectXMLSerializer.getInstance().serialize(list));
            resourcePropertiesElement.setNodeName("resourceProperties");
        } catch (DonkeyElementException e) {
            throw new SerializerException("Failed to migrate server configuration.", e);
        }
    }

    @Override
    public void migrate3_3_0(DonkeyElement element) {
        DonkeyElement librariesElement = element.addChildElement("codeTemplateLibraries");
        DonkeyElement libraryElement = librariesElement.addChildElement("codeTemplateLibrary");
        libraryElement.setAttribute("version", "3.3.0");
        libraryElement.addChildElement("id", UUID.randomUUID().toString());
        libraryElement.addChildElement("name", "Library 1");
        libraryElement.addChildElement("revision", "1");
        try {
            libraryElement.addChildElementFromXml(ObjectXMLSerializer.getInstance().serialize(Calendar.getInstance())).setNodeName("lastModified");
        } catch (DonkeyElementException e) {
            throw new SerializerException("Failed to migrate code template library last modified date.", e);
        }
        libraryElement.addChildElement("description", "This library was added upon migration to version 3.3.0. It includes all pre-existing\ncode templates, and is set to be included on all pre-existing and new channels.\n\nYou should create your own new libraries and assign code templates to them as you\nsee fit. You should also link libraries to specific channels, so that you're not\nnecessarily including all code templates on all channels all the time.");
        libraryElement.addChildElement("includeNewChannels", "true");
        libraryElement.addChildElement("enabledChannelIds");
        libraryElement.addChildElement("disabledChannelIds");
        try {
            libraryElement.addChildElementFromXml(element.removeChild("codeTemplates").toXml());
        } catch (DonkeyElementException e) {
            throw new SerializerException("Failed to migrate code templates.", e);
        }
    }
}
