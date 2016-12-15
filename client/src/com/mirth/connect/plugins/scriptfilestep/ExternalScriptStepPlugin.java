/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.scriptfilestep;

import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Step;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class ExternalScriptStepPlugin extends TransformerStepPlugin {

    private ExternalScriptPanel panel;

    public ExternalScriptStepPlugin(String name) {
        super(name);
        panel = new ExternalScriptPanel();
    }

    @Override
    public EditorPanel<Step> getPanel() {
        return panel;
    }

    @Override
    public Step newStep(String variable, String mapping) {
        ExternalScriptStep props = new ExternalScriptStep();
        props.setScriptPath(mapping);
        return props;
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    @Override
    public String getPluginPointName() {
        return ExternalScriptStep.PLUGIN_POINT;
    }
}