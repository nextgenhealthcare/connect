/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.scriptfilestep;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.ExternalScriptPanel;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class ExternalScriptStepPlugin extends TransformerStepPlugin {

    private ExternalScriptPanel panel;
    private TransformerPane parent;

    public ExternalScriptStepPlugin(String name) {
        super(name);
    }

    @Override
    public void initialize(TransformerPane pane) {
        this.parent = pane;
        panel = new ExternalScriptPanel(parent, true);
    }

    @Override
    public BasePanel getPanel() {
        return panel;
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    @Override
    public Map<Object, Object> getData(int row) {
        Map<Object, Object> data = panel.getData();
        String var = data.get("Variable").toString();

        // check for empty variable names
        if (var == null || var.trim().equals("")) {
            parent.setInvalidVar(true);
            String msg = "The script path field cannot be blank.\nPlease enter a new script path.\n";
            parent.setRowSelectionInterval(row, row);
            parent.getParentFrame().alertWarning(parent.parent, msg);
        } else {
            parent.setInvalidVar(false);
        }

        return data;
    }

    @Override
    public void setData(Map<Object, Object> data) {
        panel.setData(data);
    }

    @Override
    public void clearData() {
        panel.setData(null);
    }

    @Override
    public void initData() {
        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("Variable", "");
        panel.setData(data);
    }

    @Override
    public String getScript(Map<Object, Object> data) {
        StringBuilder script = new StringBuilder();
        String variable = (String) data.get("Variable");
        script.append(variable);
        return script.toString();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }

    @Override
    public String getPluginPointName() {
        return "External Script";
    }
}
