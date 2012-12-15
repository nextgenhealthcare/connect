/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptDispatcher extends DestinationConnector {
    private final static ConnectorType CONNECTOR_TYPE = ConnectorType.SENDER;

    private Logger logger = Logger.getLogger(this.getClass());
    private Logger scriptLogger = Logger.getLogger("js-connector");
    private JavaScriptExecutor<Response> jsExecutor = new JavaScriptExecutor<Response>();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private JavaScriptDispatcherProperties connectorProperties;
    private String scriptId;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (JavaScriptDispatcherProperties) getConnectorProperties();

        String scriptId = UUID.randomUUID().toString();

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, connectorProperties.getScript(), null, null);
        } catch (Exception e) {
            throw new DeployException("Error compiling/adding script.", e);
        }

        this.scriptId = scriptId;
    }

    @Override
    public void onUndeploy() throws UndeployException {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {}

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage message) {
        return (ConnectorProperties) SerializationUtils.clone(connectorProperties);
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException {
        try {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.BUSY);
            return jsExecutor.execute(new JavaScriptDispatcherTask(message));
        } catch (JavaScriptExecutorException e) {
            logger.error("Error executing script (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_414, "Error executing script.", e);
            return new Response(Status.ERROR, ErrorMessageBuilder.buildErrorResponse("Error executing script", e), ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_414, "Error executing script", e));
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.DONE);
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
            Status responseStatus = Status.SENT;

            Scriptable scope = JavaScriptScopeUtil.getMessageDispatcherScope(scriptLogger, getChannelId(), message);
            Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

            if (compiledScript == null) {
                responseData = ErrorMessageBuilder.buildErrorResponse("Script not found in cache", null);
                responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_414, "Script not found in cache", null);
                responseStatus = Status.ERROR;

                logger.error("Script not found in cache (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").");
                alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_414, "Script not found in cache.", null);
            } else {
                try {
                    Object result = executeScript(compiledScript, scope);

                    if (result != null) {
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
                } catch (Throwable t) {
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

                    responseData = ErrorMessageBuilder.buildErrorResponse("Error evaluating " + getConnectorProperties().getName(), t);
                    responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_414, "Error evaluating " + getConnectorProperties().getName(), t);
                    responseStatus = Status.ERROR;

                    logger.error("Error evaluating " + getConnectorProperties().getName() + " (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", t);
                    alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_414, "Error evaluating " + getConnectorProperties().getName(), t);
                } finally {
                    Context.exit();
                }
            }

            return new Response(responseStatus, responseData, responseError);
        }
    }
}
