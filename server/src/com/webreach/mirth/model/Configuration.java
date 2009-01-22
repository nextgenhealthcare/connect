package com.webreach.mirth.model;

import java.io.Serializable;
import java.util.Calendar;

public class Configuration implements Serializable {
	private String id;
	private Calendar dateCreated;
	private String data;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Calendar getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Calendar dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
	}
}