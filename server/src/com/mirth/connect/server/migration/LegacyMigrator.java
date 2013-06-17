package com.mirth.connect.server.migration;

import com.mirth.connect.server.migration.ServerMigrator;

public class LegacyMigrator extends ServerMigrator {
    private int schemaVersion;

    public LegacyMigrator(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    @Override
    public void migrate() throws DatabaseSchemaMigrationException {
        executeDeltaScript(getDatabaseType() + "-" + (schemaVersion - 1) + "-" + schemaVersion + ".sql");
    }
}
