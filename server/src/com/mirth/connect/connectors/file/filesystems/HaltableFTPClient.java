/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;

public class HaltableFTPClient extends FTPClient {

    private Set<Socket> openSockets = new HashSet<Socket>();

    @Override
    protected Socket _openDataConnection_(String command, String arg) throws IOException {
        /*
         * MIRTH-3041, MIRTH-3062 It's possible for the FTP data sockets to hang. We don't have
         * control over this because different servers may hang for different reasons. For instance
         * the FTP dispatcher can block on a socket write if the socket buffer of the server it is
         * connecting to is full. The FTP Receiver and Dispatcher can block on a socket read if the
         * server they are connecting to is hung. In case this happens, we store references to each
         * new socket so it can be closed if needed.
         */
        Socket socket = super._openDataConnection_(command, arg);

        synchronized (openSockets) {
            for (Iterator<Socket> iterator = openSockets.iterator(); iterator.hasNext();) {
                Socket openSocket = iterator.next();
                if (openSocket != null && openSocket.isClosed()) {
                    iterator.remove();
                }
            }

            if (socket != null) {
                openSockets.add(socket);
            }
        }

        return socket;
    }

    public void closeDataSockets() {
        // Close all open sockets
        synchronized (openSockets) {
            for (Iterator<Socket> iterator = openSockets.iterator(); iterator.hasNext();) {
                Socket socket = iterator.next();
                try {
                    socket.close();
                } catch (IOException e) {
                } finally {
                    iterator.remove();
                }
            }
        }
    }
}
