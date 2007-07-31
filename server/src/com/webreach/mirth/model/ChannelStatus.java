/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


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
