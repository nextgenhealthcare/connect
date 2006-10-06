package org.mule.providers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.util.UUID;
import org.mule.util.Utility;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.mule.util.GlobalVariableStore;

public class TemplateValueReplacer {
	private final String DEFAULT_DATE_FORMAT = "dd-MM-yy_HH-mm-ss.SS";
	private final String TEMPLATE_REPLACE_PATTERN = "\\$\\{[^\\}]*\\}";
	private long count = 1;

	protected synchronized long getCount() {
		return count++;
	}

	public String replaceValues(String template, MessageObject messageObject, String filename) {
		// if the template has not been set, return an empty string
		if ((template == null) || !(template.length() > 0)) {
			return new String();
		}

		Pattern pattern = Pattern.compile(TEMPLATE_REPLACE_PATTERN);
		Matcher matcher = pattern.matcher(template);
		StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			String key = matcher.group();
			String name = key.substring(2, key.length() - 1);
			matcher.appendReplacement(buffer, getTemplateValue(name, messageObject, filename).replace("\\", "\\\\").replace("$", "\\$"));
		}

		matcher.appendTail(buffer);
		return buffer.toString();
	}

	public String getTemplateValue(String name, MessageObject messageObject, String filename) {
		// message variables
		if (messageObject != null) {
			Map map = messageObject.getVariableMap();

			if (name.equals("raw_data")) {
				return messageObject.getRawData();
			} else if (name.equals("transformed_data")) {
				return messageObject.getTransformedData();
			} else if (name.equals("encoded_data")) {
				return messageObject.getEncodedData();
			} else if (name.equals("message_id")) {
				return messageObject.getId();
			} else if (map.containsKey(name)) {
				return (String) map.get(name);
			} else if (GlobalVariableStore.getInstance().containsKey(name)) {
				return (String) GlobalVariableStore.getInstance().get(name);
			}
		}

		// default filename
		if (filename == null) {
			filename = System.currentTimeMillis() + ".dat";
		}

		// system variables
		if (name.equals("DATE")) {
			return Utility.getTimeStamp(DEFAULT_DATE_FORMAT);
		} else if (name.startsWith("DATE:")) {
			String dateformat = name.substring(5, name.length() - 1);
			return Utility.getTimeStamp(dateformat);
		} else if (name.equals("COUNT")) {
			return String.valueOf(getCount());
		} else if (name.equals("UUID")) {
			return new UUID().getUUID();
		} else if (name.equals("SYSTIME")) {
			return String.valueOf(System.currentTimeMillis());
		} else if (name.equals("ORIGINALNAME")) {
			return filename;
		}

		return new String();
	}

}
