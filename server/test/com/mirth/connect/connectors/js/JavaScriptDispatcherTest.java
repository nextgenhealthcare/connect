package com.mirth.connect.connectors.js;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.Main;

import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptDispatcherTest {
	private static Logger logger = Logger.getLogger(JavaScriptDispatcherTest.class);
	
	@Test
	public void testDebug() throws Exception {
		// Deploy
		TestJavaScriptDispatcher dispatcher = new TestJavaScriptDispatcher();
		dispatcher.onDebugDeploy();
		
		Main debugger = dispatcher.getDebugger(mock(MirthContextFactory.class));
		ContextFactoryController contextFactoryController = dispatcher.getContextFactoryController();
		
		verify(debugger, times(1)).setExitAction(any());
		verify(contextFactoryController, times(1)).getDebugContextFactory(any(), any());
		
		// Undeploy
		dispatcher.onUndeploy();
		
		verify(debugger, times(1)).detach();
		verify(debugger, times(1)).dispose();
		verify(contextFactoryController, times(1)).removeDebugContextFactory(any(), any());
	}
	
	private static class TestJavaScriptDispatcher extends JavaScriptDispatcher {
		private Main debugger = mock(Main.class);
		private ContextFactoryController contextFactoryController;
		
		@Override
		protected EventController getEventController() {
	    	return mock(EventController.class);
	    }
	    
		@Override
	    protected ContextFactoryController getContextFactoryController() {
			try {
				if (contextFactoryController == null) {
			    	contextFactoryController = mock(ContextFactoryController.class);
			    	MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
			    	when(mirthContextFactory.getId()).thenReturn("contextFactoryId");
			    	when(contextFactoryController.getDebugContextFactory(any(), any()))
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
		protected Main getDebugger(MirthContextFactory contextFactory) {
			return debugger;
		}
		
		@Override
		protected void compileAndAddScript(MirthContextFactory contextFactory, String scriptId) throws Exception {
			// Do nothing
		}
	}
}
