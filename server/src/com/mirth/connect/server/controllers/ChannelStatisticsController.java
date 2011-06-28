/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import com.mirth.connect.model.ChannelStatistics;

/**
 * The StatisticsContoller provides access to channel statistics.
 * 
 * @author GeraldB
 * 
 */
public abstract class ChannelStatisticsController extends Controller {
    public static ChannelStatisticsController getInstance() {
        return ControllerFactory.getFactory().createChannelStatisticsController();
    }
    
    public abstract void startUpdaterThread();
    
    public abstract void stopUpdaterThread();

    public abstract void loadCache();

    public abstract ChannelStatistics getStatistics(String channelId);

    public abstract void createStatistics(String channelId);

    public abstract void clearStatistics(String channelId, boolean received, boolean filtered, boolean queued, boolean sent, boolean errored, boolean alerted) throws ControllerException;

    public abstract boolean checkIfStatisticsExist(String channelId);

    public abstract void incrementReceivedCount(String channelId);

    public abstract void incrementSentCount(String channelId);

    public abstract void incrementFilteredCount(String channelId);

    public abstract void incrementErrorCount(String channelId);

    public abstract void incrementQueuedCount(String channelId);

    public abstract void incrementAlertedCount(String channelId);

    public abstract void decrementQueuedCount(String channelId);

    public abstract void decrementErrorCount(String channelId);

    public abstract void decrementFilteredCount(String channelId);

    public abstract void decrementSentCount(String channelId);

    public abstract void decrementReceivedCount(String channelId);

    public abstract void updateAllStatistics();
}
