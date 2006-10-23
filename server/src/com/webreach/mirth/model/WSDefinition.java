package com.webreach.mirth.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wsif.schema.SchemaType;

public class WSDefinition {
	private List<WSOperation> operations;
	private Map<String,SchemaType> complexTypes;
	private String serviceEndpointURI;
	public WSDefinition() {
		this.operations = new ArrayList<WSOperation>();
	}

	public List<WSOperation> getOperations() {
		return this.operations;
	}

	public Map<String, SchemaType> getComplexTypes() {
		return complexTypes;
	}

	public void setComplexTypes(Map<String, SchemaType> complexTypes) {
		this.complexTypes = complexTypes;
	}

	public String getServiceEndpointURI() {
		return serviceEndpointURI;
	}

	public void setServiceEndpointURI(String serviceEndpointURI) {
		this.serviceEndpointURI = serviceEndpointURI;
	}

}
