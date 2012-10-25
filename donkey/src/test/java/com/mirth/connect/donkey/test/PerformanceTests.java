package com.mirth.connect.donkey.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.MessageResponse;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.timed.TimedDaoFactory;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestChannelWriter;
import com.mirth.connect.donkey.test.util.TestConnectorProperties;
import com.mirth.connect.donkey.test.util.TestUtils;
import com.mirth.connect.donkey.test.util.TestUtils.MessageStorageMode;
import com.mirth.connect.donkey.util.ActionTimer;

public class PerformanceTests {
    private final static String TEST_MESSAGE = "src/test/java/com/mirth/connect/donkey/test/hl7/large.hl7";
    private final static int DEFAULT_TEST_MILLIS = 5000;
    private final static int DEFAULT_WARMUP_MILLIS = 0;
    private final static int DEFAULT_CHANNELS = 1;
    private final static int DEFAULT_CHAINS = 10;
    private final static int DEFAULT_DEST_PER_CHAIN = 1;
    private final static boolean DEFAULT_WAIT_FOR_DESTINATIONS = true;

    private static String testMessage;
    private static ActionTimer daoTimer = null;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;

    @BeforeClass
    final public static void beforeClass() throws Exception {
        testMessage = FileUtils.readFileToString(new File(TEST_MESSAGE));

        Donkey donkey = Donkey.getInstance();
        donkey.startEngine(TestUtils.getDonkeyTestConfiguration());

        if (daoTimer != null) {
            donkey.setDaoFactory(new TimedDaoFactory(donkey.getDaoFactory(), daoTimer));
        }
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    @Before
    final public void before() {
        if (daoTimer != null) {
            daoTimer.reset();
        }
    }

    @Test
    public final void testDevelopment() throws Exception {
        testStorageMode(MessageStorageMode.DEVELOPMENT);
    }

    @Test
    public final void testProduction() throws Exception {
        testStorageMode(MessageStorageMode.PRODUCTION);
    }

    @Test
    public final void testRaw() throws Exception {
        testStorageMode(MessageStorageMode.RAW);
    }

    @Test
    public final void testMetadata() throws Exception {
        testStorageMode(MessageStorageMode.METADATA);
    }

    @Test
    public final void testDisabled() throws Exception {
        testStorageMode(MessageStorageMode.DISABLED);
    }

    private void testStorageMode(MessageStorageMode mode) throws Exception {
        //TestUtils.showContent(testMessage, channelId, serverId, TestUtils.getStorageSettings(mode, false), TestUtils.getStorageSettings(mode, true));

        TestUtils.runChannelTest(testMessage, channelId, serverId, null, DEFAULT_CHANNELS, DEFAULT_CHAINS, DEFAULT_DEST_PER_CHAIN, DEFAULT_WAIT_FOR_DESTINATIONS, null, DEFAULT_TEST_MILLIS, DEFAULT_WARMUP_MILLIS, TestUtils.getStorageSettings(mode, false), TestUtils.getStorageSettings(mode, true));
    }

    @Test
    public final void testMultipleSources() throws Exception {
        final int testSize = 50;
        final int numSources = 5;
        final boolean waitForDestinations = true;

        TestChannel destChannel = TestUtils.createDefaultChannel(channelId, serverId, waitForDestinations, 1, 1);
        destChannel.deploy();
        destChannel.start();

        Channel[] sourceChannels = new Channel[numSources];

        for (int i = 0; i < numSources; i++) {
            sourceChannels[i] = TestUtils.createDefaultChannel(channelId + i, serverId, waitForDestinations, 1, 1);
            ChannelController.getInstance().deleteAllMessages(sourceChannels[i].getChannelId());

            DestinationChain chain = sourceChannels[i].getDestinationChains().get(0);

            TestChannelWriter channelWriter = new TestChannelWriter(destChannel);
            channelWriter.setChannelId(channelId);
            channelWriter.setConnectorProperties(new TestConnectorProperties());
            channelWriter.setDestinationName("destination");
            chain.addDestination(1, TestUtils.createDefaultFilterTransformerExecutor(), channelWriter);

            sourceChannels[i].deploy();
            sourceChannels[i].start();
        }

        List<Long> messageIds = new ArrayList<Long>();
        List<Long> messageTimes = new ArrayList<Long>();
        long overallStartTime = System.currentTimeMillis();

        for (int i = 0; i < testSize; i++) {
            for (int j = 0; j < sourceChannels.length; j++) {
                MessageResponse messageResponse = null;
                long startTime = System.currentTimeMillis();

                try {
                    messageResponse = sourceChannels[j].handleRawMessage(new RawMessage(testMessage));
                } finally {
                    sourceChannels[j].storeMessageResponse(messageResponse);
                }

                messageTimes.add(System.currentTimeMillis() - startTime);
                messageIds.add(messageResponse.getMessageId());
            }

            MessageResponse messageResponse = null;
            long startTime = System.currentTimeMillis();

            try {
                messageResponse = destChannel.handleRawMessage(new RawMessage(testMessage));
            } finally {
                destChannel.storeMessageResponse(messageResponse);
            }

            messageTimes.add(System.currentTimeMillis() - startTime);
            messageIds.add(messageResponse.getMessageId());
        }

        for (int i = 0; i < numSources; i++) {
            sourceChannels[i].stop();
        }

        long overallEndTime = System.currentTimeMillis();

        for (int i = 0; i < numSources; i++) {
            sourceChannels[i].undeploy();
        }

        destChannel.stop();
        destChannel.undeploy();

        // assert that the destination channel received the correct number of messages
        assertEquals(testSize * (sourceChannels.length + 1), destChannel.getNumMessages());

        System.out.println(TestUtils.getPerformanceText(1, overallEndTime - overallStartTime, messageTimes));
    }
}
