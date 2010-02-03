/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.server.util.ChannelStatisticsCache;
import com.webreach.mirth.server.util.SqlConfig;

/**
 * The StatisticsContoller provides access to channel statistics.
 * 
 * @author GeraldB
 * 
 */
public class DefaultChannelStatisticsController extends ChannelStatisticsController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelStatisticsCache statsCache;
    private StatisticsUpdater statsUpdater = null;
    private Thread updaterThread = null;
    private boolean statsChanged = false;
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private static boolean running = true;

    private static DefaultChannelStatisticsController instance = null;

    private DefaultChannelStatisticsController() {

    }

    public static ChannelStatisticsController create() {
        synchronized (DefaultChannelStatisticsController.class) {
            if (instance == null) {
                instance = new DefaultChannelStatisticsController();
            }

            return instance;
        }
    }

    public void shutdown() {
        running = false;
    }

    private class StatisticsUpdater implements Runnable {
        public void run() {
            try {
                running = true;

                while (running) {
                    Thread.sleep(1000);
                    updateAllStatistics();
                }
            } catch (InterruptedException e) {

            }
        }
    }

    public void start() {
        logger.debug("initialzing statistics controller");

        statsCache = ChannelStatisticsCache.getInstance();
        reloadLocalCache();

        statsUpdater = new StatisticsUpdater();
        updaterThread = new Thread(statsUpdater);
        updaterThread.start();
    }
    
    public void reloadLocalCache() {
        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("serverId", configurationController.getServerId());
            statsCache.setCache(SqlConfig.getSqlMapClient().queryForMap("Statistic.getStatistics", parameterMap, "channelId"));
        } catch (SQLException e) {
            logger.error("Could not initialize channel statistics.", e);
        }
    }

    public void createStatistics(String channelId) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("serverId", configurationController.getServerId());
        parameterMap.put("channelId", channelId);

        try {
            SqlConfig.getSqlMapClient().insert("Statistic.createStatistics", parameterMap);
        } catch (SQLException e) {
            logger.warn("could not update statistics");
        }
    }

    public boolean checkIfStatisticsExist(String channelId) {
        try {

            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("serverId", configurationController.getServerId());
            parameterMap.put("channelId", channelId);

            Map<String, ChannelStatistics> tempStats = SqlConfig.getSqlMapClient().queryForMap("Statistic.getStatistics", parameterMap, "channelId");
            
            if (tempStats != null && tempStats.size() > 0) {
                return true;
            }
        } catch (SQLException e) {
            logger.error("Could not get channel statistics.", e);
        }
        
        return false;
    }

    public ChannelStatistics getStatistics(String channelId) {
        return statsCache.getCache().get(channelId);
    }

    public synchronized void incrementReceivedCount(String channelId) {
        statsCache.getCache().get(channelId).setReceived(statsCache.getCache().get(channelId).getReceived() + 1);
        statsChanged = true;
    }

    public synchronized void incrementSentCount(String channelId) {
        statsCache.getCache().get(channelId).setSent(statsCache.getCache().get(channelId).getSent() + 1);
        statsChanged = true;
    }

    public synchronized void incrementFilteredCount(String channelId) {
        statsCache.getCache().get(channelId).setFiltered(statsCache.getCache().get(channelId).getFiltered() + 1);
        statsChanged = true;
    }

    public synchronized void incrementErrorCount(String channelId) {
        statsCache.getCache().get(channelId).setError(statsCache.getCache().get(channelId).getError() + 1);
        statsChanged = true;
    }

    public synchronized void incrementQueuedCount(String channelId) {
        statsCache.getCache().get(channelId).setQueued(statsCache.getCache().get(channelId).getQueued() + 1);
        statsChanged = true;
    }

    public synchronized void incrementAlertedCount(String channelId) {
        statsCache.getCache().get(channelId).setAlerted(statsCache.getCache().get(channelId).getAlerted() + 1);
        statsChanged = true;
    }

    public synchronized void decrementQueuedCount(String channelId) {
        if (statsCache.getCache().get(channelId).getQueued() > 0) {
            statsCache.getCache().get(channelId).setQueued(statsCache.getCache().get(channelId).getQueued() - 1);
            statsChanged = true;
        }
    }

    public synchronized void decrementErrorCount(String channelId) {
        if (statsCache.getCache().get(channelId).getError() > 0) {
            statsCache.getCache().get(channelId).setError(statsCache.getCache().get(channelId).getError() - 1);
            statsChanged = true;
        }
    }

    public synchronized void decrementFilteredCount(String channelId) {
        if (statsCache.getCache().get(channelId).getFiltered() > 0) {
            statsCache.getCache().get(channelId).setFiltered(statsCache.getCache().get(channelId).getFiltered() - 1);
            statsChanged = true;
        }
    }

    public synchronized void decrementSentCount(String channelId) {
        if (statsCache.getCache().get(channelId).getSent() > 0) {
            statsCache.getCache().get(channelId).setSent(statsCache.getCache().get(channelId).getSent() - 1);
            statsChanged = true;
        }
    }

    public synchronized void decrementReceivedCount(String channelId) {
        if (statsCache.getCache().get(channelId).getReceived() > 0) {
            statsCache.getCache().get(channelId).setReceived(statsCache.getCache().get(channelId).getReceived() - 1);
            statsChanged = true;
        }
    }

    private void updateStatistics(String channelId) {
        try {
            SqlConfig.getSqlMapClient().update("Statistic.updateStatistics", statsCache.getCache().get(channelId));
        } catch (SQLException e) {
            logger.warn("could not update statistics");
        }
    }

    public void updateAllStatistics() {
        if (statsChanged) {
            for (ChannelStatistics stats : statsCache.getCache().values()) {
                updateStatistics(stats.getChannelId());
            }

            statsChanged = false;
        }
    }

    public void clearStatistics(String channelId, boolean received, boolean filtered, boolean queued, boolean sent, boolean errored, boolean alerted) throws ControllerException {
        if (received)
            statsCache.getCache().get(channelId).setReceived(0);
        if (filtered)
            statsCache.getCache().get(channelId).setFiltered(0);
        if (queued)
            statsCache.getCache().get(channelId).setQueued(0);
        if (sent)
            statsCache.getCache().get(channelId).setSent(0);
        if (errored)
            statsCache.getCache().get(channelId).setError(0);
        if (alerted)
            statsCache.getCache().get(channelId).setAlerted(0);

        updateStatistics(channelId);
    }
}
