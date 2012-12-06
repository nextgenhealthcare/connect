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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Channel is the main element of the Mirth architecture. Channels connect a
 * single source with multiple destinations which are represented by Connectors.
 * 
 */

@XStreamAlias("channel")
public class Channel implements Serializable, Auditable {
    private String id;
    private String name;
    private String description;
    private boolean enabled;
    private String version;
    private Calendar lastModified;
    private int revision;
    private Connector sourceConnector;
    private List<Connector> destinationConnectors;
    private Set<String> tags;
    private String preprocessingScript;
    private String postprocessingScript;
    private String deployScript;
    private String shutdownScript;
    private ChannelProperties properties;

    public Channel() {
        enabled = true;
        destinationConnectors = new ArrayList<Connector>();
        tags = new HashSet<String>();
        properties = new ChannelProperties();
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
        sourceConnector.setMetaDataId(0);
        this.sourceConnector = sourceConnector;
    }

    public List<Connector> getDestinationConnectors() {
        return this.destinationConnectors;
    }

    public void addDestination(Connector destinationConnector) {
        destinationConnector.setMetaDataId(getNextMetaDataId());
        destinationConnectors.add(destinationConnector);
    }

    public List<Connector> getEnabledDestinationConnectors() {
        List<Connector> enabledConnectors = new ArrayList<Connector>();
        for (Connector connector : getDestinationConnectors()) {
            if (connector.isEnabled()) {
                enabledConnectors.add(connector);
            }
        }
        return enabledConnectors;
    }

    public Set<String> getTags() {
        return tags;
    }
    
    public void setTags(Set<String> tags) {
        this.tags = tags;
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

    public ChannelProperties getProperties() {
        return properties;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof Channel)) {
            return false;
        }

        Channel channel = (Channel) that;

        return ObjectUtils.equals(this.getId(), channel.getId()) && ObjectUtils.equals(this.getName(), channel.getName()) && ObjectUtils.equals(this.getDescription(), channel.getDescription()) && ObjectUtils.equals(this.isEnabled(), channel.isEnabled()) && ObjectUtils.equals(this.getLastModified(), channel.getLastModified()) && ObjectUtils.equals(this.getVersion(), channel.getVersion()) && ObjectUtils.equals(this.getRevision(), channel.getRevision()) && ObjectUtils.equals(this.getSourceConnector(), channel.getSourceConnector()) && ObjectUtils.equals(this.getDestinationConnectors(), channel.getDestinationConnectors()) && ObjectUtils.equals(this.getShutdownScript(), channel.getShutdownScript()) && ObjectUtils.equals(this.getDeployScript(), channel.getDeployScript()) && ObjectUtils.equals(this.getPostprocessingScript(), channel.getPostprocessingScript()) && ObjectUtils.equals(this.getPreprocessingScript(), channel.getPreprocessingScript());
    }

    public String toString() {
        return new ToStringBuilder(this, CalendarToStringStyle.instance()).append("name", name).append("enabled", enabled).append("version", version).toString();
    }

    public String toAuditString() {
        return new ToStringBuilder(this, CalendarToStringStyle.instance()).append("id", id).append("name", name).toString();
    }

    private int getNextMetaDataId() {
        int nextMetaDataId = 1;

        for (Connector destinationConnector : destinationConnectors) {
            Integer metaDataId = destinationConnector.getMetaDataId();

            if (metaDataId >= nextMetaDataId) {
                nextMetaDataId = metaDataId + 1;
            }
        }

        return nextMetaDataId;
    }
}
