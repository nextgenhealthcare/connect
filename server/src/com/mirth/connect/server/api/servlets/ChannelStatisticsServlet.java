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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

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

    public ChannelStatisticsServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public List<ChannelStatistics> getAllStatistics() {
        List<ChannelStatistics> statistics = new ArrayList<ChannelStatistics>();
        Set<String> channelIds = engineController.getDeployedIds();

        for (String channelId : channelIds) {
            Map<Status, Long> map = channelController.getStatistics().getConnectorStats(channelId, null);

            ChannelStatistics channelStatistics = new ChannelStatistics();
            channelStatistics.setChannelId(channelId);
            channelStatistics.setError(map.get(Status.ERROR));
            channelStatistics.setFiltered(map.get(Status.FILTERED));
            channelStatistics.setReceived(map.get(Status.RECEIVED));
            channelStatistics.setSent(map.get(Status.SENT));

            statistics.add(channelStatistics);
        }

        return statistics;
    }

    @Override
    @CheckAuthorizedChannelId
    public ChannelStatistics getStatistics(String channelId) {
        Map<Status, Long> map = channelController.getStatistics().getConnectorStats(channelId, null);

        ChannelStatistics channelStatistics = new ChannelStatistics();
        channelStatistics.setChannelId(channelId);
        channelStatistics.setServerId(ConfigurationController.getInstance().getServerId());
        channelStatistics.setError(map.get(Status.ERROR));
        channelStatistics.setFiltered(map.get(Status.FILTERED));
        channelStatistics.setReceived(map.get(Status.RECEIVED));
        channelStatistics.setSent(map.get(Status.SENT));

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