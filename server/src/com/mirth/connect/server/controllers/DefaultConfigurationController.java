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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.commons.encryption.Digester;
import com.mirth.commons.encryption.Encryptor;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.commons.encryption.Output;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.JMXConnection;
import com.mirth.connect.server.util.JMXConnectionFactory;
import com.mirth.connect.server.util.PasswordRequirementsChecker;
import com.mirth.connect.server.util.ResourceUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.PropertyVerifier;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public class DefaultConfigurationController extends ConfigurationController {
    public static final String PROPERTIES_CORE = "core";
    public static final String SECRET_KEY_ALIAS = "encryption";

    private Logger logger = Logger.getLogger(this.getClass());
    private String appDataDir = null;
    private String baseDir = null;
    private static String serverId = null;
    private boolean isEngineStarting = true;
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private PasswordRequirements passwordRequirements;
    private static PropertiesConfiguration versionConfig = new PropertiesConfiguration();
    private static PropertiesConfiguration mirthConfig = new PropertiesConfiguration();
    private static EncryptionSettings encryptionConfig;

    private static KeyEncryptor encryptor = null;
    private static Digester digester = null;

    private static final String CHARSET = "ca.uhn.hl7v2.llp.charset";
    private static final String PROPERTY_TEMP_DIR = "dir.tempdata";
    private static final String PROPERTY_APP_DATA_DIR = "dir.appdata";

    // singleton pattern
    private static DefaultConfigurationController instance = null;

    private DefaultConfigurationController() {

    }

    public static ConfigurationController create() {
        synchronized (DefaultConfigurationController.class) {
            if (instance == null) {
                instance = new DefaultConfigurationController();
                instance.initialize();
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

            checkMirthConfigProperties();

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
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    /*
     * Return the server GUID
     */
    @Override
    public String getServerId() {
        return serverId;
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

    // ast: Get the list of all avaiable encodings for this JVM
    @Override
    public List<String> getAvaiableCharsetEncodings() throws ControllerException {
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
        return new ServerSettings(getPropertiesForGroup(PROPERTIES_CORE));
    }

    @Override
    public EncryptionSettings getEncryptionSettings() throws ControllerException {
        return encryptionConfig;
    }

    @Override
    public void setServerSettings(ServerSettings settings) throws ControllerException {
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
    public int getSchemaVersion() {
        return versionConfig.getInt("schema.version", -1);
    }

    @Override
    public String getBuildDate() {
        return versionConfig.getString("mirth.date");
    }

    @Override
    public int getStatus() {
        logger.debug("getting Mirth status");

        // Check if mule engine is running. First check if it is starting.
        // Also, if it is not running, double-check to see that it was not
        // starting.
        // This double-check is not foolproof, but works in most cases.
        boolean isEngineRunning = false;
        boolean localIsEngineStarting = false;

        if (isEngineStarting()) {
            localIsEngineStarting = true;
        } else {
            JMXConnection jmxConnection = null;

            try {
                jmxConnection = JMXConnectionFactory.createJMXConnection();
                Hashtable<String, String> properties = new Hashtable<String, String>();
                properties.put("type", "control");
                properties.put("name", "MuleService");

                if (!((Boolean) jmxConnection.getAttribute(properties, "Stopped"))) {
                    isEngineRunning = true;
                }
            } catch (Exception e) {
                if (isEngineStarting()) {
                    localIsEngineStarting = true;
                } else {
                    logger.warn("could not retrieve status of engine", e);
                    isEngineRunning = false;
                }
            } finally {
                if (jmxConnection != null) {
                    jmxConnection.close();
                }
            }
        }

        // check if database is running
        boolean isDatabaseRunning = false;
        Statement statement = null;
        Connection connection = null;

        try {
            connection = SqlConfig.getSqlMapClient().getDataSource().getConnection();
            statement = connection.createStatement();
            statement.execute("SELECT 'STATUS_OK' FROM CONFIGURATION");
            isDatabaseRunning = true;
        } catch (Exception e) {
            logger.warn("could not retrieve status of database", e);
            isDatabaseRunning = false;
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }

        // If the database isn't running or the engine isn't running (only if it
        // isn't starting) return STATUS_UNAVAILABLE.
        // If it's starting, return STATUS_ENGINE_STARTING.
        // All other cases return STATUS_OK.
        if (!isDatabaseRunning || (!isEngineRunning && !localIsEngineStarting)) {
            return STATUS_UNAVAILABLE;
        } else if (localIsEngineStarting) {
            return STATUS_ENGINE_STARTING;
        } else {
            return STATUS_OK;
        }
    }

    @Override
    public ServerConfiguration getServerConfiguration() throws ControllerException {
        ChannelController channelController = ControllerFactory.getFactory().createChannelController();
        AlertController alertController = ControllerFactory.getFactory().createAlertController();
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();

        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setChannels(channelController.getChannel(null));
        serverConfiguration.setAlerts(alertController.getAlert(null));
        serverConfiguration.setCodeTemplates(codeTemplateController.getCodeTemplate(null));
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

        return serverConfiguration;
    }

    @Override
    public void setServerConfiguration(ServerConfiguration serverConfiguration) throws ControllerException {
        ChannelController channelController = ControllerFactory.getFactory().createChannelController();
        AlertController alertController = ControllerFactory.getFactory().createAlertController();
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
        EngineController engineController = ControllerFactory.getFactory().createEngineController();
        ChannelStatusController channelStatusController = ControllerFactory.getFactory().createChannelStatusController();

        if (serverConfiguration.getChannels() != null) {
            // Undeploy all channels before updating or removing them
            engineController.undeployChannels(channelStatusController.getDeployedIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);

            // Remove channels that don't exist in the new configuration
            for (Channel channel : channelController.getChannel(null)) {
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
                PropertyVerifier.checkChannelProperties(channel);
                PropertyVerifier.checkConnectorProperties(channel, ControllerFactory.getFactory().createExtensionController().getConnectorMetaData());
                channelController.updateChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
            }
        }

        if (serverConfiguration.getAlerts() != null) {
            alertController.removeAlert(null);
            alertController.updateAlerts(serverConfiguration.getAlerts());
        }

        if (serverConfiguration.getCodeTemplates() != null) {
            codeTemplateController.removeCodeTemplate(null);
            codeTemplateController.updateCodeTemplates(serverConfiguration.getCodeTemplates());
        }

        if (serverConfiguration.getServerSettings() != null) {
            setServerSettings(serverConfiguration.getServerSettings());
        }

        if (serverConfiguration.getUpdateSettings() != null) {
            setUpdateSettings(serverConfiguration.getUpdateSettings());
        }

        if (serverConfiguration.getGlobalScripts() != null) {
            scriptController.setGlobalScripts(serverConfiguration.getGlobalScripts());
        }

        // Set the properties for all plugins in the server configuration,
        // whether or not the plugin is actually installed on this server.
        if (serverConfiguration.getPluginProperties() != null) {
            ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

            for (Entry<String, Properties> pluginEntry : serverConfiguration.getPluginProperties().entrySet()) {
                extensionController.setPluginProperties(pluginEntry.getKey(), pluginEntry.getValue());
            }
        }

        // Redeploy all channels
        engineController.redeployAllChannels(ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
    }

    @Override
    public boolean isEngineStarting() {
        return isEngineStarting;
    }

    @Override
    public void setEngineStarting(boolean isEngineStarting) {
        this.isEngineStarting = isEngineStarting;
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
    public Properties getPropertiesForGroup(String category) {
        logger.debug("retrieving properties: category=" + category);
        Properties properties = new Properties();

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> result = SqlConfig.getSqlMapClient().queryForMap("Configuration.selectPropertiesForCategory", category, "name", "value");

            if (!result.isEmpty()) {
                properties.putAll(result);
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
            SqlConfig.getSqlMapClient().delete("Configuration.deleteProperty", parameterMap);
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
            return (String) SqlConfig.getSqlMapClient().queryForObject("Configuration.selectProperty", parameterMap);
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
                SqlConfig.getSqlMapClient().insert("Configuration.insertProperty", parameterMap);
            } else {
                SqlConfig.getSqlMapClient().insert("Configuration.updateProperty", parameterMap);
            }

            if (DatabaseUtil.statementExists("Configuration.vacuumConfigurationTable")) {
                SqlConfig.getSqlMapClient().update("Configuration.vacuumConfigurationTable");
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
            SqlConfig.getSqlMapClient().delete("Configuration.deleteProperty", parameterMap);
        } catch (Exception e) {
            logger.error("Could not delete property: category=" + category + ", name=" + name, e);
        }
    }

    @Override
    public void initializeSecuritySettings() {
        PropertiesConfiguration properties = new PropertiesConfiguration();
        properties.setDelimiterParsingDisabled(true);

        try {
            properties.load(ResourceUtil.getResourceStream(this.getClass(), "mirth.properties"));

            /*
             * Load the encryption settings so that they can be referenced
             * client side.
             */
            encryptionConfig = new EncryptionSettings(ConfigurationConverter.getProperties(mirthConfig));

            File keyStoreFile = new File(properties.getString("keystore.path"));
            char[] keyStorePassword = properties.getString("keystore.storepass").toCharArray();
            char[] keyPassword = properties.getString("keystore.keypass").toCharArray();
            Provider provider = (Provider) Class.forName(encryptionConfig.getSecurityProvider()).newInstance();

            KeyStore keyStore = null;

            if (properties.containsKey("keystore.type")) {
                keyStore = KeyStore.getInstance(properties.getString("keystore.type"));
            } else {
                keyStore = KeyStore.getInstance("JKS");
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

            generateDefaultTrustStore(properties);
        } catch (Exception e) {
            logger.error("Could not initialize security settings.", e);
        }
    }

    /*
     * If we have the encryption key property in the database, that
     * means the previous keystore was of type JKS, so we want to delete
     * it so that a new JCEKS one can be created.
     * 
     * If we migrated from a version prior to 2.2, then the key from the
     * ENCRYTPION_KEY table has been added to the CONFIGURATION table.
     * We want to deserialize it and put it in the new keystore. We also
     * need to delete the property.
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
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                SecretKey secretKey = (SecretKey) serializer.fromXML(getProperty(PROPERTIES_CORE, "encryption.key"));

                // add the secret key entry to the new keystore
                KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
                keyStore.setEntry(SECRET_KEY_ALIAS, entry, new KeyStore.PasswordProtection(keyPassword));

                // save the keystore to the filesystem
                FileOutputStream fos = new FileOutputStream(keyStoreFile);
                
                try {
                    keyStore.store(fos, keyStorePassword);    
                } finally {
                    IOUtils.closeQuietly(fos);    
                }

                // remove the property from CONFIGURATION
                removeProperty(PROPERTIES_CORE, "encryption.key");

                // save the keystore.type property to the mirth.properties file
                properties.setProperty("keystore.type", "JCEKS");
                properties.save();
                
                // reinitialize the security settings
                initializeSecuritySettings();
            }
        } catch (Exception e) {
            logger.error("Error migrating encryption key from database to keystore.", e);
        }
    }

    /**
     * Instantiates the encryptor and digester using the configuration
     * properties. If the properties are not found, reasonable defaults are
     * used.
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
         * Now that we have a secret key, store it in the encryption settings so
         * that we can use it to encryption things client side.
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
     * Checks for an existing certificate to use for secure communication
     * between the server and client. If no certficate exists, this will
     * generate a new one.
     * 
     */
    private void generateDefaultCertificate(Provider provider, KeyStore keyStore, char[] keyPassword) throws Exception {
        final String certificateAlias = "mirthconnect";

        if (!keyStore.containsAlias(certificateAlias)) {
            // initialize the certificate attributes
            Date startDate = new Date();
            Date expiryDate = DateUtils.addYears(startDate, 50);
            BigInteger serialNumber = new BigInteger(50, new Random());
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA", provider);
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            logger.debug("generated new key pair using provider: " + provider.getName());

            // set the certificate attributes
            X509V1CertificateGenerator certificateGenerator = new X509V1CertificateGenerator();
            X500Principal dnName = new X500Principal("CN=Mirth Connect");
            certificateGenerator.setSerialNumber(serialNumber);
            certificateGenerator.setIssuerDN(dnName);
            certificateGenerator.setNotBefore(startDate);
            certificateGenerator.setNotAfter(expiryDate);
            certificateGenerator.setSubjectDN(dnName); // note: same as issuer
            certificateGenerator.setPublicKey(keyPair.getPublic());
            certificateGenerator.setSignatureAlgorithm("SHA1withDSA");

            // generate the new certificate
            X509Certificate certificate = certificateGenerator.generate(keyPair.getPrivate());
            logger.debug("generated new certificate with serial number: " + certificate.getSerialNumber());

            // add the generated certificate to the keystore using the key password
            keyStore.setKeyEntry(certificateAlias, keyPair.getPrivate(), keyPassword, new Certificate[] { certificate });
        } else {
            logger.debug("found certificate in keystore");
        }
    }

    /**
     * Checks for the existance of a trust store. If one does not exist, it will
     * create a new one.
     * 
     */
    private void generateDefaultTrustStore(PropertiesConfiguration properties) throws Exception {
        File trustStoreFile = new File(properties.getString("truststore.path"));
        char[] trustStorePassword = properties.getString("truststore.storepass").toCharArray();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        if (!trustStoreFile.exists()) {
            trustStore.load(null, trustStorePassword);
            trustStore.store(new FileOutputStream(trustStoreFile), trustStorePassword);
            logger.debug("truststore file not found, creating new one");
        } else {
            logger.debug("truststore file found: " + trustStoreFile.getAbsolutePath());
        }
    }

    /**
     * Check for required properties that have been added since 2.1.1 and add
     * them with their default value if they don't already exist. Also remove
     * properties that are no longer used after 2.1.1.
     */
    private void checkMirthConfigProperties() {
        // Add new required properties if they don't exist
        HashMap<String, Object> propertiesToAdd = new LinkedHashMap<String, Object>();
        propertiesToAdd.put("password.retrylimit", 0);
        propertiesToAdd.put("password.lockoutperiod", 0);
        propertiesToAdd.put("password.expiration", 0);
        propertiesToAdd.put("password.graceperiod", 0);
        propertiesToAdd.put("password.reuseperiod", 0);
        propertiesToAdd.put("password.reuselimit", 0);

        HashMap<String, Object> addedProperties = new LinkedHashMap<String, Object>();

        for (Entry<String, Object> propertyToAdd : propertiesToAdd.entrySet()) {
            if (!mirthConfig.containsKey(propertyToAdd.getKey())) {
                mirthConfig.setProperty(propertyToAdd.getKey(), propertyToAdd.getValue());

                // If this is the first added property, add a blank line and
                // comment before it
                if (addedProperties.isEmpty()) {
                    mirthConfig.getLayout().setBlancLinesBefore(propertyToAdd.getKey(), 1);
                    mirthConfig.getLayout().setComment(propertyToAdd.getKey(), "The following properties were automatically added on startup because they are required.\nIf you recently upgraded Mirth Connect, they may have been added in this version.");
                }

                addedProperties.put(propertyToAdd.getKey(), propertyToAdd.getValue());
            }
        }

        // Remove properties that are no longer used if they exist
        String[] propertiesToRemove = new String[] { "keystore.storetype", "keystore.algorithm", "keystore.alias", "truststore.storetype", "truststore.algorithm" };
        List<String> removedProperties = new ArrayList<String>();

        for (String propertyToRemove : propertiesToRemove) {
            if (mirthConfig.containsKey(propertyToRemove)) {
                mirthConfig.clearProperty(propertyToRemove);
                removedProperties.add(propertyToRemove);
            }
        }

        if (!addedProperties.isEmpty() || !removedProperties.isEmpty()) {
            logger.info("Adding properties in mirth.properties: " + addedProperties);
            logger.info("Removing properties in mirth.properties: " + removedProperties);

            try {
                mirthConfig.save();
            } catch (ConfigurationException e) {
                logger.error("There was an error updating mirth.properties.");
                logger.error("The following properties should be added to mirth.properties manually: " + addedProperties.toString());
                logger.error("The following properties should be removed from mirth.properties manually: " + removedProperties.toString());
            }
        }
    }
}
