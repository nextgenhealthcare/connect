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
import com.webreach.mirth.server.util.SqlConfig;

/**
 * The MigrationController migrates the database to the current version.
 * 
 * @author geraldb
 * 
 */
public class MigrationController
{
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
                if(oldSchemaVersion == 0 && newSchemaVersion > 0)
                {
                    migrate0to1();
                    oldSchemaVersion++;
                }
                if(oldSchemaVersion == 1 && newSchemaVersion > 1)
                {
                    // next time
                }
                
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

    private void migrate0to1()
    {
        try
        {
            sqlMap.update("createSchemaInfoTable");
        }
        catch (SQLException e)
        {
            logger.warn("could not create schema_info table");
        }
        
        try
        {
            sqlMap.update("dropTransportsTable");
        }
        catch (SQLException e)
        {
            logger.warn("transport table not found");
        }
        
        try
        {
            sqlMap.update("addDeployScriptColumn");
        }
        catch (SQLException e)
        {
            logger.warn("could not add deploy script column to channel");
        }
        
        try
        {
            sqlMap.update("addShutdownScriptColumn");
        }
        catch (SQLException e)
        {
            logger.warn("could not add shutdown script column to channel");
        }
    }
}
