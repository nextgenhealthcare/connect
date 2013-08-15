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
import java.util.Set;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.PauseException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.ServerEventContext;

public interface EngineController {
    public void startEngine() throws StartException, StopException, ControllerException, InterruptedException;

    public void stopEngine() throws StopException, InterruptedException;

    public boolean isRunning();

    public void deployChannels(Set<String> channelIds, ServerEventContext context);

    public void undeployChannels(Set<String> channelIds, ServerEventContext context);

    public void redeployAllChannels() throws StopException, StartException, InterruptedException;

    public boolean isDeployed(String channelId);

    public Channel getDeployedChannel(String channelId);

    public DispatchResult dispatchRawMessage(String channelId, RawMessage rawMessage) throws ChannelException;

    public void startChannel(String channelId) throws StartException, StopException;

    /**
     * Stops the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public void stopChannel(String channelId) throws StopException;

    public void haltChannel(String channelId) throws HaltException;

    /**
     * Pauses the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public void pauseChannel(String channelId) throws PauseException;

    /**
     * Resumes the channel with the specified id.
     * 
     * @param channelId
     * @throws StopException
     * @throws ControllerException
     */
    public void resumeChannel(String channelId) throws StartException, StopException;

    public void startConnector(String channelId, Integer metaDataId) throws StartException;

    public void stopConnector(String channelId, Integer metaDataId) throws StopException;

    /**
     * Returns a list of DashboardStatus objects representing the running channels.
     * 
     * @return
     * @throws ControllerException
     */
    public List<DashboardStatus> getChannelStatusList();

    /**
     * Returns a DashboardStatus object representing a running channel.
     */
    public DashboardStatus getChannelStatus(String channelId);

    /**
     * Returns a list of deployed channel ids.
     * 
     * @return
     * @throws ControllerException
     */
    public Set<String> getDeployedIds();
}