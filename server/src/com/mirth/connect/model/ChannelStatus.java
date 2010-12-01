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
import java.util.Calendar;

import org.apache.commons.lang.ObjectUtils;

/**
 * A ChannelStatus represents the status of a deployed Channel.
 * 
 */
public class ChannelStatus implements Serializable {
	public enum State {
		STARTED, STOPPED, PAUSED
	};

	private String channelId;
	private String name;
	private State state;
	private int deployedRevisionDelta;
	private Calendar deployedDate;

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public State getState() {
		return this.state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
    public void setDeployedDate(Calendar deployedDate) {
        this.deployedDate = deployedDate;
    }

    public Calendar getDeployedDate() {
        return this.deployedDate;
    }

    public void setDeployedRevisionDelta(int deployedRevisionDelta) {
        this.deployedRevisionDelta = deployedRevisionDelta;
    }

    public int getDeployedRevisionDelta() {
        return this.deployedRevisionDelta;
    }

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof ChannelStatus)) {
			return false;
		}
		
		ChannelStatus status = (ChannelStatus) that;
		
		return
			ObjectUtils.equals(this.getChannelId(), status.getChannelId()) &&
			ObjectUtils.equals(this.getName(), status.getName()) &&
			ObjectUtils.equals(this.getState(), status.getState());
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("channeldId=" + getChannelId() + ", ");
		builder.append("name=" + getName() + ", ");
		builder.append("state=" + getState().toString());
		builder.append("]");
		return builder.toString();
	}
}
