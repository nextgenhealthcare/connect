package com.mirth.connect.model;


import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class ChannelPruningSettingsTest {
    
    ChannelPruningSettings channelPruningSettings = new ChannelPruningSettings();
    
    
    @Test
    public void testPruneErroredMessages() throws Exception {
        
        assertEquals(false,channelPruningSettings.isPruneErroredMessages());
        assertEquals(true,channelPruningSettings.isArchiveEnabled());

        channelPruningSettings.setPruneErroredMessages(true);
        channelPruningSettings.setArchiveEnabled(false);
        assertEquals(true,channelPruningSettings.isPruneErroredMessages());
        assertEquals(false,channelPruningSettings.isArchiveEnabled());
        
    }
    
    

}
