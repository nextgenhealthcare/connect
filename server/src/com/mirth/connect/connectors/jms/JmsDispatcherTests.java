package com.mirth.connect.connectors.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.PassthruEncryptor;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.passthru.PassthruDaoFactory;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;

public class JmsDispatcherTests {
    private final static String TEST_CHANNEL_ID = "testchannel";
    private final static String TEST_SERVER_ID = "testserver";
    private final static String TEST_HL7_MESSAGE = "MSH|^~\\&|LABNET|Acme Labs|||20090601105700||ORU^R01|HMCDOOGAL-0088|D|2.2\rPID|1|8890088|8890088^^^72777||McDoogal^Hattie^||19350118|F||2106-3|100 Beach Drive^Apt. 5^Mission Viejo^CA^92691^US^H||(949) 555-0025|||||8890088^^^72|604422825\rPV1|1|R|C3E^C315^B||||2^HIBBARD^JULIUS^|5^ZIMMERMAN^JOE^|9^ZOIDBERG^JOHN^|CAR||||4|||2301^OBRIEN, KEVIN C|I|1783332658^1^1||||||||||||||||||||DISNEY CLINIC||N|||20090514205600\rORC|RE|928272608|056696716^LA||CM||||20090601105600||||  C3E|||^RESULT PERFORMED\rOBR|1|928272608|056696716^LA|1001520^K|||20090601101300|||MLH25|||HEMOLYZED/VP REDRAW|20090601102400||2301^OBRIEN, KEVIN C||||01123085310001100100152023509915823509915800000000101|0000915200932|20090601105600||LAB|F||^^^20090601084100^^ST~^^^^^ST\rOBX|1|NM|1001520^K||5.3|MMOL/L|3.5-5.5||||F|||20090601105600|IIM|IIM\r";

    private static String queueName;
    private static String topicName;
    private static ConnectionFactory connectionFactory;
    private static Session session;
    private static InitialContext initialContext;

    private static JmsDispatcherProperties initActiveMQ(boolean setInvalidConnection) {
        // before running tests, make these properties point to a running JMS broker (or not if setInvalidConnection is true)
        JmsDispatcherProperties connectorProperties = new JmsDispatcherProperties();

        String host = "localhost";
        int port = 61616;

        if (setInvalidConnection) {
            port++;
        }

        connectorProperties.setUseJndi(false);
        connectorProperties.setConnectionFactoryClass("org.apache.activemq.ActiveMQConnectionFactory");
        connectorProperties.getConnectionProperties().put("brokerURL", "tcp://" + host + ":" + port);

        queueName = "testQueue";
        topicName = "testTopic";

        return connectorProperties;
    }

    private static JmsDispatcherProperties initJBoss(boolean setInvalidConnection) {
        JmsDispatcherProperties connectorProperties = new JmsDispatcherProperties();

        String host = "localhost";
        int port = 1099;

        if (setInvalidConnection) {
            port++;
        }

        connectorProperties.setUseJndi(true);
        connectorProperties.setJndiProviderUrl("jnp://" + host + ":" + port);
        connectorProperties.setJndiInitialContextFactory("org.jnp.interfaces.NamingContextFactory");
        connectorProperties.setJndiConnectionFactoryName("java:/ConnectionFactory");
        connectorProperties.setClientId("mirth");
        connectorProperties.setUsername("guest");
        connectorProperties.setPassword("guest");

        queueName = "queue/mirthQueue";
        topicName = "topic/mirthTopic";

        return connectorProperties;
    }

    private static JmsDispatcherProperties getInitialProperties() {
        return getInitialProperties(false);
    }

    private static JmsDispatcherProperties getInitialProperties(boolean setInvalidConnection) {
        return initActiveMQ(setInvalidConnection);
//        return initJBoss(setInvalidConnection);
    }

    private static ConnectionFactory lookupConnectionFactoryWithJndi(JmsConnectorProperties connectorProperties) throws Exception {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.PROVIDER_URL, connectorProperties.getJndiProviderUrl());
        env.put(Context.INITIAL_CONTEXT_FACTORY, connectorProperties.getJndiInitialContextFactory());
        env.put(Context.SECURITY_PRINCIPAL, connectorProperties.getUsername());
        env.put(Context.SECURITY_CREDENTIALS, connectorProperties.getPassword());

        initialContext = new InitialContext(env);
        String connectionFactoryName = connectorProperties.getJndiConnectionFactoryName();
        return (ConnectionFactory) initialContext.lookup(connectionFactoryName);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        JmsDispatcherProperties properties = getInitialProperties();

        if (properties.isUseJndi()) {
            connectionFactory = lookupConnectionFactoryWithJndi(properties);
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
    public void testSendToQueue() throws Exception {
        JmsDispatcherProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName(queueName);
        connectorProperties.setTopic(false);
        runTest(connectorProperties);
    }

    @Test
    public void testSendToTopic() throws Exception {
        JmsDispatcherProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName(topicName);
        connectorProperties.setTopic(true);
        runTest(connectorProperties);
    }

    @Test(expected = StartException.class)
    public final void testInvalidConnection() throws Exception {
        JmsDispatcherProperties connectorProperties = getInitialProperties(true);

        DestinationConnector connector = new TestJmsDispatcher(TEST_CHANNEL_ID, 1, connectorProperties);
        connector.onDeploy();
        connector.start();
    }

    private void runTest(JmsDispatcherProperties connectorProperties) throws Exception {
        runTest(connectorProperties, 100, 100);
    }

    private void runTest(JmsDispatcherProperties connectorProperties, final int numMessagesToSend, final int numMessagesExpected) throws Exception {
        DonkeyDao dao = new PassthruDaoFactory().getDao();
        long messageIdSequence = 1;
        Destination destination;

        if (connectorProperties.isUseJndi()) {
            destination = (Destination) initialContext.lookup(connectorProperties.getDestinationName());
        } else {
            if (connectorProperties.isTopic()) {
                destination = session.createTopic(connectorProperties.getDestinationName());
            } else {
                destination = session.createQueue(connectorProperties.getDestinationName());
            }
        }

        MessageConsumer consumer = session.createConsumer(destination);

        DestinationConnector connector = new TestJmsDispatcher(TEST_CHANNEL_ID, 1, connectorProperties);
        connector.onDeploy();
        connector.start();

        for (int i = 0; i < numMessagesToSend; i++) {
            ConnectorMessage message = new ConnectorMessage();
            message.setMessageId(messageIdSequence++);
            message.setChannelId(TEST_CHANNEL_ID);
            message.setChainId(1);
            message.setServerId(TEST_SERVER_ID);

            MessageContent rawContent = new MessageContent(message.getChannelId(), message.getMessageId(), message.getMetaDataId(), ContentType.RAW, TEST_HL7_MESSAGE, "HL7", null);
            MessageContent encodedContent = SerializationUtils.clone(rawContent);
            encodedContent.setContentType(ContentType.ENCODED);

            message.setRaw(rawContent);
            message.setEncoded(encodedContent);
            message.setStatus(Status.TRANSFORMED);

            connector.process(dao, message, Status.RECEIVED);
        }

        connector.stop();
        connector.onUndeploy();
        dao.close();

        Message message = null;
        int numMessagesReceived = 0;

        while (null != (message = consumer.receiveNoWait())) {
            assertTrue((message instanceof TextMessage));
            assertEquals(TEST_HL7_MESSAGE, ((TextMessage) message).getText());
            numMessagesReceived++;
        }

        assertEquals(numMessagesExpected, numMessagesReceived);
        consumer.close();
    }

    private class TestJmsDispatcher extends JmsDispatcher {
        public TestJmsDispatcher(String channelId, Integer metaDataId, JmsDispatcherProperties properties) {
            super();
            setEncryptor(new PassthruEncryptor());
            setChannelId(channelId);
            setMetaDataId(metaDataId);
            setConnectorProperties(properties);

            if (properties.getQueueConnectorProperties().isQueueEnabled()) {
                getQueue().setDataSource(new ConnectorMessageQueueDataSource(channelId, metaDataId, Status.QUEUED, isQueueRotate(), new PassthruDaoFactory(), new PassthruEncryptor()));
                getQueue().updateSize();
            }
        }
    }
}
