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
		PLUGIN ("plugin.xml"),
		SOURCE ("source.xml"),
		DESTINATION ("destination.xml"),
		CONNECTOR (new ExtensionType[]{SOURCE, DESTINATION});
		
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
				return new String[] {fileName};
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
	
    public static final String PLUGIN_PROPERTIES_FILE = "plugin.properties";
    
    public static final String EXTENSIONS_PROPERTIES_FILE = "extensions.properties";
    
    public static final String EXTENSIONS_UNINSTALL_FILE = "uninstall";
    
    public static final String EXTENSIONS_UNINSTALL_SCRIPTS_FILE = "uninstallScripts";
    
    public static ExtensionController getInstance() {
        return ControllerFactory.getFactory().createExtensionController();
    }
    
    // Extension point for ExtensionPoint.Type.SERVER_PLUGIN
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.SERVER, type = ExtensionPoint.Type.SERVER_PLUGIN)
    public abstract void initPlugins();

    public abstract boolean isExtensionEnabled(String name);
    
    public abstract void loadExtensions();

    public abstract void startPlugins();

    public abstract void stopPlugins();
    
    public abstract Map<String, ServerPlugin> getLoadedPlugins();

    public abstract void updatePlugin(String name, Properties properties);

    public abstract void triggerDeploy();

    public abstract Object invokePluginService(String name, String method, Object object, String sessionId) throws Exception;

    public abstract Object invokeConnectorService(String name, String method, Object object, String sessionId) throws Exception;

    public abstract Map<String, ConnectorMetaData> getConnectorMetaData() throws ControllerException;

    public abstract void saveConnectorMetaData(Map<String, ConnectorMetaData> metaData) throws ControllerException;

    public abstract List<String> getClientLibraries() throws ControllerException;

    public abstract Map<String, PluginMetaData> getPluginMetaData() throws ControllerException;

    public abstract void savePluginMetaData(Map<String, PluginMetaData> metaData) throws ControllerException;

    public abstract Map<String, ConnectorMetaData> getProtocols();

    public abstract ConnectorMetaData getConnectorMetaDataByProtocol(String protocol);
    
    public abstract ConnectorMetaData getConnectorMetaDataByTransportName(String transportName);
    
    // installation and unistallation
    
    public abstract void installExtension(FileItem fileItem) throws ControllerException;

    public abstract void uninstallExtension(String packageName) throws ControllerException;
    
    public abstract void uninstallExtensions();
    
    public abstract void setUninstallScripts(List<String> uninstallScripts) throws ControllerException;
    
    public abstract List<String> getUninstallScripts() throws ControllerException;
    
    public abstract void deleteUninstallScripts();
    
    public abstract void setPluginProperties(String pluginName, Properties properties) throws ControllerException;

    public abstract Properties getPluginProperties(String pluginName) throws ControllerException;
}
