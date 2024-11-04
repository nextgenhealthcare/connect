/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.core.ws;

import java.util.Map;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.server.channel.IConnector;

public interface WebServiceConfiguration {

    public void configureConnectorDeploy(IConnector connector) throws Exception;

    public void configureConnectorUndeploy(IConnector connector);

    public void configureReceiver(IWebServiceReceiver connector) throws Exception;

    public void configureDispatcher(IWebServiceDispatcher connector, ConnectorProperties connectorProperties, Map<String, Object> requestContext) throws Exception;

    public void configureHttpHeaders(IWebServiceReceiver connector) throws Exception;
}
