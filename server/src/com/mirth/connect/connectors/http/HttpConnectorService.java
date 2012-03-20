/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.net.URL;
import java.util.Map;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.ConnectorUtil;

public class HttpConnectorService implements ConnectorService {
    private static final int TIMEOUT = 5000;

    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            Map<String, String> params = (Map<String, String>) object;
            URL url = new URL(params.get(HttpSenderProperties.HTTP_URL));
            return ConnectorUtil.testConnection(url.getHost(), url.getPort(), TIMEOUT);
        }

        return null;
    }
}
