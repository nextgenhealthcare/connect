package com.mirth.connect.connectors.js;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.model.Channel;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptDispatcherTest {
    private static Logger logger = Logger.getLogger(JavaScriptDispatcherTest.class);
    private DebugOptions debugOptions;
    
    @Before
    public void setup() {
        debugOptions = new DebugOptions();
        debugOptions.setDestinationConnectorScripts(true);
    }
    
    @Test
    public void testDebug() throws Exception {
        // Deploy
        TestJavaScriptDispatcher dispatcher = new TestJavaScriptDispatcher();
        dispatcher.onDebugDeploy(debugOptions);
        
        MirthMain debugger = dispatcher.getDebugger(mock(MirthContextFactory.class));
        ContextFactoryController contextFactoryController = dispatcher.getContextFactoryController();
        
        verify(contextFactoryController, times(1)).getDebugContextFactory(any(), any(),any());
        
        // Undeploy
        dispatcher.onUndeploy();
        
        verify(debugger, times(1)).dispose();
        verify(contextFactoryController, times(1)).removeDebugContextFactory(any(), any(), any());
    }
    
    @Test
    public void testOnStop() throws Exception {
        TestJavaScriptDispatcher dispatcher = new TestJavaScriptDispatcher();
        dispatcher.onDebugDeploy(debugOptions);
        
        MirthMain debugger = dispatcher.getDebugger(mock(MirthContextFactory.class));
        dispatcher.onStop();
        
        verify(debugger, times(1)).finishScriptExecution();
    }
    
    @Test
    public void testStopDebugging() throws Exception {
        TestJavaScriptDispatcher dispatcher = new TestJavaScriptDispatcher();
        dispatcher.onDebugDeploy(debugOptions);
        
        MirthMain debugger = dispatcher.getDebugger(mock(MirthContextFactory.class));
        dispatcher.stopDebugging();
        
        verify(debugger, times(1)).finishScriptExecution();
    }
    
    @Test
    public void testOnStart() throws Exception {
        TestJavaScriptDispatcher dispatcher = new TestJavaScriptDispatcher();
        dispatcher.onDebugDeploy(debugOptions);
        
        MirthMain debugger = dispatcher.getDebugger(mock(MirthContextFactory.class));
        dispatcher.onStart();
        
        verify(debugger, times(1)).enableDebugging();
    }
    
    private static class TestJavaScriptDispatcher extends JavaScriptDispatcher {
    	private static String TEST_CHANNEL_ID = "testChannelId";
    	
        private MirthMain debugger = mock(MirthMain.class);
        private ContextFactoryController contextFactoryController;
        private ChannelController channelController;
        private com.mirth.connect.model.Channel testChannel;
        private JavaScriptDispatcherProperties connectorProperties;
        
        public TestJavaScriptDispatcher() {
        	channelController = mock(ChannelController.class);
    		testChannel = new Channel();
    		testChannel.setId(TEST_CHANNEL_ID);
    		when(channelController.getChannelById(anyString())).thenReturn(testChannel);
    		
    		connectorProperties = new JavaScriptDispatcherProperties();
    		connectorProperties.setScript("logger.info('test script');");
		}
        
        @Override
        protected EventController getEventController() {
            return mock(EventController.class);
        }
        
        @Override
        protected ChannelController getChannelController() {
        	return channelController;
        }
        
        @Override
        protected ScriptController getScriptController() {
        	return mock(ScriptController.class);
        }
        
        @Override
        public ConnectorProperties getConnectorProperties() {
        	return connectorProperties;
        }
        
        @Override
        protected ContextFactoryController getContextFactoryController() {
            try {
                if (contextFactoryController == null) {
                    contextFactoryController = mock(ContextFactoryController.class);
                    MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
                    when(mirthContextFactory.getId()).thenReturn("contextFactoryId");
                    when(contextFactoryController.getDebugContextFactory(any(), any(), any()))
                        .thenReturn(mirthContextFactory);
                }
                
                return contextFactoryController;
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            return null;
        }
        
        @Override
        protected CompiledScriptCache getCompiledScriptCache() {
            return mock(CompiledScriptCache.class);
        }
        
        @Override
        protected MirthMain getDebugger(MirthContextFactory contextFactory) {
            return debugger;
        }
        
        @Override
        protected void compileAndAddScript(MirthContextFactory contextFactory, String scriptId) throws Exception {
            // Do nothing
        }
    }
}