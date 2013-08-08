/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.DeployedChannelInfo;
import com.mirth.connect.model.ServerEventContext;

public abstract class ChannelController extends Controller {

    private Logger logger = Logger.getLogger(this.getClass());

    public static ChannelController getInstance() {
        return ControllerFactory.getFactory().createChannelController();
    }

    public abstract List<Channel> getChannels(Set<String> channelIds);
    
    public abstract Channel getChannelById(String channelId);

    public abstract Channel getChannelByName(String channelName);

    public abstract String getDestinationName(String channelId, int metaDataId);
    
    public abstract Set<String> getChannelIds();
    
    public abstract List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException;

    public abstract boolean updateChannel(Channel channel, ServerEventContext context, boolean override) throws ControllerException;

    public abstract void removeChannel(Channel channel, ServerEventContext context) throws ControllerException;
    
    public abstract Map<String, Integer> getChannelRevisions() throws ControllerException;
    
    public abstract Set<String> getChannelTags(Set<String> channelIds);
    
    public abstract Map<Integer, String> getConnectorNames(String channelId);
    
    public abstract List<MetaDataColumn> getMetaDataColumns(String channelId);

    // deployed channel cache

    public abstract void putDeployedChannelInCache(Channel channel);

    public abstract void removeDeployedChannelFromCache(String channelId);

    public abstract Channel getDeployedChannelById(String channelId);

    public abstract Channel getDeployedChannelByName(String channelName);

    public abstract DeployedChannelInfo getDeployedChannelInfoById(String channelId);

    public abstract String getDeployedDestinationName(String connectorId);

    public abstract String getDeployedConnectorId(String channelId, String connectorName) throws Exception;
}
