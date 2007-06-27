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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.server.util.FileUtil;
import com.webreach.mirth.server.util.PluginUtil;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author brendanh
 * 
 */
public class PluginController
{
    private Logger logger = Logger.getLogger(this.getClass());
    private SystemLogger systemLogger = new SystemLogger();
    public static final String PLUGIN_LOCATION = ClassPathResource.getResourceURI("services").getPath() + System.getProperty("file.separator");
    public static final String PLUGIN_FILE_SUFFIX = ".properties";
    private Map<String, PluginMetaData> plugins;
    private List<String> pluginLibraries;
    private Map<String, ServerPlugin> loadedPlugins = null;

    // singleton pattern
    private static PluginController instance = null;

    public static PluginController getInstance()
    {
        synchronized (PluginController.class)
        {
            if (instance == null)
                instance = new PluginController();

            return instance;
        }
    }

    private PluginController()
    {

    }

    public void initialize()
    {
        try
        {
            loadPluginMetaData();
            loadPluginLibraries();
        }
        catch (Exception e)
        {
            logger.error("could not initialize plugin settings", e);
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
                ServerPlugin plugin = (ServerPlugin) Class.forName(metaData.getServerClassName()).newInstance();
                String pluginName = metaData.getName();
                Properties properties = null;

                try
                {
                    properties = getPluginProperties(pluginName);
                }
                catch (Exception e)
                {
                    properties = plugin.getDefaultProperties();
                    setPluginProperties(pluginName, properties);
                }

                plugin.init(properties);
                loadedPlugins.put(pluginName, plugin);
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

    public void stopPlugins()
    {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.stop();
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

    public Map<String, PluginMetaData> getPluginMetaData() throws ControllerException
    {
        logger.debug("retrieving plugin metadata");
        return this.plugins;
    }

    private void loadPluginMetaData() throws ControllerException
    {
        logger.debug("loading plugin metadata");
        this.plugins = (Map<String, PluginMetaData>) PluginUtil.loadPluginMetaData(PLUGIN_LOCATION);
    }

    public List<String> getPluginLibraries() throws ControllerException
    {
        logger.debug("retrieving plugin libraries");
        return this.pluginLibraries;
    }

    private void loadPluginLibraries() throws ControllerException
    {
        logger.debug("loading plugin libraries");
        this.pluginLibraries = PluginUtil.loadPluginLibraries(PLUGIN_LOCATION);;
    }
}
