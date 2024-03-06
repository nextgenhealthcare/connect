package com.mirth.connect.connectors.tcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.donkey.model.message.DataType;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.plugins.TransmissionModeProvider;
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeServerPlugin;
import com.mirth.connect.plugins.mllpmode.MLLPModeProvider;
import com.mirth.connect.server.attachments.passthru.PassthruAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;

public class TcpReceiverTest {
	
	private static final String TEST_CHANNEL_ID = UUID.randomUUID().toString();
	private static final String TEST_SERVER_ID = UUID.randomUUID().toString();
	private static final String TEST_CHANNEL_NAME = "Test TCP Listener Channel";
	
	private static Logger logger = LogManager.getLogger(TcpReceiverTest.class);
	private TcpReceiver receiver;
	private TcpReceiverProperties receiverProps;
	
	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		ControllerFactory controllerFactory = mock(ControllerFactory.class);
		
		EventController eventController = mock(EventController.class);
        when(controllerFactory.createEventController()).thenReturn(eventController);

        ExtensionController extensionController = mock(ExtensionController.class);
        Map<String, TransmissionModeProvider> transmissionModeProviders = new HashMap<>();
        transmissionModeProviders.put("MLLP", new MLLPModeProvider());
        Map<String, DataTypeServerPlugin> dataTypePlugins = new HashMap<>();
        dataTypePlugins.put("HL7V2", mock(HL7v2DataTypeServerPlugin.class));
        when(extensionController.getTransmissionModeProviders()).thenReturn(transmissionModeProviders);
        when(extensionController.getDataTypePlugins()).thenReturn(dataTypePlugins);
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
		if (receiver != null) {
			logger.debug("Stopping TCP Receiver...");
			receiver.stop();
			logger.debug("Undeploying TCP Receiver...");
			receiver.onUndeploy();
			Thread.sleep(1000);
		}
	}
	
	private void setupReceiver(TcpReceiverProperties props) throws Exception {
		receiverProps = props;
		receiver = createTcpReceiver(receiverProps);
		
		logger.debug("Deploying TCP Receiver...");
		receiver.onDeploy();
		logger.debug("Starting TCP Receiver...");
		receiver.start();
		Thread.sleep(1000);
	}

	private TcpReceiver createTcpReceiver(TcpReceiverProperties receiverProps) {
		TcpReceiver receiver = new TestTcpReceiver(TEST_CHANNEL_ID, TEST_SERVER_ID, 0, receiverProps);
		receiver.setChannelId(TEST_CHANNEL_ID);
		receiver.setInboundDataType(new DataType("HL7V2", null, null));
		
		Channel channel = new TestChannel();
		channel.setChannelId(TEST_CHANNEL_ID);
		channel.setName(TEST_CHANNEL_NAME);
		
		PassthruAttachmentHandlerProvider attachmentHandlerProvider = new PassthruAttachmentHandlerProvider(null);
		channel.setAttachmentHandlerProvider(attachmentHandlerProvider);
		
		receiver.setChannel(channel);
		return receiver;
	}
	
	@Test
	public void testServerSocketLocalHost1() throws Exception {
		TcpReceiverProperties props = new TcpReceiverProperties();
		props.getListenerConnectorProperties().setHost("127.0.0.1");
		props.getListenerConnectorProperties().setPort("6666");
		setupReceiver(props);
		
		assertEquals("127.0.0.1", receiver.getServerSocket().getInetAddress().getHostAddress());
		assertEquals(6666, receiver.getServerSocket().getLocalPort());
	}
	
	@Test
	public void testServerSocketLocalHost2() throws Exception {
		TcpReceiverProperties props = new TcpReceiverProperties();
		props.getListenerConnectorProperties().setHost("localhost");
		props.getListenerConnectorProperties().setPort("6666");
		setupReceiver(props);
		
		assertEquals("localhost", receiver.getServerSocket().getInetAddress().getHostName());
		assertEquals(6666, receiver.getServerSocket().getLocalPort());
	}
	
	@Test
	public void testServerSocketAllInterfaces() throws Exception {
		TcpReceiverProperties props = new TcpReceiverProperties();
		props.getListenerConnectorProperties().setHost("0.0.0.0");
		props.getListenerConnectorProperties().setPort("6666");
		setupReceiver(props);
		
		assertEquals("0.0.0.0", receiver.getServerSocket().getInetAddress().getHostAddress());
		assertEquals(6666, receiver.getServerSocket().getLocalPort());
	}
	
	@Test
	public void testServerSocketUnknownHost(){
		TcpReceiverProperties props = new TcpReceiverProperties();
		props.getListenerConnectorProperties().setHost("111.1.1.1");
		props.getListenerConnectorProperties().setPort("6666");
		
		boolean exceptionThrown = false;
		try {
			setupReceiver(props);
		} catch (Exception e) {
			assertTrue(e instanceof ConnectorTaskException);
			exceptionThrown = true;
		}
		
		assertTrue(exceptionThrown);
	}
	
	private static class TestTcpReceiver extends TcpReceiver {		
		public TestTcpReceiver(String channelId, String serverId, Integer metaDataId, TcpReceiverProperties properties) {
			super();
            setChannelId(channelId);
            setMetaDataId(metaDataId);
            setConnectorProperties(properties);
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
}
