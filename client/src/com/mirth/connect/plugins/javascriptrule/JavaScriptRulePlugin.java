/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.javascriptrule;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.model.Rule;
import com.mirth.connect.plugins.FilterRulePlugin;

public class JavaScriptRulePlugin extends FilterRulePlugin {

    private JavaScriptPanel panel;

    public JavaScriptRulePlugin(String name) {
        super(name);
        panel = new JavaScriptPanel();
    }

    @Override
    public JavaScriptPanel getPanel() {
        return panel;
    }

    @Override
    public boolean includesScrollPane() {
        return true;
    }

    @Override
    public Rule newObject(String variable, String mapping) {
        return new JavaScriptRule();
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
        return JavaScriptRule.PLUGIN_POINT;
    }
}