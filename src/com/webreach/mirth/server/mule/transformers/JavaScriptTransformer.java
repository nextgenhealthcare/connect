package com.webreach.mirth.server.mule.transformers;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.util.EmailSender;

public class JavaScriptTransformer extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private String script;

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public Object doTransform(Object source) throws TransformerException {
		try {
			Context context = Context.enter();
			Scriptable scope = context.initStandardObjects();
			HashMap map = new HashMap();
			
			// create email sender
			Properties properties = (new ConfigurationController()).getServerProperties();
			String host = properties.getProperty("smtp.host");
			int port = Integer.valueOf(properties.getProperty("smtp.port")).intValue();
			String username = properties.getProperty("smtp.username");
			String password = properties.getProperty("smtp.password");
			EmailSender sender = new EmailSender(host, port, username, password);

			// load variables in JavaScript scope
			scope.put("message", scope, source);
			scope.put("logger", scope, logger);
			scope.put("map", scope, map);
			scope.put("sender", scope, sender);

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("function debug(debug_message) { logger.debug(debug_message) }\n");
			jsSource.append("function sendEmail(to, cc, from, subject, body) { sender.sendEmail(to, cc, from, subject, body) }\n");
			jsSource.append("function doTransform() { default xml namespace = new Namespace(\"urn:hl7-org:v2xml\"); var msg = new XML(message); " + script + " }\n");
			jsSource.append("doTransform()\n");
			
			logger.debug("executing transformation script:\n\t" + jsSource.toString());
			context.evaluateString(scope, jsSource.toString(), "<cmd>", 1, null);

			return map;
		} catch (Exception e) {
			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}
}
