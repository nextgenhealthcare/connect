/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.client.ui.editors.filter.IteratorRulePanel;
import com.mirth.connect.model.IteratorProperties;
import com.mirth.connect.model.IteratorRule;
import com.mirth.connect.model.Rule;

public class IteratorRulePlugin extends FilterRulePlugin {

    private IteratorRulePanel panel;

    public IteratorRulePlugin(String name) {
        super(name);
        panel = new IteratorRulePanel();
    }

    @Override
    public EditorPanel<Rule> getPanel() {
        return panel;
    }

    @Override
    public Rule newObject(String variable, String mapping) {
        IteratorRule rule = (IteratorRule) getDefaults();
        rule.getProperties().setTarget(mapping);
        return rule;
    }

    @Override
    public boolean isNameEditable() {
        return false;
    }

    @Override
    public String getPluginPointName() {
        return IteratorProperties.PLUGIN_POINT;
    }
}