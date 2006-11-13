package com.webreach.mirth.model;

import java.util.Calendar;

public class MonitorMessageObject {
	private String id;
	private String channelId;
	private Calendar dateCreated;
	private MessageObject.Status status;
	private String version;
	private String type;
	private String subType;

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public Calendar getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Calendar dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MessageObject.Status getStatus() {
		return this.status;
	}

	public void setStatus(MessageObject.Status status) {
		this.status = status;
	}

	public String getSubType() {
		return this.subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
