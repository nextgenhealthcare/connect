package com.webreach.mirth.model;

import java.io.Serializable;
import java.util.Calendar;

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