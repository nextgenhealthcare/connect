/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.DonkeyConfiguration;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.plugins.messagepruner.MessagePrunerWithArchiver.Strategy;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.tests.TestUtils;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterException;

public class MessagePrunerTest {
    private final static int TEST_POWER = 10;
    private final static int PERFORMANCE_TEST_POWER = 14;
    private final static int ARCHIVER_PAGE_SIZE = 1000;
    private final static String TEST_CHANNEL_ID = "prunerTestChannel";
    private final static String TEST_SERVER_ID = "testServerId";
    private final static String TEST_MESSAGE_CONTENT = TestUtils.TEST_HL7_MESSAGE;

    // @formatter:off
    private static Calendar messageDateThreshold;
    private static Calendar contentDateThreshold;
    private static int testSize;
    private static MessageWriter[] archivers = new MessageWriter[] { new TestArchiver(), new FailingArchiver() };
    private static int[] blockSizes = new int[] { 0, 1000 };
    private static Logger logger = Logger.getLogger(MessagePrunerTest.class);
    // @formatter:on

    @BeforeClass
    public final static void init() throws Exception {
        ConfigurationController configurationController = ConfigurationController.getInstance();
        configurationController.initializeSecuritySettings();
        configurationController.initializeDatabaseSettings();

        Donkey donkey = Donkey.getInstance();
        donkey.startEngine(new DonkeyConfiguration(configurationController.getApplicationDataDir(), configurationController.getDatabaseSettings().getProperties(), null));

        ChannelController.getInstance().initChannelStorage(TEST_CHANNEL_ID);

        messageDateThreshold = Calendar.getInstance();
        messageDateThreshold.set(Calendar.DAY_OF_MONTH, messageDateThreshold.get(Calendar.DAY_OF_MONTH) - 90);

        contentDateThreshold = Calendar.getInstance();
        contentDateThreshold.set(Calendar.DAY_OF_MONTH, contentDateThreshold.get(Calendar.DAY_OF_MONTH) - 45);

        testSize = (int) Math.pow(2, TEST_POWER);
    }

    @Test
    public void testPruneAll() throws Exception {
        runPrunerTests(true, true);
    }

    @Test
    public void testPruneContentOnly() throws Exception {
        runPrunerTests(false, true);
    }

    private void runPrunerTests(boolean messagesPrunable, boolean contentPrunable) throws Exception {
        MessagePruner pruner = new MessagePrunerWithoutArchiver();
        runPrunerTest(pruner, messagesPrunable, contentPrunable);

        for (MessageWriter archiver : archivers) {
            for (Strategy strategy : Strategy.values()) {
                for (int blockSize : blockSizes) {
                    if (archiver instanceof FailingArchiver) {
                        ((FailingArchiver) archiver).getArchivedMessageIds().clear();
                        ((FailingArchiver) archiver).getFailedMessageIds().clear();
                    } else if (archiver instanceof TestArchiver) {
                        ((TestArchiver) archiver).getArchivedMessageIds().clear();
                    }

                    pruner = new MessagePrunerWithArchiver(archiver, ARCHIVER_PAGE_SIZE, strategy);
                    pruner.setRetryCount(0);
                    pruner.setBlockSize(blockSize);

                    logger.info("Running pruner test, archiver: " + archiver.getClass().getSimpleName() + ", strategy: " + strategy + ", block size: " + blockSize + ", prune messages: " + messagesPrunable + ", prune content: " + contentPrunable);
                    runPrunerTest(pruner, messagesPrunable, contentPrunable);
                }
            }
        }
    }

    private void runPrunerTest(MessagePruner pruner, boolean messagesPrunable, boolean contentPrunable) throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, messagesPrunable, contentPrunable, true, Status.SENT, TEST_POWER);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        int prunedCount;

        if (pruner instanceof MessagePrunerWithArchiver && ((MessagePrunerWithArchiver) pruner).getArchiver() instanceof FailingArchiver) {
            FailingArchiver archiver = (FailingArchiver) ((MessagePrunerWithArchiver) pruner).getArchiver();
            prunedCount = archiver.getFailedMessageIds().size();
        } else {
            prunedCount = 0;
        }

        assertEquals(messagesPrunable ? prunedCount : testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(contentPrunable ? prunedCount : testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public void testPruneNone() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, false, false, true, Status.SENT, TEST_POWER);
        MessagePruner pruner = new MessagePrunerWithoutArchiver();
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public final void testPruneSkipIncomplete() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, false, Status.SENT, TEST_POWER);
        MessagePruner pruner = new MessagePrunerWithoutArchiver();
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public final void testPruneIncomplete() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, false, Status.SENT, TEST_POWER);
        MessagePruner pruner = new MessagePrunerWithoutArchiver();
        pruner.setSkipIncomplete(false);
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public final void testPruneQueued() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, true, Status.QUEUED, TEST_POWER);
        MessagePruner pruner = new MessagePrunerWithoutArchiver();
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public final void testPruneError() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, true, Status.ERROR, TEST_POWER);
        MessagePruner pruner = new MessagePrunerWithoutArchiver();
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    @Ignore
    public final void testPerformance() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, true, Status.SENT, PERFORMANCE_TEST_POWER);

        FailingArchiver archiver = new FailingArchiver();
        MessagePruner pruner = new MessagePrunerWithArchiver(archiver, 1000, Strategy.INCLUDE_LIST);

        long startTime = System.currentTimeMillis();
        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold);
        long endTime = System.currentTimeMillis();

        logger.info("Archiver/Pruner executed in " + (endTime - startTime) + "ms");

        assertEquals(archiver.getFailedMessageIds().size(), TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(archiver.getFailedMessageIds().size(), TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

//    @Test
//    public final void testDerbyDeleteCascade() throws Exception {
//        ChannelController.getInstance().initChannelStorage(TEST_CHANNEL_ID);
//
//        Message message = MessageController.getInstance().createNewMessage(TEST_CHANNEL_ID, TEST_SERVER_ID);
//        message.setReceivedDate(Calendar.getInstance());
//        message.setProcessed(true);
//
//        ConnectorMessage sourceMessage = new ConnectorMessage(TEST_CHANNEL_ID, message.getMessageId(), 0, TEST_SERVER_ID, message.getReceivedDate(), Status.RECEIVED);
//        message.getConnectorMessages().put(0, sourceMessage);
//
//        ConnectorMessage destinationMessage = new ConnectorMessage(TEST_CHANNEL_ID, message.getMessageId(), 1, TEST_SERVER_ID, message.getReceivedDate(), Status.SENT);
//        message.getConnectorMessages().put(1, destinationMessage);
//
//        sourceMessage.setRaw(new MessageContent(TEST_CHANNEL_ID, message.getMessageId(), 0, ContentType.RAW, TEST_MESSAGE_CONTENT, null, null));
//        destinationMessage.setRaw(new MessageContent(TEST_CHANNEL_ID, message.getMessageId(), 1, ContentType.RAW, TEST_MESSAGE_CONTENT, null, null));
//
//        TestUtils.deleteAllMessages(TEST_CHANNEL_ID);
//        TestUtils.createTestMessages(TEST_CHANNEL_ID, message, 1);
//        TestUtils.deleteAllMessages(TEST_CHANNEL_ID);
//    }

//    @Test
//    public final void testPrunerConcurrency() throws Exception {
//        final int channelTestSize = 30000;
//        final int power = 10;
//
//        logger.info("Starting pruner concurrency test");
//
//        ChannelController.getInstance().initChannelStorage(TEST_CHANNEL_ID);
//        final Channel channel = TestUtils.createChannel(TEST_CHANNEL_ID, TEST_SERVER_ID, true, 4, 1);
//        prepareTestMessages(TEST_CHANNEL_ID, true, false, true, Status.SENT, power);
//
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Future<Void> future = executor.submit(new Callable<Void>() {
//            @Override
//            public Void call() {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//
//                logger.info("Running channel test");
//                long startTime = System.currentTimeMillis();
//
//                try {
//                    TestUtils.runChannelTest(channel, TestUtils.TEST_HL7_MESSAGE, channelTestSize);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                long duration = System.currentTimeMillis() - startTime;
//                logger.info("Channel test completed in " + duration + "ms");
//                return null;
//            }
//        });
//
//        logger.info("Executing pruner");
//        pruner.executePruner(TEST_CHANNEL_ID, messageDateThreshold, null);
//        logger.info("Pruner completed");
//
//        future.get();
//        executor.shutdown();
//    }

    private static void prepareTestMessages(String channelId, boolean messagesPrunable, Boolean contentPrunable, boolean processed, Status destinationStatus, int power) throws Exception {
        logger.debug("Preparing " + ((int) Math.pow(2, power)) + " test messages");
        Calendar dateThreshold;

        TestUtils.deleteAllMessages(channelId);

        if (messagesPrunable) {
            dateThreshold = messageDateThreshold;
        } else if (contentPrunable) {
            dateThreshold = contentDateThreshold;
        } else {
            dateThreshold = Calendar.getInstance();
        }

        Calendar receivedDate = Calendar.getInstance();
        receivedDate.setTime(dateThreshold.getTime());
        receivedDate.set(Calendar.DAY_OF_MONTH, receivedDate.get(Calendar.DAY_OF_MONTH) - 1);

        Message message = MessageController.getInstance().createNewMessage(channelId, TEST_SERVER_ID);
        message.setMessageId(1L);
        message.setReceivedDate(receivedDate);
        message.setProcessed(processed);

        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, message.getMessageId(), 0, TEST_SERVER_ID, message.getReceivedDate(), Status.RECEIVED);
        message.getConnectorMessages().put(0, sourceMessage);

        ConnectorMessage destinationMessage = new ConnectorMessage(channelId, message.getMessageId(), 1, TEST_SERVER_ID, message.getReceivedDate(), destinationStatus);
        message.getConnectorMessages().put(1, destinationMessage);

        if (contentPrunable != null) {
            sourceMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 0, ContentType.RAW, TEST_MESSAGE_CONTENT, null, false));
            destinationMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 1, ContentType.RAW, TEST_MESSAGE_CONTENT, null, false));
        }

        TestUtils.createTestMessagesFast(channelId, message, power);

        int testSize = (int) Math.pow(2, power);
        assertEquals(testSize, TestUtils.getNumMessages(channelId));

        if (contentPrunable != null) {
            assertEquals(testSize, TestUtils.getNumMessages(channelId, true));
        } else {
            assertEquals(0, TestUtils.getNumMessages(channelId, true));
        }

        logger.debug("Finished preparing test messages");
    }

    private static class TestArchiver implements MessageWriter {
        private List<Long> archivedMessageIds = new ArrayList<Long>();

        public List<Long> getArchivedMessageIds() {
            return archivedMessageIds;
        }

        @Override
        public boolean write(Message message) throws MessageWriterException {
            archivedMessageIds.add(message.getMessageId().longValue());
            return true;
        }

        @Override
        public void close() throws MessageWriterException {}
    }

    private static class FailingArchiver implements MessageWriter {
        private List<Long> archivedMessageIds = new ArrayList<Long>();
        private List<Long> failedMessageIds = new ArrayList<Long>();

        public List<Long> getFailedMessageIds() {
            return failedMessageIds;
        }

        public List<Long> getArchivedMessageIds() {
            return archivedMessageIds;
        }

        @Override
        public boolean write(Message message) throws MessageWriterException {
            if ((message.getMessageId() / 2) % 3 == 0) {
                failedMessageIds.add(message.getMessageId());
                return false;
            }

            archivedMessageIds.add(message.getMessageId().longValue());
            return true;
        }

        @Override
        public void close() throws MessageWriterException {}
    }
}
