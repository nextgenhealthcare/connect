/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.controllers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.donkey.server.data.DonkeyDao;

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

    private ChannelController() {}

    public void removeChannel(String channelId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            dao.removeChannel(channelId);
            dao.commit();
        } finally {
            dao.close();
        }
    }

    public void loadStatistics() {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            currentStats = dao.getChannelStatistics();
            totalStats = dao.getChannelTotalStatistics();
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
     * @param channelConnectorMap A Map of channel ids and lists of connector meta data ids
     * @param statuses A list of statuses
     */
    public void resetStatistics(Map<String, List<Integer>> channelConnectorMap, Set<Status> statuses) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

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

    public Long getLocalChannelId(String channelId) {
        Long localChannelId = null;
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

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
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            return (dao.getLocalChannelIds().get(channelId) != null);
        } finally {
            dao.close();
        }
    }

    public void deleteAllMessages(String channelId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            if (dao.getLocalChannelIds().get(channelId) != null) {
                dao.deleteAllMessages(channelId);
            }

            dao.commit();
        } finally {
            dao.close();
        }
    }

    private synchronized long getNextLocalChannelId() {
        Long nextChannelId = null;
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            nextChannelId = dao.selectMaxLocalChannelId();
        } finally {
            dao.close();
        }

        if (nextChannelId == null) {
            return 1;
        }

        return ++nextChannelId;
    }

    private long createChannel(String channelId) {
        long localChannelId = getNextLocalChannelId();
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            dao.createChannel(channelId, localChannelId);
            dao.commit();
        } finally {
            dao.close();
        }

        return localChannelId;
    }
}
