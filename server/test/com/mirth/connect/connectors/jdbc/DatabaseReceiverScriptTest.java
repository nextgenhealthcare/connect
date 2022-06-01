package com.mirth.connect.connectors.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DatabaseReceiverScriptTest {
    

    private static Logger logger = LogManager.getLogger(DatabaseReceiverScriptTest.class);
    private DebugOptions debugOptions;

    @Before
    public void setup() {
        debugOptions = new DebugOptions();
        debugOptions.setSourceConnectorScripts(true);
       
    }
    

    @Test
    public void testOnDebugDeploy() throws Exception {

        DatabaseReceiver connector = mock(DatabaseReceiver.class);
        Channel channel = mock(Channel.class);
        TestDatabaseReceiverScript dispatcher = new TestDatabaseReceiverScript(connector);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";
        ContextFactoryController contextFactoryController = dispatcher.getContextFactoryController();
        MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
        DatabaseReceiverProperties databaseDispatcherProperties =  mock(DatabaseReceiverProperties.class);
        
        when(connector.getChannel()).thenReturn(channel);
        when(channel.getDebugOptions()).thenReturn(debugOptions);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);
        when(contextFactoryController.getDebugContextFactory(any(), any(), any())).thenReturn(mirthContextFactory);
        when(connector.getConnectorProperties()).thenReturn(databaseDispatcherProperties);
        when(databaseDispatcherProperties.getSelect()).thenReturn("select");

        dispatcher.deploy();
        verify(contextFactoryController, times(1)).getDebugContextFactory(any(), any(), any());
        verify(contextFactoryController, times(0)).getContextFactory(any());
        
        //verify unDeploy method
        dispatcher.undeploy();
        verify(contextFactoryController, times(2)).removeDebugContextFactory(any(),any(),any());
    }


    @Test
    public void testDeploy() throws Exception {

        debugOptions.setSourceConnectorScripts(false);
        DatabaseReceiver connector = mock(DatabaseReceiver.class);

        Channel channel = mock(Channel.class);
        TestDatabaseReceiverScript dispatcher = new TestDatabaseReceiverScript(connector);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";
        ContextFactoryController contextFactoryController = dispatcher.getContextFactoryController();
        MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
        DatabaseReceiverProperties databaseDispatcherProperties =  mock(DatabaseReceiverProperties.class);

        
        when(connector.getChannel()).thenReturn(channel);
        when(channel.getDebugOptions()).thenReturn(debugOptions);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);
        when(contextFactoryController.getDebugContextFactory(any(), any(), any())).thenReturn(mirthContextFactory);
        when(connector.getConnectorProperties()).thenReturn(databaseDispatcherProperties);
        when(databaseDispatcherProperties.getSelect()).thenReturn("select");


        dispatcher.deploy();
    
        verify(contextFactoryController, times(0)).getDebugContextFactory(any(), any(), any());
        verify(contextFactoryController, times(1)).getContextFactory(any());
        
        //verify unDeploy method
        dispatcher.undeploy();
        verify(contextFactoryController, times(0)).removeDebugContextFactory(any(),any(),any());

    }

    
    private static class TestDatabaseReceiverScript extends DatabaseReceiverScript {
        private static String TEST_CHANNEL_ID = "testChannelId";
        private ContextFactoryController contextFactoryController;
        private ChannelController channelController;
        private DatabaseDispatcher connector;

        public TestDatabaseReceiverScript(DatabaseReceiver connector) {
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
        protected void removeScriptFromCache(String scriptId) {
            //doNothing
        }
        
        @Override
        protected MirthMain getDebugger(Channel channel, MirthContextFactory contextFactory) {
            MirthMain mirthMain = mock(MirthMain.class);
            return mirthMain;
        }
        
        @Override
        protected void compileAndAddScript(String channelId, MirthContextFactory contextFactory, String scriptId, String selectOrUpdate, ContextType contextType) throws Exception {
           //doNothing
        }


    }

}
