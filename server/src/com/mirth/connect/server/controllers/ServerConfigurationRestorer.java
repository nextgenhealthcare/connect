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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.MultiException;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.MergePropertiesInterface;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.util.ConfigurationProperty;

public class ServerConfigurationRestorer {

    private Logger logger = Logger.getLogger(getClass());

    private ConfigurationController configurationController;
    private ChannelController channelController;
    private AlertController alertController;
    private CodeTemplateController codeTemplateController;
    private EngineController engineController;
    private ScriptController scriptController;
    private ExtensionController extensionController;
    private ContextFactoryController contextFactoryController;

    public ServerConfigurationRestorer(ConfigurationController configurationController, ChannelController channelController, AlertController alertController, CodeTemplateController codeTemplateController, EngineController engineController, ScriptController scriptController, ExtensionController extensionController, ContextFactoryController contextFactoryController) {
        this.configurationController = configurationController;
        this.channelController = channelController;
        this.alertController = alertController;
        this.codeTemplateController = codeTemplateController;
        this.engineController = engineController;
        this.scriptController = scriptController;
        this.extensionController = extensionController;
        this.contextFactoryController = contextFactoryController;
    }

    public void restoreServerConfiguration(ServerConfiguration serverConfiguration, boolean deploy, boolean overwriteConfigMap) throws ControllerException {
        MultiException multiException = new MultiException();

        /*
         * Make sure users aren't deploying or undeploying channels while the server configuration
         * is being restored.
         */
        synchronized (engineController) {
            restoreChannelGroups(serverConfiguration, multiException);

            restoreChannels(serverConfiguration, multiException);

            restoreAlerts(serverConfiguration, multiException);

            restoreCodeTemplateLibraries(serverConfiguration, multiException);

            restoreConfigurationMap(serverConfiguration, overwriteConfigMap, multiException);

            restoreServerSettings(serverConfiguration, multiException);

            restoreUpdateSettings(serverConfiguration, multiException);

            restorePluginProperties(serverConfiguration, multiException);

            restoreResourceProperties(serverConfiguration, multiException);

            restoreChannelDependencies(serverConfiguration, multiException);

            restoreChannelTags(serverConfiguration, multiException);

            restoreGlobalScripts(serverConfiguration, multiException);

            deployAllChannels(deploy, multiException);
        }

        if (multiException.size() > 0) {
            logger.error("Error restoring server configuration.", multiException);
            throw new ControllerException("Error restoring server configuration.", multiException);
        }
    }

    void restoreChannelGroups(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            Set<ChannelGroup> channelGroups = new HashSet<ChannelGroup>();
            if (serverConfiguration.getChannelGroups() != null) {
                channelGroups.addAll(serverConfiguration.getChannelGroups());
            }
            channelController.updateChannelGroups(channelGroups, new HashSet<String>(), true);
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring channel groups.", t));
        }
    }

    void restoreChannels(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getChannels() != null) {
                MultiException subMultiException = new MultiException();

                // Undeploy all channels before updating or removing them
                undeployChannels(subMultiException);

                // Remove channels that don't exist in the new configuration
                removeChannels(serverConfiguration, subMultiException);

                // Update all channels from the server configuration
                updateChannels(serverConfiguration, subMultiException);

                subMultiException.ifExceptionThrowMulti();
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring channels.", t));
        }
    }

    void undeployChannels(MultiException multiException) {
        try {
            // Undeploy all channels before updating or removing them
            engineController.undeployChannels(engineController.getDeployedIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error undeploying before restoring channels.", t));
        }
    }

    void removeChannels(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            MultiException subMultiException = new MultiException();

            // Remove channels that don't exist in the new configuration
            for (Channel channel : channelController.getChannels(null)) {
                boolean found = false;

                for (Channel newChannel : serverConfiguration.getChannels()) {
                    if (newChannel.getId().equals(channel.getId())) {
                        found = true;
                    }
                }

                if (!found) {
                    removeChannel(channel, subMultiException);
                }
            }

            subMultiException.ifExceptionThrowMulti();
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error removing channels that no longer exist in the new server configuration.", t));
        }
    }

    void removeChannel(Channel channel, MultiException multiException) {
        try {
            channelController.removeChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error removing channel that no longer exists in the new server configuration.\nName: " + channel.getName() + "\nId: " + channel.getId(), t));
        }
    }

    void updateChannels(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            MultiException subMultiException = new MultiException();

            // Update all channels from the server configuration
            for (Channel channel : serverConfiguration.getChannels()) {
                updateChannel(channel, subMultiException);
            }

            subMultiException.ifExceptionThrowMulti();
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error updating channels from the new server configuration.", t));
        }
    }

    void updateChannel(Channel channel, MultiException multiException) {
        try {
            channelController.updateChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error updating channel from the new server configuration.\nName: " + channel.getName() + "\nId: " + channel.getId(), t));
        }
    }

    void restoreAlerts(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getAlerts() != null) {
                MultiException subMultiException = new MultiException();

                // Remove all existing alerts
                removeExistingAlerts(subMultiException);

                // Restore all alerts from the server configuration
                updateNewAlerts(serverConfiguration, subMultiException);

                subMultiException.ifExceptionThrowMulti();
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring alerts from the new server configuration.", t));
        }
    }

    void removeExistingAlerts(MultiException multiException) {
        try {
            MultiException subMultiException = new MultiException();

            // Remove all existing alerts
            for (AlertModel alert : alertController.getAlerts()) {
                removeExistingAlert(alert, subMultiException);
            }

            subMultiException.ifExceptionThrowMulti();
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error removing existing alerts before restoring from the new server configuration.", t));
        }
    }

    void removeExistingAlert(AlertModel alert, MultiException multiException) {
        try {
            alertController.removeAlert(alert.getId());
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error removing existing alert before restoring from the new server configuration.\nName: " + alert.getName() + "\nId: " + alert.getId(), t));
        }
    }

    void updateNewAlerts(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            MultiException subMultiException = new MultiException();

            // Restore all alerts from the server configuration
            for (AlertModel alert : serverConfiguration.getAlerts()) {
                updateNewAlert(alert, subMultiException);
            }

            subMultiException.ifExceptionThrowMulti();
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring new alerts from the new server configuration.", t));
        }
    }

    void updateNewAlert(AlertModel alert, MultiException multiException) {
        try {
            alertController.updateAlert(alert);
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring new alert from the new server configuration.\nName: " + alert.getName() + "\nId: " + alert.getId(), t));
        }
    }

    void restoreCodeTemplateLibraries(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getCodeTemplateLibraries() != null) {
                MultiException subMultiException = new MultiException();

                // Update all libraries from the server configuration
                updateCodeTemplateLibraries(serverConfiguration, subMultiException);

                // Remove code templates that don't exist in the new configuration
                removeCodeTemplates(serverConfiguration, subMultiException);

                // Update all code templates from the server configuration
                updateNewCodeTemplates(serverConfiguration, subMultiException);

                subMultiException.ifExceptionThrowMulti();
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring code template libraries from the new server configuration.", t));
        }
    }

    void updateCodeTemplateLibraries(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getCodeTemplateLibraries() != null) {
                // Clone the libraries because the controller may modify them
                List<CodeTemplateLibrary> clonedLibraries = new ArrayList<CodeTemplateLibrary>();
                for (CodeTemplateLibrary library : serverConfiguration.getCodeTemplateLibraries()) {
                    clonedLibraries.add(new CodeTemplateLibrary(library));
                }

                // Update all libraries from the server configuration
                codeTemplateController.updateLibraries(clonedLibraries, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error updating code template libraries from the new server configuration.", t));
        }
    }

    void removeCodeTemplates(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            MultiException subMultiException = new MultiException();

            // Remove code templates that don't exist in the new configuration
            for (CodeTemplate codeTemplate : codeTemplateController.getCodeTemplates(null)) {
                boolean found = false;

                for (CodeTemplateLibrary newLibrary : serverConfiguration.getCodeTemplateLibraries()) {
                    if (newLibrary.getCodeTemplates() != null) {
                        for (CodeTemplate newCodeTemplate : newLibrary.getCodeTemplates()) {
                            if (newCodeTemplate.getId().equals(codeTemplate.getId())) {
                                found = true;
                                break;
                            }
                        }
                    }

                    if (found) {
                        break;
                    }
                }

                if (!found) {
                    removeCodeTemplate(codeTemplate, subMultiException);
                }
            }

            subMultiException.ifExceptionThrowMulti();
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error removing code templates that no longer exist in the new server configuration.", t));
        }
    }

    void removeCodeTemplate(CodeTemplate codeTemplate, MultiException multiException) {
        try {
            codeTemplateController.removeCodeTemplate(codeTemplate.getId(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error removing code template that no longer exists in the new server configuration.\nName: " + codeTemplate.getName() + "\nId: " + codeTemplate.getId(), t));
        }
    }

    void updateNewCodeTemplates(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            MultiException subMultiException = new MultiException();

            // Update all code templates from the server configuration
            for (CodeTemplateLibrary library : serverConfiguration.getCodeTemplateLibraries()) {
                if (library.getCodeTemplates() != null) {
                    for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                        updateNewCodeTemplate(codeTemplate, subMultiException);
                    }
                }
            }

            subMultiException.ifExceptionThrowMulti();
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error updating new code templates from the new server configuration.", t));
        }
    }

    void updateNewCodeTemplate(CodeTemplate codeTemplate, MultiException multiException) {
        try {
            codeTemplateController.updateCodeTemplate(codeTemplate, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error updating new code template from the new server configuration.\nName: " + codeTemplate.getName() + "\nId: " + codeTemplate.getId(), t));
        }
    }

    void restoreConfigurationMap(ServerConfiguration serverConfiguration, boolean overwriteConfigMap, MultiException multiException) {
        try {
            if (overwriteConfigMap) {
                if (serverConfiguration.getConfigurationMap() != null) {
                    configurationController.setConfigurationProperties(serverConfiguration.getConfigurationMap(), true);
                } else {
                    configurationController.setConfigurationProperties(new HashMap<String, ConfigurationProperty>(), true);
                }
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring configuration map.", t));
        }
    }

    void restoreServerSettings(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getServerSettings() != null) {
                // The server name must not be restored.
                ServerSettings serverSettings = serverConfiguration.getServerSettings();
                serverSettings.setServerName(null);

                configurationController.setServerSettings(serverSettings);
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring server settings.", t));
        }
    }

    void restoreUpdateSettings(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getUpdateSettings() != null) {
                configurationController.setUpdateSettings(serverConfiguration.getUpdateSettings());
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring update settings.", t));
        }
    }

    void restorePluginProperties(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            /*
             * Set the properties for all plugins in the server configuration, whether or not the
             * plugin is actually installed on this server.
             */
            if (serverConfiguration.getPluginProperties() != null) {
                MultiException subMultiException = new MultiException();

                for (Entry<String, Properties> pluginEntry : serverConfiguration.getPluginProperties().entrySet()) {
                    restorePluginProperties(pluginEntry.getKey(), pluginEntry.getValue(), subMultiException);
                }

                subMultiException.ifExceptionThrowMulti();
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring plugin properties.", t));
        }
    }

    void restorePluginProperties(String pluginName, Properties properties, MultiException multiException) {
        try {
            // Allow the plugin to modify the properties first if it needs to
            ServicePlugin servicePlugin = extensionController.getServicePlugins().get(pluginName);
            if (servicePlugin instanceof MergePropertiesInterface) {
                ((MergePropertiesInterface) servicePlugin).modifyPropertiesOnRestore(properties);
            }

            extensionController.setPluginProperties(pluginName, properties);
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring properties for plugin: " + pluginName, t));
        }
    }

    void restoreResourceProperties(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getResourceProperties() != null) {
                configurationController.setResources(ObjectXMLSerializer.getInstance().serialize(serverConfiguration.getResourceProperties()));

                List<LibraryProperties> libraryResources = new ArrayList<LibraryProperties>();
                for (ResourceProperties resource : serverConfiguration.getResourceProperties().getList()) {
                    if (resource instanceof LibraryProperties) {
                        libraryResources.add((LibraryProperties) resource);
                    }
                }

                contextFactoryController.updateResources(libraryResources, false);
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring resource properties.", t));
        }
    }

    void restoreChannelDependencies(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getChannelDependencies() != null) {
                configurationController.setChannelDependencies(serverConfiguration.getChannelDependencies());
            } else {
                configurationController.setChannelDependencies(new HashSet<ChannelDependency>());
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring channel dependencies.", t));
        }
    }

    void restoreChannelTags(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getChannelTags() != null) {
                configurationController.setChannelTags(serverConfiguration.getChannelTags());
            } else {
                configurationController.setChannelTags(new HashSet<ChannelTag>());
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring channel tags.", t));
        }
    }

    void restoreGlobalScripts(ServerConfiguration serverConfiguration, MultiException multiException) {
        try {
            if (serverConfiguration.getGlobalScripts() != null) {
                scriptController.setGlobalScripts(serverConfiguration.getGlobalScripts());
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error restoring global scripts.", t));
        }
    }

    void deployAllChannels(boolean deploy, MultiException multiException) {
        try {
            // Deploy all channels
            if (deploy) {
                engineController.deployChannels(channelController.getChannelIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);
            }
        } catch (Throwable t) {
            multiException.add(new ControllerException("Error deploying channels after restoring server configuration.", t));
        }
    }

    ConfigurationController getConfigurationController() {
        return configurationController;
    }

    ChannelController getChannelController() {
        return channelController;
    }

    AlertController getAlertController() {
        return alertController;
    }

    CodeTemplateController getCodeTemplateController() {
        return codeTemplateController;
    }

    EngineController getEngineController() {
        return engineController;
    }

    ScriptController getScriptController() {
        return scriptController;
    }

    ExtensionController getExtensionController() {
        return extensionController;
    }

    ContextFactoryController getContextFactoryController() {
        return contextFactoryController;
    }
}
