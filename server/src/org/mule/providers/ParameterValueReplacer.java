package org.mule.providers;

import java.util.Map;

import org.mule.util.UUID;
import org.mule.util.Utility;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.mule.util.GlobalVariableStore;

public class ParameterValueReplacer {
	private static final String TEMPLATE_REPLACE_PATTERN = "\\$\\{[^\\}]*\\}";

	public static final String DEFAULT_DATE_FORMAT = "dd-MM-yy_HH-mm-ss.SS";

	private static long count = 1;
	
	protected synchronized long getCount() {
		return count++;
	}
	//TODO: check logic against current var list
	public Object getValue(String name,
		MessageObject messageObject) {
		//Remove the ${} from the string
		name = name.substring(2, name.length() - 1);
		Map map = null;
		if (messageObject != null)
			map = messageObject.getVariableMap();
		if (name.equals("message.getRawData()")) {
			return messageObject.getRawData();
		} else if (name.equals("transformed_data")) {
			return messageObject.getTransformedData();
		} else if (name.equals("encoded_data")) {
			return messageObject.getEncodedData();
		} else if (name.equals("DATE")) {
			return (Utility.getTimeStamp(DEFAULT_DATE_FORMAT));
		} else if (name.startsWith("DATE:")) {
			String dateformat = name.substring(5, name.length() - 1);
			return (Utility.getTimeStamp(dateformat));
		} else if (name.equals("COUNT")) {
			return getCount();
		} else if (name.equals("UUID") || name.equals("message_id")) {
			if (messageObject != null)
				return messageObject.getId();
			else
				return new UUID().getUUID();
		} else if (name.equals("SYSTIME")) {
			return String.valueOf(System.currentTimeMillis());
		} else if (map != null && map.containsKey(name)) {
			return map.get(name);
		} else if (GlobalVariableStore.getInstance().containsKey(name)) {
			return GlobalVariableStore.getInstance().get(name);
		} else {
			// TODO: this should return a special
			// character to indicate that the value was
			// not found
			return new String();
		}
	}
}