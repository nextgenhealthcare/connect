package com.webreach.mirth.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wsif.schema.SchemaType;

public class WSDefinition {
	private Map<String,WSOperation> operations;
	private Map<String,SchemaType> complexTypes;
	private String serviceEndpointURI;
	public WSDefinition() {
		this.operations = new HashMap<String,WSOperation>();
	}
	public WSOperation getOperation(String name){
		return getOperations().get(name);
	}
	public Map<String,WSOperation> getOperations() {
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
