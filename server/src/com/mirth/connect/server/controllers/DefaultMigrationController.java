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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.migration.DatabaseSchemaMigrationException;
import com.mirth.connect.server.migration.ServerMigrator;
import com.mirth.connect.server.migration.Version;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultMigrationController extends MigrationController {
    private static DefaultMigrationController instance = null;

    public static MigrationController create() {
        synchronized (DefaultMigrationController.class) {
            if (instance == null) {
                instance = new DefaultMigrationController();
            }

            return instance;
        }
    }
    
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private Logger logger = Logger.getLogger(this.getClass());

    private DefaultMigrationController() {}

    @Override
    public void migrate() throws DatabaseSchemaMigrationException {
        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();
        
        try {
            initDatabase(connection);
            Version version = getVersion();
    
            while (version.nextVersionExists()) {
                version = version.getNextVersion();
                ServerMigrator migrator = version.getMigrator();
                
                if (migrator != null) {
                    logger.info("Migrating database to version " + version);
                    migrator.setConnection(connection);
                    migrator.setDatabaseType(configurationController.getDatabaseType());
                    migrator.migrate();
                }
                
                updateVersion(version);
            }
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }
    
    
    /**
     * Builds the database schema on the connected database if it does not exist
     */
    private void initDatabase(Connection connection) throws DatabaseSchemaMigrationException {
        // Check for one of the tables to see if we should run the create script
        ResultSet resultSet = null;

        try {
            // Gets the database metadata
            DatabaseMetaData dbmd = connection.getMetaData();

            // Specify the type of object; in this case we want tables
            String[] types = { "TABLE" };
            // This is a table that has remained unchanged since day 1
            String tablePattern = "CONFIGURATION";

            resultSet = dbmd.getTables(null, null, tablePattern, types);

            boolean resultFound = resultSet.next();

            // Some databases only accept lowercase table names
            if (!resultFound) {
                resultSet = dbmd.getTables(null, null, tablePattern.toLowerCase(), types);
                resultFound = resultSet.next();
            }

            // If missing this table we can assume that they don't have the schema installed
            if (!resultFound) {
                String creationScript = IOUtils.toString(getClass().getResourceAsStream("/" + configurationController.getDatabaseType() + "/" + configurationController.getDatabaseType() + "-database.sql"));
                DatabaseUtil.executeScript(creationScript, false);
                updateVersion(Version.getLatest());
            }
        } catch (Exception e) {
            throw new DatabaseSchemaMigrationException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
        }
    }
    
    private Version getVersion() {
        String version = (String) SqlConfig.getSqlSessionManager().selectOne("Configuration.getSchemaVersion");

        if (version == null) {
            version = "0";
        }

        return Version.fromString(version);
    }
    
    private void updateVersion(Version version) throws DatabaseSchemaMigrationException {
        SqlSession session = null;
        
        try {
            session = SqlConfig.getSqlSessionManager().openSession(true);
            Object result = SqlConfig.getSqlSessionManager().selectOne("Configuration.getSchemaVersion");
            
            if (result == null) {
                SqlConfig.getSqlSessionManager().insert("Configuration.setInitialSchemaVersion", version.toString());
            } else {
                SqlConfig.getSqlSessionManager().update("Configuration.updateSchemaVersion", version.toString());
            }
        } catch (Exception e) {
            throw new DatabaseSchemaMigrationException("Failed to update database version information.", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    @Override
    public void migrateExtensions() {
        for (PluginMetaData plugin : extensionController.getPluginMetaData().values()) {
            try {
                Properties pluginProperties = extensionController.getPluginProperties(plugin.getName());
                int baseSchemaVersion = -1;

                if (pluginProperties.containsKey("schema")) {
                    baseSchemaVersion = Integer.parseInt(pluginProperties.getProperty("schema", "-1"));
                }

                if (plugin.getSqlScript() != null) {
                    File pluginSqlScriptFile = new File(ExtensionController.getExtensionsPath() + plugin.getPath() + File.separator + plugin.getSqlScript());
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pluginSqlScriptFile);
                    TreeMap<Integer, String> scripts = getDeltaScriptsForVersion(baseSchemaVersion, document);
                    List<String> scriptList = DatabaseUtil.joinSqlStatements(scripts.values());

                    /*
                     * If there are no scripts, that means that the database
                     * schema hasn't changed, so we won't update the schema
                     * version property.
                     */
                    if (!scriptList.isEmpty()) {
                        DatabaseUtil.executeScript(scriptList, false);
                        int maxSchemaVersion = -1;

                        for (Entry<Integer, String> entry : scripts.entrySet()) {
                            int key = entry.getKey().intValue();

                            if (key > maxSchemaVersion) {
                                maxSchemaVersion = key;
                            }
                        }

                        pluginProperties.setProperty("schema", String.valueOf(maxSchemaVersion));
                        extensionController.setPluginProperties(plugin.getName(), pluginProperties);
                    }
                }
            } catch (Exception e) {
                logger.error("Error migrating extension: " + plugin.getName(), e);
            }
        }
    }
    
    private TreeMap<Integer, String> getDeltaScriptsForVersion(int currentVersion, Document document) throws Exception {
        TreeMap<Integer, String> deltaScripts = new TreeMap<Integer, String>();
        NodeList diffNodes = document.getElementsByTagName("diff");
        String databaseType = configurationController.getDatabaseType();

        for (int i = 0; i < diffNodes.getLength(); i++) {
            Node versionAttribute = diffNodes.item(i).getAttributes().getNamedItem("version");

            if (versionAttribute != null) {
                int scriptVersion = Integer.parseInt(versionAttribute.getTextContent());

                if (scriptVersion > currentVersion) {
                    NodeList scriptNodes = ((Element) diffNodes.item(i)).getElementsByTagName("script");

                    if (scriptNodes.getLength() == 0) {
                        throw new Exception("Missing script element for version: " + scriptVersion);
                    }

                    for (int j = 0; j < scriptNodes.getLength(); j++) {
                        Node scriptNode = scriptNodes.item(j);
                        Node scriptNodeAttribute = scriptNode.getAttributes().getNamedItem("type");
                        String[] dbTypes = scriptNodeAttribute.getTextContent().split(",");

                        for (int k = 0; k < dbTypes.length; k++) {
                            if (dbTypes[k].equals("all") || dbTypes[k].equals(databaseType)) {
                                deltaScripts.put(new Integer(scriptVersion), scriptNode.getTextContent());
                            }
                        }
                    }
                }
            }
        }

        return deltaScripts;
    }
    
    @Override
    public void migrateSerializedData() {
        migrateSerializedData("Channel.getSerializedChannelData", "Channel.updateSerializedChannelData", "channel", Channel.class);
        migrateSerializedData("Alert.getAlert", "Alert.updateAlert", "alert", AlertModel.class);
        migrateSerializedData("CodeTemplate.getCodeTemplate", "CodeTemplate.updateCodeTemplate", "codeTemplate", CodeTemplate.class);
    }

    /**
     * It is assumed that for each migratable class that uses this an "id" column exists in the
     * database, which is used as the primary key when updating the row. It's also assumed that for
     * the time being, any additional columns besides the ID and serialized XML (e.g. name,
     * revision) will not change during migration.
     */
    private void migrateSerializedData(String selectQuery, String updateStatement, String serializedColumnName, Class<?> expectedClass) {
        SqlSession session = SqlConfig.getSqlSessionManager().openSession(true);
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

        try {
            List<Map<String, String>> serializedDataList = session.selectList(selectQuery);

            for (Map<String, String> serializedData : serializedDataList) {
                try {
                    String migratedData = serializer.toXML(serializer.fromXML(serializedData.get(serializedColumnName), expectedClass));
    
                    if (!migratedData.equals(serializedData.get(serializedColumnName))) {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("id", serializedData.get("id"));
                        params.put(serializedColumnName, migratedData);
    
                        session.update(updateStatement, params);
                        logger.info("Migrated " + serializedColumnName + " " + serializedData.get("id"));
                    }
                } catch (Exception e) {
                    logger.error("Failed to migrate " + serializedColumnName + " " + serializedData.get("id"), e);
                }
            }
        } finally {
            session.close();
        }
    }
}
