/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mllp;

import java.util.Map;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.ConnectorUtil;

public class LLPSenderConnectorService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            String host = params.get(LLPSenderProperties.LLP_ADDRESS);
            int port = Integer.parseInt(params.get(LLPSenderProperties.LLP_PORT));
            int timeout = Integer.parseInt(params.get(LLPSenderProperties.LLP_SERVER_TIMEOUT));
            return ConnectorUtil.testConnection(host, port, timeout);
        }

        return null;
    }
}
