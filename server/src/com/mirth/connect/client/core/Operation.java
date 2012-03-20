package com.mirth.connect.client.core;

import org.apache.commons.lang.builder.EqualsBuilder;

public class Operation {
    private String name;
    private String displayName;
    private boolean auditable;

    public Operation(String name, String displayName, boolean auditable) {
        this.name = name;
        this.displayName = displayName;
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

    public boolean isAuditable() {
        return auditable;
    }

    public void setAuditable(boolean auditable) {
        this.auditable = auditable;
    }
    
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
