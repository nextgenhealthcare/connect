/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.channel;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public abstract class ChannelTask implements Callable<Void> {

    protected String channelId;
    protected Integer metaDataId;
    private ChannelTaskHandler handler;

    public ChannelTask(String channelId) {
        this(channelId, null);
    }

    public ChannelTask(String channelId, Integer metaDataId) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
    }

    public String getChannelId() {
        return channelId;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public ChannelTaskHandler getHandler() {
        return handler;
    }

    public void setHandler(ChannelTaskHandler handler) {
        this.handler = handler;
    }

    public ChannelFuture submitTo(ExecutorService executor) throws RejectedExecutionException {
        return new ChannelFuture(channelId, metaDataId, executor.submit(this), handler);
    }

    @Override
    public final Void call() throws Exception {
        String originalThreadName = Thread.currentThread().getName();
        try {
            if (metaDataId != null) {
                Thread.currentThread().setName("Channel " + getClass().getSimpleName() + " Thread on (" + channelId + ") connector (" + metaDataId + ") < " + originalThreadName);
            } else {
                Thread.currentThread().setName("Channel " + getClass().getSimpleName() + " Thread on (" + channelId + ") < " + originalThreadName);
            }

            if (handler == null) {
                handler = new ChannelTaskHandler();
            }

            try {
                handler.taskStarted(channelId, metaDataId);
                execute();
                handler.taskCompleted(channelId, metaDataId);
            } catch (Exception e) {
                handler.taskErrored(channelId, metaDataId, e);
            }

            return null;
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }

    public abstract Void execute() throws Exception;
}
