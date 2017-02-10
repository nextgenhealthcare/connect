/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.util.LinkedList;
import java.util.Map;

import com.mirth.connect.util.ScriptBuilderException;

public class IteratorRule extends Rule implements IteratorElement<Rule>, FilterTransformerIterable<Rule> {

    private IteratorRuleProperties properties;

    public IteratorRule() {
        properties = new IteratorRuleProperties();
    }

    public IteratorRule(IteratorRule props) {
        super(props);
        properties = props.getProperties().clone();
    }

    @Override
    public IteratorRuleProperties getProperties() {
        return properties;
    }

    public void setProperties(IteratorRuleProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getScript(boolean loadFiles) throws ScriptBuilderException {
        return properties.getScript(loadFiles);
    }

    @Override
    public String getPreScript(boolean loadFiles, LinkedList<IteratorProperties<Rule>> ancestors) throws ScriptBuilderException {
        return properties.getPreScript(loadFiles, ancestors);
    }

    @Override
    public String getIterationScript(boolean loadFiles, LinkedList<IteratorProperties<Rule>> ancestors) throws ScriptBuilderException {
        return properties.getIterationScript(loadFiles, ancestors);
    }

    @Override
    public String getPostScript(boolean loadFiles, LinkedList<IteratorProperties<Rule>> ancestors) throws ScriptBuilderException {
        return properties.getPostScript(loadFiles, ancestors);
    }

    @Override
    public String getType() {
        return IteratorProperties.PLUGIN_POINT;
    }

    @Override
    public IteratorRule clone() {
        return new IteratorRule(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("properties", properties.getPurgedProperties());
        return purgedProperties;
    }
}