/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagebuilder;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Step;
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
    public String getPluginPointName() {
        return MessageBuilderStep.PLUGIN_POINT;
    }
}