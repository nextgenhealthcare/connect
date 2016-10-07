/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.codetemplates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.Cacheable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("codeTemplateLibrary")
public class CodeTemplateLibrary implements Serializable, Migratable, Purgable, Cacheable<CodeTemplateLibrary> {

    private String id;
    private String name;
    private Integer revision;
    private Calendar lastModified;
    private String description;
    private boolean includeNewChannels;
    private Set<String> enabledChannelIds;
    private Set<String> disabledChannelIds;
    private List<CodeTemplate> codeTemplates;

    public CodeTemplateLibrary() {
        id = UUID.randomUUID().toString();
        enabledChannelIds = new HashSet<String>();
        disabledChannelIds = new HashSet<String>();
        codeTemplates = new ArrayList<CodeTemplate>();
    }

    public CodeTemplateLibrary(CodeTemplateLibrary library) {
        id = library.getId();
        name = library.getName();
        revision = library.getRevision();
        lastModified = library.getLastModified();
        description = library.getDescription();
        includeNewChannels = library.isIncludeNewChannels();
        enabledChannelIds = new HashSet<String>(library.getEnabledChannelIds());
        disabledChannelIds = new HashSet<String>(library.getDisabledChannelIds());
        codeTemplates = new ArrayList<CodeTemplate>();
        if (CollectionUtils.isNotEmpty(library.getCodeTemplates())) {
            for (CodeTemplate codeTemplate : library.getCodeTemplates()) {
                codeTemplates.add(new CodeTemplate(codeTemplate));
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIncludeNewChannels() {
        return includeNewChannels;
    }

    public void setIncludeNewChannels(boolean includeNewChannels) {
        this.includeNewChannels = includeNewChannels;
    }

    public Set<String> getEnabledChannelIds() {
        return enabledChannelIds;
    }

    public void setEnabledChannelIds(Set<String> enabledChannelIds) {
        this.enabledChannelIds = enabledChannelIds;
    }

    public Set<String> getDisabledChannelIds() {
        return disabledChannelIds;
    }

    public void setDisabledChannelIds(Set<String> disabledChannelIds) {
        this.disabledChannelIds = disabledChannelIds;
    }

    public List<CodeTemplate> getCodeTemplates() {
        return codeTemplates;
    }

    public void setCodeTemplates(List<CodeTemplate> codeTemplates) {
        this.codeTemplates = codeTemplates;
    }

    public void sortCodeTemplates() {
        if (CollectionUtils.isNotEmpty(codeTemplates)) {
            Collections.sort(codeTemplates, new Comparator<CodeTemplate>() {
                @Override
                public int compare(CodeTemplate o1, CodeTemplate o2) {
                    if (o1.getName() == null && o2.getName() != null) {
                        return -1;
                    } else if (o1.getName() != null && o2.getName() == null) {
                        return 1;
                    } else if (o1.getName() == null && o2.getName() == null) {
                        return 0;
                    } else {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                }
            });
        }
    }

    public void replaceCodeTemplatesWithIds() {
        if (CollectionUtils.isNotEmpty(codeTemplates)) {
            List<CodeTemplate> list = new ArrayList<CodeTemplate>();
            for (CodeTemplate codeTemplate : codeTemplates) {
                list.add(new CodeTemplate(codeTemplate.getId()));
            }
            codeTemplates = list;
        }
    }

    @Override
    public CodeTemplateLibrary cloneIfNeeded() {
        return new CodeTemplateLibrary(this);
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
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("id", id);
        purgedProperties.put("nameChars", PurgeUtil.countChars(name));
        purgedProperties.put("lastModified", lastModified);
        purgedProperties.put("descriptionChars", PurgeUtil.countChars(description));
        purgedProperties.put("includeNewChannels", includeNewChannels);
        purgedProperties.put("enabledChannelIdsCount", enabledChannelIds.size());
        purgedProperties.put("disabledChannelIdsCount", disabledChannelIds.size());

        List<Map<String, Object>> purgedCodeTemplates = new ArrayList<Map<String, Object>>();
        for (CodeTemplate codeTemplate : codeTemplates) {
            purgedCodeTemplates.add(codeTemplate.getPurgedProperties());
        }
        purgedProperties.put("codeTemplates", purgedCodeTemplates);

        return purgedProperties;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false, null, "UNASSIGNED_LIBRARY_ID", "UNASSIGNED_LIBRARY_DESCRIPTION");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName()).append('[');
        builder.append("id=").append(id).append(", ");
        builder.append("name=").append(name).append(", ");
        builder.append("revision=").append(revision).append(", ");
        builder.append("lastModified=").append(lastModified).append(", ");
        builder.append("description=").append(description).append(", ");
        builder.append("includeNewChannels=").append(includeNewChannels).append(", ");
        builder.append("enabledChannelIds=").append(enabledChannelIds).append(", ");
        builder.append("disabledChannelIds=").append(disabledChannelIds).append(", ");
        builder.append("codeTemplates=").append(codeTemplates).append(']');
        return builder.toString();
    }
}