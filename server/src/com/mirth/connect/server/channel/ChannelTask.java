package com.mirth.connect.server.channel;

import java.util.concurrent.Callable;

public abstract class ChannelTask implements Callable<Void> {

    protected String channelId;

    public ChannelTask(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }
}
