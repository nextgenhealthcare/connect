/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.scriptfilerule;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.ExternalScriptPanel;
import com.mirth.connect.client.ui.editors.filter.FilterPane;
import com.mirth.connect.plugins.FilterRulePlugin;

public class ExternalScriptRulePlugin extends FilterRulePlugin {

    private ExternalScriptPanel panel;

    public ExternalScriptRulePlugin(String name) {
        super(name);
    }

    @Override
    public void initialize(FilterPane pane) {
        panel = new ExternalScriptPanel(pane, false);
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
        return panel.getData();
    }

    @Override
    public void setData(Map<Object, Object> data) {
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
    public String getGeneratedScript(Map<Object, Object> data) {
        return "";
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

    public String doValidate(Map<Object, Object> data) {
        String var = data.get("Variable").toString();
        // check for empty variable names
        if (var == null || var.trim().equals("")) {
            return "The script path field cannot be blank.\nPlease enter a new script path.\n";
        }
        return null;
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
