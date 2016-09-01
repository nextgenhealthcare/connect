/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.Statistics;

public class DonkeyStatisticsUpdater extends Thread implements StatisticsUpdater {

    public static final int DEFAULT_UPDATE_INTERVAL = 1000;

    private DonkeyDaoFactory daoFactory;
    private int updateInterval;
    private Statistics statistics = new Statistics(false);
    private Logger logger = Logger.getLogger(getClass());

    public DonkeyStatisticsUpdater(DonkeyDaoFactory daoFactory, int updateInterval) {
        setDaoFactory(daoFactory);
        if (updateInterval <= 0) {
            updateInterval = DEFAULT_UPDATE_INTERVAL;
        }
        this.updateInterval = updateInterval;
        setName("Statistics Updater Thread");
    }

    public void setDaoFactory(DonkeyDaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        if (daoFactory != null) {
            daoFactory.setStatisticsUpdater(this);
        }
    }

    @Override
    public void run() {
        boolean done = false;

        while (!done) {
            try {
                Thread.sleep(updateInterval);
                commit();
            } catch (InterruptedException e) {
                // Do one last commit to ensure all available statistics are committed to the database 
                try {
                    commit();
                } catch (InterruptedException e2) {
                    logger.error("Unable to update statistics before stopping engine.", e);
                }

                Thread.currentThread().interrupt();
                done = true;
            }
        }
    }

    public void shutdown() {
        if (isAlive()) {
            interrupt();
            try {
                join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void commit() throws InterruptedException {
        if (!statistics.isEmpty() && daoFactory != null) {
            Statistics tempStats = new Statistics(false);

            Map<String, Map<Integer, Map<Status, Long>>> stats = statistics.getStats();
            tempStats.update(stats);

            DonkeyDao dao = daoFactory.getDao();
            try {
                dao.addChannelStatistics(tempStats);
                dao.commit();

                // Invert the stats and update them on the Statistics object
                for (Map<Integer, Map<Status, Long>> channelMap : stats.values()) {
                    for (Map<Status, Long> connectorMap : channelMap.values()) {
                        for (Entry<Status, Long> entry : connectorMap.entrySet()) {
                            entry.setValue(-entry.getValue());
                        }
                    }
                }
                statistics.update(stats);
            } catch (Throwable t) {
                if (t instanceof InterruptedException) {
                    throw (InterruptedException) t;
                }

                if (t instanceof ChannelDoesNotExistException) {
                    ChannelDoesNotExistException e = (ChannelDoesNotExistException) t;
                    logger.debug("Unable to update statistics.", e);

                    for (String channelId : e.getChannelIds()) {
                        statistics.remove(channelId);
                    }
                } else {
                    logger.error("Unable to update statistics.", t);
                }
            } finally {
                dao.close();
            }
        }
    }

    @Override
    public void update(Statistics statistics) {
        if (!statistics.isEmpty()) {
            this.statistics.update(statistics);
        }
    }
}