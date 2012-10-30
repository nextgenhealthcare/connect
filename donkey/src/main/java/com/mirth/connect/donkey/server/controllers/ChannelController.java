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

    private Map<String, Long> localChannelIds;
    private Statistics currentStats;
    private Statistics totalStats;

    private ChannelController() {}

    public Map<String, Long> getLocalChannelIds() {
        if (localChannelIds == null) {
            DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

            try {
                localChannelIds = dao.getLocalChannelIds();
            } finally {
                dao.close();
            }
        }

        return localChannelIds;
    }

    public void removeChannel(String channelId) {
        Long localChannelId = getLocalChannelIds().get(channelId);

        if (localChannelId != null) {
            DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

            try {
                dao.removeChannel(channelId);
                dao.commit();
            } finally {
                dao.close();
                localChannelIds.remove(channelId);
            }
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
        if (!getLocalChannelIds().containsKey(channelId)) {
            initChannel(channelId);
        }

        return getLocalChannelIds().get(channelId);
    }

    private synchronized long getNextLocalChannelId() {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
        Long nextChannelId = null;

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

    public void deleteAllMessages(String channelId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            dao.deleteAllMessages(channelId);
            dao.commit();
        } finally {
            dao.close();
        }
    }

    private void initChannel(String channelId) {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
        Long localChannelId = getNextLocalChannelId();

        try {
            dao.createChannel(channelId, localChannelId);
            dao.commit();
        } finally {
            dao.close();
        }

        localChannelIds.put(channelId, localChannelId);
    }
}
