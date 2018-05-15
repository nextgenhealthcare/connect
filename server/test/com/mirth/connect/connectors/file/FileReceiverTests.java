package com.mirth.connect.connectors.file;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

public class FileReceiverTests {

    private static final String POLL_ID = "pollId";
    private static final String POLL_SEQUENCE_ID = "pollSequenceId";
    private static final String POLL_COMPLETE = "pollComplete";

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
        final String fileDirectory = "tests/filereader";
        final int messageCount = 5;

        TestFileReceiver receiver = createReceiver(fileDirectory, false, false);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == messageCount);

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
        assertTrue(receiver.rawMessages.size() == messageCount * 2);

        String pollId2 = null;
        for (int i = messageCount; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId2 == null) {
                pollId2 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId2, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(i - messageCount + 1, sourceMap.get(POLL_SEQUENCE_ID));

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
        final String fileDirectory = "tests/filereader";
        final int messageCount = 8;

        TestFileReceiver receiver = createReceiver(fileDirectory, true, false);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == messageCount);

        String pollId1 = null;
        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            System.out.println(sourceMap);
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
        assertTrue(receiver.rawMessages.size() == messageCount * 2);

        String pollId2 = null;
        for (int i = messageCount; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId2 == null) {
                pollId2 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId2, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(i - messageCount + 1, sourceMap.get(POLL_SEQUENCE_ID));

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
        final int messageCount = 6;

        TestFileReceiver receiver = createReceiver(fileDirectory, false, true);

        receiver.poll();
        assertTrue(receiver.rawMessages.size() == messageCount);

        String pollId1 = null;
        for (int i = 0; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            System.out.println(sourceMap);
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
        assertTrue(receiver.rawMessages.size() == messageCount * 2);

        String pollId2 = null;
        for (int i = messageCount; i < receiver.rawMessages.size(); i++) {
            RawMessage message = receiver.rawMessages.get(i);
            Map<String, Object> sourceMap = message.getSourceMap();
            if (pollId2 == null) {
                pollId2 = (String) sourceMap.get(POLL_ID);
            }

            assertNotNull(sourceMap.get(POLL_ID));
            assertEquals(pollId2, sourceMap.get(POLL_ID));
            assertNotNull(sourceMap.get(POLL_SEQUENCE_ID));
            assertEquals(i - messageCount + 1, sourceMap.get(POLL_SEQUENCE_ID));

            if (i == receiver.rawMessages.size() - 1) {
                assertNotNull(sourceMap.get(POLL_COMPLETE));
                assertTrue(((Boolean) sourceMap.get(POLL_COMPLETE)).booleanValue());
            } else {
                assertNull(sourceMap.get(POLL_COMPLETE));
            }
        }

    }

    private TestFileReceiver createReceiver(String directory, boolean directoryRecursion, boolean batchProcess) throws Exception {
        TestFileReceiver receiver = spy(new TestFileReceiver() {
            @Override
            protected String getConfigurationClass() {
                return null;
            }
        });
        String channelId = UUID.randomUUID().toString();
        String channelName = "File Receiver";
        TestChannel channel = mock(TestChannel.class);
        when(channel.getChannelId()).thenReturn(channelId);
        when(channel.getName()).thenReturn(channelName);
        when(channel.dispatchRawMessage(any(), anyBoolean())).thenReturn(new TestDispatchResult(0, null, new Response(), true, true));
        receiver.setChannel(channel);
        receiver.setChannelId(channelId);

        EventDispatcher eventDispatcher = mock(EventDispatcher.class);
        doNothing().when(eventDispatcher).dispatchEvent(any());
        when(channel.getEventDispatcher()).thenReturn(eventDispatcher);

        FileReceiverProperties connectorProperties = new FileReceiverProperties();
        connectorProperties.setDirectoryRecursion(directoryRecursion);
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

        @Override
        public DispatchResult dispatchRawMessage(RawMessage rawMessage) throws ChannelException {
            rawMessages.add(rawMessage);
            return super.dispatchRawMessage(rawMessage);
        }
    }

    /*
     * Mock this class to access the protected methods of Channel
     */
    class TestChannel extends Channel {

        @Override
        protected EventDispatcher getEventDispatcher() {
            return super.getEventDispatcher();
        }
        
        @Override
        protected DispatchResult dispatchRawMessage(RawMessage rawMessage, boolean batch) throws ChannelException {
            ((TestFileReceiver) getSourceConnector()).rawMessages.add(rawMessage);
//            return new TestDispatchResult(0, null, new Response(), true, true);
            return super.dispatchRawMessage(rawMessage, batch);
        }
    }

    class TestDispatchResult extends DispatchResult {
        protected TestDispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean lockAcquired) {
            super(messageId, processedMessage, selectedResponse, markAsProcessed, lockAcquired);
        }
    }
}
