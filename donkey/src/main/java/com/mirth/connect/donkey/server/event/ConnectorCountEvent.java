package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ConnectorEventType;

public class ConnectorCountEvent extends ConnectorEvent {
    
    private Integer maximum;
    private Boolean increment;
    
    public ConnectorCountEvent(String channelId, Integer metaDataId, String connectorName, ConnectorEventType state, String message, Integer maximum) {
        super(channelId, metaDataId, connectorName, state, message);

        this.maximum = maximum;
    }

    public ConnectorCountEvent(String channelId, Integer metaDataId, String connectorName, ConnectorEventType state, String message, Boolean increment) {
        super(channelId, metaDataId, connectorName, state, message);

        this.increment = increment;
    }

    public Integer getMaximum() {
        return maximum;
    }

    public void setMaximum(Integer maximum) {
        this.maximum = maximum;
    }

    public Boolean isIncrement() {
        return increment;
    }

    public void setIncrement(Boolean increment) {
        this.increment = increment;
    }
}
