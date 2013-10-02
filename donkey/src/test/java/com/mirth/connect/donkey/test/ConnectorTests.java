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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestDataType;
import com.mirth.connect.donkey.test.util.TestDestinationConnector;
import com.mirth.connect.donkey.test.util.TestPollConnector;
import com.mirth.connect.donkey.test.util.TestPollConnectorProperties;
import com.mirth.connect.donkey.test.util.TestPostProcessor;
import com.mirth.connect.donkey.test.util.TestPreProcessor;
import com.mirth.connect.donkey.test.util.TestUtils;

public class ConnectorTests {
    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey.getInstance().startEngine(TestUtils.getDonkeyTestConfiguration());
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    /*
     * Create a new poll connector channel
     * Set the polling frequency to 500 ms
     * Starts the channel, waits 3250 ms, asserts that:
     * - 7 messages are processed by the channel
     */
    @Test
    @Ignore
    public final void testPollConnector() throws Exception {
        final int pollingFrequency = 500;
        final int sleepMillis = 3250;
        final int expectedMessageCount = 7;

        String channelId = TestUtils.DEFAULT_CHANNEL_ID;
        String serverId = TestUtils.DEFAULT_SERVER_ID;

        if (ChannelController.getInstance().channelExists(channelId)) {
            ChannelController.getInstance().deleteAllMessages(channelId);
        }
        
        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        ConnectorProperties connectorProperties = new TestPollConnectorProperties();
        ((PollConnectorPropertiesInterface) connectorProperties).getPollConnectorProperties().setPollingType(PollConnectorProperties.POLLING_TYPE_INTERVAL);
        ((PollConnectorPropertiesInterface) connectorProperties).getPollConnectorProperties().setPollingFrequency(pollingFrequency);

        SourceConnector sourceConnector = new TestPollConnector();
        sourceConnector.setConnectorProperties(connectorProperties);
        sourceConnector.setInboundDataType(new TestDataType());
        sourceConnector.setOutboundDataType(new TestDataType());
        sourceConnector.setMetaDataReplacer(new MetaDataReplacer());
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());
        channel.getResponseSelector().setRespondFromName(TestUtils.DEFAULT_RESPOND_FROM_NAME);

        TestDestinationConnector destinationConnector = (TestDestinationConnector) TestUtils.createDefaultDestinationConnector();
        destinationConnector.setChannelId(channelId);

        DestinationChain chain = new DestinationChain();
        chain.setChannelId(channelId);
        chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
        chain.addDestination(1, TestUtils.createDefaultFilterTransformerExecutor(), destinationConnector);
        channel.addDestinationChain(chain);

        channel.deploy();
        channel.start();
        Thread.sleep(sleepMillis);
        channel.stop();
        channel.undeploy();

        assertEquals(expectedMessageCount, channel.getNumMessages());
    }
}
