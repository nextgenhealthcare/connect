/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ExportClearable;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.MigrationException;
import com.mirth.connect.server.util.DatabaseUtil;

public class ServerMigrator extends Migrator {
    private Logger logger = Logger.getLogger(getClass());

    public ServerMigrator() {
        setDefaultScriptPath("/deltas");
    }

    @Override
    public void migrate() throws MigrationException {
        Connection connection;
        try {
            connection = getConnection();
        } catch (SQLException e) {
            throw new MigrationException(e);
        }

        initDatabase(connection);
        Version startingVersion = getCurrentVersion();

        if (startingVersion == null) {
            startingVersion = Version.values()[0];
        }
        setStartingVersion(startingVersion);

        Version version = startingVersion.getNextVersion();

        while (version != null) {
            Migrator migrator = getMigrator(version);

            if (migrator != null) {
                logger.info("Migrating server to version " + version);
                migrator.setStartingVersion(startingVersion);
                migrator.setConnection(connection);
                migrator.setDatabaseType(getDatabaseType());
                migrator.setDefaultScriptPath(getDefaultScriptPath());
                migrator.migrate();
            }

            updateVersion(version);
            version = version.getNextVersion();
        }
    }

    @Override
    public void migrateSerializedData() {
        migrateSerializedData("SELECT ID, CHANNEL FROM CHANNEL", "UPDATE CHANNEL SET CHANNEL = ? WHERE ID = ?", Channel.class);
        migrateSerializedData("SELECT ID, ALERT FROM ALERT", "UPDATE ALERT SET ALERT = ? WHERE ID = ?", AlertModel.class);
        migrateSerializedData("SELECT ID, LIBRARY FROM CODE_TEMPLATE_LIBRARY", "UPDATE CODE_TEMPLATE_LIBRARY SET LIBRARY = ? WHERE ID = ?", CodeTemplateLibrary.class);
        migrateSerializedData("SELECT ID, CODE_TEMPLATE FROM CODE_TEMPLATE", "UPDATE CODE_TEMPLATE SET CODE_TEMPLATE = ? WHERE ID = ?", CodeTemplate.class);
    }

    public void migrateConfiguration(PropertiesConfiguration mirthConfig) throws MigrationException {
        Version version = Version.values()[1];

        while (version != null) {
            Migrator migrator = getMigrator(version);

            if (migrator != null && migrator instanceof ConfigurationMigrator) {
                runConfigurationMigrator((ConfigurationMigrator) migrator, mirthConfig, version);
            }

            version = version.getNextVersion();
        }
    }

    private void runConfigurationMigrator(ConfigurationMigrator configurationMigrator, PropertiesConfiguration mirthConfig, Version version) {
        configurationMigrator.updateConfiguration(mirthConfig);

        HashMap<String, Object> addedProperties = new LinkedHashMap<String, Object>();
        Map<String, Object> propertiesToAdd = configurationMigrator.getConfigurationPropertiesToAdd();

        if (propertiesToAdd != null) {
            for (Entry<String, Object> propertyToAdd : propertiesToAdd.entrySet()) {
                if (!mirthConfig.containsKey(propertyToAdd.getKey())) {
                    PropertiesConfigurationLayout layout = mirthConfig.getLayout();
                    String key = propertyToAdd.getKey();
                    Object value;
                    String comment = "";

                    if (propertyToAdd.getValue() instanceof Pair) {
                        // If a pair is used, get both the value and comment
                        Pair<Object, String> pair = (Pair<Object, String>) propertyToAdd.getValue();
                        value = pair.getLeft();
                        comment = pair.getRight();
                    } else {
                        // Only the value was specified
                        value = propertyToAdd.getValue();
                    }

                    mirthConfig.setProperty(key, value);

                    // If this is the first added property, add a general comment about the added properties before it
                    if (addedProperties.isEmpty()) {
                        if (StringUtils.isNotEmpty(comment)) {
                            comment = "\n\n" + comment;
                        }
                        comment = "The following properties were automatically added on startup for version " + version + comment;
                    }

                    if (StringUtils.isNotEmpty(comment)) {
                        // When a comment is specified, always put a blank line before it
                        layout.setBlancLinesBefore(key, 1);
                        layout.setComment(key, comment);
                    }

                    addedProperties.put(key, value);
                }
            }
        }

        List<String> removedProperties = new ArrayList<String>();
        String[] propertiesToRemove = configurationMigrator.getConfigurationPropertiesToRemove();

        if (propertiesToRemove != null) {
            for (String propertyToRemove : propertiesToRemove) {
                if (mirthConfig.containsKey(propertyToRemove)) {
                    mirthConfig.clearProperty(propertyToRemove);
                    removedProperties.add(propertyToRemove);
                }
            }
        }

        if (!addedProperties.isEmpty() || !removedProperties.isEmpty()) {
            if (!addedProperties.isEmpty()) {
                logger.info("Adding properties in mirth.properties: " + addedProperties);
            }

            if (!removedProperties.isEmpty()) {
                logger.info("Removing properties in mirth.properties: " + removedProperties);
            }

            try {
                mirthConfig.save();
            } catch (ConfigurationException e) {
                logger.error("There was an error updating mirth.properties.", e);

                if (!addedProperties.isEmpty()) {
                    logger.error("The following properties should be added to mirth.properties manually: " + addedProperties.toString());
                }

                if (!removedProperties.isEmpty()) {
                    logger.error("The following properties should be removed from mirth.properties manually: " + removedProperties.toString());
                }
            }
        }
    }

    private Migrator getMigrator(Version version) {
        switch (version) {// @formatter:off
            case V0: return new LegacyMigrator(0);
            case V1: return new LegacyMigrator(1);
            case V2: return new LegacyMigrator(2);
            case V3: return new LegacyMigrator(3);
            case V4: return new LegacyMigrator(4);
            case V5: return new LegacyMigrator(5);
            case V6: return new LegacyMigrator(6);
            case V7: return new Migrate2_0_0();
            case V8: return new LegacyMigrator(8);
            case V9: return new Migrate2_2_0();
            case V3_0_0: return new Migrate3_0_0();
            case V3_0_1: return null;
            case V3_0_2: return new Migrate3_0_2();
            case V3_0_3: return null;
            case V3_1_0: return new Migrate3_1_0();
            case V3_1_1: return new Migrate3_1_1();
            case V3_2_0: return new Migrate3_2_0();
            case V3_2_1: return null;
            case V3_2_2: return new Migrate3_2_2();
            case V3_3_0: return new Migrate3_3_0();
            case V3_3_1: return null;
            case V3_3_2: return null;
            case V3_4_0: return new Migrate3_4_0();
            case V3_4_1: return null;
            case V3_4_2: return null;
            case V3_5_0: return new Migrate3_5_0();
            case V3_5_1: return null;
            case V3_5_2: return null;
        } // @formatter:on

        return null;
    }

    /**
     * Builds the database schema on the connected database if it does not exist
     * 
     * @throws MigrationException
     */
    private void initDatabase(Connection connection) throws MigrationException {
        // If missing this table we can assume that they don't have the schema installed
        if (!DatabaseUtil.tableExists(connection, "CONFIGURATION")) {
            executeScript("/" + getDatabaseType() + "/" + getDatabaseType() + "-database.sql");

            /*
             * We must update the password date for the initial user. Previously we let the database
             * set this via CURRENT_TIMESTAMP, however this could create problems if the database is
             * running on a separate machine in a different timezone. (MIRTH-2902)
             */
            PreparedStatement statement = null;

            try {
                statement = getConnection().prepareStatement("UPDATE PERSON_PASSWORD SET PASSWORD_DATE = ?");
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new MigrationException(e);
            } finally {
                DbUtils.closeQuietly(statement);
            }

            updateVersion(Version.getLatest());
        }
    }

    private Version getCurrentVersion() throws MigrationException {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = getConnection().createStatement();
            resultSet = statement.executeQuery("SELECT VERSION FROM SCHEMA_INFO");

            if (resultSet.next()) {
                return Version.fromString(resultSet.getString(1));
            }

            return null;
        } catch (SQLException e) {
            throw new MigrationException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
        }
    }

    private void updateVersion(Version version) throws MigrationException {
        PreparedStatement statement = null;

        try {
            if (getCurrentVersion() == null) {
                statement = getConnection().prepareStatement("INSERT INTO SCHEMA_INFO (VERSION) VALUES (?)");
            } else {
                statement = getConnection().prepareStatement("UPDATE SCHEMA_INFO SET VERSION = ?");
            }

            statement.setString(1, version.getSchemaVersion());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new MigrationException("Failed to update database version information.", e);
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * It is assumed that for each migratable class that uses this an "id" column exists in the
     * database, which is used as the primary key when updating the row. It's also assumed that for
     * the time being, any additional columns besides the ID and serialized XML (e.g. name,
     * revision) will not change during migration.
     */
    private void migrateSerializedData(String selectSql, String updateSql, Class<?> expectedClass) {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        Connection connection = null;
        Statement selectStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            selectStatement = connection.createStatement();
            resultSet = selectStatement.executeQuery(selectSql);

            while (resultSet.next()) {
                try {
                    String id = resultSet.getString(1);
                    String serializedData = resultSet.getString(2);
                    Object obj = serializer.deserialize(serializedData, expectedClass);
                    if (obj instanceof ExportClearable) {
                        ((ExportClearable) obj).clearExportData();
                    }
                    String migratedData = serializer.serialize(obj);

                    if (!migratedData.equals(serializedData)) {
                        updateStatement = connection.prepareStatement(updateSql);
                        updateStatement.setString(1, migratedData);
                        updateStatement.setString(2, id);
                        updateStatement.executeUpdate();
                    }
                } catch (Exception e) {
                    logger.error("Failed to migrate serialized data", e);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to migrate serialized data", e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(selectStatement);
            DbUtils.closeQuietly(updateStatement);
        }
    }
}
