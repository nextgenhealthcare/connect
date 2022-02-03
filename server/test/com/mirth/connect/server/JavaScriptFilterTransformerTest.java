package com.mirth.connect.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;


import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.server.transformers.JavaScriptFilterTransformer;
import com.mirth.connect.server.transformers.JavaScriptInitializationException;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptFilterTransformerTest {

    private DebugOptions debugOptions;
    private final static String TEST_SCRIPT_ID = "testscript";
    private final static String CONNECTOR_NAME = "testconnector";

    @Before
    public void setup() {
        debugOptions = new DebugOptions();
        debugOptions.setSourceFilterTransformer(false);

    }


    
    @Test
    public void testTransformerSource() throws Exception {
        SourceConnector connector = mock(SourceConnector.class);
        Channel channel = mock(Channel.class);
        debugOptions.setSourceFilterTransformer(false);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";

        when(connector.getChannel()).thenReturn(channel);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(connector.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);

        when(connector.getChannel()).thenReturn(channel);

        TestJavaScriptFilterTransformer JavaScriptFilterTransformer2 = new TestJavaScriptFilterTransformer(connector, CONNECTOR_NAME, TEST_SCRIPT_ID, null, debugOptions);

        assertEquals(0 , JavaScriptFilterTransformer2.debuggerCallCount);
        JavaScriptFilterTransformer2.resetCount();

    }
    
    
    @Test
    public void testTransformerDestination() throws Exception {
        DestinationConnector connector = mock(DestinationConnector.class);
        Channel channel = mock(Channel.class);
        debugOptions.setSourceFilterTransformer(false);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";

        when(connector.getChannel()).thenReturn(channel);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(connector.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);

        when(connector.getChannel()).thenReturn(channel);

        TestJavaScriptFilterTransformer JavaScriptFilterTransformer2 = new TestJavaScriptFilterTransformer(connector, CONNECTOR_NAME, TEST_SCRIPT_ID, null, debugOptions);

        assertEquals(0 , JavaScriptFilterTransformer2.debuggerCallCount);
        JavaScriptFilterTransformer2.resetCount();

    }
    

    @Test
    public void testDebugSourceTransformer() throws Exception {
        SourceConnector connector = mock(SourceConnector.class);
        Channel channel = mock(Channel.class);
        debugOptions.setSourceFilterTransformer(true);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";

        when(connector.getChannel()).thenReturn(channel);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);
        when(connector.getChannel()).thenReturn(channel);

        TestJavaScriptFilterTransformer JavaScriptFilterTransformer = new TestJavaScriptFilterTransformer(connector, CONNECTOR_NAME, TEST_SCRIPT_ID, null, debugOptions);

        assertEquals(1, JavaScriptFilterTransformer.debuggerCallCount);
        JavaScriptFilterTransformer.debuggerCallCount = 0;
        JavaScriptFilterTransformer.resetCount();


    }
    
    @Test
    public void testDebugDestinationTransformer() throws Exception {
        DestinationConnector connector = mock(DestinationConnector.class);
        Channel channel = mock(Channel.class);
        debugOptions.setDestinationFilterTransformer(true);
        Set<String> resourceIds = new HashSet<String>();
        resourceIds.add("resourceId");
        String channelId = "channelId";

        when(connector.getChannel()).thenReturn(channel);
        when(channel.getResourceIds()).thenReturn(resourceIds);
        when(channel.getChannelId()).thenReturn(channelId);
        when(connector.getChannel()).thenReturn(channel);

        TestJavaScriptFilterTransformer JavaScriptFilterTransformer1 = new TestJavaScriptFilterTransformer(connector, CONNECTOR_NAME, TEST_SCRIPT_ID, null, debugOptions);

        assertEquals(1 , JavaScriptFilterTransformer1.debuggerCallCount);
        JavaScriptFilterTransformer1.debuggerCallCount = 0;
        JavaScriptFilterTransformer1.resetCount();

    }


    private static class TestJavaScriptFilterTransformer extends JavaScriptFilterTransformer {

        public static int debuggerCallCount = 0;

        public TestJavaScriptFilterTransformer(Connector connector, String connectorName, String script, String template, DebugOptions debugOptions) throws JavaScriptInitializationException {

            super(connector, connectorName, script, template, debugOptions);

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
        
        public void resetCount() {
            debuggerCallCount = 0;
        }

    }

}
