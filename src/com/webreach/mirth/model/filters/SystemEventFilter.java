package com.webreach.mirth.model.filters;

import java.util.Calendar;

import com.webreach.mirth.model.SystemEvent.Level;

/**
 * A SystemEventFilter is used to search the system event log.
 * 
 * @author geraldb
 * 
 */
public class SystemEventFilter {
	private Level level;
	private Calendar startDate;
	private Calendar endDate;
	private String event;

	public String getEvent() {
		return this.event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public Level getLevel() {
		return this.level;
	}

	public void setLevel(Level level) {
		this.level = level;
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

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SystemEventFilter[");
		builder.append("level=" + getLevel() + ", ");
		builder.append("startDate=" + getStartDate() + ", ");
		builder.append("endDate=" + getEndDate() + ", ");
		builder.append("event=" + getEvent());
		builder.append("]");
		return builder.toString();
	}
}
