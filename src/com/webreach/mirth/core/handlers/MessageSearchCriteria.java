package com.webreach.mirth.core.handlers;

public class MessageSearchCriteria {
	private String channelName;
	private int minId;
	private int maxId;
	private String minDate;
	private String maxDate;

	public String getChannelName() {
		return this.channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getMaxDate() {
		return this.maxDate;
	}

	public void setMaxDate(String maxDate) {
		this.maxDate = maxDate;
	}

	public int getMaxId() {
		return this.maxId;
	}

	public void setMaxId(int maxId) {
		this.maxId = maxId;
	}

	public String getMinDate() {
		return this.minDate;
	}

	public void setMinDate(String minDate) {
		this.minDate = minDate;
	}

	public int getMinId() {
		return this.minId;
	}

	public void setMinId(int minId) {
		this.minId = minId;
	}

}
