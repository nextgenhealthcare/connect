package com.mirth.connect.connectors.tcp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.data.passthru.PassthruDaoFactory;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.model.transmission.batch.DefaultBatchStreamReader;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameStreamHandler;
import com.mirth.connect.plugins.TransmissionModeProvider;
import com.mirth.connect.plugins.mllpmode.MLLPModeProvider;
import com.mirth.connect.server.attachments.passthru.PassthruAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;

public class TcpDispatcherTest {
	private static final String TEST_CHANNEL_ID = UUID.randomUUID().toString();
	private static final String TEST_SERVER_ID = UUID.randomUUID().toString();
	private static final String TEST_CHANNEL_NAME = "Test TCP Sender Channel";
	private static final String TEST_MESSAGE_TEMPLATE = "test";
	private static final String CONNECTOR_MAP_NO_OF_CLIENTS_KEY = "numberOfClients";
	private static final String CONNECTOR_MAP_SUCCESSFUL_SENDS_KEY = "successfulSends";
	private static final String CONNECTOR_MAP_ALL_RESPONSES_KEY = "allResponses";
	private static final boolean PRINT_DEBUG_MESSAGES = false;
	
	private static AtomicInteger incrementingPort = new AtomicInteger(9000);
	private static AtomicInteger incrementingSocketListenerId = new AtomicInteger(0);
	private TcpDispatcherProperties dispatcherProps;
	private TcpDispatcher dispatcher;
	
	private static int getNextPort() {
        return incrementingPort.getAndIncrement();
    }
	
	private static int getNextSocketListenerId() {
		return incrementingSocketListenerId.getAndIncrement();
	}
	
	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		ControllerFactory controllerFactory = mock(ControllerFactory.class);
		
		EventController eventController = mock(EventController.class);
        when(controllerFactory.createEventController()).thenReturn(eventController);

        ExtensionController extensionController = mock(ExtensionController.class);
        Map<String, TransmissionModeProvider> transmissionModeProviders = new HashMap<>();
        transmissionModeProviders.put("MLLP", new MLLPModeProvider());
        when(extensionController.getTransmissionModeProviders()).thenReturn(transmissionModeProviders);
        when(controllerFactory.createExtensionController()).thenReturn(extensionController);
        
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        injector.getInstance(ControllerFactory.class);
	}

	@After
	public void tearDown() throws Exception {
		if (dispatcher != null) {
			log("Stopping TCP Dispatcher...");
			dispatcher.stop();
			log("Undeploying TCP Dispatcher...");
			dispatcher.onUndeploy();
			Thread.sleep(1000);
		}
	}
	
	private void setupBasicDispatcher() throws Exception {
		setupDispatcher(createTcpDispatcherProperties());
	}
	
	private void setupMLLPDispatcher() throws Exception {
		TcpDispatcherProperties props = createTcpDispatcherProperties();
		props.getTransmissionModeProperties().setPluginPointName("MLLP");
		setupDispatcher(props);
	}
	
	private void setupDispatcher(TcpDispatcherProperties props) throws Exception {
		// Setup member variables
		dispatcherProps = props;
		dispatcher = createTcpDispatcher(dispatcherProps);
		
		// Deploy and start TcpDispatcher
		log("Deploying TCP Dispatcher...");
		dispatcher.onDeploy();
		log("Starting TCP Dispatcher...");
		dispatcher.start();
		Thread.sleep(1000);
	}
	
	private TcpDispatcherProperties createTcpDispatcherProperties() {
		TcpDispatcherProperties dispatcherProps = new TcpDispatcherProperties();
		dispatcherProps.getTransmissionModeProperties().setPluginPointName("Basic");
		FrameModeProperties frameProps = new FrameModeProperties();
		frameProps.setStartOfMessageBytes("02");
		frameProps.setEndOfMessageBytes("01");
		dispatcherProps.setTransmissionModeProperties(frameProps);
		dispatcherProps.setServerMode(true);
		dispatcherProps.setLocalPort(getNextPort() + "");
		dispatcherProps.setTemplate(TEST_MESSAGE_TEMPLATE);
		dispatcherProps.setKeepConnectionOpen(true);
		dispatcherProps.setSendTimeout("0");
		return dispatcherProps;
	}
	
	private TcpDispatcher createTcpDispatcher(TcpDispatcherProperties dispatcherProps) {
		TcpDispatcher dispatcher = new TestTcpDispatcher(TEST_CHANNEL_ID, TEST_SERVER_ID, 1, dispatcherProps);
		dispatcher.setChannelId(TEST_CHANNEL_ID);
		
		Channel channel = new TestChannel();
		channel.setChannelId(TEST_CHANNEL_ID);
		channel.setName(TEST_CHANNEL_NAME);
		
		PassthruAttachmentHandlerProvider attachmentHandlerProvider = new PassthruAttachmentHandlerProvider(null);
		channel.setAttachmentHandlerProvider(attachmentHandlerProvider);
		
		dispatcher.setChannel(channel);
		return dispatcher;
	}

	/*
	 * testMessageSent()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 * -create listener socket that connects to the TCP Sender socket
	 * -send message through TCP Sender
	 * -verify that listener socket received the message
	 */
	@Test
	public void testMessageSent() throws Exception {
		setupBasicDispatcher();
		Map<String, String> socketResult = new ConcurrentHashMap<>();

		int socketListenerId = getNextSocketListenerId();
		createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0).start();
		
		Thread.sleep(1000);
		log("Sending message...");
        dispatcher.send(dispatcherProps, new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING));
        
        assertEquals(TEST_MESSAGE_TEMPLATE, socketResult.get("result" + socketListenerId));
	}
	
	/*
	 * testMLLPMessageSent()
	 * -create TCP Sender
	 *   -Server mode
	 *   -MLLP mode
	 * -create listener socket that connects to the TCP Sender socket
	 * -send message through TCP Sender
	 * -verify that listener socket received the message
	 */
	@Test
	public void testMLLPMessageSent() throws Exception {
		setupMLLPDispatcher();
		Map<String, String> socketResult = new ConcurrentHashMap<>();

		int socketListenerId = getNextSocketListenerId();
		createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0).start();
		
		Thread.sleep(1000);
		log("Sending message...");
        dispatcher.send(dispatcherProps, new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING));
        
        assertEquals(TEST_MESSAGE_TEMPLATE, socketResult.get("result" + socketListenerId));
	}
	
	/*
	 * testMessageSentToMultipleClients()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 * -create 3x listener sockets that connect to the TCP Sender socket
	 * -send message through TCP Sender
	 * -verify that all 3 listener sockets received the message
	 */
	@Test
	public void testMessageSentToMultipleClients() throws Exception {
		setupBasicDispatcher();
		Map<String, String> socketResult = new ConcurrentHashMap<>();
		
		List<Integer> socketListenerIds = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			int socketListenerId = getNextSocketListenerId();
			socketListenerIds.add(socketListenerId);
			createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0).start();
		}
		Thread.sleep(1000);
		
		log("Sending message...");
        dispatcher.send(dispatcherProps, new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING));
        
        for (Integer socketListenerId : socketListenerIds) {
        	assertEquals(TEST_MESSAGE_TEMPLATE, socketResult.get("result" + socketListenerId));
        }
	}
	
	/*
	 * testMLLPMessageSentToMultipleClients()
	 * -create TCP Sender
	 *   -Server mode
	 *   -MLLP mode
	 * -create 3x listener sockets that connect to the TCP Sender socket
	 * -send message through TCP Sender
	 * -verify that all 3 listener sockets received the message
	 */
	@Test
	public void testMLLPMessageSentToMultipleClients() throws Exception {
		setupMLLPDispatcher();
		Map<String, String> socketResult = new ConcurrentHashMap<>();
		
		List<Integer> socketListenerIds = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			int socketListenerId = getNextSocketListenerId();
			socketListenerIds.add(socketListenerId);
			createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0).start();
		}
		Thread.sleep(1000);
		
		log("Sending message...");
        dispatcher.send(dispatcherProps, new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING));
        
        for (Integer socketListenerId : socketListenerIds) {
        	assertEquals(TEST_MESSAGE_TEMPLATE, socketResult.get("result" + socketListenerId));
        }
	}
	
	/*
	 * testMessageSentWhenOneOfMultipleClientsClosed()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 * -create 3x listener sockets that connect to the TCP Sender socket
	 * -close one of the listener sockets
	 * -send message through TCP Sender
	 * -verify that the 2 open listener sockets received the message
	 * -verify connectorMap says numberOfClients==2
	 * -verify connectorMap says successfulSends==2
	 */
	@Test
	public void testMessageSentWhenOneOfMultipleClientsClosed() throws Exception {
		setupBasicDispatcher();
		Map<String, String> socketResult = new ConcurrentHashMap<>();
		
		Map<Integer, SocketThread> socketListeners = new HashMap<>();
		for (int i = 0; i < 3; i++) {
			int socketListenerId = getNextSocketListenerId();
			SocketThread socketListenerThread = createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0);
			socketListeners.put(socketListenerId, socketListenerThread);
			socketListenerThread.start();
		}
		Thread.sleep(1000);
		
		Iterator<Entry<Integer, SocketThread>> iter = socketListeners.entrySet().iterator();
		SocketThread socketListenerThread = iter.next().getValue();
		socketListenerThread.closeSocket();
		socketListenerThread.join();
		
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
		dispatcher.send(dispatcherProps, message);
        Integer numberOfClients = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_NO_OF_CLIENTS_KEY);
        Integer successfulSends = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_SUCCESSFUL_SENDS_KEY);
        
        for (Entry<Integer, SocketThread> entry : socketListeners.entrySet()) {
        	if (socketListenerThread != entry.getValue()) {
        		assertEquals(TEST_MESSAGE_TEMPLATE, socketResult.get("result" + entry.getKey()));
        	}
        }
        
        assertEquals(new Integer(2), successfulSends);
        assertEquals(new Integer(2), numberOfClients);
	}
	
	/*
	 * testMLLPMessageSentWhenOneOfMultipleClientsClosed()
	 * -create TCP Sender
	 *   -Server mode
	 *   -MLLP mode
	 * -create 3x listener sockets that connect to the TCP Sender socket
	 * -close one of the listener sockets
	 * -send message through TCP Sender
	 * -verify that the 2 open listener sockets received the message
	 * -verify connectorMap says numberOfClients==2
	 * -verify connectorMap says successfulSends==2
	 */
	@Test
	public void testMLLPMessageSentWhenOneOfMultipleClientsClosed() throws Exception {
		setupMLLPDispatcher();
		Map<String, String> socketResult = new ConcurrentHashMap<>();
		
		Map<Integer, SocketThread> socketListeners = new HashMap<>();
		for (int i = 0; i < 3; i++) {
			int socketListenerId = getNextSocketListenerId();
			SocketThread socketListenerThread = createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0);
			socketListeners.put(socketListenerId, socketListenerThread);
			socketListenerThread.start();
		}
		Thread.sleep(1000);
		
		Iterator<Entry<Integer, SocketThread>> iter = socketListeners.entrySet().iterator();
		SocketThread socketListenerThread = iter.next().getValue();
		socketListenerThread.closeSocket();
		socketListenerThread.join();
		
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
		dispatcher.send(dispatcherProps, message);
        Integer numberOfClients = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_NO_OF_CLIENTS_KEY);
        Integer successfulSends = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_SUCCESSFUL_SENDS_KEY);
        log(message.getConnectorMap().get(CONNECTOR_MAP_ALL_RESPONSES_KEY));
        
        for (Entry<Integer, SocketThread> entry : socketListeners.entrySet()) {
        	if (socketListenerThread != entry.getValue()) {
        		assertEquals(TEST_MESSAGE_TEMPLATE, socketResult.get("result" + entry.getKey()));
        	}
        }
        
        assertEquals(new Integer(2), successfulSends);
        assertEquals(new Integer(2), numberOfClients);
	}
	
	/*
	 * testMaxConnections()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 *   -Max connections: 1
	 * -create 3x listener sockets that connect to the TCP Sender socket
	 * -send message through TCP Sender
	 * -verify connectorMap says numberOfClients==1
	 * -verify connectorMap says successfulSends==1
	 */
	@Test
	public void testMaxConnections() throws Exception {
		TcpDispatcherProperties dispatcherProps = createTcpDispatcherProperties();
		dispatcherProps.setMaxConnections("1");
		setupDispatcher(dispatcherProps);
		
		Map<String, String> socketResult = new ConcurrentHashMap<>();
		
		Map<Integer, SocketThread> socketListeners = new HashMap<>();
		for (int i = 0; i < 3; i++) {
			int socketListenerId = getNextSocketListenerId();
			SocketThread socketListenerThread = createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0);
			socketListeners.put(socketListenerId, socketListenerThread);
			socketListenerThread.start();
		}
		Thread.sleep(1000);
		
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
		dispatcher.send(dispatcherProps, message);
        Integer numberOfClients = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_NO_OF_CLIENTS_KEY);
        Integer successfulSends = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_SUCCESSFUL_SENDS_KEY);

        assertEquals(new Integer(1), successfulSends);
        assertEquals(new Integer(1), numberOfClients);
	}
	
	/*
	 * testMessageResponseWithNoConnections()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 * -send message through TCP Sender
	 * -verify message status==QUEUED
	 * -verify connectorMap says numberOfClients==1
	 * -verify connectorMap says successfulSends==1
	 */
	@Test
	public void testMessageResponseWithNoConnections() throws Exception {
		setupBasicDispatcher();
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
        Response response = dispatcher.send(dispatcherProps, message);
        Integer numberOfClients = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_NO_OF_CLIENTS_KEY);
        Integer successfulSends = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_SUCCESSFUL_SENDS_KEY);
        
        assertEquals(Status.QUEUED, response.getStatus());
        assertEquals(new Integer(0), successfulSends);
        assertEquals(new Integer(0), numberOfClients);
	}
	
	/*
	 * testMessageResponseWhenAllConnectionsClosed()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 * -create 3x listener sockets that connect to the TCP Sender socket
	 * -close ALL of the listener sockets
	 * -send message through TCP Sender
	 * -verify message status==QUEUED
	 * -verify connectorMap says numberOfClients==0
	 * -verify connectorMap says successfulSends==0
	 */
	@Test
	public void testMessageResponseWhenAllConnectionsClosed() throws Exception {
		setupBasicDispatcher();
		Map<String, String> socketResult = new ConcurrentHashMap<>();
		
		Map<Integer, SocketThread> socketListeners = new HashMap<>();
		for (int i = 0; i < 3; i++) {
			int socketListenerId = getNextSocketListenerId();
			SocketThread socketListenerThread = createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0);
			socketListeners.put(socketListenerId, socketListenerThread);
			socketListenerThread.start();
		}
		Thread.sleep(1000);
		
		for (SocketThread thread : socketListeners.values()) {
			thread.closeSocket();
		}
		for (SocketThread thread : socketListeners.values()) {
			thread.join();
		}
		
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
        Response response = dispatcher.send(dispatcherProps, message);
        Integer numberOfClients = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_NO_OF_CLIENTS_KEY);
        Integer successfulSends = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_SUCCESSFUL_SENDS_KEY);
        
        assertEquals(Status.QUEUED, response.getStatus());
        assertEquals(new Integer(0), successfulSends);
        assertEquals(new Integer(0), numberOfClients);
	}
	
	/*
	 * testMLLPMessageResponseWhenAllConnectionsClosed()
	 * -create TCP Sender
	 *   -Server mode
	 *   -MLLP mode
	 * -create 3x listener sockets that connect to the TCP Sender socket
	 * -close ALL of the listener sockets
	 * -send message through TCP Sender
	 * -verify message status==QUEUED
	 * -verify connectorMap says numberOfClients==0
	 * -verify connectorMap says successfulSends==0
	 */
	@Test
	public void testMLLPMessageResponseWhenAllConnectionsClosed() throws Exception {
		setupMLLPDispatcher();
		Map<String, String> socketResult = new ConcurrentHashMap<>();
		
		Map<Integer, SocketThread> socketListeners = new HashMap<>();
		for (int i = 0; i < 3; i++) {
			int socketListenerId = getNextSocketListenerId();
			SocketThread socketListenerThread = createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0);
			socketListeners.put(socketListenerId, socketListenerThread);
			socketListenerThread.start();
		}
		Thread.sleep(1000);
		
		for (SocketThread thread : socketListeners.values()) {
			thread.closeSocket();
		}
		for (SocketThread thread : socketListeners.values()) {
			thread.join();
		}
		
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
        Response response = dispatcher.send(dispatcherProps, message);
        Integer numberOfClients = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_NO_OF_CLIENTS_KEY);
        Integer successfulSends = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_SUCCESSFUL_SENDS_KEY);
        
        assertEquals(Status.QUEUED, response.getStatus());
        assertEquals(new Integer(0), successfulSends);
        assertEquals(new Integer(0), numberOfClients);
	}
	
	/*
	 * testQueueOnResponseTimeout()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 *   -Queue on response timeout=true
	 *   -Response timeout=1000
	 * -create listener socket that connects to the TCP Sender socket
	 *   -this socket will do nothing after receiving a message
	 * -send message through TCP Sender
	 * -verify message status==QUEUED
	 */
	@Test
	public void testQueueOnResponseTimeout() throws Exception {
		TcpDispatcherProperties props = createTcpDispatcherProperties();
		props.setQueueOnResponseTimeout(true);
		props.setResponseTimeout("1000");
		setupDispatcher(props);
		Map<String, String> socketResult = new ConcurrentHashMap<>();

		int socketListenerId = getNextSocketListenerId();
		createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, false, 2000).start();
		
		Thread.sleep(1000);
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
        Response response = dispatcher.send(dispatcherProps, message);
        
        log(message.getConnectorMap().get(CONNECTOR_MAP_ALL_RESPONSES_KEY));
        assertEquals(Status.QUEUED, response.getStatus());
	}
	
	/*
	 * testQueueOnResponseTimeoutIsFalse()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 *   -Queue on response timeout=false
	 *   -Response timeout=1000
	 * -create listener socket that connects to the TCP Sender socket
	 *   -this socket will do nothing after receiving a message
	 * -send message through TCP Sender
	 * -verify message status==ERROR
	 */
	@Test
	public void testQueueOnResponseTimeoutIsFalse() throws Exception {
		TcpDispatcherProperties props = createTcpDispatcherProperties();
		props.setQueueOnResponseTimeout(false);
		props.setResponseTimeout("1000");
		setupDispatcher(props);
		Map<String, String> socketResult = new ConcurrentHashMap<>();

		int socketListenerId = getNextSocketListenerId();
		createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, false, 2000).start();
		
		Thread.sleep(1000);
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
        Response response = dispatcher.send(dispatcherProps, message);
        
        log(message.getConnectorMap().get(CONNECTOR_MAP_ALL_RESPONSES_KEY));
        assertEquals(Status.ERROR, response.getStatus());
	}
	
	/*
	 * testWhenSendToOneOfMultipleClientsErrors()
	 * -create TCP Sender
	 *   -Server mode
	 *   -BASIC mode
	 *   -Queue on response timeout=false
	 *   -Response timeout=1000
	 * -create 2x listener sockets that connect to the TCP Sender socket
	 * -create listener socket that connects to the TCP Sender socket
	 *   -this socket will do nothing after receiving a message
	 * -send message through TCP Sender
	 * -verify message status==SENT
	 * -verify connectorMap says numberOfClients==3
	 * -verify connectorMap says successfulSends==2
	 */
	@Test
	public void testWhenSendToOneOfMultipleClientsErrors() throws Exception {
		TcpDispatcherProperties props = createTcpDispatcherProperties();
		props.setQueueOnResponseTimeout(false);
		props.setResponseTimeout("1000");
		setupDispatcher(props);
		Map<String, String> socketResult = new ConcurrentHashMap<>();
		
		// Create two sockets that send a response immediately
		Map<Integer, SocketThread> socketListeners = new HashMap<>();
		for (int i = 0; i < 2; i++) {
			int socketListenerId = getNextSocketListenerId();
			SocketThread socketListenerThread = createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 0);
			socketListeners.put(socketListenerId, socketListenerThread);
			socketListenerThread.start();
		}
		
		// Create a socket that will cause a timeout before it sends a response
		int socketListenerId = getNextSocketListenerId();
		SocketThread socketListenerThread = createSocketListenerThread(socketListenerId, socketResult, dispatcherProps, true, 2000);
		socketListeners.put(socketListenerId, socketListenerThread);
		socketListenerThread.start();
		Thread.sleep(1000);
		
		log("Sending message...");
		ConnectorMessage message = new ConnectorMessage(TEST_CHANNEL_ID, TEST_CHANNEL_NAME, 1L, 1, TEST_SERVER_ID, Calendar.getInstance(), Status.PENDING);
        Response response = dispatcher.send(dispatcherProps, message);
        Integer numberOfClients = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_NO_OF_CLIENTS_KEY);
        Integer successfulSends = (Integer) message.getConnectorMap().get(CONNECTOR_MAP_SUCCESSFUL_SENDS_KEY);
        
        log(message.getConnectorMap().get(CONNECTOR_MAP_ALL_RESPONSES_KEY));
        assertEquals(Status.SENT, response.getStatus());
        assertEquals(new Integer(2), successfulSends);
        assertEquals(new Integer(3), numberOfClients);
	}
	
	private static void log(Object message) {
		if (PRINT_DEBUG_MESSAGES) {
			System.out.println(message);
		}
	}
	
	private static void logError(String message) {
		if (PRINT_DEBUG_MESSAGES) {
			logError(message);
		}
	}
	
	private SocketThread createSocketListenerThread(int socketListenerId, Map<String, String> socketResult, TcpDispatcherProperties dispatcherProps, boolean sendResponse, int delayAfterRead) {
		log("Creating socket thread " + socketListenerId + "...");
		return new SocketThread(socketListenerId, socketResult, dispatcherProps, sendResponse, delayAfterRead);
	}

	private static class TestTcpDispatcher extends TcpDispatcher {		
		public TestTcpDispatcher(String channelId, String serverId, Integer metaDataId, TcpDispatcherProperties properties) {
			super();
            setChannelId(channelId);
            setMetaDataId(metaDataId);
            setConnectorProperties(properties);

            if (properties.getDestinationConnectorProperties().isQueueEnabled()) {
                getQueue().setDataSource(new ConnectorMessageQueueDataSource(channelId, serverId, metaDataId, Status.QUEUED, isQueueRotate(), new PassthruDaoFactory()));
                getQueue().updateSize();
            }
		}
		
		@Override
		protected String getConfigurationClass() {
			return "com.mirth.connect.connectors.tcp.DefaultTcpConfiguration";
		}
	}
	
	private static class TestChannel extends Channel {
		@Override
		protected EventDispatcher getEventDispatcher() {
			return mock(EventDispatcher.class);
		}
	}
	
	private static class SocketThread extends Thread {
		private int socketListenerId;
		private Map<String, String> socketResult;
		private TcpDispatcherProperties dispatcherProps;
		private Socket socket;
		private boolean sendResponse;
		private int delayAfterRead;
		
		public SocketThread(int socketListenerId, Map<String, String> socketResult, TcpDispatcherProperties dispatcherProps, boolean sendResponse, int delayAfterRead) {
			this.socketListenerId = socketListenerId;
			this.socketResult = socketResult;
			this.dispatcherProps = dispatcherProps;
			this.sendResponse = sendResponse;
			this.delayAfterRead = delayAfterRead;
		}
		
		public void closeSocket() {
			log("Closing socket with remote port: " + socket.getPort() + " and local port: "
					+ socket.getLocalPort());
			try {
				socket.close();
			} catch (Exception e) {
				logError("Error closing socket: " + e.getMessage());
			}
		}
		
		@Override
		public void run() {
			try {
				socket = SocketUtil.createSocket(new DefaultTcpConfiguration());
				socket.setKeepAlive(false);
				socket.setSoLinger(false, 0);
				SocketUtil.connectSocket(socket, "127.0.0.1", Integer.parseInt(dispatcherProps.getLocalPort()), 0);
				log("Client socket created with remote port: " + socket.getPort() + " and local port: " + socket.getLocalPort());
				
				FrameStreamHandler handler = new FrameStreamHandler(socket.getInputStream(), socket.getOutputStream(), new DefaultBatchStreamReader(socket.getInputStream()), dispatcherProps.getTransmissionModeProperties());

				try {
					byte[] byteArray = handler.read();
					socketResult.put("result" + socketListenerId, new String(byteArray));
					if (delayAfterRead > 0) {
						Thread.sleep(delayAfterRead);
					}
					if (sendResponse) {
						handler.write("received".getBytes());
					}	
				} catch (NullPointerException e) {
					socketResult.put("result" + socketListenerId, "");
				}
			} catch (Throwable e) {
				logError("Error in socket listener thread " + e.getMessage());
			} finally {
				try {
					socket.close();
					log("Closed socket with remote port: " + socket.getPort() + " and local port: " + socket.getLocalPort());
				} catch (Exception e) {
					logError("Error closing socket listener " + e);
				}
			}
		}
	}
}
