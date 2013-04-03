package com.mirth.connect.connectors.jdbc;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.passthru.PassthruDaoFactory;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.model.Channel;
import com.mirth.connect.server.Mirth;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.test.TestUtils;

public class DatabaseDispatcherTests {
    final public static String TEST_HL7_MESSAGE = "MSH|^~\\&|LABNET|Acme Labs|||20090601105700||ORU^R01|HMCDOOGAL-0088|D|2.2\rPID|1|8890088|8890088^^^72777||McDoogal^Hattie^||19350118|F||2106-3|100 Beach Drive^Apt. 5^Mission Viejo^CA^92691^US^H||(949) 555-0025|||||8890088^^^72|604422825\rPV1|1|R|C3E^C315^B||||2^HIBBARD^JULIUS^|5^ZIMMERMAN^JOE^|9^ZOIDBERG^JOHN^|CAR||||4|||2301^OBRIEN, KEVIN C|I|1783332658^1^1||||||||||||||||||||DISNEY CLINIC||N|||20090514205600\rORC|RE|928272608|056696716^LA||CM||||20090601105600||||  C3E|||^RESULT PERFORMED\rOBR|1|928272608|056696716^LA|1001520^K|||20090601101300|||MLH25|||HEMOLYZED/VP REDRAW|20090601102400||2301^OBRIEN, KEVIN C||||01123085310001100100152023509915823509915800000000101|0000915200932|20090601105600||LAB|F||^^^20090601084100^^ST~^^^^^ST\rOBX|1|NM|1001520^K||5.3|MMOL/L|3.5-5.5||||F|||20090601105600|IIM|IIM\r";

    private final static String DB_DRIVER = "org.postgresql.Driver";
    private final static String DB_URL = "jdbc:postgresql://localhost:5432/messages";
    private final static String DB_USERNAME = "mirth";
    private final static String DB_PASSWORD = "d1scgo1fisfun";
    private final static String TABLE1 = "mypatients_destination1";
    private final static String TABLE2 = "mypatients_destination2";
    private final static String TEST_CHANNEL_ID = "testchannel";
    private final static String TEST_SERVER_ID = "testserver";

    private static Connection connection;
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

        while (ConfigurationController.getInstance().isEngineStarting()) {
            Thread.sleep(100);
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        connection.close();
    }

    @Test
    public final void testSql() throws Exception {
        runTest(getDefaultProperties(false));
    }

    @Test
    public final void testScript() throws Exception {
        runTest(getDefaultProperties(true));
    }

    @Test
    public final void testMultipleStatements() throws Exception {
        // @formatter:off
        String query = "INSERT INTO " + TABLE1 + " (mypatientid, lastname, firstname, gender, dateofbirth) VALUES (${mypatientid}::integer, ${lastname}, ${firstname}, ${gender}, ${dateofbirth}::date);"
                     + "INSERT INTO " + TABLE2 + " (mypatientid, lastname, firstname, gender, dateofbirth) VALUES (${mypatientid}::integer, ${lastname}, ${firstname}, ${gender}, ${dateofbirth}::date);";
        // @formatter:on

        DatabaseDispatcherProperties properties = getDefaultProperties(false);
        properties.setQuery(query);

        List<String> tables = new ArrayList<String>();
        tables.add(TABLE1);
        tables.add(TABLE2);

        runTest(properties, tables);
    }

    @Test
    public final void testStoredProcedure() throws Exception {
        Statement statement = connection.createStatement();

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE OR REPLACE FUNCTION store_message(in int, in varchar, in varchar, in varchar, in date) RETURNS SETOF RECORD AS $$\n");
        sql.append("BEGIN INSERT INTO " + TABLE1 + " (mypatientid, lastname, firstname, gender, dateofbirth) VALUES ($1, $2, $3, $4, $5); END; $$ LANGUAGE plpgsql;");
        statement.executeUpdate(sql.toString());

        DatabaseDispatcherProperties properties = getDefaultProperties(false);
        properties.setQuery("SELECT store_message(${mypatientid}::integer, ${lastname}, ${firstname}, ${gender}, ${dateofbirth}::date)");
        runTest(properties);
    }

    private DatabaseDispatcherProperties getDefaultProperties(boolean useScript) {
        DatabaseDispatcherProperties properties = new DatabaseDispatcherProperties();
        properties.setDriver(DB_DRIVER);
        properties.setUrl(DB_URL);
        properties.setUsername(DB_USERNAME);
        properties.setPassword(DB_PASSWORD);
        properties.setUseScript(useScript);

        if (!useScript) {
            properties.setQuery("INSERT INTO " + TABLE1 + " (mypatientid, lastname, firstname, gender, dateofbirth) VALUES (${mypatientid}::integer, ${lastname}, ${firstname}, ${gender}, ${dateofbirth}::date)");
        } else {
            StringBuilder script = new StringBuilder();
            script.append("var dbConn = DatabaseConnectionFactory.createDatabaseConnection('" + DB_DRIVER + "','" + DB_URL + "','" + DB_USERNAME + "','" + DB_PASSWORD + "');\n");

            script.append("var params = new java.util.ArrayList();\n");
            script.append("params.add($('mypatientid'));\n");
            script.append("params.add($('lastname'));\n");
            script.append("params.add($('firstname'));\n");
            script.append("params.add($('gender'));\n");
            script.append("params.add($('dateofbirth'));\n");

            script.append("var result = dbConn.executeUpdate(\"INSERT INTO " + TABLE1 + " (mypatientid, lastname, firstname, gender, dateofbirth) VALUES (?::integer, ?, ?, ?, ?::date)\", params);\n");
            script.append("dbConn.close();\n");

            properties.setQuery(script.toString());
        }

        return properties;
    }

    private void initTables() throws SQLException {
        Statement statement = connection.createStatement();

        if (TestUtils.tableExists(connection, TABLE1)) {
            statement.executeUpdate("DROP TABLE " + TABLE1);
        }

        if (TestUtils.tableExists(connection, TABLE2)) {
            statement.executeUpdate("DROP TABLE " + TABLE2);
        }

        statement.executeUpdate("CREATE TABLE " + TABLE1 + " (mypatientid integer NOT NULL, lastname varchar(64) NOT NULL, firstname varchar(64) NOT NULL, gender character varying(1) NOT NULL, dateofbirth date NOT NULL)");
        statement.executeUpdate("CREATE TABLE " + TABLE2 + " (mypatientid integer NOT NULL, lastname varchar(64) NOT NULL, firstname varchar(64) NOT NULL, gender character varying(1) NOT NULL, dateofbirth date NOT NULL)");
        statement.close();
    }

    private void runTest(DatabaseDispatcherProperties properties) throws Exception {
        List<String> tables = new ArrayList<String>();
        tables.add(TABLE1);
        runTest(properties, tables);
    }

    private void runTest(DatabaseDispatcherProperties properties, List<String> tables) throws Exception {
        final int numMessages = 3;

        initTables();

        Channel channel = new Channel();
        channel.setId(TEST_CHANNEL_ID);
        channel.setName("test channel");
        ChannelController.getInstance().putDeployedChannelInCache(channel);

        DonkeyDao dao = new PassthruDaoFactory().getDao();

        DestinationConnector databaseDispatcher = new TestDatabaseDispatcher(TEST_CHANNEL_ID, 1, properties);
        databaseDispatcher.onDeploy();
        databaseDispatcher.start();

        long messageIdSequence = 1;

        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();

        Map<String, String> map = new HashMap<String, String>();
        map.put("mypatientid", "1");
        map.put("firstname", "Joe");
        map.put("lastname", "Rodriguez");
        map.put("gender", "M");
        map.put("dateofbirth", "1935-01-18");
        messages.add(map);

        map = new HashMap<String, String>();
        map.put("mypatientid", "2");
        map.put("firstname", "Hubert");
        map.put("lastname", "Farnsworth");
        map.put("gender", "M");
        map.put("dateofbirth", "1935-01-18");
        messages.add(map);

        map = new HashMap<String, String>();
        map.put("mypatientid", "3");
        map.put("firstname", "Amy");
        map.put("lastname", "Wong");
        map.put("gender", "F");
        map.put("dateofbirth", "1935-01-18");
        messages.add(map);

        for (int i = 0; i < numMessages; i++) {
            ConnectorMessage message = new ConnectorMessage();
            message.setMessageId(messageIdSequence++);
            message.setChannelId(TEST_CHANNEL_ID);
            message.setChainId(1);
            message.setServerId(TEST_SERVER_ID);

            MessageContent rawContent = new MessageContent(message.getChannelId(), message.getMessageId(), message.getMetaDataId(), ContentType.RAW, TEST_HL7_MESSAGE, "HL7", false);
            MessageContent encodedContent = SerializationUtils.clone(rawContent);
            encodedContent.setContentType(ContentType.ENCODED);

            message.setRaw(rawContent);
            message.setEncoded(encodedContent);
            message.getChannelMap().putAll(messages.get(i));
            message.setStatus(Status.TRANSFORMED);

            databaseDispatcher.process(dao, message, Status.RECEIVED);
        }

        databaseDispatcher.stop();
        databaseDispatcher.onUndeploy();
        dao.close();

        Statement statement = connection.createStatement();

        for (String table : tables) {
            ResultSet result = statement.executeQuery("SELECT mypatientid, firstname, lastname, gender, dateofbirth::varchar FROM " + table + " ORDER BY mypatientid");
            int i = 0;

            while (result.next()) {
                map = messages.get(i++);
                assertEquals(map.get("mypatientid"), result.getString(1));
                assertEquals(map.get("firstname"), result.getString(2));
                assertEquals(map.get("lastname"), result.getString(3));
                assertEquals(map.get("gender"), result.getString(4));
                assertEquals(map.get("dateofbirth"), result.getString(5));
            }

            result.close();
            assertEquals(messages.size(), i);
        }

        statement.close();
    }

    private class TestDatabaseDispatcher extends DatabaseDispatcher {
        public TestDatabaseDispatcher(String channelId, Integer metaDataId, DatabaseDispatcherProperties properties) {
            super();
            setChannelId(channelId);
            setMetaDataId(metaDataId);
            setConnectorProperties(properties);

            if (properties.getQueueConnectorProperties().isQueueEnabled()) {
                getQueue().setDataSource(new ConnectorMessageQueueDataSource(channelId, metaDataId, Status.QUEUED, isQueueRotate(), new PassthruDaoFactory()));
                getQueue().updateSize();
            }
        }
    }
}
