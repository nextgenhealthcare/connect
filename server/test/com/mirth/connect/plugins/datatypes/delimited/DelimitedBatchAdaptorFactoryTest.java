package com.mirth.connect.plugins.datatypes.delimited;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class DelimitedBatchAdaptorFactoryTest {
    private static Logger logger = LogManager.getLogger(DelimitedBatchAdaptorFactoryTest.class);
    
    @Test
    public void testDebug() throws Exception {
        SourceConnector sourceConnector = mock(SourceConnector.class);
        SerializerProperties serializerProperties = mock(SerializerProperties.class);
        DelimitedBatchProperties batchProperties = new DelimitedBatchProperties();
        batchProperties.setBatchScript("DelimitedBatchScript");
        DebugOptions debugOptions = new DebugOptions(false, true, false, false, false, false, false);
        Channel channel = mock(Channel.class);
        
        when(sourceConnector.getChannel()).thenReturn(channel);
        when(channel.getDebugOptions()).thenReturn(debugOptions);
        when(serializerProperties.getBatchProperties()).thenReturn(batchProperties);
        TestDelimitedBatchAdaptorFactory batchAdaptorFactory = spy(new TestDelimitedBatchAdaptorFactory(sourceConnector, serializerProperties));
        
        batchAdaptorFactory.onDeploy();
        verify(batchAdaptorFactory, times(1)).setDebugger(any());
        verify(batchAdaptorFactory, times(1)).getDebugger(any(), anyBoolean());
        
        MirthMain debugger = batchAdaptorFactory.getDebugger();
        
        batchAdaptorFactory.start();
        verify(debugger, times(1)).enableDebugging();
        
        batchAdaptorFactory.stop();
        verify(debugger, times(1)).finishScriptExecution();

        batchAdaptorFactory.onUndeploy();
        verify(batchAdaptorFactory.getContextFactoryController(), times(1)).removeDebugContextFactory(any(),any(),any());
        verify(debugger, times(1)).dispose();
    }
    
    private static class TestDelimitedBatchAdaptorFactory extends DelimitedBatchAdaptorFactory {
        private ContextFactoryController contextFactoryController;

        public TestDelimitedBatchAdaptorFactory(SourceConnector connector, SerializerProperties serializerProperties) {
            super(connector, serializerProperties);
            debugger = mock(MirthMain.class);
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
        protected MirthMain getDebugger(MirthContextFactory contextFactory, boolean showDebugger) {
            return debugger;
        }
        
        @Override
        protected MirthContextFactory generateContextFactory(boolean debug, String script) throws ConnectorTaskException {
            MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
            return mirthContextFactory;
        }
    }
}
