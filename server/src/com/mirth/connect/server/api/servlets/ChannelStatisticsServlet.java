/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections.CollectionUtils;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.ChannelStatisticsServletInterface;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelStatisticsServlet extends MirthServlet implements ChannelStatisticsServletInterface {

    private static final ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private static final EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    public ChannelStatisticsServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public List<ChannelStatistics> getStatistics(Set<String> channelIds, boolean includeUndeployed, Set<Integer> includeMetadataIds, Set<Integer> excludeMetadataIds, boolean aggregateStats) {
        if (CollectionUtils.isNotEmpty(includeMetadataIds) && CollectionUtils.isNotEmpty(excludeMetadataIds)) {
            throw new MirthApiException(Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN_TYPE).entity("Cannot include and exclude connectors in one call").build());
        }

        List<ChannelStatistics> stats = engineController.getChannelStatisticsList(channelIds, includeUndeployed, includeMetadataIds, excludeMetadataIds);

        if (aggregateStats) {
            ChannelStatistics totalStatistics = new ChannelStatistics();

            long errorCount = 0, filteredCount = 0, receivedCount = 0, sentCount = 0,
                    queuedCount = 0;

            for (ChannelStatistics channelStats : stats) {
                receivedCount += channelStats.getReceived();
                errorCount += channelStats.getError();
                sentCount += channelStats.getSent();
                filteredCount += channelStats.getFiltered();
                queuedCount += channelStats.getQueued();
            }
            totalStatistics.setServerId(configurationController.getServerId());
            totalStatistics.setReceived(receivedCount);
            totalStatistics.setError(errorCount);
            totalStatistics.setSent(sentCount);
            totalStatistics.setFiltered(filteredCount);
            totalStatistics.setQueued(queuedCount);

            stats = new ArrayList<ChannelStatistics>();
            stats.add(totalStatistics);
        }

        return stats;
    }

    @Override
    public List<ChannelStatistics> getStatisticsPost(Set<String> channelIds, boolean includeUndeployed, Set<Integer> includeMetadataIds, Set<Integer> excludeMetadataIds, boolean aggregateStats) {
        return getStatistics(channelIds, includeUndeployed, includeMetadataIds, excludeMetadataIds, aggregateStats);
    }

    @Override
    @CheckAuthorizedChannelId
    public ChannelStatistics getStatistics(String channelId) {
        ChannelStatistics channelStatistics = null;
        List<ChannelStatistics> channelStatisticsList = engineController.getChannelStatisticsList(new HashSet<String>(Arrays.asList(channelId)), true);

        if (CollectionUtils.isNotEmpty(channelStatisticsList)) {
            channelStatistics = channelStatisticsList.get(0);
        } else {
            channelStatistics = new ChannelStatistics();
            channelStatistics.setChannelId(channelId);
            channelStatistics.setServerId(configurationController.getServerId());
        }
        return channelStatistics;
    }

    @Override
    public void clearStatistics(Map<String, List<Integer>> channelConnectorMap, boolean received, boolean filtered, boolean sent, boolean error) {
        Set<Status> statusesToClear = new HashSet<Status>();

        if (received) {
            statusesToClear.add(Status.RECEIVED);
        }
        if (filtered) {
            statusesToClear.add(Status.FILTERED);
        }
        if (sent) {
            statusesToClear.add(Status.SENT);
        }
        if (error) {
            statusesToClear.add(Status.ERROR);
        }

        channelController.resetStatistics(channelConnectorMap, statusesToClear);
    }

    @Override
    public void clearAllStatistics() {
        channelController.resetAllStatistics();
    }
}