package com.mirth.connect.connectors.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.connectors.file.filesystems.FileSystemConnectionFactory;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.datatypes.hl7v2.ER7BatchAdaptorFactory;
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2BatchProperties;
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DeserializationProperties;
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;

public class FileReceiverTest {

    private static final String POLL_ID = "pollId";
    private static final String POLL_SEQUENCE_ID = "pollSequenceId";
    private static final String POLL_COMPLETE = "pollComplete";
    private static final String ORIGINAL_FILENAME = "originalFilename";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ControllerFactory controllerFactory = mock(ControllerFactory.class);

        EventController eventController = mock(EventController.class);
        when(controllerFactory.createEventController()).thenReturn(eventController);

        ConfigurationController configurationController = mock(ConfigurationController.class);
        when(controllerFactory.createConfigurationController()).thenReturn(configurationController);

        ExtensionController extensionController = mock(ExtensionController.class);
        when(controllerFactory.createExtensionController()).thenReturn(extensionController);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        injector.getInstance(ControllerFactory.class);

        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogSystem");
    }

    /*
     * No batch messages, no directory recursion
     */
    @Test
    public void testPoll1() throws Exception {
        final String fileDirectory = "tests/filereader/nonbatch";
        final int expectedMessageCount = 5;

        TestFileReceiver receiver = createReceiver(fileDirectory, false, false, FileReceiverProperties.SORT_BY_NAME, null);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount);

        String pollId1 = null;
        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId1 == null) {
                pollId1 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId1, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(i + 1, sourceMap.get(POLL_SEQUENCE_ID));

            if (i == receiver.rawMessages.size() - 1) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount * 2);

        String pollId2 = null;
        for (int i = expectedMessageCount; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId2 == null) {
                pollId2 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId2, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(i - expectedMessageCount + 1, sourceMap.get(POLL_SEQUENCE_ID));

            if (i == receiver.rawMessages.size() - 1) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

        assertNotSame(pollId1, pollId2);
    }

    /*
     * No batch messages, uses directory recursion
     */
    @Test
    public void testPoll2() throws Exception {
        final String fileDirectory = "tests/filereader/nonbatch";
        final int expectedMessageCount = 8;

        TestFileReceiver receiver = createReceiver(fileDirectory, true, false, FileReceiverProperties.SORT_BY_NAME, null);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount);

        String pollId1 = null;
        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId1 == null) {
                pollId1 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId1, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(i + 1, sourceMap.get(POLL_SEQUENCE_ID));

            if (i == receiver.rawMessages.size() - 1) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount * 2);

        String pollId2 = null;
        for (int i = expectedMessageCount; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId2 == null) {
                pollId2 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId2, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(i - expectedMessageCount + 1, sourceMap.get(POLL_SEQUENCE_ID));

            if (i == receiver.rawMessages.size() - 1) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

        assertNotSame(pollId1, pollId2);
    }

    /*
     * Batch messages, no directory recursion
     */
    @Test
    public void testPoll3() throws Exception {
        final String fileDirectory = "tests/filereader/batch";

        final int messagesFile1 = 3;
        final int messagesFile2 = 1;
        final int messagesFile3 = 2;
        final int expectedMessageCount = messagesFile1 + messagesFile2 + messagesFile3;

        TestFileReceiver receiver = createReceiver(fileDirectory, false, true, FileReceiverProperties.SORT_BY_NAME, null);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount);

        String pollId1 = null;
        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId1 == null) {
                pollId1 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId1, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));

            int expectedPollSequenceID = 0;
            if (i < messagesFile1) {
                expectedPollSequenceID = 1;
            } else if (i < messagesFile1 + messagesFile2) {
                expectedPollSequenceID = 2;
            } else {
                expectedPollSequenceID = 3;
            }

            assertEquals(expectedPollSequenceID, sourceMap.get(POLL_SEQUENCE_ID));

            if (i >= messagesFile1 + messagesFile2) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount * 2);

        String pollId2 = null;
        for (int i = expectedMessageCount; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId2 == null) {
                pollId2 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId2, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));

            int expectedPollSequenceID = 0;
            if (i < messagesFile1 + expectedMessageCount) {
                expectedPollSequenceID = 1;
            } else if (i < messagesFile1 + messagesFile2 + expectedMessageCount) {
                expectedPollSequenceID = 2;
            } else {
                expectedPollSequenceID = 3;
            }

            assertEquals(expectedPollSequenceID, sourceMap.get(POLL_SEQUENCE_ID));

            if (i >= messagesFile1 + messagesFile2 + expectedMessageCount) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

    }

    /*
     * Batch messages, uses directory recursion
     */
    @Test
    public void testPoll4() throws Exception {
        final String fileDirectory = "tests/filereader/batch";

        final int messagesFile1 = 3;
        final int messagesFile2 = 1;
        final int messagesFile3 = 2;
        final int messagesFile4 = 3;
        final int expectedMessageCount = messagesFile1 + messagesFile2 + messagesFile3 + messagesFile4;

        TestFileReceiver receiver = createReceiver(fileDirectory, true, true, FileReceiverProperties.SORT_BY_NAME, null);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount);

        String pollId1 = null;
        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId1 == null) {
                pollId1 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId1, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));

            int expectedPollSequenceID = 0;
            if (i < messagesFile1) {
                expectedPollSequenceID = 1;
            } else if (i < messagesFile1 + messagesFile2) {
                expectedPollSequenceID = 2;
            } else if (i < messagesFile1 + messagesFile2 + messagesFile3) {
                expectedPollSequenceID = 3;
            } else {
                expectedPollSequenceID = 4;
            }

            assertEquals(expectedPollSequenceID, sourceMap.get(POLL_SEQUENCE_ID));

            if (i >= messagesFile1 + messagesFile2 + messagesFile3) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount * 2);

        String pollId2 = null;
        for (int i = expectedMessageCount; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId2 == null) {
                pollId2 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId2, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));

            int expectedPollSequenceID = 0;
            if (i < messagesFile1 + expectedMessageCount) {
                expectedPollSequenceID = 1;
            } else if (i < messagesFile1 + messagesFile2 + expectedMessageCount) {
                expectedPollSequenceID = 2;
            } else if (i < messagesFile1 + messagesFile2 + messagesFile3 + expectedMessageCount) {
                expectedPollSequenceID = 3;
            } else {
                expectedPollSequenceID = 4;
            }

            assertEquals(expectedPollSequenceID, sourceMap.get(POLL_SEQUENCE_ID));

            if (i >= messagesFile1 + messagesFile2 + messagesFile3 + expectedMessageCount) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

    }

    /*
     * Empty directory
     */
    @Test
    public void testPoll5() throws Exception {
        final String fileDirectory = "tests/filereader/containsempty/empty";

        TestFileReceiver receiver = createReceiver(fileDirectory, true, true, FileReceiverProperties.SORT_BY_NAME, null);
        receiver.poll();
        assertTrue(receiver.rawMessages.size() == 0);

        receiver = createReceiver(fileDirectory, false, true, FileReceiverProperties.SORT_BY_NAME, null);
        receiver.poll();
        assertTrue(receiver.rawMessages.size() == 0);

        receiver = createReceiver(fileDirectory, true, false, FileReceiverProperties.SORT_BY_NAME, null);
        receiver.poll();
        assertTrue(receiver.rawMessages.size() == 0);

        receiver = createReceiver(fileDirectory, false, false, FileReceiverProperties.SORT_BY_NAME, null);
        receiver.poll();
        assertTrue(receiver.rawMessages.size() == 0);
    }

    /*
     * Tests batch processing while sorting files by size
     */
    @Test
    public void testPoll6() throws Exception {
        final String fileDirectory = "tests/filereader/batch";

        final int messagesFile1 = 1;
        final int messagesFile2 = 2;
        final int messagesFile3 = 3;
        final int expectedMessageCount = messagesFile1 + messagesFile2 + messagesFile3;

        TestFileReceiver receiver = createReceiver(fileDirectory, false, true, FileReceiverProperties.SORT_BY_SIZE, null);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount);

        String pollId1 = null;
        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId1 == null) {
                pollId1 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId1, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));

            int expectedPollSequenceID = 0;
            String expectedFilename;
            if (i < messagesFile1) {
                expectedPollSequenceID = 1;
                expectedFilename = "batch02.hl7";
            } else if (i < messagesFile1 + messagesFile2) {
                expectedPollSequenceID = 2;
                expectedFilename = "batch03.hl7";
            } else {
                expectedPollSequenceID = 3;
                expectedFilename = "batch01.hl7";
            }

            assertEquals(expectedPollSequenceID, sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(expectedFilename, sourceMap.get(ORIGINAL_FILENAME));

            if (i >= messagesFile1 + messagesFile2) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount * 2);

        String pollId2 = null;
        for (int i = expectedMessageCount; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId2 == null) {
                pollId2 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId2, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));

            int expectedPollSequenceID = 0;
            String expectedFilename;
            if (i < messagesFile1 + expectedMessageCount) {
                expectedPollSequenceID = 1;
                expectedFilename = "batch02.hl7";
            } else if (i < messagesFile1 + messagesFile2 + expectedMessageCount) {
                expectedPollSequenceID = 2;
                expectedFilename = "batch03.hl7";
            } else {
                expectedPollSequenceID = 3;
                expectedFilename = "batch01.hl7";
            }

            assertEquals(expectedPollSequenceID, sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(expectedFilename, sourceMap.get(ORIGINAL_FILENAME));

            if (i >= messagesFile1 + messagesFile2 + expectedMessageCount) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

    }

    /*
     * Tests where not all files will be processed due to size
     */
    @Test
    public void testPoll7() throws Exception {
        final String fileDirectory = "tests/filereader/batch";
        final int messagesFile2 = 1;
        final int expectedMessageCount = messagesFile2;

        TestFileReceiver receiver = createReceiver(fileDirectory, false, true, FileReceiverProperties.SORT_BY_NAME, "1000");
        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount);

        String pollId1 = null;
        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId1 == null) {
                pollId1 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId1, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));

            int expectedPollSequenceID = 0;
            String expectedFilename;
            expectedPollSequenceID = 1;
            expectedFilename = "batch02.hl7";

            assertEquals(expectedPollSequenceID, sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(expectedFilename, sourceMap.get(ORIGINAL_FILENAME));

            assertNotNull(sourceMap.get(POLL_COMPLETE));
            assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
        }
    }

    /*
     * Test when connector is stopped/terminated before executing poll
     */
    @Test
    public void testPoll8() throws Exception {
        final String fileDirectory = "tests/filereader/batch";
        final int expectedMessageCount = 0;

        TestFileReceiver receiver = createReceiver(fileDirectory, false, true, FileReceiverProperties.SORT_BY_NAME, null);
        receiver.stop();
        receiver.poll();

        assertTrue(receiver.rawMessages.size() == expectedMessageCount);
    }

    /*
     * Test when connector polls a directory with an empty file
     */
    @Test
    public void testPoll9() throws Exception {
        final String fileDirectory = "tests/filereader/emptyfile";
        final int expectedMessageCount = 0;

        TestFileReceiver receiver = createReceiver(fileDirectory, false, true, FileReceiverProperties.SORT_BY_NAME, null);
        receiver.poll();

        verify(receiver.logger, times(1)).warn(any());
        assertTrue(receiver.rawMessages.size() == expectedMessageCount);
    }

    /*
     * Test when polling with directory recursion and the last directory is empty
     */
    @Test
    public void testPoll10() throws Exception {
        final String fileDirectory = "tests/filereader/containsempty";
        final int expectedMessageCount = 1;

        TestFileReceiver receiver = createReceiver(fileDirectory, true, false, FileReceiverProperties.SORT_BY_NAME, null);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == expectedMessageCount);

        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(i + 1, sourceMap.get(POLL_SEQUENCE_ID));
            assertNotNull(sourceMap.get(POLL_COMPLETE));
            assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
        }
    }

    /*
     * Batch messages, no directory recursion, File Type Binary
     */
    @Test
    public void testPoll11() throws Exception {
        final String fileDirectory = "tests/filereader/batch";

        boolean exceptionThrown = false;
        try {
            createReceiver(fileDirectory, false, true, FileReceiverProperties.SORT_BY_NAME, null, true);
        } catch (ConnectorTaskException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    private TestFileReceiver createReceiver(String directory, boolean directoryRecursion, boolean batchProcess, String sortBy, String fileSizeMaximum) throws Exception {
        return createReceiver(directory, directoryRecursion, batchProcess, sortBy, fileSizeMaximum, false);
    }

    private TestFileReceiver createReceiver(String directory, boolean directoryRecursion, boolean batchProcess, String sortBy, String fileSizeMaximum, boolean fileTypeBinary) throws Exception {
        TestFileReceiver receiver = spy(new TestFileReceiver() {
            @Override
            protected String getConfigurationClass() {
                return null;
            }
        });
        String channelId = UUID.randomUUID().toString();
        String channelName = "File Receiver";
        TestChannel channel = new TestChannel();
        channel.setChannelId(channelId);
        channel.setName(channelName);
        channel.setSourceConnector(receiver);

        receiver.setChannel(channel);
        receiver.setChannelId(channelId);

        FileReceiverProperties connectorProperties = new FileReceiverProperties();
        connectorProperties.setDirectoryRecursion(directoryRecursion);
        connectorProperties.setSortBy(sortBy);
        if (fileSizeMaximum != null) {
            connectorProperties.setIgnoreFileSizeMaximum(false);
            connectorProperties.setFileSizeMinimum("0");
            connectorProperties.setFileSizeMaximum(fileSizeMaximum);
        }
        connectorProperties.setBinary(fileTypeBinary);
        receiver.setConnectorProperties(connectorProperties);

        FileConnector fileConnector = mock(FileConnector.class);
        URI uri = new File(directory).toURI();
        when(fileConnector.getEndpointURI(any(), any(), any(), anyBoolean())).thenReturn(uri);
        when(fileConnector.getPathPart(any())).thenReturn(uri.getPath());

        FileSystemConnectionFactory factory = new FileSystemConnectionFactory(FileScheme.FILE, null, null, 0, true, false, 30000);
        when(fileConnector.getConnection(any())).thenReturn(factory.makeObject().getObject());

        if (batchProcess) {
            HL7v2SerializationProperties hl7SerializationProperties = new HL7v2SerializationProperties();
            HL7v2DeserializationProperties hl7DeserializationProperties = new HL7v2DeserializationProperties();
            HL7v2BatchProperties hl7BatchProperties = new HL7v2BatchProperties();
            SerializerProperties serializerProperties = new SerializerProperties(hl7SerializationProperties, hl7DeserializationProperties, hl7BatchProperties);
            BatchAdaptorFactory batchFactory = new ER7BatchAdaptorFactory(receiver, serializerProperties);
            receiver.setBatchAdaptorFactory(batchFactory);
        }

        receiver.onDeploy();
        receiver.setFileConnector(fileConnector);
        receiver.start();

        return receiver;
    }

    class TestFileReceiver extends FileReceiver {
        List<RawMessage> rawMessages = new ArrayList<>();

        public TestFileReceiver() {
            logger = spy(LogFactory.getLog(getClass()));
        }

        @Override
        public DispatchResult dispatchRawMessage(RawMessage rawMessage) throws ChannelException {
            rawMessages.add(rawMessage);
            return new TestDispatchResult(0, null, new Response(), true, true);
        }

        @Override
        public void finishDispatch(DispatchResult dispatchResult) {}
    }

    /*
     * Subclass to alter methods for testing purposes
     */
    class TestChannel extends Channel {

        @Override
        protected EventDispatcher getEventDispatcher() {
            EventDispatcher eventDispatcher = mock(EventDispatcher.class);
            doNothing().when(eventDispatcher).dispatchEvent(any());
            return eventDispatcher;
        }

        @Override
        protected DispatchResult dispatchRawMessage(RawMessage rawMessage, boolean batch) throws ChannelException {
            ((TestFileReceiver) getSourceConnector()).rawMessages.add(rawMessage);
            return new TestDispatchResult(0, null, new Response(), true, true);
        }
    }

    class TestDispatchResult extends DispatchResult {
        protected TestDispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean lockAcquired) {
            super(messageId, processedMessage, selectedResponse, markAsProcessed, lockAcquired);
        }
    }
}
