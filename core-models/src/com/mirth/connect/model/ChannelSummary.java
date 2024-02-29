/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelSummary")
public class ChannelSummary implements Serializable {
    private String channelId;
    private boolean deleted;
    private boolean undeployed;
    private ChannelStatus channelStatus;

    public ChannelSummary(String channelId) {
        this.channelId = channelId;
        this.channelStatus = new ChannelStatus();
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isUndeployed() {
        return undeployed;
    }

    public void setUndeployed(boolean undeployed) {
        this.undeployed = undeployed;
    }

    public ChannelStatus getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(ChannelStatus channelStatus) {
        this.channelStatus = channelStatus;
    }
}