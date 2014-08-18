package com.mirth.connect.server.channel;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChannelFuture {

    private String channelId;
    private Integer metaDataId;
    private Future<?> delegate;
    private ChannelTaskHandler handler;

    public ChannelFuture(String channelId, Integer metaDataId, Future<?> delegate, ChannelTaskHandler handler) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
        this.delegate = delegate;
        this.handler = handler;
    }

    public void get() {
        try {
            delegate.get();
        } catch (InterruptedException e) {
            handler.taskErrored(channelId, metaDataId, e);
        } catch (ExecutionException e) {
            handler.taskErrored(channelId, metaDataId, e);
        } catch (CancellationException e) {
            handler.taskCancelled(channelId, metaDataId, e);
        }
    }

    public void get(long timeout, TimeUnit unit) throws TimeoutException {
        try {
            delegate.get(timeout, unit);
        } catch (InterruptedException e) {
            handler.taskErrored(channelId, metaDataId, e);
        } catch (ExecutionException e) {
            handler.taskErrored(channelId, metaDataId, e);
        } catch (CancellationException e) {
            handler.taskCancelled(channelId, metaDataId, e);
        }
    }
}
