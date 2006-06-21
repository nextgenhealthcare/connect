package com.webreach.mirth.model.filters;

import java.util.Calendar;

/**
 * A SystemEventFilter is used to search the system event log.
 * 
 * @author geraldb
 * 
 */
public class SystemEventFilter {
	private int channelId = -1;
	private int minLevel = -1;
	private int maxLevel = -1;
	private Calendar startDate;
	private Calendar endDate;
	private String event;

	public int getChannelId() {
		return this.channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public String getEvent() {
		return this.event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public int getMaxLevel() {
		return this.maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public int getMinLevel() {
		return this.minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
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

}
