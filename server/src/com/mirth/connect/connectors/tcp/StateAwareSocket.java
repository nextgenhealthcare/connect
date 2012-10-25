/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

public class StateAwareSocket extends Socket {
    protected BufferedInputStream bis = null;

    public StateAwareSocket() {
        super();
    }

    public StateAwareSocket(String host, int port) throws Exception {
        super(host, port);
    }

    public BufferedInputStream getBufferedInputStream() throws IOException {
        if (bis == null) {
            bis = new BufferedInputStream(getInputStream());
        }
        return bis;
    }

    /**
     * The only (portable) way in Java to detect that the remote host has closed
     * the connection is to attempt to read from the connection and see if you
     * get -1. We use the mark() and reset() feature of BufferedInputStream to
     * nondestructively peek into the stream to check for this. Warning: since
     * we've started consuming data, anyone reading from this socket must now
     * use our BIS and not create their own from getInputStream().
     * 
     * @return true if the remote end has closed its side of this socket
     */
    public boolean remoteSideHasClosed() throws IOException {
        return SocketUtil.remoteSideHasClosed(this, getBufferedInputStream());
    }
}