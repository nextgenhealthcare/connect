/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.ConnectorUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class TcpSenderService implements ConnectorService {
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public Object invoke(String channelId, String channelName, String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testConnection")) {
            TcpDispatcherProperties connectorProperties = (TcpDispatcherProperties) object;
            String host = replacer.replaceValues(connectorProperties.getRemoteAddress(), channelId, channelName);
            int port = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getRemotePort(), channelId, channelName));
            int timeout = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getResponseTimeout(), channelId, channelName));

            if (!connectorProperties.isOverrideLocalBinding()) {
                return ConnectorUtil.testConnection(host, port, timeout);
            } else {
                String localAddr = replacer.replaceValues(connectorProperties.getLocalAddress(), channelId, channelName);
                int localPort = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getLocalPort(), channelId, channelName));
                return ConnectorUtil.testConnection(host, port, timeout, localAddr, localPort);
            }
        }

        return null;
    }
}
