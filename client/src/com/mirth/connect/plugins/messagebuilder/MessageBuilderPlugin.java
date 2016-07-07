/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagebuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.MessageBuilder;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.plugins.TransformerStepPlugin;

public class MessageBuilderPlugin extends TransformerStepPlugin {

    private MessageBuilder panel;
    private TransformerPane parent;

    public MessageBuilderPlugin(String name) {
        super(name);
    }

    @Override
    public void initialize(TransformerPane pane) {
        this.parent = pane;
        panel = new MessageBuilder(parent);
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
        if (var == null || var.equals("")) {
            parent.setInvalidVar(true);
            String msg = "";

            parent.setRowSelectionInterval(row, row);

            if (var == null || var.equals("")) {
                msg = "The mapping field cannot be blank.";
            }

            msg += "\nPlease enter a new mapping field name.\n";
            alertWarning(msg);
        } else {
            parent.setInvalidVar(false);
        }

        return data;
    }

    @Override
    public void setData(Mode mode, Map<Object, Object> data) {
        panel.setData(data);
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
        panel.setData(data);
    }

    @Override
    public String getScript(Map<Object, Object> data) {
        String regexArray = buildRegexArray(data);
        StringBuilder script = new StringBuilder();
        String variable = (String) data.get("Variable");
        String defaultValue = (String) data.get("DefaultValue");
        if (defaultValue.length() == 0) {
            defaultValue = "''";
        }
        String mapping = (String) data.get("Mapping");
        if (mapping.length() == 0) {
            mapping = "''";
        }
        script.append(variable);
        script.append(" = ");
        script.append("validate(" + mapping + ", " + defaultValue + ", " + regexArray + ");");
        return script.toString();
    }

    @Override
    public String getGeneratedScript(Map<Object, Object> data) {
        return getScript(data);
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
    public boolean showValidateTask() {
        return true;
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
        return "Message Builder";
    }
}
