/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.QueueUtil;

public class DefaultChannelController extends ChannelController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelStatusController channelStatusController = ControllerFactory.getFactory().createChannelStatusController();
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
    public List<Channel> getChannel(Channel channel) throws ControllerException {
        logger.debug("getting channel");

        try {
            return SqlConfig.getSqlMapClient().queryForList("Channel.getChannel", channel);
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    @Override
    public List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException {
        logger.debug("getting channel summary");
        List<ChannelSummary> channelSummaries = new ArrayList<ChannelSummary>();

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
    }

    @Override
    public synchronized boolean updateChannel(Channel channel, ServerEventContext context, boolean override) throws ControllerException {
        /*
         * updateChannel and removeChannel must be synchronized to ensure the channel cache and database
         * never contain different versions of a channel.
         */
        
        int newRevision = channel.getRevision();
        int currentRevision = 0;

        Channel filterChannel = new Channel();
        filterChannel.setId(channel.getId());
        List<Channel> matchingChannels = getChannel(filterChannel);

        // If the channel exists, set the currentRevision
        if (!matchingChannels.isEmpty()) {
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
            if (EqualsBuilder.reflectionEquals(channel, matchingChannels.get(0), new String[] { "lastModified", "revision" })) {
                return true;
            }

            currentRevision = matchingChannels.get(0).getRevision();
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
        channel.setVersion(configurationController.getServerVersion());

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
            Channel channelFilter = new Channel();
            channelFilter.setId(channel.getId());

            if (getChannel(channelFilter).isEmpty()) {
                // If we are adding, then make sure the name isn't being used
                channelFilter = new Channel();
                channelFilter.setName(channel.getName());

                if (!getChannel(channelFilter).isEmpty()) {
                    logger.error("There is already a channel with the name " + channel.getName());
                    throw new ControllerException("A channel with that name already exists");
                }
            }

            /*
             * Put the new channel in the database. Check if the channel exists with
             * getCachedChannelById instead of getChannel to ensure the channel cache is up to date
             * before any changes are made in the database.
             */
            if (getCachedChannelById(channel.getId()) == null) {
                logger.debug("adding channel");
                SqlConfig.getSqlMapClient().insert("Channel.insertChannel", channel);
            } else {
                logger.debug("updating channel");
                SqlConfig.getSqlMapClient().update("Channel.updateChannel", channel);
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
    public synchronized void removeChannel(Channel channel, ServerEventContext context) throws ControllerException {
        /*
         * updateChannel and removeChannel must be synchronized to ensure the channel cache and database
         * never contain different versions of a channel.
         */
        
        logger.debug("removing channel");

        if ((channel != null) && channelStatusController.getDeployedIds().contains(channel.getId())) {
            logger.warn("Cannot remove deployed channel.");
            return;
        }

        try {
            if (channel != null) {
                QueueUtil.getInstance().removeAllQueuesForChannel(channel);
            } else {
                QueueUtil.getInstance().removeAllQueues();
            }

            SqlConfig.getSqlMapClient().delete("Channel.deleteChannel", channel);

            if (DatabaseUtil.statementExists("Channel.vacuumChannelTable")) {
                SqlConfig.getSqlMapClient().update("Channel.vacuumChannelTable");
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
            return SqlConfig.getSqlMapClient().queryForMap("Channel.getChannelRevision", null, "id", "revision");
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    // ---------- CHANNEL CACHE ----------
    @Override
    public Channel getCachedChannelById(String channelId) {
        return channelCache.getCachedChannelById(channelId);
    }

    @Override
    public Channel getCachedChannelByName(String channelName) {
        return channelCache.getCachedChannelByName(channelName);
    }

    @Override
    public String getCachedDestinationName(String connectorId) {
        return channelCache.getCachedDestinationName(connectorId);
    }

    @Override
    public List<String> getCachedChannelIds() {
        return channelCache.getCachedChannelIds();
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
     * before performing its function. Because of this, no two methods in this cache should operate
     * simultaneously.
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
                        Channel filterChannel = new Channel();
                        filterChannel.setId(channelId);
                        List<Channel> channelList = getChannel(filterChannel);

                        if (!channelList.isEmpty()) {
                            Channel channel = channelList.get(0);
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
                             * The channel must have been removed from the database after the
                             * initial revision query, remove it from the cache if it already
                             * existed.
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

        private List<String> getCachedChannelIds() {
            refreshCache();

            return new ArrayList<String>(channelCacheById.keySet());
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
