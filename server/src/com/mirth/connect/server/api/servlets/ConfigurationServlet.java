/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.ConfigurationServletInterface;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ResourcePropertiesList;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.api.DontCheckAuthorized;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.util.ConfigurationProperty;
import com.mirth.connect.util.ConnectionTestResponse;
import com.mirth.connect.util.MirthSSLUtil;

public class ConfigurationServlet extends MirthServlet implements ConfigurationServletInterface {

    private static final Logger logger = Logger.getLogger(ConfigurationServlet.class);
    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private static final ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private static final ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private static final ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private static final ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

    public ConfigurationServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, false);
    }

    @Override
    public String getServerId() {
        return configurationController.getServerId();
    }

    @Override
    @DontCheckAuthorized
    public String getVersion() {
        // Authorization not required
        return configurationController.getServerVersion();
    }

    @Override
    public String getBuildDate() {
        return configurationController.getBuildDate();
    }

    @Override
    @DontCheckAuthorized
    public int getStatus() {
        // Authorization not required
        return configurationController.getStatus();
    }

    @Override
    public String getServerTimezone() {
        return configurationController.getServerTimezone(request.getLocale());
    }

    @Override
    public Calendar getServerTime() {
        return configurationController.getServerTime();
    }

    @Override
    public String getJVMName() {
        return System.getProperty("java.vm.name");
    }

    @Override
    public Map<String, Object> getAbout() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", configurationController.getServerName());
        properties.put("version", configurationController.getServerVersion());
        properties.put("date", configurationController.getBuildDate());
        properties.put("database", configurationController.getDatabaseType());

        properties.put("channelCount", channelController.getChannelIds().size());

        Map<String, String> plugins = new HashMap<String, String>();

        for (MetaData plugin : extensionController.getPluginMetaData().values()) {
            plugins.put(plugin.getName(), plugin.getPluginVersion());
        }

        Map<String, String> connectors = new HashMap<String, String>();

        for (MetaData connector : extensionController.getConnectorMetaData().values()) {
            connectors.put(connector.getName(), connector.getPluginVersion());
        }

        properties.put("plugins", plugins);
        properties.put("connectors", connectors);

        return properties;
    }

    @Override
    public ServerConfiguration getServerConfiguration(DeployedState initialState, boolean pollingOnly) {
        if (initialState != null && initialState != DeployedState.STARTED && initialState != DeployedState.PAUSED && initialState != DeployedState.STOPPED) {
            throw new MirthApiException("Initial state cannot be set to " + initialState + ".");
        }

        try {
            ServerConfiguration config = configurationController.getServerConfiguration();

            if (initialState != null) {
                // Avoid messing with any in-memory objects on the server
                config = serializer.deserialize(serializer.serialize(config), ServerConfiguration.class);

                for (Channel channel : config.getChannels()) {
                    if (!pollingOnly || ArrayUtils.contains(channel.getSourceConnector().getProperties().getClass().getInterfaces(), PollConnectorPropertiesInterface.class)) {
                        channel.getProperties().setInitialState(initialState);
                    }
                }
            }

            return config;
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void setServerConfiguration(ServerConfiguration serverConfiguration, boolean deploy) {
        try {
            configurationController.setServerConfiguration(serverConfiguration, deploy);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<String> getAvailableCharsetEncodings() {
        try {
            return configurationController.getAvailableCharsetEncodings();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public ServerSettings getServerSettings() {
        try {
            return configurationController.getServerSettings();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void setServerSettings(ServerSettings settings) {
        try {
            configurationController.setServerSettings(settings);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public EncryptionSettings getEncryptionSettings() {
        try {
            return configurationController.getEncryptionSettings();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public ConnectionTestResponse sendTestEmail(Properties properties) {
        try {
            return configurationController.sendTestEmail(properties);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public UpdateSettings getUpdateSettings() {
        try {
            return configurationController.getUpdateSettings();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void setUpdateSettings(UpdateSettings settings) {
        try {
            configurationController.setUpdateSettings(settings);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public String getGuid() {
        return configurationController.generateGuid();
    }

    @Override
    public Map<String, String> getGlobalScripts() {
        try {
            return scriptController.getGlobalScripts();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void setGlobalScripts(Map<String, String> scripts) {
        try {
            scriptController.setGlobalScripts(scripts);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public Map<String, ConfigurationProperty> getConfigurationMap() {
        try {
            return configurationController.getConfigurationProperties();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void setConfigurationMap(Map<String, ConfigurationProperty> map) {
        try {
            configurationController.setConfigurationProperties(map, true);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<DriverInfo> getDatabaseDrivers() {
        try {
            return configurationController.getDatabaseDrivers();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public PasswordRequirements getPasswordRequirements() {
        return configurationController.getPasswordRequirements();
    }

    @Override
    public List<ResourceProperties> getResources() {
        return ObjectXMLSerializer.getInstance().deserialize(configurationController.getResources(), ResourcePropertiesList.class).getList();
    }

    @Override
    public void setResources(List<ResourceProperties> resources) {
        final List<LibraryProperties> libraryResources = new ArrayList<LibraryProperties>();
        for (ResourceProperties resource : resources) {
            if (resource instanceof LibraryProperties) {
                libraryResources.add((LibraryProperties) resource);
            }
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    contextFactoryController.updateResources(libraryResources, false);
                } catch (Exception e) {
                    logger.error("Unable to update libraries: " + e.getMessage(), e);
                }
            }
        });
        configurationController.setResources(ObjectXMLSerializer.getInstance().serialize(new ResourcePropertiesList(resources)));
    }

    @Override
    public void reloadResource(String resourceId) {
        try {
            contextFactoryController.reloadResource(resourceId);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public Set<ChannelTag> getChannelTags() throws ClientException {
        return configurationController.getChannelTags();
    }

    @Override
    public void setChannelTags(Set<ChannelTag> channelTags) throws ClientException {
        configurationController.setChannelTags(channelTags);
    }

    @Override
    public Set<ChannelDependency> getChannelDependencies() {
        return configurationController.getChannelDependencies();
    }

    @Override
    public void setChannelDependencies(Set<ChannelDependency> dependencies) {
        configurationController.setChannelDependencies(dependencies);
    }

    @Override
    public Map<String, ChannelMetadata> getChannelMetadata() {
        return configurationController.getChannelMetadata();
    }

    @Override
    public void setChannelMetadata(Map<String, ChannelMetadata> metadata) {
        configurationController.setChannelMetadata(metadata);
    }

    @Override
    public Map<String, String[]> getProtocolsAndCipherSuites() {
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put(MirthSSLUtil.KEY_SUPPORTED_PROTOCOLS, MirthSSLUtil.getSupportedHttpsProtocols());
        map.put(MirthSSLUtil.KEY_SUPPORTED_CIPHER_SUITES, MirthSSLUtil.getSupportedHttpsCipherSuites());
        map.put(MirthSSLUtil.KEY_ENABLED_CLIENT_PROTOCOLS, MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsClientProtocols()));
        map.put(MirthSSLUtil.KEY_ENABLED_SERVER_PROTOCOLS, MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsServerProtocols()));
        map.put(MirthSSLUtil.KEY_ENABLED_CIPHER_SUITES, MirthSSLUtil.getEnabledHttpsCipherSuites(configurationController.getHttpsCipherSuites()));
        return map;
    }
}