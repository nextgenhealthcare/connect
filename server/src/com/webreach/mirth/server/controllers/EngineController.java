package com.webreach.mirth.server.controllers;

import java.util.List;
import java.util.Map;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ConnectorMetaData;

public interface EngineController {

    public void resetConfiguration() throws Exception;

    public void deployChannels(List<Channel> channels, Map<String, ConnectorMetaData> transports) throws Exception;

    public void unregisterChannel(String channelId) throws Exception;

    public boolean isChannelRegistered(String channelId) throws Exception;

    public void start() throws Exception;

    public void stop() throws Exception;

}