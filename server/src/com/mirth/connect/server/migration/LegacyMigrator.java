/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import com.mirth.connect.model.util.MigrationException;


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
