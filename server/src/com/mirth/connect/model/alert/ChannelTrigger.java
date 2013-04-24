package com.mirth.connect.model.alert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ChannelTrigger {
    
    private Map<String, List<Integer>> channels = new HashMap<String, List<Integer>>();

    public Map<String, List<Integer>> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, List<Integer>> channels) {
        this.channels = channels;
    }
    
}
