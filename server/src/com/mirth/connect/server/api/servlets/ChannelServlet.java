/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.ChannelServletInterface;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.DontCheckAuthorized;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.CodeTemplateController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelServlet extends MirthServlet implements ChannelServletInterface {

    private static final EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private static final ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private static final CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private Logger logger = Logger.getLogger(getClass());

    public ChannelServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public boolean createChannel(Channel channel) {
        if (isChannelRedacted(channel.getId())) {
            throw new MirthApiException(Status.FORBIDDEN);
        }

        try {
            return channelController.updateChannel(channel, context, false);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @DontCheckAuthorized
    public List<Channel> getChannels(Set<String> channelIds, boolean pollingOnly, boolean includeCodeTemplateLibraries) {
        if (CollectionUtils.isNotEmpty(channelIds)) {
            parameterMap.put("channelIds", channelIds);
        }
        if (!isUserAuthorized()) {
            return new ArrayList<Channel>();
        }

        List<Channel> channels;
        if (CollectionUtils.isEmpty(channelIds)) {
            channels = redactChannels(channelController.getChannels(null));
        } else {
            channels = channelController.getChannels(redactChannelIds(channelIds));
        }

        if (pollingOnly) {
            retainPollingChannels(channels);
        }
        
        if (channels == null) {
            return channels;
        } else {
            // Add export data to each channel
            Map<String, ChannelMetadata> channelMetadata = configurationController.getChannelMetadata();
            Set<ChannelTag> channelTags = configurationController.getChannelTags();
            Set<ChannelDependency> channelDependencies = configurationController.getChannelDependencies();
            List<CodeTemplateLibrary> codeTemplateLibraries = includeCodeTemplateLibraries ? getCodeTemplateLibraries() : null;
            
            List<Channel> clonedChannels = new ArrayList<>();
            
            for (Channel channel : channels) {
                // We clone the channel, so that we do not modify channels in the ChannelController's cache
                Channel clonedChannel = channel.clone();
                addExportData(clonedChannel, channelMetadata, channelTags, channelDependencies, codeTemplateLibraries);
                clonedChannels.add(clonedChannel);
            }
            
            return clonedChannels;
        }
    }

    @Override
    @DontCheckAuthorized
    public List<Channel> getChannelsPost(Set<String> channelIds, boolean pollingOnly, boolean includeCodeTemplateLibraries) {
        return getChannels(channelIds, pollingOnly, includeCodeTemplateLibraries);
    }

    @Override
    @DontCheckAuthorized
    public Channel getChannel(String channelId, boolean includeCodeTemplateLibraries) {
        parameterMap.put("channelId", channelId);
        if (!isUserAuthorized() || isChannelRedacted(channelId)) {
            return null;
        }
        
        Channel channel = channelController.getChannelById(channelId);
        if (channel == null) {
            return channel;
        } else {
            // We clone the channel, so that we do not modify channels in the ChannelController's cache
            Channel clonedChannel = channel.clone();
            addExportData(clonedChannel, includeCodeTemplateLibraries);
            return clonedChannel;
        }
    }

    @Override
    @DontCheckAuthorized
    public Map<Integer, String> getConnectorNames(String channelId) {
        parameterMap.put("channelId", channelId);
        if (!isUserAuthorized()) {
            return new LinkedHashMap<Integer, String>();
        }
        if (isChannelRedacted(channelId)) {
            return null;
        }
        return channelController.getConnectorNames(channelId);
    }

    @Override
    @DontCheckAuthorized
    public List<MetaDataColumn> getMetaDataColumns(String channelId) {
        parameterMap.put("channelId", channelId);
        if (!isUserAuthorized()) {
            return new ArrayList<MetaDataColumn>();
        }
        if (isChannelRedacted(channelId)) {
            return null;
        }
        return channelController.getMetaDataColumns(channelId);
    }

    @Override
    @DontCheckAuthorized
    public Map<String, String> getChannelIdsAndNames() throws ClientException {
        Map<String, String> channelIdsAndNames = new HashMap<String, String>();
        if (isUserAuthorized()) {
            for (Channel channel : redactChannels(channelController.getChannels(null))) {
                channelIdsAndNames.put(channel.getId(), channel.getName());
            }
        }
        return channelIdsAndNames;
    }

    @Override
    @DontCheckAuthorized
    public List<ChannelSummary> getChannelSummary(Map<String, ChannelHeader> cachedChannels, boolean ignoreNewChannels) {
        parameterMap.put("cachedChannels", cachedChannels);
        if (!isUserAuthorized()) {
            return new ArrayList<ChannelSummary>();
        }
        try {
            return redactChannelSummaries(channelController.getChannelSummary(cachedChannels, ignoreNewChannels));
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void setChannelEnabled(Set<String> channelIds, boolean enabled) {
        if (CollectionUtils.isEmpty(channelIds)) {
            channelIds = channelController.getChannelIds();
        }
        try {
            channelController.setChannelEnabled(redactChannelIds(channelIds), context, enabled);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void setChannelEnabled(String channelId, boolean enabled) {
        try {
            channelController.setChannelEnabled(Collections.singleton(channelId), context, enabled);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void setChannelInitialState(Set<String> channelIds, DeployedState initialState) {
        if (CollectionUtils.isEmpty(channelIds)) {
            channelIds = channelController.getChannelIds();
        }
        try {
            channelController.setChannelInitialState(redactChannelIds(channelIds), context, initialState);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void setChannelInitialState(String channelId, DeployedState initialState) {
        try {
            channelController.setChannelInitialState(Collections.singleton(channelId), context, initialState);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public boolean updateChannel(String channelId, Channel channel, boolean override) {
        try {
            return channelController.updateChannel(channel, context, override);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void removeChannel(String channelId) {
        engineController.removeChannels(redactChannelIds(Collections.singleton(channelId)), context, null);
    }

    @Override
    public void removeChannels(Set<String> channelIds) {
        engineController.removeChannels(redactChannelIds(channelIds), context, null);
    }

    @Override
    public void removeChannelsPost(Set<String> channelIds) {
        removeChannels(channelIds);
    }

    private void retainPollingChannels(List<Channel> channels) {
        for (Iterator<Channel> it = channels.iterator(); it.hasNext();) {
            if (!ArrayUtils.contains(it.next().getSourceConnector().getProperties().getClass().getInterfaces(), PollConnectorPropertiesInterface.class)) {
                it.remove();
            }
        }
    }
    
    protected void addExportData(Channel channel, boolean includeCodeTemplateLibraries) {
        if (channel != null) {
            List<CodeTemplateLibrary> codeTemplateLibraries = includeCodeTemplateLibraries ? getCodeTemplateLibraries() : null;
            addExportData(channel, configurationController.getChannelMetadata(), configurationController.getChannelTags(), configurationController.getChannelDependencies(), codeTemplateLibraries);
        }
    }
    
    protected void addExportData(Channel channel, Map<String, ChannelMetadata> channelMetadata, Set<ChannelTag> channelTags, Set<ChannelDependency> channelDependencies, List<CodeTemplateLibrary> codeTemplateLibraries) {
        if (channel != null) {
            channel.getExportData().setMetadata(channelMetadata.get(channel.getId()));
            channel.getExportData().setChannelTags(channelTags
                    .stream()
                    .filter(tag -> tag.getChannelIds().contains(channel.getId()))
                    .collect(Collectors.toList()));

            channel.getExportData().setDependencyIds(channelDependencies
                    .stream()
                    .filter(dependency -> channel.getId().equals(dependency.getDependentId()))
                    .map(dependency -> dependency.getDependencyId())
                    .collect(Collectors.toSet()));
            channel.getExportData().setDependentIds(channelDependencies
                    .stream()
                    .filter(dependency -> channel.getId().equals(dependency.getDependencyId()))
                    .map(dependency -> dependency.getDependentId())
                    .collect(Collectors.toSet()));
            
            if (codeTemplateLibraries != null) {
                channel.getExportData().setCodeTemplateLibraries(
                        codeTemplateLibraries
                        .stream()
                        .filter(library -> library.getEnabledChannelIds().contains(channel.getId()))
                        .collect(Collectors.toList()));
            } else {
                channel.getExportData().setCodeTemplateLibraries(null);
            }
        }
    }
    
    private List<CodeTemplateLibrary> getCodeTemplateLibraries() {
        List<CodeTemplateLibrary> codeTemplateLibraries = null;
        try {
            codeTemplateLibraries = codeTemplateController.getLibraries(null, true);
        } catch (ControllerException e) {
            logger.error("Failed to get code template libraries.", e);
            codeTemplateLibraries = null;
        }
        return codeTemplateLibraries;
    }
}