package com.mirth.connect.server.transformers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.UUID;

import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;


public class JavaScriptResponseTransformerTest {

    public String connectorName = "Destination 1";
    private int countCompileAndAddScript = 0; 
    private int countGetDebugger = 0;
    
    @Test
    public void testDoTransform() throws Exception {
        
        Response mockResponse = mock(Response.class);
        ConnectorMessage mockConnectorMessage = mock(ConnectorMessage.class);
        Channel mockChannel = mock(Channel.class);
        DebugOptions debugOptions = new DebugOptions();
        debugOptions.setDestinationResponseTransformer(true);
        String template = null;
        String script = "script"; 
        
        Connector mockConnector = mock(Connector.class);
        
        when(mockConnector.getMetaDataId()).thenReturn(1);
        when(mockConnector.getChannel()).thenReturn(mockChannel);
        when(mockChannel.getChannelId()).thenReturn(UUID.randomUUID().toString());
        
        HashSet<String> resourceIds = new HashSet<>();
        resourceIds.add("Default Resource");
        when(mockChannel.getResourceIds()).thenReturn(resourceIds);
        
        TestJavaScriptResponseTransformer spyTransformer = spy(new TestJavaScriptResponseTransformer(mockConnector, connectorName, script, template, debugOptions));

        spyTransformer.doTransform(mockResponse, mockConnectorMessage);
        
        verify(spyTransformer, times(1)).execute(any(), any(), any());
        assertEquals(countCompileAndAddScript, 1);
        assertEquals(countGetDebugger, 1);
    }
    
    private class TestJavaScriptResponseTransformer extends JavaScriptResponseTransformer {

        public TestJavaScriptResponseTransformer(Connector connector, String connectorName, String script, String template, DebugOptions debugOptions) throws JavaScriptInitializationException {
            super(connector, connectorName, script, template, debugOptions);
        }
        
        protected void compileAndAddScript(MirthContextFactory contextFactory) throws Exception {
            countCompileAndAddScript++;
        }
        
        protected MirthMain getDebugger(MirthContextFactory contextFactory) {
            countGetDebugger++;
            return null;         
        }
   
        protected String execute(MirthContextFactory contextFactory, Response response, ConnectorMessage connectorMessage) {
            return null;
        }
        
    }
    
}
