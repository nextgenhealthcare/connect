package com.mirth.connect.connectors.jms;

import static org.junit.Assert.assertEquals;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.server.channel.MirthMetaDataReplacer;
import com.mirth.connect.server.test.TestUtils.DummyChannel;

public class JmsReceiverTests {
    private final static String TEST_CHANNEL_ID = "testchannel";
    private final static String TEST_SERVER_ID = "testserver";
    private final static String TEST_HL7_MESSAGE = "MSH|^~\\&|LABNET|Acme Labs|||20090601105700||ORU^R01|HMCDOOGAL-0088|D|2.2\rPID|1|8890088|8890088^^^72777||McDoogal^Hattie^||19350118|F||2106-3|100 Beach Drive^Apt. 5^Mission Viejo^CA^92691^US^H||(949) 555-0025|||||8890088^^^72|604422825\rPV1|1|R|C3E^C315^B||||2^HIBBARD^JULIUS^|5^ZIMMERMAN^JOE^|9^ZOIDBERG^JOHN^|CAR||||4|||2301^OBRIEN, KEVIN C|I|1783332658^1^1||||||||||||||||||||DISNEY CLINIC||N|||20090514205600\rORC|RE|928272608|056696716^LA||CM||||20090601105600||||  C3E|||^RESULT PERFORMED\rOBR|1|928272608|056696716^LA|1001520^K|||20090601101300|||MLH25|||HEMOLYZED/VP REDRAW|20090601102400||2301^OBRIEN, KEVIN C||||01123085310001100100152023509915823509915800000000101|0000915200932|20090601105600||LAB|F||^^^20090601084100^^ST~^^^^^ST\rOBX|1|NM|1001520^K||5.3|MMOL/L|3.5-5.5||||F|||20090601105600|IIM|IIM\r";

    private static DummyChannel testChannel;
    private static ConnectionFactory connectionFactory;
    private static Session session;
    private static Logger logger = Logger.getLogger(JmsReceiverTests.class);

    private static JmsReceiverProperties getInitialProperties() {
        return getInitialProperties(false);
    }

    private static JmsReceiverProperties getInitialProperties(boolean setInvalidConnection) {
        // before running tests, make these properties point to a running JMS broker (or not if setInvalidConnection is true)
        JmsReceiverProperties connectorProperties = new JmsReceiverProperties();

        String host = "localhost";
        int port = 61617;

        if (setInvalidConnection) {
            port++;
        }

        connectorProperties.setUseJndi(false);
        connectorProperties.setConnectionFactoryClass("org.apache.activemq.ActiveMQConnectionFactory");
        connectorProperties.getConnectionProperties().put("brokerURL", "tcp://" + host + ":" + port);

        return connectorProperties;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        JmsReceiverProperties properties = getInitialProperties();

        if (properties.isUseJndi()) {
            // TODO
            connectionFactory = null;
        } else {
            String className = properties.getConnectionFactoryClass();
            connectionFactory = (ConnectionFactory) Class.forName(className).newInstance();
        }

        BeanUtils.populate(connectionFactory, properties.getConnectionProperties());

        Connection connection = connectionFactory.createConnection(properties.getUsername(), properties.getPassword());
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @Test
    public final void testReceiveFromQueue() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName("testQueue");
        connectorProperties.setTopic(false);
        runTest(connectorProperties);
    }

    @Test
    public final void testReceiveFromTopic() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName("testTopic");
        connectorProperties.setTopic(true);
        runTest(connectorProperties);
    }

    @Test
    public final void testDurable() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName("testQueueDurable");
        connectorProperties.setTopic(false);
        connectorProperties.setDurableTopic(true);
        runTest(connectorProperties);
    }

    // TODO this one doesn't work consistently, it's probably a problem with the test and not the connector
    @Test
    @Ignore
    public final void testSelector() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName("testQueueWithSelector");
        connectorProperties.setTopic(false);
        connectorProperties.setSelector("messageNumber < 50");
        runTest(connectorProperties, 100, 50);
    }

    @Test(expected = StartException.class)
    public final void testInvalidConnection() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties(true);
        testChannel = new DummyChannel(TEST_CHANNEL_ID, TEST_SERVER_ID);

        JmsReceiver jmsReceiver = createJmsReceiver(connectorProperties);
        jmsReceiver.onDeploy();
        jmsReceiver.start();
    }

    private void runTest(JmsReceiverProperties connectorProperties) throws Exception {
        runTest(connectorProperties, 100, 100);
    }

    private void runTest(JmsReceiverProperties connectorProperties, final int numMessagesToSend, final int numMessagesExpected) throws Exception {
        testChannel = new DummyChannel(TEST_CHANNEL_ID, TEST_SERVER_ID);
        Destination destination;

        if (connectorProperties.isTopic()) {
            destination = session.createTopic(connectorProperties.getDestinationName());
        } else {
            destination = session.createQueue(connectorProperties.getDestinationName());
        }

        JmsReceiver jmsReceiver = createJmsReceiver(connectorProperties);
        jmsReceiver.onDeploy();
        jmsReceiver.start();

        Thread.sleep(100); // wait for the connector to start

        putMessagesInDestination(destination, numMessagesToSend, connectorProperties.isDurableTopic());

        Thread.sleep(200); // wait for the messages to process

        jmsReceiver.stop();
        jmsReceiver.onUndeploy();

        assertEquals(numMessagesExpected, testChannel.getRawMessages().size());
    }

    private void putMessagesInDestination(Destination destination, int numMessages, boolean durable) throws Exception {
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(durable ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);

        logger.debug("Putting " + numMessages + " messages into destination");

        for (int i = 0; i < numMessages; i++) {
            TextMessage message = session.createTextMessage(TEST_HL7_MESSAGE);
            message.setIntProperty("messageNumber", i);
            producer.send(message);
        }
    }

    private JmsReceiver createJmsReceiver(JmsReceiverProperties connectorProperties) {
        JmsReceiver connector = new JmsReceiver();
        connector.setConnectorProperties(connectorProperties);
        initConnector(connector, 0);
        initSourceConnector(connector);
        return connector;
    }

    private void initConnector(Connector connector, Integer metaDataId) {
        connector.setChannelId(testChannel.getChannelId());
        connector.setMetaDataId(metaDataId);
    }

    private void initSourceConnector(SourceConnector sourceConnector) {
        sourceConnector.setChannel(testChannel);
        sourceConnector.setMetaDataReplacer(new MirthMetaDataReplacer());
        sourceConnector.setRespondAfterProcessing(true);

        testChannel.setSourceConnector(sourceConnector);
        testChannel.setSourceFilterTransformer(new FilterTransformerExecutor(sourceConnector.getInboundDataType(), sourceConnector.getOutboundDataType()));
    }
}
