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

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.server.channel.ChannelFuture;
import com.mirth.connect.server.channel.ChannelTask;
import com.mirth.connect.server.channel.ChannelTaskHandler;
import com.mirth.connect.server.mybatis.MessageSearchResult;

public interface EngineController {
    public void startEngine() throws StartException, StopException, ControllerException, InterruptedException;

    public void stopEngine() throws StopException, InterruptedException;

    public boolean isRunning();

    public List<ChannelFuture> submitTasks(List<ChannelTask> tasks, ChannelTaskHandler handler);

    public void startupDeploy() throws StartException, StopException, InterruptedException;

    public void deployChannels(Set<String> channelIds, ServerEventContext context, ChannelTaskHandler handler);

    public void undeployChannels(Set<String> channelIds, ServerEventContext context, ChannelTaskHandler handler);

    public void redeployAllChannels(ServerEventContext context, ChannelTaskHandler handler);

    public void startChannels(Set<String> channelIds, ChannelTaskHandler handler);

    public void stopChannels(Set<String> channelIds, ChannelTaskHandler handler);

    public void pauseChannels(Set<String> channelIds, ChannelTaskHandler handler);

    public void resumeChannels(Set<String> channelIds, ChannelTaskHandler handler);

    public void haltChannels(Set<String> channelIds, ChannelTaskHandler handler);

    public void removeChannels(Set<String> channelIds, ServerEventContext context, ChannelTaskHandler handler);

    public void startConnector(Map<String, List<Integer>> connectorInfo, ChannelTaskHandler handler);

    public void stopConnector(Map<String, List<Integer>> connectorInfo, ChannelTaskHandler handler);

    public void removeMessages(String channelId, Map<Long, MessageSearchResult> results, ChannelTaskHandler handler);

    public void removeAllMessages(Set<String> channelIds, boolean force, boolean clearStatistics, ChannelTaskHandler handler);

    public boolean isDeployed(String channelId);

    public Channel getDeployedChannel(String channelId);

    public DispatchResult dispatchRawMessage(String channelId, RawMessage rawMessage, boolean force, boolean canBatch) throws ChannelException, BatchMessageException;

    /**
     * Returns a list of DashboardStatus objects representing the running channels.
     * 
     * @return
     * @throws ControllerException
     */
    public List<DashboardStatus> getChannelStatusList();

    /**
     * Returns a list of DashboardStatus objects representing the running channels.
     * 
     * @return
     * @throws ControllerException
     */
    public List<DashboardStatus> getChannelStatusList(Set<String> channelIds);

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