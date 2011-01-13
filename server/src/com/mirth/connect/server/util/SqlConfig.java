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
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.MapUtils;
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
import com.mirth.connect.util.PropertyLoader;

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
        databaseDriverMap.put("sqlserver2005", "net.sourceforge.jtds.jdbc.Driver");
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

                    Properties props = PropertyLoader.loadProperties("mirth");
                    String database = props.getProperty("database");
                    BufferedReader br = new BufferedReader(Resources.getResourceAsReader("SqlMapConfig.xml"));

                    // Parse the SqlMapConfig (ignoring the DTD)
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
                                if (pmd.getSqlMapConfigs().get(database) != null) {
                                    String sqlMapConfigPath = ExtensionController.getExtensionsPath() + pmd.getPath() + File.separator + pmd.getSqlMapConfigs().get(database);
                                    Element sqlMapElement = document.createElement("sqlMap");
                                    sqlMapElement.setAttribute("url", new File(sqlMapConfigPath).toURI().toURL().toString());
                                    sqlMapConfigElement.appendChild(sqlMapElement);
                                } else {
                                    throw new RuntimeException("SQL map file not found for database: " + database);
                                }
                            }
                        }
                    }

                    DocumentSerializer docSerializer = new DocumentSerializer();
                    Reader reader = new StringReader(docSerializer.toXML(document));
                    
                    if (!props.containsKey("database.driver") || StringUtils.isBlank(props.getProperty("database.driver"))) {
                        props.setProperty("database.driver", MapUtils.getString(databaseDriverMap, database));    
                    }
                    
                    props.setProperty("dir.base", ControllerFactory.getFactory().createConfigurationController().getBaseDir());
                    sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader, props);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return sqlMapClient;
        }
    }
}