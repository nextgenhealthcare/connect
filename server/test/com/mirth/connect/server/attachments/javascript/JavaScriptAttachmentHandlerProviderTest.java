package com.mirth.connect.server.attachments.javascript;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class JavaScriptAttachmentHandlerProviderTest {
    private static Logger logger = LogManager.getLogger(JavaScriptAttachmentHandlerProviderTest.class);
    private DebugOptions debugOptions;
    private AttachmentHandlerProperties attachmentProperties;
    
    @Before
    public void setup() {
        debugOptions = new DebugOptions();
        debugOptions.setAttachmentBatchScripts(true);
        attachmentProperties = new AttachmentHandlerProperties(null, null);
        attachmentProperties.getProperties().put("javascript.script", "I am a string");
    }
    
    @Test
    public void testDebug() throws Exception {
        Channel testChannel;
        String TEST_CHANNEL_ID = "testChannelId";
        // Deploying debugger
        TestJavaScriptAttachmentHandlerProvider attachmentHandlerProvider = new TestJavaScriptAttachmentHandlerProvider();

        testChannel = new Channel();
        testChannel.setChannelId(TEST_CHANNEL_ID);
        testChannel.setDebugOptions(debugOptions);
        testChannel.setAttachmentHandlerProvider(attachmentHandlerProvider);
        ContextFactoryController contextFactoryController = attachmentHandlerProvider.getContextFactoryController();
        
        attachmentHandlerProvider.setProperties(testChannel, attachmentProperties);
        MirthMain debugger = attachmentHandlerProvider.getDebugger(mock(MirthContextFactory.class), null);
        
        verify(contextFactoryController, times(1)).getDebugContextFactory(any(), any(),any());
    }
    
    private static class TestJavaScriptAttachmentHandlerProvider extends JavaScriptAttachmentHandlerProvider {
        
        private MirthMain debugger = mock(MirthMain.class);
        private ContextFactoryController contextFactoryController;
        public TestJavaScriptAttachmentHandlerProvider() {
        }
        
        @Override
        public ContextFactoryController getContextFactoryController() {
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
        protected MirthMain getDebugger(MirthContextFactory contextFactory, Channel channel) {
            return debugger;
        }
        
        @Override
        protected void compileAndAddScript(Channel channel, MirthContextFactory contextFactory, String scriptId, String attachmentScript, Set<String> scriptOptions) throws Exception {
            // Do nothing
        }
    }
}
