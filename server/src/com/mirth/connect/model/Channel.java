/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.mirth.connect.util.EqualsUtil;

/**
 * A Channel is the main element of the Mirth architecture. Channels connect a
 * single source with multiple destinations which are represented by Connectors.
 * 
 */
public class Channel implements Serializable {
	private String id;
	private String name;
	private String description;
	private boolean enabled;
	private String version;
	private Calendar lastModified;
	private int revision;
	private Connector sourceConnector;
	private List<Connector> destinationConnectors = new ArrayList<Connector>();
	private Properties properties = new Properties();
	private String preprocessingScript;
    private String postprocessingScript;
    private String deployScript;
    private String shutdownScript;

	public Channel() {

	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getRevision() {
		return this.revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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

	public List<Connector> getDestinationConnectors() {
		return this.destinationConnectors;
	}
	
	public List<Connector> getEnabledDestinationConnectors() {
		List<Connector> enabledConnectors = new ArrayList<Connector>();
		for(Connector connector : getDestinationConnectors()) {
			if(connector.isEnabled()) { 
				enabledConnectors.add(connector);
			}
		}
		return enabledConnectors;
	}

	public void setDestinationConnectors(List<Connector> destinationConnectors) {
		this.destinationConnectors = destinationConnectors;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
    
    public String getPostprocessingScript() {
        return postprocessingScript;
    }

    public void setPostprocessingScript(String postprocessingScript) {
        this.postprocessingScript = postprocessingScript;
    }

    public String getPreprocessingScript() {
        return preprocessingScript;
    }

    public void setPreprocessingScript(String preprocessingScript) {
        this.preprocessingScript = preprocessingScript;
    }
	
    public String getDeployScript() {
        return this.deployScript;
    }

    public void setDeployScript(String deployScript) {
        this.deployScript = deployScript;
    }
    
    public String getShutdownScript() {
        return this.shutdownScript;
    }

    public void setShutdownScript(String shutdownScript) {
        this.shutdownScript = shutdownScript;
    }
    
	public Calendar getLastModified() {
		return lastModified;
	}

	public void setLastModified(Calendar lastModified) {
		this.lastModified = lastModified;
	}
    
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		
		if (!(that instanceof Channel)) {
			return false;
		}

		Channel channel = (Channel) that;
			
		return
			EqualsUtil.areEqual(this.getId(), channel.getId()) &&
			EqualsUtil.areEqual(this.getName(), channel.getName()) &&
			EqualsUtil.areEqual(this.getDescription(), channel.getDescription()) &&
			EqualsUtil.areEqual(this.isEnabled(), channel.isEnabled()) &&
			EqualsUtil.areEqual(this.getLastModified(), channel.getLastModified()) &&
			EqualsUtil.areEqual(this.getVersion(), channel.getVersion()) &&
			EqualsUtil.areEqual(this.getRevision(), channel.getRevision()) &&
			EqualsUtil.areEqual(this.getSourceConnector(), channel.getSourceConnector()) &&
			EqualsUtil.areEqual(this.getDestinationConnectors(), channel.getDestinationConnectors()) &&
			EqualsUtil.areEqual(this.getProperties(), channel.getProperties()) &&
            EqualsUtil.areEqual(this.getShutdownScript(), channel.getShutdownScript()) &&
            EqualsUtil.areEqual(this.getDeployScript(), channel.getDeployScript()) &&
            EqualsUtil.areEqual(this.getPostprocessingScript(), channel.getPostprocessingScript()) &&
			EqualsUtil.areEqual(this.getPreprocessingScript(), channel.getPreprocessingScript());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName() + "[");
		builder.append("id=" + getId() + ", ");
		builder.append("name=" + getName() + ", ");
		builder.append("enabled=" + isEnabled() + ", ");
		builder.append("version=" + getVersion());
		builder.append("]");
		return builder.toString();
	}
}
