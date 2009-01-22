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
