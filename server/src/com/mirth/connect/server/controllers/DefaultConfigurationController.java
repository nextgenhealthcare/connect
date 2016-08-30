/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.commons.encryption.Digester;
import com.mirth.commons.encryption.Encryptor;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.commons.encryption.Output;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.data.DonkeyStatisticsUpdater;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplateLibrary;
import com.mirth.connect.model.DatabaseSettings;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ResourcePropertiesList;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.MergePropertiesInterface;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.plugins.directoryresource.DirectoryResourceProperties;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.mybatis.KeyValuePair;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.PasswordRequirementsChecker;
import com.mirth.connect.server.util.ResourceUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.ChannelDependencyException;
import com.mirth.connect.util.ChannelDependencyGraph;
import com.mirth.connect.util.ConfigurationProperty;
import com.mirth.connect.util.ConnectionTestResponse;
import com.mirth.connect.util.MigrationUtil;
import com.mirth.connect.util.MirthSSLUtil;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public class DefaultConfigurationController extends ConfigurationController {
    public static final String PROPERTIES_CORE = "core";
    public static final String PROPERTIES_RESOURCES = "resources";
    public static final String PROPERTIES_DEPENDENCIES = "channelDependencies";
    public static final String PROPERTIES_CHANNEL_METADATA = "channelMetadata";
    public static final String SECRET_KEY_ALIAS = "encryption";

    private Logger logger = Logger.getLogger(this.getClass());
    private String appDataDir = null;
    private String baseDir = null;
    private String configurationFile = null;
    private static String serverId = null;
    private String serverName = null;
    private int status = ConfigurationController.STATUS_UNAVAILABLE;
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private PasswordRequirements passwordRequirements;
    private String[] httpsClientProtocols;
    private String[] httpsServerProtocols;
    private String[] httpsCipherSuites;
    private boolean startupDeploy;
    private volatile Map<String, String> configurationMap = Collections.unmodifiableMap(new HashMap<String, String>());
    private volatile Map<String, String> commentMap = Collections.unmodifiableMap(new HashMap<String, String>());
    private static PropertiesConfiguration versionConfig = new PropertiesConfiguration();
    private static PropertiesConfiguration mirthConfig = new PropertiesConfiguration();
    private static EncryptionSettings encryptionConfig;
    private static DatabaseSettings databaseConfig;
    private static String apiBypassword;
    private static int statsUpdateInterval;

    private static KeyEncryptor encryptor = null;
    private static Digester digester = null;

    private static final String CHARSET = "ca.uhn.hl7v2.llp.charset";
    private static final String PROPERTY_TEMP_DIR = "dir.tempdata";
    private static final String PROPERTY_APP_DATA_DIR = "dir.appdata";
    private static final String CONFIGURATION_MAP_PATH = "configurationmap.path";
    private static final String HTTPS_CLIENT_PROTOCOLS = "https.client.protocols";
    private static final String HTTPS_SERVER_PROTOCOLS = "https.server.protocols";
    private static final String HTTPS_CIPHER_SUITES = "https.ciphersuites";
    private static final String STARTUP_DEPLOY = "server.startupdeploy";
    private static final String API_BYPASSWORD = "server.api.bypassword";
    private static final String STATS_UPDATE_INTERVAL = "donkey.statsupdateinterval";

    // singleton pattern
    private static ConfigurationController instance = null;

    private DefaultConfigurationController() {

    }

    public static ConfigurationController create() {
        synchronized (DefaultConfigurationController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(ConfigurationController.class);

                if (instance == null) {
                    instance = new DefaultConfigurationController();
                    ((DefaultConfigurationController) instance).initialize();
                }
            }

            return instance;
        }
    }

    private void initialize() {
        try {
            // Disable delimiter parsing so getString() returns the whole
            // property, even if there are commas
            mirthConfig.setDelimiterParsingDisabled(true);
            mirthConfig.setFile(new File(ClassPathResource.getResourceURI("mirth.properties")));
            mirthConfig.load();

            MigrationController.getInstance().migrateConfiguration(mirthConfig);

            // load the server version
            versionConfig.setDelimiterParsingDisabled(true);
            InputStream versionPropertiesStream = ResourceUtil.getResourceStream(this.getClass(), "version.properties");
            versionConfig.load(versionPropertiesStream);
            IOUtils.closeQuietly(versionPropertiesStream);

            if (mirthConfig.getString(PROPERTY_TEMP_DIR) != null) {
                File tempDataDirFile = new File(mirthConfig.getString(PROPERTY_TEMP_DIR));

                if (!tempDataDirFile.exists()) {
                    if (tempDataDirFile.mkdirs()) {
                        logger.debug("created tempdir: " + tempDataDirFile.getAbsolutePath());
                    } else {
                        logger.error("error creating tempdir: " + tempDataDirFile.getAbsolutePath());
                    }
                }

                System.setProperty("java.io.tmpdir", tempDataDirFile.getAbsolutePath());
                logger.debug("set temp data dir: " + tempDataDirFile.getAbsolutePath());
            }

            File appDataDirFile = null;

            if (mirthConfig.getString(PROPERTY_APP_DATA_DIR) != null) {
                appDataDirFile = new File(mirthConfig.getString(PROPERTY_APP_DATA_DIR));

                if (!appDataDirFile.exists()) {
                    if (appDataDirFile.mkdir()) {
                        logger.debug("created app data dir: " + appDataDirFile.getAbsolutePath());
                    } else {
                        logger.error("error creating app data dir: " + appDataDirFile.getAbsolutePath());
                    }
                }
            } else {
                appDataDirFile = new File(".");
            }

            appDataDir = appDataDirFile.getAbsolutePath();
            logger.debug("set app data dir: " + appDataDir);

            baseDir = new File(ClassPathResource.getResourceURI("mirth.properties")).getParentFile().getParent();
            logger.debug("set base dir: " + baseDir);

            if (mirthConfig.getString(CHARSET) != null) {
                System.setProperty(CHARSET, mirthConfig.getString(CHARSET));
            }

            String[] httpsClientProtocolsArray = mirthConfig.getStringArray(HTTPS_CLIENT_PROTOCOLS);
            if (ArrayUtils.isNotEmpty(httpsClientProtocolsArray)) {
                // Support both comma separated and multiline values
                List<String> httpsClientProtocolsList = new ArrayList<String>();
                for (String protocol : httpsClientProtocolsArray) {
                    httpsClientProtocolsList.addAll(Arrays.asList(StringUtils.split(protocol, ',')));
                }
                httpsClientProtocols = httpsClientProtocolsList.toArray(new String[httpsClientProtocolsList.size()]);
            } else {
                httpsClientProtocols = MirthSSLUtil.DEFAULT_HTTPS_CLIENT_PROTOCOLS;
            }

            String[] httpsServerProtocolsArray = mirthConfig.getStringArray(HTTPS_SERVER_PROTOCOLS);
            if (ArrayUtils.isNotEmpty(httpsServerProtocolsArray)) {
                // Support both comma separated and multiline values
                List<String> httpsServerProtocolsList = new ArrayList<String>();
                for (String protocol : httpsServerProtocolsArray) {
                    httpsServerProtocolsList.addAll(Arrays.asList(StringUtils.split(protocol, ',')));
                }
                httpsServerProtocols = httpsServerProtocolsList.toArray(new String[httpsServerProtocolsList.size()]);
            } else {
                httpsServerProtocols = MirthSSLUtil.DEFAULT_HTTPS_SERVER_PROTOCOLS;
            }

            String[] httpsCipherSuitesArray = mirthConfig.getStringArray(HTTPS_CIPHER_SUITES);
            if (ArrayUtils.isNotEmpty(httpsCipherSuitesArray)) {
                // Support both comma separated and multiline values
                List<String> httpsCipherSuitesList = new ArrayList<String>();
                for (String cipherSuite : httpsCipherSuitesArray) {
                    httpsCipherSuitesList.addAll(Arrays.asList(StringUtils.split(cipherSuite, ',')));
                }
                httpsCipherSuites = httpsCipherSuitesList.toArray(new String[httpsCipherSuitesList.size()]);
            } else {
                httpsCipherSuites = MirthSSLUtil.DEFAULT_HTTPS_CIPHER_SUITES;
            }

            String deploy = String.valueOf(mirthConfig.getProperty(STARTUP_DEPLOY));
            if (StringUtils.isNotBlank(deploy)) {
                startupDeploy = Boolean.parseBoolean(deploy);
            }

            // Check for server GUID and generate a new one if it doesn't exist
            PropertiesConfiguration serverIdConfig = new PropertiesConfiguration(new File(getApplicationDataDir(), "server.id"));

            if ((serverIdConfig.getString("server.id") != null) && (serverIdConfig.getString("server.id").length() > 0)) {
                serverId = serverIdConfig.getString("server.id");
            } else {
                serverId = generateGuid();
                logger.debug("generated new server id: " + serverId);
                serverIdConfig.setProperty("server.id", serverId);
                serverIdConfig.save();
            }

            passwordRequirements = PasswordRequirementsChecker.getInstance().loadPasswordRequirements(mirthConfig);

            apiBypassword = mirthConfig.getString(API_BYPASSWORD);
            if (StringUtils.isNotBlank(apiBypassword)) {
                apiBypassword = new String(Base64.decodeBase64(apiBypassword), "US-ASCII");
            }

            statsUpdateInterval = NumberUtils.toInt(mirthConfig.getString(STATS_UPDATE_INTERVAL), DonkeyStatisticsUpdater.DEFAULT_UPDATE_INTERVAL);

            // Check for configuration map properties
            if (mirthConfig.getString(CONFIGURATION_MAP_PATH) != null) {
                configurationFile = mirthConfig.getString(CONFIGURATION_MAP_PATH);
            } else {
                configurationFile = getApplicationDataDir() + File.separator + "configuration.properties";
            }

            PropertiesConfiguration configurationMapProperties = new PropertiesConfiguration();
            configurationMapProperties.setDelimiterParsingDisabled(true);
            configurationMapProperties.setListDelimiter((char) 0);
            try {
                configurationMapProperties.load(new File(configurationFile));
            } catch (ConfigurationException e) {
                logger.warn("Failed to find configuration map file");
            }

            Map<String, ConfigurationProperty> configurationMap = new HashMap<String, ConfigurationProperty>();
            Iterator<String> iterator = configurationMapProperties.getKeys();

            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = configurationMapProperties.getString(key);
                String comment = configurationMapProperties.getLayout().getCanonicalComment(key, false);

                configurationMap.put(key, new ConfigurationProperty(value, comment));
            }

            setConfigurationProperties(configurationMap, false);
        } catch (Exception e) {
            logger.error("Failed to initialize configuration controller", e);
        }
    }

    /*
     * Return the server GUID
     */
    @Override
    public String getServerId() {
        return serverId;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    /*
     * Return the server timezone in the following format: PDT (UTC -7)
     */
    @Override
    public String getServerTimezone(Locale locale) {
        TimeZone timeZone = TimeZone.getDefault();
        boolean daylight = timeZone.inDaylightTime(new Date());

        // Get the short timezone display name with respect to DST
        String timeZoneDisplay = timeZone.getDisplayName(daylight, TimeZone.SHORT, locale);

        // Get the offset in hours (divide by number of milliseconds in an hour)
        int offset = timeZone.getOffset(System.currentTimeMillis()) / (3600000);

        // Get the offset display in either UTC -x or UTC +x
        String offsetDisplay = (offset < 0) ? String.valueOf(offset) : "+" + offset;
        timeZoneDisplay += " (UTC " + offsetDisplay + ")";

        return timeZoneDisplay;
    }

    /*
     * Return the server time
     */
    @Override
    public Calendar getServerTime() {
        return Calendar.getInstance();
    }

    // ast: Get the list of all available encodings for this JVM
    @Override
    public List<String> getAvailableCharsetEncodings() throws ControllerException {
        logger.debug("Retrieving avaiable character encodings");

        try {
            SortedMap<String, Charset> avaiablesCharsets = Charset.availableCharsets();
            List<String> simpleAvaiableCharsets = new ArrayList<String>();

            for (Charset charset : avaiablesCharsets.values()) {
                String charsetName = charset.name();

                try {
                    if (StringUtils.isEmpty(charsetName)) {
                        charsetName = charset.aliases().iterator().next();
                    }
                } catch (Exception e) {
                    charsetName = "UNKNOWN";
                }

                simpleAvaiableCharsets.add(charsetName);
            }

            return simpleAvaiableCharsets;
        } catch (Exception e) {
            throw new ControllerException("Error retrieving available charset encodings.", e);
        }
    }

    @Override
    public ServerSettings getServerSettings() throws ControllerException {
        serverName = getProperty(PROPERTIES_CORE + "." + serverId, "server.name");
        Properties serverSettings = getPropertiesForGroup(PROPERTIES_CORE);
        return new ServerSettings(serverName, serverSettings);
    }

    @Override
    public EncryptionSettings getEncryptionSettings() throws ControllerException {
        return encryptionConfig;
    }

    @Override
    public DatabaseSettings getDatabaseSettings() throws ControllerException {
        return databaseConfig;
    }

    @Override
    public void setServerSettings(ServerSettings settings) throws ControllerException {
        String serverName = settings.getServerName();
        if (serverName != null) {
            saveProperty(PROPERTIES_CORE + "." + serverId, "server.name", serverName);
            this.serverName = serverName;
        }

        Properties properties = settings.getProperties();
        for (Object name : properties.keySet()) {
            saveProperty(PROPERTIES_CORE, (String) name, (String) properties.get(name));
        }
    }

    @Override
    public UpdateSettings getUpdateSettings() throws ControllerException {
        return new UpdateSettings(getPropertiesForGroup(PROPERTIES_CORE));
    }

    @Override
    public void setUpdateSettings(UpdateSettings settings) throws ControllerException {
        Properties properties = settings.getProperties();
        for (Object name : properties.keySet()) {
            saveProperty(PROPERTIES_CORE, (String) name, (String) properties.get(name));
        }
    }

    @Override
    public String generateGuid() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getDatabaseType() {
        return mirthConfig.getString("database");
    }

    @Override
    public Encryptor getEncryptor() {
        return encryptor;
    }

    @Override
    public Digester getDigester() {
        return digester;
    }

    @Override
    public List<DriverInfo> getDatabaseDrivers() throws ControllerException {
        logger.debug("retrieving database driver list");
        File driversFile = new File(ClassPathResource.getResourceURI("dbdrivers.xml"));

        if (driversFile.exists()) {
            try {
                ArrayList<DriverInfo> drivers = new ArrayList<DriverInfo>();
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(driversFile);
                Element driversElement = document.getDocumentElement();

                for (int i = 0; i < driversElement.getElementsByTagName("driver").getLength(); i++) {
                    Element driverElement = (Element) driversElement.getElementsByTagName("driver").item(i);
                    DriverInfo driver = new DriverInfo(driverElement.getAttribute("name"), driverElement.getAttribute("class"), driverElement.getAttribute("template"), driverElement.getAttribute("selectLimit"));
                    logger.debug("found database driver: " + driver);
                    drivers.add(driver);
                }

                return drivers;
            } catch (Exception e) {
                throw new ControllerException("Error during loading of database drivers file: " + driversFile.getAbsolutePath(), e);
            }
        } else {
            throw new ControllerException("Could not locate database drivers file: " + driversFile.getAbsolutePath());
        }
    }

    @Override
    public String getServerVersion() {
        return versionConfig.getString("mirth.version");
    }

    @Override
    public String getBuildDate() {
        return versionConfig.getString("mirth.date");
    }

    @Override
    public String[] getHttpsClientProtocols() {
        return ArrayUtils.clone(httpsClientProtocols);
    }

    @Override
    public String[] getHttpsServerProtocols() {
        return ArrayUtils.clone(httpsServerProtocols);
    }

    @Override
    public String[] getHttpsCipherSuites() {
        return ArrayUtils.clone(httpsCipherSuites);
    }

    @Override
    public boolean isStartupDeploy() {
        return startupDeploy;
    }

    @Override
    public int getStatsUpdateInterval() {
        return statsUpdateInterval;
    }

    @Override
    public int getStatus() {
        logger.debug("getting Mirth status");

        // If the database isn't running or the engine isn't running (only if it isn't starting) return STATUS_UNAVAILABLE.
        if (!isDatabaseRunning() || (!ControllerFactory.getFactory().createEngineController().isRunning() && status != STATUS_ENGINE_STARTING)) {
            return STATUS_UNAVAILABLE;
        }

        return status;
    }

    @Override
    public ServerConfiguration getServerConfiguration() throws ControllerException {
        ChannelController channelController = ControllerFactory.getFactory().createChannelController();
        AlertController alertController = ControllerFactory.getFactory().createAlertController();
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();

        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setChannelGroups(channelController.getChannelGroups(null));
        serverConfiguration.setChannels(channelController.getChannels(null));
        serverConfiguration.setAlerts(alertController.getAlerts());
        serverConfiguration.setCodeTemplateLibraries(codeTemplateController.getLibraries(null, true));
        serverConfiguration.setServerSettings(getServerSettings());
        serverConfiguration.setUpdateSettings(getUpdateSettings());
        serverConfiguration.setGlobalScripts(scriptController.getGlobalScripts());

        // Put the properties for every plugin with properties in a map.
        Map<String, Properties> pluginProperties = new HashMap<String, Properties>();
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

        for (PluginMetaData pluginMetaData : extensionController.getPluginMetaData().values()) {
            String pluginName = pluginMetaData.getName();
            Properties properties = extensionController.getPluginProperties(pluginName);

            if (MapUtils.isNotEmpty(properties)) {
                pluginProperties.put(pluginName, properties);
            }
        }

        serverConfiguration.setPluginProperties(pluginProperties);
        serverConfiguration.setResourceProperties(ObjectXMLSerializer.getInstance().deserialize(getResources(), ResourcePropertiesList.class));
        serverConfiguration.setChannelDependencies(getChannelDependencies());

        return serverConfiguration;
    }

    @Override
    public void setServerConfiguration(ServerConfiguration serverConfiguration, boolean deploy) throws StartException, StopException, ControllerException, InterruptedException {
        ChannelController channelController = ControllerFactory.getFactory().createChannelController();
        AlertController alertController = ControllerFactory.getFactory().createAlertController();
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
        EngineController engineController = ControllerFactory.getFactory().createEngineController();

        /*
         * Make sure users aren't deploying or undeploying channels while the server configuration
         * is being restored.
         */
        synchronized (engineController) {
            Set<ChannelGroup> channelGroups = new HashSet<ChannelGroup>();
            if (serverConfiguration.getChannelGroups() != null) {
                channelGroups.addAll(serverConfiguration.getChannelGroups());
            }
            channelController.updateChannelGroups(channelGroups, new HashSet<String>(), true);

            if (serverConfiguration.getChannels() != null) {
                // Undeploy all channels before updating or removing them
                engineController.undeployChannels(engineController.getDeployedIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);

                // Remove channels that don't exist in the new configuration
                for (Channel channel : channelController.getChannels(null)) {
                    boolean found = false;

                    for (Channel newChannel : serverConfiguration.getChannels()) {
                        if (newChannel.getId().equals(channel.getId())) {
                            found = true;
                        }
                    }

                    if (!found) {
                        channelController.removeChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
                    }
                }

                // Update all channels from the server configuration
                for (Channel channel : serverConfiguration.getChannels()) {
                    channelController.updateChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
                }
            }

            if (serverConfiguration.getAlerts() != null) {
                // Remove all existing alerts
                for (AlertModel alert : alertController.getAlerts()) {
                    alertController.removeAlert(alert.getId());
                }

                for (AlertModel alert : serverConfiguration.getAlerts()) {
                    alertController.updateAlert(alert);
                }
            }

            if (serverConfiguration.getCodeTemplateLibraries() != null) {
                List<CodeTemplateLibrary> clonedLibraries = new ArrayList<CodeTemplateLibrary>();
                for (CodeTemplateLibrary library : serverConfiguration.getCodeTemplateLibraries()) {
                    clonedLibraries.add(new CodeTemplateLibrary(library));
                }

                // Update all libraries from the server configuration
                codeTemplateController.updateLibraries(clonedLibraries, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);

                // Remove code templates that don't exist in the new configuration
                for (CodeTemplate codeTemplate : codeTemplateController.getCodeTemplates(null)) {
                    boolean found = false;

                    for (CodeTemplateLibrary newLibrary : serverConfiguration.getCodeTemplateLibraries()) {
                        if (newLibrary.getCodeTemplates() != null) {
                            for (CodeTemplate newCodeTemplate : newLibrary.getCodeTemplates()) {
                                if (newCodeTemplate.getId().equals(codeTemplate.getId())) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (found) {
                            break;
                        }
                    }

                    if (!found) {
                        codeTemplateController.removeCodeTemplate(codeTemplate.getId(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
                    }
                }

                // Update all code templates from the server configuration
                for (CodeTemplateLibrary library : serverConfiguration.getCodeTemplateLibraries()) {
                    if (library.getCodeTemplates() != null) {
                        for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                            codeTemplateController.updateCodeTemplate(codeTemplate, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
                        }
                    }
                }
            }

            if (serverConfiguration.getServerSettings() != null) {
                // The server name must not be restored.
                ServerSettings serverSettings = serverConfiguration.getServerSettings();
                serverSettings.setServerName(null);

                setServerSettings(serverSettings);
            }

            if (serverConfiguration.getUpdateSettings() != null) {
                setUpdateSettings(serverConfiguration.getUpdateSettings());
            }

            // Set the properties for all plugins in the server configuration,
            // whether or not the plugin is actually installed on this server.
            if (serverConfiguration.getPluginProperties() != null) {
                ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

                for (Entry<String, Properties> pluginEntry : serverConfiguration.getPluginProperties().entrySet()) {
                    String pluginName = pluginEntry.getKey();
                    Properties properties = pluginEntry.getValue();

                    try {
                        // Allow the plugin to modify the properties first if it needs to
                        ServicePlugin servicePlugin = extensionController.getServicePlugins().get(pluginName);
                        if (servicePlugin != null && servicePlugin instanceof MergePropertiesInterface) {
                            ((MergePropertiesInterface) servicePlugin).modifyPropertiesOnRestore(properties);
                        }

                        extensionController.setPluginProperties(pluginName, properties);
                    } catch (Exception e) {
                        logger.error("Error restoring " + pluginName + " properties.", e);
                    }
                }
            }

            if (serverConfiguration.getResourceProperties() != null) {
                setResources(ObjectXMLSerializer.getInstance().serialize(serverConfiguration.getResourceProperties()));

                try {
                    List<LibraryProperties> libraryResources = new ArrayList<LibraryProperties>();
                    for (ResourceProperties resource : serverConfiguration.getResourceProperties().getList()) {
                        if (resource instanceof LibraryProperties) {
                            libraryResources.add((LibraryProperties) resource);
                        }
                    }

                    ControllerFactory.getFactory().createContextFactoryController().updateResources(libraryResources, false);
                } catch (Exception e) {
                    logger.error("Unable to update libraries: " + e.getMessage(), e);
                }
            }

            if (serverConfiguration.getChannelDependencies() != null) {
                setChannelDependencies(serverConfiguration.getChannelDependencies());
            } else {
                setChannelDependencies(new HashSet<ChannelDependency>());
            }

            if (serverConfiguration.getGlobalScripts() != null) {
                scriptController.setGlobalScripts(serverConfiguration.getGlobalScripts());
            }

            // Deploy all channels
            if (deploy) {
                engineController.deployChannels(channelController.getChannelIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);
            }
        }
    }

    @Override
    public Map<String, String> getConfigurationMap() {
        return configurationMap;
    }

    @Override
    public synchronized Map<String, ConfigurationProperty> getConfigurationProperties() {
        Map<String, ConfigurationProperty> map = new HashMap<String, ConfigurationProperty>();

        for (Entry<String, String> entry : configurationMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String comment = commentMap.get(key);

            map.put(key, new ConfigurationProperty(value, comment));
        }

        return map;
    }

    @Override
    public synchronized void setConfigurationProperties(Map<String, ConfigurationProperty> map, boolean persist) throws ControllerException {
        if (persist) {
            saveConfigurationProperties(map);
        }

        Map<String, String> valueMap = new HashMap<String, String>();
        Map<String, String> commentMap = new HashMap<String, String>();

        for (Entry<String, ConfigurationProperty> entry : map.entrySet()) {
            valueMap.put(entry.getKey(), entry.getValue().getValue());
            commentMap.put(entry.getKey(), entry.getValue().getComment());
        }

        configurationMap = Collections.unmodifiableMap(valueMap);
        this.commentMap = Collections.unmodifiableMap(commentMap);
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String getBaseDir() {
        return baseDir;
    }

    @Override
    public String getApplicationDataDir() {
        return appDataDir;
    }

    @Override
    public String getConfigurationDir() {
        return baseDir + File.separator + "conf";
    }

    @Override
    public PasswordRequirements getPasswordRequirements() {
        return passwordRequirements;
    }

    @Override
    public boolean isBypasswordEnabled() {
        return StringUtils.isNotBlank(apiBypassword);
    }

    @Override
    public boolean checkBypassword(String password) {
        return isBypasswordEnabled() && StringUtils.equals(password, apiBypassword);
    }

    @Override
    public Properties getPropertiesForGroup(String category) {
        logger.debug("retrieving properties: category=" + category);
        Properties properties = new Properties();

        try {
            List<KeyValuePair> result = SqlConfig.getSqlSessionManager().selectList("Configuration.selectPropertiesForCategory", category);

            for (KeyValuePair pair : result) {
                properties.setProperty(pair.getKey(), StringUtils.defaultString(pair.getValue()));
            }
        } catch (Exception e) {
            logger.error("Could not retrieve properties: category=" + category, e);
        }

        return properties;
    }

    public void removePropertiesForGroup(String category) {
        logger.debug("deleting all properties: category=" + category);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("category", category);
            SqlConfig.getSqlSessionManager().delete("Configuration.deleteProperty", parameterMap);
        } catch (Exception e) {
            logger.error("Could not delete properties: category=" + category);
        }
    }

    @Override
    public String getProperty(String category, String name) {
        logger.debug("retrieving property: category=" + category + ", name=" + name);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("category", category);
            parameterMap.put("name", name);
            return (String) SqlConfig.getSqlSessionManager().selectOne("Configuration.selectProperty", parameterMap);
        } catch (Exception e) {
            logger.warn("Could not retrieve property: category=" + category + ", name=" + name, e);
        }

        return null;
    }

    @Override
    public void saveProperty(String category, String name, String value) {
        logger.debug("storing property: category=" + category + ", name=" + name);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("category", category);
            parameterMap.put("name", name);
            parameterMap.put("value", value);

            if (getProperty(category, name) == null) {
                SqlConfig.getSqlSessionManager().insert("Configuration.insertProperty", parameterMap);
            } else {
                SqlConfig.getSqlSessionManager().insert("Configuration.updateProperty", parameterMap);
            }

            if (DatabaseUtil.statementExists("Configuration.vacuumConfigurationTable")) {
                SqlConfig.getSqlSessionManager().update("Configuration.vacuumConfigurationTable");
            }
        } catch (Exception e) {
            logger.error("Could not store property: category=" + category + ", name=" + name, e);
        }
    }

    @Override
    public void removeProperty(String category, String name) {
        logger.debug("deleting property: category=" + category + ", name=" + name);

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("category", category);
            parameterMap.put("name", name);
            SqlConfig.getSqlSessionManager().delete("Configuration.deleteProperty", parameterMap);
        } catch (Exception e) {
            logger.error("Could not delete property: category=" + category + ", name=" + name, e);
        }
    }

    @Override
    public String getResources() {
        String resources = getProperty(PROPERTIES_CORE, PROPERTIES_RESOURCES);

        if (StringUtils.isBlank(resources)) {
            ResourcePropertiesList list = new ResourcePropertiesList();

            DirectoryResourceProperties defaultResource = new DirectoryResourceProperties();
            defaultResource.setId(ResourceProperties.DEFAULT_RESOURCE_ID);
            defaultResource.setName(ResourceProperties.DEFAULT_RESOURCE_NAME);
            defaultResource.setDescription("Loads libraries from the custom-lib folder in the Mirth Connect home directory.");
            defaultResource.setIncludeWithGlobalScripts(true);
            defaultResource.setDirectory("custom-lib");

            list.getList().add(defaultResource);
            resources = ObjectXMLSerializer.getInstance().serialize(list);
            saveProperty(PROPERTIES_CORE, PROPERTIES_RESOURCES, resources);
        }

        return resources;
    }

    @Override
    public void setResources(String resources) {
        saveProperty(PROPERTIES_CORE, PROPERTIES_RESOURCES, resources);
    }

    @Override
    public Set<ChannelDependency> getChannelDependencies() {
        String dependenciesXml = getProperty(PROPERTIES_CORE, PROPERTIES_DEPENDENCIES);
        Set<ChannelDependency> dependencies;

        if (StringUtils.isNotBlank(dependenciesXml)) {
            dependencies = ObjectXMLSerializer.getInstance().deserialize(dependenciesXml, Set.class);
        } else {
            dependencies = new HashSet<ChannelDependency>();
            setChannelDependencies(dependencies);
        }

        return dependencies;
    }

    @Override
    public synchronized void setChannelDependencies(Set<ChannelDependency> dependencies) {
        try {
            new ChannelDependencyGraph(dependencies);
        } catch (ChannelDependencyException e) {
            logger.error("Error saving channel dependencies: " + e.getMessage(), e);
            return;
        }

        saveProperty(PROPERTIES_CORE, PROPERTIES_DEPENDENCIES, ObjectXMLSerializer.getInstance().serialize(dependencies));
    }

    @Override
    public Map<String, ChannelMetadata> getChannelMetadata() {
        String channelMetadataXml = getProperty(PROPERTIES_CORE, PROPERTIES_CHANNEL_METADATA);
        Map<String, ChannelMetadata> channelMetadata;

        if (StringUtils.isNotBlank(channelMetadataXml)) {
            channelMetadata = ObjectXMLSerializer.getInstance().deserialize(channelMetadataXml, Map.class);
        } else {
            channelMetadata = new HashMap<String, ChannelMetadata>();
            setChannelMetadata(channelMetadata);
        }

        return channelMetadata;
    }

    @Override
    public void setChannelMetadata(Map<String, ChannelMetadata> channelMetadata) {
        saveProperty(PROPERTIES_CORE, PROPERTIES_CHANNEL_METADATA, ObjectXMLSerializer.getInstance().serialize(channelMetadata));
    }

    @Override
    public void initializeSecuritySettings() {
        try {
            /*
             * Load the encryption settings so that they can be referenced client side.
             */
            encryptionConfig = new EncryptionSettings(ConfigurationConverter.getProperties(mirthConfig));

            File keyStoreFile = new File(mirthConfig.getString("keystore.path"));
            char[] keyStorePassword = mirthConfig.getString("keystore.storepass").toCharArray();
            char[] keyPassword = mirthConfig.getString("keystore.keypass").toCharArray();
            Provider provider = (Provider) Class.forName(encryptionConfig.getSecurityProvider()).newInstance();

            KeyStore keyStore = null;

            // if the current server version is pre-2.2, load the keystore as JKS
            if (MigrationUtil.compareVersions("2.2.0", getServerVersion()) == 1) {
                keyStore = KeyStore.getInstance("JKS");
            } else {
                keyStore = KeyStore.getInstance("JCEKS");
            }

            if (keyStoreFile.exists()) {
                keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);
                logger.debug("found and loaded keystore: " + keyStoreFile.getAbsolutePath());
            } else {
                keyStore.load(null, keyStorePassword);
                logger.debug("keystore file not found, created new one");
            }

            configureEncryption(provider, keyStore, keyPassword);
            generateDefaultCertificate(provider, keyStore, keyPassword);

            // write the kesytore back to the file
            FileOutputStream fos = new FileOutputStream(keyStoreFile);
            keyStore.store(fos, keyStorePassword);
            IOUtils.closeQuietly(fos);
        } catch (Exception e) {
            logger.error("Could not initialize security settings.", e);
        }
    }

    @Override
    public void initializeDatabaseSettings() {
        try {
            databaseConfig = new DatabaseSettings(ConfigurationConverter.getProperties(mirthConfig));

            // dir.base is not included in mirth.properties, so set it manually
            databaseConfig.setDirBase(getBaseDir());

            String password = databaseConfig.getDatabasePassword();

            if (StringUtils.isNotEmpty(password)) {
                ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
                EncryptionSettings encryptionSettings = configurationController.getEncryptionSettings();
                Encryptor encryptor = configurationController.getEncryptor();

                if (encryptionSettings.getEncryptProperties()) {
                    if (StringUtils.startsWith(password, EncryptionSettings.ENCRYPTION_PREFIX)) {
                        String encryptedPassword = StringUtils.removeStart(password, EncryptionSettings.ENCRYPTION_PREFIX);
                        String decryptedPassword = encryptor.decrypt(encryptedPassword);
                        databaseConfig.setDatabasePassword(decryptedPassword);
                    } else if (StringUtils.isNotBlank(password)) {
                        // encrypt the password and write it back to the file
                        String encryptedPassword = EncryptionSettings.ENCRYPTION_PREFIX + encryptor.encrypt(password);
                        mirthConfig.setProperty("database.password", encryptedPassword);

                        /*
                         * Save using a FileOutputStream so that the file will be saved to the
                         * proper location, even if running from the IDE.
                         */
                        File confDir = new File(ControllerFactory.getFactory().createConfigurationController().getConfigurationDir());
                        OutputStream os = new FileOutputStream(new File(confDir, "mirth.properties"));

                        try {
                            mirthConfig.save(os);
                        } finally {
                            IOUtils.closeQuietly(os);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * If we have the encryption key property in the database, that means the previous keystore was
     * of type JKS, so we want to delete it so that a new JCEKS one can be created.
     * 
     * If we migrated from a version prior to 2.2, then the key from the ENCRYTPION_KEY table has
     * been added to the CONFIGURATION table. We want to deserialize it and put it in the new
     * keystore. We also need to delete the property.
     * 
     * NOTE that this method should only execute once.
     */

    public void migrateKeystore() {
        PropertiesConfiguration properties = new PropertiesConfiguration();
        properties.setDelimiterParsingDisabled(true);

        try {
            if (getProperty(PROPERTIES_CORE, "encryption.key") != null) {
                // load the keystore path and passwords
                properties.load(ResourceUtil.getResourceStream(this.getClass(), "mirth.properties"));
                File keyStoreFile = new File(properties.getString("keystore.path"));
                char[] keyStorePassword = properties.getString("keystore.storepass").toCharArray();
                char[] keyPassword = properties.getString("keystore.keypass").toCharArray();

                // delete the old JKS keystore
                keyStoreFile.delete();

                // create and load a new one as type JCEKS
                KeyStore keyStore = KeyStore.getInstance("JCEKS");
                keyStore.load(null, keyStorePassword);

                // deserialize the XML secret key to an Object
                ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                String xml = getProperty(PROPERTIES_CORE, "encryption.key");

                /*
                 * This is a fix to account for an error that occurred when testing migration from
                 * version 1.8.2 to 3.0.0. The key was serialized as an instance of
                 * com.sun.crypto.provider.DESedeKey, but fails to correctly deserialize as an
                 * instance of java.security.KeyRep. The fix below extracts the "<default>" node
                 * from the serialized xml and uses that to deserialize to java.security.KeyRep.
                 * (MIRTH-2552)
                 */
                Document document = new DocumentSerializer().fromXML(xml);
                DonkeyElement root = new DonkeyElement(document.getDocumentElement());
                DonkeyElement keyRep = root.getChildElement("java.security.KeyRep");

                if (keyRep != null) {
                    DonkeyElement defaultElement = keyRep.getChildElement("default");

                    if (defaultElement != null) {
                        defaultElement.setNodeName("java.security.KeyRep");
                        xml = defaultElement.toXml();
                    }
                }

                SecretKey secretKey = serializer.deserialize(xml, SecretKey.class);

                // add the secret key entry to the new keystore
                KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
                keyStore.setEntry(SECRET_KEY_ALIAS, entry, new KeyStore.PasswordProtection(keyPassword));

                // save the keystore to the filesystem
                OutputStream keyStoreOuputStream = new FileOutputStream(keyStoreFile);

                try {
                    keyStore.store(keyStoreOuputStream, keyStorePassword);
                } finally {
                    IOUtils.closeQuietly(keyStoreOuputStream);
                }

                // remove the property from CONFIGURATION
                removeProperty(PROPERTIES_CORE, "encryption.key");

                // reinitialize the security settings
                initializeSecuritySettings();
            }
        } catch (Exception e) {
            logger.error("Error migrating encryption key from database to keystore.", e);
        }
    }

    /**
     * Instantiates the encryptor and digester using the configuration properties. If the properties
     * are not found, reasonable defaults are used.
     * 
     * @param provider
     *            The provider to use (ex. BC)
     * @param keyStore
     *            The keystore from which to load the secret encryption key
     * @param keyPassword
     *            The secret key password
     * @throws Exception
     */
    private void configureEncryption(Provider provider, KeyStore keyStore, char[] keyPassword) throws Exception {
        SecretKey secretKey = null;

        if (!keyStore.containsAlias(SECRET_KEY_ALIAS)) {
            logger.debug("encryption key not found, generating new one");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionConfig.getEncryptionAlgorithm(), provider);
            keyGenerator.init(encryptionConfig.getEncryptionKeyLength());
            secretKey = keyGenerator.generateKey();
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
            keyStore.setEntry(SECRET_KEY_ALIAS, entry, new KeyStore.PasswordProtection(keyPassword));
        } else {
            logger.debug("found encryption key in keystore");
            secretKey = (SecretKey) keyStore.getKey(SECRET_KEY_ALIAS, keyPassword);
        }

        /*
         * Now that we have a secret key, store it in the encryption settings so that we can use it
         * to encryption things client side.
         */
        encryptionConfig.setSecretKey(secretKey.getEncoded());

        encryptor = new KeyEncryptor();
        encryptor.setProvider(provider);
        encryptor.setKey(secretKey);
        encryptor.setFormat(Output.BASE64);

        digester = new Digester();
        digester.setProvider(provider);
        digester.setAlgorithm(encryptionConfig.getDigestAlgorithm());
        digester.setFormat(Output.BASE64);
    }

    /**
     * Checks for an existing certificate to use for secure communication between the server and
     * client. If no certficate exists, this will generate a new one.
     * 
     */
    private void generateDefaultCertificate(Provider provider, KeyStore keyStore, char[] keyPassword) throws Exception {
        final String certificateAlias = "mirthconnect";

        if (!keyStore.containsAlias(certificateAlias)) {
            // Common CA and SSL cert attributes
            Date startDate = new Date(); // time from which certificate is valid
            Date expiryDate = DateUtils.addYears(startDate, 50); // time after which certificate is not valid
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", provider);
            keyPairGenerator.initialize(2048);

            KeyPair caKeyPair = keyPairGenerator.generateKeyPair();
            logger.debug("generated new key pair for CA cert using provider: " + provider.getName());

            // Generate CA cert
            X500Name caSubjectName = new X500Name("CN=Mirth Connect Certificate Authority");
            SubjectPublicKeyInfo caSubjectKey = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(caKeyPair.getPublic().getEncoded()));
            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(caSubjectName, BigInteger.ONE, startDate, expiryDate, caSubjectName, caSubjectKey);
            certBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.basicConstraints, true, new BasicConstraints(0));
            ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider(provider).build(caKeyPair.getPrivate());
            Certificate caCert = new JcaX509CertificateConverter().setProvider(provider).getCertificate(certBuilder.build(sigGen));

            // Generate SSL cert
            KeyPair sslKeyPair = keyPairGenerator.generateKeyPair();
            logger.debug("generated new key pair for SSL cert using provider: " + provider.getName());

            X500Name sslSubjectName = new X500Name("CN=mirth-connect");
            SubjectPublicKeyInfo sslSubjectKey = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(sslKeyPair.getPublic().getEncoded()));
            X509v3CertificateBuilder sslCertBuilder = new X509v3CertificateBuilder(caSubjectName, new BigInteger(50, new SecureRandom()), startDate, expiryDate, sslSubjectName, sslSubjectKey);
            sslCertBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.authorityKeyIdentifier, false, new AuthorityKeyIdentifier(caCert.getEncoded()));
            sslCertBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.subjectKeyIdentifier, false, new SubjectKeyIdentifier(sslKeyPair.getPublic().getEncoded()));

            sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider(provider).build(caKeyPair.getPrivate());
            Certificate sslCert = new JcaX509CertificateConverter().setProvider(provider).getCertificate(sslCertBuilder.build(sigGen));

            logger.debug("generated new certificate with serial number: " + ((X509Certificate) sslCert).getSerialNumber());

            // add the generated SSL cert to the keystore using the key password
            keyStore.setKeyEntry(certificateAlias, sslKeyPair.getPrivate(), keyPassword, new Certificate[] {
                    sslCert });
        } else {
            logger.debug("found certificate in keystore");
        }
    }

    private boolean isDatabaseRunning() {
        Statement statement = null;
        Connection connection = null;
        SqlConfig.getSqlSessionManager().startManagedSession();

        try {
            connection = SqlConfig.getSqlSessionManager().getConnection();
            statement = connection.createStatement();
            statement.execute("SELECT 1 FROM CHANNEL");
            return true;
        } catch (Exception e) {
            logger.warn("could not retrieve status of database", e);
            return false;
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }

    private void saveConfigurationProperties(Map<String, ConfigurationProperty> map) throws ControllerException {
        try {
            PropertiesConfiguration configurationMapProperties = new PropertiesConfiguration();
            configurationMapProperties.setDelimiterParsingDisabled(true);
            configurationMapProperties.setListDelimiter((char) 0);
            configurationMapProperties.clear();

            PropertiesConfigurationLayout layout = configurationMapProperties.getLayout();

            Map<String, ConfigurationProperty> sortedMap = new TreeMap<String, ConfigurationProperty>(String.CASE_INSENSITIVE_ORDER);
            sortedMap.putAll(map);

            for (Entry<String, ConfigurationProperty> entry : sortedMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().getValue();
                String comment = entry.getValue().getComment();

                if (StringUtils.isNotBlank(key)) {
                    configurationMapProperties.addProperty(key, value);
                    layout.setComment(key, StringUtils.isBlank(comment) ? null : comment);
                }
            }

            configurationMapProperties.save(new File(configurationFile));
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public ConnectionTestResponse sendTestEmail(Properties properties) throws Exception {
        String portString = properties.getProperty("port");
        String encryption = properties.getProperty("encryption");
        String host = properties.getProperty("host");
        String timeoutString = properties.getProperty("timeout");
        Boolean authentication = Boolean.parseBoolean(properties.getProperty("authentication"));
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String to = properties.getProperty("toAddress");
        String from = properties.getProperty("fromAddress");

        int port = -1;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Invalid port: \"" + portString + "\"");
        }

        Email email = new SimpleEmail();
        email.setDebug(true);
        email.setHostName(host);
        email.setSmtpPort(port);

        try {
            int timeout = Integer.parseInt(timeoutString);
            email.setSocketTimeout(timeout);
            email.setSocketConnectionTimeout(timeout);
        } catch (NumberFormatException e) {
            // Don't set if the value is invalid
        }

        if ("SSL".equalsIgnoreCase(encryption)) {
            email.setSSLOnConnect(true);
            email.setSslSmtpPort(portString);
        } else if ("TLS".equalsIgnoreCase(encryption)) {
            email.setStartTLSEnabled(true);
        }

        if (authentication) {
            email.setAuthentication(username, password);
        }

        // These have to be set after the authenticator, so that a new mail session isn't created
        ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
        String protocols = properties.getProperty("protocols", StringUtils.join(MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsClientProtocols()), ' '));
        String cipherSuites = properties.getProperty("cipherSuites", StringUtils.join(MirthSSLUtil.getEnabledHttpsCipherSuites(configurationController.getHttpsCipherSuites()), ' '));
        email.getMailSession().getProperties().setProperty("mail.smtp.ssl.protocols", protocols);
        email.getMailSession().getProperties().setProperty("mail.smtp.ssl.ciphersuites", cipherSuites);

        SSLSocketFactory socketFactory = (SSLSocketFactory) properties.get("socketFactory");
        if (socketFactory != null) {
            email.getMailSession().getProperties().put("mail.smtp.ssl.socketFactory", socketFactory);
            if ("SSL".equalsIgnoreCase(encryption)) {
                email.getMailSession().getProperties().put("mail.smtp.socketFactory", socketFactory);
            }
        }

        email.setSubject("Mirth Connect Test Email");

        try {
            for (String toAddress : StringUtils.split(to, ",")) {
                email.addTo(toAddress);
            }

            email.setFrom(from);
            email.setMsg("Receipt of this email confirms that mail originating from this Mirth Connect Server is capable of reaching its intended destination.\n\nSMTP Configuration:\n- Host: " + host + "\n- Port: " + port);

            email.send();
            return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully sent test email to: " + to);
        } catch (EmailException e) {
            return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, e.getMessage());
        }
    }
}
