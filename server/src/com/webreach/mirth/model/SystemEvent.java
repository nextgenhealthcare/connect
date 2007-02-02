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
import java.util.Calendar;
import java.util.Properties;

import com.webreach.mirth.util.EqualsUtil;

/**
 * A SystemEvent represents a system event which can be logged.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
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
			EqualsUtil.areEqual(this.getId(), systemEvent.getId()) &&
			EqualsUtil.areEqual(this.getLevel(), systemEvent.getLevel()) &&
			EqualsUtil.areEqual(this.getEvent(), systemEvent.getEvent()) &&
			EqualsUtil.areEqual(this.getDescription(), systemEvent.getDescription()) &&
			EqualsUtil.areEqual(this.getDate(), systemEvent.getDate()) &&
			EqualsUtil.areEqual(this.getAttributes(), systemEvent.getAttributes());
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
