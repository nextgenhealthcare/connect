/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcMessageDispatcher.java,v 1.8 2005/08/29 12:29:08 rossmason Exp $
 * $Revision: 1.8 $
 * $Date: 2005/08/29 12:29:08 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.JavaScriptScopeUtil;

public class JavaScriptMessageDispatcher extends AbstractMessageDispatcher {
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
	private AlertController alertController = ControllerFactory.getFactory().createAlertController();
	private JavaScriptConnector connector;
	private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
	private ConnectorType connectorType = ConnectorType.WRITER;

	public JavaScriptMessageDispatcher(JavaScriptConnector connector) {
		super(connector);
		this.connector = connector;
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public void doDispose() {

	}

	public void doDispatch(UMOEvent event) throws Exception {
		monitoringController.updateStatus(connector, connectorType, Event.BUSY);
		if (logger.isDebugEnabled()) {
			logger.debug("Dispatch event: " + event);
		}

		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}

		try {
			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);
			JavaScriptScopeUtil.buildScope(scope, messageObject, logger);
			Script compiledScript = compiledScriptCache.getCompiledScript(this.connector.getScriptId());

			if (compiledScript == null) {
				logger.warn("script could not be found in cache");
				messageObjectController.setError(messageObject, Constants.ERROR_414, "Script not found in cache", null);
			} else {
				compiledScript.exec(context, scope);
				String response = "Script execution successful";

				if (messageObject.getResponseMap().containsKey(messageObject.getConnectorName())) {
					response = (String) messageObject.getResponseMap().get(messageObject.getConnectorName());
				}
				
				messageObjectController.setSuccess(messageObject, response);
			}

		} catch (Throwable e) {
			logger.debug("Error dispatching event: " + e.getMessage(), e);

			alertController.sendAlerts(((JavaScriptConnector) connector).getChannelId(), Constants.ERROR_414, "Error executing script", e);
			messageObjectController.setError(messageObject, Constants.ERROR_414, "Error executing script: ", e);
			connector.handleException(new Exception(e));
		} finally {
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}

	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		return null;
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}
}
