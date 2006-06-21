package com.webreach.mirth.model.filters;

import java.util.Calendar;

/**
 * A MessageEventFilter is used to search the message event logs.
 * 
 * @author geraldb
 * 
 */
public class MessageEventFilter {
	private int channelId = -1;
	private Calendar startDate;
	private Calendar endDate;
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

	public Calendar getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public Calendar getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public String getSendingFacility() {
		return this.sendingFacility;
	}

	public void setSendingFacility(String sendingFacility) {
		this.sendingFacility = sendingFacility;
	}
}
