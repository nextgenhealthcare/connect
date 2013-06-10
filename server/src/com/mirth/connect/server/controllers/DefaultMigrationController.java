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
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.migration.Migrate2_0_0;
import com.mirth.connect.server.migration.Migrate3_0_0;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MigrationUtil;

/**
 * The MigrationController migrates the database to the current version.
 */
public class DefaultMigrationController extends MigrationController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    public enum Version {
        V0("0"), V1("1"), V2("2"), V3("3"), V4("4"), V5("5"), V6("6"), V7("7"), V8("8"), V9("9"), V3_0_0(
                "3.0.0");

        private String value;

        private Version(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static Version fromString(String value) {
            for (Version version : values()) {
                if (version.toString().equals(value)) {
                    return version;
                }
            }
            return null;
        }
    }

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

    @Override
    public void migrate() {
        // Check for one of the tables to see if we should run the create script
        Connection conn = null;
        ResultSet resultSet = null;
        SqlConfig.getSqlSessionManager().startManagedSession();

        try {
            conn = SqlConfig.getSqlSessionManager().getConnection();

            // Gets the database metadata
            DatabaseMetaData dbmd = conn.getMetaData();

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
                createSchema(conn);
                return;
            }
        } catch (Exception e) {
            logger.error("Could not create schema on the configured database.", e);
            return;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(conn);
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }

        // Otherwise proceed with migration if necessary
        String newVersion = configurationController.getServerVersion();

        try {
            String oldVersion;

            if (newVersion == null) {
                return;
            }

            Object result = SqlConfig.getSqlSessionManager().selectOne("Configuration.getSchemaVersion");

            if (result == null) {
                oldVersion = "0";
            } else {
                oldVersion = (String) result;
            }

            if (compareVersions(oldVersion, newVersion) < 0) {
                migrate(Version.fromString(oldVersion));

                if (result == null) {
                    SqlConfig.getSqlSessionManager().update("Configuration.setInitialSchemaVersion", newVersion);
                } else {
                    SqlConfig.getSqlSessionManager().update("Configuration.updateSchemaVersion", newVersion);
                }
            }
        } catch (Exception e) {
            logger.error("Could not migrate to version " + newVersion, e);
        }
    }

    /**
     * Compares two schema versions to see which one is most recent. Versions 1-9 are the old
     * versions; after version 9 the product version is used.
     * 
     * @return zero if the versions are equal, a negative value if version1 is less than version2,
     *         and a positive value if version1 is greater than version2
     */
    private int compareVersions(String version1, String version2) {
        boolean version1IsOld = version1.matches("\\d");
        boolean version2IsOld = version2.matches("\\d");

        // Deal with old versions first
        if (version1IsOld && !version2IsOld) {
            return -1;
        } else if (!version1IsOld && version2IsOld) {
            return 1;
        } else if (version1IsOld && version2IsOld) {
            return Integer.parseInt(version1) - Integer.parseInt(version2);
        }

        // Assume both versions are new
        return MigrationUtil.compareVersions(version1, version2);
    }

    @Override
    public void migrateChannels() {
        SqlSession session = SqlConfig.getSqlSessionManager().openSession(true);
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

        try {
            List<Map<String, String>> serializedDataList = session.selectList("Channel.getSerializedChannelData");

            for (Map<String, String> serializedData : serializedDataList) {
                String channel = serializer.toXML(serializer.fromXML(serializedData.get("channel"), Channel.class));

                if (!channel.equals(serializedData.get("channel"))) {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("id", serializedData.get("id"));
                    params.put("channel", channel);

                    session.update("Channel.updateSerializedChannelData", params);
                    logger.info("Migrated channel " + serializedData.get("id") + " to version " + ConfigurationController.getInstance().getServerVersion());
                }
            }
        } finally {
            session.close();
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

    private void createSchema(Connection conn) throws Exception {
        String creationScript = IOUtils.toString(getClass().getResourceAsStream("/" + configurationController.getDatabaseType() + "/" + configurationController.getDatabaseType() + "-database.sql"));
        DatabaseUtil.executeScript(creationScript, true);
    }

    private void migrate(Version oldSchemaVersion) throws Exception {
        int oldVersionNumber = Integer.parseInt(oldSchemaVersion.toString());

        switch (oldSchemaVersion) {
            case V0:
            case V1:
            case V2:
            case V3:
            case V4:
            case V5:
            case V6:
                Migrate2_0_0.migrate(oldVersionNumber);
                oldVersionNumber = 7;
            case V7:
            case V8:
            case V9:
                Migrate3_0_0.migrate(oldVersionNumber);
            case V3_0_0:
        }
    }
}
