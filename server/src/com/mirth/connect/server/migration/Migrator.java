package com.mirth.connect.server.migration;

public interface Migrator {
    public void migrate() throws DatabaseSchemaMigrationException;
}
