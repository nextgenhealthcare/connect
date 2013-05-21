/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.FilterTransformerResult;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerException;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.donkey.test.util.TestAutoResponder;
import com.mirth.connect.donkey.test.util.TestDataType;
import com.mirth.connect.donkey.test.util.TestFilterTransformer;
import com.mirth.connect.donkey.test.util.TestResponseValidator;
import com.mirth.connect.donkey.test.util.TestSerializer;
import com.mirth.connect.donkey.test.util.TestUtils;

public class FilterTransformerTests {
    private static int TEST_SIZE = 10;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;
    private static String testMessage = TestUtils.TEST_HL7_MESSAGE;

    private Logger logger = Logger.getLogger(this.getClass());

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey.getInstance().startEngine(TestUtils.getDonkeyTestConfiguration());
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    /*
     * For each test, create a new connector message with some raw content
     * 
     * Set the inbound data type to a FailingTestDataType which always throws a
     * SerializerException
     * Process the connector message, and assert that:
     * - The status is ERROR
     * - The transformed content is null
     * - The encoded content is null
     * 
     * Set the filter/transformer interface to one that always returns false
     * Process the connector message, and assert that:
     * - The status is FILTERED
     * - The transformed content is not null
     * - The encoded content is null
     * 
     * Set the filter/transformer interface to one that always throws a
     * FilterTransformerException
     * Process the connector message, and assert that:
     * - The status is ERROR
     * - The transformed content is not null
     * - The encoded content is null
     * 
     * Set the processed raw content on the connector message
     * Process the connector message, and assert that:
     * - The status is TRANSFORMED
     * - The transformed content is not null
     * - The encoded content is not null
     * - The transformed content is equal to the serialized processed raw
     * content
     * - The encoded content is equal to the serialized/deserialized processed
     * raw content
     */
    @Test
    public void testWithFilterTransformer() throws Exception {
        ConnectorMessage connectorMessage;
        FilterTransformerExecutor filterTransformerExecutor;

        class FailingTestSerializer implements XmlSerializer {
            @Override
            public boolean isSerializationRequired(boolean isXml) {
                return false;
            }

            @Override
            public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
                return message;
            }

            @Override
            public String toXML(String message) throws SerializerException {
                throw new SerializerException("Inbound serialization failed.");
            }

            @Override
            public String fromXML(String message) throws SerializerException {
                throw new SerializerException("Outbound serialization failed.");
            }
        }

        class FailingTestDataType extends DataType {
            public FailingTestDataType() {
                super("HL7V2", new FailingTestSerializer(), null, new TestAutoResponder(), new TestResponseValidator());
            }
        }

        logger.info("Testing FilterTransformerExecutor.processConnectorMessage with a FilterTransformer...");

        /*
         * Assert that if inbound serialization failed, the status is set to
         * ERROR, and the transformed/encoded content is not set
         */
        filterTransformerExecutor = new FilterTransformerExecutor(new FailingTestDataType(), new TestDataType());
        filterTransformerExecutor.setFilterTransformer(new TestFilterTransformer());
        for (int i = 1; i <= TEST_SIZE; i++) {
            connectorMessage = new ConnectorMessage(channelId, 1, 1, serverId, Calendar.getInstance(), Status.RECEIVED);
            connectorMessage.setRaw(new MessageContent(channelId, 1, 1, ContentType.RAW, testMessage, "HL7V2", false));

            try {
                filterTransformerExecutor.processConnectorMessage(connectorMessage);
            } catch (SerializerException e) {
            }

            assertNull(connectorMessage.getTransformed());
            assertNull(connectorMessage.getEncoded());
        }

        /*
         * Assert that if the message is filtered, the status is set to
         * FILTERED, the transformed content is set, and the encoded content is
         * not set
         */
        filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new TestDataType());
        filterTransformerExecutor.setFilterTransformer(new TestFilterTransformer() {
            @Override
            public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException {
                return new FilterTransformerResult(true, null);
            }
        });
        for (int i = 1; i <= TEST_SIZE; i++) {
            connectorMessage = new ConnectorMessage(channelId, 1, 1, serverId, Calendar.getInstance(), Status.RECEIVED);
            connectorMessage.setRaw(new MessageContent(channelId, 1, 1, ContentType.RAW, testMessage, "HL7V2", false));

            try {
                filterTransformerExecutor.processConnectorMessage(connectorMessage);
            } catch (FilterTransformerException e) {
            }

            assertEquals(Status.FILTERED, connectorMessage.getStatus());
            assertNotNull(connectorMessage.getTransformed());
            assertNull(connectorMessage.getEncoded());
        }

        /*
         * Assert that if the filter/transformer interface throws an exception,
         * then the status is set to ERROR, the transformed content is set, and
         * the encoded content is not set
         */
        filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new TestDataType());
        filterTransformerExecutor.setFilterTransformer(new TestFilterTransformer() {
            @Override
            public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException {
                throw new FilterTransformerException("Failed to run filter/transformer.", new Exception());
            }
        });
        for (int i = 1; i <= TEST_SIZE; i++) {
            connectorMessage = new ConnectorMessage(channelId, 1, 1, serverId, Calendar.getInstance(), Status.RECEIVED);
            connectorMessage.setRaw(new MessageContent(channelId, 1, 1, ContentType.RAW, testMessage, "HL7V2", false));

            try {
                filterTransformerExecutor.processConnectorMessage(connectorMessage);
            } catch (FilterTransformerException e) {
            }

            assertNotNull(connectorMessage.getTransformed());
            assertNull(connectorMessage.getEncoded());
        }

        /*
         * Assert that if the outbound deserialization fails, then the status is
         * set to ERROR, the transformed content is set, and the encoded content
         * is not set
         */
        filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new FailingTestDataType());
        filterTransformerExecutor.setFilterTransformer(new TestFilterTransformer());
        for (int i = 1; i <= TEST_SIZE; i++) {
            connectorMessage = new ConnectorMessage(channelId, 1, 1, serverId, Calendar.getInstance(), Status.RECEIVED);
            connectorMessage.setRaw(new MessageContent(channelId, 1, 1, ContentType.RAW, testMessage, "HL7V2", false));

            try {
                filterTransformerExecutor.processConnectorMessage(connectorMessage);
            } catch (DonkeyException e) {
            }

            assertNotNull(connectorMessage.getTransformed());
            assertNull(connectorMessage.getEncoded());
        }

        /*
         * Assert that if everything runs without errors, then the status is set
         * to TRANSFORMED, and the transformed/encoded content is set
         */
        filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new TestDataType());
        filterTransformerExecutor.setFilterTransformer(new TestFilterTransformer());
        for (int i = 1; i <= TEST_SIZE; i++) {
            connectorMessage = new ConnectorMessage(channelId, 1, 1, serverId, Calendar.getInstance(), Status.RECEIVED);
            connectorMessage.setRaw(new MessageContent(channelId, 1, 1, ContentType.RAW, testMessage, "HL7V2", false));
            filterTransformerExecutor.processConnectorMessage(connectorMessage);

            assertEquals(Status.TRANSFORMED, connectorMessage.getStatus());
            assertNotNull(connectorMessage.getTransformed());
            assertNotNull(connectorMessage.getEncoded());
        }

        /*
         * Assert that if the processed raw content is set, then the status is
         * set to TRANSFORMED, the transformed/encoded content is set, and the
         * transformed content is the serialized processed raw content rather
         * than the raw content
         */
        filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new TestDataType());
        filterTransformerExecutor.setFilterTransformer(new TestFilterTransformer());
        for (int i = 1; i <= TEST_SIZE; i++) {
            connectorMessage = new ConnectorMessage(channelId, 1, 1, serverId, Calendar.getInstance(), Status.RECEIVED);
            connectorMessage.setRaw(new MessageContent(channelId, 1, 1, ContentType.RAW, "", "HL7V2", false));
            connectorMessage.setProcessedRaw(new MessageContent(channelId, 1, 1, ContentType.PROCESSED_RAW, testMessage, "HL7V2", false));
            filterTransformerExecutor.processConnectorMessage(connectorMessage);

            assertEquals(Status.TRANSFORMED, connectorMessage.getStatus());
            assertNotNull(connectorMessage.getTransformed());
            assertNotNull(connectorMessage.getEncoded());
            assertEquals((new TestSerializer()).toXML(testMessage), connectorMessage.getTransformed().getContent());
            assertEquals((new TestSerializer()).fromXML((new TestSerializer()).toXML(testMessage)), connectorMessage.getEncoded().getContent());
        }
    }

    /*
     * For each test, create a new connector message with some raw content
     * 
     * Process the connector message, and assert that:
     * - The status is TRANSFORMED
     * - The transformed content is null
     * - The encoded content is equal to the raw content
     * 
     * Set the processed raw content on the connector message
     * Process the connector message, and assert that:
     * - The status is TRANSFORMED
     * - The transformed content is null
     * - The encoded content is equal to the processed raw content
     */
    @Test
    public void testWithoutFilterTransformer() throws Exception {
        ConnectorMessage connectorMessage;
        FilterTransformerExecutor filterTransformerExecutor;

        logger.info("Testing FilterTransformerExecutor.processConnectorMessage without a FilterTransformer...");

        /*
         * Assert that if the processed raw content is not set, then the status
         * is set to TRANSFORMED, the transformed content is not set, and the
         * encoded content is set to the raw content
         */
        filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new TestDataType());
        for (int i = 1; i <= TEST_SIZE; i++) {
            connectorMessage = new ConnectorMessage(channelId, 1, 1, serverId, Calendar.getInstance(), Status.RECEIVED);
            connectorMessage.setRaw(new MessageContent(channelId, 1, 1, ContentType.RAW, testMessage, "HL7V2", false));
            filterTransformerExecutor.processConnectorMessage(connectorMessage);

            assertEquals(Status.TRANSFORMED, connectorMessage.getStatus());
            assertNull(connectorMessage.getTransformed());
            assertEquals(testMessage, connectorMessage.getEncoded().getContent());
        }

        /*
         * Assert that if the processed raw content is set, then the status is
         * set to TRANSFORMED, the transformed content is not set, the encoded
         * content is set to the processed raw content rather than the raw
         * content
         */
        filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new TestDataType());
        for (int i = 1; i <= TEST_SIZE; i++) {
            connectorMessage = new ConnectorMessage(channelId, 1, 1, serverId, Calendar.getInstance(), Status.RECEIVED);
            connectorMessage.setRaw(new MessageContent(channelId, 1, 1, ContentType.RAW, "", "HL7V2", false));
            connectorMessage.setProcessedRaw(new MessageContent(channelId, 1, 1, ContentType.PROCESSED_RAW, testMessage, "HL7V2", false));
            filterTransformerExecutor.processConnectorMessage(connectorMessage);

            assertEquals(Status.TRANSFORMED, connectorMessage.getStatus());
            assertNull(connectorMessage.getTransformed());
            assertEquals(testMessage, connectorMessage.getEncoded().getContent());
        }
    }
}
