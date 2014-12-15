/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private Logger scriptLogger = Logger.getLogger("js-connector");
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private JavaScriptDispatcherProperties connectorProperties;
    private String scriptId;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (JavaScriptDispatcherProperties) getConnectorProperties();

        String scriptId = UUID.randomUUID().toString();

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, connectorProperties.getScript(), ContextType.MESSAGE_CONTEXT, null, null);
        } catch (Exception e) {
            throw new ConnectorTaskException("Error compiling/adding script.", e);
        }

        this.scriptId = scriptId;
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    @Override
    public void onStart() throws ConnectorTaskException {}

    @Override
    public void onStop() throws ConnectorTaskException {}

    @Override
    public void onHalt() throws ConnectorTaskException {}

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message) {}

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException {
        JavaScriptDispatcherProperties javaScriptDispatcherProperties = (JavaScriptDispatcherProperties) connectorProperties;

        try {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.SENDING));

            Response response = JavaScriptUtil.execute(new JavaScriptDispatcherTask(message));
            response.setValidate(javaScriptDispatcherProperties.getDestinationConnectorProperties().isValidateResponse());

            return response;
        } catch (JavaScriptExecutorException e) {
            logger.error("Error executing script (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), message.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error executing script", e));
            return new Response(Status.ERROR, null, ErrorMessageBuilder.buildErrorResponse("Error executing script", e), ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error executing script", e));
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
        }
    }

    private class JavaScriptDispatcherTask extends JavaScriptTask<Response> {
        private ConnectorMessage message;

        public JavaScriptDispatcherTask(ConnectorMessage message) {
            this.message = message;
        }

        @Override
        public Response call() throws Exception {
            String responseData = null;
            String responseError = null;
            String responseStatusMessage = "JavaScript evaluation successful.";
            Status responseStatus = Status.SENT;

            Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

            if (compiledScript == null) {
                responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Script not found in cache", null);
                responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Script not found in cache", null);
                responseStatus = Status.ERROR;

                logger.error("Script not found in cache (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), null, ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Script not found in cache", null));
            } else {
                try {
                    Scriptable scope = JavaScriptScopeUtil.getMessageDispatcherScope(scriptLogger, getChannelId(), new ImmutableConnectorMessage(message, true, JavaScriptDispatcher.this.getDestinationIdMap()));
                    Object result = executeScript(compiledScript, scope);

                    if (result != null && !(result instanceof Undefined)) {
                        /*
                         * If the script return value is a response, return it as-is. If it's a
                         * status, only update the response status. Otherwise, set the response data
                         * to the string representation of the object.
                         */
                        if (result instanceof NativeJavaObject) {
                            Object object = ((NativeJavaObject) result).unwrap();

                            if (object instanceof com.mirth.connect.userutil.Response) {
                                return JavaScriptUtil.convertToDonkeyResponse(object);
                            } else if (object instanceof com.mirth.connect.userutil.Status) {
                                responseStatus = JavaScriptUtil.convertToDonkeyStatus((com.mirth.connect.userutil.Status) object);
                            } else {
                                responseData = object.toString();
                            }
                        } else if (result instanceof com.mirth.connect.userutil.Response) {
                            return JavaScriptUtil.convertToDonkeyResponse(result);
                        } else if (result instanceof com.mirth.connect.userutil.Status) {
                            responseStatus = JavaScriptUtil.convertToDonkeyStatus((com.mirth.connect.userutil.Status) result);
                        } else {
                            responseData = (String) Context.jsToJava(result, java.lang.String.class);
                        }
                    }
                } catch (Throwable t) {
                    // Set the status message before we overwrite the exception
                    responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error evaluating " + getConnectorProperties().getName(), t);

                    if (t instanceof RhinoException) {
                        try {
                            String script = CompiledScriptCache.getInstance().getSourceScript(scriptId);
                            int linenumber = ((RhinoException) t).lineNumber();
                            String errorReport = JavaScriptUtil.getSourceCode(script, linenumber, 0);
                            t = new MirthJavascriptTransformerException((RhinoException) t, getChannelId(), getDestinationName(), 0, getConnectorProperties().getName(), errorReport);
                        } catch (Exception ee) {
                            t = new MirthJavascriptTransformerException((RhinoException) t, getChannelId(), getDestinationName(), 0, getConnectorProperties().getName(), null);
                        }
                    }

                    responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error evaluating " + getConnectorProperties().getName(), t);
                    responseStatus = Status.ERROR;

                    logger.error("Error evaluating " + getConnectorProperties().getName() + " (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", t);
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), null, ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error evaluating " + getConnectorProperties().getName(), t));
                } finally {
                    Context.exit();
                }
            }

            return new Response(responseStatus, responseData, responseStatusMessage, responseError);
        }
    }
}
