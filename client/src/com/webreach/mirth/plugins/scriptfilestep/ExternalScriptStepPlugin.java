package com.webreach.mirth.plugins.scriptfilestep;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.ExternalScriptPanel;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.plugins.TransformerStepPlugin;

import java.util.HashMap;
import java.util.Map;

public class ExternalScriptStepPlugin extends TransformerStepPlugin {

    private ExternalScriptPanel panel;

    public ExternalScriptStepPlugin(String name) {
        super(name);
    }

    public ExternalScriptStepPlugin(String name, TransformerPane parent) {
        super(name, parent);
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
            ((TransformerPane) parent).setInvalidVar(true);
            String msg = "The script path field cannot be blank.\nPlease enter a new script path.\n";
            ((TransformerPane) parent).setRowSelectionInterval(row, row);
            ((TransformerPane) parent).getParentFrame().alertWarning(parent.parent, msg);
        } else {
            ((TransformerPane) parent).setInvalidVar(false);
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

    public String getDisplayName() {
        return "External Script";
    }
}
