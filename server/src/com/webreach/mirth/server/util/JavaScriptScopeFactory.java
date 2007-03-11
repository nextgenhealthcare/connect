package com.webreach.mirth.server.util;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Scriptable;

import com.webreach.mirth.model.MessageObject;

public class JavaScriptScopeFactory {
	public void buildScope(Scriptable scope, MessageObject messageObject){
		scope.put("alert", scope, new AlertSender(messageObject.getChannelId()));
		scope.put("router", scope, new VMRouter());
		scope.put("response", scope, new ResponseFactory());
		scope.put("messageObject", scope, messageObject);
		scope.put("message", scope, messageObject.getTransformedData());
		scope.put("connectorContextMap", scope, messageObject.getVariableMap());
		scope.put("channelContextMap", scope, messageObject.getContextMap());
		scope.put("responseContextMap", scope, messageObject.getResponseMap());
		scope.put("globalContextMap", scope, GlobalVariableStore.getInstance());
		scope.put("connector", scope, messageObject.getConnectorName());
	}
	public void buildScope(Scriptable scope, MessageObject messageObject, Log scriptLogger){
		buildScope(scope, messageObject);
		scope.put("logger", scope, scriptLogger);
	}
	public void buildScope(Scriptable scope, MessageObject messageObject, Logger scriptLogger){
		buildScope(scope, messageObject);
		scope.put("logger", scope, scriptLogger);
	}
}
