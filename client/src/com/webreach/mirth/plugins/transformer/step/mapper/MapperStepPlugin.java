package com.webreach.mirth.plugins.transformer.step.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.ScriptPanel;
import com.webreach.mirth.client.ui.editors.MapperPanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.plugins.TransformerStepPlugin;

public class MapperStepPlugin extends TransformerStepPlugin{
	private MapperPanel panel;
	public MapperStepPlugin(String name, TransformerPane parent) {
		super(name, parent);	
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

	@Override
	public Map<Object, Object> getData(int row) {
		Map<Object, Object> data = panel.getData();
        String var = data.get("Variable").toString();

        if (var == null || var.equals("") || !((TransformerPane)parent).isUnique(var, row, false) || var.indexOf(" ") != -1 || var.indexOf(".") != -1)
        {
            ((TransformerPane)parent).setInvalidVar(true);
            String msg = "";
            ((TransformerPane)parent).setRowSelectionInterval(row, row);

            if (var == null || var.equals(""))
                msg = "The variable name cannot be blank.";
            else if (var.indexOf(" ") != -1 || var.indexOf(".") != -1)
                msg = "The variable name contains invalid characters.";
            else
                // var is not unique
                msg = "'" + data.get("Variable") + "'" + " is not unique.";
            msg += "\nPlease enter a new variable name.\n";

            ((TransformerPane)parent).getParentFrame().alertWarning(msg);
        }
        else
        {
            ((TransformerPane)parent).setInvalidVar(false);
        }
        return data;
	}
	@Override
	public void setData(Map<Object, Object> data) {
		panel.setData(data);
	}
	public String getName(){
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
        data.put(UIConstants.IS_GLOBAL, UIConstants.IS_GLOBAL_CONNECTOR);
        panel.setData(data);
	}
	@Override
	public String getScript(Map<Object, Object> map) {
		String regexArray = buildRegexArray(map);

        StringBuilder script = new StringBuilder();

        if (map.get(UIConstants.IS_GLOBAL) != null)
            script.append((String)map.get(UIConstants.IS_GLOBAL) + "Map.put(");
        else
            script.append(UIConstants.IS_GLOBAL_CONNECTOR + "Map.put(");

        // default values need to be provided
        // so we don't cause syntax errors in the JS
        script.append("'" + map.get("Variable") + "', ");
        String defaultValue = (String) map.get("DefaultValue");
        if (defaultValue.length() == 0)
        {
            defaultValue = "''";
        }
        String mapping = (String) map.get("Mapping");
        if (mapping.length() == 0)
        {
            mapping = "''";
        }
        script.append("validate(" + mapping + ", " + defaultValue + ", " + regexArray + "));");
        return script.toString();
	}
	private String buildRegexArray(Map<Object, Object> map)
    {
        ArrayList<String[]> regexes = (ArrayList<String[]>) map.get("RegularExpressions");
        StringBuilder regexArray = new StringBuilder();
        regexArray.append("new Array(");
        if(regexes.size() > 0)
        {
            for(int i = 0; i < regexes.size(); i++)
            {
                regexArray.append("new Array(" + regexes.get(i)[0] + ", " + regexes.get(i)[1] + ")");
                if (i+1 == regexes.size())
                    regexArray.append(")");
                else
                    regexArray.append(",");
            }
        }
        else
        {
            regexArray.append(")");
        }
        return regexArray.toString();
    }
	@Override
	public void setHighlighters() {
		panel.setHighlighters();
	}

	@Override
	public void unsetHighlighters() {
		panel.unsetHighlighters();
	}
	@Override
	public String getDisplayName() {
		return "Mapper";
	}
}
