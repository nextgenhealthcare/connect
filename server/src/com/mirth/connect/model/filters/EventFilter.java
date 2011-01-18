/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.filters;

import java.util.Calendar;

import com.mirth.connect.model.Event.Level;

/**
 * A SystemEventFilter is used to search the system event log.
 * 
 * @author geraldb
 * 
 */
public class EventFilter {
	private Integer id;
	private Level level;
	private Calendar startDate;
	private Calendar endDate;
	private String event;
	
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

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
		builder.append(this.getClass().getName() + "[");
		builder.append("id=" + getId() + ", ");
		builder.append("level=" + getLevel() + ", ");
		builder.append("startDate=" + String.format("%1$tY-%1$tm-%1$td", getStartDate()) + ", ");
		builder.append("endDate=" + String.format("%1$tY-%1$tm-%1$td", getEndDate()) + ", ");
		builder.append("event=" + getEvent());
		builder.append("]");
		return builder.toString();
	}
}
