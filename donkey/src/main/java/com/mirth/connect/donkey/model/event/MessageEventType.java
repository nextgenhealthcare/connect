/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang3.text.WordUtils;

import com.mirth.connect.donkey.model.message.Status;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("messageEventType")
public enum MessageEventType {
    RECEIVED, FILTERED, SENT, QUEUED, ERRORED;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }

    public static MessageEventType fromStatus(Status status) {
        switch (status) {
            case RECEIVED:
                return MessageEventType.RECEIVED;
            case FILTERED:
                return MessageEventType.FILTERED;
            case ERROR:
                return MessageEventType.ERRORED;
            case SENT:
                return MessageEventType.SENT;
            case QUEUED:
                return MessageEventType.QUEUED;

            default:
                return null;
        }
    }
}
