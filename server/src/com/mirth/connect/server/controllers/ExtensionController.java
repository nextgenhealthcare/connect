/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.fileupload.FileItem;

import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.ExtensionPoint;
import com.mirth.connect.model.ExtensionPointDefinition;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.plugins.ServerPlugin;
import com.mirth.connect.server.tools.ClassPathResource;

public abstract class ExtensionController extends Controller {

    public enum ExtensionType {
        PLUGIN("plugin.xml"), SOURCE("source.xml"), DESTINATION(
                "destination.xml"), CONNECTOR(
                new ExtensionType[] { SOURCE, DESTINATION });

        private String fileName = null;
        private ExtensionType[] types = null;

        ExtensionType(String fileName) {
            this.fileName = fileName;
        }

        ExtensionType(ExtensionType[] types) {
            this.types = types;
        }

        public String[] getFileNames() {
            if (types == null) {
                return new String[] { fileName };
            } else {
                String[] fileNames = new String[types.length];

                for (int i = 0; i < fileNames.length; i++) {
                    fileNames[i] = types[i].getFileName();
                }

                return fileNames;
            }
        }

        private String getFileName() {
            return fileName;
        }
    }

    /**
     * If in an IDE, extensions will be on the classpath as a resource. If
     * that's the case, use that directory. Otherwise, use the mirth home
     * directory and append extensions.
     * 
     * @return
     */
    public static String getExtensionsPath() {
        if (ClassPathResource.getResourceURI("extensions") != null) {
            return ClassPathResource.getResourceURI("extensions").getPath() + File.separator;
        } else {
            return ControllerFactory.getFactory().createConfigurationController().getBaseDir() + File.separator + "extensions" + File.separator;
        }
    }

    public static final String EXTENSIONS_UNINSTALL_FILE = "uninstall";

    public static final String EXTENSIONS_UNINSTALL_SCRIPTS_FILE = "uninstallScripts";

    public static ExtensionController getInstance() {
        return ControllerFactory.getFactory().createExtensionController();
    }

    // Extension point for ExtensionPoint.Type.SERVER_PLUGIN
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.SERVER, type = ExtensionPoint.Type.SERVER_PLUGIN)
    public abstract void initPlugins();

    /**
     * Returns true if the extension (either plugin or connector) with the
     * specified name is enabled, false otherwise.
     * 
     * @param name
     * @return
     */
    public abstract boolean isExtensionEnabled(String name);

    /**
     * Loads the metadata for all extensions (plugins and connectors) from the
     * filesystem.
     */
    public abstract void loadExtensions();

    /**
     * Invokes the start method on all loaded server plugins.
     */
    public abstract void startPlugins();

    /**
     * Invoked the stop method on all loaded server plugins.
     */
    public abstract void stopPlugins();

    /**
     * Invokes the onDeploy method on all loaded server plugins.
     */
    public abstract void triggerDeploy();

    /**
     * Returns a map of all loaded server plugins ekeyed by plugin name.
     */
    public abstract Map<String, ServerPlugin> getLoadedServerPlugins();

    /**
     * Returns a list of paths to extension libraries needed to WebStart the
     * administrator.
     */
    public abstract List<String> getClientExtensionLibraries();

    // plugins

    /**
     * Returns the metadata for all plugins.
     */
    public abstract Map<String, PluginMetaData> getPluginMetaData();

    /**
     * Stores all plugin metadata to the database.
     * 
     * @param metaData
     * @throws ControllerException
     */
    public abstract void savePluginMetaData(Map<String, PluginMetaData> metaData) throws ControllerException;

    // the following two should be combined

    /**
     * Updates the properties for the specified server plugin. Note that this
     * calls the update on the plugin itself and does not store the properties
     * in the database.
     */
    public abstract void updatePluginProperties(String name, Properties properties);

    /**
     * Stores the properties for the specified plugin in the database. Removes
     * any properties beforehand.
     * 
     * @param name
     * @param properties
     * @throws ControllerException
     */
    public abstract void setPluginProperties(String name, Properties properties) throws ControllerException;

    /**
     * Returns properties for the specified plugin from the database.
     * 
     * @param name
     * @return
     * @throws ControllerException
     */
    public abstract Properties getPluginProperties(String name) throws ControllerException;

    /**
     * Invokes a server plugin service.
     * 
     * @param name
     *            the name of the plugin
     * @param method
     *            the signature of the method to invoke
     * @param object
     *            parameters for the method (for example, a Map for multiple
     *            parameters)
     * @param sessionId
     *            the user's session ID
     * @return the result of invoking the plugin service
     * @throws Exception
     */
    public abstract Object invokePluginService(String name, String method, Object object, String sessionId) throws Exception;

    // connectors

    public abstract Map<String, ConnectorMetaData> getConnectorMetaData();

    /**
     * Stores all connector metadata to the database.
     * 
     * @param metaData
     * @throws ControllerException
     */
    public abstract void saveConnectorMetaData(Map<String, ConnectorMetaData> metaData) throws ControllerException;

    public abstract ConnectorMetaData getConnectorMetaDataByProtocol(String protocol);

    public abstract ConnectorMetaData getConnectorMetaDataByTransportName(String transportName);

    /**
     * Invokes a connector service.
     * 
     * @param name
     *            the name of the connector
     * @param method
     *            the signature of the method to invoke
     * @param object
     *            parameters for the method (for example, a Map for multiple
     *            parameters)
     * @param sessionId
     *            the user's session ID
     * @return the result of invoking the connector service
     * @throws Exception
     */
    public abstract Object invokeConnectorService(String name, String method, Object object, String sessionId) throws Exception;

    // installation and unistallation

    /**
     * Extracts the contents of the uploaded zip file into the installation temp
     * directory to be picked up by MirthLauncher on next restart.
     */
    public abstract void extractExtension(FileItem fileItem) throws ControllerException;

    /**
     * Adds the extension's SQL uninstall script to the server uninstallation
     * script. Also adds the extenion path to the uninstallation list that is
     * read on startup.
     * 
     * @param extensionPath
     * @throws ControllerException
     */
    public abstract void prepareExtensionForUninstallation(String extensionPath) throws ControllerException;

    /**
     * Executes the server uninstallation SQL script.
     */
    public abstract void uninstallExtensions();

    // metadata

    // public abstract void enableExtension(String name);

    // public abstract void disableExtension(String name);
}
