package com.mirth.connect.server.migration;

/**
 * A schema migration exception occurs if the database schema could not be migrated to a newer version.
 */
public class MigrationException extends Exception {
    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(Throwable cause) {
        super(cause);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
