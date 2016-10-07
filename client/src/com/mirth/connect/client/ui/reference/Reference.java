/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.mirth.connect.client.ui.components.rsta.ac.MirthCompletion;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.model.codetemplates.CodeTemplateProperties.CodeTemplateType;

public abstract class Reference {

    public enum Type {
        CLASS, FUNCTION, VARIABLE, CODE
    }

    private String id;
    private Type type;
    private CodeTemplateContextSet contextSet;
    private String category;
    private String name;
    private String description;
    private String replacementCode;
    private String summary;
    private String iconName;
    private boolean deprecated;

    public Reference(Type type, CodeTemplateContextSet contextSet, String category) {
        this(type, contextSet, category, null);
    }

    public Reference(Type type, CodeTemplateContextSet contextSet, String category, String name) {
        this(type, contextSet, category, name, null, null);
    }

    public Reference(Type type, CodeTemplateContextSet contextSet, String category, String name, String description, String replacementCode) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.contextSet = contextSet;
        this.category = category;
        this.name = name;
        this.description = description;
        this.replacementCode = replacementCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CodeTemplateContextSet getContextSet() {
        return contextSet;
    }

    public void setContextSet(CodeTemplateContextSet contextSet) {
        this.contextSet = contextSet;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getReplacementCode() {
        return replacementCode;
    }

    public void setReplacementCode(String replacementCode) {
        this.replacementCode = replacementCode;
    }

    public String getSummary() {
        if (StringUtils.isNotBlank(summary)) {
            return summary;
        } else {
            return getDescription();
        }
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public CodeTemplate toCodeTemplate() {
        return new CodeTemplate(name, getCodeTemplateType(type), contextSet, replacementCode, description);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MirthCompletion) {
            return ((MirthCompletion) obj).getId().equals(id);
        } else if (obj instanceof Reference) {
            return ((Reference) obj).getId().equals(id);
        }
        return false;
    }

    private CodeTemplateType getCodeTemplateType(Type type) {
        return type == Type.FUNCTION ? CodeTemplateType.FUNCTION : CodeTemplateType.DRAG_AND_DROP_CODE;
    }
}