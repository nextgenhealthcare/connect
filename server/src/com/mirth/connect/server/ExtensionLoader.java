/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginClass;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.ResourceUtil;

public final class ExtensionLoader {
    private final static ExtensionLoader instance = new ExtensionLoader();

    public static ExtensionLoader getInstance() {
        return instance;
    }

    private Map<String, ConnectorMetaData> connectorMetaDataMap = new HashMap<String, ConnectorMetaData>();
    private Map<String, PluginMetaData> pluginMetaDataMap = new HashMap<String, PluginMetaData>();
    private Map<String, ConnectorMetaData> connectorProtocolsMap = new HashMap<String, ConnectorMetaData>();
    private boolean loadedExtensions = false;
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private static Logger logger = Logger.getLogger(ExtensionLoader.class);

    private ExtensionLoader() {}

    public Map<String, ConnectorMetaData> getConnectorMetaData() {
        loadExtensions();
        return connectorMetaDataMap;
    }

    public Map<String, PluginMetaData> getPluginMetaData() {
        loadExtensions();
        return pluginMetaDataMap;
    }

    public Map<String, ConnectorMetaData> getConnectorProtocols() {
        loadExtensions();
        return connectorProtocolsMap;
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> getControllerClass(Class<T> abstractClass) {
        Class<T> overrideClass = null;
        PluginClass highestPluginClassModel = null;

        for (PluginMetaData pluginMetaData : getPluginMetaData().values()) {
            List<PluginClass> controllerClasses = pluginMetaData.getControllerClasses();

            if (controllerClasses != null) {
                for (PluginClass controllerClassModel : controllerClasses) {
                    try {
                        Class<?> pluginClass = Class.forName(controllerClassModel.getName());

                        if (abstractClass.isAssignableFrom(pluginClass) && (highestPluginClassModel == null || highestPluginClassModel.getWeight() < controllerClassModel.getWeight())) {
                            highestPluginClassModel = controllerClassModel;
                            overrideClass = (Class<T>) pluginClass;
                        }
                    } catch (Exception e) {
                        logger.error("An error occurred while attempting to load \"" + controllerClassModel.getName() + "\" from plugin: " + pluginMetaData.getName(), e);
                    }
                }
            }
        }

        return overrideClass;
    }

    public <T> T getControllerInstance(Class<T> abstractClass) {
        Class<T> overrideClass = getControllerClass(abstractClass);

        if (overrideClass != null) {
            try {
                T instance = overrideClass.newInstance();
                logger.debug("Using custom " + abstractClass.getSimpleName() + ": " + overrideClass.getName());
                return instance;
            } catch (Exception e) {
                logger.error("An error occurred while attempting to instantiate " + abstractClass.getSimpleName() + " implementation: " + overrideClass.getName(), e);
            }
        }

        logger.debug("Using default " + abstractClass.getSimpleName());
        return null;
    }

    public boolean isExtensionCompatible(MetaData metaData) {
        String serverMirthVersion;
        try {
            serverMirthVersion = getServerVersion();
        } catch (Exception e) {
            logger.error("An error occurred while attempting to determine the current server version.", e);
            return false;
        }

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

    /**
     * Loads the metadata files (plugin.xml, source.xml, destination.xml) for all extensions of the
     * specified type. If this function fails to parse the metadata file for an extension, it will
     * skip it and continue.
     */
    private synchronized void loadExtensions() {
        if (!loadedExtensions) {
            try {
                // match all of the file names for the extension
                IOFileFilter nameFileFilter = new NameFileFilter(new String[] {
                        "plugin.xml", "source.xml", "destination.xml" });
                // this is probably not needed, but we dont want to pick up directories,
                // so we AND the two filters
                IOFileFilter andFileFilter = new AndFileFilter(nameFileFilter, FileFilterUtils.fileFileFilter());
                // this is directory where extensions are located
                File extensionPath = new File(getExtensionsPath());
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
            } catch (Exception e) {
                logger.error("Error loading extension metadata.", e);
            } finally {
                loadedExtensions = true;
            }
        }
    }

    /**
     * If in an IDE, extensions will be on the classpath as a resource. If that's the case, use that
     * directory. Otherwise, use the mirth home directory and append extensions.
     * 
     * @return
     */
    private String getExtensionsPath() {
        if (ClassPathResource.getResourceURI("extensions") != null) {
            return ClassPathResource.getResourceURI("extensions").getPath() + File.separator;
        } else {
            return new File(ClassPathResource.getResourceURI("mirth.properties")).getParentFile().getParent() + File.separator + "extensions" + File.separator;
        }
    }

    private String getServerVersion() throws FileNotFoundException, ConfigurationException {
        PropertiesConfiguration versionConfig = new PropertiesConfiguration();
        versionConfig.setDelimiterParsingDisabled(true);
        InputStream versionPropertiesStream = ResourceUtil.getResourceStream(ExtensionLoader.class, "version.properties");
        versionConfig.load(versionPropertiesStream);
        IOUtils.closeQuietly(versionPropertiesStream);
        return versionConfig.getString("mirth.version");
    }
}
