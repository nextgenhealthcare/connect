/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

/**
 * The MigrationController migrates the database to the current version.
 * 
 */
public abstract class MigrationController extends Controller {
    public static MigrationController getInstance() {
        return ControllerFactory.getFactory().createMigrationController();
    }

    public abstract void migrate();

    public abstract void migrateChannels();

    public abstract void migrateExtensions();
}
