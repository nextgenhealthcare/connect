package org.mule.providers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.UUID;
import org.mule.util.Utility;

import sun.management.snmp.AdaptorBootstrap;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.mule.util.GlobalVariableStore;

public class ProviderUtil {
	private static final String TEMPLATE_REPLACE_PATTERN = "\\$\\{[^\\}]*\\}";

	public static final String DEFAULT_DATE_FORMAT = "dd-MM-yy_HH-mm-ss.SS";

	private static long count = 1;
	
	public static String replaceValues(String template, MessageObject messageObject) throws Exception {
		return replaceValues(template, "", messageObject);
	}
	public static String replaceValues(String template, String filename,
			MessageObject messageObject) throws Exception {
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
			matcher.appendReplacement(sb, getTemplateValue(name, messageObject, filename)
					.replace("\\", "\\\\").replace("$", "\\$"));
		}

		matcher.appendTail(sb);
		return sb.toString();
	}

	protected static synchronized long getCount() {
		return count++;
	}

	public static String getTemplateValue(String name,
			MessageObject messageObject) {
		return getTemplateValue(name, messageObject, "");
	}

	public static String getTemplateValue(String name,
			MessageObject messageObject, String filename) {
		Map map = messageObject.getVariableMap();

		if (name.equals("raw_data")) {
			return messageObject.getRawData();
		} else if (name.equals("transformed_data")) {
			return messageObject.getTransformedData();
		} else if (name.equals("encoded_data")) {
			return messageObject.getEncodedData();
		} else if (name.equals("DATE")) {
			return (Utility.getTimeStamp(DEFAULT_DATE_FORMAT));
		} else if (name.startsWith("DATE:")) {
			String dateformat = name.substring(7, name.length() - 1);
			return (Utility.getTimeStamp(dateformat));
		} else if (name.equals("COUNT")) {
			return (String.valueOf(getCount()));
		} else if (name.equals("UUID") || name.equals("message_id")) {
			return messageObject.getId();
		} else if (name.equals("SYSTIME")) {
			return String.valueOf(System.currentTimeMillis());
		} else if (name.equals("ORIGINALNAME")) {
			return filename;
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
