package com.webreach.mirth.core;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.webreach.mirth.core.util.JMXConnection;

public class Channel {
	public enum Status { STARTED, STOPPED, PAUSED };
	public enum Direction { INBOUND, OUTBOUND };
	public enum Type { ROUTER, BROADCAST, APPLICATION };
	
	private int id;
	private String name;
	private String description;
	private boolean enabled;
	private boolean modified;
	private Direction direction;
	private Type type;
	private Filter filter;
	private Connector sourceConnector;
	private List<Connector> destinationConnectors;
	private Statistics statistics;
	private Validator validator;
	
	private JMXConnection jmxConnection;
	
	public Channel() {
		destinationConnectors = new ArrayList<Connector>();
		statistics = new Statistics(this);
		
		try {
			jmxConnection = new JMXConnection();	
		} catch (Exception e) {
			// TODO: handle this exception
		}
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Filter getFilter() {
		return this.filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isModified() {
		return this.modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Connector getSourceConnector() {
		return this.sourceConnector;
	}

	public void setSourceConnector(Connector sourceConnector) {
		this.sourceConnector = sourceConnector;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Validator getValidator() {
		return this.validator;
	}

	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	public List<Connector> getDestinationConnectors() {
		return this.destinationConnectors;
	}

	public Statistics getStatistics() {
		return this.statistics;
	}
	
	public void start() {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", getId() + "ComponentService");
			jmxConnection.invokeOperation(properties, "start", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", getId() + "ComponentService");
			jmxConnection.invokeOperation(properties, "stop", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pause() {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", getId() + "ComponentService");
			jmxConnection.invokeOperation(properties, "pause", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resume() {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", getId() + "ComponentService");
			jmxConnection.invokeOperation(properties, "resume", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Status getStatus() {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", getId() + "ComponentService");
			
			if ((Boolean) jmxConnection.getAttribute(properties, "Paused")) {
				return Status.PAUSED;
			} else if ((Boolean) jmxConnection.getAttribute(properties, "Stopped")) {
				return Status.STOPPED;
			} else {
				return Status.STARTED;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Status.STOPPED;
		}
	}
	
	public void store(OutputStream os) {
		// TODO: convert to DOM object, can use StringOutputStream to write it to a string
	}

}
