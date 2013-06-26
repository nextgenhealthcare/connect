/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImmutableMessage {
    private Message message;

    public ImmutableMessage(Message message) {
        this.message = message;
    }

    public Long getMessageId() {
        return message.getMessageId();
    }

    public String getServerId() {
        return message.getServerId();
    }

    public String getChannelId() {
        return message.getChannelId();
    }

    public Calendar getReceivedDate() {
        return (Calendar) message.getReceivedDate().clone();
    }

    public boolean isProcessed() {
        return message.isProcessed();
    }

    public Map<Integer, ImmutableConnectorMessage> getConnectorMessages() {
        // MIRTH-2523: Overriding the get method to allow doubles to be passed in
        Map<Integer, ImmutableConnectorMessage> map = new LinkedHashMap<Integer, ImmutableConnectorMessage>() {
            @Override
            public ImmutableConnectorMessage get(Object key) {
                if (key instanceof Double) {
                    key = ((Double) key).intValue();
                }
                return super.get(key);
            }
        };

        for (Integer key : message.getConnectorMessages().keySet()) {
            map.put(key, new ImmutableConnectorMessage(message.getConnectorMessages().get(key), false, getDestinationNameMap()));
        }
        return map;
    }

    public ImmutableConnectorMessage getMergedConnectorMessage() {
        return new ImmutableConnectorMessage(message.getMergedConnectorMessage(), false, getDestinationNameMap());
    }
    
    private Map<String, String> getDestinationNameMap() {
        Map<String, String> destinationNameMap = new HashMap<String, String>();

        for (ConnectorMessage destinationMessage : message.getConnectorMessages().values()) {
            destinationNameMap.put(destinationMessage.getConnectorName(), "d" + String.valueOf(destinationMessage.getMetaDataId()));
        }
        
        return destinationNameMap;
    }

    public String toString() {
        return message.toString();
    }
}
