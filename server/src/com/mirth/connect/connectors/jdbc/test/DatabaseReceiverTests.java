/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc.test;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.connectors.jdbc.DatabaseReceiver;
import com.mirth.connect.connectors.jdbc.DatabaseReceiverProperties;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.server.Mirth;
import com.mirth.connect.server.channel.MirthMetaDataReplacer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.test.TestUtils;
import com.mirth.connect.server.test.TestUtils.DummyChannel;

public class DatabaseReceiverTests {
    private final static String DB_DRIVER = "org.postgresql.Driver";
    private final static String DB_URL = "jdbc:postgresql://localhost:5432/mirthdb";
    private final static String DB_USERNAME = "";
    private final static String DB_PASSWORD = "";
    private final static String TABLE = "mypatients";
    private final static String TEST_CHANNEL_ID = "testchannel";
    private final static String TEST_SERVER_ID = "testserver";

    private static Connection connection;
    private static DummyChannel testChannel;
    private static Mirth server = new Mirth();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        connection.setAutoCommit(true);

        // start a basic server
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                server.run();
            }
        });

        while (ConfigurationController.getInstance().getStatus() != ConfigurationController.STATUS_OK) {
            Thread.sleep(100);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        connection.close();
    }

    private int numMessages = 0;

    @Test
    public final void testSqlNoUpdate() throws Exception {
        runTest(getDefaultProperties(false, DatabaseReceiverProperties.UPDATE_NEVER));
    }

    @Test
    public final void testSqlUpdateEach() throws Exception {
        DatabaseReceiverProperties properties = getDefaultProperties(false, DatabaseReceiverProperties.UPDATE_EACH);
        properties.getPollConnectorProperties().setPollingFrequency(300);
        runTest(properties, 1000, false);
    }

    @Test
    public final void testSqlUpdateOnce() throws Exception {
        runTest(getDefaultProperties(false, DatabaseReceiverProperties.UPDATE_ONCE));
    }

    @Test
    public final void testJavaScriptNoUpdate() throws Exception {
        runTest(getDefaultProperties(true, DatabaseReceiverProperties.UPDATE_NEVER));
    }

    @Test
    public final void testJavaScriptUpdateEach() throws Exception {
        runTest(getDefaultProperties(true, DatabaseReceiverProperties.UPDATE_EACH));
    }

    @Test
    public final void testJavaScriptUpdateOnce() throws Exception {
        runTest(getDefaultProperties(true, DatabaseReceiverProperties.UPDATE_ONCE));
    }

    @Test
    public final void testSourceQueueEnabled() throws Exception {
        runTest(getDefaultProperties(false, DatabaseReceiverProperties.UPDATE_ONCE), 300, true);
    }

    @Test
    public final void testCacheEnabled() throws Exception {
        DatabaseReceiverProperties properties = getDefaultProperties(false, DatabaseReceiverProperties.UPDATE_ONCE);
        properties.setCacheResults(true);
        runTest(properties);
    }

    @Test
    public final void testKeepConnectionOpenDisabled() throws Exception {
        DatabaseReceiverProperties properties = getDefaultProperties(false, DatabaseReceiverProperties.UPDATE_ONCE);
        properties.setKeepConnectionOpen(false);
        runTest(properties);
    }

    @Test
    public final void testMultipleStatements() throws Exception {
        final String testLastName = "newlastname";

        DatabaseReceiverProperties properties = getDefaultProperties(false, DatabaseReceiverProperties.UPDATE_EACH);
        properties.setUpdate("UPDATE " + TABLE + " SET lastname = '" + testLastName + "' WHERE mypatientid = ${mypatientid}; UPDATE " + TABLE + " SET processed = TRUE WHERE mypatientid = ${mypatientid};");
        runTest(properties);

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT lastname FROM " + TABLE + " WHERE processed = TRUE");

        while (result.next()) {
            assertEquals(testLastName, result.getString(1));
        }

        result.close();
        statement.close();
    }

    @Test
    public final void testStoredProcedures() throws Exception {
        Statement statement = connection.createStatement();

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE OR REPLACE FUNCTION select_messages() RETURNS SETOF integer\n");
        sql.append("AS $$ SELECT mypatientid FROM mypatients WHERE processed = FALSE; $$ LANGUAGE SQL;");
        statement.executeUpdate(sql.toString());

        sql = new StringBuilder();
        sql.append("CREATE OR REPLACE FUNCTION update_messages(in int) RETURNS SETOF RECORD AS $$\n");
        sql.append("BEGIN UPDATE mypatients SET processed = TRUE WHERE mypatientid = $1; END; $$ LANGUAGE plpgsql;");
        statement.executeUpdate(sql.toString());

        statement.close();

        DatabaseReceiverProperties properties = getDefaultProperties(false, DatabaseReceiverProperties.UPDATE_EACH);
        properties.setSelect("SELECT select_messages() AS mypatientid");
        properties.setUpdate("SELECT update_messages(${mypatientid})");
        runTest(properties);
    }

    @Test
    public final void testScriptReturnList() throws Exception {
        StringBuilder script = new StringBuilder();
        script.append("var results = new java.util.ArrayList();\n");

        script.append("var entry = new java.util.HashMap();\n");
        script.append("entry.put(\"mypatientid\", 1);\n");
        script.append("entry.put(\"lastname\", \"Rodriguez\");\n");
        script.append("entry.put(\"firstname\", \"Joe\");\n");
        script.append("entry.put(\"gender\", \"M\");\n");
        script.append("entry.put(\"dateofbirth\", \"2000-01-01\");\n");
        script.append("results.add(entry);\n");

        script.append("entry = new java.util.HashMap();\n");
        script.append("entry.put(\"mypatientid\", 1);\n");
        script.append("entry.put(\"lastname\", \"Farnsworth\");\n");
        script.append("entry.put(\"firstname\", \"Hubert\");\n");
        script.append("entry.put(\"gender\", \"M\");\n");
        script.append("entry.put(\"dateofbirth\", \"2000-01-01\");\n");
        script.append("results.add(entry);\n");

        script.append("entry = new java.util.HashMap();\n");
        script.append("entry.put(\"mypatientid\", 1);\n");
        script.append("entry.put(\"lastname\", \"Wong\");\n");
        script.append("entry.put(\"firstname\", \"Amy\");\n");
        script.append("entry.put(\"gender\", \"F\");\n");
        script.append("entry.put(\"dateofbirth\", \"2000-01-01\");\n");
        script.append("results.add(entry);\n");

        script.append("return results;\n");

        DatabaseReceiverProperties properties = getDefaultProperties(true, DatabaseReceiverProperties.UPDATE_NEVER);
        properties.setSelect(script.toString());
        runTest(properties);
    }

    private DatabaseReceiverProperties getDefaultProperties(boolean useScript, int updateMode) {
        DatabaseReceiverProperties properties = new DatabaseReceiverProperties();
        properties.setDriver(DB_DRIVER);
        properties.setUrl(DB_URL);
        properties.setUsername(DB_USERNAME);
        properties.setPassword(DB_PASSWORD);
        properties.setCacheResults(false);
        properties.setUseScript(useScript);
        properties.setUpdateMode(updateMode);

        if (!useScript) {
            properties.setSelect("SELECT mypatientid, lastname, firstname, gender, dateofbirth FROM " + TABLE + " WHERE processed = FALSE");

            switch (updateMode) {
                case DatabaseReceiverProperties.UPDATE_EACH:
                    properties.setUpdate("UPDATE " + TABLE + " SET processed = TRUE WHERE mypatientid = ${mypatientid}");
                    break;

                case DatabaseReceiverProperties.UPDATE_ONCE:
                    properties.setUpdate("UPDATE " + TABLE + " SET processed = TRUE");
                    break;
            }
        } else {
            StringBuilder selectScript = new StringBuilder();
            selectScript.append("var dbConn = DatabaseConnectionFactory.createDatabaseConnection('" + DB_DRIVER + "','" + DB_URL + "','" + DB_USERNAME + "','" + DB_PASSWORD + "');\n");
            selectScript.append("var result = dbConn.executeCachedQuery(\"SELECT mypatientid, lastname, firstname, gender, dateofbirth FROM " + TABLE + " WHERE processed = FALSE\");\n");
            selectScript.append("dbConn.close();\n");
            selectScript.append("return result;\n");

            properties.setSelect(selectScript.toString());

            if (updateMode != DatabaseReceiverProperties.UPDATE_NEVER) {
                StringBuilder updateScript = new StringBuilder();
                updateScript.append("var dbConn = DatabaseConnectionFactory.createDatabaseConnection('" + DB_DRIVER + "','" + DB_URL + "','" + DB_USERNAME + "','" + DB_PASSWORD + "');\n");

                switch (updateMode) {
                    case DatabaseReceiverProperties.UPDATE_EACH:
                        updateScript.append("var params = new java.util.ArrayList();");
                        updateScript.append("params.add($('mypatientid'));");
                        updateScript.append("var result = dbConn.executeUpdate(\"UPDATE " + TABLE + " SET processed = TRUE WHERE mypatientid = ?\", params);");
                        break;

                    case DatabaseReceiverProperties.UPDATE_ONCE:
                        updateScript.append("var result = dbConn.executeUpdate(\"UPDATE " + TABLE + " SET processed = TRUE\");");
                        break;
                }

                updateScript.append("dbConn.close();");
                properties.setUpdate(updateScript.toString());
            }
        }

        return properties;
    }

    private void initTable() throws SQLException {
        Statement statement = connection.createStatement();

        if (TestUtils.tableExists(connection, TABLE)) {
            statement.executeUpdate("DROP TABLE " + TABLE);
        }

        statement.executeUpdate("CREATE TABLE " + TABLE + " (mypatientid serial NOT NULL, lastname varchar(64) NOT NULL, firstname varchar(64) NOT NULL, gender character varying(1) NOT NULL, dateofbirth date NOT NULL, processed boolean NOT NULL DEFAULT false, CONSTRAINT mypatients_pkey PRIMARY KEY (mypatientid))");
        statement.close();
        numMessages = 0;

        PreparedStatement insert = connection.prepareStatement("INSERT INTO " + TABLE + " (lastname, firstname, gender, dateofbirth) VALUES (?, ?, ?, ?)");
        insert.setString(1, "Rodriguez");
        insert.setString(2, "Joe");
        insert.setString(3, "M");
        insert.setDate(4, new Date(Calendar.getInstance().getTimeInMillis()));
        insert.executeUpdate();
        numMessages++;

        insert.setString(1, "Farnsworth");
        insert.setString(2, "Hubert");
        insert.setString(3, "M");
        insert.setDate(4, new Date(Calendar.getInstance().getTimeInMillis()));
        insert.executeUpdate();
        numMessages++;

        insert.setString(1, "Wong");
        insert.setString(2, "Amy");
        insert.setString(3, "F");
        insert.setDate(4, new Date(Calendar.getInstance().getTimeInMillis()));
        insert.executeUpdate();
        numMessages++;

        insert.close();
    }

    private void runTest(DatabaseReceiverProperties properties) throws Exception {
        runTest(properties, 300, false);
    }

    private void runTest(DatabaseReceiverProperties properties, long sleepMillis, boolean sourceQueueEnabled) throws Exception {
        initTable();

        testChannel = new DummyChannel(TEST_CHANNEL_ID, TEST_SERVER_ID);

        DatabaseReceiver databaseReceiver = createDatabaseReceiver(properties);
        databaseReceiver.onDeploy();
        databaseReceiver.start();

        Thread.sleep(sleepMillis);

        databaseReceiver.stop();
        databaseReceiver.onUndeploy();

        assertEquals(numMessages, testChannel.getRawMessages().size());

        if (properties.getUpdateMode() != DatabaseReceiverProperties.UPDATE_NEVER) {
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + TABLE + " WHERE processed = TRUE");
            result.next();
            assertEquals(numMessages, result.getInt(1));
            result.close();

            result = statement.executeQuery("SELECT COUNT(*) FROM " + TABLE + " WHERE processed = FALSE");
            result.next();
            assertEquals(0, result.getInt(1));
            result.close();
        }
    }

    private DatabaseReceiver createDatabaseReceiver(DatabaseReceiverProperties properties) {
        DatabaseReceiver connector = new DatabaseReceiver();
        connector.setConnectorProperties(properties);
        initConnector(connector, 0);
        initSourceConnector(connector);
        return connector;
    }

    private void initConnector(Connector connector, Integer metaDataId) {
        connector.setChannelId(testChannel.getChannelId());
        connector.setMetaDataId(metaDataId);
    }

    private void initSourceConnector(SourceConnector sourceConnector) {
        sourceConnector.setChannelId(testChannel.getChannelId());
        sourceConnector.setChannel(testChannel);
        sourceConnector.setMetaDataReplacer(new MirthMetaDataReplacer());
        sourceConnector.setRespondAfterProcessing(true);
        sourceConnector.setFilterTransformerExecutor(new FilterTransformerExecutor(sourceConnector.getInboundDataType(), sourceConnector.getOutboundDataType()));

        testChannel.setSourceConnector(sourceConnector);
    }
}
