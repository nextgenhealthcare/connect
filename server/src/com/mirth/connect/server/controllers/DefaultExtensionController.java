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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.ExtensionPoint;
import com.mirth.connect.model.ExtensionPointDefinition;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.ServerPlugin;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.util.ExtensionUtil;

public class DefaultExtensionController extends ExtensionController {
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<String, PluginMetaData> plugins = null;
    private Map<String, ServerPlugin> loadedPlugins = null;
    private Map<String, ConnectorMetaData> connectors = null;
    private List<String> clientLibraries = null;
    private Map<String, ConnectorMetaData> protocols = null;

    // singleton pattern
    private static DefaultExtensionController instance = null;
    
    public static ExtensionController create() {
        synchronized (DefaultExtensionController.class) {
            if (instance == null) {
                instance = new DefaultExtensionController();
            }

            return instance;
        }
    }

    private DefaultExtensionController() {

    }

    public void loadExtensions() {
        try {
            loadConnectorMetaData();
            loadPluginMetaData();
            loadClientLibraries();
        } catch (Exception e) {
            logger.error("could not initialize extension settings", e);
            return;
        }
    }
    
    // Extension point for ExtensionPoint.Type.SERVER_PLUGIN
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.SERVER, type = ExtensionPoint.Type.SERVER_PLUGIN)
    public void initPlugins() {
        loadedPlugins = new HashMap<String, ServerPlugin>();

        for (PluginMetaData metaData : plugins.values()) {
            try {
                if (metaData.isEnabled()) {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
                        if (extensionPoint.getMode() == ExtensionPoint.Mode.SERVER && extensionPoint.getType() == ExtensionPoint.Type.SERVER_PLUGIN && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0) {
                            ServerPlugin serverPlugin = (ServerPlugin) Class.forName(extensionPoint.getClassName()).newInstance();
                            String pluginId = extensionPoint.getName();
                            
                            // Get the current properties for this plugin
                            Properties properties = getPluginProperties(pluginId);

                            // If there aren't any stored, store the default properties for the plugin
                            if (properties.isEmpty()) {
                                properties = serverPlugin.getDefaultProperties();
                                setPluginProperties(pluginId, properties);                 
                            }
                            
                            serverPlugin.init(properties);
                            loadedPlugins.put(pluginId, serverPlugin);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error initializing extensions.", e);
            }
        }
    }

    public boolean isExtensionEnabled(String name) {
        for (PluginMetaData plugin : plugins.values()) {
            if (plugin.isEnabled() && plugin.getName().equals(name))
                return true;
        }
        for (ConnectorMetaData connector : connectors.values()) {
            if (connector.isEnabled() && connector.getName().equals(name))
                return true;
        }

        return false;
    }

    public void startPlugins() {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.start();
    }

    public void updatePlugin(String name, Properties properties) {
        loadedPlugins.get(name).update(properties);
    }

    public void triggerDeploy() {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.onDeploy();
    }

    public void stopPlugins() {
        for (ServerPlugin plugin : loadedPlugins.values())
            plugin.stop();
    }

    public Object invokePluginService(String name, String method, Object object, String sessionId) throws Exception {
        return loadedPlugins.get(name).invoke(method, object, sessionId);
    }

    public Object invokeConnectorService(String name, String method, Object object, String sessionsId) throws Exception {
        ConnectorMetaData connectorMetaData = connectors.get(name);

        if (connectorMetaData.getServiceClassName() != null) {
            ConnectorService connectorService = (ConnectorService) Class.forName(connectorMetaData.getServiceClassName()).newInstance();
            return connectorService.invoke(method, object, sessionsId);
        }

        return null;
    }

    public void installExtension(FileItem fileItem) throws ControllerException {
        ExtensionUtil.installExtension(fileItem);
    }
    
    public void uninstallExtension(String packageName) throws ControllerException {
    	File uninstallFile = new File(getExtensionsPath() + EXTENSIONS_UNINSTALL_FILE);

    	try {
			FileWriter fileWriter = new FileWriter(uninstallFile, true);
			fileWriter.write(packageName + System.getProperty("line.separator"));
			fileWriter.close();
			
			for (PluginMetaData plugin : plugins.values()) {
				if (plugin.getPath().equals(packageName) && plugin.getSqlScript() != null) {
				    File pluginSqlScriptFile = new File(ExtensionController.getExtensionsPath() + plugin.getPath() + File.separator + plugin.getSqlScript());
					String contents = FileUtils.readFileToString(pluginSqlScriptFile);
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contents)));
                    
                    String script = getUninstallScript(document);

                    if (script != null) {
	                    List<String> scriptList = new LinkedList<String>();
	
	                    script = script.trim();
	                    StringBuilder sb = new StringBuilder();
	                    boolean blankLine = false;
	                    Scanner s = new Scanner(script);
	
	                    while (s.hasNextLine()) {
	                        String temp = s.nextLine();
	
	                        if (temp.trim().length() > 0)
	                            sb.append(temp + " ");
	                        else
	                            blankLine = true;
	
	                        if (blankLine || !s.hasNextLine()) {
	                            scriptList.add(sb.toString().trim());
	                            blankLine = false;
	                            sb.delete(0, sb.length());
	                        }
	                    }
	                    
	                    // If there was an uninstall script, then save the script to
	                    // run later and remove the schema.plugin from extensions.properties
	                    if (scriptList.size() > 0) {
		                    List<String> uninstallScripts = getUninstallScripts();
		                    uninstallScripts.addAll(scriptList);
		                    setUninstallScripts(uninstallScripts);
		                    ControllerFactory.getFactory().createConfigurationController().removeProperty(plugin.getName(), "schema");
	                    }
                    }
				}
			}
		} catch (Exception e) {
			throw new ControllerException(e);
		}
    }
    
    private String getUninstallScript(Document document) {
    	String script = null;
        Element uninstallElement = (Element) document.getElementsByTagName("uninstall").item(0);
        String databaseType = ControllerFactory.getFactory().createConfigurationController().getDatabaseType();
        NodeList scriptNodes = uninstallElement.getElementsByTagName("script");

        for (int i = 0; i < scriptNodes.getLength(); i++) {
            Node scriptNode = scriptNodes.item(i);
            Node scriptNodeAttribute = scriptNode.getAttributes().getNamedItem("type");

            String[] dbTypes = scriptNodeAttribute.getTextContent().split(",");
            for (int k = 0; k < dbTypes.length; k++) {
                if (dbTypes[k].equals("all") || dbTypes[k].equals(databaseType)) {
                    script = scriptNode.getTextContent();
                }
            }
        }
        
        return script;
    }

    public void setPluginProperties(String pluginName, Properties properties) throws ControllerException {
        for (Object name : properties.keySet()) {
            ControllerFactory.getFactory().createConfigurationController().saveProperty(pluginName, (String) name, (String) properties.get(name));
        }
    }

    public Properties getPluginProperties(String pluginName) throws ControllerException {
        return ControllerFactory.getFactory().createConfigurationController().getPropertiesForGroup(pluginName);
    }
    
    public Map<String, ConnectorMetaData> getConnectorMetaData() throws ControllerException {
        logger.debug("retrieving connector metadata");
        return connectors;
    }

    private void loadConnectorMetaData() throws ControllerException {
        logger.debug("loading connector metadata");
        connectors = (Map<String, ConnectorMetaData>) ExtensionUtil.loadExtensionMetaData(ExtensionType.CONNECTOR);
        protocols = new HashMap<String, ConnectorMetaData>();

        for (ConnectorMetaData connectorMetaData : this.connectors.values()) {
            String protocol = connectorMetaData.getProtocol();
            
            if (protocol.indexOf(':') > -1) {
                String[] protocolStrings = protocol.split(":");
                
                for (int i = 0; i < protocolStrings.length; i++) {
                    protocols.put(protocolStrings[i], connectorMetaData);
                }
            } else {
                protocols.put(connectorMetaData.getProtocol(), connectorMetaData);
            }
        }
    }

    public void saveConnectorMetaData(Map<String, ConnectorMetaData> metaData) throws ControllerException {
        logger.debug("saving connector metadata");
        connectors = metaData;
        ExtensionUtil.saveExtensionMetaData(metaData);
    }

    public List<String> getClientLibraries() throws ControllerException {
        logger.debug("retrieving client libraries");
        return clientLibraries;
    }

    private void loadClientLibraries() {
        logger.debug("loading client libraries");
        List<MetaData> extensionMetaData = new ArrayList<MetaData>();

        extensionMetaData.addAll(plugins.values());
        extensionMetaData.addAll(connectors.values());

        clientLibraries = ExtensionUtil.loadClientLibraries(extensionMetaData);
    }

    public Map<String, PluginMetaData> getPluginMetaData() throws ControllerException {
        logger.debug("retrieving plugin metadata");
        return plugins;
    }

    public void savePluginMetaData(Map<String, PluginMetaData> metaData) throws ControllerException {
        logger.debug("saving plugin metadata");
        plugins = metaData;
        ExtensionUtil.saveExtensionMetaData(metaData);
    }

    private void loadPluginMetaData() throws ControllerException {
        logger.debug("loading plugin metadata");
        plugins = (Map<String, PluginMetaData>) ExtensionUtil.loadExtensionMetaData(ExtensionType.PLUGIN);
    }

    public Map<String, ConnectorMetaData> getProtocols() {
        logger.debug("retrieving plugin protocols");
        return protocols;
    }

    public ConnectorMetaData getConnectorMetaDataByProtocol(String protocol) {
        return protocols.get(protocol);
    }
    
    public ConnectorMetaData getConnectorMetaDataByTransportName(String transportName) {
        return connectors.get(transportName);
    }

    public Map<String, ServerPlugin> getLoadedPlugins() {
        return loadedPlugins;
    }
    
    public void uninstallExtensions() {
    	try {
    		DatabaseUtil.executeScript(getUninstallScripts(), true);
    	} catch (Exception e) {
    		logger.error("Error uninstalling extensions.", e);
    	}
    	
    	deleteUninstallScripts();
    }
    
    public void setUninstallScripts(List<String> uninstallScripts) throws ControllerException {
    	File uninstallScriptsFile = new File(getExtensionsPath() + EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
    	ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    	
    	try {
    	    FileUtils.writeStringToFile(uninstallScriptsFile, serializer.toXML(uninstallScripts));
		} catch (IOException e) {
			throw new ControllerException(e);
		}
    }
    
    public List<String> getUninstallScripts() throws ControllerException {
    	List<String> uninstallScripts = new LinkedList<String>();
    	File uninstallScriptsFile = new File(getExtensionsPath() + EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
    	
    	if (uninstallScriptsFile.exists()) {
    		ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        	try {
				uninstallScripts = (List<String>) serializer.fromXML(FileUtils.readFileToString(uninstallScriptsFile));
			} catch (IOException e) {
				throw new ControllerException(e);
			}
    	}
    	
    	return uninstallScripts;
    }
    
    public void deleteUninstallScripts() {
    	File uninstallScriptsFile = new File(getExtensionsPath() + EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
    	
    	if (uninstallScriptsFile.exists()) {
    		uninstallScriptsFile.delete();
    	}
    }
}
