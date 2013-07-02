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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("connectorMetaData")
public class ConnectorMetaData extends MetaData implements Serializable {
    public enum Type {
        SOURCE, DESTINATION
    }

    private String serverClassName;
    private String sharedClassName;
    private String clientClassName;
    private String serviceClassName;
    private String transformers;
    private String protocol;
    private Type type;

    public String getServerClassName() {
        return this.serverClassName;
    }

    public void setServerClassName(String serverClassName) {
        this.serverClassName = serverClassName;
    }

    public String getSharedClassName() {
        return sharedClassName;
    }

    public void setSharedClassName(String sharedClassName) {
        this.sharedClassName = sharedClassName;
    }

    public String getClientClassName() {
        return clientClassName;
    }

    public void setClientClassName(String clientClassName) {
        this.clientClassName = clientClassName;
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public void setServiceClassName(String serviceClassName) {
        this.serviceClassName = serviceClassName;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTransformers() {
        return this.transformers;
    }

    public void setTransformers(String transformers) {
        this.transformers = transformers;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }

}
