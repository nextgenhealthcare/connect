/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;

public class DefaultHttpConfiguration implements HttpConfiguration {
    public void configureConnector(String channelId, Integer metaDataId, String host) throws Exception {
        checkHost(host);
    }

    public void configureReceiver(Server server, String channelId, String host, int port, int timeout) throws Exception {
        Connector connector = new SocketConnector();
        connector.setHost(host);
        connector.setPort(port);
        connector.setMaxIdleTime(timeout);
        server.addConnector(connector);
    }

    public void configureDispatcher(String channelId, Integer metaDataId, String host) throws Exception {
        checkHost(host);
    }
    
    private void checkHost(String host) throws Exception {
        try {
            String scheme = new URI(host).getScheme();
            if (scheme != null && scheme.equalsIgnoreCase("https")) {
                throw new Exception("The HTTPS protocol is not supported for this connector.");
            }
        } catch (URISyntaxException e) {
        }
    }
}