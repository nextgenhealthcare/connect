/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.rulebuilder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.client.ui.editors.FilterTransformerTreeTableNode;
import com.mirth.connect.client.ui.editors.IteratorUtil;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Rule;
import com.mirth.connect.plugins.FilterRulePlugin;

public class RuleBuilderPlugin extends FilterRulePlugin {

    private RuleBuilderPanel panel;

    public RuleBuilderPlugin(String name) {
        super(name);
        panel = new RuleBuilderPanel();
    }

    @Override
    public EditorPanel<Rule> getPanel() {
        return panel;
    }

    @Override
    public Rule newObject(String variable, String mapping) {
        RuleBuilderRule props = new RuleBuilderRule();
        props.setField(mapping);
        return props;
    }

    @Override
    public boolean isNameEditable() {
        return false;
    }

    public boolean showValidateTask() {
        return true;
    }

    @Override
    public Pair<String, String> getIteratorInfo(String variable, String mapping) {
        return new ImmutablePair<String, String>(mapping, null);
    }

    @Override
    public Pair<String, String> getIteratorInfo(Rule element) {
        RuleBuilderRule props = (RuleBuilderRule) element;
        return new ImmutablePair<String, String>(props.getField(), null);
    }

    @Override
    public void setIteratorInfo(Rule element, String target, String outbound) {
        RuleBuilderRule props = (RuleBuilderRule) element;
        props.setField(target);
    }

    @Override
    public void replaceOrRemoveIteratorVariables(Rule element, FilterTransformerTreeTableNode<Filter, Rule> parent, boolean replace) {
        RuleBuilderRule props = (RuleBuilderRule) element;
        props.setField(IteratorUtil.replaceOrRemoveIteratorVariables(props.getField(), parent, replace));
        if (CollectionUtils.isNotEmpty(props.getValues())) {
            for (int i = 0; i < props.getValues().size(); i++) {
                props.getValues().set(i, IteratorUtil.replaceOrRemoveIteratorVariables(props.getValues().get(i), parent, replace));
            }
        }
    }

    @Override
    public String getPluginPointName() {
        return RuleBuilderRule.PLUGIN_POINT;
    }
}