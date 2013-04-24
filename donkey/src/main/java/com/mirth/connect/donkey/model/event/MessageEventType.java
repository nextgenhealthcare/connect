package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang.WordUtils;


public enum MessageEventType {
    RECEIVED,
    FILTERED,
    SENT,
    QUEUED,
    ERRORED;
    
    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }
}
