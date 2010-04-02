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
import com.mirth.connect.model.ConnectorMetaData;

public interface EngineController {

    public void resetConfiguration() throws Exception;

    public void deployChannels(List<Channel> channels, Map<String, ConnectorMetaData> transports) throws Exception;

    public void unregisterChannel(String channelId) throws Exception;

    public boolean isChannelRegistered(String channelId) throws Exception;
    
    public List<String> getDeployedChannelIds() throws Exception;

    public void start() throws Exception;

    public void stop() throws Exception;

}