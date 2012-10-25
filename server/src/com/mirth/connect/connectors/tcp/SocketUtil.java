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
import java.net.SocketTimeoutException;

public class SocketUtil {
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
    public static boolean remoteSideHasClosed(Socket socket) throws IOException {
        return remoteSideHasClosed(socket, new BufferedInputStream(socket.getInputStream()));
    }

    public static boolean remoteSideHasClosed(Socket socket, BufferedInputStream bis) throws IOException {
        int oldTimeout = socket.getSoTimeout();
        socket.setSoTimeout(100);
        bis.mark(1);
        try {
            return bis.read() == -1;
        } catch (SocketTimeoutException e) {
            return false;
        } finally {
            bis.reset();
            socket.setSoTimeout(oldTimeout);
        }
    }
}
