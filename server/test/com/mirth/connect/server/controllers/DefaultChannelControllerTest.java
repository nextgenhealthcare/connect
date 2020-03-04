package com.mirth.connect.server.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;

import edu.emory.mathcs.backport.java.util.Arrays;

public class DefaultChannelControllerTest {

    private static final String CHANNEL_ID_1 = "channelId1";
    private static final String CHANNEL_ID_2 = "channelId2";
    private static final String CHANNEL_ID_3 = "channelId3";

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setupClass() throws Exception {
        ControllerFactory controllerFactory = mock(ControllerFactory.class);

        ExtensionController extensionController = mock(ExtensionController.class);
        when(controllerFactory.createExtensionController()).thenReturn(extensionController);

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

    @SuppressWarnings("unchecked")
    @Test
    public void testAddExportData() throws Exception {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID_1);

        DefaultChannelController controller = new DefaultChannelController();
        controller.addExportData(channel);

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

        assertEquals(1, exportData.getCodeTemplateLibraries().size());
        CodeTemplateLibrary library = exportData.getCodeTemplateLibraries().get(0);
        assertEquals("libraryId1", library.getId());
        assertEquals("Library 1", library.getName());
        assertEquals(2, library.getEnabledChannelIds().size());
        assertTrue(library.getEnabledChannelIds().contains(CHANNEL_ID_1));
        assertEquals(1, library.getDisabledChannelIds().size());
    }

}
