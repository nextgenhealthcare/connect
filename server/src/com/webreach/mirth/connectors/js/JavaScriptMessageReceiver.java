/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcMessageReceiver.java,v 1.10 2005/10/23 15:21:21 holger Exp $
 * $Revision: 1.10 $
 * $Date: 2005/10/23 15:21:21 $
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.impl.MuleMessage;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.mule.transformers.JavaScriptPostprocessor;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.JavaScriptScopeUtil;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.10 $
 */
public class JavaScriptMessageReceiver extends PollingMessageReceiver {
	Logger scriptLogger = Logger.getLogger("js-receiver");
	private JavaScriptConnector connector;
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private JavaScriptPostprocessor postprocessor = new JavaScriptPostprocessor();
	private ConnectorType connectorType = ConnectorType.READER;

	public JavaScriptMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
		super(connector, component, endpoint, new Long(((JavaScriptConnector) connector).getPollingFrequency()));

		if (((JavaScriptConnector) connector).getPollingType().equals(JavaScriptConnector.POLLING_TYPE_TIME))
			setTime(((JavaScriptConnector) connector).getPollingTime());
		else
			setFrequency(((JavaScriptConnector) connector).getPollingFrequency());

		this.connector = (JavaScriptConnector) connector;
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public JavaScriptMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, String readStmt, String ackStmt) throws InitialisationException {
		super(connector, component, endpoint, new Long(((JavaScriptConnector) connector).getPollingFrequency()));

		this.connector = (JavaScriptConnector) connector;

		if (((JavaScriptConnector) connector).getPollingType().equals(JavaScriptConnector.POLLING_TYPE_TIME))
			setTime(((JavaScriptConnector) connector).getPollingTime());
		else
			setFrequency(((JavaScriptConnector) connector).getPollingFrequency());

		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}
	
	public void poll() {
		monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);
		try {
			List messages = getMessages();
			
			for (int i = 0; i < messages.size(); i++) {
				monitoringController.updateStatus(connector, connectorType, Event.BUSY);
				processMessage(messages.get(i));
				monitoringController.updateStatus(connector, connectorType, Event.DONE);
			}
		} catch (Exception e) {
			alertController.sendAlerts(((JavaScriptConnector) connector).getChannelId(), Constants.ERROR_414, null, e);
			handleException(e);
		}finally{
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}
	
	public void processMessage(Object message) throws Exception {
		try {
			monitoringController.updateStatus(connector, connectorType, Event.BUSY);

			// dispatch messages
			UMOMessageAdapter msgAdapter = this.connector.getMessageAdapter(message);
			UMOMessage umoMessage = new MuleMessage(msgAdapter);
			// we should get an MO back (if we're synchronized...)
			umoMessage = routeMessage(umoMessage, endpoint.isSynchronous());

			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);
			// load variables in JavaScript scope
			JavaScriptScopeUtil.buildScope(scope, connector.getName(), scriptLogger);
			
			if (umoMessage != null) {
				MessageObject messageObject = (MessageObject) umoMessage.getPayload();
				postprocessor.doPostProcess(messageObject);
				scope.put("responseMap", scope, messageObject.getResponseMap());
			}

		} catch (Exception e) {
			alertController.sendAlerts(((JavaScriptConnector) connector).getChannelId(), Constants.ERROR_414, null, e);
			throw e;
		} finally {
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}

	public List getMessages() throws Exception {
		monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);
		try {

			Context context = Context.enter();
			Scriptable scope = new ImporterTopLevel(context);

			// load variables in JavaScript scope
			JavaScriptScopeUtil.buildScope(scope, connector.getName(), scriptLogger);
			// each time we poll, we want to clear the map.
			// we need to document this
			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledScript(this.connector.getScriptId());

			if (compiledScript == null) {
				logger.error("Script could not be found in cache");
				throw new Exception("Script could not be found in cache");
			} else {
				Object result = null;
				try {
					result = compiledScript.exec(context, scope);
				} catch (Exception e) {
					logger.error(e);
					alertController.sendAlerts(((JavaScriptConnector) connector).getChannelId(), Constants.ERROR_414, null, e);
					return null;
				}
				if (result instanceof NativeJavaObject) {
					Object javaRetVal = ((NativeJavaObject) result).unwrap();

					if (javaRetVal instanceof String) {
						List list = new ArrayList();
						list.add((String) javaRetVal);
						return list;
					} else if (javaRetVal instanceof List) {
						return (List) javaRetVal;
					} else {
						logger.error("Got a result of: " + javaRetVal.toString());
					}
				} else {
					List list = new ArrayList();
					list.add(result.toString());
					return list;
					//logger.error("Got a result of: " + result.toString());
				}

				return null;
			}

		} catch (Exception e) {
			alertController.sendAlerts(((JavaScriptConnector) connector).getChannelId(), Constants.ERROR_414, null, e);
			throw e;
		} finally {
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}

	@Override
	public void doConnect() throws Exception {
	// TODO Auto-generated method stub

	}

	@Override
	public void doDisconnect() throws Exception {
	// TODO Auto-generated method stub

	}

}
