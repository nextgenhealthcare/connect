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

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;

public abstract class ChannelController extends Controller {
    public static ChannelController getInstance() {
        return ControllerFactory.getFactory().createChannelController();
    }
    
    public abstract List<Channel> getChannel(Channel channel) throws ControllerException;

    public abstract List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException;

    public abstract boolean updateChannel(Channel channel, boolean override) throws ControllerException;

    public abstract void removeChannel(Channel channel) throws ControllerException;
    
    // channel cache
    public abstract void loadChannelCache();
    
    public abstract Map<String, Channel> getChannelCache();

    public abstract void refreshChannelCache(List<Channel> channels) throws ControllerException;

    // utility methods
    public abstract String getChannelId(String channelName);
    
    public abstract String getChannelName(String channelId);

    public abstract String getDestinationName(String id);

    public abstract String getConnectorId(String channelId, String connectorName) throws Exception;
}
