/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.destinationsetfilter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.client.ui.editors.FilterTransformerTreeTableNode;
import com.mirth.connect.client.ui.editors.IteratorUtil;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class DestinationSetFilterPlugin extends TransformerStepPlugin {

    private DestinationSetFilterPanel panel;

    public DestinationSetFilterPlugin(String name) {
        super(name);
        panel = new DestinationSetFilterPanel();
    }

    @Override
    public EditorPanel<Step> getPanel() {
        return panel;
    }

    @Override
    public Step newObject(String variable, String mapping) {
        DestinationSetFilterStep props = new DestinationSetFilterStep();
        props.setField(mapping);
        return props;
    }

    @Override
    public boolean isNameEditable() {
        return false;
    }

    @Override
    public boolean onlySourceConnector() {
        return true;
    }

    @Override
    public Pair<String, String> getIteratorInfo(String variable, String mapping) {
        return new ImmutablePair<String, String>(mapping, null);
    }

    @Override
    public Pair<String, String> getIteratorInfo(Step element) {
        DestinationSetFilterStep props = (DestinationSetFilterStep) element;
        return new ImmutablePair<String, String>(props.getField(), null);
    }

    @Override
    public void setIteratorInfo(Step element, String target, String outbound) {
        DestinationSetFilterStep props = (DestinationSetFilterStep) element;
        props.setField(target);
    }

    @Override
    public void replaceOrRemoveIteratorVariables(Step element, FilterTransformerTreeTableNode<Transformer, Step> parent, boolean replace) {
        DestinationSetFilterStep props = (DestinationSetFilterStep) element;
        props.setField(IteratorUtil.replaceOrRemoveIteratorVariables(props.getField(), parent, replace));
        if (CollectionUtils.isNotEmpty(props.getValues())) {
            for (int i = 0; i < props.getValues().size(); i++) {
                props.getValues().set(i, IteratorUtil.replaceOrRemoveIteratorVariables(props.getValues().get(i), parent, replace));
            }
        }
    }

    @Override
    public String getPluginPointName() {
        return DestinationSetFilterStep.PLUGIN_POINT;
    }
}