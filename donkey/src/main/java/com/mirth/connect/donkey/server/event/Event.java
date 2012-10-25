/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import java.util.Calendar;

import com.mirth.connect.donkey.model.message.Status;

public class Event {
    private EventType eventType;
    private String channelId;
    private Integer metaDataId;
    private Long messageId;
    private Status messageStatus;
    private Calendar eventDate;
    private Status oldMessageStatus;
    private boolean queueEnabled;

    public Event(EventType eventType, String channelId) {
        this(eventType, channelId, null, null, null);
    }

    public Event(EventType eventType, String channelId, Integer metaDataId) {
        this(eventType, channelId, metaDataId, null, null);
    }

    public Event(EventType eventType, String channelId, Integer metaDataId, Long messageId) {
        this(eventType, channelId, metaDataId, messageId, null);
    }

    public Event(EventType eventType, String channelId, Integer metaDataId, Long messageId, Status messageStatus) {
        this(eventType, channelId, metaDataId, messageId, messageStatus, null, false);
    }

    public Event(EventType eventType, String channelId, Integer metaDataId, Long messageId, Status messageStatus, Status oldMessageStatus, boolean queueEnabled) {
        if (eventType == null || channelId == null) {
            throw new NullPointerException();
        }

        this.eventType = eventType;
        this.channelId = channelId;
        this.messageId = messageId;
        this.metaDataId = metaDataId;
        this.messageStatus = messageStatus;
        this.oldMessageStatus = oldMessageStatus;
        this.queueEnabled = queueEnabled;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        if (eventType == null) {
            throw new NullPointerException();
        }

        this.eventType = eventType;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        if (channelId == null) {
            throw new NullPointerException();
        }

        this.channelId = channelId;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(int metaDataId) {
        this.metaDataId = metaDataId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public Status getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(Status messageStatus) {
        this.messageStatus = messageStatus;
    }

    public Status getOldMessageStatus() {
        return oldMessageStatus;
    }

    public void setOldMessageStatus(Status oldMessageStatus) {
        this.oldMessageStatus = oldMessageStatus;
    }

    public boolean isQueueEnabled() {
        return queueEnabled;
    }

    public void setQueueEnabled(boolean queueEnabled) {
        this.queueEnabled = queueEnabled;
    }

    public Calendar getEventDate() {
        return eventDate;
    }

    public void setEventDate(Calendar date) {
        this.eventDate = date;
    }

    public String toString() {
        return eventType.toString();
    }
}
