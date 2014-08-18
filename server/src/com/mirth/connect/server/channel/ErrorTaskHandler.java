package com.mirth.connect.server.channel;

import java.util.concurrent.CancellationException;

/**
 * An implementation of ChannelTaskHandler that provides access to the error that occurred for the
 * task. Normally this should only be used when only a single task is expected to be dispatched.
 * Otherwise if multiple errors occur only the most recent error will be retrievable.
 */
public class ErrorTaskHandler extends ChannelTaskHandler {
    private Exception error;

    public Exception getError() {
        return error;
    }

    public boolean isErrored() {
        return error != null;
    }

    public void taskErrored(String channelId, Integer metaDataId, Exception e) {
        error = e;
    }

    public void taskCancelled(String channelId, Integer metaDataId, CancellationException e) {
        error = e;
    }
}
