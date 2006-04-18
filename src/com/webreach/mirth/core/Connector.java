package com.webreach.mirth.core;

import java.util.Map;

public class Connector {
	private Transport transport;
	private String name;
	private Map<String, String> properties;
	private Transformer transformer;
	
	public Connector() {
		
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

	public Transport getTransport() {
		return this.transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
}
