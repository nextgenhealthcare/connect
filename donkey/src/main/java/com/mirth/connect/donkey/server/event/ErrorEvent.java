package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ErrorEventType;



public class ErrorEvent extends Event {

    private String channelId;
    private ErrorEventType type;
    private String source;
    private String customMessage;
    private Throwable throwable;

    public ErrorEvent(String channelId, ErrorEventType type, String source, String customMessage, Throwable throwable) {
        this.channelId = channelId;
        this.type = type;
        this.source = source;
        this.customMessage = customMessage;
        this.throwable = throwable;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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
