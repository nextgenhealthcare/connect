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
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.util.ImportConverter;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.PropertyVerifier;

/**
 * The MigrationController migrates the database to the current version.
 * 
 */
public class DefaultMigrationController extends MigrationController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();

    // singleton pattern
    private static DefaultMigrationController instance = null;

    private DefaultMigrationController() {

    }

    public static MigrationController create() {
        synchronized (DefaultMigrationController.class) {
            if (instance == null) {
                instance = new DefaultMigrationController();
            }

            return instance;
        }
    }

    // TODO: Rewrite with fewer returns
    public void migrate() {
        // check for one of the tables to see if we should run the create script

        Connection conn = null;
        ResultSet resultSet = null;

        try {
            conn = SqlConfig.getSqlMapClient().getDataSource().getConnection();
            // Gets the database metadata
            DatabaseMetaData dbmd = conn.getMetaData();

            // Specify the type of object; in this case we want tables
            String[] types = { "TABLE" };
            String tablePattern = "CONFIGURATION"; // this is a table that has
            // remained unchanged since
            // day 1
            resultSet = dbmd.getTables(null, null, tablePattern, types);

            boolean resultFound = resultSet.next();

            // Some databases only accept lowercase table names
            if (!resultFound) {
                resultSet = dbmd.getTables(null, null, tablePattern.toLowerCase(), types);
                resultFound = resultSet.next();
            }

            // If missing this table we can assume that they don't have the
            // schema installed
            if (!resultFound) {
                createSchema(conn);
                migrateServerProperties();
                return;
            }
        } catch (Exception e) {
            logger.error("Could not create schema on the configured database.", e);
            return;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(conn);
        }

        // otherwise proceed with migration if necessary
        try {
            int newSchemaVersion = configurationController.getSchemaVersion();
            int oldSchemaVersion;

            if (newSchemaVersion == -1)
                return;

            Object result = null;

            try {
                result = SqlConfig.getSqlMapClient().queryForObject("Configuration.getSchemaVersion");
            } catch (SQLException e) {

            }

            if (result == null) {
                oldSchemaVersion = 0;
            } else {
                oldSchemaVersion = ((Integer) result).intValue();
            }

            if (oldSchemaVersion != newSchemaVersion) {
                migrate(oldSchemaVersion, newSchemaVersion);

                if (result == null)
                    SqlConfig.getSqlMapClient().update("Configuration.setInitialSchemaVersion", newSchemaVersion);
                else
                    SqlConfig.getSqlMapClient().update("Configuration.updateSchemaVersion", newSchemaVersion);
            }

            migrateServerProperties();
        } catch (Exception e) {
            logger.error("Could not initialize migration controller.", e);
        }
    }

    public void migrateChannels() {
        try {
            for (Channel channel : channelController.getChannel(null)) {
                if (!channel.getVersion().equals(configurationController.getServerVersion())) {
                    Channel updatedChannel = ImportConverter.convertChannelObject(channel);
                    PropertyVerifier.checkChannelProperties(updatedChannel);
                    PropertyVerifier.checkConnectorProperties(updatedChannel, extensionController.getConnectorMetaData());
                    updatedChannel.setVersion(configurationController.getServerVersion());
                    ServerEventContext context = new ServerEventContext();
                    context.setUserId(ServerEventContext.SYSTEM_USER_ID);
                    channelController.updateChannel(updatedChannel, context, true);
                }
            }
        } catch (Exception e) {
            logger.error("Could not migrate channels.", e);
        }
    }

    public void migrateExtensions() {
        try {
            for (PluginMetaData plugin : extensionController.getPluginMetaData().values()) {
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

                    // if there were no scripts, don't update the schema
                    // version
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
            }
        } catch (Exception e) {
            logger.error("Could not initialize migration controller.", e);
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

    private void createSchema(Connection conn) throws Exception {
        String creationScript = IOUtils.toString(getClass().getResourceAsStream("/" + configurationController.getDatabaseType() + "/" + configurationController.getDatabaseType() + "-database.sql"));
        DatabaseUtil.executeScript(creationScript, true);
    }

    private void migrate(int oldSchemaVersion, int newSchemaVersion) throws Exception {
        while (oldSchemaVersion < newSchemaVersion) {
            int nextSchemaVersion = oldSchemaVersion + 1;

            // gets and executes the correct migration script based on dbtype
            // and versions
            String migrationScript = IOUtils.toString(getClass().getResourceAsStream("/deltas/" + configurationController.getDatabaseType() + "-" + oldSchemaVersion + "-" + nextSchemaVersion + ".sql"));
            DatabaseUtil.executeScript(migrationScript, true);

            // executes any necessary database content migration
            migrateContents(oldSchemaVersion, nextSchemaVersion);

            oldSchemaVersion++;
        }
    }

    /**
     * When migrating Mirth Connect versions, certain configurations saved to
     * the database might also need to be updated. This method uses the schema
     * version migration process to migrate configurations saved in the
     * database.
     */
    private void migrateContents(int oldVersion, int newVersion) throws Exception {

        // This migration is for 2.0.0
        if ((oldVersion == 6) && (newVersion == 7)) {
            // Update the code template scopes and package names
            CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
            try {
                codeTemplateController.updateCodeTemplates(ImportConverter.convertCodeTemplates(codeTemplateController.getCodeTemplate(null)));
            } catch (Exception e) {
                logger.error("Error migrating code templates.", e);
            }

            // Update the global script package names
            try {
                scriptController.setGlobalScripts(ImportConverter.convertGlobalScripts(scriptController.getGlobalScripts()));
            } catch (Exception e) {
                logger.error("Error migrating global scripts.", e);
            }

            // Update the connector package names in the database so the
            // connector objects can serialize to the new package names
            Connection conn = null;
            Statement statement = null;
            ResultSet results = null;

            try {
                conn = SqlConfig.getSqlMapClient().getDataSource().getConnection();
                
                /*
                 * MIRTH-1667: Derby fails if autoCommit is set to true and
                 * there are a large number of results. The following error
                 * occurs: "ERROR 40XD0: Container has been closed"
                 */
                conn.setAutoCommit(false);
                
                statement = conn.createStatement();
                results = statement.executeQuery("SELECT ID, SOURCE_CONNECTOR, DESTINATION_CONNECTORS FROM CHANNEL");

                while (results.next()) {
                    String channelId = results.getString(1);
                    String sourceConnector = results.getString(2);
                    String destinationConnectors = results.getString(3);

                    sourceConnector = sourceConnector.replaceAll("com.webreach.mirth", "com.mirth.connect");
                    destinationConnectors = destinationConnectors.replaceAll("com.webreach.mirth", "com.mirth.connect");

                    PreparedStatement preparedStatement = null;
                    try {
                        preparedStatement = conn.prepareStatement("UPDATE CHANNEL SET SOURCE_CONNECTOR = ?, DESTINATION_CONNECTORS = ? WHERE ID = ?");
                        preparedStatement.setString(1, sourceConnector);
                        preparedStatement.setString(2, destinationConnectors);
                        preparedStatement.setString(3, channelId);

                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                    } catch (Exception ex) {
                        logger.error("Error migrating connectors.", ex);
                    } finally {
                        DbUtils.closeQuietly(preparedStatement);
                    }
                }

                // Since autoCommit was set to false, commit the updates
                conn.commit();

            } catch (Exception e) {
                logger.error("Error migrating connectors.", e);
            } finally {
                DbUtils.closeQuietly(results);
                DbUtils.closeQuietly(statement);
                DbUtils.closeQuietly(conn);
            }
        }
    }

    /*
     * Since we moved the server properties from a file to the database, we need
     * to copy over the previous properties into the database if a file exists
     */
    private void migrateServerProperties() {
        try {
            File propertiesFile = new File(configurationController.getBaseDir() + File.separator + "server.properties");

            if (propertiesFile.exists()) {
                Properties newProperties = configurationController.getServerSettings().getProperties();
                Properties oldProperties = new Properties();

                oldProperties.load(new FileInputStream(propertiesFile));
                newProperties.putAll(oldProperties);
                configurationController.setServerSettings(new ServerSettings(newProperties));
                configurationController.setUpdateSettings(new UpdateSettings(newProperties));

                if (!propertiesFile.delete()) {
                    logger.error("Could not delete existing server.properties file. Please delete it manually.");
                }
            }
        } catch (ControllerException ce) {
            logger.error("Error loading current server properties from database.", ce);
        } catch (IOException ioe) {
            logger.error("Error loading existing server.properties file.", ioe);
        }
    }
}
