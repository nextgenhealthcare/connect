/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.ibatis.common.logging.LogFactory;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.model.EncryptionSettings;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class SqlConfig {
    private static final String ENCRYPTION_PREFIX = "{enc}";
    private static SqlMapClient sqlMapClient = null;
    private static Map<String, String> databaseDriverMap = null;

    static {
        databaseDriverMap = new HashMap<String, String>();
        databaseDriverMap.put("derby", "org.apache.derby.jdbc.EmbeddedDriver");
        databaseDriverMap.put("mysql", "com.mysql.jdbc.Driver");
        databaseDriverMap.put("oracle", "oracle.jdbc.OracleDriver");
        databaseDriverMap.put("postgres", "org.postgresql.Driver");
        databaseDriverMap.put("sqlserver2000", "net.sourceforge.jtds.jdbc.Driver");
        databaseDriverMap.put("sqlserver", "net.sourceforge.jtds.jdbc.Driver");
    }

    private SqlConfig() {

    }

    /**
     * This method loads the iBatis SQL config file for the database in use,
     * then appends sqlMap entries from any installed plugins, and returns a
     * SQLMapClient.
     * 
     * @return
     */
    public static SqlMapClient getSqlMapClient() {
        synchronized (SqlConfig.class) {
            if (sqlMapClient == null) {
                try {
                    LogFactory.selectLog4JLogging();
                    System.setProperty("derby.stream.error.method", "com.mirth.connect.server.Mirth.getNullOutputStream");

                    // load the database properties
                    PropertiesConfiguration mirthProperties = new PropertiesConfiguration();
                    InputStream is = ResourceUtil.getResourceStream(SqlMapClient.class, "mirth.properties");
                    mirthProperties.setDelimiterParsingDisabled(true);
                    mirthProperties.load(is);
                    IOUtils.closeQuietly(is);

                    String database = mirthProperties.getString("database");
                    BufferedReader br = new BufferedReader(Resources.getResourceAsReader("SqlMapConfig.xml"));

                    // parse the SqlMapConfig (ignoring the DTD)
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    Document document = factory.newDocumentBuilder().parse(new InputSource(br));

                    addPluginSqlMaps(database, document);

                    DocumentSerializer docSerializer = new DocumentSerializer();
                    Reader reader = new StringReader(docSerializer.toXML(document));

                    PropertiesConfiguration databaseProperties = new PropertiesConfiguration();
                    databaseProperties.setProperty("dir.base", ControllerFactory.getFactory().createConfigurationController().getBaseDir());
                    databaseProperties.setProperty("database", database);
                    databaseProperties.setProperty("database.url", mirthProperties.getString("database.url"));

                    // if a database driver is not being set, use the default
                    databaseProperties.setProperty("database.driver", mirthProperties.getString("database.driver", MapUtils.getString(databaseDriverMap, database)));

                    /*
                     * MIRTH-1749: in case someone comments out the username and
                     * password properties
                     */
                    databaseProperties.setProperty("database.username", mirthProperties.getString("database.username", StringUtils.EMPTY));

                    if (!mirthProperties.containsKey("database.password")) {
                        databaseProperties.setProperty("database.password", StringUtils.EMPTY);
                    } else {
                        ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
                        EncryptionSettings encryptionSettings = configurationController.getEncryptionSettings();
                        Encryptor encryptor = configurationController.getEncryptor();

                        if (encryptionSettings.getEncryptProperties()) {
                            if (StringUtils.startsWith(mirthProperties.getString("database.password"), ENCRYPTION_PREFIX)) {
                                String encryptedPassword = StringUtils.removeStart(mirthProperties.getString("database.password"), ENCRYPTION_PREFIX);
                                String decryptedPassword = encryptor.decrypt(encryptedPassword);
                                databaseProperties.setProperty("database.password", decryptedPassword);
                            } else if (StringUtils.isNotBlank(mirthProperties.getString("database.password"))){
                                // first we need to encrypt the plaintext password
                                String decryptedPassword = mirthProperties.getString("database.password");
                                databaseProperties.setProperty("database.password", decryptedPassword);

                                // now encrypt the password and write it back to the file
                                String encryptedPassword = ENCRYPTION_PREFIX + encryptor.encrypt(decryptedPassword);
                                mirthProperties.setProperty("database.password", encryptedPassword);
                                File confDir = new File(ControllerFactory.getFactory().createConfigurationController().getConfigurationDir());
                                OutputStream os = new FileOutputStream(new File(confDir, "mirth.properties"));

                                try {
                                    mirthProperties.save(os);
                                } finally {
                                    IOUtils.closeQuietly(os);
                                }
                            }
                        } else {
                            databaseProperties.setProperty("database.password", mirthProperties.getString("database.password"));
                        }
                    }

                    sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader, ConfigurationConverter.getProperties(databaseProperties));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return sqlMapClient;
        }
    }

    private static void addPluginSqlMaps(String database, Document document) throws Exception {
        Element sqlMapConfigElement = document.getDocumentElement();
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        Map<String, PluginMetaData> plugins = extensionController.getPluginMetaData();

        if (MapUtils.isNotEmpty(plugins)) {
            for (String pluginName : plugins.keySet()) {
                PluginMetaData pmd = plugins.get(pluginName);

                // only add configs for plugins that have some configs defined
                if (pmd.getSqlMapConfigs() != null) {
                    /* get the SQL map for the current database */
                    String pluginSqlMapName = pmd.getSqlMapConfigs().get(database);

                    if (StringUtils.isBlank(pluginSqlMapName)) {
                        /*
                         * if we couldn't find one for the current
                         * database, check for one that works with
                         * all databases
                         */
                        pluginSqlMapName = pmd.getSqlMapConfigs().get("all");
                    }

                    if (StringUtils.isNotBlank(pluginSqlMapName)) {
                        File sqlMapConfigFile = new File(ExtensionController.getExtensionsPath() + pmd.getPath(), pluginSqlMapName);
                        Element sqlMapElement = document.createElement("sqlMap");
                        sqlMapElement.setAttribute("url", sqlMapConfigFile.toURI().toURL().toString());
                        sqlMapConfigElement.appendChild(sqlMapElement);
                    } else {
                        throw new RuntimeException("SQL map file not found for database: " + database);
                    }
                }
            }
        }
    }
}