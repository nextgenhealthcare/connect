/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class DatabaseDispatcherScript implements DatabaseDispatcherDelegate {
    private String scriptId;
    private DatabaseDispatcher connector;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private Logger scriptLogger = Logger.getLogger("db-connector");
    private Logger logger = Logger.getLogger(this.getClass());

    public DatabaseDispatcherScript(DatabaseDispatcher connector) {
        this.connector = connector;
    }

    @Override
    public void deploy() throws DeployException {
        DatabaseDispatcherProperties connectorProperties = (DatabaseDispatcherProperties) connector.getConnectorProperties();
        scriptId = UUID.randomUUID().toString();

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, connectorProperties.getQuery(), null, null);
        } catch (Exception e) {
            throw new DeployException("Error compiling script " + scriptId + ".", e);
        }
    }

    @Override
    public void undeploy() throws UndeployException {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    @Override
    public Response send(DatabaseDispatcherProperties connectorProperties, ConnectorMessage connectorMessage) throws DatabaseDispatcherException, InterruptedException {
        // TODO Attachments will not be re-attached when using JavaScript yet.
        try {
            return (Response) JavaScriptUtil.execute(new DatabaseDispatcherTask(connectorMessage));
        } catch (JavaScriptExecutorException e) {
            throw new DatabaseDispatcherException("Error executing script " + scriptId, e);
        }
    }

    private class DatabaseDispatcherTask extends JavaScriptTask<Object> {
        private ConnectorMessage connectorMessage;

        public DatabaseDispatcherTask(ConnectorMessage connectorMessage) {
            this.connectorMessage = connectorMessage;
        }

        @Override
        public Object call() {
            String responseData = null;
            String responseError = null;
            String responseStatusMessage = "Database write success";
            Status responseStatus = Status.SENT;

            Scriptable scope = JavaScriptScopeUtil.getMessageDispatcherScope(scriptLogger, connector.getChannelId(), new ImmutableConnectorMessage(connectorMessage, true, connector.getDestinationNameMap()));

            try {
                Object result = JavaScriptUtil.executeScript(this, scriptId, scope, connector.getChannelId(), connector.getDestinationName());

                if (result != null && !(result instanceof Undefined)) {
                    /*
                     * If the script return value is a response, return it as-is. If it's a
                     * status, only update the response status. Otherwise, set the response data
                     * to the string representation of the object.
                     */
                    if (result instanceof NativeJavaObject) {
                        Object object = ((NativeJavaObject) result).unwrap();

                        if (object instanceof Response) {
                            return (Response) object;
                        } else if (object instanceof Status) {
                            responseStatus = (Status) object;
                        } else {
                            responseData = object.toString();
                        }
                    } else if (result instanceof Response) {
                        return (Response) result;
                    } else if (result instanceof Status) {
                        responseStatus = (Status) result;
                    } else {
                        responseData = (String) Context.jsToJava(result, java.lang.String.class);
                    }
                }
            } catch (Exception e) {
                ConnectorProperties connectorProperties = connector.getConnectorProperties();
                responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error evaluating " + connectorProperties.getName(), e);
                responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_414, "Error evaluating " + connector.getConnectorProperties().getName(), e);
                responseStatus = Status.QUEUED;

                logger.error("Error evaluating " + connectorProperties.getName() + " (" + connectorProperties.getName() + " \"" + connector.getDestinationName() + "\" on channel " + connector.getChannelId() + ").", e);
                eventController.dispatchEvent(new ErrorEvent(connector.getChannelId(), connector.getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, connector.getDestinationName(), "Error evaluating " + connectorProperties.getName(), e));
            }

            return new Response(responseStatus, responseData, responseStatusMessage, responseError);
        }
    }
}
