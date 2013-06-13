package com.mirth.connect.server.migration;

import org.apache.commons.io.IOUtils;

import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.DatabaseUtil;

public class LegacyMigrator implements Migrator {
    private int schemaVersion;
    
    public LegacyMigrator(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    @Override
    public void migrate() throws DatabaseSchemaMigrationException {
        try {
            String databaseType = ControllerFactory.getFactory().createConfigurationController().getDatabaseType();
            String migrationScript = IOUtils.toString(getClass().getResourceAsStream("/deltas/" + databaseType + "-" + (schemaVersion - 1) + "-" + schemaVersion + ".sql"));
            DatabaseUtil.executeScript(migrationScript, false);
        } catch (Exception e) {
            throw new DatabaseSchemaMigrationException(e);
        }
    }
}
