package com.webreach.mirth.plugins.rulebuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.RuleBuilderPanel;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.plugins.FilterRulePlugin;

public class RuleBuilderPlugin extends FilterRulePlugin
{
    private RuleBuilderPanel panel;
    private FilterPane parent;

    public RuleBuilderPlugin(String name)
    {
        super(name);
    }

    public RuleBuilderPlugin(String name, FilterPane parent)
    {
        super(name, parent);
        this.parent = parent;
        panel = new RuleBuilderPanel(parent, this);
    }

    @Override
    public BasePanel getPanel()
    {
        return panel;
    }

    @Override
    public boolean isNameEditable()
    {
        return false;
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

    @Override
    public String getScript(Map<Object, Object> map)
    {

        StringBuilder script = new StringBuilder();

        String field = (String) map.get("Field");
        ArrayList<String> values = (ArrayList<String>) map.get("Values");
        String acceptReturn, finalReturn, equals, equalsOperator;

        if (((String) map.get("Accept")).equals(UIConstants.YES_OPTION))
        {
            acceptReturn = "true";
            finalReturn = "false";
        }
        else
        {
            acceptReturn = "false";
            finalReturn = "true";
        }

        script.append("if(");

        if (((String) map.get("Equals")).equals(UIConstants.EXISTS_OPTION))
        {
            script.append(field + ".length > 0)\n");
        }
        else if (((String) map.get("Equals")).equals(UIConstants.DOES_NOT_EXISTS_OPTION))
        {
            script.append(field + ".length == 0)\n");
        }
        else
        {
            if (((String) map.get("Equals")).equals(UIConstants.YES_OPTION))
            {
                equals = "==";
                equalsOperator = "||";
            }
            else
            {
                equals = "!=";
                equalsOperator = "&&";
            }

            for (int i = 0; i < values.size(); i++)
            {
                script.append(field + " " + equals + " " + values.get(i));
                if (i + 1 == values.size())
                    script.append(")\n");
                else
                    script.append(" " + equalsOperator + " ");
            }
        }

        script.append("{\n");
        script.append("return " + acceptReturn + ";");
        script.append("\n}\n");
        script.append("return " + finalReturn + ";");

        return script.toString();
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

    public boolean showValidateTask()
    {
        return true;
    }

    @Override
    public String getDisplayName()
    {
        return "Rule Builder";
    }

    public boolean isProvideOwnStepName()
    {
        return true;
    }

    public String getName()
    {
        /*
         * data.put("Field", mapping); data.put("Equals",
         * UIConstants.EXISTS_OPTION); data.put("Values", new ArrayList());
         * data.put("Accept", UIConstants.YES_OPTION);
         */
        Map<Object, Object> map = panel.getData();
        if (map == null || map.get("Equals") == null || map.get("Field") == null || map.get("Values") == null)
        {
            return "New Rule";
        }
        String accept = ((String) map.get("Accept"));
        String name = "";
        String equals = "";
        String blankVal = "";
        String valueCompound = "or";
        boolean disableValues = false;
        if (accept.equals(UIConstants.YES_OPTION))
        {
            name = "Accept";
        }
        else
        {
            name = "Reject";
        }
        if (((String) map.get("Equals")).equals(UIConstants.EXISTS_OPTION))
        {
            equals = "equals";
            blankVal = "exists";
            disableValues = true;
            // valueCompound = "or";
        }
        else if (((String) map.get("Equals")).equals(UIConstants.DOES_NOT_EXISTS_OPTION))
        {
            equals = "does not equal";
            blankVal = "does not exist";
            disableValues = true;
            // valueCompound = "and";
        }
        else if (((String) map.get("Equals")).equals(UIConstants.YES_OPTION))
        {
            equals = "equals";
            blankVal = "is blank";
            disableValues = false;
        }
        else if (((String) map.get("Equals")).equals(UIConstants.NO_OPTION))
        {
            equals = "does not equal";
            blankVal = "is not blank";
            disableValues = false;
        }

        String fieldDescription = "";
        if (((String) map.get("Field")).equals((String) map.get("OriginalField")))
            fieldDescription = (String) map.get("Name");
        else
            fieldDescription = (String) map.get("Field");

        ArrayList<String> values = (ArrayList<String>) map.get("Values");
        String valueList = "";
        if (values.isEmpty() || disableValues)
        {
            return name + " message if \"" + fieldDescription + "\" " + blankVal;
        }
        else
        {
            for (Iterator iter = values.iterator(); iter.hasNext();)
            {
                String value = (String) iter.next();
                valueList += value + " or ";
            }
            valueList = valueList.substring(0, valueList.length() - 4);
            return name + " message if \"" + fieldDescription + "\" " + equals + " " + valueList;
        }
    }

    public void updateName()
    {
        parent.updateName(parent.getSelectedRow(), getName());
    }
}
