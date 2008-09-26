package com.webreach.mirth.plugins.javascriptrule;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.ScriptPanel;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.model.CodeTemplate.ContextType;
import com.webreach.mirth.plugins.FilterRulePlugin;

public class JavascriptRulePlugin extends FilterRulePlugin
{
    private ScriptPanel panel;

    public JavascriptRulePlugin(String name)
    {
        super(name);
    }

    public JavascriptRulePlugin(String name, FilterPane parent)
    {
        super(name, parent);
        panel = new ScriptPanel(parent, new JavaScriptTokenMarker(), ContextType.MESSAGE_CONTEXT.getContext());
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

    public String doValidate(Map<Object, Object> data)
    {
        try
        {
            Context context = Context.enter();
            Script compiledFilterScript = context.compileString("function rhinoWrapper() {" + getScript(data) + "\n}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null);
        }
        catch (EvaluatorException e)
        {
            return "Error on line " + e.lineNumber() + ": " + e.getMessage() + ".";
        }
        catch (Exception e)
        {
            return "Unknown error occurred during validation.";
        }
        finally
        {
            Context.exit();
        }
        return null;
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
    public String getDisplayName()
    {
        return "JavaScript";
    }

}
