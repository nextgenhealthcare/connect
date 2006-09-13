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

import com.webreach.mirth.model.MessageObject;

/**
 * A MessageObjectSFilter is used to search the message store.
 * 
 * @author geraldb
 * 
 */
public class MessageObjectFilter {
	private String id;
	private String channelId;
	private Calendar startDate;
	private Calendar endDate;
	private MessageObject.Status status;
	private int page = -1;
	private int pageSize = -1;

	public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Calendar getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Calendar getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public MessageObject.Status getStatus() {
		return this.status;
	}

	public void setStatus(MessageObject.Status status) {
		this.status = status;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessageEventFilter[");
		builder.append("id=" + getChannelId() + ", ");
		builder.append("channelId=" + getChannelId() + ", ");
		builder.append("startDate=" + getStartDate() + ", ");
		builder.append("endDate=" + getEndDate() + ", ");
		builder.append("status=" + getStatus() + ", ");
		builder.append("page=" + getPage() + ", ");
		builder.append("pageSize=" + getPageSize() + ", ");
		builder.append("]");
		return builder.toString();
	}
}
