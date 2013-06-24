/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.message.ImmutableMessage;

public class ValueReplacerMessage {
    private ImmutableMessage message;

    public ValueReplacerMessage(ImmutableMessage message) {
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
        return message.getReceivedDate();
    }

    public boolean isProcessed() {
        return message.isProcessed();
    }

    public Map<Integer, ValueReplacerConnectorMessage> getConnectorMessages() {
        Map<Integer, ValueReplacerConnectorMessage> map = new LinkedHashMap<Integer, ValueReplacerConnectorMessage>();

        for (Integer key : message.getConnectorMessages().keySet()) {
            map.put(key, new ValueReplacerConnectorMessage(message.getConnectorMessages().get(key)));
        }

        return map;
    }

    public String toString() {
        return message.toString();
    }
}
