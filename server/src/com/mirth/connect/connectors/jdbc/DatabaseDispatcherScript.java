/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.server.MirthScopeProvider;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.ErrorMessageBuilder;

public class DatabaseDispatcherScript implements DatabaseDispatcherDelegate {
    private String scriptId;
    private DatabaseDispatcher connector;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ContextFactoryController contextFactoryController = getContextFactoryController();
    private Logger scriptLogger = Logger.getLogger("db-connector");
    private Logger logger = Logger.getLogger(this.getClass());
    private List<String> contextFactoryIdList = new ArrayList<String>();
    private volatile String contextFactoryId;
	private boolean debug = false;
    private MirthMain debugger;
    private boolean ignoreBreakpoints = false;
    private MirthScopeProvider scopeProvider = new MirthScopeProvider();

    public DatabaseDispatcherScript(DatabaseDispatcher connector) {
        this.connector = connector;
    }


    @Override
    public void deploy() throws ConnectorTaskException {
        Channel channel = connector.getChannel();
        DebugOptions debugOptions = channel.getDebugOptions();
        
        DatabaseDispatcherProperties connectorProperties = (DatabaseDispatcherProperties) connector.getConnectorProperties();
        this.debug  = debugOptions != null && debugOptions.isDestinationConnectorScripts();
        scriptId = UUID.randomUUID().toString() + "_Database_Writer";
        try {
            MirthContextFactory contextFactory = null;
            Map<String, MirthContextFactory> contextFactories = new HashMap<>();
            
            if (debug) {
                contextFactory = contextFactoryController.getDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), scriptId);
                contextFactoryIdList.add(contextFactory.getId());
                contextFactory.setContextType(ContextType.DESTINATION_DISPATCHER);
                contextFactory.setScriptText(connectorProperties.getQuery());
                contextFactory.setDebugType(true);
                contextFactories.put(scriptId, contextFactory);
                debugger = getDebugger(channel, contextFactory);
            } else {
                contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());
            }
            contextFactoryId = contextFactory.getId();
            compileAndAddScript(connectorProperties, contextFactory);
        } catch (Exception e) {
            throw new ConnectorTaskException("Error compiling script " + scriptId + ".", e);
        }
    }


    @Override
    public void undeploy() throws ConnectorTaskException {
        removeScriptFromCache();
        
      if (debug && debugger != null) {
          contextFactoryController.removeDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), scriptId);
          debugger.dispose();
          debugger = null;
      }
    }

    @Override
    public void start() throws ConnectorTaskException {
        ignoreBreakpoints = false;
        if (debug && debugger != null) {
            debugger.enableDebugging();
        }
    }

    @Override
    public void stop() throws ConnectorTaskException {
        if (debug && debugger != null) {
            debugger.finishScriptExecution();
        }
    }

    @Override
    public void halt() throws ConnectorTaskException {}

    

    protected void compileAndAddScript(DatabaseDispatcherProperties connectorProperties, MirthContextFactory contextFactory) throws Exception {
        JavaScriptUtil.compileAndAddScript(connector.getChannelId(), contextFactory, scriptId, connectorProperties.getQuery(), ContextType.DESTINATION_DISPATCHER, null, null);
    }


    protected MirthMain getDebugger(Channel channel, MirthContextFactory contextFactory) {
        return JavaScriptUtil.getDebugger(contextFactory, scopeProvider, channel, scriptId);
    }


    public ContextFactoryController getContextFactoryController() {
        return ControllerFactory.getFactory().createContextFactoryController();
    }


    protected void removeScriptFromCache() {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    @Override
    public Response send(DatabaseDispatcherProperties connectorProperties, ConnectorMessage connectorMessage) throws DatabaseDispatcherException, InterruptedException {
        try {
            MirthContextFactory contextFactory = debug ? contextFactoryController.getDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), scriptId) : contextFactoryController.getContextFactory(connector.getResourceIds()); 


            if (!contextFactoryId.equals(contextFactory.getId())) {
                synchronized (this) {
                    contextFactory = debug ? contextFactoryController.getDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), scriptId) : contextFactoryController.getContextFactory(connector.getResourceIds());

                    if (contextFactoryId.equals(contextFactory.getId())) {
                        JavaScriptUtil.recompileGeneratedScript(contextFactory, scriptId);
                        contextFactoryId = contextFactory.getId();
                    }
                }
            }

            return (Response) JavaScriptUtil.execute(new DatabaseDispatcherTask(contextFactory, connectorMessage));
        } catch (Exception e) {
            throw new DatabaseDispatcherException("Error executing script " + scriptId, e);
        }
    }
    

    private class DatabaseDispatcherTask extends JavaScriptTask<Object> {
        private ConnectorMessage connectorMessage;

        public DatabaseDispatcherTask(MirthContextFactory contextFactory, ConnectorMessage connectorMessage) {
            super(contextFactory, connector);
            this.connectorMessage = connectorMessage;
        }

        @Override
        public Object doCall() {
            String responseData = null;
            String responseError = null;
            String responseStatusMessage = "Database write success";
            Status responseStatus = Status.SENT;

            try {
                Scriptable scope = JavaScriptScopeUtil.getMessageDispatcherScope(getContextFactory(), scriptLogger, connector.getChannelId(), new ImmutableConnectorMessage(connectorMessage, true, connector.getDestinationIdMap()));

                if (debug) {
                    scopeProvider.setScope(scope);

                    if (debugger != null && !ignoreBreakpoints) {
                        debugger.doBreak();
                        
                        if (!debugger.isVisible()) {
                            debugger.setVisible(true);
                        }
                    }
                }
                
                Object result = JavaScriptUtil.executeScript(this, scriptId, scope, connector.getChannelId(), connector.getDestinationName());

                if (result != null && !(result instanceof Undefined)) {
                    /*
                     * If the script return value is a response, return it as-is. If it's a status,
                     * only update the response status. Otherwise, set the response data to the
                     * string representation of the object.
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
            } catch (Exception e) {
                ConnectorProperties connectorProperties = connector.getConnectorProperties();
                responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error evaluating " + connectorProperties.getName(), e);
                responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error evaluating " + connector.getConnectorProperties().getName(), e);
                responseStatus = Status.QUEUED;

                logger.error("Error evaluating " + connectorProperties.getName() + " (" + connectorProperties.getName() + " \"" + connector.getDestinationName() + "\" on channel " + connector.getChannelId() + ").", e);
                eventController.dispatchEvent(new ErrorEvent(connector.getChannelId(), connector.getMetaDataId(), null, ErrorEventType.DESTINATION_CONNECTOR, connector.getDestinationName(), connectorProperties.getName(), "Error evaluating " + connectorProperties.getName(), e));
            } finally {
                Context.exit();
            }

            return new Response(responseStatus, responseData, responseStatusMessage, responseError);
        }
    }
}
