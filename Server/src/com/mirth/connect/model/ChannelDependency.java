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

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelDependency")
public class ChannelDependency implements Serializable {

    private String dependentId;
    private String dependencyId;

    public ChannelDependency(String dependentId, String dependencyId) {
        this.dependentId = dependentId;
        this.dependencyId = dependencyId;
    }

    public String getDependentId() {
        return dependentId;
    }

    public void setDependentId(String dependentId) {
        this.dependentId = dependentId;
    }

    public String getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(String dependencyId) {
        this.dependencyId = dependencyId;
    }

    @Override
    public int hashCode() {
        return dependentId.hashCode() ^ dependencyId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return dependentId + " dependent on " + dependencyId;
    }
}