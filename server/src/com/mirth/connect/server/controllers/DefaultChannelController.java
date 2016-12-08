/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelDependency;
import com.mirth.connect.model.ChannelExportData;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelMetadata;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.InvalidChannel;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultChannelController extends ChannelController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
    private CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();

    private ChannelCache channelCache = new ChannelCache();
    private DeployedChannelCache deployedChannelCache = new DeployedChannelCache();
    private Cache<ChannelGroup> channelGroupCache = new Cache<ChannelGroup>("Channel Group", "Channel.getChannelGroupRevision", "Channel.getChannelGroup");
    private Donkey donkey;

    private static ChannelController instance = null;

    protected DefaultChannelController() {

    }

    public static ChannelController create() {
        synchronized (DefaultChannelController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(ChannelController.class);

                if (instance == null) {
                    instance = new DefaultChannelController();
                }
            }

            return instance;
        }
    }

    @Override
    public List<Channel> getChannels(Set<String> channelIds) {
        Map<String, Channel> channelMap = channelCache.getAllItems();

        List<Channel> channels = new ArrayList<Channel>();

        if (channelIds == null) {
            channels.addAll(channelMap.values());
        } else {
            for (String channelId : channelIds) {
                if (channelMap.containsKey(channelId)) {
                    channels.add(channelMap.get(channelId));
                } else {
                    logger.error("Cannot find channel, it may have been removed: " + channelId);
                }
            }
        }

        return channels;
    }

    @Override
    public Channel getChannelById(String channelId) {
        return channelCache.getCachedItemById(channelId);
    }

    @Override
    public Channel getChannelByName(String channelName) {
        return channelCache.getCachedItemByName(channelName);
    }

    @Override
    public String getDestinationName(String channelId, int metaDataId) {
        return channelCache.getCachedDestinationName(channelId, metaDataId);
    }

    @Override
    public Set<String> getChannelIds() {
        return channelCache.getCachedIds();
    }

    @Override
    public Set<String> getChannelNames() {
        return channelCache.getCachedNames();
    }

    @Override
    public List<ChannelSummary> getChannelSummary(Map<String, ChannelHeader> clientChannels, boolean ignoreNewChannels) throws ControllerException {
        logger.debug("getting channel summary");
        List<ChannelSummary> channelSummaries = new ArrayList<ChannelSummary>();

        try {
            Map<String, Channel> serverChannels = new HashMap<String, Channel>();
            List<Channel> channels = getChannels(ignoreNewChannels ? clientChannels.keySet() : null);
            for (Channel serverChannel : channels) {
                serverChannels.put(serverChannel.getId(), serverChannel);
            }

            Map<String, Long> localChannelIds;
            if (donkey == null) {
                donkey = Donkey.getInstance();
            }

            DonkeyDao dao = donkey.getDaoFactory().getDao();

            try {
                localChannelIds = dao.getLocalChannelIds();
            } finally {
                dao.close();
            }

            /*
             * Iterate through the cached channel list and check if a channel with the id exists on
             * the server. If it does, and the revision numbers aren't equal, then add the channel
             * to the updated list. If the cached deployed date is outdated, also add the updated
             * deployed channel info (date and revision delta). Otherwise, if the channel is not
             * found, add it to the deleted list.
             */
            for (String cachedChannelId : clientChannels.keySet()) {
                ChannelSummary summary = new ChannelSummary(cachedChannelId);
                boolean addSummary = false;
                if (localChannelIds != null) {
                    summary.getChannelStatus().setLocalChannelId(localChannelIds.get(cachedChannelId));
                }

                if (serverChannels.containsKey(cachedChannelId)) {
                    ChannelHeader header = clientChannels.get(cachedChannelId);

                    // If the revision numbers aren't equal, add the updated Channel object
                    Integer revision = serverChannels.get(cachedChannelId).getRevision();
                    boolean channelOutdated = !revision.equals(header.getRevision());
                    if (channelOutdated) {
                        summary.getChannelStatus().setChannel(serverChannels.get(cachedChannelId));
                        addSummary = true;
                    }

                    DeployedChannelInfo deployedChannelInfo = getDeployedChannelInfoById(cachedChannelId);
                    boolean serverChannelDeployed = deployedChannelInfo != null;
                    boolean clientChannelDeployed = header.getDeployedDate() != null;

                    if (!serverChannelDeployed) {
                        if (clientChannelDeployed) {
                            // The channel is not deployed, but the client still thinks it's deployed
                            summary.setUndeployed(true);
                            addSummary = true;
                        }
                    } else {
                        if (channelOutdated || !clientChannelDeployed || deployedChannelInfo.getDeployedDate().compareTo(header.getDeployedDate()) != 0) {
                            // The channel is deployed, but the client doesn't think it's deployed, or it's deployed date/revision is outdated
                            summary.getChannelStatus().setDeployedRevisionDelta(revision - deployedChannelInfo.getDeployedRevision());
                            summary.getChannelStatus().setDeployedDate(deployedChannelInfo.getDeployedDate());
                            addSummary = true;
                        }

                        summary.getChannelStatus().setCodeTemplatesChanged(!codeTemplateController.getCodeTemplateRevisionsForChannel(cachedChannelId).equals(deployedChannelInfo.getCodeTemplateRevisions()));
                        if (summary.getChannelStatus().isCodeTemplatesChanged() != header.isCodeTemplatesChanged()) {
                            addSummary = true;
                        }
                    }
                } else {
                    // If a channel with the ID is never found on the server, add it as deleted
                    summary.setDeleted(true);
                    addSummary = true;
                }

                if (addSummary) {
                    channelSummaries.add(summary);
                }
            }

            /*
             * Add summaries for any entries on the server but not in the client's cache.
             */
            for (String serverChannelId : serverChannels.keySet()) {
                if (!clientChannels.containsKey(serverChannelId)) {
                    ChannelSummary summary = new ChannelSummary(serverChannelId);
                    summary.getChannelStatus().setChannel(serverChannels.get(serverChannelId));
                    summary.getChannelStatus().setLocalChannelId(com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().getLocalChannelId(serverChannelId));

                    DeployedChannelInfo deployedChannelInfo = getDeployedChannelInfoById(serverChannelId);
                    boolean serverChannelDeployed = deployedChannelInfo != null;
                    if (serverChannelDeployed) {
                        summary.getChannelStatus().setDeployedRevisionDelta(serverChannels.get(serverChannelId).getRevision() - deployedChannelInfo.getDeployedRevision());
                        summary.getChannelStatus().setDeployedDate(deployedChannelInfo.getDeployedDate());

                        if (!codeTemplateController.getCodeTemplateRevisionsForChannel(serverChannelId).equals(deployedChannelInfo.getCodeTemplateRevisions())) {
                            summary.getChannelStatus().setCodeTemplatesChanged(true);
                        }
                    }

                    channelSummaries.add(summary);
                }
            }

            return channelSummaries;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public synchronized void setChannelEnabled(Set<String> channelIds, ServerEventContext context, boolean enabled) throws ControllerException {
        ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
        Map<String, ChannelMetadata> metadataMap = configurationController.getChannelMetadata();
        Map<String, Channel> cachedChannelMap = channelCache.getAllItems();
        boolean changed = false;

        for (String channelId : channelIds) {
            Channel cachedChannel = cachedChannelMap.get(channelId);
            ChannelMetadata metadata = metadataMap.get(channelId);

            if (cachedChannel != null && !(cachedChannel instanceof InvalidChannel) && (metadata == null || metadata.isEnabled() != enabled)) {
                if (metadata == null) {
                    metadata = new ChannelMetadata();
                    metadataMap.put(channelId, metadata);
                }
                metadata.setEnabled(enabled);
                changed = true;
            }
        }

        if (changed) {
            configurationController.setChannelMetadata(metadataMap);
        }
    }

    @Override
    public synchronized void setChannelInitialState(Set<String> channelIds, ServerEventContext context, DeployedState initialState) throws ControllerException {
        /*
         * Methods that update the channel must be synchronized to ensure the channel cache and
         * database never contain different versions of a channel.
         */
        if (initialState != DeployedState.STARTED && initialState != DeployedState.PAUSED && initialState != DeployedState.STOPPED) {
            throw new ControllerException("Cannot set initial state to " + initialState);
        }

        ControllerException firstCause = null;
        List<Channel> cachedChannels = getChannels(channelIds);

        for (Channel cachedChannel : cachedChannels) {
            // If the channel is not invalid, and its enabled flag isn't already the same as what was passed in
            if (!(cachedChannel instanceof InvalidChannel) && cachedChannel.getProperties().getInitialState() != initialState) {
                Channel channel = (Channel) SerializationUtils.clone(cachedChannel);
                channel.getProperties().setInitialState(initialState);
                channel.setRevision(channel.getRevision() + 1);

                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", channel.getId());
                    params.put("name", channel.getName());
                    params.put("revision", channel.getRevision());
                    params.put("channel", channel);

                    // Update the new channel in the database
                    logger.debug("updating channel");
                    SqlConfig.getSqlSessionManager().update("Channel.updateChannel", params);

                    // invoke the channel plugins
                    for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
                        channelPlugin.save(channel, context);
                    }
                } catch (Exception e) {
                    if (firstCause == null) {
                        firstCause = new ControllerException(e);
                    }
                }
            }
        }

        if (firstCause != null) {
            throw firstCause;
        }
    }

    @Override
    public synchronized boolean updateChannel(Channel channel, ServerEventContext context, boolean override) throws ControllerException {
        // Extract metadata and then clear it from the channel model so it won't be stored in the database
        ChannelExportData exportData = channel.getExportData();
        channel.clearExportData();

        /*
         * Methods that update the channel must be synchronized to ensure the channel cache and
         * database never contain different versions of a channel.
         */

        int newRevision = channel.getRevision();
        int currentRevision = 0;

        Channel matchingChannel = getChannelById(channel.getId());

        // If the channel exists, set the currentRevision
        if (matchingChannel != null) {
            /*
             * If the channel in the database is the same as what's being passed in, don't bother
             * saving it.
             * 
             * Ignore the channel revision and last modified date when comparing the channel being
             * passed in to the existing channel in the database. This will prevent the channel from
             * being saved if the only thing that changed was the revision and/or the last modified
             * date. The client/CLI take care of this by passing in the proper revision number, but
             * the API alone does not.
             */
            if (EqualsBuilder.reflectionEquals(channel, matchingChannel, new String[] {
                    "lastModified", "revision" })) {
                updateChannelMetadata(channel.getId(), exportData.getMetadata());
                updateChannelTags(channel.getId(), exportData.getChannelTags());
                return true;
            }

            currentRevision = matchingChannel.getRevision();

            // Use the larger nextMetaDataId to ensure a metadata ID will never be reused if an older version of a channel is imported or saved.
            channel.setNextMetaDataId(Math.max(matchingChannel.getNextMetaDataId(), channel.getNextMetaDataId()));
        }

        /*
         * If it's not a new channel, and its version is different from the one in the database (in
         * case it has been changed on the server since the client started modifying it), and
         * override is not enabled
         */
        if ((currentRevision > 0) && (currentRevision != newRevision) && !override) {
            return false;
        } else {
            channel.setRevision(currentRevision + 1);
        }

        ArrayList<String> destConnectorNames = new ArrayList<String>(channel.getDestinationConnectors().size());

        for (Connector connector : channel.getDestinationConnectors()) {
            if (destConnectorNames.contains(connector.getName())) {
                throw new ControllerException("Destination connectors must have unique names");
            }
            destConnectorNames.add(connector.getName());
        }

        try {
            // If we are adding, then make sure the name isn't being used
            matchingChannel = getChannelByName(channel.getName());

            if (matchingChannel != null) {
                if (!channel.getId().equals(matchingChannel.getId())) {
                    logger.error("There is already a channel with the name " + channel.getName());
                    throw new ControllerException("A channel with that name already exists");
                }
            }

            // Update the metadata first
            updateChannelMetadata(channel.getId(), exportData.getMetadata());
            updateChannelTags(channel.getId(), exportData.getChannelTags());

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", channel.getId());
            params.put("name", channel.getName());
            params.put("revision", channel.getRevision());
            params.put("channel", channel);

            // Put the new channel in the database
            if (getChannelById(channel.getId()) == null) {
                logger.debug("adding channel");
                SqlConfig.getSqlSessionManager().insert("Channel.insertChannel", params);
            } else {
                logger.debug("updating channel");
                SqlConfig.getSqlSessionManager().update("Channel.updateChannel", params);
            }

            // invoke the channel plugins
            for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
                channelPlugin.save(channel, context);
            }

            return true;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private void updateChannelMetadata(String channelId, ChannelMetadata metadata) {
        ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

        Map<String, ChannelMetadata> metadataMap = configurationController.getChannelMetadata();
        if (!Objects.equals(metadataMap.get(channelId), metadata)) {
            // Only need to update if the metadata has changed
            metadataMap.put(channelId, metadata);
            configurationController.setChannelMetadata(metadataMap);
        }
    }

    private void updateChannelTags(String channelId, List<ChannelTag> channelTags) {
        ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

        boolean updateTags = false;
        Set<ChannelTag> serverChannelTags = configurationController.getChannelTags();
        for (ChannelTag existingTag : serverChannelTags) {
            boolean found = false;

            for (Iterator<ChannelTag> it = channelTags.iterator(); it.hasNext();) {
                ChannelTag tag = it.next();
                if (existingTag.getId().equals(tag.getId()) || existingTag.getName().equalsIgnoreCase(tag.getName())) {
                    found = true;

                    if (!existingTag.getChannelIds().contains(channelId)) {
                        updateTags = true;
                        existingTag.getChannelIds().add(channelId);
                    }

                    it.remove();
                    break;
                }
            }

            if (!found) {
                if (existingTag.getChannelIds().contains(channelId)) {
                    updateTags = true;
                    existingTag.getChannelIds().remove(channelId);
                }
            }
        }

        for (ChannelTag tag : channelTags) {
            updateTags = true;
            serverChannelTags.add(tag);
        }

        if (updateTags) {
            configurationController.setChannelTags(serverChannelTags);
        }
    }

    /**
     * Removes a channel. If the channel is NULL, then all channels are removed.
     * 
     * @param channel
     * @throws ControllerException
     */
    @Override
    public synchronized void removeChannel(Channel channel, ServerEventContext context) throws ControllerException {
        /*
         * Methods that update the channel must be synchronized to ensure the channel cache and
         * database never contain different versions of a channel.
         */

        logger.debug("removing channel");

        if (channel != null && ControllerFactory.getFactory().createEngineController().isDeployed(channel.getId())) {
            logger.warn("Cannot remove deployed channel.");
            return;
        }

        try {
            //TODO combine and organize these.
            // Delete the "d_" tables and the channel record from "d_channels"
            com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().removeChannel(channel.getId());
            // Delete the channel record from the "channel" table
            SqlConfig.getSqlSessionManager().delete("Channel.deleteChannel", channel.getId());

            if (DatabaseUtil.statementExists("Channel.vacuumChannelTable")) {
                SqlConfig.getSqlSessionManager().update("Channel.vacuumChannelTable");
            }

            // Update any groups that contained this channel
            Set<ChannelGroup> groups = new HashSet<ChannelGroup>(channelGroupCache.getAllItems().values());
            boolean groupsChanged = false;
            for (ChannelGroup group : groups) {
                for (Iterator<Channel> it = group.getChannels().iterator(); it.hasNext();) {
                    if (channel.getId().equals(it.next().getId())) {
                        it.remove();
                        groupsChanged = true;
                    }
                }
            }
            if (groupsChanged) {
                updateChannelGroups(groups, new HashSet<String>(), true);
            }

            // Remove any dependencies that were tied to this channel
            ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
            Set<ChannelDependency> dependencies = configurationController.getChannelDependencies();
            boolean dependenciesChanged = false;
            for (Iterator<ChannelDependency> it = dependencies.iterator(); it.hasNext();) {
                ChannelDependency dependency = it.next();
                if (channel.getId().equals(dependency.getDependentId()) || channel.getId().equals(dependency.getDependencyId())) {
                    it.remove();
                    dependenciesChanged = true;
                }
            }
            if (dependenciesChanged) {
                configurationController.setChannelDependencies(dependencies);
            }

            // Update the metadata
            Map<String, ChannelMetadata> metadataMap = configurationController.getChannelMetadata();
            if (metadataMap.remove(channel.getId()) != null) {
                configurationController.setChannelMetadata(metadataMap);
            }

            // Remove any tags this channel was a part of
            boolean tagsRemoved = false;
            Set<ChannelTag> tags = configurationController.getChannelTags();
            for (ChannelTag tag : tags) {
                if (tag.getChannelIds().contains(channel.getId())) {
                    tagsRemoved = true;
                    tag.getChannelIds().remove(channel.getId());
                }
            }

            if (tagsRemoved) {
                configurationController.setChannelTags(tags);
            }

            // invoke the channel plugins
            for (ChannelPlugin channelPlugin : extensionController.getChannelPlugins().values()) {
                channelPlugin.remove(channel, context);
            }
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public Map<String, Integer> getChannelRevisions() throws ControllerException {
        try {
            List<Map<String, Object>> results = SqlConfig.getSqlSessionManager().selectList("Channel.getChannelRevision");

            Map<String, Integer> channelRevisions = new HashMap<String, Integer>();
            for (Map<String, Object> result : results) {
                channelRevisions.put((String) result.get("id"), (Integer) result.get("revision"));
            }

            return channelRevisions;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public Map<Integer, String> getConnectorNames(String channelId) {
        logger.debug("getting connector names");
        Channel channel = getChannelById(channelId);

        if (channel == null || channel instanceof InvalidChannel) {
            return null;
        }

        Map<Integer, String> connectorNames = new LinkedHashMap<Integer, String>();
        connectorNames.put(0, "Source");

        for (Connector connector : channel.getDestinationConnectors()) {
            connectorNames.put(connector.getMetaDataId(), connector.getName());
        }

        return connectorNames;
    }

    @Override
    public List<MetaDataColumn> getMetaDataColumns(String channelId) {
        logger.debug("getting metadata columns");
        Channel channel = getChannelById(channelId);

        if (channel == null || channel instanceof InvalidChannel) {
            return null;
        }

        return channel.getProperties().getMetaDataColumns();
    }

    // ---------- DEPLOYED CHANNEL CACHE ----------
    @Override
    public void putDeployedChannelInCache(Channel channel) {
        deployedChannelCache.putDeployedChannelInCache(channel);
    }

    @Override
    public void removeDeployedChannelFromCache(String channelId) {
        deployedChannelCache.removeDeployedChannelFromCache(channelId);
    }

    @Override
    public Channel getDeployedChannelById(String channelId) {
        return deployedChannelCache.getDeployedChannelById(channelId);
    }

    @Override
    public Channel getDeployedChannelByName(String channelName) {
        return deployedChannelCache.getDeployedChannelByName(channelName);
    }

    @Override
    public DeployedChannelInfo getDeployedChannelInfoById(String channelId) {
        return deployedChannelCache.getDeployedChannelInfoById(channelId);
    }

    @Override
    public String getDeployedDestinationName(String channelId, int metaDataId) {
        return deployedChannelCache.getDeployedDestinationName(channelId, metaDataId);
    }

    @Override
    public Statistics getStatistics() {
        return com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().getStatistics();
    }

    @Override
    public Statistics getTotalStatistics() {
        return com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().getTotalStatistics();
    }

    @Override
    public Statistics getStatisticsFromStorage(String serverId) {
        return com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().getStatisticsFromStorage(serverId);
    }

    @Override
    public Statistics getTotalStatisticsFromStorage(String serverId) {
        return com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().getTotalStatisticsFromStorage(serverId);
    }

    @Override
    public int getConnectorMessageCount(String channelId, String serverId, int metaDataId, Status status) {
        return com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().getConnectorMessageCount(channelId, serverId, metaDataId, status);
    }

    @Override
    public void resetStatistics(Map<String, List<Integer>> channelConnectorMap, Set<Status> statuses) {
        com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().resetStatistics(channelConnectorMap, statuses);
    }

    @Override
    public void resetAllStatistics() {
        com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().resetAllStatistics();
    }

    @Override
    public List<Channel> getDeployedChannels(Set<String> channelIds) {
        return deployedChannelCache.getDeployedChannels(channelIds);
    }

    @Override
    public List<ChannelGroup> getChannelGroups(Set<String> channelGroupIds) {
        Map<String, ChannelGroup> channelGroupMap = channelGroupCache.getAllItems();

        List<ChannelGroup> channelGroups = new ArrayList<ChannelGroup>();

        if (channelGroupIds == null) {
            channelGroups.addAll(channelGroupMap.values());
        } else {
            for (String groupId : channelGroupIds) {
                if (channelGroupMap.containsKey(groupId)) {
                    channelGroups.add(channelGroupMap.get(groupId));
                } else {
                    logger.error("Cannot find channel group, it may have been removed: " + groupId);
                }
            }
        }

        return channelGroups;
    }

    @Override
    public synchronized boolean updateChannelGroups(Set<ChannelGroup> channelGroups, Set<String> removedChannelGroupIds, boolean override) throws ControllerException {
        // If override is disabled, first check all channel groups to make sure they haven't been modified already
        if (!override) {
            Map<String, ChannelGroup> channelGroupMap = channelGroupCache.getAllItems();

            for (ChannelGroup group : channelGroups) {
                ChannelGroup matchingGroup = channelGroupMap.get(group.getId());

                if (matchingGroup != null) {
                    if (!EqualsBuilder.reflectionEquals(group, matchingGroup, "lastModified", "revision")) {
                        /*
                         * If it's not a new group, and its version is different from the one in the
                         * database (in case it has been changed on the server since the client
                         * started modifying it), and override is not enabled, then don't allow the
                         * update
                         */
                        if (!group.getRevision().equals(matchingGroup.getRevision())) {
                            return false;
                        }
                    }

                    // If a matching group was found, always remove it from the map
                    channelGroupMap.remove(group.getId());
                }
            }

            // Remove any groups that were expected to be removed
            for (String removedChannelGroupId : removedChannelGroupIds) {
                channelGroupMap.remove(removedChannelGroupId);
            }

            // If any groups are left, the client is out of sync
            if (!channelGroupMap.isEmpty()) {
                return false;
            }
        }

        Map<String, ChannelGroup> channelGroupMap = channelGroupCache.getAllItems();
        Map<String, String> channelIdMap = new HashMap<String, String>();
        List<ChannelGroup> groupsToRemove = new ArrayList<ChannelGroup>(channelGroupMap.values());
        Set<String> groupNames = new HashSet<String>();
        Set<String> unchangedGroupIds = new HashSet<String>();

        for (ChannelGroup group : channelGroups) {
            if (StringUtils.equals(group.getId(), ChannelGroup.DEFAULT_ID) || StringUtils.equals(group.getName(), ChannelGroup.DEFAULT_NAME)) {
                String errorMessage = "Channel groups cannot have the same ID or name as the default group.";
                logger.error(errorMessage);
                throw new ControllerException(errorMessage);
            }

            for (Channel channel : group.getChannels()) {
                // Make sure this group's channels aren't contained in any other group
                if (channelIdMap.put(channel.getId(), group.getId()) != null) {
                    String errorMessage = "Channel \"" + channel.getId() + "\" belongs to more than one group.";
                    logger.error(errorMessage);
                    throw new ControllerException(errorMessage);
                }
            }

            /*
             * Channels are stored separately in the database. Only the channel ID is needed when
             * storing the group.
             */
            group.replaceChannelsWithIds();

            // Make sure there isn't another group with the same name
            if (!groupNames.add(group.getName())) {
                String errorMessage = "There is already a channel group with the name " + group.getName();
                logger.error(errorMessage);
                throw new ControllerException(errorMessage);
            }

            ChannelGroup matchingGroup = channelGroupMap.get(group.getId());

            if (matchingGroup != null) {
                if (EqualsBuilder.reflectionEquals(group, matchingGroup, "lastModified", "revision")) {
                    unchangedGroupIds.add(group.getId());
                } else {
                    /*
                     * If it's not a new group, and its version is different from the one in the
                     * database (in case it has been changed on the server since the client started
                     * modifying it), and override is not enabled, then don't allow the update
                     */
                    if (!group.getRevision().equals(matchingGroup.getRevision()) && !override) {
                        return false;
                    } else {
                        group.setRevision(matchingGroup.getRevision() + 1);
                    }
                }

                // Either way, this group is not being removed
                groupsToRemove.remove(matchingGroup);
            } else {
                // Always start at revision 1 for new groups
                group.setRevision(1);
            }
        }

        // Remove groups
        for (ChannelGroup group : groupsToRemove) {
            try {
                SqlConfig.getSqlSessionManager().delete("Channel.deleteChannelGroup", group.getId());

                // TODO: Add this to mapper
                if (DatabaseUtil.statementExists("Channel.vacuumChannelGroupTable")) {
                    SqlConfig.getSqlSessionManager().update("Channel.vacuumChannelGroupTable");
                }
            } catch (Exception e) {
                throw new ControllerException(e);
            }
        }

        // Insert or update groups
        for (ChannelGroup group : channelGroups) {
            if (!unchangedGroupIds.contains(group.getId())) {
                try {
                    group.setLastModified(Calendar.getInstance());

                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", group.getId());
                    params.put("name", group.getName());
                    params.put("revision", group.getRevision());
                    params.put("channelGroup", group);

                    // If its a new group, insert it, otherwise, update it
                    if (channelGroupCache.getCachedItemById(group.getId()) == null) {
                        logger.debug("Inserting channel group");
                        SqlConfig.getSqlSessionManager().insert("Channel.insertChannelGroup", params);
                    } else {
                        logger.debug("Updating channel group");
                        SqlConfig.getSqlSessionManager().update("Channel.updateChannelGroup", params);
                    }
                } catch (Exception e) {
                    throw new ControllerException(e);
                }
            }
        }

        return true;
    }

    // ---------- CHANNEL CACHE ----------

    /**
     * The Channel cache holds all channels currently stored in the database. Every method first
     * should call refreshCache() to update any outdated, missing, or removed channels in the cache
     * before performing its function. No two threads should refresh the cache simultaneously.
     */
    private class ChannelCache extends Cache<Channel> {

        public ChannelCache() {
            super("Channel", "Channel.getChannelRevision", "Channel.getChannel");
        }

        private String getCachedDestinationName(String channelId, int metaDataId) {
            refreshCache();
            Channel channel = cacheById.get(channelId);

            if (channel != null) {
                for (Connector connector : channel.getDestinationConnectors()) {
                    if (connector.getMetaDataId() == metaDataId) {
                        return connector.getName();
                    }
                }
            }

            return null;
        }
    }

    // ---------- DEPLOYED CHANNEL CACHE ----------
    /**
     * The deployed channel cache holds all channels currently deployed on this server.
     * 
     */
    private class DeployedChannelCache {
        // deployed channel cache
        private Map<String, Channel> deployedChannelCacheById = new ConcurrentHashMap<String, Channel>();
        private Map<String, Channel> deployedChannelCacheByName = new ConcurrentHashMap<String, Channel>();

        private Map<String, DeployedChannelInfo> deployedChannelInfoCache = new ConcurrentHashMap<String, DeployedChannelInfo>();

        private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
        private Lock readLock = readWriteLock.readLock();
        private Lock writeLock = readWriteLock.writeLock();

        private void putDeployedChannelInCache(Channel channel) {
            Map<String, Integer> codeTemplateRevisions = null;
            try {
                codeTemplateRevisions = codeTemplateController.getCodeTemplateRevisionsForChannel(channel.getId());
            } catch (ControllerException e) {
                // Just exclude the code template info, rather than preventing the channel from deploying
            }

            try {
                writeLock.lock();

                Channel oldDeployedChannel = channelCache.getCachedItemById(channel.getId());

                DeployedChannelInfo deployedChannelInfo = new DeployedChannelInfo();
                deployedChannelInfo.setDeployedDate(Calendar.getInstance());
                deployedChannelInfo.setDeployedRevision(channel.getRevision());
                deployedChannelInfo.setCodeTemplateRevisions(codeTemplateRevisions);
                deployedChannelInfoCache.put(channel.getId(), deployedChannelInfo);

                deployedChannelCacheById.put(channel.getId(), channel);
                deployedChannelCacheByName.put(channel.getName(), channel);

                /*
                 * If the channel being put in the cache already existed and it has a new name, make
                 * sure to remove the entry with its old name from the channelCacheByName map.
                 */
                if (oldDeployedChannel != null && !oldDeployedChannel.getName().equals(channel.getName())) {
                    deployedChannelCacheByName.remove(oldDeployedChannel.getName());
                }
            } finally {
                writeLock.unlock();
            }

        }

        private void removeDeployedChannelFromCache(String channelId) {
            try {
                writeLock.lock();

                deployedChannelInfoCache.remove(channelId);

                String channelName = getDeployedChannelById(channelId).getName();
                deployedChannelCacheById.remove(channelId);
                deployedChannelCacheByName.remove(channelName);
            } finally {
                writeLock.unlock();
            }
        }

        private Channel getDeployedChannelById(String channelId) {
            try {
                readLock.lock();

                return deployedChannelCacheById.get(channelId);
            } finally {
                readLock.unlock();
            }

        }

        private Channel getDeployedChannelByName(String channelName) {
            try {
                readLock.lock();

                return deployedChannelCacheByName.get(channelName);
            } finally {
                readLock.unlock();
            }
        }

        private DeployedChannelInfo getDeployedChannelInfoById(String channelId) {
            try {
                readLock.lock();

                return deployedChannelInfoCache.get(channelId);
            } finally {
                readLock.unlock();
            }
        }

        private String getDeployedDestinationName(String channelId, int metaDataId) {
            try {
                readLock.lock();
                Channel channel = getDeployedChannelById(channelId);

                if (channel != null) {
                    for (Connector connector : channel.getDestinationConnectors()) {
                        if (connector.getMetaDataId() == metaDataId) {
                            return connector.getName();
                        }
                    }
                }

                return null;
            } finally {
                readLock.unlock();
            }
        }

        private List<Channel> getDeployedChannels(Set<String> channelIds) {
            try {
                readLock.lock();

                List<Channel> channels = new ArrayList<Channel>();

                if (channelIds == null) {
                    channels.addAll(deployedChannelCacheById.values());
                } else {
                    for (String channelId : channelIds) {
                        if (deployedChannelCacheById.containsKey(channelId)) {
                            channels.add(deployedChannelCacheById.get(channelId));
                        }
                    }
                }

                return channels;
            } finally {
                readLock.unlock();
            }
        }
    }
}