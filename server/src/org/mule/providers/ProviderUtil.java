package org.mule.providers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.mule.util.GlobalVariableStore;

public class ProviderUtil {
	private static final String TEMPLATE_REPLACE_PATTERN = "\\$\\{[^\\}]*\\}";

	public static String replaceValues(String template, MessageObject messageObject) throws Exception {
		// if the template has not been set, return an empty string
		if ((template == null) || !(template.length() > 0)) {
			return new String();
		}

		Pattern pattern = Pattern.compile(TEMPLATE_REPLACE_PATTERN);
		Matcher matcher = pattern.matcher(template);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String key = matcher.group();
			String name = key.substring(2, key.length() - 1);
			matcher.appendReplacement(sb, getTemplateValue(name, messageObject).replace("\\", "\\\\").replace("$", "\\$"));
		}

		matcher.appendTail(sb);
		return sb.toString();
	}

	public static String getTemplateValue(String name, MessageObject messageObject) {
		Map map = messageObject.getVariableMap();
		
		if (name.equals("raw_data")) {
			return messageObject.getRawData();
		} else if (name.equals("transformed_data")) {
			return messageObject.getTransformedData();
		} else if (name.equals("encoded_data")) {
			return messageObject.getEncodedData();
		} else if (map.containsKey(name)) {
			return (String) map.get(name);
		} else if (GlobalVariableStore.getInstance().containsKey(name)) {
			return (String) GlobalVariableStore.getInstance().get(name);
		} else {
			// TODO: this should return a special
			// character to indicate that the value was
			// not found
			return new String();
		}
	}
}
