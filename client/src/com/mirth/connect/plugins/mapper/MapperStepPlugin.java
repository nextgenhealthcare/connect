/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mapper;

import com.mirth.connect.model.Step;
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
    public String getPluginPointName() {
        return MapperStep.PLUGIN_POINT;
    }
}