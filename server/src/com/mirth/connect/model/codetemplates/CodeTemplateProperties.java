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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.model.CalendarToStringStyle;

public abstract class CodeTemplateProperties implements Serializable, Purgable {

    public enum CodeTemplateType {
        FUNCTION("Function"), DRAG_AND_DROP_CODE("Drag-and-Drop Code Block"), COMPILED_CODE(
                "Compiled Code Block");

        private String value;

        private CodeTemplateType(String value) {
            this.value = value;
        }

        public static CodeTemplateType fromString(String value) {
            for (CodeTemplateType type : values()) {
                if (StringUtils.equals(type.toString(), value)) {
                    return type;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private CodeTemplateType type;

    public CodeTemplateProperties(CodeTemplateType type) {
        this.type = type;
    }

    public abstract String getPluginPointName();

    public CodeTemplateType getType() {
        return type;
    }

    public void setType(CodeTemplateType type) {
        this.type = type;
    }

    @Override
    public abstract CodeTemplateProperties clone();

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}