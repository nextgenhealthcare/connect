/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.xsltstep;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Step;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class XsltStepPlugin extends TransformerStepPlugin {

    private XsltStepPanel panel;

    public XsltStepPlugin(String name) {
        super(name);
        panel = new XsltStepPanel();
    }

    @Override
    public EditorPanel<Step> getPanel() {
        return panel;
    }

    @Override
    public boolean includesScrollPane() {
        return true;
    }

    @Override
    public Step newObject(String variable, String mapping) {
        XsltStep props = new XsltStep();
        props.setSourceXml(mapping);
        props.setResultVariable(variable);
        return props;
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    @Override
    public Pair<String, String> getIteratorInfo(String variable, String mapping) {
        return new ImmutablePair<String, String>(mapping, null);
    }

    @Override
    public String getPluginPointName() {
        return XsltStep.PLUGIN_POINT;
    }
}