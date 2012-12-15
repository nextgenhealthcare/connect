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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.plugins.ChannelPlugin;
import com.mirth.connect.server.sqlmap.extensions.MapResultHandler;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultChannelController extends ChannelController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    // channel cache
    private static Map<String, Channel> channelCacheById = new HashMap<String, Channel>();
    private static Map<String, Channel> channelCacheByName = new HashMap<String, Channel>();

    // deployed channel cache
    private static Map<String, Channel> deployedChannelCacheById = new HashMap<String, Channel>();
    private static Map<String, Channel> deployedChannelCacheByName = new HashMap<String, Channel>();

    private static Map<String, DeployedChannelInfo> deployedChannelInfoCache = new HashMap<String, DeployedChannelInfo>();

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

    public List<Channel> getChannel(Channel channel) throws ControllerException {
        logger.debug("getting channel");

        try {
            List<Channel> channels = SqlConfig.getSqlSessionManager().selectList("Channel.getChannel", channel);
            
            ChannelTagResultHandler resultHandler = new ChannelTagResultHandler();
            SqlConfig.getSqlSessionManager().select("Channel.getChannelTags", channel, resultHandler);
            Map<String, Set<String>> tagMap = resultHandler.getTagMap();
            
            for (Channel currentChannel : channels) {
                Set<String> channelTags = tagMap.get(currentChannel.getId());
                
                if (channelTags != null) {
                    currentChannel.setTags(channelTags);
                }
            }
            
            return channels;
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }
    
    private class ChannelTagResultHandler implements ResultHandler {
        private Map<String, Set<String>> tags = new HashMap<String, Set<String>>();

        public Map<String, Set<String>> getTagMap() {
            return tags;
        }

        @Override
        public void handleResult(ResultContext context) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> result = (Map<Object, Object>) context.getResultObject();
            
            String channelId = (String) result.get("channel_id");
            String tag = (String) result.get("tag");
            
            Set<String> channelTags = tags.get(channelId);
            
            if (channelTags == null) {
                channelTags = new LinkedHashSet<String>();
                tags.put(channelId, channelTags);
            }
            
            channelTags.add(tag);
        }
    }

    public List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException {
        logger.debug("getting channel summary");
        List<ChannelSummary> channelSummaries = new ArrayList<ChannelSummary>();

        try {
            MapResultHandler<String, Integer> mapResultHandler = new MapResultHandler<String,Integer>("id", "revision");
            SqlConfig.getSqlSessionManager().select("Channel.getChannelRevision", mapResultHandler);              
            Map<String, Integer> serverChannels = mapResultHandler.getMap();

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
        } catch (PersistenceException e) {
            throw new ControllerException(e);
        }
    }
    
    @Override
    public Set<String> getChannelTags() {
        return getChannelTags(null);
    }
    
    @Override
    public Set<String> getChannelTags(Set<String> channelIds) {
        logger.debug("getting channel tags");
        List<String> tags = SqlConfig.getSqlSessionManager().selectList("Channel.getAllChannelTags", channelIds);
        return new LinkedHashSet<String>(tags);
    }

    public boolean updateChannel(Channel channel, ServerEventContext context, boolean override) throws ControllerException {
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
            
            // Use the larger nextMetaDataId to ensure a metadata ID will never be reused if an older version of a channel is imported or saved.
            channel.setNextMetaDataId(Math.max(matchingChannels.get(0).getNextMetaDataId(), channel.getNextMetaDataId()));
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

            channelFilter = new Channel();
            channelFilter.setId(channel.getId());

            // Put the new channel in the database
            if (getChannel(channelFilter).isEmpty()) {
                logger.debug("adding channel");
                SqlConfig.getSqlSessionManager().insert("Channel.insertChannel", channel);
            } else {
                logger.debug("updating channel");
                SqlConfig.getSqlSessionManager().update("Channel.updateChannel", channel);
            }
            
            // update channel tags
            logger.debug("updating channel tags");
            SqlConfig.getSqlSessionManager().update("Channel.deleteChannelTags", channel.getId());
            
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("channelId", channel.getId());
            
            for (String tag : channel.getTags()) {
                params.put("tag", tag);
                SqlConfig.getSqlSessionManager().update("Channel.insertChannelTag", params);
            }

            // Update the channel in the channelCache
            putChannelInCache(channel);

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
    public void removeChannel(Channel channel, ServerEventContext context) throws ControllerException {
        logger.debug("removing channel");

        if ((channel != null) && ControllerFactory.getFactory().createEngineController().isDeployed(channel.getId())) {
            logger.warn("Cannot remove deployed channel.");
            return;
        }

        try {
            if (channel != null) {
                removeChannelFromCache(channel.getId());
            } else {
                clearChannelCache();
            }
            
            //TODO combine and organize these.
            // Delete the "d_" tables and the channel record from "d_channels"
            com.mirth.connect.donkey.server.controllers.ChannelController.getInstance().removeChannel(channel.getId());
            // Delete the channel record from the "channel" table
            SqlConfig.getSqlSessionManager().delete("Channel.deleteChannel", channel);

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

    // ---------- CHANNEL CACHE ----------

    public void loadCache() {
        try {
            for (Channel channel : getChannel(null)) {
                putChannelInCache(channel);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public Channel getCachedChannelById(String channelId) {
        return channelCacheById.get(channelId);
    }

    public Channel getCachedChannelByName(String channelName) {
        return channelCacheByName.get(channelName);
    }

    private void clearChannelCache() {
        channelCacheById.clear();
        channelCacheByName.clear();
    }

    private void putChannelInCache(Channel channel) {
        channelCacheById.put(channel.getId(), channel);
        channelCacheByName.put(channel.getName(), channel);
    }

    private void removeChannelFromCache(String channelId) {
        String channelName = getCachedChannelById(channelId).getName();
        channelCacheById.remove(channelId);
        channelCacheByName.remove(channelName);
    }

    // ---------- DEPLOYED CHANNEL CACHE ----------

    public void putDeployedChannelInCache(Channel channel) {
        DeployedChannelInfo deployedChannelInfo = new DeployedChannelInfo();
        deployedChannelInfo.setDeployedDate(Calendar.getInstance());
        deployedChannelInfo.setDeployedRevision(channel.getRevision());
        deployedChannelInfoCache.put(channel.getId(), deployedChannelInfo);

        deployedChannelCacheById.put(channel.getId(), channel);
        deployedChannelCacheByName.put(channel.getName(), channel);
    }

    public void removeDeployedChannelFromCache(String channelId) {
        deployedChannelInfoCache.remove(channelId);

        String channelName = getDeployedChannelById(channelId).getName();
        deployedChannelCacheById.remove(channelId);
        deployedChannelCacheByName.remove(channelName);
    }

    public Channel getDeployedChannelById(String channelId) {
        return deployedChannelCacheById.get(channelId);
    }

    public Channel getDeployedChannelByName(String channelName) {
        return deployedChannelCacheByName.get(channelName);
    }

    public DeployedChannelInfo getDeployedChannelInfoById(String channelId) {
        return deployedChannelInfoCache.get(channelId);
    }

    public String getDeployedDestinationName(String id) {
        // String format: channelid_destination_index
        String destinationName = id;
        // if we can't parse the name, just use the id
        String channelId = id.substring(0, id.indexOf('_'));
        String strIndex = id.substring(id.indexOf("destination_") + 12, id.indexOf("_connector"));
        int index = Integer.parseInt(strIndex) - 1;
        Channel channel = getDeployedChannelById(channelId);

        if (channel != null) {
            if (index < channel.getDestinationConnectors().size())
                destinationName = channel.getDestinationConnectors().get(index).getName();
        }

        return destinationName;
    }

    public String getCachedDestinationName(String id) {
        // String format: channelid_destination_index
        String destinationName = id;
        // if we can't parse the name, just use the id
        String channelId = id.substring(0, id.indexOf('_'));
        String strIndex = id.substring(id.indexOf("destination_") + 12, id.indexOf("_connector"));
        int index = Integer.parseInt(strIndex) - 1;
        Channel channel = getCachedChannelById(channelId);

        if (channel != null) {
            if (index < channel.getDestinationConnectors().size())
                destinationName = channel.getDestinationConnectors().get(index).getName();
        }

        return destinationName;
    }
    
    public List<String> getCachedChannelIds() {
        return new ArrayList<String>(channelCacheById.keySet());
    }

    public String getDeployedConnectorId(String channelId, String connectorName) throws Exception {
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
    }
}
