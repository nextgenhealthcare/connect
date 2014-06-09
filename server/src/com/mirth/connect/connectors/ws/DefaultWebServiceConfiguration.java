/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.util.Map;

import com.mirth.connect.donkey.server.channel.Connector;
import com.sun.net.httpserver.HttpServer;

public class DefaultWebServiceConfiguration implements WebServiceConfiguration {

    @Override
    public void configureConnectorDeploy(Connector connector) throws Exception {}

    @Override
    public void configureConnectorUndeploy(Connector connector) {}

    @Override
    public void configureReceiver(WebServiceReceiver connector) throws Exception {
        connector.setServer(HttpServer.create());
    }

    @Override
    public void configureDispatcher(WebServiceDispatcher connector, WebServiceDispatcherProperties connectorProperties, Map<String, Object> requestContext) throws Exception {}
}