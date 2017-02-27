/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.editors.FilterTransformerTreeTableNode;
import com.mirth.connect.client.ui.editors.IteratorUtil;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class MapperStepPlugin extends TransformerStepPlugin {

    private MapperPanel panel;

    public MapperStepPlugin(String name) {
        super(name);
        panel = new MapperPanel();
    }

    @Override
    public MapperPanel getPanel() {
        return panel;
    }

    @Override
    public Step newObject(String variable, String mapping) {
        MapperStep props = new MapperStep();
        props.setVariable(variable);
        props.setMapping(mapping);
        return props;
    }

    @Override
    public boolean isNameEditable() {
        return false;
    }

    @Override
    public Pair<String, String> getIteratorInfo(String variable, String mapping) {
        return new ImmutablePair<String, String>(mapping, null);
    }

    @Override
    public Pair<String, String> getIteratorInfo(Step element) {
        MapperStep props = (MapperStep) element;
        return new ImmutablePair<String, String>(props.getMapping(), null);
    }

    @Override
    public void setIteratorInfo(Step element, String target, String outbound) {
        MapperStep props = (MapperStep) element;
        props.setMapping(target);
    }

    @Override
    public void replaceOrRemoveIteratorVariables(Step element, FilterTransformerTreeTableNode<Transformer, Step> parent, boolean replace) {
        MapperStep props = (MapperStep) element;
        props.setMapping(IteratorUtil.replaceOrRemoveIteratorVariables(props.getMapping(), parent, replace));
        props.setDefaultValue(IteratorUtil.replaceOrRemoveIteratorVariables(props.getDefaultValue(), parent, replace));
        if (CollectionUtils.isNotEmpty(props.getReplacements())) {
            for (int i = 0; i < props.getReplacements().size(); i++) {
                props.getReplacements().set(i, new ImmutablePair<String, String>(IteratorUtil.replaceOrRemoveIteratorVariables(props.getReplacements().get(i).getLeft(), parent, replace), IteratorUtil.replaceOrRemoveIteratorVariables(props.getReplacements().get(i).getRight(), parent, replace)));
            }
        }
    }

    @Override
    public String getPluginPointName() {
        return MapperStep.PLUGIN_POINT;
    }
}