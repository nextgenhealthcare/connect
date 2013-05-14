package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ConnectorEventType;

public class ConnectorCountEvent extends ConnectorEvent {
    private Boolean increment;

    public ConnectorCountEvent(String channelId, Integer metaDataId, ConnectorEventType state, ConnectorEventType displayState, String message, Boolean increment) {
        super(channelId, metaDataId, state, displayState, message);

        this.setIncrement(increment);
    }

    public Boolean isIncrement() {
        return increment;
    }

    public void setIncrement(Boolean increment) {
        this.increment = increment;
    }
}
