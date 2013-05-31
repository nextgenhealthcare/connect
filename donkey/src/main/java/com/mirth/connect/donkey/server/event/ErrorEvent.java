package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.event.Event;

public class ErrorEvent extends Event {

    private String channelId;
    private Integer metaDataId;
    private ErrorEventType type;
    private String connectorName;
    private String customMessage;
    private Throwable throwable;

    public ErrorEvent(String channelId, Integer metaDataId, ErrorEventType type, String connectorName, String customMessage, Throwable throwable) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
        this.type = type;
        this.connectorName = connectorName;
        this.customMessage = customMessage;
        this.throwable = throwable;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(Integer metaDataId) {
        this.metaDataId = metaDataId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public ErrorEventType getType() {
        return type;
    }

    public void setType(ErrorEventType type) {
        this.type = type;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
