/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.client.ui.editors.FilterTransformerTreeTableNode;
import com.mirth.connect.client.ui.editors.IteratorUtil;
import com.mirth.connect.client.ui.editors.filter.IteratorRulePanel;
import com.mirth.connect.model.Filter;
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
    public Pair<String, String> getIteratorInfo(Rule element) {
        IteratorRule props = (IteratorRule) element;
        return new ImmutablePair<String, String>(props.getProperties().getTarget(), null);
    }

    @Override
    public void setIteratorInfo(Rule element, String target, String outbound) {
        IteratorRule props = (IteratorRule) element;
        props.getProperties().setTarget(target);
    }

    @Override
    public void replaceOrRemoveIteratorVariables(Rule element, FilterTransformerTreeTableNode<Filter, Rule> parent, boolean replace) {
        IteratorRule props = (IteratorRule) element;
        props.getProperties().setTarget(IteratorUtil.replaceOrRemoveIteratorVariables(props.getProperties().getTarget(), parent, replace));
        for (int i = 0; i < props.getProperties().getPrefixSubstitutions().size(); i++) {
            props.getProperties().getPrefixSubstitutions().set(i, IteratorUtil.replaceOrRemoveIteratorVariables(props.getProperties().getPrefixSubstitutions().get(i), parent, replace));
        }
    }

    @Override
    public String getPluginPointName() {
        return IteratorProperties.PLUGIN_POINT;
    }
}