/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.javascriptstep;

import com.mirth.connect.model.Step;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class JavaScriptStepPlugin extends TransformerStepPlugin {

    private JavaScriptPanel panel;

    public JavaScriptStepPlugin(String name) {
        super(name);
        panel = new JavaScriptPanel();
    }

    @Override
    public JavaScriptPanel getPanel() {
        return panel;
    }

    @Override
    public Step newObject(String variable, String mapping) {
        return new JavaScriptStep();
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    @Override
    public String getPluginPointName() {
        return JavaScriptStep.PLUGIN_POINT;
    }
}