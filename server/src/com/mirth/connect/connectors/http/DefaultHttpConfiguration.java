/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.net.InetAddress;

import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mule.umo.endpoint.UMOEndpoint;

public class DefaultHttpConfiguration implements HttpConfiguration {
    public void configureConnector(HttpConnector connector) {

    }

    public void configureReceiver(HttpServer server, UMOEndpoint endpoint) throws Exception {
        SocketListener listener = new SocketListener();
        listener.setInetAddress(InetAddress.getByName(endpoint.getEndpointURI().getUri().getHost()));
        listener.setPort(endpoint.getEndpointURI().getUri().getPort());
        server.addListener(listener);
    }

    public void configureDispatcher() {
        
    }
}
