/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.ConnectorUtil;

public class TcpSenderService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            TcpDispatcherProperties connectorProperties = (TcpDispatcherProperties) object;
            String host = connectorProperties.getHost();
            int port = Integer.parseInt(connectorProperties.getPort(), 10);
            int timeout = Integer.parseInt(connectorProperties.getResponseTimeout());
            return ConnectorUtil.testConnection(host, port, timeout);
        }

        return null;
    }
}
