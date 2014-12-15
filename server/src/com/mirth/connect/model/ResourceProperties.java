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
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;

public abstract class ResourceProperties implements Serializable, Migratable, Purgable {

    public static final String DEFAULT_RESOURCE_ID = "Default Resource";
    public static final String DEFAULT_RESOURCE_NAME = "[Default Resource]";

    private String pluginPointName;
    private String type;
    private String id;
    private String name;
    private String description;
    private boolean includeWithGlobalScripts;

    public ResourceProperties(String pluginPointName, String type) {
        this.pluginPointName = pluginPointName;
        this.type = type;
        this.id = UUID.randomUUID().toString();
    }

    public String getPluginPointName() {
        return pluginPointName;
    }

    protected void setPluginPointName(String pluginPointName) {
        this.pluginPointName = pluginPointName;
    }

    public String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIncludeWithGlobalScripts() {
        return includeWithGlobalScripts;
    }

    public void setIncludeWithGlobalScripts(boolean includeWithGlobalScripts) {
        this.includeWithGlobalScripts = includeWithGlobalScripts;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("type", type);
        return purgedProperties;
    }
}