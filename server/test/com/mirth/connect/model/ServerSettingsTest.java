package com.mirth.connect.model;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;

public class ServerSettingsTest {

    public static ServerSettings serverSettings = new ServerSettings();
    
    @BeforeClass
    public static void setup() throws Exception {
        ControllerFactory controllerFactory = mock(ControllerFactory.class);

        ScriptController scriptController = mock(ScriptController.class);
        when(controllerFactory.createScriptController()).thenReturn(scriptController);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        injector.getInstance(ControllerFactory.class);
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
