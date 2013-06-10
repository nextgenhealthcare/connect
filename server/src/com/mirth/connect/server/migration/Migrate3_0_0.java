package com.mirth.connect.server.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MigrationUtil;

public class Migrate3_0_0 {
    private static Logger logger = Logger.getLogger(Migrate3_0_0.class);
    private static ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    public static void migrate(int oldVersion) throws Exception {
        for (int i = oldVersion; i <= 8; i++) {
            String migrationScript = IOUtils.toString(Migrate3_0_0.class.getResourceAsStream("/deltas/" + configurationController.getDatabaseType() + "-" + i + "-" + (i + 1) + ".sql"));
            DatabaseUtil.executeScript(migrationScript, true);
        }

        String migrationScript = IOUtils.toString(Migrate3_0_0.class.getResourceAsStream("/deltas/" + configurationController.getDatabaseType() + "-9-3.0.0.sql"));
        DatabaseUtil.executeScript(migrationScript, true);

        migrateChannelTable();
        migrateCodeTemplateTable();
    }

    private static void migrateChannelTable() {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
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

            preparedStatement = conn.prepareStatement("SELECT ID, NAME, DESCRIPTION, IS_ENABLED, VERSION, REVISION, LAST_MODIFIED, SOURCE_CONNECTOR, DESTINATION_CONNECTORS, PROPERTIES, PREPROCESSING_SCRIPT, POSTPROCESSING_SCRIPT, DEPLOY_SCRIPT, SHUTDOWN_SCRIPT FROM __CHANNEL");
            results = preparedStatement.executeQuery();

            while (results.next()) {
                String channelId = results.getString(1);
                String name = results.getString(2);
                String description = results.getString(3);
                Boolean isEnabled = results.getBoolean(4);
                String version = results.getString(5);
                Integer revision = results.getInt(6);

                Calendar lastModified = Calendar.getInstance();
                lastModified.setTimeInMillis(results.getTimestamp(7).getTime());

                String sourceConnector = results.getString(8);
                String destinationConnectors = results.getString(9);
                String properties = results.getString(10);
                String preprocessingScript = results.getString(11);
                String postprocessingScript = results.getString(12);
                String deployScript = results.getString(13);
                String shutdownScript = results.getString(14);

                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element element = document.createElement("channel");
                document.appendChild(element);
                DonkeyElement channel = new DonkeyElement(element);

                channel.addChildElement("id", channelId);
                channel.addChildElement("name", name);
                channel.addChildElement("description", description);
                channel.addChildElement("enabled", Boolean.toString(isEnabled));
                channel.addChildElement("version", version);

                DonkeyElement lastModifiedElement = channel.addChildElement("lastModified");
                lastModifiedElement.addChildElement("time", String.valueOf(lastModified.getTimeInMillis()));
                lastModifiedElement.addChildElement("timezone", lastModified.getTimeZone().getDisplayName());

                channel.addChildElement("revision", String.valueOf(revision));

                new DonkeyElement((Element) channel.appendChild(document.importNode(MigrationUtil.elementFromXml(sourceConnector), true))).setNodeName("sourceConnector");
                new DonkeyElement((Element) channel.appendChild(document.importNode(MigrationUtil.elementFromXml(destinationConnectors), true))).setNodeName("destinationConnectors");
                channel.appendChild(document.importNode(MigrationUtil.elementFromXml(properties), true));

                channel.addChildElement("preprocessingScript", preprocessingScript);
                channel.addChildElement("postprocessingScript", postprocessingScript);
                channel.addChildElement("deployScript", deployScript);
                channel.addChildElement("shutdownScript", shutdownScript);

                String serializedChannel = new DocumentSerializer().toXML(document);

                PreparedStatement updateStatement = null;
                try {
                    updateStatement = conn.prepareStatement("INSERT INTO CHANNEL (ID, NAME, REVISION, CHANNEL) VALUES (?, ?, ?, ?)");
                    updateStatement.setString(1, channelId);
                    updateStatement.setString(2, name);
                    updateStatement.setInt(3, revision);
                    updateStatement.setString(4, serializedChannel);
                    updateStatement.executeUpdate();
                    updateStatement.close();
                } catch (Exception e) {
                    logger.error("Error migrating channel " + channelId + ".", e);
                } finally {
                    DbUtils.closeQuietly(updateStatement);
                }
            }

            // Since autoCommit was set to false, commit the updates
            conn.commit();

        } catch (Exception e) {
            logger.error("Error migrating channels.", e);
        } finally {
            DbUtils.closeQuietly(results);
            DbUtils.closeQuietly(preparedStatement);
            DbUtils.closeQuietly(conn);
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }

    private static void migrateCodeTemplateTable() {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
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

            preparedStatement = conn.prepareStatement("SELECT ID, NAME, CODE_SCOPE, CODE_TYPE, TOOLTIP, CODE FROM __CODE_TEMPLATE");
            results = preparedStatement.executeQuery();

            while (results.next()) {
                String id = results.getString(1);
                String name = results.getString(2);
                String codeScope = results.getString(3);
                String codeType = results.getString(4);
                String toolTip = results.getString(5);
                String code = results.getString(6);

                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element element = document.createElement("codeTemplate");
                document.appendChild(element);
                DonkeyElement codeTemplate = new DonkeyElement(element);

                codeTemplate.addChildElement("id", id);
                codeTemplate.addChildElement("name", name);
                codeTemplate.addChildElement("tooltip", toolTip);
                codeTemplate.addChildElement("code", code);
                codeTemplate.addChildElement("type", codeType);
                codeTemplate.addChildElement("scope", codeScope);

                String serializedCodeTemplate = new DocumentSerializer().toXML(document);

                PreparedStatement updateStatement = null;
                try {
                    updateStatement = conn.prepareStatement("INSERT INTO CODE_TEMPLATE (ID, CODE_TEMPLATE) VALUES (?, ?)");
                    updateStatement.setString(1, id);
                    updateStatement.setString(2, serializedCodeTemplate);
                    updateStatement.executeUpdate();
                    updateStatement.close();
                } catch (Exception e) {
                    logger.error("Error migrating code template " + id + ".", e);
                } finally {
                    DbUtils.closeQuietly(updateStatement);
                }
            }

            // Since autoCommit was set to false, commit the updates
            conn.commit();

        } catch (Exception e) {
            logger.error("Error migrating code templates.", e);
        } finally {
            DbUtils.closeQuietly(results);
            DbUtils.closeQuietly(preparedStatement);
            DbUtils.closeQuietly(conn);
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }
}
