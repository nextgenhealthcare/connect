package com.mirth.connect.model;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.util.purge.PurgeHelper;

public interface ChannelPurgeHelper extends PurgeHelper {

    public default Map<Status, Long> getMessageStatistics(String channelId, Integer metaDataId) {
        return new HashMap<Status, Long>();
    }
}
