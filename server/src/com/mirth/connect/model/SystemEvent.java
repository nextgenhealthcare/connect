/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.lang.ObjectUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A SystemEvent represents a system event which can be logged.
 * 
 */

@XStreamAlias("systemEvent")
public class SystemEvent implements Serializable {
	public enum Level {
		NORMAL, HIGH
	}

	private int id;
	private Calendar date;
	private Level level;
	private String event;
	private String description;
	private Properties attributes;

	public SystemEvent() {
		this.level = Level.NORMAL;
		this.description = new String();
		this.attributes = new Properties();
	}
	
	public SystemEvent(String event) {
		this.event = event;
		this.level = Level.NORMAL;
		this.description = new String();
		this.attributes = new Properties();
	}

	public Calendar getDate() {
		return this.date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String event) {
		this.description = event;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Level getLevel() {
		return this.level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public Properties getAttributes() {
		return this.attributes;
	}

	public void setAttributes(Properties attributes) {
		this.attributes = attributes;
	}

	public String getEvent() {
		return this.event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof SystemEvent)) {
			return false;
		}
		
		SystemEvent systemEvent = (SystemEvent) that;
		
		return
			ObjectUtils.equals(this.getId(), systemEvent.getId()) &&
			ObjectUtils.equals(this.getLevel(), systemEvent.getLevel()) &&
			ObjectUtils.equals(this.getEvent(), systemEvent.getEvent()) &&
			ObjectUtils.equals(this.getDescription(), systemEvent.getDescription()) &&
			ObjectUtils.equals(this.getDate(), systemEvent.getDate()) &&
			ObjectUtils.equals(this.getAttributes(), systemEvent.getAttributes());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("id=" + getId() + ", ");
		builder.append("level=" + getLevel() + ", ");
		builder.append("event=" + getEvent() + ", ");
		builder.append("description=" + getDescription() + ", ");
		builder.append("date=" + getDate());
		builder.append("]");
		return builder.toString();
	}
}
