/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.Calendar;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelSummary")
public class ChannelSummary implements Serializable {
	private String id;
	private boolean added;
	private boolean deleted;
	private Calendar lastUpdated;

	public ChannelSummary() {
		this.added = false;
		this.deleted = false;
	}

	public boolean isAdded() {
		return this.added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Calendar getLastUpdated() {
		return this.lastUpdated;
	}

	public void setLastUpdated(Calendar lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}