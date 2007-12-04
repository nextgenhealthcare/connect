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
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.SqlConfig;

/**
 * The MigrationController migrates the database to the current version.
 * 
 * @author geraldb
 * 
 */
public class MigrationController
{
    private static final String DELTA_FOLDER = "deltas";
    
    private Logger logger = Logger.getLogger(this.getClass());
    private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
    ConfigurationController configurationController = ConfigurationController.getInstance();

    // singleton pattern
    private static MigrationController instance = null;

    private MigrationController()
    {

    }

    public static MigrationController getInstance()
    {
        synchronized (MigrationController.class)
        {
            if (instance == null)
                instance = new MigrationController();

            return instance;
        }
    }

    public void initialize()
    {
        try
        {
            int newSchemaVersion = configurationController.getSchemaVersion();
			int oldSchemaVersion;
			
			if (newSchemaVersion == -1)
				return;
				
            Object result = null;
            
            try
            {
                result = sqlMap.queryForObject("getSchemaVersion");
            }
            catch(SQLException e)
            {
                
            }
            
            if(result == null)
                oldSchemaVersion = 0;
            else
                oldSchemaVersion = ((Integer)result).intValue();
            
            if (oldSchemaVersion == newSchemaVersion)
                return;
            else
            {                
                migrate(oldSchemaVersion, newSchemaVersion);
                
                if(result == null)
                    sqlMap.update("setInitialSchemaVersion", newSchemaVersion);
                else
                    sqlMap.update("updateSchemaVersion", newSchemaVersion);
                
                try
                {
                    sqlMap.update("clearConfiguration");
                    File configurationFile = new File(configurationController.getMuleConfigurationPath());
                    
                    if(configurationFile != null)
                        configurationFile.delete();
                    
                    File bootFile = new File(configurationController.getMuleBootPath());
                    
                    if(bootFile != null)
                    	bootFile.delete();
                }
                catch(Exception e)
                {
                    logger.error("Could not remove previous configuration.", e);
                }
            }
        }
        catch(Exception e)
        {
            logger.error("Could not initialize migration controller.", e);
        }        
    }

    private void migrate(int oldVersion, int newVersion) throws Exception
    {
        File deltaFolder = new File(ClassPathResource.getResourceURI(DELTA_FOLDER));
        String deltaPath = deltaFolder.getPath() + System.getProperty("file.separator");
        String databaseType = configurationController.getDatabaseType();
        
        while (oldVersion < newVersion)
        {
            // gets the correct migration script based on dbtype and versions
            File migrationFile = new File(deltaPath + databaseType + "-" + oldVersion + "-" + ++oldVersion + ".sql");
            DatabaseUtil.executeScript(migrationFile);
        }
    }
}
