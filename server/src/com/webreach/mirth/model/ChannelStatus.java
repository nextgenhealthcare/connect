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

/**
 * A ChannelStatus represents the status of a deployed Channel.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class ChannelStatus implements Serializable {
	public enum State {
		STARTED, STOPPED, PAUSED
	};

	private String channelId;
	private String name;
	private State state;

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

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof ChannelStatus)) {
			return false;
		}
		
		ChannelStatus status = (ChannelStatus) that;
		
		return
			EqualsUtil.areEqual(this.getChannelId(), status.getChannelId()) &&
			EqualsUtil.areEqual(this.getName(), status.getName()) &&
			EqualsUtil.areEqual(this.getState(), status.getState());
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
