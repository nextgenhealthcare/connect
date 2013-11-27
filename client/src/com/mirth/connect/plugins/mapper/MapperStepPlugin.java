/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.MapperPanel;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class MapperStepPlugin extends TransformerStepPlugin {

    private MapperPanel panel;
    private TransformerPane parent;

    public MapperStepPlugin(String name) {
        super(name);
    }

    @Override
    public void initialize(TransformerPane pane) {
        this.parent = pane;
        panel = new MapperPanel(parent);
    }

    @Override
    public BasePanel getPanel() {
        return panel;
    }

    @Override
    public boolean isNameEditable() {
        return false;
    }

    private boolean isInvalidVar(String var) {
        return !var.matches("[a-zA-Z0-9_]+");
    }

    @Override
    public Map<Object, Object> getData(int row) {
        Map<Object, Object> data = panel.getData();
        String var = data.get("Variable").toString();

        if (var == null || var.equals("") || !((TransformerPane) parent).isUnique(var, row, false) || isInvalidVar(var)) {
            parent.setInvalidVar(true);
            String msg = "";
            parent.setRowSelectionInterval(row, row);

            if (var == null || var.equals("")) {
                msg = "The variable name cannot be blank.";
            } else if (isInvalidVar(var)) {
                msg = "The variable name contains invalid characters.";
            } else // var is not unique
            {
                msg = "'" + data.get("Variable") + "'" + " is not unique.";
            }
            msg += "\nPlease enter a new variable name.\n";

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
    public String getStepName() {
        return (String) ((Map<Object, Object>) panel.getData()).get("Variable");
    }

    @Override
    public void clearData() {
        panel.setData(null);
    }

    @Override
    public void initData() {
        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("Mapping", "");
        data.put("Variable", "");
        data.put(UIConstants.IS_GLOBAL, UIConstants.IS_GLOBAL_CHANNEL);
        panel.setData(data);
    }

    @Override
    public String getScript(Map<Object, Object> map) {
        String regexArray = buildRegexArray(map);

        StringBuilder script = new StringBuilder();

        script.append("var mapping;");
        script.append("try { mapping = " + (String) map.get("Mapping") + "; }");
        script.append("catch (e) { logger.error(e);  mapping = '';}");

        if (map.get(UIConstants.IS_GLOBAL) != null) {
            script.append((String) map.get(UIConstants.IS_GLOBAL) + "Map.put(");
        } else {
            script.append(UIConstants.IS_GLOBAL_CHANNEL + "Map.put(");
        }

        // default values need to be provided
        // so we don't cause syntax errors in the JS
        script.append("'" + map.get("Variable") + "', ");
        String defaultValue = (String) map.get("DefaultValue");
        if (defaultValue.length() == 0) {
            defaultValue = "''";
        }

        script.append("validate( mapping , " + defaultValue + ", " + regexArray + "));");
        return script.toString();
    }

    private String buildRegexArray(Map<Object, Object> map) {
        ArrayList<String[]> regexes = (ArrayList<String[]>) map.get("RegularExpressions");
        StringBuilder regexArray = new StringBuilder();
        regexArray.append("new Array(");
        if (regexes.size() > 0) {
            for (int i = 0; i < regexes.size(); i++) {
                regexArray.append("new Array(" + regexes.get(i)[0] + ", " + regexes.get(i)[1] + ")");
                if (i + 1 == regexes.size()) {
                    regexArray.append(")");
                } else {
                    regexArray.append(",");
                }
            }
        } else {
            regexArray.append(")");
        }
        return regexArray.toString();
    }

    @Override
    public String doValidate(Map<Object, Object> data) {
        try {
            Context context = Context.enter();
            Script compiledFilterScript = context.compileString("function rhinoWrapper() {" + getScript(data) + "\n}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null);
        } catch (EvaluatorException e) {
            return "Error on line " + e.lineNumber() + ": " + e.getMessage() + ".";
        } catch (Exception e) {
            return "Unknown error occurred during validation.";
        } finally {
            Context.exit();
        }
        return null;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}

    @Override
    public String getPluginPointName() {
        return "Mapper";
    }

    @Override
    public void moveStart() {
        // Disable the document listener while this step is being moved so it doesn't rename other step
        panel.setDocumentListenerEnabled(false);
    }

    @Override
    public void moveEnd() {
        // Enable the document listener when the step has finished being moved
        panel.setDocumentListenerEnabled(true);
    }
}
