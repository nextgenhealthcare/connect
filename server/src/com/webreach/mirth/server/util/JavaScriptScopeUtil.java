package com.webreach.mirth.server.util;

import org.mozilla.javascript.Scriptable;
import org.mule.providers.TemplateValueReplacer;

import com.webreach.mirth.model.MessageObject;

public class JavaScriptScopeUtil {
	// MessageObject builder
	public static void addMessageObject(Scriptable scope, MessageObject messageObject) {
		scope.put("messageObject", scope, messageObject);
		scope.put("message", scope, messageObject.getTransformedData());
		scope.put("connectorMap", scope, messageObject.getConnectorMap());
		scope.put("channelMap", scope, messageObject.getChannelMap());
		scope.put("responseMap", scope, messageObject.getResponseMap());
		scope.put("connector", scope, messageObject.getConnectorName());
	}

	// Generic and Channel Builder
	public static void addChannel(Scriptable scope, String channelId) {
		scope.put("alerts", scope, new AlertSender(channelId));
		scope.put("globalMap", scope, GlobalVariableStore.getInstance());
		scope.put("channelId", scope, channelId);
	}

	public static void buildScope(Scriptable scope) {
		scope.put("router", scope, new VMRouter());
		scope.put("replacer", scope, new TemplateValueReplacer());
	}

	// Logger builder
	public static void addLogger(Scriptable scope, Object logger) {
		scope.put("logger", scope, logger);
	}

	// Composite scopes
	public static void buildScope(Scriptable scope, MessageObject messageObject, Object logger) {
		addChannel(scope, messageObject.getChannelId());
		addMessageObject(scope, messageObject);
		addLogger(scope, logger);
	}

	public static void buildScope(Scriptable scope, String channelId, Object logger) {
		addChannel(scope, channelId);
		addLogger(scope, logger);
	}
}
