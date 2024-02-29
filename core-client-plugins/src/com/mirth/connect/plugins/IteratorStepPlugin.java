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
import com.mirth.connect.client.ui.editors.transformer.IteratorStepPanel;
import com.mirth.connect.model.IteratorProperties;
import com.mirth.connect.model.IteratorStep;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;

public class IteratorStepPlugin extends TransformerStepPlugin {

    private IteratorStepPanel panel;

    public IteratorStepPlugin(String name) {
        super(name);
        panel = new IteratorStepPanel();
    }

    @Override
    public EditorPanel<Step> getPanel() {
        return panel;
    }

    @Override
    public Step newObject(String variable, String mapping) {
        IteratorStep step = (IteratorStep) getDefaults();
        step.getProperties().setTarget(mapping);
        return step;
    }

    @Override
    public boolean isNameEditable() {
        return false;
    }

    @Override
    public Pair<String, String> getIteratorInfo(Step element) {
        IteratorStep props = (IteratorStep) element;
        return new ImmutablePair<String, String>(props.getProperties().getTarget(), null);
    }

    @Override
    public void setIteratorInfo(Step element, String target, String outbound) {
        IteratorStep props = (IteratorStep) element;
        props.getProperties().setTarget(target);
    }

    @Override
    public void replaceOrRemoveIteratorVariables(Step element, FilterTransformerTreeTableNode<Transformer, Step> parent, boolean replace) {
        IteratorStep props = (IteratorStep) element;
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