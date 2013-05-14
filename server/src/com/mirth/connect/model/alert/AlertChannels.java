package com.mirth.connect.model.alert;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("alertChannels")
public class AlertChannels {

    private Map<String, Map<Integer, Boolean>> channels = new HashMap<String, Map<Integer, Boolean>>();

    public boolean isChannelEnabled(String channelId) {
        return channels.containsKey(channelId) || channels.containsKey(null);
    }

    public boolean isConnectorEnabled(String channelId, Integer metaDataId) {
        String key;

        if (channels.containsKey(channelId)) {
            key = channelId;
        } else if (channels.containsKey(null)) {
            key = null;
        } else {
            return false;
        }

        Map<Integer, Boolean> connectors = channels.get(key);
        if (connectors.containsKey(metaDataId)) {
            return connectors.get(metaDataId);
        } else if ((metaDataId == null || metaDataId > 0) && connectors.containsKey(null)) {
            return connectors.get(null);
        } else {
            return false;
        }
    }
    
    public void addChannel(String channelId) {
        if (!channels.containsKey(channelId)) {
            channels.put(channelId, new HashMap<Integer, Boolean>());
        }
    }

    public void addConnector(String channelId, Integer metaDataId, boolean enabled) {
        Map<Integer, Boolean> connectors = channels.get(channelId);

        connectors.put(metaDataId, enabled);
    }
}
