package com.webreach.mirth.plugins.transformer.step.messagebuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.MessageBuilder;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.plugins.TransformerStepPlugin;

public class MessageBuilderPlugin extends TransformerStepPlugin {
	private MessageBuilder panel;

	public MessageBuilderPlugin(String name, TransformerPane parent) {
		super(name, parent);
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
			((TransformerPane) parent).setInvalidVar(true);
			String msg = "";

			((TransformerPane) parent).setRowSelectionInterval(row, row);

			if (var == null || var.equals(""))
				msg = "The mapping field cannot be blank.";

			msg += "\nPlease enter a new mapping field name.\n";

			((TransformerPane) parent).getParentFrame().alertWarning(msg);
		} else {
			((TransformerPane) parent).setInvalidVar(false);
		}

		return data;
	}

	@Override
	public void setData(Map<Object, Object> data) {
		panel.setData(data);
	}

	public String getName() {
		String name = "Assign";
		String target = ((String) ((Map<Object, Object>) panel.getData()).get("Variable"));
		String mapping = ((String) ((Map<Object, Object>) panel.getData()).get("Mapping"));
		
		// If there is no mapping, just use the target for the name.
		if (mapping.equals(""))
			return target;
		
		// Remove the indexes that are added for multiple segments with the same name.
		target = target.replaceAll("\\[[^']\\]", "");
		mapping = mapping.replaceAll("\\[[^']\\]", "");
		
		String targetVar = "";
		String mappingVar = "";
		String targetDescription = "";
		String mappingDescription = "";
		if (target.startsWith("tmp[")) {
			targetVar = "(out)";
			target = target.substring(4);
		} else if (target.startsWith("msg[")) {
			targetVar = "(in)";
			target = target.substring(4);
		}
		target = target.replaceAll("'", "");
		target = target.substring(0, target.length() - 1); // get rid of
		// trailing "]"
		String[] targetparts = target.split("]\\[");
		targetDescription = getVocabDescription(targetparts);
		if (targetDescription.length() == 0){
			if (targetparts.length == 0){
				targetDescription = ((String) ((Map<Object, Object>) panel.getData()).get("Variable")).replaceAll("\\.toString\\(\\)","");
			}else{
				targetDescription = targetparts[targetparts.length-1].replaceAll("_", " ");
				targetDescription = targetDescription.substring(0, 1).toUpperCase() + targetDescription.substring(1);
			}
		}
		if (mapping.startsWith("tmp[")) {
			mappingVar = "(out)";
			mapping = mapping.substring(4);
		} else if (mapping.startsWith("msg[")) {
			mappingVar = "(in)";
			mapping = mapping.substring(4);
		}
		if (mapping != null && mapping.length() > 0 && mapping.indexOf("].toString()") > -1) {
			mapping = mapping.substring(0, mapping.indexOf("].toString()")).replaceAll("'", "");
			String[] mappingparts = mapping.split("]\\[");
			mappingDescription = getVocabDescription(mappingparts);
		} else {
			mappingDescription = "";
		}
		if (mappingDescription.length() == 0){
			String[] mappingParts = mapping.split("]\\[");
			if (mappingParts.length == 1){
				mappingDescription = ((String) ((Map<Object, Object>) panel.getData()).get("Mapping")).replaceAll("\\.toString\\(\\)","");
			}else{
				mappingDescription = mappingParts[mappingParts.length-1].replaceAll("_", " ");
				if (mappingDescription.length() > 0){
					mappingDescription = mappingDescription.substring(0, 1).toUpperCase() + mappingDescription.substring(1);
				}else{
					mappingDescription = "";
				}
			}
			
		}
		//return name + " " + mappingVar + " " + mappingDescription + " to " + targetVar + " " + targetDescription;
		return targetDescription + " " + targetVar + " <-- " + mappingDescription + " " + mappingVar; 
		
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

	public boolean isProvideOwnStepName() {
		return true;
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

	private String buildRegexArray(Map<Object, Object> map) {
		ArrayList<String[]> regexes = (ArrayList<String[]>) map.get("RegularExpressions");
		StringBuilder regexArray = new StringBuilder();
		regexArray.append("new Array(");
		if (regexes.size() > 0) {
			for (int i = 0; i < regexes.size(); i++) {
				regexArray.append("new Array(" + regexes.get(i)[0] + ", " + regexes.get(i)[1] + ")");
				if (i + 1 == regexes.size())
					regexArray.append(")");
				else
					regexArray.append(",");
			}
		} else {
			regexArray.append(")");
		}
		return regexArray.toString();
	}

	public String getDisplayName() {
		return "Message Builder";
	}
}
