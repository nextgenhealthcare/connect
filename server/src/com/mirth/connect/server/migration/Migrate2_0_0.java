/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.util.MigrationException;

public class Migrate2_0_0 extends Migrator {
    private Logger logger = Logger.getLogger(getClass());
    
    @Override
    public void migrate() throws MigrationException {
        executeScript(getDatabaseType() + "-6-7.sql");
        migrateGlobalScripts();
        migrateServerProperties();
    }
    
    @Override
    public void migrateSerializedData() throws MigrationException {}

    private void migrateGlobalScripts() throws MigrationException {
        migrateGlobalScript("Deploy");
        migrateGlobalScript("Shutdown");
        migrateGlobalScript("Preprocessor");
        migrateGlobalScript("Postprocessor");
    }
    
    private void migrateGlobalScript(String scriptId) throws MigrationException {
        final String globalGroupId = "Global";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            Connection connection = getConnection();
            statement = connection.prepareStatement("SELECT SCRIPT FROM SCRIPT WHERE GROUP_ID = ? AND ID = ?");
            statement.setString(1, globalGroupId);
            statement.setString(2, scriptId);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                String script = resultSet.getString(1).replaceAll("com.webreach.mirth", "com.mirth.connect");
                resultSet.close();
                statement.close();
                
                statement = connection.prepareStatement("UPDATE SCRIPT SET SCRIPT = ? WHERE GROUP_ID = ? AND ID = ?");
                statement.setString(1, script);
                statement.setString(2, globalGroupId);
                statement.setString(3, scriptId);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new MigrationException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
        }
    }
    
    private void migrateServerProperties() throws MigrationException {
        /*
         * Since we moved the server properties from a file to the database, we need
         * to copy over the previous properties into the database if a file exists
         */
        File propertiesFile = new File(getBaseDir() + IOUtils.DIR_SEPARATOR + "server.properties");
        
        if (propertiesFile.exists()) {
            try {
                Properties serverProperties = new Properties();
                serverProperties.load(new FileInputStream(propertiesFile));
                
                for (Object name : serverProperties.keySet()) {
                    updateServerProperty((String) name, serverProperties.getProperty((String) name));
                }
                
                if (!propertiesFile.delete()) {
                    logger.error("Could not delete existing server.properties file. Please delete it manually.");
                }
            } catch (FileNotFoundException e) {
                logger.error("Error loading existing server.properties file.", e);
            } catch (IOException e) {
                logger.error("Error loading existing server.properties file.", e);
            }
        }
    }
    
    private String getBaseDir() {
        URL url = getClass().getResource("mirth.properties");
        
        if (url == null) {
            url = getClass().getResource("/mirth.properties");
        }
        
        if (url != null) {
            try {
                return new File(url.toURI()).getParentFile().getParent();
            } catch (URISyntaxException e) {
            }
        }
        
        return null;
    }
    
    private void updateServerProperty(String name, String value) throws MigrationException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            Connection connection = getConnection();
            statement = connection.prepareStatement("UPDATE CONFIGURATION SET VALUE = ? WHERE CATEGORY = 'core' AND NAME = ?");
            statement.setString(1, value);
            statement.setString(2, name);
            
            if (statement.executeUpdate() == 0) {
                statement.close();
                statement = connection.prepareStatement("INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', ?, ?)");
                statement.setString(1, name);
                statement.setString(2, value);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new MigrationException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
        }
    }
}
