/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.javascriptstep;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.ScriptPanel;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.plugins.TransformerStepPlugin;
import com.mirth.connect.util.JavaScriptContextUtil;

public class JavascriptStepPlugin extends TransformerStepPlugin {

    private ScriptPanel panel;
    private TransformerPane parent;

    public JavascriptStepPlugin(String name) {
        super(name);
    }

    @Override
    public void initialize(TransformerPane pane) {
        this.parent = pane;
        panel = new ScriptPanel(parent, new JavaScriptTokenMarker());
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
        return "New Step";
    }

    @Override
    public Map<Object, Object> getData(int row) {
        return panel.getData();
    }

    @Override
    public void setData(Mode mode, Map<Object, Object> data) {
        panel.setData(data);
        panel.setContextType(mode == Mode.SOURCE ? ContextType.SOURCE_FILTER_TRANSFORMER : ContextType.DESTINATION_FILTER_TRANSFORMER);
    }

    @Override
    public void clearData() {
        panel.setData(null);
    }

    @Override
    public void initData() {
        parent.invalidVar = false;
        clearData();
    }

    public String doValidate(Map<Object, Object> data) {
        try {
            Context context = JavaScriptContextUtil.getGlobalContextForValidation();
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

    @Override
    public String getGeneratedScript(Map<Object, Object> data) {
        return getScript(data);
    }

    public boolean showValidateTask() {
        return true;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}

    @Override
    public String getPluginPointName() {
        return "JavaScript";
    }
}
