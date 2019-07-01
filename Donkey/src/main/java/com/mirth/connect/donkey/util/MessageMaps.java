/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import com.mirth.connect.donkey.model.message.ConnectorMessage;

public class MessageMaps {

    public Object get(String key, ConnectorMessage connectorMessage) {
        return get(key, connectorMessage, true);
    }

    public Object get(String key, ConnectorMessage connectorMessage, boolean includeResponseMap) {
        Object value = null;

        if (includeResponseMap && connectorMessage.getResponseMap() != null && connectorMessage.getResponseMap().containsKey(key)) {
            value = connectorMessage.getResponseMap().get(key);
        } else if (connectorMessage.getConnectorMap() != null && connectorMessage.getConnectorMap().containsKey(key)) {
            value = connectorMessage.getConnectorMap().get(key);
        } else if (connectorMessage.getChannelMap() != null && connectorMessage.getChannelMap().containsKey(key)) {
            value = connectorMessage.getChannelMap().get(key);
        } else if (connectorMessage.getSourceMap() != null && connectorMessage.getSourceMap().containsKey(key)) {
            value = connectorMessage.getSourceMap().get(key);
        }

        return value;
    }
}