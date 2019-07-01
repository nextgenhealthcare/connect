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

public class IteratorStep extends Step implements IteratorElement<Step>, FilterTransformerIterable<Step> {

    private IteratorStepProperties properties;

    public IteratorStep() {
        properties = new IteratorStepProperties();
    }

    public IteratorStep(IteratorStep props) {
        super(props);
        properties = props.getProperties().clone();
    }

    @Override
    public IteratorStepProperties getProperties() {
        return properties;
    }

    public void setProperties(IteratorStepProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getScript(boolean loadFiles) throws ScriptBuilderException {
        return properties.getScript(loadFiles);
    }

    @Override
    public String getPreScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        return properties.getPreScript(loadFiles, ancestors);
    }

    @Override
    public String getIterationScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        return properties.getIterationScript(loadFiles, ancestors);
    }

    @Override
    public String getPostScript(boolean loadFiles, LinkedList<IteratorProperties<Step>> ancestors) throws ScriptBuilderException {
        return properties.getPostScript(loadFiles, ancestors);
    }

    @Override
    public String getType() {
        return IteratorProperties.PLUGIN_POINT;
    }

    @Override
    public IteratorStep clone() {
        return new IteratorStep(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("properties", properties.getPurgedProperties());
        return purgedProperties;
    }
}