package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang.WordUtils;


public enum ConnectorEventType {
    STARTING,
    STARTED,
    STOPPING,
    STOPPED;
    
    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }
}
