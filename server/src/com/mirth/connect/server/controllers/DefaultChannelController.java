/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultChannelController extends ChannelController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    private ChannelCache channelCache = new ChannelCache();
    private DeployedChannelCache deployedChannelCache = new DeployedChannelCache();

    private static DefaultChannelController instance = null;

    private DefaultChannelController() {

    }

    public static ChannelController create() {
        synchronized (DefaultChannelController.class) {
            if (instance == null) {
                instance = new DefaultChannelController();
            }

            return instance;
        }
    }

    @Override
    public List<Channel> getChannels(Set<String> channelIds) {
        Map<String, Channel> channelMap = channelCache.getAllChannels();

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
        return channelCache.getCachedChannelById(channelId);
    }

    @Override
    public Channel getChannelByName(String channelName) {
        return channelCache.getCachedChannelByName(channelName);
    }

    @Override
    public String getDestinationName(String connectorId) {
        return channelCache.getCachedDestinationName(connectorId);
    }

    @Override
    public Set<String> getChannelIds() {
        return channelCache.getCachedChannelIds();
    }

    @Override
    public List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException {
        logger.debug("getting channel summary");
        List<ChannelSummary> channelSummaries = new ArrayList<ChannelSummary>();

        try {
            Map<String, Integer> serverChannels = getChannelRevisions();

            /*
             * Iterate through the cached channel list and check if a channel
             * with the id exists on the server. If it does, and the revision
             * numbers aren't equal, then add the channel to the updated list.
             * Otherwise, if the channel is not found, add it to the deleted
             * list.
             */
            for (String cachedChannelId : cachedChannels.keySet()) {
                boolean channelExistsOnServer = false;

                // iterate through all of the channels on the server
                for (Entry<String, Integer> entry : serverChannels.entrySet()) {
                    String id = entry.getKey();
                    Integer revision = entry.getValue();

                    // if the channel with the cached id exists
                    if (id.equals(cachedChannelId)) {
                        // and the revision numbers aren't equal, add it as
                        // updated
                        if (!revision.equals(cachedChannels.get(cachedChannelId))) {
                            ChannelSummary summary = new ChannelSummary();
                            summary.setId(id);
                            channelSummaries.add(summary);
                        }

                        channelExistsOnServer = true;
                    }
                }

                // if a channel with the id is never found on the server, add it
                // as deleted
                if (!channelExistsOnServer) {
                    ChannelSummary summary = new ChannelSummary();
                    summary.setId(cachedChannelId);
                    summary.setDeleted(true);
                    channelSummaries.add(summary);
                }
            }

            /*
             * Iterate through the server channel list, check if every id exists
             * in the cached channel list. If it doesn't, add it to the summary
             * list as added.
             */
            for (Entry<String, Integer> entry : serverChannels.entrySet()) {
                String id = entry.getKey();

                if (!cachedChannels.keySet().contains(id)) {
                    ChannelSummary summary = new ChannelSummary();
                    summary.setId(id);
                    summary.setAdded(true);
                    channelSummaries.add(summary);
                }
            }

            return channelSummaries;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public Set<String> getChannelTags(Set<String> channelIds) {

        Set<String> channelTags = new LinkedHashSet<String>();
        List<Channel> channels = getChannels(channelIds);

        for (Channel channel : channels) {
            channelTags.addAll(channel.getProperties().getTags());
        }

        return channelTags;
    }

    @Override
    public boolean updateChannel(Channel channel, ServerEventContext context, boolean override) throws ControllerException {
        int newRevision = channel.getRevision();
        int currentRevision = 0;

        Channel matchingChannel = getChannelById(channel.getId());

        // If the channel exists, set the currentRevision
        if (matchingChannel != null) {
            /*
             * If the channel in the database is the same as what's being passed
             * in, don't bother saving it.
             * 
             * Ignore the channel revision and last modified date when comparing
             * the channel being passed in to the existing channel in the
             * database. This will prevent the channel from being saved if the
             * only thing that changed was the revision and/or the last modified
             * date. The client/CLI take care of this by passing in the proper
             * revision number, but the API alone does not.
             */
            if (EqualsBuilder.reflectionEquals(channel, matchingChannel, new String[] {
                    "lastModified", "revision" })) {
                return true;
            }

            currentRevision = matchingChannel.getRevision();

            // Use the larger nextMetaDataId to ensure a metadata ID will never be reused if an older version of a channel is imported or saved.
            channel.setNextMetaDataId(Math.max(matchingChannel.getNextMetaDataId(), channel.getNextMetaDataId()));
        }

        /*
         * If it's not a new channel, and its version is different from the one
         * in the database (in case it has been changed on the server since the
         * client started modifying it), and override is not enabled
         */
        if ((currentRevision > 0) && (currentRevision != newRevision) && !override) {
            return false;
        } else {
            channel.setRevision(currentRevision + 1);
        }

        ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

        String sourceVersion = extensionController.getConnectorMetaDataByTransportName(channel.getSourceConnector().getTransportName()).getPluginVersion();
        channel.getSourceConnector().setVersion(sourceVersion);

        ArrayList<String> destConnectorNames = new ArrayList<String>(channel.getDestinationConnectors().size());

        for (Connector connector : channel.getDestinationConnectors()) {
            if (destConnectorNames.contains(connector.getName())) {
                throw new ControllerException("Destination connectors must have unique names");
            }
            destConnectorNames.add(connector.getName());

            String destinationVersion = extensionController.getConnectorMetaDataByTransportName(connector.getTransportName()).getPluginVersion();
            connector.setVersion(destinationVersion);
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

    /**
     * Removes a channel. If the channel is NULL, then all channels are removed.
     * 
     * @param channel
     * @throws ControllerException
     */
    @Override
    public void removeChannel(Channel channel, ServerEventContext context) throws ControllerException {
        logger.debug("removing channel");

        if ((channel != null) && ControllerFactory.getFactory().createEngineController().isDeployed(channel.getId())) {
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

        if (channel == null) {
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

        if (channel == null) {
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
    public String getDeployedDestinationName(String connectorId) {
        return deployedChannelCache.getDeployedDestinationName(connectorId);
    }

    @Override
    public String getDeployedConnectorId(String channelId, String connectorName) throws Exception {
        return deployedChannelCache.getDeployedConnectorId(channelId, connectorName);
    }

    // ---------- CHANNEL CACHE ----------

    /**
     * The Channel cache holds all channels currently stored in the database. Every method first
     * should call refreshCache() to update any outdated, missing, or removed channels in the cache
     * before performing its function. No two threads should refresh the cache simultaneously.
     * 
     */
    private class ChannelCache {
        // channel cache
        private Map<String, Channel> channelCacheById = new ConcurrentHashMap<String, Channel>();
        private Map<String, Channel> channelCacheByName = new ConcurrentHashMap<String, Channel>();

        private synchronized void refreshCache() {
            try {
                // Get the current revisions of channels in the database
                Map<String, Integer> databaseChannelRevisions = getChannelRevisions();

                // Remove any channels from the cache that no longer exist in the database
                for (String channelId : channelCacheById.keySet()) {
                    if (!databaseChannelRevisions.containsKey(channelId)) {

                        // Remove channel from cache
                        String channelName = channelCacheById.get(channelId).getName();
                        channelCacheById.remove(channelId);
                        channelCacheByName.remove(channelName);
                    }
                }

                // Put any new or updated channels in the database in the cache
                for (Entry<String, Integer> channelRevision : databaseChannelRevisions.entrySet()) {
                    String channelId = channelRevision.getKey();

                    if (!channelCacheById.containsKey(channelId) || channelRevision.getValue() > channelCacheById.get(channelId).getRevision()) {
                        Channel channel = null;
                        
                        try {
                            channel = SqlConfig.getSqlSessionManager().selectOne("Channel.getChannel", channelId);
                        } catch (Exception e) {
                            logger.error("Failed to load channel " + channelId + " from the database", e);
                        }

                        if (channel != null) {
                            Channel oldChannel = channelCacheById.get(channelId);

                            channelCacheById.put(channel.getId(), channel);
                            channelCacheByName.put(channel.getName(), channel);

                            /*
                             * If the channel being put in the cache already existed and it has a
                             * new name, make sure to remove the entry with its old name from the
                             * channelCacheByName map.
                             */
                            if (oldChannel != null && !oldChannel.getName().equals(channel.getName())) {
                                channelCacheByName.remove(oldChannel.getName());
                            }
                        } else {
                            /*
                             * The channel was either removed from the database after the initial
                             * revision query or an error occurred while attempting to retrieve it,
                             * remove it from the cache if it already existed.
                             */
                            if (channelCacheById.containsKey(channelId)) {
                                // Remove channel from cache
                                String channelName = channelCacheById.get(channelId).getName();
                                channelCacheById.remove(channelId);
                                channelCacheByName.remove(channelName);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error refreshing channel cache", e);
            }
        }

        private Map<String, Channel> getAllChannels() {
            refreshCache();

            return new ConcurrentHashMap<String, Channel>(channelCacheById);
        }

        private Channel getCachedChannelById(String channelId) {
            refreshCache();

            return channelCacheById.get(channelId);
        }

        private Channel getCachedChannelByName(String channelName) {
            refreshCache();

            return channelCacheByName.get(channelName);
        }

        private String getCachedDestinationName(String connectorId) {
            refreshCache();

            // String format: channelid_destination_index
            String destinationName = connectorId;
            // if we can't parse the name, just use the id
            String channelId = connectorId.substring(0, connectorId.indexOf('_'));
            String strIndex = connectorId.substring(connectorId.indexOf("destination_") + 12, connectorId.indexOf("_connector"));
            int index = Integer.parseInt(strIndex) - 1;
            Channel channel = channelCacheById.get(channelId);

            if (channel != null) {
                if (index < channel.getDestinationConnectors().size())
                    destinationName = channel.getDestinationConnectors().get(index).getName();
            }

            return destinationName;
        }

        private Set<String> getCachedChannelIds() {
            refreshCache();

            return new LinkedHashSet<String>(channelCacheById.keySet());
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
            try {
                writeLock.lock();

                Channel oldDeployedChannel = channelCache.getCachedChannelById(channel.getId());

                DeployedChannelInfo deployedChannelInfo = new DeployedChannelInfo();
                deployedChannelInfo.setDeployedDate(Calendar.getInstance());
                deployedChannelInfo.setDeployedRevision(channel.getRevision());
                deployedChannelInfoCache.put(channel.getId(), deployedChannelInfo);

                deployedChannelCacheById.put(channel.getId(), channel);
                deployedChannelCacheByName.put(channel.getName(), channel);

                /*
                 * If the channel being put in the cache already existed and it has a
                 * new name, make sure to remove the entry with its old name from the
                 * channelCacheByName map.
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

        private String getDeployedDestinationName(String connectorId) {
            try {
                readLock.lock();

                // String format: channelid_destination_index
                String destinationName = connectorId;
                // if we can't parse the name, just use the id
                String channelId = connectorId.substring(0, connectorId.indexOf('_'));
                String strIndex = connectorId.substring(connectorId.indexOf("destination_") + 12, connectorId.indexOf("_connector"));
                int index = Integer.parseInt(strIndex) - 1;
                Channel channel = getDeployedChannelById(channelId);

                if (channel != null) {
                    if (index < channel.getDestinationConnectors().size())
                        destinationName = channel.getDestinationConnectors().get(index).getName();
                }

                return destinationName;
            } finally {
                readLock.unlock();
            }
        }

        private String getDeployedConnectorId(String channelId, String connectorName) throws Exception {
            try {
                readLock.lock();

                Channel channel = getDeployedChannelById(channelId);
                int index = 1;

                for (Connector connector : channel.getDestinationConnectors()) {
                    if (connector.getName().equals(connectorName)) {
                        return String.valueOf(index);
                    } else {
                        index++;
                    }
                }

                throw new Exception("Connector name not found");
            } finally {
                readLock.unlock();
            }

        }
    }
}
