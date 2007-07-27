package com.webreach.mirth.plugins.transformer.step.javascript;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.JavaScriptPanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.plugins.TransformerStepPlugin;

public class JavascriptStepPlugin extends TransformerStepPlugin{
	private JavaScriptPanel panel;
	
	public JavascriptStepPlugin(String name, TransformerPane parent) {
		super(name, parent);
		panel = new JavaScriptPanel(parent);
	}

	@Override
	public BasePanel getPanel() {
		return panel;
	}

	@Override
	public boolean isStepNameEditable() {
		return true;
	}

	public String getNewStepName(){
		return "New Step";
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
	public void doValidate(){
		 try
	        {
	            Context context = Context.enter();
	            Script compiledFilterScript = context.compileString("function rhinoWrapper() {" + panel.getJavaScript() + "}", null, 1, null);
	            parent.getParentFrame().alertInformation("JavaScript was successfully validated.");
	        }
	        catch (EvaluatorException e)
	        {
	        	parent.getParentFrame().alertInformation("Error on line " + e.lineNumber() + ": " + e.getMessage() + ".");
	        }
	        finally
	        {
	            Context.exit();
	        }
	}

	@Override
	public String getScript(Map<Object, Object> data) {
		return data.get("Script").toString();
	}
	public boolean showValidateTask(){
		return true;
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
		return "JavaScript";
	}
	
}
