/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.log4j.Logger;

import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.server.util.SqlConfig;

@Deprecated
public class DefaultChannelStatisticsController {
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<String, ChannelStatistics> cache = null;
    private StatisticsUpdater statsUpdater = null;
    private Thread updaterThread = null;
    private boolean statsChanged = false;
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private static boolean running = true;

    private static DefaultChannelStatisticsController instance = null;

    private DefaultChannelStatisticsController() {

    }

    public static DefaultChannelStatisticsController create() {
        synchronized (DefaultChannelStatisticsController.class) {
            if (instance == null) {
                instance = new DefaultChannelStatisticsController();
            }

            return instance;
        }
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

    public void startUpdaterThread() {
        statsUpdater = new StatisticsUpdater();
        updaterThread = new Thread(statsUpdater);
        updaterThread.start();
    }

    public void stopUpdaterThread() {
        running = false;
    }
    
    public void loadCache() {
        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("serverId", configurationController.getServerId());
            cache = SqlConfig.getSqlSessionManager().selectMap("Statistic.getStatistics", parameterMap, "channelId");
        } catch (PersistenceException e) {
            logger.error("Could not initialize channel statistics.", e);
        }
    }

    public void createStatistics(String channelId) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("serverId", configurationController.getServerId());
        parameterMap.put("channelId", channelId);

        try {
            SqlConfig.getSqlSessionManager().insert("Statistic.createStatistics", parameterMap);
        } catch (PersistenceException e) {
            logger.warn("could not update statistics");
        }

        // add a new channel with 0 stats to the cache
        ChannelStatistics channelStatistics = new ChannelStatistics();
        channelStatistics.setServerId(configurationController.getServerId());
        channelStatistics.setChannelId(channelId);
        
        // synchronized with updateAllStatistics because it iterates through the cache map
        synchronized (cache) {
            cache.put(channelId, channelStatistics);
        }
    }

    public boolean checkIfStatisticsExist(String channelId) {
        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put("serverId", configurationController.getServerId());
            parameterMap.put("channelId", channelId);

            Map<String, ChannelStatistics> channelStatistics = SqlConfig.getSqlSessionManager().selectMap("Statistic.getStatistics", parameterMap, "channelId");

            if (channelStatistics != null && channelStatistics.size() > 0) {
                return true;
            }
        } catch (PersistenceException e) {
            logger.error("Could not get channel statistics.", e);
        }
        
        return false;
    }

    public ChannelStatistics getStatistics(String channelId) {
        return cache.get(channelId);
    }

    public synchronized void incrementReceivedCount(String channelId) {
        cache.get(channelId).setReceived(cache.get(channelId).getReceived() + 1);
        statsChanged = true;
    }

    public synchronized void incrementSentCount(String channelId) {
        cache.get(channelId).setSent(cache.get(channelId).getSent() + 1);
        statsChanged = true;
    }

    public synchronized void incrementFilteredCount(String channelId) {
        cache.get(channelId).setFiltered(cache.get(channelId).getFiltered() + 1);
        statsChanged = true;
    }

    public synchronized void incrementErrorCount(String channelId) {
        cache.get(channelId).setError(cache.get(channelId).getError() + 1);
        statsChanged = true;
    }

    public synchronized void incrementQueuedCount(String channelId) {
        cache.get(channelId).setQueued(cache.get(channelId).getQueued() + 1);
        statsChanged = true;
    }

    public synchronized void incrementAlertedCount(String channelId) {
        cache.get(channelId).setAlerted(cache.get(channelId).getAlerted() + 1);
        statsChanged = true;
    }

    public synchronized void decrementQueuedCount(String channelId) {
        if (cache.get(channelId).getQueued() > 0) {
            cache.get(channelId).setQueued(cache.get(channelId).getQueued() - 1);
            statsChanged = true;
        }
    }

    public synchronized void decrementErrorCount(String channelId) {
        if (cache.get(channelId).getError() > 0) {
            cache.get(channelId).setError(cache.get(channelId).getError() - 1);
            statsChanged = true;
        }
    }

    public synchronized void decrementFilteredCount(String channelId) {
        if (cache.get(channelId).getFiltered() > 0) {
            cache.get(channelId).setFiltered(cache.get(channelId).getFiltered() - 1);
            statsChanged = true;
        }
    }

    public synchronized void decrementSentCount(String channelId) {
        if (cache.get(channelId).getSent() > 0) {
            cache.get(channelId).setSent(cache.get(channelId).getSent() - 1);
            statsChanged = true;
        }
    }

    public synchronized void decrementReceivedCount(String channelId) {
        if (cache.get(channelId).getReceived() > 0) {
            cache.get(channelId).setReceived(cache.get(channelId).getReceived() - 1);
            statsChanged = true;
        }
    }

    private void updateStatistics(String channelId) {
        try {
            SqlConfig.getSqlSessionManager().update("Statistic.updateStatistics", cache.get(channelId));
        } catch (PersistenceException e) {
            logger.warn("could not update statistics");
        }
    }

    public void updateAllStatistics() {
        if (statsChanged) {
            
            // synchronized with createStatistics because it adds to the hash map
            synchronized (cache) {
                for (ChannelStatistics stats : cache.values()) {
                    updateStatistics(stats.getChannelId());
                }
            }
            statsChanged = false;
        }
    }

    public synchronized void clearStatistics(String channelId, boolean received, boolean filtered, boolean queued, boolean sent, boolean errored, boolean alerted) throws ControllerException {
        if (received)
            cache.get(channelId).setReceived(0);
        if (filtered)
            cache.get(channelId).setFiltered(0);
        if (queued)
            cache.get(channelId).setQueued(0);
        if (sent)
            cache.get(channelId).setSent(0);
        if (errored)
            cache.get(channelId).setError(0);
        if (alerted)
            cache.get(channelId).setAlerted(0);

        updateStatistics(channelId);
    }
}
