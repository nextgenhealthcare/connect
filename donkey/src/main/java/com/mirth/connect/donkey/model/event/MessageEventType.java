package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang.WordUtils;

import com.mirth.connect.donkey.model.message.Status;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("errorEventType")
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
