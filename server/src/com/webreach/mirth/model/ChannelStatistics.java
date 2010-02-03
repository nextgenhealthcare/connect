/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model;

import java.io.Serializable;

import com.webreach.mirth.util.EqualsUtil;

public class ChannelStatistics implements Serializable {
    private String serverId;
    private String channelId;
	private int received = 0;
	private int sent = 0;
	private int error = 0;
	private int filtered = 0;
	private int queued = 0;
	private int alerted = 0;
	

    public String getServerId()
    {
        return serverId;
    }

    public void setServerId(String serverId)
    {
        this.serverId = serverId;
    }
    
	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public int getReceived() {
		return this.received;
	}

	public void setReceived(int receivedCount) {
		this.received = receivedCount;
	}
    
    public int getFiltered() {
        return filtered;
    }

    public void setFiltered(int filteredCount) {
        this.filtered = filteredCount;
    }

    public int getQueued() {
        return this.queued;
    }

    public void setQueued(int queuedCount) {
        this.queued = queuedCount;
    }
    
	public int getSent() {
		return this.sent;
	}

	public void setSent(int sentCount) {
		this.sent = sentCount;
	}
    
    public int getError() {
        return this.error;
    }

    public void setError(int errorCount) {
        this.error = errorCount;
    }
    
    public int getAlerted() {
        return this.alerted;
    }

    public void setAlerted(int alerted) {
        this.alerted = alerted;
    }

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof ChannelStatistics)) {
			return false;
		}
		
		ChannelStatistics statistic = (ChannelStatistics) that;
		
		return
            EqualsUtil.areEqual(this.getServerId(), statistic.getServerId()) &&
			EqualsUtil.areEqual(this.getChannelId(), statistic.getChannelId()) &&
			EqualsUtil.areEqual(this.getReceived(), statistic.getReceived()) &&
			EqualsUtil.areEqual(this.getSent(), statistic.getSent()) &&
			EqualsUtil.areEqual(this.getError(), statistic.getError()) &&
			EqualsUtil.areEqual(this.getFiltered(), statistic.getReceived()) &&
			EqualsUtil.areEqual(this.getQueued(), statistic.getQueued()) &&
			EqualsUtil.areEqual(this.getAlerted(), statistic.getAlerted());
	}	

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
        builder.append("serverId=" + getServerId() + ", ");
		builder.append("channelId=" + getChannelId() + ", ");
		builder.append("received=" + getReceived() + ", ");
		builder.append("filtered=" + getFiltered() + ", ");
		builder.append("sent=" + getSent() + ", ");
		builder.append("queued=" + getQueued() + ", ");
		builder.append("error=" + getError() + ", ");
		builder.append("alerted=" + getAlerted());
		builder.append("]");
		return builder.toString();
	}
}
