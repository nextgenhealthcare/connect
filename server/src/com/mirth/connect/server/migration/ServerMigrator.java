package com.mirth.connect.server.migration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.server.util.DatabaseUtil;

public class ServerMigrator extends Migrator {
    private final static String DELTA_SCRIPT_FOLDER = IOUtils.DIR_SEPARATOR + "deltas";
    
    private Logger logger = Logger.getLogger(getClass());

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
                migrator.setDefaultScriptFolder(DELTA_SCRIPT_FOLDER);
                migrator.migrate();
            }

            updateVersion(version);
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
                String databaseType = getDatabaseType();
                String creationScript = IOUtils.toString(getClass().getResourceAsStream("/" + databaseType + "/" + databaseType + "-database.sql"));
                DatabaseUtil.executeScript(creationScript, false);
                updateVersion(Version.getLatest());
            }
        } catch (Exception e) {
            throw new MigrationException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
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
}
