package com.mirth.connect.server.channel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ChannelFuture {

    private String channelId;
    private Future<?> delegate;

    public ChannelFuture(String channelId, Future<?> delegate) {
        this.channelId = channelId;
        this.delegate = delegate;
    }

    public String getChannelId() {
        return channelId;
    }

    public Object get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }
}
