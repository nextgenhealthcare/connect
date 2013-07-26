/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.HashSet;
import java.util.Set;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelUtil {

    private static EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private static ChannelController channelController = ControllerFactory.getFactory().createChannelController();

    public static void startChannel(String channelId) throws Exception {
        engineController.startChannel(convertId(channelId));
    }

    public static void stopChannel(String channelId) throws Exception {
        engineController.stopChannel(convertId(channelId));
    }

    public static void pauseChannel(String channelId) throws Exception {
        engineController.pauseChannel(convertId(channelId));
    }

    public static void resumeChannel(String channelId) throws Exception {
        engineController.resumeChannel(convertId(channelId));
    }

    public static void haltChannel(String channelId) throws Exception {
        engineController.haltChannel(convertId(channelId));
    }

    public static ChannelState getChannelState(String channelId) {
        DashboardStatus dashboardStatus = getDashboardStatus(convertId(channelId), null);
        return dashboardStatus != null ? convertChannelState(dashboardStatus.getState()) : null;
    }

    public static void deployChannel(String channelId) {
        Set<String> channelIds = new HashSet<String>();
        channelIds.add(convertId(channelId));
        engineController.deployChannels(channelIds, null);
    }

    public static void undeployChannel(String channelId) throws Exception {
        Set<String> channelIds = new HashSet<String>();
        channelIds.add(convertId(channelId));
        engineController.undeployChannels(channelIds, null);
    }

    public static boolean isChannelDeployed(String channelId) {
        return engineController.getDeployedIds().contains(convertId(channelId));
    }

    public void startConnector(String channelId, Integer metaDataId) throws Exception {
        engineController.startConnector(channelId, metaDataId);
    }

    public void stopConnector(String channelId, Integer metaDataId) throws Exception {
        engineController.stopConnector(channelId, metaDataId);
    }

    public static ChannelState getConnectorState(String channelId, Number metaDataId) {
        DashboardStatus dashboardStatus = getDashboardStatus(convertId(channelId), metaDataId);
        return dashboardStatus != null ? convertChannelState(dashboardStatus.getState()) : null;
    }

    public static Long getReceivedCount(String channelId) {
        return getReceivedCount(channelId, null);
    }

    public static Long getReceivedCount(String channelId, Number metaDataId) {
        return getStatisticByStatus(channelId, metaDataId, Status.RECEIVED);
    }

    public static Long getFilteredCount(String channelId) {
        return getFilteredCount(channelId, null);
    }

    public static Long getFilteredCount(String channelId, Number metaDataId) {
        return getStatisticByStatus(channelId, metaDataId, Status.FILTERED);
    }

    public static Long getQueuedCount(String channelId) {
        return getQueuedCount(channelId, null);
    }

    public static Long getQueuedCount(String channelId, Number metaDataId) {
        return getStatisticByStatus(channelId, metaDataId, Status.QUEUED);
    }

    public static Long getSentCount(String channelId) {
        return getSentCount(channelId, null);
    }

    public static Long getSentCount(String channelId, Number metaDataId) {
        return getStatisticByStatus(channelId, metaDataId, Status.SENT);
    }

    public static Long getErrorCount(String channelId) {
        return getErrorCount(channelId, null);
    }

    public static Long getErrorCount(String channelId, Number metaDataId) {
        return getStatisticByStatus(channelId, metaDataId, Status.ERROR);
    }

    private static String convertId(String channelId) {
        if (!channelController.getChannelIds().contains(channelId)) {
            // Assume the name was passed in instead, check the deployed cache first
            Channel channel = channelController.getDeployedChannelByName(channelId);
            if (channel != null) {
                return channel.getId();
            }

            // Check the regular cache second
            channel = channelController.getChannelByName(channelId);
            if (channel != null) {
                return channel.getId();
            }
        }

        return channelId;
    }

    private static DashboardStatus getDashboardStatus(String channelId, Number metaDataId) {
        for (DashboardStatus dashboardStatus : engineController.getChannelStatusList()) {
            if (dashboardStatus.getChannelId().equals(channelId)) {
                if (metaDataId == null) {
                    return dashboardStatus;
                } else {
                    for (DashboardStatus childStatus : dashboardStatus.getChildStatuses()) {
                        if (childStatus.getMetaDataId().equals(toInteger(metaDataId))) {
                            return childStatus;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static Long getStatisticByStatus(String channelId, Number metaDataId, Status status) {
        DashboardStatus dashboardStatus = getDashboardStatus(convertId(channelId), toInteger(metaDataId));
        if (dashboardStatus != null) {
            if (status == Status.QUEUED) {
                return dashboardStatus.getQueued();
            } else {
                return dashboardStatus.getStatistics().get(status);
            }
        }
        return null;
    }

    private static Integer toInteger(Number number) {
        if (number != null) {
            return number.intValue();
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