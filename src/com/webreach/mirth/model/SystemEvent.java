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

import java.sql.Timestamp;
import java.util.Properties;

public class SystemEvent {
	private int id;
	private int channelId;
	private int level = 0;
	private Timestamp date;
	private String event;
	private String description;
	private Properties attributes;

	public SystemEvent() {
		this.attributes = new Properties();
	}

	public SystemEvent(String event) {
		this.attributes = new Properties();
		this.description = event;
	}

	public SystemEvent(int channelId, String event) {
		this.attributes = new Properties();
		this.channelId = channelId;
		this.description = event;
	}

	public int getChannelId() {
		return this.channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public Timestamp getDate() {
		return this.date;
	}

	public void setDate(Timestamp date) {
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

	public int getLevel() {
		return this.level;
	}

	public void setLevel(int level) {
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

	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("System Event[");
		buffer.append("id=" + getId() + ", ");
		buffer.append("channelId=" + getChannelId() + ", ");
		buffer.append("level=" + getLevel() + ", ");
		buffer.append("event=" + getEvent() + ", ");
		buffer.append("description=" + getDescription() + ", ");
		buffer.append("date=" + getDate().toString());
		buffer.append("]");
		return buffer.toString();
	}

}
