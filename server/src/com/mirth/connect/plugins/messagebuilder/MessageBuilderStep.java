/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagebuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.model.Step;

public class MessageBuilderStep extends Step {

    public static final String PLUGIN_POINT = "Message Builder";

    private String messageSegment;
    private String mapping;
    private String defaultValue;
    private List<Pair<String, String>> replacements;

    public MessageBuilderStep() {
        messageSegment = "";
        mapping = "";
        defaultValue = "";
        replacements = new ArrayList<Pair<String, String>>();
    }

    public MessageBuilderStep(MessageBuilderStep props) {
        messageSegment = props.getMessageSegment();
        mapping = props.getMapping();
        defaultValue = props.getDefaultValue();

        if (props.getReplacements() != null) {
            List<Pair<String, String>> replacements = new ArrayList<Pair<String, String>>();
            for (Pair<String, String> pair : props.getReplacements()) {
                replacements.add(new ImmutablePair<String, String>(pair.getLeft(), pair.getRight()));
            }
            this.replacements = replacements;
        }
    }

    @Override
    public String getScript(boolean loadFiles) {
        String regexArray = buildRegexArray();
        StringBuilder script = new StringBuilder();
        String tempDefaultValue = defaultValue;
        if (StringUtils.isEmpty(tempDefaultValue)) {
            tempDefaultValue = "''";
        }
        String tempMapping = mapping;
        if (StringUtils.isEmpty(tempMapping)) {
            tempMapping = "''";
        }
        script.append(messageSegment);
        script.append(" = ");
        script.append("validate(" + tempMapping + ", " + tempDefaultValue + ", " + regexArray + ");");
        return script.toString();
    }

    private String buildRegexArray() {
        StringBuilder regexArray = new StringBuilder();
        regexArray.append("new Array(");
        if (replacements != null && replacements.size() > 0) {
            for (int i = 0; i < replacements.size(); i++) {
                regexArray.append("new Array(" + replacements.get(i).getKey() + ", " + replacements.get(i).getValue() + ")");
                if (i + 1 == replacements.size()) {
                    regexArray.append(")");
                } else {
                    regexArray.append(",");
                }
            }
        } else {
            regexArray.append(")");
        }
        return regexArray.toString();
    }

    public String getMessageSegment() {
        return messageSegment;
    }

    public void setMessageSegment(String messageSegment) {
        this.messageSegment = messageSegment;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<Pair<String, String>> getReplacements() {
        return replacements;
    }

    public void setReplacements(List<Pair<String, String>> replacements) {
        this.replacements = replacements;
    }

    @Override
    public String getType() {
        return PLUGIN_POINT;
    }

    @Override
    public Step clone() {
        return new MessageBuilderStep(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        if (replacements != null) {
            purgedProperties.put("replacementsCount", replacements.size());
        }
        return purgedProperties;
    }
}