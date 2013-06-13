package com.mirth.connect.server.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.DatabaseUtil;

public class Migrate2_0_0 implements Migrator {
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private Logger logger = Logger.getLogger(getClass());
    
    @Override
    public void migrate() throws DatabaseSchemaMigrationException {
        try {
            String migrationScript = IOUtils.toString(getClass().getResourceAsStream("/deltas/" + configurationController.getDatabaseType() + "-6-7.sql"));
            DatabaseUtil.executeScript(migrationScript, false);
        } catch (Exception e) {
            throw new DatabaseSchemaMigrationException(e);
        }

        migrateGlobalScripts();
        migrateServerProperties();
    }

    private void migrateGlobalScripts() {
        try {
            ScriptController scriptController = ScriptController.getInstance();
            Map<String, String> globalScripts = scriptController.getGlobalScripts();

            for (Entry<String, String> globalScriptEntry : globalScripts.entrySet()) {
                globalScripts.put(globalScriptEntry.getKey(), globalScriptEntry.getValue().replaceAll("com.webreach.mirth", "com.mirth.connect"));
            }

            scriptController.setGlobalScripts(globalScripts);
        } catch (Exception e) {
            logger.error("Error migrating global scripts.", e);
        }
    }

    private void migrateServerProperties() {
        /*
         * Since we moved the server properties from a file to the database, we need
         * to copy over the previous properties into the database if a file exists
         */
        try {
            File propertiesFile = new File(configurationController.getBaseDir() + File.separator + "server.properties");

            if (propertiesFile.exists()) {
                Properties newProperties = configurationController.getServerSettings().getProperties();
                Properties oldProperties = new Properties();

                oldProperties.load(new FileInputStream(propertiesFile));
                newProperties.putAll(oldProperties);
                configurationController.setServerSettings(new ServerSettings(newProperties));
                configurationController.setUpdateSettings(new UpdateSettings(newProperties));

                if (!propertiesFile.delete()) {
                    logger.error("Could not delete existing server.properties file. Please delete it manually.");
                }
            }
        } catch (ControllerException ce) {
            logger.error("Error loading current server properties from database.", ce);
        } catch (IOException ioe) {
            logger.error("Error loading existing server.properties file.", ioe);
        }
    }
}
