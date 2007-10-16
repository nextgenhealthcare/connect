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

import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.MessageObject.Status;

/**
 * A MessageObjectFilter is used to search the message store.
 * 
 * @author geraldb
 * 
 */
public class MessageObjectFilter {
	private String id;
	private String channelId;
	private Calendar startDate;
	private Calendar endDate;
	private Status status;
	private String source;
	private String connectorName;
	private boolean searchRawData;
	private boolean searchTransformedData;
	private boolean searchEncodedData;
	private String searchCriteria;
	private String type;
	private Protocol protocol;

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

	public Calendar getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getConnectorName() {
		return this.connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Protocol getProtocol() {
		return this.protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}
	
	public String getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(String searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

	public boolean isSearchEncodedData() {
		return searchEncodedData;
	}

	public void setSearchEncodedData(boolean searchEncodedData) {
		this.searchEncodedData = searchEncodedData;
	}

	public boolean isSearchRawData() {
		return searchRawData;
	}

	public void setSearchRawData(boolean searchRawData) {
		this.searchRawData = searchRawData;
	}

	public boolean isSearchTransformedData() {
		return searchTransformedData;
	}

	public void setSearchTransformedData(boolean searchTransformedData) {
		this.searchTransformedData = searchTransformedData;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("id=" + getId() + ", ");
		builder.append("channelId=" + getChannelId() + ", ");
		builder.append("source=" + getSource() + ", ");
		builder.append("type=" + getType() + ", ");
		builder.append("startDate=" + String.format("%1$tY-%1$tm-%1$td", getStartDate()) + ", ");
		builder.append("endDate=" + String.format("%1$tY-%1$tm-%1$td", getEndDate()) + ", ");
		builder.append("status=" + getStatus() + ", ");
		builder.append("protocol=" + getProtocol() + ", ");
		builder.append("searchCriteria=" + getSearchCriteria() + ", ");
		builder.append("searchEncodedData=" + isSearchEncodedData() + ", ");
		builder.append("searchRawData=" + isSearchRawData() + ", ");
		builder.append("searchTransformedData=" + isSearchTransformedData() + ", ");
		builder.append("connectorName=" + getConnectorName());
		builder.append("]");
		return builder.toString();
	}
}
