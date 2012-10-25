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

import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class DefaultHttpConfiguration implements HttpConfiguration {
    public void configureConnector(ConnectorProperties connectorProperties) {

    }

    public void configureReceiver(Server server, String host, int port, int timeout) throws Exception {
        Connector connector = new SocketConnector();
        connector.setHost(host);
        connector.setPort(port);
        connector.setMaxIdleTime(timeout);
        server.addConnector(connector);
    }

    public void configureDispatcher() {

    }
}
