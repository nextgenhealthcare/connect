package com.mirth.connect.server.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.CodeTemplateController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class Migrate2_0_0 {
    private static final int SCHEMA_VERSION = 7;

    private static Logger logger = Logger.getLogger(Migrate2_0_0.class);
    private static ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private static ScriptController scriptController = ControllerFactory.getFactory().createScriptController();

    public static void migrate(int oldVersion) throws Exception {
        if (oldVersion >= SCHEMA_VERSION) {
            return;
        }

        for (int i = oldVersion; i < SCHEMA_VERSION; i++) {
            String migrationScript = IOUtils.toString(Migrate3_0_0.class.getResourceAsStream("/deltas/" + configurationController.getDatabaseType() + "-" + i + "-" + (i + 1) + ".sql"));
            DatabaseUtil.executeScript(migrationScript, true);
        }

        // Update the code template scopes and package names
        CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
        try {
            ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
            List<CodeTemplate> codeTemplates = codeTemplateController.getCodeTemplate(null);
            // TODO: Build the 2.0.0 code template document manually, otherwise this won't work if the model changes
            List<CodeTemplate> convertedCodeTemplates = (List<CodeTemplate>) serializer.fromXML(serializer.toXML(codeTemplates));
            codeTemplateController.updateCodeTemplates(convertedCodeTemplates);
        } catch (Exception e) {
            logger.error("Error migrating code templates.", e);
        }

        // Update the global script package names
        try {
            Map<String, String> globalScripts = scriptController.getGlobalScripts();

            for (Entry<String, String> globalScriptEntry : globalScripts.entrySet()) {
                globalScripts.put(globalScriptEntry.getKey(), globalScriptEntry.getValue().replaceAll("com.webreach.mirth", "com.mirth.connect"));
            }

            scriptController.setGlobalScripts(globalScripts);
        } catch (Exception e) {
            logger.error("Error migrating global scripts.", e);
        }

        // Update the connector package names in the database so the
        // connector objects can serialize to the new package names
        Connection conn = null;
        Statement statement = null;
        ResultSet results = null;

        try {
            SqlConfig.getSqlSessionManager().startManagedSession();
            conn = SqlConfig.getSqlSessionManager().getConnection();

            /*
             * MIRTH-1667: Derby fails if autoCommit is set to true and
             * there are a large number of results. The following error
             * occurs: "ERROR 40XD0: Container has been closed"
             */
            conn.setAutoCommit(false);

            statement = conn.createStatement();
            results = statement.executeQuery("SELECT ID, SOURCE_CONNECTOR, DESTINATION_CONNECTORS FROM CHANNEL");

            while (results.next()) {
                String channelId = results.getString(1);
                String sourceConnector = results.getString(2);
                String destinationConnectors = results.getString(3);

                sourceConnector = sourceConnector.replaceAll("com.webreach.mirth", "com.mirth.connect");
                destinationConnectors = destinationConnectors.replaceAll("com.webreach.mirth", "com.mirth.connect");

                PreparedStatement preparedStatement = null;
                try {
                    preparedStatement = conn.prepareStatement("UPDATE CHANNEL SET SOURCE_CONNECTOR = ?, DESTINATION_CONNECTORS = ? WHERE ID = ?");
                    preparedStatement.setString(1, sourceConnector);
                    preparedStatement.setString(2, destinationConnectors);
                    preparedStatement.setString(3, channelId);

                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (Exception ex) {
                    logger.error("Error migrating connectors.", ex);
                } finally {
                    DbUtils.closeQuietly(preparedStatement);
                }
            }

            // Since autoCommit was set to false, commit the updates
            conn.commit();

        } catch (Exception e) {
            logger.error("Error migrating connectors.", e);
        } finally {
            DbUtils.closeQuietly(results);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }

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
