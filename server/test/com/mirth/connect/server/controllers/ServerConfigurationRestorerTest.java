/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.util.MultiException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.Version;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.model.ResourceProperties;
import com.mirth.connect.model.ResourcePropertiesList;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.DefaultTrigger;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.MergePropertiesInterface;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.util.ConfigurationProperty;

public class ServerConfigurationRestorerTest {

    private static ServerConfiguration config1;

    @BeforeClass
    public static void setup() throws Exception {
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        serializer.init(Version.getLatest().toString());

        config1 = serializer.deserialize(IOUtils.toString(ServerConfigurationRestorerTest.class.getResourceAsStream("Config 1.xml")), ServerConfiguration.class);
    }

    @Test
    public void testRestoreServerConfiguration() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        // Plain config, success
        ServerConfiguration config = config1;
        boolean deploy = false;
        boolean overwriteConfigMap = false;
        restorer.restoreServerConfiguration(config, deploy, overwriteConfigMap);

        verify(restorer, times(1)).restoreChannelGroups(eq(config), any());
        verify(restorer, times(1)).restoreChannels(eq(config), any());
        verify(restorer, times(1)).restoreAlerts(eq(config), any());
        verify(restorer, times(1)).restoreCodeTemplateLibraries(eq(config), any());
        verify(restorer, times(1)).restoreConfigurationMap(eq(config), eq(overwriteConfigMap), any());
        verify(restorer, times(1)).restoreServerSettings(eq(config), any());
        verify(restorer, times(1)).restoreUpdateSettings(eq(config), any());
        verify(restorer, times(1)).restorePluginProperties(eq(config), any());
        verify(restorer, times(1)).restoreResourceProperties(eq(config), any());
        verify(restorer, times(1)).restoreChannelDependencies(eq(config), any());
        verify(restorer, times(1)).restoreChannelTags(eq(config), any());
        verify(restorer, times(1)).restoreGlobalScripts(eq(config), any());
        verify(restorer, times(1)).deployAllChannels(eq(deploy), any());

        // Restore channels failed
        restorer = createRestorer();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                MultiException e = ((MultiException) invocation.getArgument(1));
                e.add(new ControllerException("testing"));
                return null;
            }
        }).when(restorer).restoreChannels(any(), any());

        try {
            restorer.restoreServerConfiguration(config, deploy, overwriteConfigMap);
            fail("Exception should have been thrown");
        } catch (ControllerException e) {
            MultiException multi = (MultiException) e.getCause();
            assertEquals(1, multi.size());
        }

        verify(restorer, times(1)).restoreChannelGroups(eq(config), any());
        verify(restorer, times(1)).restoreChannels(eq(config), any());
        verify(restorer, times(1)).restoreAlerts(eq(config), any());
        verify(restorer, times(1)).restoreCodeTemplateLibraries(eq(config), any());
        verify(restorer, times(1)).restoreConfigurationMap(eq(config), eq(overwriteConfigMap), any());
        verify(restorer, times(1)).restoreServerSettings(eq(config), any());
        verify(restorer, times(1)).restoreUpdateSettings(eq(config), any());
        verify(restorer, times(1)).restorePluginProperties(eq(config), any());
        verify(restorer, times(1)).restoreResourceProperties(eq(config), any());
        verify(restorer, times(1)).restoreChannelDependencies(eq(config), any());
        verify(restorer, times(1)).restoreChannelTags(eq(config), any());
        verify(restorer, times(1)).restoreGlobalScripts(eq(config), any());
        verify(restorer, times(1)).deployAllChannels(eq(deploy), any());

        // Test synchronization
        ConfigurationController configurationController = mock(ConfigurationController.class);
        ChannelController channelController = mock(ChannelController.class);
        AlertController alertController = mock(AlertController.class);
        CodeTemplateController codeTemplateController = mock(CodeTemplateController.class);
        final EngineController engineController = mock(EngineController.class);
        ScriptController scriptController = mock(ScriptController.class);
        ExtensionController extensionController = mock(ExtensionController.class);
        ContextFactoryController contextFactoryController = mock(ContextFactoryController.class);

        final ServerConfigurationRestorer restorer2 = spy(new ServerConfigurationRestorer(configurationController, channelController, alertController, codeTemplateController, engineController, scriptController, extensionController, contextFactoryController));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(2000);
                return null;
            }
        }).when(restorer2).deployAllChannels(anyBoolean(), any());

        Thread restoreThread = new Thread() {
            @Override
            public void run() {
                try {
                    restorer2.restoreServerConfiguration(config, deploy, overwriteConfigMap);
                } catch (ControllerException e) {
                }
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                synchronized (engineController) {
                    System.out.print("Entered synchronization block");
                }
            }
        };

        restoreThread.start();
        Thread.sleep(100);
        thread.start();
        thread.join(1000);
        assertTrue(thread.isAlive());
        thread.join();
        restoreThread.join();

        doThrow(RuntimeException.class).when(restorer).deployAllChannels(anyBoolean(), any());
        try {
            restorer.restoreServerConfiguration(config, deploy, overwriteConfigMap);
            fail("Exception should have been thrown");
        } catch (RuntimeException e) {
        }
    }

    @Test
    public void testRestoreChannelGroups() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();

        restorer.restoreChannelGroups(config, multiException);
        verify(restorer.getChannelController(), times(1)).updateChannelGroups(new HashSet<ChannelGroup>(), new HashSet<String>(), true);

        ChannelGroup group1 = new ChannelGroup("1", "1", "");
        ChannelGroup group2 = new ChannelGroup("2", "2", "");
        List<ChannelGroup> groups = new ArrayList<ChannelGroup>();
        groups.add(group1);
        groups.add(group1);
        groups.add(group2);
        config.setChannelGroups(groups);

        restorer.restoreChannelGroups(config, multiException);

        verify(restorer.getChannelController(), times(1)).updateChannelGroups(argThat(new ArgumentMatcher<Set<ChannelGroup>>() {
            @Override
            public boolean matches(Set<ChannelGroup> channelGroups) {
                return channelGroups.size() == 2;
            }
        }), eq(new HashSet<String>()), eq(true));

        ChannelController channelController = restorer.getChannelController();
        doThrow(ControllerException.class).when(channelController).updateChannelGroups(any(), any(), anyBoolean());
        restorer.restoreChannelGroups(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreChannels() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();
        restorer.restoreChannels(config, multiException);

        verify(restorer, times(0)).undeployChannels(any());
        verify(restorer, times(0)).removeChannels(any(), any());
        verify(restorer, times(0)).updateChannels(any(), any());

        config = config1;
        multiException = new MultiException();
        restorer.restoreChannels(config, multiException);

        verify(restorer, times(1)).undeployChannels(any());
        verify(restorer, times(1)).removeChannels(eq(config), any());
        verify(restorer, times(1)).updateChannels(eq(config), any());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MultiException) invocation.getArgument(1)).add(new ControllerException("test"));
                return null;
            }
        }).when(restorer).updateChannels(any(), any());

        restorer.restoreChannels(config, multiException);

        assertEquals(1, multiException.size());
    }

    @Test
    public void testUndeployChannels() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        Set<String> channelIds = new HashSet<String>();
        channelIds.add("1");
        channelIds.add("2");
        when(restorer.getEngineController().getDeployedIds()).thenReturn(channelIds);

        restorer.undeployChannels(new MultiException());

        verify(restorer.getEngineController(), times(1)).undeployChannels(channelIds, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);

        EngineController engineController = restorer.getEngineController();
        doThrow(ControllerException.class).when(engineController).undeployChannels(any(), any(), any());
        MultiException multiException = new MultiException();
        restorer.undeployChannels(multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRemoveChannels() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        Channel channel = new Channel("1");
        List<Channel> channels = new ArrayList<Channel>();
        channels.add(channel);
        channels.add(config1.getChannels().get(0));
        when(restorer.getChannelController().getChannels(null)).thenReturn(channels);

        restorer.removeChannels(config1, new MultiException());

        verify(restorer, times(1)).removeChannel(eq(channel), any());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MultiException) invocation.getArgument(1)).add(new ControllerException("test"));
                return null;
            }
        }).when(restorer).removeChannel(any(), any());

        MultiException multiException = new MultiException();
        restorer.removeChannels(config1, multiException);

        assertEquals(1, multiException.size());
    }

    @Test
    public void testRemoveChannel() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        Channel channel = new Channel("1");
        restorer.removeChannel(channel, new MultiException());

        verify(restorer.getChannelController(), times(1)).removeChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);

        ChannelController channelController = restorer.getChannelController();
        doThrow(ControllerException.class).when(channelController).removeChannel(any(), any());
        MultiException multiException = new MultiException();
        restorer.removeChannel(channel, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testUpdateChannels() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        restorer.updateChannels(config1, new MultiException());

        for (Channel channel : config1.getChannels()) {
            verify(restorer, times(1)).updateChannel(eq(channel), any());
        }

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MultiException) invocation.getArgument(1)).add(new ControllerException("test"));
                return null;
            }
        }).when(restorer).updateChannel(any(), any());

        MultiException multiException = new MultiException();
        restorer.updateChannels(config1, multiException);

        assertEquals(1, multiException.size());
    }

    @Test
    public void testUpdateChannel() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        Channel channel = new Channel("1");
        restorer.updateChannel(channel, new MultiException());

        verify(restorer.getChannelController(), times(1)).updateChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);

        ChannelController channelController = restorer.getChannelController();
        doThrow(ControllerException.class).when(channelController).updateChannel(any(), any(), anyBoolean());
        MultiException multiException = new MultiException();
        restorer.updateChannel(channel, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreAlerts() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        restorer.restoreAlerts(config, new MultiException());

        verify(restorer, times(0)).removeExistingAlerts(any());
        verify(restorer, times(0)).updateNewAlerts(eq(config), any());

        restorer = createRestorer();
        config = config1;
        restorer.restoreAlerts(config, new MultiException());

        verify(restorer, times(1)).removeExistingAlerts(any());
        verify(restorer, times(1)).updateNewAlerts(eq(config), any());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MultiException) invocation.getArgument(1)).add(new ControllerException("test"));
                return null;
            }
        }).when(restorer).updateNewAlerts(any(), any());

        MultiException multiException = new MultiException();
        restorer.restoreAlerts(config1, multiException);

        assertEquals(1, multiException.size());
    }

    @Test
    public void testRemoveExistingAlerts() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        List<AlertModel> alerts = new ArrayList<AlertModel>();
        AlertModel alert1 = new AlertModel(new DefaultTrigger(), new AlertActionGroup());
        alert1.setId("1");
        alerts.add(alert1);
        AlertModel alert2 = new AlertModel(new DefaultTrigger(), new AlertActionGroup());
        alert2.setId("2");
        alerts.add(alert2);

        when(restorer.getAlertController().getAlerts()).thenReturn(alerts);

        restorer.removeExistingAlerts(new MultiException());

        for (AlertModel alert : alerts) {
            verify(restorer, times(1)).removeExistingAlert(eq(alert), any());
        }

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MultiException) invocation.getArgument(1)).add(new ControllerException("test"));
                return null;
            }
        }).when(restorer).removeExistingAlert(any(), any());

        MultiException multiException = new MultiException();
        restorer.removeExistingAlerts(multiException);

        assertEquals(1, multiException.size());
    }

    @Test
    public void testRemoveExistingAlert() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        AlertModel alert = new AlertModel(new DefaultTrigger(), new AlertActionGroup());
        alert.setId("1");

        restorer.removeExistingAlert(alert, new MultiException());

        verify(restorer.getAlertController(), times(1)).removeAlert(anyString());

        AlertController alertController = restorer.getAlertController();
        doThrow(new ControllerException("test")).when(alertController).removeAlert(alert.getId());
        MultiException multiException = new MultiException();
        restorer.removeExistingAlert(alert, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testUpdateNewAlerts() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = config1;
        restorer.updateNewAlerts(config, new MultiException());

        for (AlertModel alert : config.getAlerts()) {
            verify(restorer, times(1)).updateNewAlert(eq(alert), any());
        }

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MultiException) invocation.getArgument(1)).add(new ControllerException("test"));
                return null;
            }
        }).when(restorer).updateNewAlert(any(), any());

        MultiException multiException = new MultiException();
        restorer.updateNewAlerts(config, multiException);

        assertEquals(1, multiException.size());
    }

    @Test
    public void testUpdateNewAlert() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        AlertModel alert = new AlertModel(new DefaultTrigger(), new AlertActionGroup());
        alert.setId("1");

        restorer.updateNewAlert(alert, new MultiException());

        verify(restorer.getAlertController(), times(1)).updateAlert(alert);

        AlertController alertController = restorer.getAlertController();
        doThrow(ControllerException.class).when(alertController).updateAlert(alert);
        MultiException multiException = new MultiException();
        restorer.updateNewAlert(alert, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreCodeTemplateLibraries() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();
        restorer.restoreCodeTemplateLibraries(config, multiException);

        verify(restorer, times(0)).updateCodeTemplateLibraries(any(), any());
        verify(restorer, times(0)).removeCodeTemplates(any(), any());
        verify(restorer, times(0)).updateNewCodeTemplates(any(), any());

        config = config1;
        restorer.restoreCodeTemplateLibraries(config, multiException);

        verify(restorer, times(1)).updateCodeTemplateLibraries(eq(config), any());
        verify(restorer, times(1)).removeCodeTemplates(eq(config), any());
        verify(restorer, times(1)).updateNewCodeTemplates(eq(config), any());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MultiException) invocation.getArgument(1)).add(new ControllerException("test"));
                return null;
            }
        }).when(restorer).updateNewCodeTemplates(any(), any());

        restorer.restoreCodeTemplateLibraries(config, multiException);

        assertEquals(1, multiException.size());
    }

    @Test
    public void testUpdateCodeTemplateLibraries() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();
        restorer.updateCodeTemplateLibraries(config, multiException);

        verify(restorer.getCodeTemplateController(), times(0)).updateLibraries(any(), any(), anyBoolean());

        config = config1;
        restorer.updateCodeTemplateLibraries(config, multiException);
        verify(restorer.getCodeTemplateController(), times(1)).updateLibraries(eq(config.getCodeTemplateLibraries()), eq(ServerEventContext.SYSTEM_USER_EVENT_CONTEXT), eq(true));

        CodeTemplateController codeTemplateController = restorer.getCodeTemplateController();
        doThrow(ControllerException.class).when(codeTemplateController).updateLibraries(any(), any(), anyBoolean());
        restorer.updateCodeTemplateLibraries(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRemoveCodeTemplates() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        config.setCodeTemplateLibraries(new ArrayList<CodeTemplateLibrary>(config1.getCodeTemplateLibraries()));
        CodeTemplateLibrary library = new CodeTemplateLibrary();
        library.setCodeTemplates(null);
        config.getCodeTemplateLibraries().add(library);

        CodeTemplate codeTemplate = new CodeTemplate("1");
        List<CodeTemplate> codeTemplates = new ArrayList<CodeTemplate>();
        codeTemplates.add(config.getCodeTemplateLibraries().get(0).getCodeTemplates().get(0));
        codeTemplates.add(codeTemplate);
        when(restorer.getCodeTemplateController().getCodeTemplates(null)).thenReturn(codeTemplates);

        MultiException multiException = new MultiException();
        restorer.removeCodeTemplates(config, multiException);

        verify(restorer, times(1)).removeCodeTemplate(eq(codeTemplate), any());

        CodeTemplateController codeTemplateController = restorer.getCodeTemplateController();
        doThrow(ControllerException.class).when(codeTemplateController).getCodeTemplates(any());
        restorer.removeCodeTemplates(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRemoveCodeTemplate() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        CodeTemplate codeTemplate = new CodeTemplate("1");
        MultiException multiException = new MultiException();

        restorer.removeCodeTemplate(codeTemplate, multiException);

        verify(restorer.getCodeTemplateController(), times(1)).removeCodeTemplate(codeTemplate.getId(), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);

        CodeTemplateController codeTemplateController = restorer.getCodeTemplateController();
        doThrow(ControllerException.class).when(codeTemplateController).removeCodeTemplate(anyString(), any());
        restorer.removeCodeTemplate(codeTemplate, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testUpdateNewCodeTemplates() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        config.setCodeTemplateLibraries(new ArrayList<CodeTemplateLibrary>(config1.getCodeTemplateLibraries()));
        CodeTemplateLibrary library = new CodeTemplateLibrary();
        library.setCodeTemplates(null);
        config.getCodeTemplateLibraries().add(library);

        MultiException multiException = new MultiException();
        restorer.updateNewCodeTemplates(config, multiException);

        for (CodeTemplateLibrary codeTemplateLibrary : config.getCodeTemplateLibraries()) {
            if (codeTemplateLibrary.getCodeTemplates() != null) {
                for (CodeTemplate codeTemplate : codeTemplateLibrary.getCodeTemplates()) {
                    verify(restorer, times(1)).updateNewCodeTemplate(eq(codeTemplate), any());
                }
            }
        }

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MultiException) invocation.getArgument(1)).add(new ControllerException("test"));
                return null;
            }
        }).when(restorer).updateNewCodeTemplate(any(), any());
        restorer.updateNewCodeTemplates(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testUpdateNewCodeTemplate() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        CodeTemplate codeTemplate = new CodeTemplate("1");
        MultiException multiException = new MultiException();

        restorer.updateNewCodeTemplate(codeTemplate, multiException);

        verify(restorer.getCodeTemplateController(), times(1)).updateCodeTemplate(codeTemplate, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);

        CodeTemplateController codeTemplateController = restorer.getCodeTemplateController();
        doThrow(ControllerException.class).when(codeTemplateController).updateCodeTemplate(any(), any(), anyBoolean());
        restorer.updateNewCodeTemplate(codeTemplate, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreConfigurationMap() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = config1;
        MultiException multiException = new MultiException();

        restorer.restoreConfigurationMap(config, false, multiException);
        verify(restorer.getConfigurationController(), times(0)).setConfigurationProperties(any(), anyBoolean());

        restorer.restoreConfigurationMap(config, true, multiException);
        verify(restorer.getConfigurationController(), times(1)).setConfigurationProperties(config.getConfigurationMap(), true);

        config = new ServerConfiguration();
        restorer.restoreConfigurationMap(config, true, multiException);
        verify(restorer.getConfigurationController(), times(1)).setConfigurationProperties(new HashMap<String, ConfigurationProperty>(), true);

        ConfigurationController configurationController = restorer.getConfigurationController();
        doThrow(ControllerException.class).when(configurationController).setConfigurationProperties(any(), anyBoolean());
        restorer.restoreConfigurationMap(config, true, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreServerSettings() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();

        restorer.restoreServerSettings(config, multiException);
        verify(restorer.getConfigurationController(), times(0)).setServerSettings(any());

        config = config1;
        restorer.restoreServerSettings(config, multiException);
        verify(restorer.getConfigurationController(), times(1)).setServerSettings(config.getServerSettings());
        assertNull(config.getServerSettings().getServerName());

        ConfigurationController configurationController = restorer.getConfigurationController();
        doThrow(ControllerException.class).when(configurationController).setServerSettings(any());
        restorer.restoreServerSettings(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreUpdateSettings() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();

        restorer.restoreUpdateSettings(config, multiException);
        verify(restorer.getConfigurationController(), times(0)).setUpdateSettings(any());

        config = config1;
        restorer.restoreUpdateSettings(config, multiException);
        verify(restorer.getConfigurationController(), times(1)).setUpdateSettings(config.getUpdateSettings());

        ConfigurationController configurationController = restorer.getConfigurationController();
        doThrow(ControllerException.class).when(configurationController).setUpdateSettings(any());
        restorer.restoreUpdateSettings(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestorePluginProperties1() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();

        restorer.restorePluginProperties(config, multiException);
        verify(restorer, times(0)).restorePluginProperties(anyString(), any(), any());

        config = config1;
        restorer.restorePluginProperties(config, multiException);
        for (Entry<String, Properties> pluginEntry : config.getPluginProperties().entrySet()) {
            verify(restorer, times(1)).restorePluginProperties(eq(pluginEntry.getKey()), eq(pluginEntry.getValue()), any());
        }
        assertEquals(0, multiException.size());

        doThrow(ControllerException.class).when(restorer).restorePluginProperties(anyString(), any(), any());
        restorer.restorePluginProperties(config, multiException);
        assertEquals(config.getPluginProperties().size(), multiException.size());
    }

    @Test
    public void testRestorePluginProperties2() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        String pluginName = "test";
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        MultiException multiException = new MultiException();

        restorer.restorePluginProperties(pluginName, properties, multiException);
        verify(restorer.getExtensionController(), times(1)).setPluginProperties(pluginName, properties);

        ServicePlugin servicePlugin = mock(ServicePlugin.class);
        Map<String, ServicePlugin> servicePlugins = new HashMap<String, ServicePlugin>();
        servicePlugins.put(pluginName, servicePlugin);
        when(restorer.getExtensionController().getServicePlugins()).thenReturn(servicePlugins);
        reset(restorer.getExtensionController());
        restorer.restorePluginProperties(pluginName, properties, multiException);
        verify(restorer.getExtensionController(), times(1)).setPluginProperties(pluginName, properties);

        TestServicePlugin testServicePlugin = mock(TestServicePlugin.class);
        servicePlugins.put(pluginName, testServicePlugin);
        reset(restorer.getExtensionController());
        when(restorer.getExtensionController().getServicePlugins()).thenReturn(servicePlugins);
        restorer.restorePluginProperties(pluginName, properties, multiException);
        verify(restorer.getExtensionController(), times(1)).setPluginProperties(pluginName, properties);
        verify(testServicePlugin, times(1)).modifyPropertiesOnRestore(properties);

        ExtensionController extensionController = restorer.getExtensionController();
        doThrow(ControllerException.class).when(extensionController).setPluginProperties(anyString(), any());
        restorer.restorePluginProperties(pluginName, properties, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreResourceProperties() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();

        restorer.restoreResourceProperties(config, multiException);
        verify(restorer.getConfigurationController(), times(0)).setResources(anyString());
        verify(restorer.getContextFactoryController(), times(0)).updateResources(any(), anyBoolean());

        config.setResourceProperties(new ResourcePropertiesList());
        config.getResourceProperties().getList().addAll(config1.getResourceProperties().getList());
        TestResourceProperties resourceProperties = new TestResourceProperties();
        config.getResourceProperties().getList().add(resourceProperties);

        restorer.restoreResourceProperties(config, multiException);
        verify(restorer.getConfigurationController(), times(1)).setResources(ObjectXMLSerializer.getInstance().serialize(config.getResourceProperties()));
        List<LibraryProperties> libraryResources = new ArrayList<LibraryProperties>();
        libraryResources.add((LibraryProperties) config1.getResourceProperties().getList().get(0));
        libraryResources.add((LibraryProperties) config1.getResourceProperties().getList().get(1));
        verify(restorer.getContextFactoryController(), times(1)).updateResources(libraryResources, false);

        ContextFactoryController contextFactoryController = restorer.getContextFactoryController();
        doThrow(ControllerException.class).when(contextFactoryController).updateResources(any(), anyBoolean());
        restorer.restoreResourceProperties(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreChannelDependencies() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();

        restorer.restoreChannelDependencies(config, multiException);
        verify(restorer.getConfigurationController(), times(1)).setChannelDependencies(new HashSet<ChannelDependency>());

        config = config1;
        restorer.restoreChannelDependencies(config, multiException);
        verify(restorer.getConfigurationController(), times(1)).setChannelDependencies(config.getChannelDependencies());

        ConfigurationController configurationController = restorer.getConfigurationController();
        doThrow(ControllerException.class).when(configurationController).setChannelDependencies(any());
        restorer.restoreChannelDependencies(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreChannelTags() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();

        restorer.restoreChannelTags(config, multiException);
        verify(restorer.getConfigurationController(), times(1)).setChannelTags(new HashSet<ChannelTag>());

        config = config1;
        restorer.restoreChannelTags(config, multiException);
        verify(restorer.getConfigurationController(), times(1)).setChannelTags(config.getChannelTags());

        ConfigurationController configurationController = restorer.getConfigurationController();
        doThrow(ControllerException.class).when(configurationController).setChannelTags(any());
        restorer.restoreChannelTags(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testRestoreGlobalScripts() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        ServerConfiguration config = new ServerConfiguration();
        MultiException multiException = new MultiException();

        restorer.restoreGlobalScripts(config, multiException);
        verify(restorer.getScriptController(), times(0)).setGlobalScripts(any());

        config = config1;
        restorer.restoreGlobalScripts(config, multiException);
        verify(restorer.getScriptController(), times(1)).setGlobalScripts(config.getGlobalScripts());

        ScriptController scriptController = restorer.getScriptController();
        doThrow(ControllerException.class).when(scriptController).setGlobalScripts(any());
        restorer.restoreGlobalScripts(config, multiException);
        assertEquals(1, multiException.size());
    }

    @Test
    public void testDeployAllChannels() throws Exception {
        ServerConfigurationRestorer restorer = createRestorer();

        boolean deploy = false;
        MultiException multiException = new MultiException();

        restorer.deployAllChannels(deploy, multiException);
        verify(restorer.getEngineController(), times(0)).deployChannels(any(), any(), any());

        deploy = true;
        Set<String> channelIds = new HashSet<String>();
        channelIds.add("1");
        when(restorer.getChannelController().getChannelIds()).thenReturn(channelIds);
        restorer.deployAllChannels(deploy, multiException);
        verify(restorer.getEngineController(), times(1)).deployChannels(channelIds, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);

        EngineController engineController = restorer.getEngineController();
        doThrow(ControllerException.class).when(engineController).deployChannels(any(), any(), any());
        restorer.deployAllChannels(deploy, multiException);
        assertEquals(1, multiException.size());
    }

    private ServerConfigurationRestorer createRestorer() {
        ConfigurationController configurationController = mock(ConfigurationController.class);
        ChannelController channelController = mock(ChannelController.class);
        AlertController alertController = mock(AlertController.class);
        CodeTemplateController codeTemplateController = mock(CodeTemplateController.class);
        EngineController engineController = mock(EngineController.class);
        ScriptController scriptController = mock(ScriptController.class);
        ExtensionController extensionController = mock(ExtensionController.class);
        ContextFactoryController contextFactoryController = mock(ContextFactoryController.class);

        return spy(new ServerConfigurationRestorer(configurationController, channelController, alertController, codeTemplateController, engineController, scriptController, extensionController, contextFactoryController));
    }

    private class TestServicePlugin implements ServicePlugin, MergePropertiesInterface {

        @Override
        public String getPluginPointName() {
            return null;
        }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public void modifyPropertiesOnRestore(Properties properties) throws ControllerException {}

        @Override
        public void init(Properties properties) {}

        @Override
        public void update(Properties properties) {}

        @Override
        public Properties getDefaultProperties() {
            return null;
        }

        @Override
        public ExtensionPermission[] getExtensionPermissions() {
            return null;
        }
    }

    private class TestResourceProperties extends ResourceProperties {

        public TestResourceProperties() {
            super("test", "type");
        }
    }
}
