package com.mirth.connect.server.migration;

/**
 * A schema migration exception occurs if the database schema could not be migrated to a newer version.
 */
public class DatabaseSchemaMigrationException extends Exception {
    public DatabaseSchemaMigrationException(String message) {
        super(message);
    }

    public DatabaseSchemaMigrationException(Throwable cause) {
        super(cause);
    }

    public DatabaseSchemaMigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
