/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
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

    private static String queueName;
    private static String selectorQueueName;
    private static String topicName;
    private static String durableTopicName;
    private static DummyChannel testChannel;
    private static ConnectionFactory connectionFactory;
    private static Session session;
    private static InitialContext initialContext;
    private static Logger logger = Logger.getLogger(JmsReceiverTests.class);

    private static JmsReceiverProperties getInitialProperties() {
        return getInitialProperties(false);
    }

    private static JmsReceiverProperties initActiveMQ(boolean setInvalidConnection) {
        JmsReceiverProperties connectorProperties = new JmsReceiverProperties();

        String host = "localhost";
        int port = 61616;

        if (setInvalidConnection) {
            port++;
        }

        connectorProperties.setUseJndi(false);
        connectorProperties.setConnectionFactoryClass("org.apache.activemq.ActiveMQConnectionFactory");
        connectorProperties.getConnectionProperties().put("brokerURL", "tcp://" + host + ":" + port);
        connectorProperties.setClientId("mirth");

        queueName = "testQueue";
        selectorQueueName = "selectorQueue";
        topicName = "testTopic";
        durableTopicName = "durableTopic";

        return connectorProperties;
    }

    private static JmsReceiverProperties initJBoss(boolean setInvalidConnection) {
        JmsReceiverProperties connectorProperties = new JmsReceiverProperties();

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
        selectorQueueName = "queue/mirthQueue";
        topicName = "topic/mirthTopic";
        durableTopicName = "topic/mirthDurableTopic";

        return connectorProperties;
    }

//    private static JmsReceiverProperties initJBoss7(boolean setInvalidConnection) {
//        JmsReceiverProperties connectorProperties = new JmsReceiverProperties();
//        connectorProperties.setUseJndi(false);
//        connectorProperties.setConnectionFactoryClass("com.mirth.connect.connectors.jms.HornetQConnectionFactory");
//        connectorProperties.setClientId("mirth");
//        connectorProperties.getConnectionProperties().put("host", "localhost");
//        connectorProperties.getConnectionProperties().put("port", "5445");
//        connectorProperties.setUsername("guest");
//        connectorProperties.setPassword("guest");
//
//        if (setInvalidConnection) {
//            connectorProperties.getConnectionProperties().put("port", "5446");
//        }
//
//        queueName = "queue/mirthQueue";
//        selectorQueueName = "queue/mirthQueue";
//        topicName = "topic/mirthTopic";
//        durableTopicName = "topic/mirthDurableTopic";
//
//        return connectorProperties;
//    }
//    
//    private static JmsReceiverProperties initJBoss7Jndi(boolean setInvalidConnection) {
//        JmsReceiverProperties connectorProperties = new JmsReceiverProperties();
//
//        String host = "localhost";
//        int port = 4447;
//
//        if (setInvalidConnection) {
//            port++;
//        }
//
//        connectorProperties.setUseJndi(true);
//        connectorProperties.setJndiProviderUrl("remote://" + host + ":" + port);
//        connectorProperties.setJndiInitialContextFactory("org.jboss.naming.remote.client.InitialContextFactory");
//        connectorProperties.setJndiConnectionFactoryName("jms/RemoteConnectionFactory");
//        connectorProperties.setClientId("mirth");
//        connectorProperties.setUsername("mirth");
//        connectorProperties.setPassword("d1scgo1fisfun");
//
//        queueName = "queue/mirthQueue";
//        selectorQueueName = "queue/mirthQueue";
//        topicName = "topic/mirthTopic";
//        durableTopicName = "topic/mirthDurableTopic";
//
//        return connectorProperties;
//    }

    private static JmsReceiverProperties getInitialProperties(boolean setInvalidConnection) {
        return initActiveMQ(setInvalidConnection);
//        return initJBoss(setInvalidConnection);
//        return initJBoss7Jndi(setInvalidConnection);
    }

    private static ConnectionFactory lookupConnectionFactoryWithJndi(JmsConnectorProperties connectorProperties) throws Exception {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
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
        JmsReceiverProperties properties = getInitialProperties();

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
    public final void testReceiveFromQueue() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName(queueName);
        connectorProperties.setTopic(false);
        runTest(connectorProperties, 0, 100, 100);
    }

    @Test
    public final void testReceiveFromTopic() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName(topicName);
        connectorProperties.setTopic(true);
        runTest(connectorProperties, 0, 100, 100);
    }

    @Test
    public final void testDurable() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties();
        connectorProperties.setDestinationName(durableTopicName);
        connectorProperties.setTopic(true);
        connectorProperties.setDurableTopic(true);
        flushTopic(connectorProperties);
        runTest(connectorProperties, 100, 100, 200);
        connectorProperties.setDurableTopic(false);
        runTest(connectorProperties, 100, 100, 100);
    }

    @Test(expected = StartException.class)
    public final void testInvalidConnection() throws Exception {
        JmsReceiverProperties connectorProperties = getInitialProperties(true);
        testChannel = new DummyChannel(TEST_CHANNEL_ID, TEST_SERVER_ID);

        JmsReceiver jmsReceiver = createJmsReceiver(connectorProperties);
        jmsReceiver.onDeploy();
        jmsReceiver.start();
    }

    private void runTest(JmsReceiverProperties connectorProperties, final int numSendBeforeConnect, final int numSendAfterConnect, final int numMessagesExpected) throws Exception {
        testChannel = new DummyChannel(TEST_CHANNEL_ID, TEST_SERVER_ID);
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

        putMessagesInDestination(destination, numSendBeforeConnect);

        JmsReceiver jmsReceiver = createJmsReceiver(connectorProperties);
        jmsReceiver.onDeploy();
        jmsReceiver.start();

        Thread.sleep(500); // wait for the connector to start

        putMessagesInDestination(destination, numSendAfterConnect);

        Thread.sleep(1000); // wait for the messages to process

        jmsReceiver.stop();
        jmsReceiver.onUndeploy();

        assertEquals(numMessagesExpected, testChannel.getRawMessages().size());
    }

    private void flushTopic(JmsReceiverProperties connectorProperties) throws Exception {
        JmsReceiver jmsReceiver = createJmsReceiver(connectorProperties);
        jmsReceiver.onDeploy();
        jmsReceiver.start();
        Thread.sleep(1000);
        jmsReceiver.stop();
        jmsReceiver.onUndeploy();
    }

    private void putMessagesInDestination(Destination destination, int numMessages) throws Exception {
        if (numMessages > 0) {
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            logger.debug("Putting " + numMessages + " messages into destination");

            for (int i = 0; i < numMessages; i++) {
                TextMessage message = session.createTextMessage(TEST_HL7_MESSAGE);
                message.setIntProperty("messageNumber", i);
                producer.send(message);
            }
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
        sourceConnector.setChannelId(testChannel.getChannelId());
        sourceConnector.setChannel(testChannel);
        sourceConnector.setMetaDataReplacer(new MirthMetaDataReplacer());
        sourceConnector.setRespondAfterProcessing(true);

        testChannel.setSourceConnector(sourceConnector);
        testChannel.setSourceFilterTransformer(new FilterTransformerExecutor(sourceConnector.getInboundDataType(), sourceConnector.getOutboundDataType()));
    }
}
