/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagebuilder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.client.ui.editors.FilterTransformerTreeTableNode;
import com.mirth.connect.client.ui.editors.IteratorUtil;
import com.mirth.connect.model.Step;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class MessageBuilderPlugin extends TransformerStepPlugin {

    private MessageBuilderPanel panel;

    public MessageBuilderPlugin(String name) {
        super(name);
        panel = new MessageBuilderPanel();
    }

    @Override
    public EditorPanel<Step> getPanel() {
        return panel;
    }

    @Override
    public Step newObject(String variable, String mapping) {
        MessageBuilderStep props = new MessageBuilderStep();
        props.setMessageSegment(variable);
        props.setMapping(mapping);
        return props;
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    @Override
    public Pair<String, String> getIteratorInfo(String variable, String mapping) {
        if (StringUtils.isBlank(mapping)) {
            return new ImmutablePair<String, String>(variable, null);
        } else {
            return new ImmutablePair<String, String>(mapping, variable);
        }
    }

    @Override
    public Pair<String, String> getIteratorInfo(Step element) {
        MessageBuilderStep props = (MessageBuilderStep) element;
        if (StringUtils.isBlank(props.getMapping())) {
            return new ImmutablePair<String, String>(props.getMessageSegment(), null);
        } else {
            return new ImmutablePair<String, String>(props.getMapping(), props.getMessageSegment());
        }
    }

    @Override
    public void setIteratorInfo(Step element, String target, String outbound) {
        MessageBuilderStep props = (MessageBuilderStep) element;
        if (StringUtils.isBlank(outbound)) {
            props.setMessageSegment(target);
        } else {
            props.setMessageSegment(outbound);
            props.setMapping(target);
        }
    }

    @Override
    public void replaceOrRemoveIteratorVariables(Step element, FilterTransformerTreeTableNode<Transformer, Step> parent, boolean replace) {
        MessageBuilderStep props = (MessageBuilderStep) element;
        props.setMessageSegment(IteratorUtil.replaceOrRemoveIteratorVariables(props.getMessageSegment(), parent, replace));
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
        return MessageBuilderStep.PLUGIN_POINT;
    }
}