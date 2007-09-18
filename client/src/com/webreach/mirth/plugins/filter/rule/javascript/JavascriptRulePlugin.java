package com.webreach.mirth.plugins.filter.rule.javascript;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.ScriptPanel;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory;
import com.webreach.mirth.plugins.FilterRulePlugin;

public class JavascriptRulePlugin extends FilterRulePlugin
{
    private ScriptPanel panel;

    public JavascriptRulePlugin(String name, FilterPane parent)
    {
        super(name, parent);
        panel = new ScriptPanel(parent, new JavaScriptTokenMarker(), ReferenceListFactory.MESSAGE_CONTEXT);
    }

    @Override
    public BasePanel getPanel()
    {
        return panel;
    }

    @Override
    public boolean isNameEditable()
    {
        return true;
    }

    public String getNewName()
    {
        return "New Rule";
    }

    @Override
    public Map<Object, Object> getData(int row)
    {
        return panel.getData();
    }

    @Override
    public void setData(Map<Object, Object> data)
    {
        panel.setData(data);
    }

    @Override
    public void clearData()
    {
        panel.setData(null);
    }

    @Override
    public void initData()
    {
        clearData();
    }

    public void doValidate()
    {
        try
        {
            Context context = Context.enter();
            Script compiledFilterScript = context.compileString("function rhinoWrapper() {" + panel.getScript() + "}", null, 1, null);
            ((FilterPane)parent).getParentFrame().alertInformation("JavaScript was successfully validated.");
        }
        catch (EvaluatorException e)
        {
            ((FilterPane)parent).getParentFrame().alertInformation("Error on line " + e.lineNumber() + ": " + e.getMessage() + ".");
        }
        finally
        {
            Context.exit();
        }
    }

    @Override
    public String getScript(Map<Object, Object> data)
    {
        return data.get("Script").toString();
    }

    public boolean showValidateTask()
    {
        return true;
    }

    @Override
    public void setHighlighters()
    {
        panel.setHighlighters();
    }

    @Override
    public void unsetHighlighters()
    {
        panel.unsetHighlighters();
    }

    @Override
    public String getDisplayName()
    {
        return "JavaScript";
    }

}
