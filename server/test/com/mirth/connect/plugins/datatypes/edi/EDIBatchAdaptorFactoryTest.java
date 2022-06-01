package com.mirth.connect.plugins.datatypes.edi;

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
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class EDIBatchAdaptorFactoryTest {
    private static Logger logger = LogManager.getLogger(EDIBatchAdaptorFactoryTest.class);
    
    @Test
    public void testDebug() throws Exception {
        SourceConnector sourceConnector = mock(SourceConnector.class);
        SerializerProperties serializerProperties = mock(SerializerProperties.class);
        EDISerializationProperties serializationProperties = new EDISerializationProperties();
        serializationProperties.setSegmentDelimiter("\r\n");
        serializationProperties.setSerializationType(SerializationType.XML);
        serializationProperties.setElementDelimiter("*");
        serializationProperties.setInferX12Delimiters(true);
        serializationProperties.setSubelementDelimiter(":");
        serializationProperties.setSegmentDelimiter("~");
        
        EDIBatchProperties batchProperties = new EDIBatchProperties();
        batchProperties.setBatchScript("var segDelim = '~';\n"
                + "var elementDelim = '*';\n"
                + "var subelementDelim = ':';\n"
                + "var infer = true;\n"
                + "​\n"
                + "var message = new java.lang.StringBuilder();\n"
                + "var ch;\n"
                + "while ((ch = consume(1)) != null) {\n"
                + "    if (infer) {\n"
                + "        elementDelim = consume(3);\n"
                + "        subelementDelim = consume(101);\n"
                + "        segDelim = consume(1);\n"
                + "        consumeNewline();\n"
                + "        infer = false;\n"
                + "        continue;\n"
                + "    } else if (ch == segDelim) {\n"
                + "        consumeNewline();\n"
                + "        reader.mark(3);\n"
                + "        if (readChar() == 'S' && readChar() == 'T') {\n"
                + "            reader.reset();\n"
                + "            break;\n"
                + "        }\n"
                + "        reader.reset();\n"
                + "    }\n"
                + "}\n"
                + "​\n"
                + "return message.toString();\n"
                + "​\n"
                + "function readChar() {\n"
                + "    var c = reader.read();\n"
                + "    return c == -1 ? null : java.lang.Character.valueOf(c);\n"
                + "}\n"
                + "​\n"
                + "function consume(num) {\n"
                + "    var ch;\n"
                + "    for (var i = 1; i <= num; i++) {\n"
                + "        ch = readChar();\n"
                + "        if (ch != null) {\n"
                + "            message.append(ch);\n"
                + "        }\n"
                + "    }\n"
                + "    return ch;\n"
                + "}\n"
                + "​\n"
                + "function consumeNewline() {\n"
                + "    reader.mark(1);\n"
                + "    var ch = readChar();\n"
                + "    if (ch == '\\r') {\n"
                + "        message.append(ch);\n"
                + "        reader.mark(1);\n"
                + "        if ((ch = readChar()) == '\\n') {\n"
                + "            message.append(ch);\n"
                + "        } else {\n"
                + "            reader.reset();\n"
                + "        }\n"
                + "    } else if (ch == '\\n') {\n"
                + "        message.append(ch);\n"
                + "    } else {\n"
                + "        reader.reset();\n"
                + "    }\n"
                + "} \n"
                + "");
        DebugOptions debugOptions = new DebugOptions(false, true, false, false, false, false, false);
        Channel channel = mock(Channel.class);
        
        when(sourceConnector.getChannel()).thenReturn(channel);
        when(channel.getDebugOptions()).thenReturn(debugOptions);
        when(serializerProperties.getBatchProperties()).thenReturn(batchProperties);
        when(serializerProperties.getSerializationProperties()).thenReturn(serializationProperties);
        TestEDIBatchAdaptorFactory batchAdaptorFactory = spy(new TestEDIBatchAdaptorFactory(sourceConnector, serializerProperties));
        
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
    
    private static class TestEDIBatchAdaptorFactory extends EDIBatchAdaptorFactory {
        private ContextFactoryController contextFactoryController;

        public TestEDIBatchAdaptorFactory(SourceConnector connector, SerializerProperties serializerProperties) {
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
