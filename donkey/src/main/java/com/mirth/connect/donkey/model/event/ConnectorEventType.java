package com.mirth.connect.donkey.model.event;

import org.apache.commons.lang.WordUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("connectorEventType")
public enum ConnectorEventType {
    IDLE, READING, WRITING, POLLING, RECEIVING, SENDING, WAITING_FOR_RESPONSE, CONNECTED, CONNECTING, DISCONNECTED, INFO, FAILURE;

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace("_", " "));
    }
}
