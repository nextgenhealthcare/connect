/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DatabaseDispatcherScriptTest {

    private static Logger logger = Logger.getLogger(DatabaseDispatcherScriptTest.class);
    private DebugOptions debugOptions;

    @Before
    public void setup() {
        debugOptions = new DebugOptions();
        debugOptions.setDestinationConnectorScripts(true);
       
    }

    @Test
    public void testOnDebugDeploy() throws Exception {

        DatabaseDispatcher connector = mock(DatabaseDispatcher.class);
        Channel channel = mock(Channel.class);
        TestDatabaseDispatcherScript dispatcher = new TestDatabaseDispatcherScript(connector);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";
        ContextFactoryController contextFactoryController = dispatcher.getContextFactoryController();
        MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
        DatabaseDispatcherProperties databaseDispatcherProperties =  mock(DatabaseDispatcherProperties.class);
        
        when(connector.getChannel()).thenReturn(channel);
        when(channel.getDebugOptions()).thenReturn(debugOptions);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);
        when(contextFactoryController.getDebugContextFactory(any(), any(), any())).thenReturn(mirthContextFactory);
        when(connector.getConnectorProperties()).thenReturn(databaseDispatcherProperties);
        when(databaseDispatcherProperties.getQuery()).thenReturn("testScript");

        dispatcher.deploy();
        verify(contextFactoryController, times(1)).getDebugContextFactory(any(), any(), any());
        verify(contextFactoryController, times(0)).getContextFactory(any());
        
        //verify unDeploy method
        dispatcher.undeploy();
        verify(contextFactoryController, times(1)).removeDebugContextFactory(any(),any(),any());
    }

    @Test
    public void testDeploy() throws Exception {

        debugOptions.setDestinationConnectorScripts(false);
        DatabaseDispatcher connector = mock(DatabaseDispatcher.class);

        Channel channel = mock(Channel.class);
        TestDatabaseDispatcherScript dispatcher = new TestDatabaseDispatcherScript(connector);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";
        ContextFactoryController contextFactoryController = dispatcher.getContextFactoryController();
        MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
        
        when(connector.getChannel()).thenReturn(channel);
        when(channel.getDebugOptions()).thenReturn(debugOptions);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);
        when(contextFactoryController.getDebugContextFactory(any(), any(), any())).thenReturn(mirthContextFactory);

        dispatcher.deploy();
    
        verify(contextFactoryController, times(0)).getDebugContextFactory(any(), any(), any());
        verify(contextFactoryController, times(1)).getContextFactory(any());
        
        //verify unDeploy method
        dispatcher.undeploy();
        verify(contextFactoryController, times(0)).removeDebugContextFactory(any(),any(),any());

    }


    private static class TestDatabaseDispatcherScript extends DatabaseDispatcherScript {
        private static String TEST_CHANNEL_ID = "testChannelId";
        private ContextFactoryController contextFactoryController;
        private ChannelController channelController;
        private DatabaseDispatcher connector;

        public TestDatabaseDispatcherScript(DatabaseDispatcher connector) {
            super(connector);
                
        }

        @Override
        public ContextFactoryController getContextFactoryController() {
            try {
                if (contextFactoryController == null) {
                    contextFactoryController = mock(ContextFactoryController.class);
                    MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
                    when(mirthContextFactory.getId()).thenReturn("contextFactoryId");
                    when(contextFactoryController.getDebugContextFactory(any(), any(), any())).thenReturn(mirthContextFactory);
                    when(contextFactoryController.getContextFactory(any())).thenReturn(mirthContextFactory);
                   
                }

                return contextFactoryController;
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return null;
        }
        
        @Override 
        protected void removeScriptFromCache() {
            //doNothing
        }
        
        @Override
        protected MirthMain getDebugger(Channel channel, MirthContextFactory contextFactory) {
            MirthMain mirthMain = mock(MirthMain.class);
            return mirthMain;
        }
        
        @Override
        protected void compileAndAddScript(DatabaseDispatcherProperties connectorProperties, MirthContextFactory contextFactory) throws Exception {
           //doNothing
        }


    }

}
