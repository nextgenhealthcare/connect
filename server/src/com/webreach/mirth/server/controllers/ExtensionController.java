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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.util.ExtensionUtil;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author brendanh
 * 
 */
public class ExtensionController
{
    private Logger logger = Logger.getLogger(this.getClass());
    private SystemLogger systemLogger = SystemLogger.getInstance();
    public static final String PLUGIN_LOCATION = ClassPathResource.getResourceURI("plugins").getPath() + System.getProperty("file.separator");
    public static final String PLUGIN_FILE_SUFFIX = ".properties";
    private Map<String, PluginMetaData> plugins;
    private List<String> pluginLibraries;
    private Map<String, ServerPlugin> loadedPlugins = null;
    private Map<String, ConnectorMetaData> connectors = null;
    private List<String> connectorLibraries = null;
    private Map<String, ConnectorMetaData> protocols = null;
    private static String CONNECTORS_LOCATION = ClassPathResource.getResourceURI("connectors").getPath() + System.getProperty("file.separator");
    
    // singleton pattern
    private static ExtensionController instance = null;

    public static ExtensionController getInstance()
    {
        synchronized (ExtensionController.class)
        {
            if (instance == null)
                instance = new ExtensionController();

            return instance;
        }
    }

    private ExtensionController()
    {

    }

    public void initialize()
    {
        try
        {
            loadConnectorMetaData();
            loadConnectorLibraries();
            loadPluginMetaData();
            loadPluginLibraries();
        }
        catch (Exception e)
        {
            logger.error("could not initialize extension settings", e);
            return;
        }

        initPlugins();
    }

    public void initPlugins()
    {
        loadedPlugins = new HashMap<String, ServerPlugin>();

        for (PluginMetaData metaData : plugins.values())
        {
            try
            {
            	if (metaData.isEnabled()){
	            	for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()){
		                if(extensionPoint.getMode() == ExtensionPoint.Mode.SERVER && 
		                		extensionPoint.getType() == ExtensionPoint.Type.SERVER_PLUGIN && 
		                		extensionPoint.getClassName() != null && 
		                		extensionPoint.getClassName().length() > 0)
		                {
		                    ServerPlugin serverPlugin = (ServerPlugin) Class.forName(extensionPoint.getClassName()).newInstance();
		                    String pluginName = metaData.getName();
		                    Properties properties = null;
		    
		                    try
		                    {
		                        properties = getPluginProperties(pluginName);
		                    }
		                    catch (Exception e)
		                    {
		                        properties = serverPlugin.getDefaultProperties();
		                        setPluginProperties(pluginName, properties);
		                    }
		    
		                    serverPlugin.init(properties);
		                    loadedPlugins.put(pluginName, serverPlugin);
		                }
	            	}
            	}
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void startPlugins()
    {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.start();
    }

    public void updatePlugin(String name, Properties properties)
    {
        loadedPlugins.get(name).update(properties);
    }
    
    public void deployTriggered()
    {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.onDeploy();
    }

    public void stopPlugins()
    {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.stop();
    }
    
    public Object invoke (String name, String method, Object object)
    {
        return loadedPlugins.get(name).invoke(method, object);
    }
    
    public void installExtension(String location, byte[] fileContents) throws ControllerException
    {
        if(location.equals("connectors"))
            location = CONNECTORS_LOCATION;
        else if(location.equals("plugins"))
            location = PLUGIN_LOCATION;
        ExtensionUtil.installExtension(location, fileContents);
    }

    public void setPluginProperties(String pluginName, Properties properties) throws ControllerException
    {
        logger.debug("setting " + pluginName + " properties");

        FileOutputStream fileOutputStream = null;

        try
        {
            File propertiesFile = new File(PLUGIN_LOCATION + pluginName + PLUGIN_FILE_SUFFIX);
            fileOutputStream = new FileOutputStream(propertiesFile);
            properties.store(fileOutputStream, "Updated " + pluginName + " properties");
        }
        catch (Exception e)
        {
            throw new ControllerException(e);
        }
        finally
        {
            try
            {
                if (fileOutputStream != null)
                {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
            catch (IOException e)
            {
                logger.warn(e);
            }
        }
    }

    public Properties getPluginProperties(String pluginName) throws ControllerException
    {
        logger.debug("retrieving " + pluginName + " properties");

        FileInputStream fileInputStream = null;
        Properties properties = null;

        try
        {
            File propertiesFile = new File(PLUGIN_LOCATION + pluginName + PLUGIN_FILE_SUFFIX);
            fileInputStream = new FileInputStream(propertiesFile);
            properties = new Properties();
            properties.load(fileInputStream);
        }
        catch (Exception e)
        {
            throw new ControllerException(e);
        }
        finally
        {
            try
            {
                if (fileInputStream != null)
                    fileInputStream.close();
            }
            catch (IOException e)
            {
                logger.warn(e);
            }
        }

        return properties;
    }
    
    public Map<String, ConnectorMetaData> getConnectorMetaData() throws ControllerException {
        logger.debug("retrieving connector metadata");
        return this.connectors;
    }

    private void loadConnectorMetaData() throws ControllerException {
        logger.debug("loading connector metadata");
        this.connectors = (Map<String, ConnectorMetaData>) ExtensionUtil.loadExtensionMetaData(CONNECTORS_LOCATION);
        this.protocols = new HashMap<String, ConnectorMetaData>();
        for(ConnectorMetaData cmd: this.connectors.values()){
        	protocols.put(cmd.getProtocol(), cmd);
        }
    }
    
    public void saveConnectorMetaData(Map<String, ConnectorMetaData> metaData) throws ControllerException
    {
        logger.debug("saving connector metadata");
        this.connectors = metaData;
        ExtensionUtil.saveExtensionMetaData(metaData, CONNECTORS_LOCATION);
    }

    public List<String> getConnectorLibraries() throws ControllerException {
        logger.debug("retrieving connector libraries");
        return this.connectorLibraries;
    }

    private void loadConnectorLibraries() throws ControllerException {
        logger.debug("loading connector libraries");
        this.connectorLibraries = ExtensionUtil.loadExtensionLibraries(CONNECTORS_LOCATION);
    }
    
    public Map<String, PluginMetaData> getPluginMetaData() throws ControllerException
    {
        logger.debug("retrieving plugin metadata");
        return this.plugins;
    }
    
    public void savePluginMetaData(Map<String, PluginMetaData> metaData) throws ControllerException
    {
        logger.debug("saving plugin metadata");
        this.plugins = metaData;
        ExtensionUtil.saveExtensionMetaData(metaData, PLUGIN_LOCATION);
    }

    private void loadPluginMetaData() throws ControllerException
    {
        logger.debug("loading plugin metadata");
        this.plugins = (Map<String, PluginMetaData>) ExtensionUtil.loadExtensionMetaData(PLUGIN_LOCATION);
    }

    public List<String> getPluginLibraries() throws ControllerException
    {
        logger.debug("retrieving plugin libraries");
        return this.pluginLibraries;
    }
    public Map<String, ConnectorMetaData> getProtocols(){
    	logger.debug("retrieving plugin protocols");
    	return this.protocols;
    }
    public ConnectorMetaData getConnectorMetaDataByProtocol(String protocol){
    	return protocols.get(protocol);
    }

    private void loadPluginLibraries() throws ControllerException
    {
        logger.debug("loading plugin libraries");
        this.pluginLibraries = ExtensionUtil.loadExtensionLibraries(PLUGIN_LOCATION);;
    }
}
