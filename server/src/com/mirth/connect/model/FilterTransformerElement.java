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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.util.ScriptBuilderException;

public abstract class FilterTransformerElement implements Serializable, Purgable, Migratable {

    private String name;
    private String sequenceNumber;
    private boolean enabled;

    public FilterTransformerElement() {
        enabled = true;
    }

    public FilterTransformerElement(FilterTransformerElement props) {
        name = props.getName();
        sequenceNumber = props.getSequenceNumber();
        enabled = props.isEnabled();
    }

    public abstract String getScript(boolean loadFiles) throws ScriptBuilderException;

    public abstract String getType();

    @Override
    public abstract FilterTransformerElement clone();

    public Collection<String> getResponseVariables() {
        return null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, CalendarToStringStyle.instance());
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("sequenceNumber", sequenceNumber);
        purgedProperties.put("enabled", enabled);
        return purgedProperties;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // @formatter:off
    @Override public void migrate3_0_1(DonkeyElement element) {}
    @Override public void migrate3_0_2(DonkeyElement element) {}
    @Override public void migrate3_1_0(DonkeyElement element) {}
    @Override public void migrate3_2_0(DonkeyElement element) {}
    @Override public void migrate3_3_0(DonkeyElement element) {}
    @Override public void migrate3_4_0(DonkeyElement element) {}
    @Override public void migrate3_5_0(DonkeyElement element) {}
    @Override public void migrate3_6_0(DonkeyElement element) {} // @formatter:on

    @Override
    public void migrate3_7_0(DonkeyElement element) {
        element.addChildElement("enabled", Boolean.toString(Boolean.TRUE));
    }
    
    // @formatter:off
    @Override public void migrate3_9_0(DonkeyElement element) {} // @formatter:on
}