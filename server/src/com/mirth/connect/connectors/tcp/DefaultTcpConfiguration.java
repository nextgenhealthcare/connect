/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.server.channel.Connector;

public class DefaultTcpConfiguration implements TcpConfiguration {

    @Override
    public void configureConnectorDeploy(Connector connector) throws Exception {}

    @Override
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return new StateAwareServerSocket(port, backlog);
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        return new StateAwareServerSocket(port, backlog, bindAddr);
    }

    @Override
    public Socket createSocket() {
        return new StateAwareSocket();
    }

    @Override
    public Socket createResponseSocket() {
        return new StateAwareSocket();
    }

    @Override
    public Map<String, Object> getSocketInformation(Socket socket) {
        return new HashMap<String, Object>();
    }
}