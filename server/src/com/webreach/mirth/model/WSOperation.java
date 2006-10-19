package com.webreach.mirth.model;

import java.util.List;

public class WSOperation {
	private String name;
	private List<WSParameter> parameters;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WSParameter> getParameters() {
		return this.parameters;
	}

	public void setParameters(List<WSParameter> parameters) {
		this.parameters = parameters;
	}
}
