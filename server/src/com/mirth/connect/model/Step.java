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

import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.util.ScriptBuilderException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("step")
public abstract class Step implements Serializable, Purgable {

    private String name;
    private int sequenceNumber;

    public abstract String getScript(boolean loadFiles) throws ScriptBuilderException;

    public abstract String getType();

    @Override
    public abstract Step clone();

    public Collection<String> getResponseVariables() {
        return null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
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
        return purgedProperties;
    }
}
