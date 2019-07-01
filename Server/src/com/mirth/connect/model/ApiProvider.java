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

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("apiProvider")
public class ApiProvider implements Serializable {

    public enum Type {
        SERVLET_INTERFACE_PACKAGE, SERVLET_INTERFACE, CORE_PACKAGE, SERVER_PACKAGE, CORE_CLASS, SERVER_CLASS;
    }

    @XStreamAsAttribute
    private Type type;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private String minVersion;

    @XStreamAsAttribute
    private String maxVersion;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}