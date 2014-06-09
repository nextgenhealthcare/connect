/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import com.mirth.connect.connectors.ConnectorService;

public abstract class ConnectorServicePlugin implements ServerPlugin {

    public abstract String getTransportName();

    public abstract Object invoke(ConnectorService service, String channelId, String method, Object object, String sessionId) throws Exception;

    @Override
    public void start() {}

    @Override
    public void stop() {}
}