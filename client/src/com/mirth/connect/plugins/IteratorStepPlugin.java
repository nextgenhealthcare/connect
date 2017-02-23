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
import com.mirth.connect.client.ui.editors.transformer.IteratorStepPanel;
import com.mirth.connect.model.IteratorProperties;
import com.mirth.connect.model.IteratorStep;
import com.mirth.connect.model.Step;

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
    public Pair<String, String> getIteratorInfo(String variable, String mapping) {
        return new ImmutablePair<String, String>(null, null);
    }

    @Override
    public String getPluginPointName() {
        return IteratorProperties.PLUGIN_POINT;
    }
}