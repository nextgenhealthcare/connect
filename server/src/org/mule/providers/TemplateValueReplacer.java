package org.mule.providers;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.tools.VelocityFormatter;
import org.apache.velocity.tools.generic.DateTool;
import org.mule.util.UUID;
import org.mule.util.Utility;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.util.GlobalVariableStore;
import com.webreach.mirth.util.Entities;

public class TemplateValueReplacer {
	private Logger logger = Logger.getLogger(this.getClass());
	private long count = 1;

	protected synchronized long getCount() {
		return count++;
	}

	public String replaceValues(String template, MessageObject messageObject, String originalFilename) {
		VelocityContext context = new VelocityContext();
		loadContext(context, messageObject, originalFilename);
		StringWriter writer = new StringWriter();

		try {
			Velocity.init();
			Velocity.evaluate(context, writer, "LOG", template);
		} catch (Exception e) {
			logger.warn("could not replace template values", e);
		}

		return writer.toString();
	}

	private void loadContext(VelocityContext context, MessageObject messageObject, String originalFilename) {
		// message variables
		if (messageObject != null) {
			context.put("message", messageObject);

			// load variables from global map
			// we don't use an iterator here because of concurrent modification
			// issues
			Map<String, Object> globalVariables = GlobalVariableStore.getInstance().getVariables();
			String[] keys = {};
			keys = globalVariables.keySet().toArray(keys);

			for (int i = 0; i < keys.length; i++) {
				context.put(keys[i], globalVariables.get(keys[i]));
			}

			// load variables from local map
			for (Iterator iter = messageObject.getConnectorMap().entrySet().iterator(); iter.hasNext();) {
				Entry entry = (Entry) iter.next();
				context.put(entry.getKey().toString(), entry.getValue());
			}
		}

		// default filename
		if (originalFilename == null) {
			originalFilename = System.currentTimeMillis() + ".dat";
		}

		// system variables
		// Calendar today = Calendar.getInstance();
		context.put("date", new DateTool());
		context.put("DATE", Utility.getTimeStamp("dd-MM-yy_HH-mm-ss.SS"));
		context.put("FORMATTER", new VelocityFormatter(context));
		context.put("COUNT", String.valueOf(getCount()));
		context.put("UUID", (new UUID()).getUUID());
		context.put("SYSTIME", String.valueOf(System.currentTimeMillis()));
		context.put("ORIGINALNAME", originalFilename);
		context.put("encoder", new Entities());
	}
}
