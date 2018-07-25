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
import java.util.Properties;

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

import com.google.inject.Inject;
import com.mirth.connect.donkey.model.DatabaseConstants;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.DatabaseSettings;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class SqlConfig {

    @Inject
    private static volatile SqlConfig instance = null;

    private boolean splitReadWrite = false;
    private boolean writePoolCache = false;

    private SqlSessionFactory sqlSessionfactory;
    private SqlSessionManager sqlSessionManager = null;

    private SqlSessionFactory readOnlySqlSessionfactory;
    private SqlSessionManager readOnlySqlSessionManager = null;

    public static SqlConfig getInstance() {
        SqlConfig sqlConfig = instance;

        if (sqlConfig == null) {
            synchronized (SqlConfig.class) {
                sqlConfig = instance;
                if (sqlConfig == null) {
                    instance = sqlConfig = new SqlConfig();
                }
            }
        }

        return sqlConfig;
    }

    public SqlConfig() {
        init();
    }

    public static SqlSessionManager getSqlSessionManager() {
        return getInstance().getInstanceSqlSessionManager();
    }

    public SqlSessionManager getInstanceSqlSessionManager() {
        return sqlSessionManager;
    }

    public static SqlSessionManager getReadOnlySqlSessionManager() {
        return getInstance().getInstanceReadOnlySqlSessionManager();
    }

    public SqlSessionManager getInstanceReadOnlySqlSessionManager() {
        return readOnlySqlSessionManager;
    }

    public static boolean isSplitReadWrite() {
        return getInstance().isInstanceSplitReadWrite();
    }

    public boolean isInstanceSplitReadWrite() {
        return splitReadWrite;
    }

    public static boolean isWritePoolCache() {
        return getInstance().isInstanceWritePoolCache();
    }

    public boolean isInstanceWritePoolCache() {
        return writePoolCache;
    }

    /**
     * This method loads the MyBatis SQL config file for the database in use, then appends sqlMap
     * entries from any installed plugins
     */
    private void init() {
        try {
            LogFactory.useLog4JLogging();
            System.setProperty("derby.stream.error.method", "com.mirth.connect.server.Mirth.getNullOutputStream");

            DatabaseSettings databaseSettings = ControllerFactory.getFactory().createConfigurationController().getDatabaseSettings();

            sqlSessionfactory = createFactory(databaseSettings.getDatabase(), databaseSettings, null);
            sqlSessionManager = SqlSessionManager.newInstance(sqlSessionfactory);

            if (databaseSettings.isSplitReadWrite()) {
                // SqlMapConfig uses the "database" variable so we may need to overwrite it with the readonly version
                String readOnlyDatabase = StringUtils.defaultIfBlank(databaseSettings.getDatabaseReadOnly(), databaseSettings.getDatabase());
                readOnlySqlSessionfactory = createFactory(readOnlyDatabase, databaseSettings, "readonly");
                readOnlySqlSessionManager = SqlSessionManager.newInstance(readOnlySqlSessionfactory);
                splitReadWrite = true;
            } else {
                readOnlySqlSessionfactory = sqlSessionfactory;
                readOnlySqlSessionManager = sqlSessionManager;
            }

            writePoolCache = databaseSettings.isWritePoolCache();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SqlSessionFactory createFactory(String database, DatabaseSettings databaseSettings, String environment) throws Exception {
        BufferedReader br = new BufferedReader(Resources.getResourceAsReader("SqlMapConfig.xml"));

        // parse the SqlMapConfig (ignoring the DTD)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        Document document = factory.newDocumentBuilder().parse(new InputSource(br));

        addPluginSqlMaps(database, new DonkeyElement(document.getDocumentElement()).getChildElement("mappers"));

        DocumentSerializer docSerializer = new DocumentSerializer();
        Reader reader = new StringReader(docSerializer.toXML(document));

        Properties dbProperties = new Properties();
        dbProperties.putAll(databaseSettings.getProperties());
        dbProperties.setProperty(DatabaseConstants.DATABASE, database);

        if (environment != null) {
            return new SqlSessionFactoryBuilder().build(reader, environment, dbProperties);
        } else {
            return new SqlSessionFactoryBuilder().build(reader, dbProperties);
        }
    }

    protected void addPluginSqlMaps(String database, DonkeyElement sqlMapConfigElement) throws Exception {
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
                             * if we couldn't find one for the current database, check for one that
                             * works with all databases
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