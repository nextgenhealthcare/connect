/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.server.migration.MigrationException;
import com.mirth.connect.server.migration.Migrator;
import com.mirth.connect.server.migration.ServerMigrator;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultMigrationController extends MigrationController {
    private static DefaultMigrationController instance = null;

    public static MigrationController create() {
        synchronized (DefaultMigrationController.class) {
            if (instance == null) {
                instance = new DefaultMigrationController();
            }

            return instance;
        }
    }

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private ServerMigrator serverMigrator;
    private Collection<Migrator> pluginMigrators;
    private Logger logger = Logger.getLogger(this.getClass());

    public DefaultMigrationController() {
        serverMigrator = new ServerMigrator();
    }

    @Override
    public void migrateConfiguration(PropertiesConfiguration configuration) throws MigrationException {
        serverMigrator.migrateConfiguration(configuration);
    }

    private void initPluginMigrators() {
        if (pluginMigrators == null) {
            pluginMigrators = new ArrayList<Migrator>();

            for (PluginMetaData pluginMetaData : extensionController.getPluginMetaData().values()) {
                String migratorClassName = pluginMetaData.getMigratorClass();

                if (migratorClassName != null) {
                    try {
                        Migrator migrator = (Migrator) Class.forName(migratorClassName).newInstance();
                        migrator.setDefaultScriptPath("extensions/" + pluginMetaData.getPath());
                        pluginMigrators.add(migrator);
                    } catch (Exception e) {
                        logger.error("Failed to run migration for plugin: " + pluginMetaData.getName());
                    }
                }
            }
        }
    }

    @Override
    public void migrate() throws MigrationException {
        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();

        try {
            serverMigrator.setConnection(connection);
            serverMigrator.setDatabaseType(configurationController.getDatabaseType());
            serverMigrator.migrate();
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }

    @Override
    public void migrateExtensions() {
        initPluginMigrators();

        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();

        try {
            for (Migrator migrator : pluginMigrators) {
                try {
                    migrator.setConnection(connection);
                    migrator.setDatabaseType(configurationController.getDatabaseType());
                    migrator.migrate();
                } catch (MigrationException e) {
                    // TODO return a list of the extensions that failed so that they can be automatically disabled?
                    logger.error("Failed to migrate extension", e);
                }
            }
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }

    @Override
    public void migrateSerializedData() {
        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();

        try {
            serverMigrator.setConnection(connection);
            serverMigrator.setDatabaseType(configurationController.getDatabaseType());
            serverMigrator.migrateSerializedData();

            initPluginMigrators();

            for (Migrator migrator : pluginMigrators) {
                try {
                    migrator.setConnection(connection);
                    migrator.setDatabaseType(configurationController.getDatabaseType());
                    migrator.migrateSerializedData();
                } catch (MigrationException e) {
                    logger.error("Failed to migrate serialized data for plugin", e);
                }
            }
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }
}
