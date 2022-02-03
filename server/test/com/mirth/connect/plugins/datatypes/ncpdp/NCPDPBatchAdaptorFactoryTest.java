package com.mirth.connect.plugins.datatypes.ncpdp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class NCPDPBatchAdaptorFactoryTest {
    private static Logger logger = Logger.getLogger(NCPDPBatchAdaptorFactoryTest.class);
    private DebugOptions debugOptions;

    @Before
    public void setup() {
        debugOptions = new DebugOptions();
        debugOptions.setSourceConnectorScripts(true);
    }
    
    @Test
    public void testDebug() throws Exception {
        SourceConnector sourceConnector = mock(SourceConnector.class);
        SerializerProperties serializerProperties = mock(SerializerProperties.class);
        NCPDPBatchProperties batchProperties = new NCPDPBatchProperties();
        batchProperties.setBatchScript("610066C4B112345678901044563663bbbbbbbb2003050198765bbbbbAM01C419620615C51CAJOSEPHCBSMITHCM123 MAIN STREETCNMY TOWNCOCOCP34567CQ2014658923CZ50Z1231C2AM04C2987654321ACCJOSEPHCDSMITHAM07EM1D21234567E103D700009011312E71D30D530D61D80DE20030501DF5DJ1DK9ET1C82DT128EAAM03EZ06DBA12345DRJONESPM20136395722E1DL123456H51014EWRIGHTAM11D9557{DC100{DX100{H71H801H9150{DQ700{DU807{DN3");
        DebugOptions debugOptions = new DebugOptions(false, true, false, false, false, false, false);
        Channel channel = mock(Channel.class);
        
        when(sourceConnector.getChannel()).thenReturn(channel);
        when(channel.getDebugOptions()).thenReturn(debugOptions);
        when(serializerProperties.getBatchProperties()).thenReturn(batchProperties);
        TestNCPDPBatchAdaptorFactory batchAdaptorFactory = spy(new TestNCPDPBatchAdaptorFactory(sourceConnector, serializerProperties));
        
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
    
    private static class TestNCPDPBatchAdaptorFactory extends NCPDPBatchAdaptorFactory {
        private ContextFactoryController contextFactoryController;

        public TestNCPDPBatchAdaptorFactory(SourceConnector connector, SerializerProperties serializerProperties) {
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
