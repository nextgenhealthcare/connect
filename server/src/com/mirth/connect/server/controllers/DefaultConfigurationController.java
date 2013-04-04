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
import java.io.OutputStream;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.commons.encryption.Digester;
import com.mirth.commons.encryption.Encryptor;
import com.mirth.commons.encryption.KeyEncryptor;
import com.mirth.commons.encryption.Output;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.DatabaseSettings;
import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.sqlmap.extensions.MapResultHandler;
import com.mirth.connect.server.tools.ClassPathResource;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.PasswordRequirementsChecker;
import com.mirth.connect.server.util.ResourceUtil;
import com.mirth.connect.server.util.SqlConfig;

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
    private static DatabaseSettings databaseConfig;

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
    public DatabaseSettings getDatabaseSettings() throws ControllerException {
        return databaseConfig;
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

        // If the database isn't running or the engine isn't running (only if it isn't starting) return STATUS_UNAVAILABLE.
        // If it's starting, return STATUS_ENGINE_STARTING. All other cases return STATUS_OK.
        if (!isDatabaseRunning() || (!ControllerFactory.getFactory().createEngineController().isRunning() && !isEngineStarting)) {
            return STATUS_UNAVAILABLE;
        } else if (isEngineStarting) {
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
    public void setServerConfiguration(ServerConfiguration serverConfiguration) throws StartException, StopException, ControllerException, InterruptedException {
        ChannelController channelController = ControllerFactory.getFactory().createChannelController();
        AlertController alertController = ControllerFactory.getFactory().createAlertController();
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
        EngineController engineController = ControllerFactory.getFactory().createEngineController();

        if (serverConfiguration.getChannels() != null) {
            // Undeploy all channels before updating or removing them
            engineController.undeployChannels(engineController.getDeployedIds(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);

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
//                PropertyVerifier.checkChannelProperties(channel);
//                PropertyVerifier.checkConnectorProperties(channel, ControllerFactory.getFactory().createExtensionController().getConnectorMetaData());
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
        engineController.redeployAllChannels();
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
            MapResultHandler<String, String> mapResultHandler = new MapResultHandler<String, String>("name", "value");
            SqlConfig.getSqlSessionManager().select("Configuration.selectPropertiesForCategory", category, mapResultHandler);
            Map<String, String> result = mapResultHandler.getMap();

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
    public void initializeSecuritySettings() {
        try {
            /*
             * Load the encryption settings so that they can be referenced
             * client side.
             */
            encryptionConfig = new EncryptionSettings(ConfigurationConverter.getProperties(mirthConfig));

            File keyStoreFile = new File(mirthConfig.getString("keystore.path"));
            char[] keyStorePassword = mirthConfig.getString("keystore.storepass").toCharArray();
            char[] keyPassword = mirthConfig.getString("keystore.keypass").toCharArray();
            Provider provider = (Provider) Class.forName(encryptionConfig.getSecurityProvider()).newInstance();

            KeyStore keyStore = null;

            // if the current server version is pre-2.2, load the keystore as JKS
            if (compareVersions("2.2.0", getServerVersion()) == 1) {
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

            generateDefaultTrustStore();
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
                         * Save using a FileOutputStream so that the file will
                         * be saved to the proper location, even if running from
                         * the IDE.
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
                ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                SecretKey secretKey = (SecretKey) serializer.fromXML(getProperty(PROPERTIES_CORE, "encryption.key"));

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
            // Common CA and SSL cert attributes
            Date startDate = new Date(); // time from which certificate is valid
            Date expiryDate = DateUtils.addYears(startDate, 50); // time after which certificate is not valid
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", provider);
            keyPairGenerator.initialize(2048);

            KeyPair caKeyPair = keyPairGenerator.generateKeyPair();
            logger.debug("generated new key pair for CA cert using provider: " + provider.getName());

            // Generate CA cert
            X509V3CertificateGenerator caCertGen = new X509V3CertificateGenerator();
            X500Principal caSubjectName = new X500Principal("CN=Mirth Connect Certificate Authority");

            caCertGen.setSerialNumber(BigInteger.ONE);
            caCertGen.setIssuerDN(caSubjectName);
            caCertGen.setNotBefore(startDate);
            caCertGen.setNotAfter(expiryDate);
            caCertGen.setSubjectDN(caSubjectName); // same as issuer
            caCertGen.setPublicKey(caKeyPair.getPublic());
            caCertGen.setSignatureAlgorithm("SHA1withRSA");

            caCertGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(0)); // CA:TRUE

            X509Certificate caCert = caCertGen.generate(caKeyPair.getPrivate()); // note: private key of CA

            // Generate SSL cert
            KeyPair sslKeyPair = keyPairGenerator.generateKeyPair();
            logger.debug("generated new key pair for SSL cert using provider: " + provider.getName());

            X509V3CertificateGenerator sslCertGen = new X509V3CertificateGenerator();
            X500Principal sslSubjectName = new X500Principal("CN=mirth-connect");

            sslCertGen.setSerialNumber(new BigInteger(50, new Random())); // serial number for certificate
            sslCertGen.setIssuerDN(caCert.getSubjectX500Principal());
            sslCertGen.setNotBefore(startDate);
            sslCertGen.setNotAfter(expiryDate);
            sslCertGen.setSubjectDN(sslSubjectName);
            sslCertGen.setPublicKey(sslKeyPair.getPublic());
            sslCertGen.setSignatureAlgorithm("SHA1withRSA");

            sslCertGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
            sslCertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(sslKeyPair.getPublic()));

            X509Certificate sslCert = sslCertGen.generate(caKeyPair.getPrivate()); // note: private key of CA
            logger.debug("generated new certificate with serial number: " + sslCert.getSerialNumber());

            // add the generated SSL cert to the keystore using the key password
            keyStore.setKeyEntry(certificateAlias, sslKeyPair.getPrivate(), keyPassword, new Certificate[] { sslCert });
        } else {
            logger.debug("found certificate in keystore");
        }
    }

    /**
     * Checks for the existance of a trust store. If one does not exist, it will
     * create a new one.
     * 
     */
    private void generateDefaultTrustStore() throws Exception {
        File trustStoreFile = new File(mirthConfig.getString("truststore.path"));
        char[] trustStorePassword = mirthConfig.getString("truststore.storepass").toCharArray();
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

    /**
     * Compares two versions strings (ex. 1.6.1.2335).
     * 
     * @param version1
     * @param version2
     * @return -1 if version1 < version2,
     *         1 if version1 > version2,
     *         0 if version1 = version2
     */
    private int compareVersions(String version1, String version2) {
        if ((version1 == null) && (version2 == null)) {
            return 0;
        } else if ((version1 != null) && (version2 == null)) {
            return 1;
        } else if ((version1 == null) && (version2 != null)) {
            return -1;
        } else {
            String[] numbers1 = normalizeVersion(version1, 3).split("\\.");
            String[] numbers2 = normalizeVersion(version2, 3).split("\\.");

            for (int i = 0; i < numbers1.length; i++) {
                if (Integer.valueOf(numbers1[i]) < Integer.valueOf(numbers2[i])) {
                    return -1;
                } else if (Integer.valueOf(numbers1[i]) > Integer.valueOf(numbers2[i])) {
                    return 1;
                }
            }
        }

        return 0;
    }

    private String normalizeVersion(String version, int length) {
        List<String> numbers = new ArrayList<String>(Arrays.asList(version.split("\\.")));

        while (numbers.size() < length) {
            numbers.add("0");
        }

        StringBuilder builder = new StringBuilder();

        for (Iterator<String> iterator = numbers.iterator(); iterator.hasNext();) {
            String number = iterator.next();

            if (iterator.hasNext()) {
                builder.append(number + ".");
            } else {
                builder.append(number);
            }
        }

        return builder.toString();
    }

    private boolean isDatabaseRunning() {
        Statement statement = null;
        Connection connection = null;
        SqlConfig.getSqlSessionManager().startManagedSession();

        try {
            connection = SqlConfig.getSqlSessionManager().getConnection();
            statement = connection.createStatement();
            statement.execute("SELECT 1 FROM channels");
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
}
