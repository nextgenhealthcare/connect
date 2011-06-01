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

import org.apache.log4j.Logger;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.DeployedChannelInfo;

public abstract class ChannelController extends Controller {

    private Logger logger = Logger.getLogger(this.getClass());

    public static ChannelController getInstance() {
        return ControllerFactory.getFactory().createChannelController();
    }

    public abstract List<Channel> getChannel(Channel channel) throws ControllerException;

    public abstract List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException;

    public abstract boolean updateChannel(Channel channel, boolean override) throws ControllerException;

    public abstract void removeChannel(Channel channel) throws ControllerException;

    // channel cache

    public abstract void loadCache();

    public abstract Channel getCachedChannelById(String channelId);

    public abstract Channel getCachedChannelByName(String channelName);

    public abstract String getCachedDestinationName(String connectorId);

    // deployed channel cache

    public abstract void putDeployedChannelInCache(Channel channel);

    public abstract void removeDeployedChannelFromCache(String channelId);

    public abstract Channel getDeployedChannelById(String channelId);

    public abstract Channel getDeployedChannelByName(String channelName);

    public abstract DeployedChannelInfo getDeployedChannelInfoById(String channelId);

    public abstract String getDeployedDestinationName(String connectorId);

    public abstract String getDeployedConnectorId(String channelId, String connectorName) throws Exception;

    @Deprecated
    // TODO: Remove in 2.2
    public String getChannelName(String channelId) {
        logger.error("The getChannelName(channelId) method is deprecated and will soon be removed. Please use getDeployedChannelById(channelId).getName()");
        return getDeployedChannelById(channelId).getName();
    }

    @Deprecated
    // TODO: Remove in 2.2
    public String getChannelId(String channelName) {
        logger.error("The getChannelId(channelName) method is deprecated and will soon be removed. Please use getDeployedChannelByName(channelName).getId()");
        return getDeployedChannelByName(channelName).getId();
    }
}
