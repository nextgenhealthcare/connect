/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.ServerEventContext;

public abstract class ChannelController extends Controller {

    private Logger logger = LogManager.getLogger(this.getClass());

    public static ChannelController getInstance() {
        return ControllerFactory.getFactory().createChannelController();
    }

    public abstract List<Channel> getChannels(Set<String> channelIds);

    public abstract Channel getChannelById(String channelId);

    public abstract Channel getChannelByName(String channelName);

    public abstract String getDestinationName(String channelId, int metaDataId);

    public abstract Set<String> getChannelIds();

    public abstract Set<String> getChannelNames();

    public abstract List<ChannelSummary> getChannelSummary(Map<String, ChannelHeader> cachedChannels, boolean ignoreNewChannels) throws ControllerException;

    public abstract void setChannelEnabled(Set<String> channelIds, ServerEventContext context, boolean enabled) throws ControllerException;

    public abstract void setChannelInitialState(Set<String> channelIds, ServerEventContext context, DeployedState initialState) throws ControllerException;

    public abstract boolean updateChannel(Channel channel, ServerEventContext context, boolean override) throws ControllerException;

    public abstract void removeChannel(Channel channel, ServerEventContext context) throws ControllerException;

    public abstract Map<String, Integer> getChannelRevisions() throws ControllerException;

    public abstract Map<Integer, String> getConnectorNames(String channelId);

    public abstract List<MetaDataColumn> getMetaDataColumns(String channelId);

    // deployed channel cache

    public abstract void putDeployedChannelInCache(Channel channel);

    public abstract void removeDeployedChannelFromCache(String channelId);

    public abstract Channel getDeployedChannelById(String channelId);

    public abstract Channel getDeployedChannelByName(String channelName);

    public abstract DeployedChannelInfo getDeployedChannelInfoById(String channelId);

    public abstract String getDeployedDestinationName(String channelId, int metaDataId);

    public abstract Statistics getStatistics();

    public abstract Statistics getTotalStatistics();

    public abstract Statistics getStatisticsFromStorage(String serverId);

    public abstract Statistics getTotalStatisticsFromStorage(String serverId);

    public abstract int getConnectorMessageCount(String channelId, String serverId, int metaDataId, Status status);

    public abstract void resetStatistics(Map<String, List<Integer>> channelConnectorMap, Set<Status> statuses);

    public abstract void resetAllStatistics();

    public abstract List<Channel> getDeployedChannels(Set<String> channelIds);

    public abstract List<ChannelGroup> getChannelGroups(Set<String> channelGroupIds);

    public abstract boolean updateChannelGroups(Set<ChannelGroup> channelGroups, Set<String> removedChannelGroupIds, boolean override) throws ControllerException;
}
