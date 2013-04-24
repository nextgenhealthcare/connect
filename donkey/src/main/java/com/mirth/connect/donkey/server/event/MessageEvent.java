package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.MessageEventType;

public class MessageEvent extends Event {

    private String channelId;
    private MessageEventType type;

    public MessageEvent(String channelId, MessageEventType type) {
        this.channelId = channelId;
        this.type = type;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public MessageEventType getType() {
        return type;
    }

    public void setType(MessageEventType type) {
        this.type = type;
    }
}
