/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.controllers;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;

public class ChannelController {
    private static ChannelController instance;

    public static ChannelController getInstance() {
        synchronized (ChannelController.class) {
            if (instance == null) {
                instance = new ChannelController();
            }

            return instance;
        }
    }

    private Statistics currentStats;
    private Statistics totalStats;
    private Donkey donkey = Donkey.getInstance();

    protected ChannelController() {}

    public void removeChannel(String channelId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            dao.removeChannel(channelId);
            dao.commit();
        } finally {
            dao.close();
        }
    }

    public void loadStatistics(String serverId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            currentStats = dao.getChannelStatistics(serverId);
            totalStats = dao.getChannelTotalStatistics(serverId);
        } finally {
            dao.close();
        }
    }

    public Statistics getStatistics() {
        return currentStats;
    }

    public Statistics getTotalStatistics() {
        return totalStats;
    }

    /**
     * Reset the statistics for the given channels/connectors and statuses
     * 
     * @param channelConnectorMap
     *            A Map of channel ids and lists of connector meta data ids
     * @param statuses
     *            A list of statuses
     */
    public void resetStatistics(Map<String, List<Integer>> channelConnectorMap, Set<Status> statuses) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            for (Entry<String, List<Integer>> entry : channelConnectorMap.entrySet()) {
                String channelId = entry.getKey();
                List<Integer> metaDataIds = entry.getValue();

                for (Integer metaDataId : metaDataIds) {
                    dao.resetStatistics(channelId, metaDataId, statuses);

                    // Each update here must have its own transaction, otherwise deadlocks may occur.
                    dao.commit();
                }
            }
        } finally {
            dao.close();
        }
    }

    public void resetAllStatistics() {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            for (String channelId : dao.getLocalChannelIds().keySet()) {
                dao.resetAllStatistics(channelId);

                // Each update here must have its own transaction, otherwise deadlocks may occur.
                dao.commit();
            }
        } finally {
            dao.close();
        }
    }

    public Long getLocalChannelId(String channelId) {
        Long localChannelId = null;
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            localChannelId = dao.getLocalChannelIds().get(channelId);
        } finally {
            dao.close();
        }

        if (localChannelId == null) {
            localChannelId = createChannel(channelId);
        }

        return localChannelId;
    }

    public void initChannelStorage(String channelId) {
        getLocalChannelId(channelId);
    }

    public boolean channelExists(String channelId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            return (dao.getLocalChannelIds().get(channelId) != null);
        } finally {
            dao.close();
        }
    }

    public void deleteAllMessages(String channelId) {
        DonkeyDao dao = donkey.getDaoFactory().getDao();

        try {
            if (dao.getLocalChannelIds().get(channelId) != null) {
                dao.deleteAllMessages(channelId);
            }

            dao.commit();
        } finally {
            dao.close();
        }
    }

    private synchronized long createChannel(String channelId) {
        int attemptsRemaining = 3;

        while (true) {
            DonkeyDao dao = donkey.getDaoFactory().getDao();

            try {
                Long localChannelId = dao.selectMaxLocalChannelId();
                if (localChannelId == null) {
                    localChannelId = 1L;
                } else {
                    localChannelId++;
                }

                dao.createChannel(channelId, localChannelId);
                dao.commit();

                return localChannelId;
            } catch (DonkeyDaoException e) {
                /*
                 * MIRTH-3475 If two server instances connected to a shared database attempt to create
                 * channels at the same time, they may both obtain the same next local channel
                 * ID, which will result in a duplicate key error. In the rare case that this
                 * happens, we retry 2 more times.
                 */
                if (e.getCause() instanceof SQLException) {
                    SQLException sqlException = (SQLException) e.getCause();

                    /*
                     * The second part of this conditional tests if the SQLException was the result
                     * of a duplicate key violation. MySQL, Oracle and SQL Server generate an
                     * exception with SQLState == 23000, while Postgres returns SQLState == 23505.
                     */
                    if (--attemptsRemaining == 0 || !(StringUtils.equals(sqlException.getSQLState(), "23000") || StringUtils.equals(sqlException.getSQLState(), "23505") || StringUtils.containsIgnoreCase(sqlException.getMessage(), "duplicate") || StringUtils.containsIgnoreCase(sqlException.getMessage(), "unique constraint"))) {
                        throw e;
                    }
                } else {
                    throw e;
                }
            } finally {
                dao.close();
            }
        }
    }
}
