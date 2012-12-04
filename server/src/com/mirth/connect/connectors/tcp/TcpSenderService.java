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
import com.mirth.connect.util.TcpUtil;

public class TcpSenderService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            TcpDispatcherProperties connectorProperties = (TcpDispatcherProperties) object;
            String host = connectorProperties.getRemoteAddress();
            int port = TcpUtil.parseInt(connectorProperties.getRemotePort());
            int timeout = TcpUtil.parseInt(connectorProperties.getResponseTimeout());

            if (!connectorProperties.isOverrideLocalBinding()) {
                return ConnectorUtil.testConnection(host, port, timeout);
            } else {
                String localAddr = connectorProperties.getLocalAddress();
                int localPort = TcpUtil.parseInt(connectorProperties.getLocalPort());
                return ConnectorUtil.testConnection(host, port, timeout, localAddr, localPort);
            }
        }

        return null;
    }
}
