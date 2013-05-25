package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.Event;

public class ConnectorEvent extends Event {

    private String channelId;
    private Integer metaDataId;
    private ConnectorEventType state;
    private ConnectorEventType displayState;
    private String message;

    public ConnectorEvent(String channelId, Integer metaDataId, ConnectorEventType state) {
        this(channelId, metaDataId, state, null, "");
    }

    public ConnectorEvent(String channelId, Integer metaDataId, ConnectorEventType state, String message) {
        this(channelId, metaDataId, state, null, message);
    }

    public ConnectorEvent(String channelId, Integer metaDataId, ConnectorEventType state, ConnectorEventType displayState, String message) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
        this.state = state;
        this.displayState = displayState;
        this.message = message;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(Integer metaDataId) {
        this.metaDataId = metaDataId;
    }

    public ConnectorEventType getState() {
        return state;
    }

    public void setState(ConnectorEventType state) {
        this.state = state;
    }

    public ConnectorEventType getDisplayState() {
        return displayState;
    }

    public void setDisplayState(ConnectorEventType displayState) {
        this.displayState = displayState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
