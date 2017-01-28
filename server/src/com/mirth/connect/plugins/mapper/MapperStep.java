/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.model.FilterTransformerIterable;
import com.mirth.connect.model.IteratorProperties;
import com.mirth.connect.model.Step;
import com.mirth.connect.util.JavaScriptSharedUtil;
import com.mirth.connect.util.ScriptBuilderException;

public class MapperStep extends Step implements FilterTransformerIterable<Step> {

    public static final String PLUGIN_POINT = "Mapper";

    private String variable;
    private String mapping;
    private String defaultValue;
    private List<Pair<String, String>> replacements;
    private Scope scope;

    public MapperStep() {
        variable = "";
        mapping = "";
        defaultValue = "";
        replacements = new ArrayList<Pair<String, String>>();
        scope = Scope.CHANNEL;
    }

    public MapperStep(MapperStep props) {
        super(props);
        variable = props.getVariable();
        mapping = props.getMapping();
        defaultValue = props.getDefaultValue();

        if (props.getReplacements() != null) {
            List<Pair<String, String>> replacements = new ArrayList<Pair<String, String>>();
            for (Pair<String, String> pair : props.getReplacements()) {
                replacements.add(new ImmutablePair<String, String>(pair.getLeft(), pair.getRight()));
            }
            this.replacements = replacements;
        }

        scope = props.getScope();
    }

    @Override
    public Collection<String> getResponseVariables() {
        Collection<String> varCollection = null;
        if (scope.equals(Scope.RESPONSE)) {
            Collections.singletonList(variable);
        }
        return varCollection;
    }

    @Override
    public String getScript(boolean loadFiles) {
        String regexArray = buildRegexArray();

        StringBuilder script = new StringBuilder();

        script.append("var mapping;\n\n");
        script.append("try {\n\tmapping = " + StringUtils.defaultIfBlank(mapping, "''") + "; \n} ");
        script.append("catch (e) {\n\tlogger.error(e);\n\tmapping = '';\n}\n\n");

        if (scope != null) {
            script.append(scope.map + ".put(");
        } else {
            script.append(Scope.CHANNEL.map + ".put(");
        }

        // default values need to be provided
        // so we don't cause syntax errors in the JS
        script.append("'" + variable + "', ");
        String tempDefault = defaultValue;
        if (tempDefault.length() == 0) {
            tempDefault = "''";
        }

        script.append("validate( mapping , " + tempDefault + ", " + regexArray + "));");
        return script.toString();
    }

    @Override
    public String getPreScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        script.append("var _").append(JavaScriptSharedUtil.convertIdentifier(variable)).append(" = Lists.list();");
        return script.toString();
    }

    @Override
    public String getIterationScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        String regexArray = buildRegexArray();

        StringBuilder script = new StringBuilder();

        script.append("var mapping;\n\n");
        script.append("try {\n\tmapping = ").append(StringUtils.defaultIfBlank(mapping, "''")).append("; \n} ");
        script.append("catch (e) {\n\tlogger.error(e);\n\tmapping = '';\n}\n\n");

        String tempDefault = defaultValue;
        if (tempDefault.length() == 0) {
            tempDefault = "''";
        }

        script.append('_').append(JavaScriptSharedUtil.convertIdentifier(variable)).append(".add(validate( mapping , ").append(tempDefault).append(", ").append(regexArray).append("));");
        return script.toString();
    }

    @Override
    public String getPostScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        script.append(scope != null ? scope.map : Scope.CHANNEL.map).append(".put('").append(variable).append("', _").append(JavaScriptSharedUtil.convertIdentifier(variable)).append(".toArray());");
        return script.toString();
    }

    private String buildRegexArray() {
        StringBuilder regexArray = new StringBuilder();
        regexArray.append("new Array(");
        if (replacements != null && replacements.size() > 0) {
            for (int i = 0; i < replacements.size(); i++) {
                regexArray.append("new Array(" + replacements.get(i).getLeft() + ", " + replacements.get(i).getRight() + ")");
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

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
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

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Scope getScope() {
        return this.scope;
    }

    @Override
    public String getType() {
        return PLUGIN_POINT;
    }

    @Override
    public Step clone() {
        return new MapperStep(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        if (replacements != null) {
            purgedProperties.put("replacementsCount", replacements.size());
        }
        purgedProperties.put("scope", scope);
        return purgedProperties;
    }

    public static enum Scope {
        // @formatter:off
        CONNECTOR("Connector Map", "connectorMap"),
        CHANNEL("Channel Map", "channelMap"),
        GLOBAL_CHANNEL("Global Channel Map", "globalChannelMap"), 
        GLOBAL("Global Map","globalMap"),
        RESPONSE("Response Map", "responseMap");
        // @formatter:on

        public String label;
        public String map;

        private Scope(String label, String map) {
            this.label = label;
            this.map = map;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
