package com.mirth.connect.server.util;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.Statistics;
import com.mirth.connect.model.ChannelPurgeHelper;

public class ServerPurgeHelper implements ChannelPurgeHelper {

    public Map<Status, Long> getMessageStatistics(String channelId, Integer metaDataId) {
        com.mirth.connect.donkey.server.controllers.ChannelController donkeyChannelController = com.mirth.connect.donkey.server.controllers.ChannelController.getInstance();
        Statistics totalStats = donkeyChannelController.getTotalStatistics();
        Map<Status, Long> lifetimeStats = new HashMap<Status, Long>();

        if (totalStats != null) {
            lifetimeStats = totalStats.getConnectorStats(channelId, metaDataId);
        }
        return lifetimeStats;
    }
}
