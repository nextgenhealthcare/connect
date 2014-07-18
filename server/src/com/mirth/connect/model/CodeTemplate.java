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

import org.apache.commons.lang3.ObjectUtils;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("codeTemplate")
public class CodeTemplate implements Serializable, Migratable, Purgable {
    public enum CodeSnippetType {
        CODE("Code"), VARIABLE("Variable"), FUNCTION("Function");

        private String value;

        CodeSnippetType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ContextType {
        GLOBAL_CONTEXT("Global", 0), GLOBAL_CHANNEL_CONTEXT("Global Channel", 1), CHANNEL_CONTEXT("Channel", 2), MESSAGE_CONTEXT("Message", 3);

        private String value;
        private int context;

        ContextType(String value, int context) {
            this.value = value;
            this.context = context;
        }

        public String getValue() {
            return value;
        }

        public int getContext() {
            return context;
        }
    }

    private String id;
    private String name;
    private String tooltip;
    private String code;
    private CodeSnippetType type;
    private int scope;

    public CodeTemplate() {

    }

    public CodeTemplate(String name, String tooltip, String code, CodeSnippetType type, int scope) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.tooltip = tooltip;
        this.code = code;
        this.type = type;
        this.scope = scope;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public CodeSnippetType getType() {
        return type;
    }

    public void setType(CodeSnippetType type) {
        this.type = type;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof CodeTemplate)) {
            return false;
        }

        CodeTemplate codeTemplate = (CodeTemplate) that;

        return ObjectUtils.equals(this.getId(), codeTemplate.getId()) && ObjectUtils.equals(this.getName(), codeTemplate.getName()) && ObjectUtils.equals(this.getTooltip(), codeTemplate.getTooltip()) && ObjectUtils.equals(this.getScope(), codeTemplate.getScope()) && ObjectUtils.equals(this.getType(), codeTemplate.getType()) && ObjectUtils.equals(this.getCode(), codeTemplate.getCode());
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName() + "[");
        builder.append("id=" + getId() + ", ");
        builder.append("tooltip=" + getTooltip() + ", ");
        builder.append("scope=" + getScope() + ", ");
        builder.append("type=" + getType() + ", ");
        builder.append("code=" + getCode() + ", ");
        builder.append("name=" + getName());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("id", id);
        purgedProperties.put("nameChars", PurgeUtil.countChars(name));
        purgedProperties.put("tooltipChars", PurgeUtil.countChars(tooltip));
        purgedProperties.put("codeLines", PurgeUtil.countLines(code));
        purgedProperties.put("type", type);
        purgedProperties.put("scope", scope);
        return purgedProperties;
    }
}
