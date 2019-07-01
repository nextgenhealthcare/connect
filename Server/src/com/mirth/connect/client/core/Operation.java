/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Operation {
    private String name;
    private String displayName;
    private ExecuteType executeType;
    private boolean auditable;

    public enum ExecuteType {
        SYNC, ASYNC, ABORT_PENDING
    }

    public Operation(String name, String displayName, ExecuteType executeType, boolean auditable) {
        this.name = name;
        this.displayName = displayName;
        this.executeType = executeType;
        this.auditable = auditable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ExecuteType getExecuteType() {
        return executeType;
    }

    public void setExecuteType(ExecuteType executeType) {
        this.executeType = executeType;
    }

    public boolean isAuditable() {
        return auditable;
    }

    public void setAuditable(boolean auditable) {
        this.auditable = auditable;
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, "executeType");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "executeType");
    }
}
