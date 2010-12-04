/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.javascriptrule;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.ScriptPanel;
import com.mirth.connect.client.ui.editors.filter.FilterPane;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.plugins.FilterRulePlugin;

public class JavascriptRulePlugin extends FilterRulePlugin {

    private ScriptPanel panel;

    public JavascriptRulePlugin(String name) {
        super(name);
    }

    @Override
    public void initialize(FilterPane pane) {
        panel = new ScriptPanel(pane, new JavaScriptTokenMarker(), ContextType.MESSAGE_CONTEXT.getContext());
    }

    @Override
    public BasePanel getPanel() {
        return panel;
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    public String getNewName() {
        return "New Rule";
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
    public String getScript(Map<Object, Object> data) {
        return data.get("Script").toString();
    }

    public boolean showValidateTask() {
        return true;
    }
}
