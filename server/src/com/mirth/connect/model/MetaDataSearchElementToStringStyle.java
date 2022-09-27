package com.mirth.connect.model;

import org.apache.commons.lang3.builder.ToStringStyle;

public class MetaDataSearchElementToStringStyle extends ToStringStyle {
    public MetaDataSearchElementToStringStyle() {
        super();
        this.setUseShortClassName(true);
        this.setUseIdentityHashCode(false);
    }
    
    public static MetaDataSearchElementToStringStyle instance() {
        return new MetaDataSearchElementToStringStyle();
    }
}
