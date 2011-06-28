/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package org.mule.providers;

import java.util.Map;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.util.GlobalVariableStore;

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