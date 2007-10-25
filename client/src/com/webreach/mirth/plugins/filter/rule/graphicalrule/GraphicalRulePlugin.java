package com.webreach.mirth.plugins.filter.rule.graphicalrule;

import java.util.ArrayList;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.GraphicalRulePanel;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.plugins.FilterRulePlugin;
import com.webreach.mirth.server.util.UUIDGenerator;

public class GraphicalRulePlugin extends FilterRulePlugin{
	private GraphicalRulePanel panel;
	public GraphicalRulePlugin(String name, FilterPane parent) {
		super(name, parent);	
		panel = new GraphicalRulePanel(parent);
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
        
        if(((String)map.get("Accept")).equals(UIConstants.YES_OPTION))
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
        
        if(((String)map.get("Equals")).equals(UIConstants.EXISTS_OPTION))
        {
            script.append(field + ".length > 0)\n"); 
        }        
        else
        {            
            if(((String)map.get("Equals")).equals(UIConstants.YES_OPTION))
            {
                equals = "==";
                equalsOperator = "||";
            }
            else
            {
                equals = "!=";
                equalsOperator = "&&";
            } 
            
            for(int i = 0; i < values.size(); i++)
            {
                script.append(field + " " + equals + " " + values.get(i));
                if(i + 1 == values.size())
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
    
    public String doValidate()
    {
        try
        {
            Context context = Context.enter();
            Script compiledFilterScript = context.compileString("function rhinoWrapper() {" + getScript(panel.getData()) + "}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null);
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
	public String getDisplayName() {
		return "Graphical Rule";
	}
}
