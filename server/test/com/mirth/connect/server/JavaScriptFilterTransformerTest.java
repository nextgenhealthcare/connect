package com.mirth.connect.server;

import static org.junit.Assert.assertEquals;
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
import org.mockito.Mockito;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.connectors.jdbc.DatabaseDispatcher;
import com.mirth.connect.connectors.jdbc.DatabaseDispatcherProperties;
import com.mirth.connect.connectors.jdbc.DatabaseDispatcherScript;
import com.mirth.connect.connectors.jdbc.DatabaseDispatcherScriptTest;
import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.transformers.JavaScriptFilterTransformer;
import com.mirth.connect.server.transformers.JavaScriptInitializationException;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptFilterTransformerTest {

    private static Logger logger = Logger.getLogger(JavaScriptFilterTransformerTest.class);
    private DebugOptions debugOptions;
    private final static String TEST_SCRIPT_ID = "testscript";
    private final static String PERFORMANCE_SCRIPT_ID = "performancescript";
    private final static String CONNECTOR_NAME = "testconnector";

    @Before
    public void setup() {
        debugOptions = new DebugOptions();
        debugOptions.setSourceFilterTransformer(false);

    }

    @Test
    public void testTransformer() throws Exception {
        SourceConnector connector = mock(SourceConnector.class);
        Channel channel = mock(Channel.class);
        debugOptions.setSourceFilterTransformer(false);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";

        MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
//        
        when(connector.getChannel()).thenReturn(channel);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(connector.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);

        when(connector.getChannel()).thenReturn(channel);

        TestJavaScriptFilterTransformer JavaScriptFilterTransformer1 = new TestJavaScriptFilterTransformer(connector, CONNECTOR_NAME, TEST_SCRIPT_ID, null, debugOptions);

        assertEquals(JavaScriptFilterTransformer1.debuggerCallCount, 0);

    }

    @Test
    public void testDebugSourceTransformer() throws Exception {
        SourceConnector connector = mock(SourceConnector.class);
        Channel channel = mock(Channel.class);
        debugOptions.setSourceFilterTransformer(true);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";

        MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
        when(connector.getChannel()).thenReturn(channel);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);
        when(connector.getChannel()).thenReturn(channel);

        TestJavaScriptFilterTransformer JavaScriptFilterTransformer = new TestJavaScriptFilterTransformer(connector, CONNECTOR_NAME, TEST_SCRIPT_ID, null, debugOptions);

        assertEquals(JavaScriptFilterTransformer.debuggerCallCount, 1);

    }

    private static class TestJavaScriptFilterTransformer extends JavaScriptFilterTransformer {

        private static String TEST_CHANNEL_ID = "testChannelId";
        private ContextFactoryController contextFactoryController;
        private ChannelController channelController;
        private DatabaseDispatcher connector;
        public static int debuggerCallCount = 0;

        public TestJavaScriptFilterTransformer(Connector connector, String connectorName, String script, String template, DebugOptions debugOptions) throws JavaScriptInitializationException {

            super(connector, connectorName, script, template, debugOptions);

            // TODO Auto-generated constructor stub
        }

        @Override
        protected void removeScriptFromCache() {
            //doNothing
        }

        @Override
        protected void compileAndAddScript(String script, MirthContextFactory contextFactory) throws Exception {
            //doNothing
        }

        @Override
        protected MirthContextFactory getContextFactory() throws Exception {

            MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);
            return mirthContextFactory;
        }

        @Override
        protected MirthMain getDebugger(Channel channel, MirthContextFactory contextFactory) {
            this.debuggerCallCount = debuggerCallCount + 1;
            MirthMain mirthMain = mock(MirthMain.class);
            return mirthMain;

        }

        @Override
        protected MirthContextFactory getDebugContextFactory() throws Exception {
            MirthContextFactory mirthContextFactory = mock(MirthContextFactory.class);

            return mirthContextFactory;
        }

    }

}
