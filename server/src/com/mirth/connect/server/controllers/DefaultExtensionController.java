/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mirth.connect.client.core.VersionMismatchException;
import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginClass;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.AuthorizationPlugin;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.plugins.ConnectorServicePlugin;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.plugins.ServerPlugin;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.migration.Migrator;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.ServerUUIDGenerator;

public class DefaultExtensionController extends ExtensionController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    // these maps store the metadata for all extensions
    private Map<String, PluginMetaData> pluginMetaDataMap = new HashMap<String, PluginMetaData>();
    private Map<String, ConnectorMetaData> connectorMetaDataMap = new HashMap<String, ConnectorMetaData>();
    private Map<String, ConnectorMetaData> connectorProtocolsMap = new HashMap<String, ConnectorMetaData>();

    // these are plugins for specific extension points, keyed by plugin name
    // (not path)
    private List<ServerPlugin> serverPlugins = new ArrayList<ServerPlugin>();
    private Map<String, ServicePlugin> servicePlugins = new LinkedHashMap<String, ServicePlugin>();
    private Map<String, ChannelPlugin> channelPlugins = new LinkedHashMap<String, ChannelPlugin>();
    private Map<String, DataTypeServerPlugin> dataTypePlugins = new LinkedHashMap<String, DataTypeServerPlugin>();
    private Map<String, ConnectorServicePlugin> connectorServicePlugins = new LinkedHashMap<String, ConnectorServicePlugin>();
    private AuthorizationPlugin authorizationPlugin = null;
    private boolean loadedExtensions = false;

    private static PropertiesConfiguration extensionProperties = null;

    // singleton pattern
    private static DefaultExtensionController instance = null;

    public static ExtensionController create() {
        synchronized (DefaultExtensionController.class) {
            if (instance == null) {
                instance = new DefaultExtensionController();
                instance.initialize();
            }

            return instance;
        }
    }

    private DefaultExtensionController() {

    }

    private void initialize() {
        try {
            extensionProperties = new PropertiesConfiguration(new File(configurationController.getApplicationDataDir(), "extension.properties"));
            extensionProperties.setDelimiterParsingDisabled(true);

            // Auto reload changes
            FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
            fileChangedReloadingStrategy.setRefreshDelay(1000);
            extensionProperties.setReloadingStrategy(fileChangedReloadingStrategy);
        } catch (ConfigurationException e) {
            logger.error("There was an error loading extension.properties", e);
        }
    }

    @Override
    public void removePropertiesForUninstalledExtensions() {
        try {
            File uninstallFile = new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_PROPERTIES_FILE);

            if (uninstallFile.exists()) {
                List<String> extensionPaths = FileUtils.readLines(uninstallFile);

                for (String extensionPath : extensionPaths) {
                    configurationController.removePropertiesForGroup(extensionPath);
                }

                // delete the uninstall file when we're done
                FileUtils.deleteQuietly(uninstallFile);
            }
        } catch (Exception e) {
            logger.error("Error removing properties for uninstalled extensions.", e);
        }
    }

    @Override
    public synchronized void loadExtensions() {
        if (!loadedExtensions) {
            try {
                // match all of the file names for the extension
                IOFileFilter nameFileFilter = new NameFileFilter(new String[] { "plugin.xml",
                        "source.xml", "destination.xml" });
                // this is probably not needed, but we dont want to pick up directories,
                // so we AND the two filters
                IOFileFilter andFileFilter = new AndFileFilter(nameFileFilter, FileFilterUtils.fileFileFilter());
                // this is directory where extensions are located
                File extensionPath = new File(ExtensionController.getExtensionsPath());
                // do a recursive scan for extension files
                Collection<File> extensionFiles = FileUtils.listFiles(extensionPath, andFileFilter, FileFilterUtils.trueFileFilter());

                for (File extensionFile : extensionFiles) {
                    try {
                        MetaData metaData = (MetaData) serializer.deserialize(FileUtils.readFileToString(extensionFile), MetaData.class);

                        if (isExtensionCompatible(metaData)) {
                            if (metaData instanceof ConnectorMetaData) {
                                ConnectorMetaData connectorMetaData = (ConnectorMetaData) metaData;
                                connectorMetaDataMap.put(connectorMetaData.getName(), connectorMetaData);

                                if (StringUtils.contains(connectorMetaData.getProtocol(), ":")) {
                                    for (String protocol : connectorMetaData.getProtocol().split(":")) {
                                        connectorProtocolsMap.put(protocol, connectorMetaData);
                                    }
                                } else {
                                    connectorProtocolsMap.put(connectorMetaData.getProtocol(), connectorMetaData);
                                }
                            } else if (metaData instanceof PluginMetaData) {
                                pluginMetaDataMap.put(metaData.getName(), (PluginMetaData) metaData);
                            }
                        } else {
                            logger.error("Extension \"" + metaData.getName() + "\" is not compatible with this version of Mirth Connect and was not loaded. Please install a compatible version.");
                        }
                    } catch (Exception e) {
                        logger.error("Error reading or parsing extension metadata file: " + extensionFile.getName(), e);
                    }
                }

                loadedExtensions = true;
            } catch (Exception e) {
                logger.error("Error loading extension metadata.", e);
            }
        }
    }

    @Override
    public void setDefaultExtensionStatus() {
        for (MetaData metaData : getPluginMetaData().values()) {
            if (!extensionProperties.containsKey(metaData.getName())) {
                extensionProperties.setProperty(metaData.getName(), true);
                try {
                    extensionProperties.save();
                } catch (ConfigurationException e) {
                    logger.error("Could not save default enabled status for plugin: " + metaData.getName(), e);
                }
            }
        }

        for (MetaData metaData : getConnectorMetaData().values()) {
            if (!extensionProperties.containsKey(metaData.getName())) {
                extensionProperties.setProperty(metaData.getName(), true);
                try {
                    extensionProperties.save();
                } catch (ConfigurationException e) {
                    logger.error("Could not save default enabled status for connector: " + metaData.getName(), e);
                }
            }
        }

        /*
         * Remove extensions from the extensionProperties if they are not in the pluginMetaDataMap
         * or connectorMetaDataMap
         */
        @SuppressWarnings("unchecked")
        Iterator<String> keys = extensionProperties.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!getPluginMetaData().containsKey(key) && !getConnectorMetaData().containsKey(key)) {
                extensionProperties.clearProperty(key);
                try {
                    extensionProperties.save();
                } catch (ConfigurationException e) {
                    logger.error("Could not remove extension status for extension: " + key);
                }
            }
        }
    }

    @Override
    public void initPlugins() {
        // Order all the plugins by their weight before loading any of them.
        Map<String, String> pluginNameMap = new HashMap<String, String>();
        NavigableMap<Integer, List<String>> weightedPlugins = new TreeMap<Integer, List<String>>();
        for (PluginMetaData pmd : pluginMetaDataMap.values()) {
            if (isExtensionEnabled(pmd.getName())) {
                if (pmd.getServerClasses() != null) {
                    for (PluginClass pluginClass : pmd.getServerClasses()) {
                        String clazzName = pluginClass.getName();
                        int weight = pluginClass.getWeight();
                        pluginNameMap.put(clazzName, pmd.getName());

                        List<String> classList = weightedPlugins.get(weight);
                        if (classList == null) {
                            classList = new ArrayList<String>();
                            weightedPlugins.put(weight, classList);
                        }

                        classList.add(clazzName);
                    }
                }
            } else {
                logger.warn("Plugin \"" + pmd.getName() + "\" is not enabled.");
            }
        }

        // Load the plugins in order of their weight
        for (List<String> classList : weightedPlugins.descendingMap().values()) {
            for (String clazzName : classList) {
                String pluginName = pluginNameMap.get(clazzName);

                try {
                    ServerPlugin serverPlugin = (ServerPlugin) Class.forName(clazzName).newInstance();

                    if (serverPlugin instanceof ServicePlugin) {
                        ServicePlugin servicePlugin = (ServicePlugin) serverPlugin;
                        /*
                         * load any properties that may currently be in the database
                         */
                        Properties currentProperties = getPluginProperties(pluginName);
                        /* get the default properties for the plugin */
                        Properties defaultProperties = servicePlugin.getDefaultProperties();

                        /*
                         * if there are any properties that not currently set, set them to the the
                         * default
                         */
                        for (Object key : defaultProperties.keySet()) {
                            if (!currentProperties.containsKey(key)) {
                                currentProperties.put(key, defaultProperties.get(key));
                            }
                        }

                        /* save the properties to the database */
                        setPluginProperties(pluginName, currentProperties);

                        /*
                         * initialize the plugin with those properties and add it to the list of
                         * loaded plugins
                         */
                        servicePlugin.init(currentProperties);
                        servicePlugins.put(servicePlugin.getPluginPointName(), servicePlugin);
                        serverPlugins.add(servicePlugin);
                        logger.debug("sucessfully loaded server plugin: " + serverPlugin.getPluginPointName());
                    }

                    if (serverPlugin instanceof ChannelPlugin) {
                        ChannelPlugin channelPlugin = (ChannelPlugin) serverPlugin;
                        channelPlugins.put(channelPlugin.getPluginPointName(), channelPlugin);
                        serverPlugins.add(channelPlugin);
                        logger.debug("sucessfully loaded server channel plugin: " + serverPlugin.getPluginPointName());
                    }

                    if (serverPlugin instanceof DataTypeServerPlugin) {
                        DataTypeServerPlugin dataTypePlugin = (DataTypeServerPlugin) serverPlugin;
                        dataTypePlugins.put(dataTypePlugin.getPluginPointName(), dataTypePlugin);
                        serverPlugins.add(dataTypePlugin);
                        logger.debug("sucessfully loaded server data type plugin: " + serverPlugin.getPluginPointName());
                    }

                    if (serverPlugin instanceof ConnectorServicePlugin) {
                        ConnectorServicePlugin connectorService = (ConnectorServicePlugin) serverPlugin;
                        connectorServicePlugins.put(connectorService.getTransportName(), connectorService);
                        serverPlugins.add(connectorService);
                        logger.debug("sucessfully loaded connector service plugin: " + serverPlugin.getPluginPointName());
                    }

                    if (serverPlugin instanceof AuthorizationPlugin) {
                        AuthorizationPlugin authorizationPlugin = (AuthorizationPlugin) serverPlugin;

                        if (this.authorizationPlugin != null) {
                            throw new Exception("Multiple Authorization Plugins are not permitted.");
                        }

                        this.authorizationPlugin = authorizationPlugin;
                        serverPlugins.add(authorizationPlugin);
                        logger.debug("sucessfully loaded server authorization plugin: " + serverPlugin.getPluginPointName());
                    }
                } catch (Exception e) {
                    logger.error("Error instantiating plugin: " + pluginName, e);
                }
            }
        }
    }

    /* These are the maps for the different types of plugins */
    /* ********************************************************************** */

    @Override
    public Map<String, ServicePlugin> getServicePlugins() {
        return servicePlugins;
    }

    @Override
    public Map<String, ChannelPlugin> getChannelPlugins() {
        return channelPlugins;
    }

    @Override
    public Map<String, DataTypeServerPlugin> getDataTypePlugins() {
        return dataTypePlugins;
    }

    @Override
    public Map<String, ConnectorServicePlugin> getConnectorServicePlugins() {
        return connectorServicePlugins;
    }

    @Override
    public AuthorizationPlugin getAuthorizationPlugin() {
        return authorizationPlugin;
    }

    /* ********************************************************************** */

    @Override
    public void setExtensionEnabled(String extensionName, boolean enabled) throws ControllerException {
        extensionProperties.setProperty(extensionName, enabled);
        try {
            extensionProperties.save();
        } catch (ConfigurationException e) {
            logger.error("Could not save enabled status " + enabled + " for extension: " + extensionName, e);
        }
    }

    @Override
    public boolean isExtensionEnabled(String extensionName) {
        return extensionProperties.getBoolean(extensionName, true);
    }

    @Override
    public void startPlugins() {
        for (ServerPlugin serverPlugin : serverPlugins) {
            serverPlugin.start();
        }

        // Get all of the server plugin extension permissions and add those to
        // the authorization controller.
        AuthorizationController authorizationController = ControllerFactory.getFactory().createAuthorizationController();

        for (ServicePlugin plugin : servicePlugins.values()) {
            if (plugin.getExtensionPermissions() != null) {
                for (ExtensionPermission extensionPermission : plugin.getExtensionPermissions()) {
                    authorizationController.addExtensionPermission(extensionPermission);
                }
            }
        }
    }

    @Override
    public void stopPlugins() {
        for (ServerPlugin serverPlugin : serverPlugins) {
            serverPlugin.stop();
        }
    }

    @Override
    public void updatePluginProperties(String name, Properties properties) {
        ServicePlugin servicePlugin = servicePlugins.get(name);

        if (servicePlugin != null) {
            servicePlugin.update(properties);
        } else {
            logger.error("Error setting properties for service plugin that has not been loaded: name=" + name);
        }
    }

    @Override
    public Object invokePluginService(String name, String method, Object object, String sessionId) throws Exception {
        ServicePlugin servicePlugin = servicePlugins.get(name);

        if (servicePlugin != null) {
            return servicePlugins.get(name).invoke(method, object, sessionId);
        } else {
            logger.error("Error invoking service plugin that has not been loaded: name=" + name + ", method=" + method);
            return null;
        }
    }

    @Override
    public Object invokeConnectorService(String channelId, String name, String method, Object object, String sessionId) throws Exception {
        ConnectorMetaData connectorMetaData = connectorMetaDataMap.get(name);

        if (StringUtils.isNotBlank(connectorMetaData.getServiceClassName())) {
            ConnectorService connectorService = (ConnectorService) Class.forName(connectorMetaData.getServiceClassName()).newInstance();

            if (connectorServicePlugins.containsKey(name)) {
                return connectorServicePlugins.get(name).invoke(connectorService, channelId, method, object, sessionId);
            } else {
                return connectorService.invoke(channelId, method, object, sessionId);
            }
        }

        return null;
    }

    @Override
    public void extractExtension(FileItem fileItem) throws ControllerException {
        File installTempDir = new File(ExtensionController.getExtensionsPath(), "install_temp");

        if (!installTempDir.exists()) {
            installTempDir.mkdir();
        }

        File tempFile = null;
        ZipFile zipFile = null;

        try {
            /*
             * create a new temp file (in the install temp dir) to store the zip file contents
             */
            tempFile = File.createTempFile(ServerUUIDGenerator.getUUID(), ".zip", installTempDir);
            // write the contents of the multipart fileitem to the temp file
            fileItem.write(tempFile);
            // create a new zip file from the temp file
            zipFile = new ZipFile(tempFile);
            // get a list of all of the entries in the zip file
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith("plugin.xml") || entryName.endsWith("destination.xml") || entryName.endsWith("source.xml")) {
                    // parse the extension metadata xml file
                    MetaData extensionMetaData = serializer.deserialize(IOUtils.toString(zipFile.getInputStream(entry)), MetaData.class);

                    if (!isExtensionCompatible(extensionMetaData)) {
                        throw new VersionMismatchException("Extension \"" + entry.getName() + "\" is not compatible with this version of Mirth Connect.");
                    }
                }
            }

            // reset the entries and extract
            entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    /*
                     * assume directories are stored parents first then children.
                     * 
                     * TODO: this is not robust, just for demonstration purposes.
                     */
                    File directory = new File(installTempDir, entry.getName());
                    directory.mkdir();
                } else {
                    // otherwise, write the file out to the install temp dir
                    InputStream inputStream = zipFile.getInputStream(entry);
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(installTempDir, entry.getName())));
                    IOUtils.copy(inputStream, outputStream);
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        } catch (Exception e) {
            throw new ControllerException("Error extracting extension. " + e.toString(), e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {
                    throw new ControllerException(e);
                }
            }

            // delete the temp file since it is no longer needed
            FileUtils.deleteQuietly(tempFile);
        }
    }

    /**
     * Adds the specified plugin path to a list of plugins that should be deleted on next server
     * startup. Also deletes the schema version property from the database. If this function fails
     * to add the extension path to the uninstall file, it will still continue to remove add the
     * database uninstall scripts, and the folder must be deleted manually.
     * 
     */
    @Override
    public void prepareExtensionForUninstallation(String pluginPath) throws ControllerException {
        addExtensionToUninstallFile(pluginPath);

        for (PluginMetaData plugin : pluginMetaDataMap.values()) {
            if (plugin.getPath().equals(pluginPath)) {
                addExtensionToUninstallPropertiesFile(plugin.getName());

                if (plugin.getMigratorClass() != null) {
                    try {
                        Migrator migrator = (Migrator) Class.forName(plugin.getMigratorClass()).newInstance();
                        migrator.setDatabaseType(ConfigurationController.getInstance().getDatabaseType());
                        migrator.setDefaultScriptPath("extensions/" + plugin.getPath());
                        appendToUninstallScript(migrator.getUninstallStatements());
                    } catch (Exception e) {
                        logger.error("Failed to retrieve uninstall database statements for plugin: " + pluginPath, e);
                    }
                }
            }
        }
    }

    /*
     * Parses the uninstallation script and returns a list of statements.
     */
    private List<String> parseUninstallScript(String script) {
        List<String> scriptList = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean blankLine = false;
        Scanner scanner = new Scanner(script);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (StringUtils.isNotBlank(line)) {
                sb.append(line + " ");
            } else {
                blankLine = true;
            }

            if (blankLine || !scanner.hasNextLine()) {
                scriptList.add(sb.toString().trim());
                blankLine = false;
                sb.delete(0, sb.length());
            }
        }

        return scriptList;
    }

    /*
     * append the extension path name to a list of extensions that should be deleted on next startup
     * by MirthLauncher
     */
    private void addExtensionToUninstallFile(String pluginPath) {
        File uninstallFile = new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_FILE);
        FileWriter writer = null;

        try {
            writer = new FileWriter(uninstallFile, true);
            writer.write(pluginPath + System.getProperty("line.separator"));
        } catch (IOException e) {
            logger.error("Error adding extension to uninstall file: " + pluginPath, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private void addExtensionToUninstallPropertiesFile(String pluginName) {
        File uninstallFile = new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_PROPERTIES_FILE);
        FileWriter writer = null;

        try {
            writer = new FileWriter(uninstallFile, true);
            writer.write(pluginName + System.getProperty("line.separator"));
        } catch (IOException e) {
            logger.error("Error adding extension to uninstall properties file: " + pluginName, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private String getUninstallScriptForCurrentDatabase(String pluginSqlScripts) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(pluginSqlScripts)));
        Element uninstallElement = (Element) document.getElementsByTagName("uninstall").item(0);
        String databaseType = ControllerFactory.getFactory().createConfigurationController().getDatabaseType();
        NodeList scriptNodes = uninstallElement.getElementsByTagName("script");
        String script = null;

        for (int i = 0; i < scriptNodes.getLength(); i++) {
            Node scriptNode = scriptNodes.item(i);
            Node scriptType = scriptNode.getAttributes().getNamedItem("type");
            String[] databaseTypes = scriptType.getTextContent().split(",");

            for (int j = 0; j < databaseTypes.length; j++) {
                if (databaseTypes[j].equals("all") || databaseTypes[j].equals(databaseType)) {
                    script = scriptNode.getTextContent().trim();
                }
            }
        }

        return script;
    }

    @Override
    public void setPluginProperties(String pluginName, Properties properties) throws ControllerException {
        configurationController.removePropertiesForGroup(pluginName);

        for (Object name : properties.keySet()) {
            configurationController.saveProperty(pluginName, (String) name, (String) properties.get(name));
        }
    }

    @Override
    public Properties getPluginProperties(String pluginName) throws ControllerException {
        return ControllerFactory.getFactory().createConfigurationController().getPropertiesForGroup(pluginName);
    }

    @Override
    public Map<String, ConnectorMetaData> getConnectorMetaData() {
        return connectorMetaDataMap;
    }

    @Override
    public Map<String, PluginMetaData> getPluginMetaData() {
        return pluginMetaDataMap;
    }

    @Override
    public ConnectorMetaData getConnectorMetaDataByProtocol(String protocol) {
        return connectorProtocolsMap.get(protocol);
    }

    @Override
    public ConnectorMetaData getConnectorMetaDataByTransportName(String transportName) {
        return connectorMetaDataMap.get(transportName);
    }

    /**
     * Executes the script that removes that database tables for plugins that are marked for
     * removal. The actual removal of the plugin directory happens in MirthLauncher.java, before
     * they can be added to the server classpath.
     * 
     */
    @Override
    public void uninstallExtensions() {
        try {
            DatabaseUtil.executeScript(readUninstallScript(), true);
        } catch (Exception e) {
            logger.error("Error uninstalling extensions.", e);
        }

        // delete the uninstall scripts file
        FileUtils.deleteQuietly(new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_SCRIPTS_FILE));
    }

    private void appendToUninstallScript(List<String> uninstallStatements) throws IOException {
        if (uninstallStatements != null) {
            List<String> uninstallScripts = readUninstallScript();
            uninstallScripts.addAll(uninstallStatements);
            File uninstallScriptsFile = new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
            FileUtils.writeStringToFile(uninstallScriptsFile, serializer.serialize(uninstallScripts));
        }
    }

    /*
     * This MUST return an empty list if there is no uninstall file.
     */
    @SuppressWarnings("unchecked")
    private List<String> readUninstallScript() throws IOException {
        File uninstallScriptsFile = new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
        List<String> scripts = new ArrayList<String>();

        if (uninstallScriptsFile.exists()) {
            scripts = serializer.deserializeList(FileUtils.readFileToString(uninstallScriptsFile), String.class);
        }

        return scripts;
    }

    private boolean isExtensionCompatible(MetaData metaData) {
        String serverMirthVersion = ControllerFactory.getFactory().createConfigurationController().getServerVersion();
        String[] extensionMirthVersions = metaData.getMirthVersion().split(",");

        logger.debug("checking extension \"" + metaData.getName() + "\" version compatability: versions=" + ArrayUtils.toString(extensionMirthVersions) + ", server=" + serverMirthVersion);

        // if there is no build version, just use the patch version
        if (serverMirthVersion.split("\\.").length == 4) {
            serverMirthVersion = serverMirthVersion.substring(0, serverMirthVersion.lastIndexOf('.'));
        }

        for (int i = 0; i < extensionMirthVersions.length; i++) {
            if (extensionMirthVersions[i].trim().equals(serverMirthVersion)) {
                return true;
            }
        }

        return false;
    }

    public List<String> getClientLibraries() {
        List<String> clientLibFilenames = new ArrayList<String>();
        File clientLibDir = new File("client-lib");

        if (!clientLibDir.exists() || !clientLibDir.isDirectory()) {
            clientLibDir = new File("build/client-lib");
        }

        if (clientLibDir.exists() && clientLibDir.isDirectory()) {
            Collection<File> clientLibs = FileUtils.listFiles(clientLibDir, new SuffixFileFilter(".jar"), FileFilterUtils.falseFileFilter());

            for (File clientLib : clientLibs) {
                clientLibFilenames.add(FilenameUtils.getName(clientLib.getName()));
            }
        } else {
            logger.error("Could not find client-lib directory: " + clientLibDir.getAbsolutePath());
        }

        return clientLibFilenames;
    }

    public List<ServerPlugin> getServerPlugins() {
        return serverPlugins;
    }
}
