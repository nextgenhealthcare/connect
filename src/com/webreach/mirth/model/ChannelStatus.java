package com.webreach.mirth.model;

/**
 * A ChannelStatus represents the status of a deployed Channel.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class ChannelStatus {
	public enum State {
		STARTED, STOPPED, PAUSED
	};

	private int channelId;
	private String name;
	private State state;

	public int getChannelId() {
		return this.channelId;
	}

	public void setChannelId(int channelId) {
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

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelStatus[");
		builder.append("channeldId=" + getChannelId() + ", ");
		builder.append("name=" + getName() + ", ");
		builder.append("state=" + getState().toString());
		builder.append("]");
		return builder.toString();
	}
}
