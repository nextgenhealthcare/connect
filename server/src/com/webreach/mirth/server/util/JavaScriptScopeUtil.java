/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.util;

import org.mozilla.javascript.Scriptable;
import org.mule.providers.TemplateValueReplacer;

import com.webreach.mirth.model.MessageObject;

public class JavaScriptScopeUtil {
	// Composite scopes
	public static void buildScope(Scriptable scope) {
		scope.put("router", scope, new VMRouter());
		scope.put("replacer", scope, new TemplateValueReplacer());
	}

	public static void buildScope(Scriptable scope, Object logger) {
		buildScope(scope);
		addGlobalMap(scope);
		addLogger(scope, logger);
	}

	public static void buildScope(Scriptable scope, String channelId, Object logger) {
		buildScope(scope, logger);
		addChannel(scope, channelId);
	}

	public static void buildScope(Scriptable scope, MessageObject messageObject, Object logger) {
		buildScope(scope, messageObject.getChannelId(), logger);
		addMessageObject(scope, messageObject);
	}

	// MessageObject builder
	public static void addMessageObject(Scriptable scope, MessageObject messageObject) {
		scope.put("messageObject", scope, messageObject);
		scope.put("message", scope, messageObject.getTransformedData());
		scope.put("connectorMap", scope, messageObject.getConnectorMap());
		scope.put("channelMap", scope, messageObject.getChannelMap());
		scope.put("responseMap", scope, messageObject.getResponseMap());
		scope.put("connector", scope, messageObject.getConnectorName());
	}

	public static void addGlobalMap(Scriptable scope) {
		scope.put("globalMap", scope, GlobalVariableStore.getInstance());
	}

	// Generic and Channel Builder
	public static void addChannel(Scriptable scope, String channelId) {
		scope.put("alerts", scope, new AlertSender(channelId));
		scope.put("channelId", scope, channelId);
		scope.put("globalChannelMap", scope, GlobalChannelVariableStoreFactory.getInstance().get(channelId));
	}

	// Logger builder
	public static void addLogger(Scriptable scope, Object logger) {
		scope.put("logger", scope, logger);
	}
}