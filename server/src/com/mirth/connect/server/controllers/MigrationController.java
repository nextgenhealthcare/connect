/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.mirth.connect.server.migration.MigrationException;

/**
 * The MigrationController migrates the database to the current version.
 * 
 */
public abstract class MigrationController extends Controller {
    public static MigrationController getInstance() {
        return ControllerFactory.getFactory().createMigrationController();
    }

    /**
     * Runs migration procedures when a version change is detected
     * 
     * @throws MigrationException Thrown if an error occurred while attempting to migrate the database schema
     */
    public abstract void migrate() throws MigrationException;

    public abstract void migrateSerializedData();

    public abstract void migrateExtensions();
    
    public abstract void migrateConfiguration(PropertiesConfiguration configuration) throws MigrationException;
}
