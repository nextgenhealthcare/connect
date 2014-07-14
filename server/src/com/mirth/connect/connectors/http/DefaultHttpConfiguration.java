/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.eclipse.jetty.server.bio.SocketConnector;

import com.mirth.connect.donkey.server.channel.Connector;

public class DefaultHttpConfiguration implements HttpConfiguration {

    @Override
    public void configureConnectorDeploy(Connector connector) throws Exception {
        if (connector instanceof HttpDispatcher) {
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(SSLContexts.createDefault(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ((HttpDispatcher) connector).getSocketFactoryRegistry().register("https", sslConnectionSocketFactory);
        }
    }

    @Override
    public void configureConnectorUndeploy(Connector connector) {}

    @Override
    public void configureReceiver(HttpReceiver connector) throws Exception {
        SocketConnector listener = new SocketConnector();
        listener.setHost(connector.getHost());
        listener.setPort(connector.getPort());
        listener.setMaxIdleTime(connector.getTimeout());
        connector.getServer().addConnector(listener);
    }

    @Override
    public void configureDispatcher(HttpDispatcher connector, HttpDispatcherProperties connectorProperties) throws Exception {}
}