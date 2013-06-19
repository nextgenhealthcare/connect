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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
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
    private Logger logger = Logger.getLogger(this.getClass());

    private DefaultMigrationController() {}

    @Override
    public void migrate() throws MigrationException {
        runMigrator(new ServerMigrator());
    }
    
    @Override
    public void migrateExtensions() {
        for (PluginMetaData pluginMetaData : extensionController.getPluginMetaData().values()) {
            String migratorClassName = pluginMetaData.getMigratorClass();
            
            if (migratorClassName != null) {
                try {
                    runMigrator((Migrator) Class.forName(migratorClassName).newInstance());
                } catch (Exception e) {
                    logger.error("Failed to run migration for plugin: " + pluginMetaData.getName());
                }
            }
        }
    }
    
    private void runMigrator(Migrator migrator) throws MigrationException {
        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();
        
        try {
            migrator.setConnection(connection);
            migrator.setDatabaseType(configurationController.getDatabaseType());
            migrator.migrate();
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }
    
    @Override
    public void migrateSerializedData() {
        migrateSerializedData("Channel.getSerializedChannelData", "Channel.updateSerializedChannelData", "channel", Channel.class);
        migrateSerializedData("Alert.getAlert", "Alert.updateAlert", "alert", AlertModel.class);
        migrateSerializedData("CodeTemplate.getCodeTemplate", "CodeTemplate.updateCodeTemplate", "codeTemplate", CodeTemplate.class);
    }

    /**
     * It is assumed that for each migratable class that uses this an "id" column exists in the
     * database, which is used as the primary key when updating the row. It's also assumed that for
     * the time being, any additional columns besides the ID and serialized XML (e.g. name,
     * revision) will not change during migration.
     */
    private void migrateSerializedData(String selectQuery, String updateStatement, String serializedColumnName, Class<?> expectedClass) {
        SqlSession session = SqlConfig.getSqlSessionManager().openSession(true);
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();

        try {
            List<Map<String, String>> serializedDataList = session.selectList(selectQuery);

            for (Map<String, String> serializedData : serializedDataList) {
                try {
                    String migratedData = serializer.toXML(serializer.fromXML(serializedData.get(serializedColumnName), expectedClass));
    
                    if (!migratedData.equals(serializedData.get(serializedColumnName))) {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("id", serializedData.get("id"));
                        params.put(serializedColumnName, migratedData);
    
                        session.update(updateStatement, params);
                        logger.info("Migrated " + serializedColumnName + " " + serializedData.get("id"));
                    }
                } catch (Exception e) {
                    logger.error("Failed to migrate " + serializedColumnName + " " + serializedData.get("id"), e);
                }
            }
        } finally {
            session.close();
        }
    }
}
