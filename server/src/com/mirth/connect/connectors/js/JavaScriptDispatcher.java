/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.MirthScopeProvider;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private Logger scriptLogger = Logger.getLogger("js-connector");
    private EventController eventController = getEventController();
    private ScriptController scriptController = getScriptController();
    private ContextFactoryController contextFactoryController = getContextFactoryController();
    private CompiledScriptCache compiledScriptCache = getCompiledScriptCache();
    private JavaScriptDispatcherProperties connectorProperties;
    private String scriptId;
    List<String> contextFactoryIdList = new ArrayList<String>();
    private MirthScopeProvider scopeProvider = new MirthScopeProvider();
    private boolean debug = false;
    private MirthMain debugger;
    private boolean ignoreBreakpoints = false;
    
    protected EventController getEventController() {
        return ControllerFactory.getFactory().createEventController();
    }
    
    protected ScriptController getScriptController() {
        return ControllerFactory.getFactory().createScriptController();
    }
    
    
    protected ChannelController getChannelController() {
        return ControllerFactory.getFactory().createChannelController();
    }
    
    protected ContextFactoryController getContextFactoryController() {
        return ControllerFactory.getFactory().createContextFactoryController();
    }
    
    protected CompiledScriptCache getCompiledScriptCache() {
        return CompiledScriptCache.getInstance();
    }

    @Override
    public void onDeploy() throws ConnectorTaskException {
        onDeploy(null);
    }
    
    @Override
    public void onDebugDeploy(DebugOptions debugOptions) throws ConnectorTaskException {
        onDeploy(debugOptions);
    }
    
    public void onDeploy(DebugOptions debugOptions) throws ConnectorTaskException {
        this.connectorProperties = (JavaScriptDispatcherProperties) getConnectorProperties();
        this.debug = debugOptions != null && debugOptions.isDestinationConnectorScripts();
        com.mirth.connect.model.Channel channelModel = new com.mirth.connect.model.Channel();
        channelModel = getChannelController().getChannelById(getChannelId());
        scriptId = ScriptController.getScriptId("JavaScript_Writer", getChannelId());

        try {
            MirthContextFactory contextFactory = null;
            Map<String, MirthContextFactory> contextFactories = new HashMap<>();
            
            if (debug) {
                contextFactory = contextFactoryController.getDebugContextFactory(getResourceIds(), getChannelId(), scriptId);
                contextFactoryIdList.add(contextFactory.getId());
                contextFactory.setContextType(ContextType.DESTINATION_DISPATCHER);
                contextFactory.setScriptText(connectorProperties.getScript());
                contextFactory.setDebugType(true);
                contextFactories.put(scriptId, contextFactory);
                debugger = getDebugger(contextFactory);
            } else {
                //default case
                contextFactory = contextFactoryController.getContextFactory(getResourceIds());
                contextFactory.setContextType(ContextType.DESTINATION_DISPATCHER);
                contextFactoryIdList.add(contextFactory.getId());
                contextFactory.setScriptText(connectorProperties.getScript());
                contextFactories.put(scriptId, contextFactory);
            }
           
            scriptController.compileChannelScripts(contextFactories, channelModel);
            
        } catch (Exception e) {
            throw new ConnectorTaskException("Error compiling/adding script.", e);
        }

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }
    

    protected MirthMain getDebugger(MirthContextFactory contextFactory) {
        return MirthMain.mirthMainEmbedded(contextFactory, scopeProvider, getChannel().getName() + "-" + getChannelId(), scriptId);
    }
    
    protected void compileAndAddScript(MirthContextFactory contextFactory, String scriptId) throws Exception {
        JavaScriptUtil.compileAndAddScript(getChannelId(), contextFactory, scriptId, connectorProperties.getScript(), ContextType.DESTINATION_DISPATCHER, null, null);
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {
        if(scriptId != null) {
            JavaScriptUtil.removeScriptFromCache(scriptId);
              
            if (debug && debugger != null) {
                contextFactoryController.removeDebugContextFactory(getResourceIds(), getChannelId(), scriptId);
                debugger.dispose();
                debugger = null;
            }
        } 
    }

    @Override
    public void onStart() throws ConnectorTaskException {
        ignoreBreakpoints = false;
        if (debug && debugger != null) {
            debugger.enableDebugging();
        }
    }

    @Override
    public void onStop() throws ConnectorTaskException {
        if (debug && debugger != null) {
            debugger.finishScriptExecution();
        }
    }

    @Override
    public void onHalt() throws ConnectorTaskException {}

    @Override
    public void stopDebugging() throws ConnectorTaskException {
        ignoreBreakpoints = true;
        if (debug && debugger != null) {
            debugger.finishScriptExecution();
        }
    }
    
    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message) {}

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException {       
        JavaScriptDispatcherProperties javaScriptDispatcherProperties = (JavaScriptDispatcherProperties) connectorProperties;

        try {
            MirthContextFactory contextFactory = debug ? contextFactoryController.getDebugContextFactory(getResourceIds(), getChannelId(), scriptId) : contextFactoryController.getContextFactory(getResourceIds()); 

            if (!contextFactoryIdList.contains(contextFactory.getId())) {
                synchronized (this) {
                    contextFactory = debug ? contextFactoryController.getDebugContextFactory(getResourceIds(), getChannelId(), scriptId) : contextFactoryController.getContextFactory(getResourceIds());

                    if (!contextFactoryIdList.contains(contextFactory.getId())) {
                        JavaScriptUtil.recompileGeneratedScript(contextFactory, scriptId);
                        contextFactoryIdList.add(contextFactory.getId());
                    }
                }
            }

            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.SENDING));

            Response response = JavaScriptUtil.execute(new JavaScriptDispatcherTask(contextFactory, message));
            response.setValidate(javaScriptDispatcherProperties.getDestinationConnectorProperties().isValidateResponse());

            return response;
        } catch (Exception e) {
            logger.error("Error executing script (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), message.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error executing script", e));
            return new Response(Status.ERROR, null, ErrorMessageBuilder.buildErrorResponse("Error executing script", e), ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error executing script", e));
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
        }
    }

    private class JavaScriptDispatcherTask extends JavaScriptTask<Response> {
        private ConnectorMessage message;

        public JavaScriptDispatcherTask(MirthContextFactory contextFactory, ConnectorMessage message) {
            super(contextFactory, JavaScriptDispatcher.this);
            this.message = message;
        }

        @Override
        public Response doCall() throws Exception {
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
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), message.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Script not found in cache", null));
            } else {
                try {
                    Scriptable scope = JavaScriptScopeUtil.getMessageDispatcherScope(getContextFactory(), scriptLogger, getChannelId(), new ImmutableConnectorMessage(message, true, JavaScriptDispatcher.this.getDestinationIdMap()));
                    
                    if (debug) {
                        scopeProvider.setScope(scope);

                        if (debugger != null && !ignoreBreakpoints) {
                            debugger.doBreak();
                            
                            if (!debugger.isVisible()) {
                                debugger.setVisible(true);
                            }
                        }
                    }
                    
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
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), message.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error evaluating " + getConnectorProperties().getName(), t));
                } finally {
                    Context.exit();
                }
            }

            return new Response(responseStatus, responseData, responseStatusMessage, responseError);
        }
    }
}