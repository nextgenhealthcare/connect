/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelStatistics")
public class ChannelStatistics implements Serializable {
    private String serverId;
    private String channelId;
    private long received = 0L;
    private long sent = 0L;
    private long error = 0L;
    private long filtered = 0L;
    private long queued = 0L;
    private long alerted = 0L;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getReceived() {
        return this.received;
    }

    public void setReceived(long receivedCount) {
        this.received = receivedCount;
    }

    public long getFiltered() {
        return filtered;
    }

    public void setFiltered(long filteredCount) {
        this.filtered = filteredCount;
    }

    public long getQueued() {
        return this.queued;
    }

    public void setQueued(long queuedCount) {
        this.queued = queuedCount;
    }

    public long getSent() {
        return this.sent;
    }

    public void setSent(long sentCount) {
        this.sent = sentCount;
    }

    public long getError() {
        return this.error;
    }

    public void setError(long errorCount) {
        this.error = errorCount;
    }

    public long getAlerted() {
        return this.alerted;
    }

    public void setAlerted(long alerted) {
        this.alerted = alerted;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
