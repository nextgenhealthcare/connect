package com.mirth.connect.plugins.datatypes.hl7v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class ER7BatchAdaptorFactoryTest {
    private static Logger logger = Logger.getLogger(ER7BatchAdaptorFactoryTest.class);
    
    @Test
    public void testDebug() throws Exception {
        SourceConnector sourceConnector = mock(SourceConnector.class);
        SerializerProperties serializerProperties = mock(SerializerProperties.class);
        HL7v2SerializationProperties serializationProperties = new HL7v2SerializationProperties();
        serializationProperties.setSegmentDelimiter("\r\n");
        serializationProperties.setSerializationType(SerializationType.XML);
        serializationProperties.setConvertLineBreaks(true);
        HL7v2BatchProperties batchProperties = new HL7v2BatchProperties();
        batchProperties.setBatchScript("var message = new java.lang.StringBuilder();\r\n"
        		+ "var line;\r\n"
        		+ "while ((line = reader.readLine()) != null) {\r\n"
        		+ " message.append(line).append('\\r');\r\n"
        		+ " // Mark the stream for 3 characters while we check for MSH\r\n"
        		+ " reader.mark(3);\r\n"
        		+ " // Check for the code points corresponding to MSH\r\n"
        		+ " if (reader.read() == 77 && reader.read() == 83 && reader.read() == 72) {\r\n"
        		+ " reader.reset();\r\n"
        		+ " break;\r\n"
        		+ " }\r\n"
        		+ " reader.reset();\r\n"
        		+ "}\r\n"
        		+ "return message.toString();");
        DebugOptions debugOptions = new DebugOptions(false, true, false, false, false, false, false);
        Channel channel = mock(Channel.class);
        
        when(sourceConnector.getChannel()).thenReturn(channel);
        when(channel.getDebugOptions()).thenReturn(debugOptions);
        when(serializerProperties.getBatchProperties()).thenReturn(batchProperties);
        when(serializerProperties.getSerializationProperties()).thenReturn(serializationProperties);
        TestER7BatchAdaptorFactory batchAdaptorFactory = spy(new TestER7BatchAdaptorFactory(sourceConnector, serializerProperties));
        
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
    
    private static class TestER7BatchAdaptorFactory extends ER7BatchAdaptorFactory {
        private ContextFactoryController contextFactoryController;

        public TestER7BatchAdaptorFactory(SourceConnector connector, SerializerProperties serializerProperties) {
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
