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

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author brendanh
 * 
 */
public interface ExtensionController extends Controller {
    // Extension point for ExtensionPoint.Type.SERVER_PLUGIN
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.SERVER, type = ExtensionPoint.Type.SERVER_PLUGIN)
    public void initPlugins();

    public boolean isExtensionEnabled(String name);

    public void startPlugins();

    public void stopPlugins();

    public void updatePlugin(String name, Properties properties);

    public void deployTriggered();

    public Object invoke(String name, String method, Object object, String sessionId);

    public Object invokeConnectorService(String name, String method, Object object, String sessionsId) throws Exception;

    public void installExtension(String location, FileItem fileItem) throws ControllerException;

    public void setPluginProperties(String pluginName, Properties properties) throws ControllerException;

    public Properties getPluginProperties(String pluginName) throws ControllerException;

    public Map<String, ConnectorMetaData> getConnectorMetaData() throws ControllerException;

    public void saveConnectorMetaData(Map<String, ConnectorMetaData> metaData) throws ControllerException;

    public List<String> getConnectorLibraries() throws ControllerException;

    public Map<String, PluginMetaData> getPluginMetaData() throws ControllerException;

    public void savePluginMetaData(Map<String, PluginMetaData> metaData) throws ControllerException;

    public List<String> getPluginLibraries() throws ControllerException;

    public Map<String, ConnectorMetaData> getProtocols();

    public ConnectorMetaData getConnectorMetaDataByProtocol(String protocol);

    public Map<String, ServerPlugin> getLoadedPlugins();
}
