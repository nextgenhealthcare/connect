/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.net.URL;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.ConnectorUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class HttpConnectorService implements ConnectorService {
    private static final int TIMEOUT = 5000;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public Object invoke(String channelId, String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            HttpDispatcherProperties props = (HttpDispatcherProperties) object;
            URL url = new URL(replacer.replaceValues(props.getHost(), channelId));
            int port = url.getPort();
            // If no port was provided, default to port 80.
            return ConnectorUtil.testConnection(url.getHost(), (port == -1) ? 80 : port, TIMEOUT);
        }

        return null;
    }
}
