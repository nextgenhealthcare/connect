package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ChannelEventType;

public class ChannelEvent extends Event {

    private String channelId;
    private ChannelEventType state;

    public ChannelEvent(String channelId, ChannelEventType state) {
        this.channelId = channelId;
        this.state = state;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public ChannelEventType getState() {
        return state;
    }

    public void setState(ChannelEventType state) {
        this.state = state;
    }
}
