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
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.TcpUtil;

public class TcpSenderService implements ConnectorService {
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public Object invoke(String channelId, String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            TcpDispatcherProperties connectorProperties = (TcpDispatcherProperties) object;
            String host = replacer.replaceValues(connectorProperties.getRemoteAddress(), channelId);
            int port = TcpUtil.parseInt(replacer.replaceValues(connectorProperties.getRemotePort(), channelId));
            int timeout = TcpUtil.parseInt(replacer.replaceValues(connectorProperties.getResponseTimeout(), channelId));

            if (!connectorProperties.isOverrideLocalBinding()) {
                return ConnectorUtil.testConnection(host, port, timeout);
            } else {
                String localAddr = replacer.replaceValues(connectorProperties.getLocalAddress(), channelId);
                int localPort = TcpUtil.parseInt(replacer.replaceValues(connectorProperties.getLocalPort(), channelId));
                return ConnectorUtil.testConnection(host, port, timeout, localAddr, localPort);
            }
        }

        return null;
    }
}
