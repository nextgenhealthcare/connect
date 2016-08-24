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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.donkey.util.purge.Purgable;

public class ChannelExportData implements Serializable, Purgable {

    private ChannelMetadata metadata;
    private List<CodeTemplateLibrary> codeTemplateLibraries;
    private Set<String> dependentIds;
    private Set<String> dependencyIds;

    public ChannelMetadata getMetadata() {
        if (metadata == null) {
            metadata = new ChannelMetadata();
        }
        return metadata;
    }

    public void setMetadata(ChannelMetadata metadata) {
        this.metadata = metadata;
    }

    public void clearMetadata() {
        metadata = null;
    }

    public List<CodeTemplateLibrary> getCodeTemplateLibraries() {
        if (codeTemplateLibraries == null) {
            codeTemplateLibraries = new ArrayList<CodeTemplateLibrary>();
        }
        return codeTemplateLibraries;
    }

    public void setCodeTemplateLibraries(List<CodeTemplateLibrary> codeTemplateLibraries) {
        this.codeTemplateLibraries = codeTemplateLibraries;
    }

    public void clearCodeTemplateLibraries() {
        codeTemplateLibraries = null;
    }

    public Set<String> getDependentIds() {
        if (dependentIds == null) {
            dependentIds = new HashSet<String>();
        }
        return dependentIds;
    }

    public void setDependentIds(Set<String> dependentIds) {
        this.dependentIds = dependentIds;
    }

    public Set<String> getDependencyIds() {
        if (dependencyIds == null) {
            dependencyIds = new HashSet<String>();
        }
        return dependencyIds;
    }

    public void setDependencyIds(Set<String> dependencyIds) {
        this.dependencyIds = dependencyIds;
    }

    public void clearDependencies() {
        dependentIds = null;
        dependencyIds = null;
    }

    public void clearAllExceptMetadata() {
        clearCodeTemplateLibraries();
        clearDependencies();
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();

        if (metadata != null) {
            purgedProperties.put("metadata", metadata.getPurgedProperties());
        }

        if (codeTemplateLibraries != null) {
            List<Map<String, Object>> purgedCodeTemplateLibraries = new ArrayList<Map<String, Object>>();
            for (CodeTemplateLibrary codeTemplateLibrary : codeTemplateLibraries) {
                purgedCodeTemplateLibraries.add(codeTemplateLibrary.getPurgedProperties());
            }
            purgedProperties.put("codeTemplateLibraries", purgedCodeTemplateLibraries);
        }

        if (dependentIds != null) {
            purgedProperties.put("dependentIdsCount", dependentIds.size());
        }

        if (dependencyIds != null) {
            purgedProperties.put("dependencyIdsCount", dependencyIds.size());
        }

        return purgedProperties;
    }
}