package com.webreach.mirth.server.mule.filters;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

public class JavaScriptFilter implements UMOFilter {
	private Logger logger = Logger.getLogger(JavaScriptFilter.class);
	private String script;

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public boolean accept(UMOMessage source) {
		try {
			String message = (String) source.getPayloadAsString();

			Context context = Context.enter();
			Scriptable scope = context.initStandardObjects();

			scope.put("message", scope, message);
			scope.put("logger", scope, logger);

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("function debug(debug_message) { logger.debug(debug_message) }\n");
			jsSource.append("function doFilter() { default xml namespace = new Namespace(\"urn:hl7-org:v2xml\"); var msg = new XML(message); " + script + " }\n");
			jsSource.append("doFilter()\n");

			logger.debug("executing filter script:\n\t" + jsSource.toString());
			Object result = context.evaluateString(scope, jsSource.toString(), "<cmd>", 1, null);

			return ((Boolean) Context.jsToJava(result, java.lang.Boolean.class)).booleanValue();
		} catch (Exception e) {
			logger.error(e);
			return false;
		} finally {
			Context.exit();
		}
	}
}
