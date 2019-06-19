/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.server.TestUtils.DummyChannel;
import com.mirth.connect.server.channel.MirthMetaDataReplacer;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DatabaseReceiverInvalidColumnNameTests {

    private final static String DB_DRIVER = "org.postgresql.Driver";
    private final static String DB_URL = "jdbc:postgresql://localhost:5432/test";
    private final static String DB_USERNAME = "";
    private final static String DB_PASSWORD = "";
    private final static String TEST_CHANNEL_ID = "testchannel";
    private final static String TEST_SERVER_ID = "testserver";

    @BeforeClass
    public static void setupControllers() throws Exception {
        ControllerFactory controllerFactory = mock(ControllerFactory.class);

        EventController eventController = mock(EventController.class);
        when(controllerFactory.createEventController()).thenReturn(eventController);

        ConfigurationController configurationController = mock(ConfigurationController.class);
        when(controllerFactory.createConfigurationController()).thenReturn(configurationController);

        ExtensionController extensionController = mock(ExtensionController.class);
        when(controllerFactory.createExtensionController()).thenReturn(extensionController);

        ContextFactoryController contextFactoryController = mock(ContextFactoryController.class);
        when(contextFactoryController.getContextFactory(any())).thenReturn(new MirthContextFactory(null, null));
        when(controllerFactory.createContextFactoryController()).thenReturn(contextFactoryController);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        injector.getInstance(ControllerFactory.class);
    }

    @Test
    public void test() throws Exception {
        /**
         * Create a table invalidchartest with column names that don't have valid XML characters.
         * 
         * Examples: .abc [def]
         */

        DatabaseReceiverProperties properties = new DatabaseReceiverProperties();
        properties.setDriver(DB_DRIVER);
        properties.setUrl(DB_URL);
        properties.setUsername(DB_USERNAME);
        properties.setPassword(DB_PASSWORD);
        properties.setSelect("SELECT * FROM invalidchartest");
        properties.getPollConnectorProperties().setPollOnStart(true);

        TestChannel testChannel = new TestChannel();

        DatabaseReceiver connector = new DatabaseReceiver();
        connector.setConnectorProperties(properties);
        connector.setChannelId(testChannel.getChannelId());
        connector.setChannel(testChannel);
        connector.setMetaDataId(0);
        connector.setMetaDataReplacer(new MirthMetaDataReplacer());
        connector.setRespondAfterProcessing(true);
        connector.setFilterTransformerExecutor(new FilterTransformerExecutor(connector.getInboundDataType(), connector.getOutboundDataType()));
        testChannel.setSourceConnector(connector);

        connector.onDeploy();
        connector.start();

        Thread.sleep(1000);

        connector.stop();
        connector.onUndeploy();

        assertTrue(testChannel.messages.size() > 0);
    }

    private class TestChannel extends DummyChannel {

        private List<RawMessage> messages = new ArrayList<RawMessage>();

        public TestChannel() {
            super(TEST_CHANNEL_ID, TEST_SERVER_ID);
        }

        @Override
        protected EventDispatcher getEventDispatcher() {
            return e -> {
            };
        }

        @Override
        protected DispatchResult dispatchRawMessage(RawMessage rawMessage, boolean batch) throws ChannelException {
            messages.add(rawMessage);
            return null;
        }
    }
}
