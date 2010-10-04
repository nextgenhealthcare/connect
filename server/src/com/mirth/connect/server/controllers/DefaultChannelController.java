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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.Connector;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.QueueUtil;

public class DefaultChannelController extends ChannelController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelStatisticsController statisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private ChannelStatusController channelStatusController = ControllerFactory.getFactory().createChannelStatusController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    // channel cache
    private static Map<String, Channel> channelCacheById = new HashMap<String, Channel>();
    private static Map<String, Channel> channelCacheByName = new HashMap<String, Channel>();

    // deployed channel cache
    private static Map<String, Channel> deployedChannelCacheById = new HashMap<String, Channel>();
    private static Map<String, Channel> deployedChannelCacheByName = new HashMap<String, Channel>();

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
            return SqlConfig.getSqlMapClient().queryForList("Channel.getChannel", channel);
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException {
        logger.debug("getting channel summary");
        List<ChannelSummary> channelSummaries = new ArrayList<ChannelSummary>();

        try {
            Map<String, Integer> serverChannels = SqlConfig.getSqlMapClient().queryForMap("Channel.getChannelRevision", null, "id", "revision");

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
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public boolean updateChannel(Channel channel, boolean override) throws ControllerException {
        // if it's not a new channel, and its version is different from the one
        // in the database, and override is not enabled
        if ((channel.getRevision() > 0) && !getChannel(channel).isEmpty() && (getChannel(channel).get(0).getRevision() != channel.getRevision()) && !override) {
            return false;
        } else {
            channel.setRevision(channel.getRevision() + 1);
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

                // If we are adding, then make sure the name isnt being used
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
                SqlConfig.getSqlMapClient().insert("Channel.insertChannel", channel);
            } else {
                logger.debug("updating channel");
                SqlConfig.getSqlMapClient().update("Channel.updateChannel", channel);
            }

            // Create statistics for this channel if they don't already exist
            if (!statisticsController.checkIfStatisticsExist(channel.getId())) {
                statisticsController.createStatistics(channel.getId());
            }

            // Update the channel in the channelCache
            putChannelInCache(channel);

            return true;
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Removes a channel. If the channel is NULL, then all channels are removed.
     * 
     * @param channel
     * @throws ControllerException
     */
    public void removeChannel(Channel channel) throws ControllerException {
        logger.debug("removing channel");

        if ((channel != null) && channelStatusController.getDeployedIds().contains(channel.getId())) {
            logger.warn("Cannot remove deployed channel.");
            return;
        }

        try {
            if (channel != null) {
                QueueUtil.getInstance().removeAllQueuesForChannel(channel);
                removeChannelFromCache(channel.getId());
            } else {
                QueueUtil.getInstance().removeAllQueues();
                clearChannelCache();
            }

            SqlConfig.getSqlMapClient().delete("Channel.deleteChannel", channel);

            if (DatabaseUtil.statementExists("Channel.vacuumChannelTable")) {
                SqlConfig.getSqlMapClient().update("Channel.vacuumChannelTable");
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
        deployedChannelCacheById.put(channel.getId(), channel);
        deployedChannelCacheByName.put(channel.getName(), channel);
    }

    public void removeDeployedChannelFromCache(String channelId) {
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
