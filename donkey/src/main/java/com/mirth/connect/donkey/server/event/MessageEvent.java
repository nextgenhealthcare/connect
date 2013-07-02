/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.model.event.MessageEventType;

public class MessageEvent extends Event {

    private String channelId;
    private Integer metaDataId;
    private MessageEventType type;
    private Long count;
    private boolean statUpdate;

    public MessageEvent(String channelId, Integer metaDataId, MessageEventType type, Long count, boolean statUpdate) {
        this.channelId = channelId;
        this.metaDataId = metaDataId;
        this.type = type;
        this.count = count;
        this.statUpdate = statUpdate;
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

    public MessageEventType getType() {
        return type;
    }

    public void setType(MessageEventType type) {
        this.type = type;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public boolean isStatUpdate() {
        return statUpdate;
    }

    public void setStatUpdate(boolean statUpdate) {
        this.statUpdate = statUpdate;
    }
}
