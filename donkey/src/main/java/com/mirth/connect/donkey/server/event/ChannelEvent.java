package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.channel.ChannelState;

public class ChannelEvent extends Event {

    private String channelId;
    private ChannelState state;

    public ChannelEvent(String channelId, ChannelState state) {
        this.channelId = channelId;
        this.state = state;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public ChannelState getState() {
        return state;
    }

    public void setState(ChannelState state) {
        this.state = state;
    }
}
