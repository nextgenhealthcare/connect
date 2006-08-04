package com.webreach.mirth.server.mule.filters;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

import com.webreach.mirth.server.mule.components.ChannelComponent;
import com.webreach.mirth.server.mule.util.ER7Util;

public class JavaScriptFilter implements UMOFilter {
	private Logger logger = Logger.getLogger(this.getClass());
	private String script;

	public String getScript() {
		return this.script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public boolean accept(UMOMessage source) {
		try {
			String message = source.getPayloadAsString();
			
			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);
			
			HashMap localMap = new HashMap();

			// load variables in JavaScript scope
			scope.put("message", scope, (String) new ER7Util().ConvertToXML(message));
			scope.put("incomingMessage", scope, ((String) new ER7Util().ConvertToER7(message)));
			scope.put("logger", scope, logger);
			scope.put("localMap", scope, localMap);
			scope.put("globalMap", scope, ChannelComponent.globalMap);

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("importPackage(Packages.com.webreach.mirth.server.util);\n	");
			jsSource.append("function doFilter() {default xml namespace = new Namespace(\"urn:hl7-org:v2xml\"); var msg = new XML(message);\n " + script + " }\n");
			jsSource.append("doFilter()\n");

			logger.debug("executing filter script:\n\t" + jsSource.toString().replace("\\", "\\\\"));
			Object result = context.evaluateString(scope, jsSource.toString().replace("\\", "\\\\"), "<cmd>", 1, null);
			return ((Boolean) Context.jsToJava(result, java.lang.Boolean.class)).booleanValue();
		} catch (Exception e) {
			logger.error(e);
			return false;
		} finally {
			Context.exit();
		}
	}
}
