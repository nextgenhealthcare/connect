package com.webreach.mirth.server.mule.filters;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.mule.components.ChannelComponent;
import com.webreach.mirth.server.mule.util.ER7Util;
import com.webreach.mirth.server.util.SMTPConnection;
import com.webreach.mirth.server.util.SMTPConnectionFactory;

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
			Scriptable scope = context.initStandardObjects();
			HashMap localMap = new HashMap();

			// load variables in JavaScript scope
			scope.put("message", scope, (String) new ER7Util().ConvertToXML(message));
			scope.put("incomingMessage", scope, ((String) new ER7Util().ConvertToER7(message)));
			scope.put("logger", scope, logger);
			scope.put("localMap", scope, localMap);
			scope.put("globalMap", scope, ChannelComponent.globalMap);
			scope.put("smtpConnection", scope, SMTPConnectionFactory.getSMTPConnection());

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("function debug(debug_message) { logger.debug(debug_message) }\n");
			jsSource.append("function queryDatabase(driver, address, username, password, expression) { DatabaseConnection conn = DatabaseConnectionFactory.createDatabaseConnection(driver, address, username, password); return conn.executeQuery(expression); conn.close(); }\n");
			jsSource.append("function updateDatabase(driver, address, username, password, expression) { DatabaseConnection conn = DatabaseConnectionFactory.createDatabaseConnection(driver, address, username, password); return conn.executeUpdate(expression); conn.close() }\n");
			jsSource.append("function sendEmail(to, cc, from, subject, body) { smtpConnection.send(to, cc, from, subject, body) }");
			jsSource.append("function doFilter() { default xml namespace = new Namespace(\"urn:hl7-org:v2xml\"); var msg = new XML(message); " + script + " }\n");
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
