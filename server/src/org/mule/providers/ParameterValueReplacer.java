package org.mule.providers;

import java.util.Map;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.util.GlobalVariableStore;

public class ParameterValueReplacer {
	public Object getValue(String template, MessageObject messageObject) {
		// Remove the ${} from the string
		String key = template.substring(2, template.length() - 1);

		Map variableMap = null;
		
		if (messageObject != null) {
			variableMap = messageObject.getConnectorMap();
		}
		
		if ((variableMap != null) && variableMap.containsKey(key)) {
			return variableMap.get(key);
		} else if (GlobalVariableStore.getInstance().getVariables().containsKey(key)) {
			return GlobalVariableStore.getInstance().get(key);
		} else {
			TemplateValueReplacer replacer = new TemplateValueReplacer();
			return replacer.replaceValues(template, messageObject);
		}
	}
}