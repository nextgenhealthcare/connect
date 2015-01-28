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
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;

public abstract class Reference {

    public enum Type {
        CLASS, FUNCTION, VARIABLE, CODE
    }

    private String id;
    private Type type;
    private int scope;
    private String category;
    private String name;
    private String description;
    private String replacementCode;
    private String summary;
    private String iconName;
    private boolean deprecated;

    public Reference(Type type, int scope, String category) {
        this(type, scope, category, null);
    }

    public Reference(Type type, int scope, String category, String name) {
        this(type, scope, category, name, null, null);
    }

    public Reference(Type type, int scope, String category, String name, String description, String replacementCode) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.scope = scope;
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

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
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
        return new CodeTemplate(name, description, replacementCode, getCodeSnippetType(type), scope);
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

    private CodeSnippetType getCodeSnippetType(Type type) {
        switch (type) {
            case FUNCTION:
                return CodeSnippetType.FUNCTION;
            case CODE:
                return CodeSnippetType.CODE;
            case VARIABLE:
                return CodeSnippetType.VARIABLE;
            default:
                return null;
        }
    }
}