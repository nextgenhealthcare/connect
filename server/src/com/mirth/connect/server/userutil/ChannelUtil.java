/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Collections;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelUtil {

    private static EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private static ChannelController channelController = ControllerFactory.getFactory().createChannelController();

    public static void startChannel(String channelIdOrName) throws Exception {
        engineController.startChannel(convertId(channelIdOrName));
    }

    public static void stopChannel(String channelIdOrName) throws Exception {
        engineController.stopChannel(convertId(channelIdOrName));
    }

    public static void pauseChannel(String channelIdOrName) throws Exception {
        engineController.pauseChannel(convertId(channelIdOrName));
    }

    public static void resumeChannel(String channelIdOrName) throws Exception {
        engineController.resumeChannel(convertId(channelIdOrName));
    }

    public static void haltChannel(String channelIdOrName) throws Exception {
        engineController.haltChannel(convertId(channelIdOrName));
    }

    public static ChannelState getChannelState(String channelIdOrName) {
        DashboardStatus dashboardStatus = getDashboardStatus(channelIdOrName, null);
        return dashboardStatus != null ? convertChannelState(dashboardStatus.getState()) : null;
    }

    public static void deployChannel(String channelIdOrName) {
        engineController.deployChannels(Collections.singleton(convertId(channelIdOrName)), null);
    }

    public static void undeployChannel(String channelIdOrName) {
        engineController.undeployChannels(Collections.singleton(convertId(channelIdOrName)), null);
    }

    public static boolean isChannelDeployed(String channelIdOrName) {
        return engineController.getDeployedIds().contains(convertId(channelIdOrName));
    }

    public void startConnector(String channelIdOrName, Integer metaDataId) throws Exception {
        engineController.startConnector(convertId(channelIdOrName), metaDataId);
    }

    public void stopConnector(String channelIdOrName, Integer metaDataId) throws Exception {
        engineController.stopConnector(convertId(channelIdOrName), metaDataId);
    }

    public static ChannelState getConnectorState(String channelIdOrName, Number metaDataId) {
        DashboardStatus dashboardStatus = getDashboardStatus(channelIdOrName, metaDataId);
        return dashboardStatus != null ? convertChannelState(dashboardStatus.getState()) : null;
    }

    public static Long getReceivedCount(String channelIdOrName) {
        return getReceivedCount(channelIdOrName, null);
    }

    public static Long getReceivedCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.RECEIVED);
    }

    public static Long getFilteredCount(String channelIdOrName) {
        return getFilteredCount(channelIdOrName, null);
    }

    public static Long getFilteredCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.FILTERED);
    }

    public static Long getQueuedCount(String channelIdOrName) {
        return getQueuedCount(channelIdOrName, null);
    }

    public static Long getQueuedCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.QUEUED);
    }

    public static Long getSentCount(String channelIdOrName) {
        return getSentCount(channelIdOrName, null);
    }

    public static Long getSentCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.SENT);
    }

    public static Long getErrorCount(String channelIdOrName) {
        return getErrorCount(channelIdOrName, null);
    }

    public static Long getErrorCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.ERROR);
    }

    private static String convertId(String channelIdOrName) {
        if (!channelController.getChannelIds().contains(channelIdOrName)) {
            // Assume the name was passed in instead, check the deployed cache first
            Channel channel = channelController.getDeployedChannelByName(channelIdOrName);
            if (channel != null) {
                return channel.getId();
            }

            // Check the regular cache second
            channel = channelController.getChannelByName(channelIdOrName);
            if (channel != null) {
                return channel.getId();
            }
        }

        return channelIdOrName;
    }

    private static DashboardStatus getDashboardStatus(String channelIdOrName, Number metaDataId) {
        DashboardStatus dashboardStatus = engineController.getChannelStatus(convertId(channelIdOrName));

        if (dashboardStatus != null) {
            if (metaDataId == null) {
                return dashboardStatus;
            } else {
                metaDataId = metaDataId.intValue();

                for (DashboardStatus childStatus : dashboardStatus.getChildStatuses()) {
                    if (childStatus.getMetaDataId().equals(metaDataId)) {
                        return childStatus;
                    }
                }
            }
        }

        return null;
    }

    private static Long getStatisticByStatus(String channelIdOrName, Number metaDataId, Status status) {
        DashboardStatus dashboardStatus = getDashboardStatus(channelIdOrName, metaDataId);
        if (dashboardStatus != null) {
            if (status == Status.QUEUED) {
                return dashboardStatus.getQueued();
            } else {
                return dashboardStatus.getStatistics().get(status);
            }
        }
        return null;
    }

    private static ChannelState convertChannelState(com.mirth.connect.donkey.model.channel.ChannelState channelState) {
        // @formatter:off
        switch (channelState) {
            case STARTING: return ChannelState.STARTING;
            case STARTED: return ChannelState.STARTED;
            case PAUSING: return ChannelState.PAUSING;
            case PAUSED: return ChannelState.PAUSED;
            case STOPPING: return ChannelState.STOPPING;
            case STOPPED: return ChannelState.STOPPED;
            default: return null;
        }
        // @formatter:on
    }
}