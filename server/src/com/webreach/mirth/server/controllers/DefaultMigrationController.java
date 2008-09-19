/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.SqlConfig;

/**
 * The MigrationController migrates the database to the current version.
 * 
 * @author geraldb
 * 
 */
public class DefaultMigrationController implements MigrationController {
    private static final String DELTA_FOLDER = "deltas";
    private Logger logger = Logger.getLogger(this.getClass());
    ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    // singleton pattern
    private static DefaultMigrationController instance = null;

    private DefaultMigrationController() {

    }

    public static MigrationController getInstance() {
        synchronized (DefaultMigrationController.class) {
            if (instance == null) {
                instance = new DefaultMigrationController();
            }

            return instance;
        }
    }

    public void migrate() {
        // check for one of the tables to see if we should run the create script
    	
    	Connection conn = null;
        ResultSet resultSet = null;

        try {
        	conn = SqlConfig.getSqlMapClient().getDataSource().getConnection();
            // Gets the database metadata
            DatabaseMetaData dbmd = conn.getMetaData();

            // Specify the type of object; in this case we want tables
            String[] types = { "TABLE" };
            String tablePattern = "CONFIGURATION"; // this is a table that has remained unchanged since day 1
            resultSet = dbmd.getTables(null, null, tablePattern, types);

            boolean resultFound = resultSet.next();

            // Some databases only accept lowercase table names
            if (!resultFound) {
                resultSet = dbmd.getTables(null, null, tablePattern.toLowerCase(), types);
                resultFound = resultSet.next();
            }
            
            // If missing this table we can assume that they don't have the schema installed
            if (!resultFound) { 
            	createSchema(conn);
            	return;
            }
        }
        catch (Exception e) {
            logger.error("Could not create schema on the configured database.", e);
            return;
        } finally {
            DatabaseUtil.close(resultSet);
            DatabaseUtil.close(conn);
        }
        
        // otherwise proceed with migration if necessary    	
    	try {
            int newSchemaVersion = configurationController.getSchemaVersion();
            int oldSchemaVersion;

            if (newSchemaVersion == -1)
                return;

            Object result = null;

            try {
                result = SqlConfig.getSqlMapClient().queryForObject("getSchemaVersion");
            } catch (SQLException e) {

            }

            if (result == null)
                oldSchemaVersion = 0;
            else
                oldSchemaVersion = ((Integer) result).intValue();

            if (oldSchemaVersion == newSchemaVersion)
                return;
            else {
                migrate(oldSchemaVersion, newSchemaVersion);

                if (result == null)
                    SqlConfig.getSqlMapClient().update("setInitialSchemaVersion", newSchemaVersion);
                else
                    SqlConfig.getSqlMapClient().update("updateSchemaVersion", newSchemaVersion);

                try {
                    SqlConfig.getSqlMapClient().update("clearConfiguration");
                    File configurationFile = new File(configurationController.getMuleConfigurationPath());
                    configurationFile.delete();
                    File bootFile = new File(configurationController.getMuleBootPath());
                    bootFile.delete();
                } catch (Exception e) {
                    logger.error("Could not remove previous configuration.", e);
                }
            }
        } catch (Exception e) {
            logger.error("Could not initialize migration controller.", e);
        }
    }
    
    private void createSchema(Connection conn) throws Exception{ 
    	 File baseDir = new File(configurationController.getBaseDir());
    	 String databaseType = configurationController.getDatabaseType();
    	 File creationScript = new File(baseDir.getPath() + System.getProperty("file.separator") + databaseType + "-database.sql");
    	 
    	 DatabaseUtil.executeScript(creationScript, true);
    }
    
    private void migrate(int oldVersion, int newVersion) throws Exception {
        File deltaFolder = new File(ClassPathResource.getResourceURI(DELTA_FOLDER));
        String deltaPath = deltaFolder.getPath() + System.getProperty("file.separator");
        String databaseType = configurationController.getDatabaseType();

        while (oldVersion < newVersion) {
            // gets the correct migration script based on dbtype and versions
            File migrationFile = new File(deltaPath + databaseType + "-" + oldVersion + "-" + ++oldVersion + ".sql");
            DatabaseUtil.executeScript(migrationFile, false);
        }
    }
}
