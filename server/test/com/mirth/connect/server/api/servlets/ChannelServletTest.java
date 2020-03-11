package com.mirth.connect.server.api.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelExportData;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.server.api.ServletTestBase;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.CodeTemplateController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;


public class ChannelServletTest extends ServletTestBase {

    private static final String CHANNEL_ID_1 = "channelId1";
    private static final String CHANNEL_ID_2 = "channelId2";
    private static final String CHANNEL_ID_3 = "channelId3";
    private static final String NONEXISTENT_CHANNEL_ID = "nonexistentChannelId";
    
    private ChannelServlet channelServlet;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ServletTestBase.setup();
        
        ChannelController channelController = mock(ChannelController.class);
        when(controllerFactory.createChannelController()).thenReturn(channelController);
        
        Channel channel1 = createChannel(CHANNEL_ID_1);
        Channel channel2 = createChannel(CHANNEL_ID_2);
        Channel channel3 = createChannel(CHANNEL_ID_3);
        when(channelController.getChannels(isNull())).thenReturn(Arrays.asList(new Channel[] { channel1, channel2, channel3 }));
        when(channelController.getChannels(new HashSet<String>(Arrays.asList(new String[] { NONEXISTENT_CHANNEL_ID })))).thenReturn(null);
        when(channelController.getChannelById(CHANNEL_ID_1)).thenReturn(channel1);

        ConfigurationController configurationController = mock(ConfigurationController.class);
        when(controllerFactory.createConfigurationController()).thenReturn(configurationController);

        Map<String, ChannelMetadata> metadataMap = new HashMap<>();
        ChannelMetadata metadata = new ChannelMetadata();
        metadata.setEnabled(true);
        metadata.getPruningSettings().setArchiveEnabled(true);
        metadata.getPruningSettings().setPruneContentDays(7);
        metadataMap.put(CHANNEL_ID_1, metadata);
        when(configurationController.getChannelMetadata()).thenReturn(metadataMap);

        Set<ChannelTag> tags = new HashSet<>();
        tags.add(new ChannelTag("tag1", "Tag 1", new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_1, CHANNEL_ID_2 }))));
        tags.add(new ChannelTag("tag2", "Tag 2", new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_1, CHANNEL_ID_3 }))));
        tags.add(new ChannelTag("tag3", "Tag 3", new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_2, CHANNEL_ID_3 }))));
        when(configurationController.getChannelTags()).thenReturn(tags);

        Set<ChannelDependency> dependencies = new HashSet<>();
        dependencies.add(new ChannelDependency(CHANNEL_ID_1, CHANNEL_ID_2));
        dependencies.add(new ChannelDependency(CHANNEL_ID_3, CHANNEL_ID_1));
        when(configurationController.getChannelDependencies()).thenReturn(dependencies);

        CodeTemplateController codeTemplateController = mock(CodeTemplateController.class);
        when(controllerFactory.createCodeTemplateController()).thenReturn(codeTemplateController);

        List<CodeTemplateLibrary> codeTemplateLibraries = new ArrayList<>();
        CodeTemplateLibrary library = new CodeTemplateLibrary();
        library.setId("libraryId1");
        library.setName("Library 1");
        library.setEnabledChannelIds(new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_1, CHANNEL_ID_2 })));
        library.setDisabledChannelIds(new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_3 })));
        codeTemplateLibraries.add(library);

        library = new CodeTemplateLibrary();
        library.setId("libraryId2");
        library.setName("Library 2");
        library.setEnabledChannelIds(new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_2 })));
        library.setDisabledChannelIds(new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_1, CHANNEL_ID_3 })));
        codeTemplateLibraries.add(library);

        when(codeTemplateController.getLibraries(isNull(), anyBoolean())).thenReturn(codeTemplateLibraries);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                requestStaticInjection(ControllerFactory.class);
                bind(ControllerFactory.class).toInstance(controllerFactory);
            }
        });
        injector.getInstance(ControllerFactory.class);
    }
    
    private static Channel createChannel(String id) {
        Channel channel = new Channel();
        channel.setId(id);
        channel.setName("Channel " + id);
        channel.setSourceConnector(new Connector());
        return channel;
    }
    
    @Before
    public void beforeTest() {
        channelServlet = new TestChannelServlet(request, mock(SecurityContext.class));
    }

    @Test
    public void testAddExportData() throws Exception {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID_1);

        channelServlet.addExportData(channel, true);
        verifyChannel1(channel, true);
    }
    
    @Test
    public void testAddExportDataWithoutCodeTemplateLibraries() throws Exception {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID_1);

        // Testing a case where the code template libraries would incorrectly be returned if they had previously been added to the export data of the channel
        channelServlet.addExportData(channel, true);
        channelServlet.addExportData(channel, false);
        ChannelExportData exportData = channel.getExportData();
        
        assertTrue(exportData.getCodeTemplateLibraries().isEmpty());
    }
    
    @Test
    public void testGetChannels() throws Exception {
        verifyChannels(channelServlet.getChannels(null, false, true), true);
        verifyChannels(channelServlet.getChannels(null, false, false), false);
    }

    @Test
    public void testGetChannelsPost() throws Exception {
        verifyChannels(channelServlet.getChannelsPost(null, false, true), true);
        verifyChannels(channelServlet.getChannelsPost(null, false, false), false);
    }
    
    @Test
    public void testGetChannelsIsNull() throws Exception {
        List<Channel> channels = channelServlet.getChannels(new HashSet<String>(Arrays.asList(new String[] { NONEXISTENT_CHANNEL_ID })), false, true);
        assertNull(channels);
    }

    @Test
    public void testGetChannel() throws Exception {
        Channel channel = channelServlet.getChannel(CHANNEL_ID_1, true);
        verifyChannel1(channel, true);
        channel = channelServlet.getChannel(CHANNEL_ID_1, false);
        verifyChannel1(channel, false);
    }
    
    @Test
    public void testGetChannelIsNull() throws Exception {
        Channel channel = channelServlet.getChannel(NONEXISTENT_CHANNEL_ID, true);
        assertNull(channel);
    }

    private void verifyChannels(List<Channel> channels, boolean includeCodeTemplateLibraries) {
        assertEquals(3, channels.size());

        for (Channel channel : channels) {
            if (CHANNEL_ID_1.equals(channel.getId())) {
                verifyChannel1(channel, includeCodeTemplateLibraries);
            } 
        }
    }

    private void verifyChannel1(Channel channel, boolean includeCodeTemplateLibraries) {
        ChannelExportData exportData = channel.getExportData();
        assertTrue(exportData.getMetadata().isEnabled());
        assertTrue(exportData.getMetadata().getPruningSettings().isArchiveEnabled());
        assertEquals(new Integer(7), exportData.getMetadata().getPruningSettings().getPruneContentDays());

        assertEquals(2, exportData.getChannelTags().size());
        assertTrue(exportData.getChannelTags().contains(new ChannelTag("tag1", "Tag 1", new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_1, CHANNEL_ID_2 })))));
        assertTrue(exportData.getChannelTags().contains(new ChannelTag("tag2", "Tag 2", new HashSet<>(Arrays.asList(new String[] { CHANNEL_ID_1, CHANNEL_ID_3 })))));

        assertEquals(1, exportData.getDependencyIds().size());
        assertTrue(exportData.getDependencyIds().contains(CHANNEL_ID_2));
        assertEquals(1, exportData.getDependentIds().size());
        assertTrue(exportData.getDependentIds().contains(CHANNEL_ID_3));

        if (includeCodeTemplateLibraries) {
            assertNotNull(exportData.getCodeTemplateLibraries());
            assertEquals(1, exportData.getCodeTemplateLibraries().size());
            CodeTemplateLibrary library = exportData.getCodeTemplateLibraries().get(0);
            assertEquals("libraryId1", library.getId());
            assertEquals("Library 1", library.getName());
            assertEquals(2, library.getEnabledChannelIds().size());
            assertTrue(library.getEnabledChannelIds().contains(CHANNEL_ID_1));
            assertEquals(1, library.getDisabledChannelIds().size());
        } else {
            assertTrue(exportData.getCodeTemplateLibraries().isEmpty());
        }
    }
    
    public class TestChannelServlet extends ChannelServlet {
        public TestChannelServlet(HttpServletRequest request, SecurityContext sc) {
            super(request, sc);
        }

        @Override
        protected boolean isUserAuthorized() {
            return true;
        }
    }

}
