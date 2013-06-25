package com.mirth.connect.server.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class ServerMigrator extends Migrator {
    private Logger logger = Logger.getLogger(getClass());

    public ServerMigrator() {
        setDefaultScriptFolder(IOUtils.DIR_SEPARATOR + "deltas");
    }
    
    @Override
    public void migrate() throws MigrationException {
        Connection connection = getConnection();
        initDatabase(connection);
        Version version = getCurrentVersion();

        if (version == null) {
            version = Version.V0;
        }

        while (version.nextVersionExists()) {
            version = version.getNextVersion();
            Migrator migrator = getMigrator(version);

            if (migrator != null) {
                logger.info("Migrating server to version " + version);
                migrator.setConnection(connection);
                migrator.setDatabaseType(getDatabaseType());
                migrator.setDefaultScriptFolder(getDefaultScriptFolder());
                migrator.migrate();
            }

            updateVersion(version);
        }
    }
    
    @Override
    public void migrateSerializedData() {
        migrateSerializedData("SELECT ID, CHANNEL FROM CHANNEL", "UPDATE CHANNEL SET CHANNEL = ? WHERE ID = ?", Channel.class);
        migrateSerializedData("SELECT ID, ALERT FROM ALERT", "UPDATE ALERT SET ALERT = ? WHERE ID = ?", AlertModel.class);
        migrateSerializedData("SELECT ID, CODE_TEMPLATE FROM CODE_TEMPLATE", "UPDATE CODE_TEMPLATE SET CODE_TEMPLATE = ? WHERE ID = ?", CodeTemplate.class);
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
            case V9: return new LegacyMigrator(9);
            case V3_0_0: return new Migrate3_0_0();
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
        if (!tableExists("CONFIGURATION")) {
            executeScript(IOUtils.DIR_SEPARATOR + getDatabaseType() + IOUtils.DIR_SEPARATOR + getDatabaseType() + "-database.sql");
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

            statement.setString(1, version.toString());
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
        Connection connection = getConnection();
        Statement selectStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;
        
        try {
            selectStatement = connection.createStatement();
            resultSet = selectStatement.executeQuery(selectSql);
            
            while (resultSet.next()) {
                try {
                    String id = resultSet.getString(1);
                    String serializedData = resultSet.getString(2);
                    String migratedData = serializer.toXML(serializer.fromXML(serializedData, expectedClass));
                    
                    if (!migratedData.equals(serializedData)) {
                        updateStatement = connection.prepareStatement(updateSql);
                        updateStatement.setString(1, migratedData);
                        updateStatement.setString(2, id);
                        updateStatement.executeUpdate();
                    }
                } catch (SQLException e) {
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
