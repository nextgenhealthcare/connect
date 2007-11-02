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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOMessageAdapter;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.JavaScriptScopeUtil;

public class JavaScriptMessageDispatcher extends AbstractMessageDispatcher {
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = AlertController.getInstance();
	private JavaScriptConnector connector;
	private MonitoringController monitoringController = MonitoringController.getInstance();
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

			// load variables in JavaScript scope
			JavaScriptScopeUtil.buildScope(scope, messageObject, logger);

			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledScript(this.connector.getScriptId());

			if (compiledScript == null) {
				logger.warn("script could not be found in cache");
				messageObjectController.setError(messageObject, Constants.ERROR_406, "Script not found in cache", null);
			} else {
				compiledScript.exec(context, scope);
				String response = "Script execution successful";
				// the user could write Javascript that sets the response
				// for this connector
				// if that's the case, then let's save it
				if (messageObject.getResponseMap().containsKey(messageObject.getConnectorName())) {
					response = (String) messageObject.getResponseMap().get(messageObject.getConnectorName());
				}
				messageObjectController.setSuccess(messageObject, response);
			}

		} catch (Exception e) {
			logger.debug("Error dispatching event: " + e.getMessage(), e);

			alertController.sendAlerts(((JavaScriptConnector) connector).getChannelId(), Constants.ERROR_406, "Error writing to database", e);
			messageObjectController.setError(messageObject, Constants.ERROR_406, "Error writing to database: ", e);
			connector.handleException(e);
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
		// TODO Auto-generated method stub
		return null;
	}
}
