/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.event;

import com.mirth.connect.donkey.model.event.ChannelEventType;
import com.mirth.connect.donkey.model.event.Event;

public class ChannelEvent extends Event {

    private String channelId;
    private ChannelEventType state;

    public ChannelEvent(String channelId, ChannelEventType state) {
        this.channelId = channelId;
        this.state = state;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public ChannelEventType getState() {
        return state;
    }

    public void setState(ChannelEventType state) {
        this.state = state;
    }
}
