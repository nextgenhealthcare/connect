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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.util.ScriptBuilderException;

public abstract class IteratorProperties<C extends FilterTransformerElement> implements Serializable, Purgable, FilterTransformerIterable<C> {

    private String target;
    private String indexVariable;
    private List<String> prefixSubstitutions;
    private List<C> children;

    public IteratorProperties() {
        target = "";
        indexVariable = "i";
        prefixSubstitutions = new ArrayList<String>();
        children = new ArrayList<C>();
    }

    @SuppressWarnings("unchecked")
    public IteratorProperties(IteratorProperties<C> props) {
        target = props.getTarget();
        indexVariable = props.getIndexVariable();
        prefixSubstitutions = new ArrayList<String>(props.getPrefixSubstitutions());
        children = new ArrayList<C>();
        for (C element : props.getChildren()) {
            children.add((C) element.clone());
        }
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getIndexVariable() {
        return indexVariable;
    }

    public void setIndexVariable(String indexVariable) {
        this.indexVariable = indexVariable;
    }

    public List<String> getPrefixSubstitutions() {
        return prefixSubstitutions;
    }

    public void setPrefixSubstitutions(List<String> prefixSubstitutions) {
        this.prefixSubstitutions = prefixSubstitutions;
    }

    public List<C> getChildren() {
        return children;
    }

    public void setChildren(List<C> children) {
        this.children = children;
    }

    public String getScript(boolean loadFiles) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        LinkedList<IteratorProperties<C>> ancestors = new LinkedList<IteratorProperties<C>>();
        script.append(StringUtils.defaultString(getPreScript(loadFiles, ancestors)));
        script.append(StringUtils.defaultString(getIterationScript(loadFiles, ancestors)));
        script.append(StringUtils.defaultString(getPostScript(loadFiles, ancestors)));
        return script.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getPreScript(boolean loadFiles, LinkedList<IteratorProperties<C>> ancestors) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        ancestors.push(this);

        for (C child : children) {
            if (child instanceof FilterTransformerIterable) {
                script.append(StringUtils.defaultString(((FilterTransformerIterable<C>) child).getPreScript(loadFiles, ancestors))).append('\n');
            }
        }

        ancestors.pop();
        return script.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getIterationScript(boolean loadFiles, LinkedList<IteratorProperties<C>> ancestors) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        ancestors.push(this);

        script.append("for (var ").append(indexVariable).append(" = 0; ").append(indexVariable).append(" < getArrayOrXmlLength(").append(target).append("); ").append(indexVariable).append("++) {\n");

        for (C child : children) {
            script.append('\n');
            if (child instanceof FilterTransformerIterable) {
                script.append(StringUtils.defaultString(((FilterTransformerIterable<C>) child).getIterationScript(loadFiles, ancestors)));
            } else {
                script.append(StringUtils.defaultString(child.getScript(loadFiles)));
            }
            script.append('\n');
        }

        script.append("\n}\n");

        ancestors.pop();
        return script.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getPostScript(boolean loadFiles, LinkedList<IteratorProperties<C>> ancestors) throws ScriptBuilderException {
        StringBuilder script = new StringBuilder();
        ancestors.push(this);

        for (C child : children) {
            if (child instanceof FilterTransformerIterable) {
                script.append(StringUtils.defaultString(((FilterTransformerIterable<C>) child).getPostScript(loadFiles, ancestors))).append('\n');
            }
        }

        ancestors.pop();
        return script.toString();
    }

    @Override
    public abstract IteratorProperties<C> clone();

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
        purgedProperties.put("prefixSubstitutionsCount", CollectionUtils.size(prefixSubstitutions));
        purgedProperties.put("children", PurgeUtil.purgeList(children));
        return purgedProperties;
    }
}