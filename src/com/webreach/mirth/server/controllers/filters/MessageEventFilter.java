package com.webreach.mirth.server.controllers.filters;

import java.sql.Timestamp;

public class MessageEventFilter {
	private int id = -1;
	private int channelId = -1;
	private Timestamp minDate;
	private Timestamp maxDate;
	private String sendingFacility;
	private String event;
	private String controlId;

	public int getChannelId() {
		return this.channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public String getControlId() {
		return this.controlId;
	}

	public void setControlId(String controlId) {
		this.controlId = controlId;
	}

	public String getEvent() {
		return this.event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getMaxDate() {
		return this.maxDate;
	}

	public void setMaxDate(Timestamp maxDate) {
		this.maxDate = maxDate;
	}

	public Timestamp getMinDate() {
		return this.minDate;
	}

	public void setMinDate(Timestamp minDate) {
		this.minDate = minDate;
	}

	public String getSendingFacility() {
		return this.sendingFacility;
	}

	public void setSendingFacility(String sendingFacility) {
		this.sendingFacility = sendingFacility;
	}
}
