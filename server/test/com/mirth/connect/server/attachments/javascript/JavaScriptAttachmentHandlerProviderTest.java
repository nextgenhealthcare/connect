package com.mirth.connect.server.attachments.javascript;

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

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.model.Channel;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptAttachmentHandlerProviderTest {
    private static Logger logger = Logger.getLogger(JavaScriptAttachmentHandlerProviderTest.class);
    private DebugOptions debugOptions;
    private AttachmentHandlerProperties attachmentProperties;
    
    @Before
    public void setup() {
        debugOptions = new DebugOptions();
        debugOptions.setDestinationConnectorScripts(true);
        attachmentProperties = new AttachmentHandlerProperties(null, null);
    }
    
    @Test
    public void testDebug() throws Exception {
        // Deploy
        TestJavaScriptAttachmentHandleProvider dispatcher = new TestJavaScriptAttachmentHandleProvider();
        
        MirthMain debugger = dispatcher.getDebugger(mock(MirthContextFactory.class), null);
        ContextFactoryController contextFactoryController = dispatcher.getContextFactoryController();
        
        verify(contextFactoryController, times(1)).getDebugContextFactory(any(), any(),any());
        
        dispatcher.setProperties(null, null);
        
        verify(debugger, times(1)).dispose();
        verify(contextFactoryController, times(1)).removeDebugContextFactory(any(), any(), any());
    }
    
    private static class TestJavaScriptAttachmentHandleProvider extends JavaScriptAttachmentHandlerProvider {
        private static String TEST_CHANNEL_ID = "testChannelId";
        
        private MirthMain debugger = mock(MirthMain.class);
        private ContextFactoryController contextFactoryController;
        private ChannelController channelController;
        private com.mirth.connect.model.Channel testChannel;
        
        public TestJavaScriptAttachmentHandleProvider() {
            channelController = mock(ChannelController.class);
            testChannel = new Channel();
            testChannel.setId(TEST_CHANNEL_ID);
            when(channelController.getChannelById(anyString())).thenReturn(testChannel);
        }

        public ContextFactoryController getContextFactoryController() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
