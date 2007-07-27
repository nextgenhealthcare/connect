package com.webreach.mirth.plugins;

import java.util.Map;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;

public abstract class TransformerStepPlugin {
	protected String name;
	protected TransformerPane parent;
	public TransformerStepPlugin(String name, TransformerPane parent) {
		this.parent = parent;
		this.name = name;
	}
	public String getName(){
		return name;
	}
	public abstract BasePanel getPanel();
	public abstract boolean isStepNameEditable();
	public String getNewStepName(){
		return new String();
	}
	public abstract String getDisplayName();
	public abstract Map<Object, Object> getData(int row);
	public abstract void setData(Map<Object, Object> data);
	public String getStepName(){
		return null;
	}
	public void doValidate(){
		return;
	}
	public boolean showValidateTask(){
		return false;
	}
	public abstract void setHighlighters();
	public abstract void unsetHighlighters();
	public abstract String getScript(Map<Object, Object> data);
	public abstract void clearData();
	public abstract void initData();
}
