package com.mirth.connect.server.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MigrationUtil;

public class Migrate3_0_0 implements Migrator {
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void migrate() throws DatabaseSchemaMigrationException {
        try {
            String databaseType = ControllerFactory.getFactory().createConfigurationController().getDatabaseType();
            String migrationScript = IOUtils.toString(getClass().getResourceAsStream("/deltas/" + databaseType + "-9-3.0.0.sql"));
            DatabaseUtil.executeScript(migrationScript, false);
        } catch (Exception e) {
            throw new DatabaseSchemaMigrationException(e);
        }

        migrateChannelTable();
        migrateAlertTable();
        migrateCodeTemplateTable();
    }

    private void migrateChannelTable() {
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
                String channelId = "";

                try {
                    channelId = results.getString(1);
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
                    } finally {
                        DbUtils.closeQuietly(updateStatement);
                    }
                } catch (Exception e) {
                    logger.error("Error migrating channel " + channelId + ".", e);
                }
            }

            conn.commit();
        } catch (SQLException e) {
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

    private void migrateAlertTable() {
        Logger logger = Logger.getLogger(getClass());
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;

        try {
            SqlConfig.getSqlSessionManager().startManagedSession();
            conn = SqlConfig.getSqlSessionManager().getConnection();

            Map<String, List<String>> alertEmails = new HashMap<String, List<String>>();
            Map<String, List<String>> alertChannels = new HashMap<String, List<String>>();

            /*
             * MIRTH-1667: Derby fails if autoCommit is set to true and
             * there are a large number of results. The following error
             * occurs: "ERROR 40XD0: Container has been closed"
             */
            conn.setAutoCommit(false);

            // Build a list of emails for each alert
            statement = conn.prepareStatement("SELECT ALERT_ID, EMAIL FROM __ALERT_EMAIL");
            results = statement.executeQuery();

            while (results.next()) {
                String alertId = results.getString(1);
                String email = results.getString(2);

                List<String> emailSet = alertEmails.get(alertId);

                if (emailSet == null) {
                    emailSet = new ArrayList<String>();
                    alertEmails.put(alertId, emailSet);
                }

                emailSet.add(email);
            }

            DbUtils.closeQuietly(results);

            // Build a list of applied channels for each alert
            statement = conn.prepareStatement("SELECT CHANNEL_ID, ALERT_ID FROM __CHANNEL_ALERT");
            results = statement.executeQuery();

            while (results.next()) {
                String channelId = results.getString(1);
                String alertId = results.getString(2);

                List<String> channelSet = alertChannels.get(alertId);

                if (channelSet == null) {
                    channelSet = new ArrayList<String>();
                    alertChannels.put(alertId, channelSet);
                }

                channelSet.add(channelId);
            }

            DbUtils.closeQuietly(results);

            statement = conn.prepareStatement("SELECT ID, NAME, IS_ENABLED, EXPRESSION, TEMPLATE, SUBJECT FROM __ALERT");
            results = statement.executeQuery();

            while (results.next()) {
                String alertId = "";

                try {
                    alertId = results.getString(1);
                    String name = results.getString(2);
                    boolean enabled = results.getBoolean(3);
                    String expression = results.getString(4);
                    String template = results.getString(5);
                    String subject = results.getString(6);

                    /*
                     * Create a new document with alertModel as the root node
                     */
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    Element alertNode = document.createElement("alert");
                    document.appendChild(alertNode);

                    Element node = document.createElement("id");
                    node.setTextContent(alertId);
                    alertNode.appendChild(node);

                    node = document.createElement("name");
                    node.setTextContent(name);
                    alertNode.appendChild(node);

                    node = document.createElement("expression");
                    node.setTextContent(expression);
                    alertNode.appendChild(node);

                    node = document.createElement("template");
                    node.setTextContent(template);
                    alertNode.appendChild(node);

                    node = document.createElement("enabled");
                    node.setTextContent(Boolean.toString(enabled));
                    alertNode.appendChild(node);

                    node = document.createElement("subject");
                    node.setTextContent(subject);
                    alertNode.appendChild(node);

                    // Add each applied channel to the document
                    Element channelNode = document.createElement("channels");
                    alertNode.appendChild(channelNode);
                    List<String> channelList = alertChannels.get(alertId);
                    if (channelList != null) {
                        for (String channelId : channelList) {
                            Element stringNode = document.createElement("string");
                            stringNode.setTextContent(channelId);
                            channelNode.appendChild(stringNode);
                        }
                    }

                    // Add each email address to the document
                    Element emailNode = document.createElement("emails");
                    alertNode.appendChild(emailNode);
                    List<String> emailList = alertEmails.get(alertId);
                    if (emailList != null) {
                        for (String email : emailList) {
                            Element stringNode = document.createElement("string");
                            stringNode.setTextContent(email);
                            emailNode.appendChild(stringNode);
                        }
                    }

                    String alert = new DocumentSerializer(true).toXML(document);

                    PreparedStatement updateStatement = null;

                    try {
                        updateStatement = conn.prepareStatement("INSERT INTO ALERT VALUES (?, ?, ?)");
                        updateStatement.setString(1, alertId);
                        updateStatement.setString(2, name);
                        updateStatement.setString(3, alert);
                        updateStatement.executeUpdate();
                        updateStatement.close();
                    } finally {
                        DbUtils.closeQuietly(updateStatement);
                    }
                } catch (Exception e) {
                    logger.error("Error migrating alert " + alertId + ".", e);
                }
            }

            conn.commit();
        } catch (SQLException e) {
            logger.error("Error migrating alerts.", e);
        } finally {
            DbUtils.closeQuietly(results);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(conn);

            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
        }
    }

    private void migrateCodeTemplateTable() {
        Logger logger = Logger.getLogger(getClass());
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
                String id = "";

                try {
                    id = results.getString(1);
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
                    } finally {
                        DbUtils.closeQuietly(updateStatement);
                    }
                } catch (Exception e) {
                    logger.error("Error migrating code template " + id + ".", e);
                }
            }

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
