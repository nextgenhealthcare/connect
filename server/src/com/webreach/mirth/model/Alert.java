package com.webreach.mirth.model;

import java.util.List;

public class Alert {
	private String id;
	private String name;
	private String expression;
	private String template;
	private List<String> channels;
	private List<String> emails;

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
}
