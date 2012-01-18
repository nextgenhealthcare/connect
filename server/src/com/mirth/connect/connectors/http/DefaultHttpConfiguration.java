/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.mule.umo.endpoint.UMOEndpoint;

public class DefaultHttpConfiguration implements HttpConfiguration {
    public void configureConnector(HttpConnector connector) {

    }

    public void configureReceiver(Server server, UMOEndpoint endpoint, int timeout) throws Exception {
        Connector connector = new SocketConnector();
        connector.setHost(endpoint.getEndpointURI().getUri().getHost());
        connector.setPort(endpoint.getEndpointURI().getUri().getPort());
        connector.setMaxIdleTime(timeout);
        server.addConnector(connector);
    }

    public void configureDispatcher() {

    }
}
