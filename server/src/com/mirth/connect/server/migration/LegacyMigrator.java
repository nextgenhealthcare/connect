package com.mirth.connect.server.migration;

import com.mirth.connect.server.migration.Migrator;

public class LegacyMigrator extends Migrator {
    private int schemaVersion;

    public LegacyMigrator(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-" + (schemaVersion - 1) + "-" + schemaVersion + ".sql");
    }

    @Override
    public void migrateSerializedData() throws MigrationException {}
}
