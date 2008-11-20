package com.webreach.mirth.plugins.scriptfilerule;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.ExternalScriptPanel;
import com.webreach.mirth.client.ui.editors.filter.FilterPane;
import com.webreach.mirth.plugins.FilterRulePlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: chrisr
 * Date: Nov 19, 2008
 * Time: 12:52:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalScriptRulePlugin extends FilterRulePlugin
{
	private ExternalScriptPanel panel;

    public ExternalScriptRulePlugin(String name)
    {
        super(name);
    }

	public ExternalScriptRulePlugin(String name, FilterPane parent) {
		super(name, parent);
		panel = new ExternalScriptPanel(parent, false);
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
	public String getDisplayName() {
		return "External Script";
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
	public String getScript(Map<Object, Object> data) {
		StringBuilder script = new StringBuilder();
		String variable = (String) data.get("Variable");
        script.append(variable);
		return script.toString();
	}

	@Override
	public void clearData() {
		panel.setData(null);
	}

	@Override
	public void initData() {
		Map<Object, Object> data = new HashMap<Object, Object>();
		data.put("Variable", "");
		panel.setData(data);
	}

    public String doValidate(Map<Object, Object> data)
    {
		String var = data.get("Variable").toString();
		// check for empty variable names
		if (var == null || var.trim().equals("")) {
			return "The script path field cannot be blank.\nPlease enter a new script path.\n";
		}
	    return null;
    }
}
