/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.fileupload.FileItem;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.tools.ClassPathResource;

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
	
	// If in an IDE, extensions will be on the classpath as a resource.  If that's the case,
	// use that directory.  Otherwise, use the mirth home directory and append extensions.
	public static final String EXTENSIONS_LOCATION = ((ClassPathResource.getResourceURI("extensions") != null) ? 
			ClassPathResource.getResourceURI("extensions").getPath() : ControllerFactory.getFactory().createConfigurationController().getBaseDir() + System.getProperty("file.separator") + "extensions") 
			+ System.getProperty("file.separator");
	
    public static final String PLUGIN_PROPERTIES_FILE = "plugin.properties";
    
    public static final String EXTENSIONS_PROPERTIES_FILE = "extensions.properties";
    
    public ExtensionController getInstance() {
        return ControllerFactory.getFactory().createExtensionController();
    }
    
    // Extension point for ExtensionPoint.Type.SERVER_PLUGIN
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.SERVER, type = ExtensionPoint.Type.SERVER_PLUGIN)
    public abstract void initPlugins();

    public abstract boolean isExtensionEnabled(String name);

    public abstract void startPlugins();

    public abstract void stopPlugins();

    public abstract void updatePlugin(String name, Properties properties);

    public abstract void deployTriggered();

    public abstract Object invoke(String name, String method, Object object, String sessionId);

    public abstract Object invokeConnectorService(String name, String method, Object object, String sessionsId) throws Exception;

    public abstract void installExtension(FileItem fileItem) throws ControllerException;

    public abstract void uninstallExtension(String packageName) throws ControllerException;
    
    public abstract void setPluginProperties(String pluginName, Properties properties) throws ControllerException;

    public abstract Properties getPluginProperties(String pluginName) throws ControllerException;

    public abstract Map<String, ConnectorMetaData> getConnectorMetaData() throws ControllerException;

    public abstract void saveConnectorMetaData(Map<String, ConnectorMetaData> metaData) throws ControllerException;

    public abstract List<String> getClientLibraries() throws ControllerException;

    public abstract Map<String, PluginMetaData> getPluginMetaData() throws ControllerException;

    public abstract void savePluginMetaData(Map<String, PluginMetaData> metaData) throws ControllerException;

    public abstract Map<String, ConnectorMetaData> getProtocols();

    public abstract ConnectorMetaData getConnectorMetaDataByProtocol(String protocol);

    public abstract Map<String, ServerPlugin> getLoadedPlugins();
    
    public abstract void setExtensionsProperties(Properties properties) throws ControllerException;
    
    public abstract Properties getExtensionsProperties() throws ControllerException;
}
