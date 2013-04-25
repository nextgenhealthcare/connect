package com.mirth.connect.server.controllers.tests;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.server.Mirth;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.MessageController;

public class MessageControllerTest {
    private static Mirth server = new Mirth();
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    private Logger logger = Logger.getLogger(getClass());

    @BeforeClass
    public final static void init() throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                server.run();
            }
        });

        while (ConfigurationController.getInstance().isEngineStarting()) {
            Thread.sleep(1000);
        }
    }

    /**
     * Simulates messages being reprocessed while simultaneously running the message browser search,
     * in order to see if a database deadlock occurs.
     * 
     * @throws Exception
     */
    @Test
    public final void testConcurrentReprocessAndSearch() throws Exception {
        final int testSize = 1000;
        final int pageSize = 20;
        final int searchCount = 1000;
        final MessageController messageController = MessageController.getInstance();
        final MessageFilter filter = new MessageFilter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        EngineController engineController = ControllerFactory.getFactory().createEngineController();

        // create test channel
        final Channel channel = TestUtils.createTestChannelModel("sqlserverdeadlocktest", "test channel");
        ChannelController.getInstance().updateChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
        engineController.deployChannel(channel.getId(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);

        // fill the channel with test messages
        logger.info("Dispatching " + testSize + " messages into the test channel");

        for (int i = 0; i < testSize; i++) {
            engineController.dispatchRawMessage(channel.getId(), new RawMessage(TestUtils.TEST_HL7_MESSAGE));
        }

        // start re-processing messages
        Future<Void> future = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                logger.info("Reprocessing messages");

                try {
                    messageController.reprocessMessages(channel.getId(), filter, false, null);
                } catch (Exception e) {
                    logger.error(e);
                }

                logger.info("Reprocessing completed");
                return null;
            }
        });

        // repeatedly run the message browser search
        com.mirth.connect.donkey.server.channel.Channel donkeyChannel = new com.mirth.connect.donkey.server.channel.Channel();
        donkeyChannel.setChannelId(channel.getId());

        logger.info("Running select queries");

        for (int i = 0; i < searchCount; i++) {
            messageController.getMessages(filter, donkeyChannel, false, 0, pageSize);
        }

        logger.info("Select queries completed");

        future.get();
        executor.shutdown();
    }
}
