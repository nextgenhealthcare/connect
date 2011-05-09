/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mirth.connect.client.core.VersionMismatchException;
import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.ExtensionPoint;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.plugins.ConnectorStatusPlugin;
import com.mirth.connect.plugins.ServerPlugin;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.UUIDGenerator;

public class DefaultExtensionController extends ExtensionController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    // these maps store the metadata for all extensions
    private Map<String, PluginMetaData> pluginMetaDataMap = null;
    private Map<String, ConnectorMetaData> connectorMetaDataMap = null;
    private Map<String, ConnectorMetaData> connectorProtocolsMap = null;

    // these are plugins for specific extension points, keyed by plugin name (not path)
    private Map<String, ServerPlugin> serverPlugins = new HashMap<String, ServerPlugin>();
    private Map<String, ConnectorStatusPlugin> connectorStatusPlugins = new HashMap<String, ConnectorStatusPlugin>();
    private Map<String, ChannelPlugin> channelPlugins = new HashMap<String, ChannelPlugin>();

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
        } catch (Exception e) {
            logger.error("Error loading extension metadata.", e);
        }
    }

    public void initPlugins() {
        for (PluginMetaData pmd : pluginMetaDataMap.values()) {
            if (pmd.isEnabled() && isExtensionCompatible(pmd)) {
                for (ExtensionPoint extensionPoint : pmd.getExtensionPoints()) {
                    if ((extensionPoint.getMode() == ExtensionPoint.Mode.SERVER) && StringUtils.isNotBlank(extensionPoint.getClassName())) {
                        String pluginName = extensionPoint.getName();

                        try {
                            switch (extensionPoint.getType()) {
                                case SERVER_PLUGIN:
                                    ServerPlugin serverPlugin = (ServerPlugin) Class.forName(extensionPoint.getClassName()).newInstance();
                                    /*
                                     * load any properties that may currently be
                                     * in the database
                                     */
                                    Properties currentProperties = getPluginProperties(pluginName);
                                    /* get the default properties for the plugin */
                                    Properties defaultProperties = serverPlugin.getDefaultProperties();

                                    /*
                                     * if there are any properties that not
                                     * currently set, set them to the the
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
                                     * initialize the plugin with those
                                     * properties and add it to the list of
                                     * loaded plugins
                                     */
                                    serverPlugin.init(currentProperties);
                                    serverPlugins.put(pluginName, serverPlugin);
                                    logger.debug("sucessfully loaded server plugin: " + pluginName);
                                    break;
                                case SERVER_CONNECTOR_STATUS:
                                    /*
                                     * This is needed in case you add a second
                                     * constructor to your plugin. Java is not
                                     * able to find the default one for the
                                     * plugin.
                                     */
                                    Constructor<?>[] constructors = Class.forName(extensionPoint.getClassName()).getDeclaredConstructors();

                                    for (int i = 0; i < constructors.length; i++) {
                                        Class<?> parameters[] = constructors[i].getParameterTypes();

                                        // load plugin if the number of
                                        // parameters is 0
                                        if (parameters.length == 0) {
                                            ConnectorStatusPlugin connectorStatusPlugin = (ConnectorStatusPlugin) constructors[i].newInstance(new Object[] {});
                                            connectorStatusPlugins.put(pluginName, connectorStatusPlugin);
                                            i = constructors.length;
                                        }
                                    }

                                    logger.debug("sucessfully loaded connector status plugin: " + pluginName);
                                    break;
                                case SERVER_CHANNEL:
                                    ChannelPlugin channelPlugin = (ChannelPlugin) Class.forName(extensionPoint.getClassName()).newInstance();
                                    channelPlugins.put(pluginName, channelPlugin);
                                    logger.debug("sucessfully loaded server channel plugin: " + pluginName);
                                    break;
                                default:
                                    break;
                            }
                        } catch (Exception e) {
                            logger.error("Error initializing plugin \"" + pmd.getName() + "\" with properties.", e);
                        }
                    }
                }
            } else {
                logger.warn("Plugin \"" + pmd.getName() + "\" is not enabled or is not compatible with this version of Mirth Connect.");
            }
        }
    }

    public Map<String, ServerPlugin> getServerPlugins() {
        return serverPlugins;
    }

    public Map<String, ConnectorStatusPlugin> getConnectorStatusPlugins() {
        return connectorStatusPlugins;
    }

    public Map<String, ChannelPlugin> getChannelPlugins() {
        return channelPlugins;
    }

    public boolean isExtensionEnabled(String name) {
        for (PluginMetaData plugin : pluginMetaDataMap.values()) {
            if (plugin.isEnabled() && plugin.getName().equals(name)) {
                return true;
            }
        }

        for (ConnectorMetaData connector : connectorMetaDataMap.values()) {
            if (connector.isEnabled() && connector.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public void startPlugins() {
        for (ServerPlugin serverPlugin : getServerPlugins().values()) {
            serverPlugin.start();
        }

        for (ConnectorStatusPlugin connectorStatusPlugin : getConnectorStatusPlugins().values()) {
            connectorStatusPlugin.start();
        }

        // Get all of the server plugin extension permissions and add those to
        // the authorization controller.
        AuthorizationController authorizationController = ControllerFactory.getFactory().createAuthorizationController();

        for (ServerPlugin plugin : serverPlugins.values()) {
            if (plugin.getExtensionPermissions() != null) {
                for (ExtensionPermission extensionPermission : plugin.getExtensionPermissions()) {
                    authorizationController.addExtensionPermission(extensionPermission);
                }
            }
        }
    }

    public void updatePluginProperties(String name, Properties properties) {
        serverPlugins.get(name).update(properties);
    }

    public void triggerDeploy() {
        for (ServerPlugin plugin : serverPlugins.values()) {
            plugin.onDeploy();
        }
    }

    public void stopPlugins() {
        for (ServerPlugin serverPlugin : serverPlugins.values()) {
            serverPlugin.stop();
        }

        for (ConnectorStatusPlugin connectorStatusPlugin : connectorStatusPlugins.values()) {
            connectorStatusPlugin.stop();
        }
    }

    public Object invokePluginService(String name, String method, Object object, String sessionId) throws Exception {
        return serverPlugins.get(name).invoke(method, object, sessionId);
    }

    public Object invokeConnectorService(String name, String method, Object object, String sessionsId) throws Exception {
        ConnectorMetaData connectorMetaData = connectorMetaDataMap.get(name);

        if (StringUtils.isNotBlank(connectorMetaData.getServiceClassName())) {
            ConnectorService connectorService = (ConnectorService) Class.forName(connectorMetaData.getServiceClassName()).newInstance();
            return connectorService.invoke(method, object, sessionsId);
        }

        return null;
    }

    public void extractExtension(FileItem fileItem) throws ControllerException {
        String pluginFilename = ExtensionController.ExtensionType.PLUGIN.getFileNames()[0];
        String destinationFilename = ExtensionController.ExtensionType.DESTINATION.getFileNames()[0];
        String sourceFilename = ExtensionController.ExtensionType.SOURCE.getFileNames()[0];

        File installTempDir = new File(ExtensionController.getExtensionsPath(), "install_temp");

        if (!installTempDir.exists()) {
            installTempDir.mkdir();
        }

        File tempFile = null;
        ZipFile zipFile = null;

        try {
            /*
             * create a new temp file (in the install temp dir) to store the zip
             * file contents
             */
            tempFile = File.createTempFile(UUIDGenerator.getUUID(), ".zip", installTempDir);
            // write the contents of the multipart fileitem to the temp file
            fileItem.write(tempFile);
            // create a new zip file from the temp file
            zipFile = new ZipFile(tempFile);
            // get a list of all of the entries in the zip file
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.endsWith(pluginFilename) || entryName.endsWith(destinationFilename) || entryName.endsWith(sourceFilename)) {
                    ObjectXMLSerializer serializer = new ObjectXMLSerializer();

                    // parse the extension metadata xml file
                    MetaData extensionMetaData = (MetaData) serializer.fromXML(IOUtils.toString(zipFile.getInputStream(entry)));

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
                     * assume directories are stored parents first then
                     * children. TODO: this is not robust, just for
                     * demonstration purposes.
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
            throw new ControllerException("Error extracting extension.", e);
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
     * Adds the specified plugin path to a list of plugins that should be
     * deleted on next server startup. Also deletes the schema version property
     * from the database. If this function fails to add the extension path to
     * the uninstall file, it will still continue to remove add the database
     * uninstall scripts, and the folder must be deleted manually.
     * 
     */
    public void prepareExtensionForUninstallation(String pluginPath) throws ControllerException {
        try {
            addExtensionToUninstallFile(pluginPath);

            for (PluginMetaData plugin : pluginMetaDataMap.values()) {
                if (plugin.getPath().equals(pluginPath) && plugin.getSqlScript() != null) {
                    String pluginSqlScripts = FileUtils.readFileToString(new File(ExtensionController.getExtensionsPath() + plugin.getPath() + File.separator + plugin.getSqlScript()));
                    String script = getUninstallScriptForCurrentDatabase(pluginSqlScripts);

                    if (script != null) {
                        List<String> scriptList = parseUninstallScript(script);

                        /*
                         * If there was an uninstall script, then save the
                         * script to run later and remove the schema.plugin from
                         * extensions.properties
                         */
                        if (scriptList.size() > 0) {
                            List<String> uninstallScripts = readUninstallScript();
                            uninstallScripts.addAll(scriptList);
                            writeUninstallScript(uninstallScripts);
                            configurationController.removeProperty(plugin.getName(), "schema");
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ControllerException("Error preparing extension \"" + pluginPath + "\" for uninstallation.", e);
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
     * append the extension path name to a list of extensions that should be
     * deleted on next startup by MirthLauncher
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

    public void setPluginProperties(String pluginName, Properties properties) throws ControllerException {
        configurationController.removePropertiesForGroup(pluginName);

        for (Object name : properties.keySet()) {
            configurationController.saveProperty(pluginName, (String) name, (String) properties.get(name));
        }
    }

    public Properties getPluginProperties(String pluginName) throws ControllerException {
        return ControllerFactory.getFactory().createConfigurationController().getPropertiesForGroup(pluginName);
    }

    public Map<String, ConnectorMetaData> getConnectorMetaData() {
        return connectorMetaDataMap;
    }

    private void loadConnectorMetaData() throws ControllerException {
        logger.debug("loading connector metadata");
        connectorMetaDataMap = (Map<String, ConnectorMetaData>) getMetaDataForExtensionType(ExtensionType.CONNECTOR);
        connectorProtocolsMap = new HashMap<String, ConnectorMetaData>();

        for (ConnectorMetaData connectorMetaData : connectorMetaDataMap.values()) {
            if (StringUtils.contains(connectorMetaData.getProtocol(), ":")) {
                for (String protocol : connectorMetaData.getProtocol().split(":")) {
                    connectorProtocolsMap.put(protocol, connectorMetaData);
                }
            } else {
                connectorProtocolsMap.put(connectorMetaData.getProtocol(), connectorMetaData);
            }
        }
    }

    public void saveConnectorMetaData(Map<String, ConnectorMetaData> metaData) throws ControllerException {
        connectorMetaDataMap = metaData;

        try {
            saveExtensionMetaData(metaData);
        } catch (IOException e) {
            throw new ControllerException("Error saving connector metadata.", e);
        }
    }

    public List<String> getClientExtensionLibraries() {
        List<String> clientLibraries = new ArrayList<String>();
        List<MetaData> extensionMetaData = new ArrayList<MetaData>();
        extensionMetaData.addAll(pluginMetaDataMap.values());
        extensionMetaData.addAll(connectorMetaDataMap.values());

        for (MetaData metaData : extensionMetaData) {
            // Only add enabled extension libraries to the classpath
            if (metaData.isEnabled()) {
                for (ExtensionLibrary library : metaData.getLibraries()) {
                    if (library.getType().equals(ExtensionLibrary.Type.CLIENT) || library.getType().equals(ExtensionLibrary.Type.SHARED)) {
                        clientLibraries.add(metaData.getPath() + "/" + library.getPath());
                    }
                }
            }
        }

        return clientLibraries;
    }

    public Map<String, PluginMetaData> getPluginMetaData() {
        return pluginMetaDataMap;
    }

    public void savePluginMetaData(Map<String, PluginMetaData> metaData) throws ControllerException {
        pluginMetaDataMap = metaData;

        try {
            saveExtensionMetaData(metaData);
        } catch (IOException e) {
            throw new ControllerException("Error saving plugin metadata.", e);
        }
    }

    private void loadPluginMetaData() throws ControllerException {
        pluginMetaDataMap = (Map<String, PluginMetaData>) getMetaDataForExtensionType(ExtensionType.PLUGIN);
    }

    public ConnectorMetaData getConnectorMetaDataByProtocol(String protocol) {
        return connectorProtocolsMap.get(protocol);
    }

    public ConnectorMetaData getConnectorMetaDataByTransportName(String transportName) {
        return connectorMetaDataMap.get(transportName);
    }

    /**
     * Executes the script that removes that database tables for plugins that
     * are marked for removal. The actual removal of the plguin directory
     * happens in MirthLauncher.java, before they can be added to the server
     * classpath.
     * 
     */
    public void uninstallExtensions() {
        try {
            DatabaseUtil.executeScript(readUninstallScript(), true);
        } catch (Exception e) {
            logger.error("Error uninstalling extensions.", e);
        }

        // delete the uninstall scripts file
        FileUtils.deleteQuietly(new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_SCRIPTS_FILE));
    }

    private void writeUninstallScript(List<String> uninstallScripts) throws IOException {
        File uninstallScriptsFile = new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
        FileUtils.writeStringToFile(uninstallScriptsFile, serializer.toXML(uninstallScripts));
    }

    /*
     * This MUST return an empty list if there is no uninstall file.
     */
    private List<String> readUninstallScript() throws IOException {
        File uninstallScriptsFile = new File(getExtensionsPath(), EXTENSIONS_UNINSTALL_SCRIPTS_FILE);
        List<String> scripts = new ArrayList<String>();

        if (uninstallScriptsFile.exists()) {
            scripts = (List<String>) serializer.fromXML(FileUtils.readFileToString(uninstallScriptsFile));
        }

        return scripts;
    }

    /**
     * Returns the metadata files (plugin.xml, source.xml, destination.xml) for
     * all extensions of the specified type. If this function fails to parse the
     * metadata file for an extension, it will skip it and continue.
     * 
     * @param extensionType
     * @return
     * @throws ControllerException
     */
    private Map<String, ? extends MetaData> getMetaDataForExtensionType(ExtensionType extensionType) {
        // match all of the file names for the extension (plugin.xml,
        // source.xml, destination.xml)
        IOFileFilter nameFileFilter = new NameFileFilter(extensionType.getFileNames());
        // this is probably not needed, but we dont want to pick up directories,
        // so we AND the two filters
        IOFileFilter andFileFilter = new AndFileFilter(nameFileFilter, FileFilterUtils.fileFileFilter());
        // this is directory where extensions are located
        File extensionPath = new File(ExtensionController.getExtensionsPath());
        // do a recursive scan for extension files
        Collection<File> extensionFiles = FileUtils.listFiles(extensionPath, andFileFilter, FileFilterUtils.trueFileFilter());

        Map<String, MetaData> extensionMetaDataMap = new HashMap<String, MetaData>();

        for (File extensionFile : extensionFiles) {
            try {
                MetaData extensionMetaData = (MetaData) serializer.fromXML(FileUtils.readFileToString(extensionFile));
                extensionMetaDataMap.put(extensionMetaData.getName(), extensionMetaData);
            } catch (Exception e) {
                logger.error("Error reading or parsing extension metadata file: " + extensionFile.getName(), e);
            }
        }

        return extensionMetaDataMap;
    }

    /**
     * Saves the extension metadata to the file system.
     * 
     * @param metaData
     * @throws ControllerException
     */
    private void saveExtensionMetaData(Map<String, ? extends MetaData> metaData) throws IOException {
        for (Entry<String, ? extends MetaData> entry : metaData.entrySet()) {
            MetaData extensionMetaData = entry.getValue();
            String fileName = ExtensionType.PLUGIN.getFileNames()[0];

            if (extensionMetaData instanceof ConnectorMetaData) {
                if (((ConnectorMetaData) extensionMetaData).getType().equals(ConnectorMetaData.Type.SOURCE)) {
                    fileName = ExtensionType.SOURCE.getFileNames()[0];
                } else {
                    fileName = ExtensionType.DESTINATION.getFileNames()[0];
                }
            }

            File metaDataFile = new File(ExtensionController.getExtensionsPath() + extensionMetaData.getPath() + File.separator + fileName);
            FileUtils.writeStringToFile(metaDataFile, serializer.toXML(metaData.get(entry.getKey())));
        }
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
}
