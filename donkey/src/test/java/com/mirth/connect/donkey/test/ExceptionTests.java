/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.donkey.server.channel.ResponseSelector;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestConnectorProperties;
import com.mirth.connect.donkey.test.util.TestDataType;
import com.mirth.connect.donkey.test.util.TestDestinationConnector;
import com.mirth.connect.donkey.test.util.TestPostProcessor;
import com.mirth.connect.donkey.test.util.TestPreProcessor;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;

public class ExceptionTests {
    private static int TEST_SIZE = 10;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;
    private static String testMessage = TestUtils.TEST_HL7_MESSAGE;

    private Logger logger = Logger.getLogger(this.getClass());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey.getInstance().startEngine(TestUtils.getDonkeyTestConfiguration());
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    /*
     * Create a new channel with a TestSourceConnector that always throws an
     * exception from onStop().
     * Start the channel, then pause it, and assert:
     * - A PauseException was thrown
     * - The source connector is now stopped
     * 
     * Pause the channel again, and assert:
     * - A PauseException was thrown
     */
    @Test
    final public void testChannelPause() throws Exception {
        ChannelController.getInstance().getLocalChannelId(channelId);

        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        SourceConnector sourceConnector = new TestSourceConnector() {
            @Override
            public void onStop() throws StopException {
                throw new StopException("testing");
            }
        };

        sourceConnector.setConnectorProperties(new TestConnectorProperties());
        sourceConnector.setInboundDataType(new TestDataType());
        sourceConnector.setOutboundDataType(new TestDataType());
        sourceConnector.setMetaDataReplacer(new MetaDataReplacer());

        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.setResponseSelector(new ResponseSelector(sourceConnector.getInboundDataType()));
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());
        channel.getResponseSelector().setRespondFromName(TestUtils.DEFAULT_RESPOND_FROM_NAME);

        TestDestinationConnector destinationConnector = (TestDestinationConnector) TestUtils.createDefaultDestinationConnector();
        destinationConnector.setChannelId(channel.getChannelId());

        DestinationChain chain = new DestinationChain();
        chain.setChannelId(channel.getChannelId());
        chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
        chain.setMetaDataColumns(channel.getMetaDataColumns());
        chain.addDestination(1, TestUtils.createDefaultFilterTransformerExecutor(), destinationConnector);
        channel.addDestinationChain(chain);

        try {
            channel.deploy();
            channel.start();

            // Assert that pausing the channel will cause an exception to be thrown from the source connector
            Exception e = null;
            try {
                sourceConnector.stop();
            } catch (StopException e2) {
                e = e2;
            }
            assertNotNull(e);

            // Assert that the source connector is now stopped
            assertFalse(channel.getSourceConnector().getCurrentState().equals(DeployedState.STOPPED));

            // Assert that pausing the channel while the source connector is stopped will throw an exception
            e = null;
            try {
                sourceConnector.stop();
            } catch (StopException e2) {
                e = e2;
            }
            assertNotNull(e);
        } finally {
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Start up a new channel and create a new source connector message
     * Set the metadata ID to 1, process the message, and assert:
     * - An InvalidConnectorMessageState exception was thrown
     * 
     * Reset the metadata ID, set the status to TRANSFORMER, process the
     * message, and assert that:
     * - An InvalidConnectorMessageState exception was thrown
     * 
     * Reset the status, change the preprocessor to one that always throws
     * exceptions, process the message, and assert that:
     * - No exception was thrown
     * - The connector message status was set to ERROR
     * - The connector message errors field was set
     * 
     * Create a new source connector message
     * Set the channel's destination chain to one that always throws exceptions
     * during call()
     * Process the message, and assert:
     * - No exception was thrown
     * - Channel.process returns a MessageResponse
     */
    @Test
    final public void testChannelProcess() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);

        try {
            logger.info("Testing Channel.process exceptions...");

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
            channel.deploy();
            channel.start();

            ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId()).getConnectorMessages().get(0);

            // Assert that an error in the preprocessor will update the message status to ERROR
            channel.setPreProcessor(new TestPreProcessor() {
                @Override
                public String doPreProcess(ConnectorMessage message) throws DonkeyException {
                    throw new DonkeyException("testing", "formatted error message");
                }
            });
            channel.process(sourceMessage, true);

            assertEquals(Status.ERROR, sourceMessage.getStatus());
            assertNotNull(sourceMessage.getProcessingError());
        } finally {
            channel.stop();
            channel.undeploy();
        }
    }
}
