/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.rulebuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.RuleBuilderPanel;
import com.mirth.connect.client.ui.editors.filter.FilterPane;
import com.mirth.connect.plugins.FilterRulePlugin;

public class RuleBuilderPlugin extends FilterRulePlugin {

    private RuleBuilderPanel panel;
    private FilterPane parent;

    public RuleBuilderPlugin(String name) {
        super(name);
    }

    @Override
    public void initialize(FilterPane pane) {
        this.parent = pane;
        panel = new RuleBuilderPanel(parent, this);
    }

    @Override
    public BasePanel getPanel() {
        return panel;
    }

    @Override
    public boolean isNameEditable() {
        return false;
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
    public void clearData() {
        panel.setData(null);
    }

    @Override
    public void initData() {
        clearData();
    }

    @Override
    public String getScript(Map<Object, Object> map) {

        StringBuilder script = new StringBuilder();

        String field = (String) map.get("Field");
        ArrayList<String> values = (ArrayList<String>) map.get("Values");
        String acceptReturn, finalReturn, equals, equalsOperator;

        acceptReturn = "true";
        finalReturn = "false";

        script.append("if(");

        if (((String) map.get("Equals")).equals(UIConstants.EXISTS_OPTION)) {
            script.append(field + ".length > 0)\n");
        } else if (((String) map.get("Equals")).equals(UIConstants.DOES_NOT_EXIST_OPTION)) {
            script.append(field + ".length == 0)\n");
        } else if (((String) map.get("Equals")).equals(UIConstants.CONTAINS_OPTION) || ((String) map.get("Equals")).equals(UIConstants.DOES_NOT_CONTAIN_OPTION)) {
            if (((String) map.get("Equals")).equals(UIConstants.CONTAINS_OPTION)) {
                equals = "!=";
                equalsOperator = "||";
            } else {
                equals = "==";
                equalsOperator = "&&";
            }

            if (values.size() > 0) {
                for (int i = 0; i < values.size(); i++) {
                    script.append("(" + field + ".indexOf(" + values.get(i) + ") " + equals + " -1)");
                    if (i + 1 == values.size()) {
                        script.append(")\n");
                    } else {
                        script.append(" " + equalsOperator + " ");
                    }
                }
            } else {
                script.append(field + ".indexOf(\"\") " + equals + " -1)\n");
            }
        } else {
            if (((String) map.get("Equals")).equals(UIConstants.YES_OPTION)) {
                equals = "==";
                equalsOperator = "||";
            } else {
                equals = "!=";
                equalsOperator = "&&";
            }

            if (values.size() > 0) {
                for (int i = 0; i < values.size(); i++) {
                    script.append(field + " " + equals + " " + values.get(i));
                    if (i + 1 == values.size()) {
                        script.append(")\n");
                    } else {
                        script.append(" " + equalsOperator + " ");
                    }
                }
            } else {
                script.append(field + " " + equals + " \"\")\n");
            }
        }

        script.append("{\n");
        script.append("return " + acceptReturn + ";");
        script.append("\n}\n");
        script.append("return " + finalReturn + ";");

        return script.toString();
    }

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

    public boolean showValidateTask() {
        return true;
    }

    public boolean isProvideOwnStepName() {
        return true;
    }

    @Override
    public String getStepName() {
        Map<Object, Object> map = panel.getData();
        if (map == null || map.get("Equals") == null || map.get("Field") == null || map.get("Values") == null) {
            return "New Rule";
        }
        String name = "";
        String equals = "";
        String blankVal = "";
        boolean disableValues = false;
        name = "Accept";

        if (((String) map.get("Equals")).equals(UIConstants.EXISTS_OPTION)) {
            equals = "equals";
            blankVal = "exists";
            disableValues = true;
        } else if (((String) map.get("Equals")).equals(UIConstants.DOES_NOT_EXIST_OPTION)) {
            equals = "does not equal";
            blankVal = "does not exist";
            disableValues = true;
        } else if (((String) map.get("Equals")).equals(UIConstants.YES_OPTION)) {
            equals = "equals";
            blankVal = "is blank";
            disableValues = false;
        } else if (((String) map.get("Equals")).equals(UIConstants.NO_OPTION)) {
            equals = "does not equal";
            blankVal = "is not blank";
            disableValues = false;
        } else if (((String) map.get("Equals")).equals(UIConstants.CONTAINS_OPTION)) {
            equals = "contains";
            blankVal = "contains \"\"";
            disableValues = false;
        } else if (((String) map.get("Equals")).equals(UIConstants.DOES_NOT_CONTAIN_OPTION)) {
            equals = "does not contain";
            blankVal = "does not contain \"\"";
            disableValues = false;
        }

        String fieldDescription = "";
        if (((String) map.get("Field")).equals((String) map.get("OriginalField"))) {
            fieldDescription = (String) map.get("Name");
        } else {
            fieldDescription = (String) map.get("Field");
        }

        ArrayList<String> values = (ArrayList<String>) map.get("Values");
        String valueList = "";
        if (values.isEmpty() || disableValues) {
            return name + " message if \"" + fieldDescription + "\" " + blankVal;
        } else {
            for (Iterator iter = values.iterator(); iter.hasNext();) {
                String value = (String) iter.next();
                valueList += value + " or ";
            }
            valueList = valueList.substring(0, valueList.length() - 4);
            return name + " message if \"" + fieldDescription + "\" " + equals + " " + valueList;
        }
    }

    public void updateName() {
        parent.updateName(parent.getSelectedRow(), getStepName());
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}

    @Override
    public String getPluginPointName() {
        return "Rule Builder";
    }

    @Override
    public void moveStart() {
        // Disable the document listener while this rule is being moved so it doesn't rename other rules
        panel.setDocumentListenerEnabled(false);
    }

    @Override
    public void moveEnd() {
        // Enable the document listener when the rule has finished being moved
        panel.setDocumentListenerEnabled(true);
    }
}
