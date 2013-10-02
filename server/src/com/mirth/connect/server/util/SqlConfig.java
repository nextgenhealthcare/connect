/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.DatabaseSettings;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class SqlConfig {
    private static SqlSessionFactory sqlSessionfactory;
    private static SqlSessionManager sqlSessionManager = null;

    private SqlConfig() {}

    public static SqlSessionManager getSqlSessionManager() {
        synchronized (SqlConfig.class) {
            if (sqlSessionManager == null) {
                init();
            }
            return sqlSessionManager;
        }
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionfactory;
    }

    /**
     * This method loads the MyBatis SQL config file for the database in use,
     * then appends sqlMap entries from any installed plugins
     */
    public static void init() {
        try {
            LogFactory.useLog4JLogging();
            System.setProperty("derby.stream.error.method", "com.mirth.connect.server.Mirth.getNullOutputStream");

            DatabaseSettings databaseSettings = ControllerFactory.getFactory().createConfigurationController().getDatabaseSettings();

            BufferedReader br = new BufferedReader(Resources.getResourceAsReader("SqlMapConfig.xml"));

            // parse the SqlMapConfig (ignoring the DTD)
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(br));

            addPluginSqlMaps(databaseSettings.getDatabase(), new DonkeyElement(document.getDocumentElement()).getChildElement("mappers"));

            DocumentSerializer docSerializer = new DocumentSerializer();
            Reader reader = new StringReader(docSerializer.toXML(document));

            sqlSessionfactory = new SqlSessionFactoryBuilder().build(reader, databaseSettings.getProperties());
            sqlSessionManager = SqlSessionManager.newInstance(sqlSessionfactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addPluginSqlMaps(String database, DonkeyElement sqlMapConfigElement) throws Exception {
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        Map<String, PluginMetaData> plugins = extensionController.getPluginMetaData();

        if (MapUtils.isNotEmpty(plugins)) {
            for (String pluginName : plugins.keySet()) {
                PluginMetaData pmd = plugins.get(pluginName);

                if (extensionController.isExtensionEnabled(pluginName)) {
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
                            Element sqlMapElement = sqlMapConfigElement.addChildElement("mapper");
                            sqlMapElement.setAttribute("url", sqlMapConfigFile.toURI().toURL().toString());
                        } else {
                            throw new RuntimeException("SQL map file not found for database: " + database);
                        }
                    }
                }
            }
        }
    }
}