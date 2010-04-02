/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.util.Map;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.ConnectorUtil;

public class TcpConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            String host = params.get(TCPSenderProperties.TCP_ADDRESS);
            int port = Integer.parseInt(params.get(TCPSenderProperties.TCP_PORT));
            int timeout = Integer.parseInt(params.get(TCPSenderProperties.TCP_SERVER_TIMEOUT));
            return ConnectorUtil.testConnection(host, port, timeout);
        }

        return null;
    }
}
