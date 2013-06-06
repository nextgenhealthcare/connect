package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ConnectorEventType;

public class ConnectorCountEvent extends ConnectorEvent {
    private Boolean increment;

    public ConnectorCountEvent(String channelId, Integer metaDataId, String connectorName, ConnectorEventType state, String message, Boolean increment) {
        super(channelId, metaDataId, connectorName, state, message);

        this.setIncrement(increment);
    }

    public Boolean isIncrement() {
        return increment;
    }

    public void setIncrement(Boolean increment) {
        this.increment = increment;
    }
}
