package com.webreach.mirth.server.mule.filters;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.mule.components.InboundChannel;
import com.webreach.mirth.server.mule.util.ER7Util;
import com.webreach.mirth.server.util.EmailSender;
import com.webreach.mirth.server.util.EmbeddedDatabaseConnection;

public class JavaScriptFilter implements UMOFilter {
	private Logger logger = Logger.getLogger(this.getClass());
	private String script;
	private EmbeddedDatabaseConnection dbConnection = new EmbeddedDatabaseConnection();

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
//			 create email sender
			Properties properties = (new ConfigurationController()).getServerProperties();
			String host = properties.getProperty("smtp.host");
			
			int port = 25;
			
			if (properties.getProperty("smtp.port") != null && !properties.getProperty("smtp.port").equals("")) {
				port = Integer.valueOf(properties.getProperty("smtp.port")).intValue();	
			}
		
			String username = properties.getProperty("smtp.username");
			String password = properties.getProperty("smtp.password");
			EmailSender sender = new EmailSender(host, port, username, password);
			
			// load variables in JavaScript scope
			scope.put("message", scope, source);
			scope.put("incomingMessage", scope, ((String)new ER7Util().ConvertToER7(message)));
			scope.put("logger", scope, logger);
			scope.put("localMap", scope, localMap);
			scope.put("globalMap", scope, InboundChannel.globalMap);
			scope.put("sender", scope, sender);
			scope.put("dbconnection", scope, dbConnection);

			StringBuilder jsSource = new StringBuilder();
			jsSource.append("function debug(debug_message) { logger.debug(debug_message) }\n");
			jsSource.append("function queryDatabase(driver, address, query) { return dbConnection.executeQuery(driver, address, query) }\n");
			jsSource.append("function updateDatabase(driver, address, query) { return dbConnection.executeUpdate(driver, address, query) }\n");
			jsSource.append("function sendEmail(to, cc, from, subject, body) { sender.sendEmail(to, cc, from, subject, body) }");
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
