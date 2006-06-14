package com.webreach.mirth.server.controllers.filters;

import java.sql.Timestamp;

/**
 * A SystemEventFilter is used to search the system event log.
 * 
 * @author geraldb
 *
 */
public class SystemEventFilter {
	private int id = -1;
	private int channelId = -1;
	private int minLevel = -1;
	private int maxLevel = -1;
	private Timestamp minDate;
	private Timestamp maxDate;
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

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
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
}
