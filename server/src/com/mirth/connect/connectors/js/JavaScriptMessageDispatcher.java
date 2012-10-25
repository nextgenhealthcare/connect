/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.Response;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;

public class JavaScriptMessageDispatcher extends AbstractMessageDispatcher {
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private JavaScriptConnector connector;
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.WRITER;
    private Logger scriptLogger = Logger.getLogger("js-connector");
    
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
            JavaScriptScopeUtil.buildScope(scope, messageObject, scriptLogger);
            Script compiledScript = compiledScriptCache.getCompiledScript(this.connector.getScriptId());

            if (compiledScript == null) {
                logger.warn("script could not be found in cache");
                messageObjectController.setError(messageObject, Constants.ERROR_414, "Script not found in cache", null, null);
            } else {
                compiledScript.exec(context, scope);
                Response response = new Response("Script execution successful");
                response.setStatus(Response.Status.SUCCESS);
                
                if (messageObject.getResponseMap().containsKey(messageObject.getConnectorName())) {
                    Object responseObj = messageObject.getResponseMap().get(messageObject.getConnectorName());
                    
                    if (responseObj instanceof String) {
                        response.setMessage((String) responseObj);
                    } else if (responseObj instanceof Response) {
                        response = (Response) responseObj;
                    }
                }

                messageObjectController.setSuccess(messageObject, response.getMessage(), null);
            }
        } catch (Throwable e) {
            logger.debug("Error dispatching event: " + e.getMessage(), e);

            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_414, "Error executing script", e);
            messageObjectController.setError(messageObject, Constants.ERROR_414, "Error executing script: ", e, null);
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
