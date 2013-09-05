/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.DonkeyConfiguration;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.plugins.datapruner.DataPruner.Strategy;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.tests.TestUtils;
import com.mirth.connect.util.messagewriter.MessageWriter;
import com.mirth.connect.util.messagewriter.MessageWriterException;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DataPrunerTest {
    private final static String TEMP_ARCHIVE_FOLDER = "/tmp/prunertest";
    private final static int TEST_POWER = 7;
    private final static int PERFORMANCE_TEST_POWER = 10;
    private final static int ARCHIVER_PAGE_SIZE = 1000;
    private final static String TEST_CHANNEL_ID = "prunerTestChannel";
    private final static String TEST_SERVER_ID = "testServerId";
    private final static String TEST_MESSAGE_CONTENT = TestUtils.TEST_HL7_MESSAGE;

    // @formatter:off
    private static Calendar messageDateThreshold;
    private static Calendar contentDateThreshold;
    private static int testSize;
    private static int[] blockSizes = new int[] { 0, 1000 };
    private static Logger logger = Logger.getLogger(DataPrunerTest.class);
    // @formatter:on

    @BeforeClass
    public final static void init() throws Exception {
        ConfigurationController configurationController = ConfigurationController.getInstance();
        configurationController.initializeSecuritySettings();
        configurationController.initializeDatabaseSettings();

        Donkey donkey = Donkey.getInstance();
        donkey.startEngine(new DonkeyConfiguration(configurationController.getApplicationDataDir(), configurationController.getDatabaseSettings().getProperties(), null, new EventDispatcher() {
            @Override
            public void dispatchEvent(Event event) {}
        }, "testserverid"));

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
        prepareTestMessages(TEST_CHANNEL_ID, messagesPrunable, contentPrunable, true, Status.SENT, TEST_POWER);
        new DataPruner().pruneChannel(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold, null);
        assertEquals(messagesPrunable ? 0 : testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(contentPrunable ? 0 : testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
        
        for (Strategy strategy : Strategy.values()) {
            for (int blockSize : blockSizes) {
                try {
                    MessageWriterOptions writerOptions = new MessageWriterOptions();
                    writerOptions.setRootFolder(TEMP_ARCHIVE_FOLDER);
                    writerOptions.setFilePattern("message${message.messageId}.xml");
                    
                    DataPruner pruner = new DataPruner();
                    pruner.setArchiveEnabled(true);
                    pruner.setArchiverOptions(writerOptions);
                    pruner.setPageSize(ARCHIVER_PAGE_SIZE);
                    pruner.setStrategy(strategy);
                    pruner.setRetryCount(0);
                    pruner.setBlockSize(blockSize);
    
                    logger.info("Running pruner w/ archiver test, strategy: " + strategy + ", block size: " + blockSize + ", prune messages: " + messagesPrunable + ", prune content: " + contentPrunable);
                    
                    prepareTestMessages(TEST_CHANNEL_ID, messagesPrunable, contentPrunable, true, Status.SENT, TEST_POWER);
                    pruner.pruneChannel(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold, pruner.getArchiverOptions().getRootFolder());
                    assertEquals(messagesPrunable ? 0 : testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
                    assertEquals(contentPrunable ? 0 : testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
                } catch (AssertionError e) {
                    logger.error(e);
                    throw e;
                } finally {
                    FileUtils.deleteQuietly(new File(TEMP_ARCHIVE_FOLDER));
                }
            }
        }
    }

    @Test
    public void testPruneNone() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, false, false, true, Status.SENT, TEST_POWER);
        DataPruner pruner = new DataPruner();
        pruner.pruneChannel(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold, null);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public final void testPruneSkipIncomplete() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, false, Status.SENT, TEST_POWER);
        DataPruner pruner = new DataPruner();
        pruner.pruneChannel(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold, null);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public final void testPruneIncomplete() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, false, Status.SENT, TEST_POWER);
        DataPruner pruner = new DataPruner();
        pruner.setSkipIncomplete(false);
        pruner.pruneChannel(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold, null);
        assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public final void testPruneQueued() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, true, Status.QUEUED, TEST_POWER);
        DataPruner pruner = new DataPruner();
        pruner.pruneChannel(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold, null);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    public final void testPruneError() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, true, Status.ERROR, TEST_POWER);
        DataPruner pruner = new DataPruner();
        pruner.pruneChannel(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold, null);
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(testSize, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }

    @Test
    @Ignore
    public final void testPerformance() throws Exception {
        prepareTestMessages(TEST_CHANNEL_ID, true, true, true, Status.SENT, PERFORMANCE_TEST_POWER);
        DataPruner pruner = new DataPruner();

        long startTime = System.currentTimeMillis();
        pruner.pruneChannel(TEST_CHANNEL_ID, messageDateThreshold, contentDateThreshold, null);
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("Archiver/Pruner executed in " + duration + "ms");
        System.out.println(TestUtils.getPerSecondRate((long) Math.pow(2, PERFORMANCE_TEST_POWER), duration, 2) + " msg/sec");
        
        assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID));
        assertEquals(0, TestUtils.getNumMessages(TEST_CHANNEL_ID, true));
    }
    
    @Test
    @Ignore
    public final void testConcurrency() throws Exception {
        /*
         * To run this concurrency test, you must setup a "reader" channel through the
         * administrator, that routes messages to other channels that will be pruned. Then specify
         * the ids of those channels below.
         */
        final String readerChannelId = "f7158274-8692-4e53-9d17-db732c3346b8";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        TestUtils.startMirthServer(15000);

        DataPruner pruner = new DataPruner();
        pruner.setBlockSize(1);
        pruner.setStrategy(Strategy.INCLUDE_LIST);
        pruner.setRetryCount(0);
        
        TestUtils.deleteAllMessages(readerChannelId);
        TestUtils.deleteAllMessages("0831345e-bbe0-4d62-8f2d-c65280bd479b");
        TestUtils.deleteAllMessages("b2e28f1b-d867-435a-a5f6-3b33d5261e66");

        // send messages into the test channel on a separate thread
        Future<Void> future = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                EngineController engineController = ControllerFactory.getFactory().createEngineController();
                logger.info("Sending messages");

                for (int i = 0; i < 100000; i++) {
                    logger.info("sending message #" + i);
                    engineController.dispatchRawMessage(readerChannelId, new RawMessage(TestUtils.TEST_HL7_MESSAGE), false);
                }

                logger.info("Finished sending messages");
                return null;
            }
        });
        
        logger.info("Executing pruner");
        
        // run the pruner while messages are processing
        while (!future.isDone()) {
            pruner.run();
            Thread.sleep(2000);
        }
        
        logger.info("Test completed");
    }

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

        Message message = new Message();
        message.setMessageId(1L);
        message.setChannelId(channelId);
        message.setServerId(TEST_SERVER_ID);
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
    
    private static class PassthruArchiver implements MessageWriter {
        @Override
        public boolean write(Message message) throws MessageWriterException {
            return true;
        }

        @Override
        public void close() throws MessageWriterException {}
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
}
