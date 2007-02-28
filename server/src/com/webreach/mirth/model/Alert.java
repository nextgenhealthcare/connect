package com.webreach.mirth.model;

import java.util.ArrayList;
import java.util.List;

import com.webreach.mirth.util.EqualsUtil;

public class Alert {
	private String id;
	private String name;
	private String expression;
	private String template;
	private boolean enabled;
	private List<String> channels;
	private List<String> emails;

	public Alert() {
		channels = new ArrayList<String>();
		emails = new ArrayList<String>();
	}
	
	public List<String> getChannels() {
		return this.channels;
	}

	public void setChannels(List<String> channels) {
		this.channels = channels;
	}

	public List<String> getEmails() {
		return this.emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public String getExpression() {
		return this.expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof Alert)) {
			return false;
		}
		
		Alert alert = (Alert) that;
		
		return
			EqualsUtil.areEqual(this.getId(), alert.getId()) &&
			EqualsUtil.areEqual(this.isEnabled(), alert.isEnabled()) &&
			EqualsUtil.areEqual(this.getExpression(), alert.getExpression()) &&
			EqualsUtil.areEqual(this.getTemplate(), alert.getTemplate()) &&
			EqualsUtil.areEqual(this.getChannels(), alert.getChannels()) &&
			EqualsUtil.areEqual(this.getEmails(), alert.getEmails());
	}
}
