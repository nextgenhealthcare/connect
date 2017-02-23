/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.scriptfilerule;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Rule;
import com.mirth.connect.plugins.FilterRulePlugin;

public class ExternalScriptRulePlugin extends FilterRulePlugin {

    private ExternalScriptPanel panel;

    public ExternalScriptRulePlugin(String name) {
        super(name);
        panel = new ExternalScriptPanel();
    }

    @Override
    public EditorPanel<Rule> getPanel() {
        return panel;
    }

    @Override
    public Rule newObject(String variable, String mapping) {
        ExternalScriptRule props = new ExternalScriptRule();
        props.setScriptPath(mapping);
        return props;
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    @Override
    public Pair<String, String> getIteratorInfo(String variable, String mapping) {
        return new ImmutablePair<String, String>(null, null);
    }

    @Override
    public String getPluginPointName() {
        return ExternalScriptRule.PLUGIN_POINT;
    }
}