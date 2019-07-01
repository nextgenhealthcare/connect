/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

public class ChannelException extends Exception {
    private boolean stopped;

    public ChannelException(boolean stopped) {
        this(stopped, null, "");
    }

    public ChannelException(boolean stopped, Throwable cause) {
        this(stopped, cause, "");
    }

    public ChannelException(boolean stopped, Throwable cause, String message) {
        super(message, cause);
        this.stopped = stopped;
    }

    public boolean isStopped() {
        return stopped;
    }
}
