package com.mirth.connect.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

public class ServerSettingsTest {

    public static ServerSettings serverSettings = new ServerSettings();
    
    @BeforeClass
    public static void setup() throws Exception {
        serverSettings.setEnvironmentName("envName");
        serverSettings.setServerName("serverName");
        serverSettings.setDefaultMetaDataColumns(null);
        serverSettings.setQueueBufferSize(1000);
        
    }
    
    @Test
    public void envornmentNameTest() {
        assertEquals("envName", serverSettings.getEnvironmentName());

    }
    
    @Test
    public void serverNameTest() {
        assertEquals("serverName", serverSettings.getServerName());
    }

    @Test
    public void defaultMetaDataColumnsTest() {
        assertNull(serverSettings.getDefaultMetaDataColumns());
    }
    
    @Test
    public void queueBufferSizeTest() {
        assertTrue(1000 == serverSettings.getQueueBufferSize());
    }
    
}
