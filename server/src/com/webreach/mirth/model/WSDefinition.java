package com.webreach.mirth.model;

import java.util.ArrayList;
import java.util.List;

public class WSDefinition {
	private List<WSOperation> operations;
	private String serviceEndpoint;
	public WSDefinition() {
		this.operations = new ArrayList<WSOperation>();
	}

	public List<WSOperation> getOperations() {
		return this.operations;
	}

	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}
}
