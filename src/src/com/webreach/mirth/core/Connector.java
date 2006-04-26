package com.webreach.mirth.core;

import java.util.HashMap;
import java.util.Map;

public class Connector {
	private String name;
	private Map<String, String> properties;
	private Transformer transformer;
	private String transport;
	
	public Connector() {
		properties = new HashMap<String, String>();
	}
	
	public Connector(String name) {
		setName(name);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Transformer getTransformer() {
		return this.transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public String getTransport() {
		return this.transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
}
