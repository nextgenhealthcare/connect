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
import java.io.InputStream;
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
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class SqlConfig {
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
                    PropertiesConfiguration properties = new PropertiesConfiguration();
                    InputStream is = ResourceUtil.getResourceStream(SqlMapClient.class, "mirth.properties");
                    properties.setDelimiterParsingDisabled(true);
                    properties.load(is);
                    IOUtils.closeQuietly(is);

                    String database = properties.getString("database");
                    BufferedReader br = new BufferedReader(Resources.getResourceAsReader("SqlMapConfig.xml"));

                    // parse the SqlMapConfig (ignoring the DTD)
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    Document document = factory.newDocumentBuilder().parse(new InputSource(br));
                    Element sqlMapConfigElement = document.getDocumentElement();

                    Map<String, PluginMetaData> plugins = ControllerFactory.getFactory().createExtensionController().getPluginMetaData();

                    // add custom mappings from plugins
                    if (MapUtils.isNotEmpty(plugins)) {
                        for (String pluginName : plugins.keySet()) {
                            PluginMetaData pmd = plugins.get(pluginName);

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

                    DocumentSerializer docSerializer = new DocumentSerializer();
                    Reader reader = new StringReader(docSerializer.toXML(document));

                    // if a database driver is not being set, use the default
                    if (!properties.containsKey("database.driver") || StringUtils.isBlank(properties.getString("database.driver"))) {
                        properties.setProperty("database.driver", MapUtils.getString(databaseDriverMap, database));
                    }

                    /*
                     * MIRTH-1749: in case someone comments out the username and
                     * password properties
                     */
                    if (!properties.containsKey("database.username")) {
                        properties.setProperty("database.username", StringUtils.EMPTY);
                    }

                    if (!properties.containsKey("database.password")) {
                        properties.setProperty("database.password", StringUtils.EMPTY);
                    }

                    properties.setProperty("dir.base", ControllerFactory.getFactory().createConfigurationController().getBaseDir());
                    sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader, ConfigurationConverter.getProperties(properties));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return sqlMapClient;
        }
    }
}