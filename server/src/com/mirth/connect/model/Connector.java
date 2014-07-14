/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Connector represents a connection to either a source or destination. Each
 * Connector has an associated Filter and Transformer. A connector is also of a
 * specific Transport type (TCP, HTTP, etc.).
 * 
 * 
 */

@XStreamAlias("connector")
public class Connector implements Serializable, Migratable, Purgable {
    public enum Mode {
        SOURCE, DESTINATION
    }

    private Integer metaDataId;
    private String name;
    private ConnectorProperties properties;
    private Transformer transformer;
    private Transformer responseTransformer;
    private Filter filter;
    private String transportName;
    private Mode mode;
    private boolean enabled;
    private boolean waitForPrevious = true;

    public Connector() {}

    public Connector(String name) {
        this.name = name;
    }

    public Integer getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(Integer metaDataId) {
        this.metaDataId = metaDataId;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
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

    public Transformer getResponseTransformer() {
        return responseTransformer;
    }

    public void setResponseTransformer(Transformer responseTransformer) {
        this.responseTransformer = responseTransformer;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getTransportName() {
        return this.transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public ConnectorProperties getProperties() {
        return this.properties;
    }

    public void setProperties(ConnectorProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }

    public boolean isWaitForPrevious() {
        return waitForPrevious;
    }

    public void setWaitForPrevious(boolean waitForPrevious) {
        this.waitForPrevious = waitForPrevious;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("metaDataId", metaDataId);
        purgedProperties.put("nameChars", PurgeUtil.countChars(name));
        purgedProperties.put("properties", properties.getPurgedProperties());
        purgedProperties.put("transformer", transformer.getPurgedProperties());
        purgedProperties.put("responseTransformer", transformer.getPurgedProperties());
        purgedProperties.put("filter", filter.getPurgedProperties());
        purgedProperties.put("transportName", transportName);
        purgedProperties.put("mode", mode);
        purgedProperties.put("enabled", enabled);
        purgedProperties.put("waitForPrevious", waitForPrevious);
        return purgedProperties;
    }
}
