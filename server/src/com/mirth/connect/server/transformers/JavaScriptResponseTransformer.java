/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.transformers;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.channel.DebugOptions;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformerException;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.MirthScopeProvider;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptResponseTransformer implements ResponseTransformer {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    private Connector connector;
    private String connectorName;
    private String scriptId;
    private String template;
    private String script;
    private boolean debug = false;
    private MirthMain debugger;
    private MirthScopeProvider scopeProvider = new MirthScopeProvider();
    private DebugOptions debugOptions;
    private volatile String contextFactoryId;

    public JavaScriptResponseTransformer(Connector connector, String connectorName, String script, String template, DebugOptions debugOptions) throws JavaScriptInitializationException {
        this.connector = connector;
        this.connectorName = connectorName;
        this.template = template;
        this.debugOptions = debugOptions;
        this.script = script;
        initialize(script);
    }

    private void initialize(String script) throws JavaScriptInitializationException {
        try {
            /*
             * Scripts are not compiled if they are blank or do not exist in the database. Note that
             * in Oracle, a blank script is the same as a NULL script.
             */


            MirthContextFactory contextFactory;

            if (StringUtils.isNotBlank(script)) {
                scriptId = ScriptController.getScriptId(ScriptController.RESPONSE_TRANSFORMER + "_" + connector.getMetaDataId(), connector.getChannel().getChannelId());
                contextFactory = getContextFactory();
                contextFactory.setContextType(ContextType.DESTINATION_RESPONSE_TRANSFORMER);
                contextFactory.setScriptText(script);
                if ((debugOptions != null) && this.debugOptions.isDestinationResponseTransformer()) {
                    debug = true;
                    contextFactory.setDebugType(true);
                    debugger = getDebugger(contextFactory);
                }

                contextFactoryId = contextFactory.getId();

                compileAndAddScript(contextFactory);
            }

        } catch (Exception e) {
            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, connector.getChannelId(), connectorName, 0, "response", null);
            }

            logger.error(ErrorMessageBuilder.buildErrorMessage(ErrorEventType.RESPONSE_TRANSFORMER.toString(), null, e)); // TODO Add new error code for Response Transformer
            throw new JavaScriptInitializationException("Error initializing JavaScript response transformer", e);
        }
    }

    @Override
    public String doTransform(Response response, ConnectorMessage connectorMessage) throws ResponseTransformerException, InterruptedException {
        try {

            MirthContextFactory contextFactory = getContextFactory();
            if (!contextFactoryId.equals(contextFactory.getId())) {
                synchronized (this) {
                    contextFactory = getContextFactory();

                    if (!contextFactoryId.equals(contextFactory.getId())) {
                        JavaScriptUtil.recompileGeneratedScript(contextFactory, scriptId);
                        contextFactoryId = contextFactory.getId();
                    }
                }
            }

            return execute(contextFactory, response, connectorMessage);
        } catch (JavaScriptExecutorException e) {
            Throwable cause = e.getCause();

            if (cause instanceof ResponseTransformerException) {
                throw (ResponseTransformerException) cause;
            }

            throw new ResponseTransformerException(cause.getMessage(), cause, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.RESPONSE_TRANSFORMER.toString(), null, cause));
        } catch (Exception e) {
            throw new ResponseTransformerException(e.getMessage(), e, ErrorMessageBuilder.buildErrorMessage("Filter/Transformer", null, e));
        }
    }
    
    protected MirthMain getDebugger(MirthContextFactory contextFactory) {
        return JavaScriptUtil.getDebugger(contextFactory, scopeProvider, connector.getChannel(), scriptId);
        
    }

    protected String execute(MirthContextFactory contextFactory, Response response, ConnectorMessage connectorMessage) throws JavaScriptExecutorException, InterruptedException {
        return JavaScriptUtil.execute(new ResponseTransformerTask(contextFactory, response, connectorMessage, scriptId, template, debugOptions));
    }

    protected void compileAndAddScript(MirthContextFactory contextFactory) throws Exception {
        JavaScriptUtil.compileAndAddScript(connector.getChannelId(), contextFactory, scriptId, script, ContextType.DESTINATION_RESPONSE_TRANSFORMER, null, null);
    }

    protected MirthContextFactory getContextFactory() throws Exception {
        if ((debugOptions!=null) && (debugOptions.isDestinationResponseTransformer())) {
            return contextFactoryController.getDebugContextFactory(connector.getChannel().getResourceIds(), connector.getChannel().getChannelId(), scriptId);
        } else {
            return contextFactoryController.getContextFactory(connector.getChannel().getResourceIds());
        }
    }

    @Override
    public void dispose() {
        JavaScriptUtil.removeScriptFromCache(scriptId);
        if (debug && debugger != null) {
            contextFactoryController.removeDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), scriptId);
            debugger.dispose();
            debugger = null;
        }

    }

    private class ResponseTransformerTask extends JavaScriptTask<String> {
        private Response response;
        private ConnectorMessage connectorMessage;
        private String scriptId;
        private String template;
        private DebugOptions debugOptions;

        public ResponseTransformerTask(MirthContextFactory contextFactory, Response response, ConnectorMessage connectorMessage, String scriptId, String template, DebugOptions debugOptions) {
            super(contextFactory, "Response Transformer", connector);
            this.response = response;
            this.connectorMessage = connectorMessage;
            this.scriptId = scriptId;
            this.template = template;
            this.debugOptions = debugOptions;
        }

        @Override
        public String doCall() throws Exception {
            Logger scriptLogger = Logger.getLogger("response");

            // Get the script from the cache
            Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

            if (compiledScript == null) {
                logger.debug("Could not find script " + scriptId + " in cache.");
                throw new ResponseTransformerException("Could not find script " + scriptId + " in cache.", null, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.RESPONSE_TRANSFORMER.toString(), "Could not find script " + scriptId + " in cache.", null));
            } else {
                try {
                    com.mirth.connect.userutil.Response userResponse = new com.mirth.connect.userutil.Response(response);
                    Scriptable scope = JavaScriptScopeUtil.getResponseTransformerScope(getContextFactory(), scriptLogger, userResponse, new ImmutableConnectorMessage(connectorMessage, true, connector.getDestinationIdMap()), template);

                    if (debugOptions != null) {

                        if (debugOptions.isDestinationResponseTransformer()) {

                            scopeProvider.setScope(scope);

                            if (debugger != null) {
                                debugger.enableDebugging();
                                debugger.doBreak();

                                if (!debugger.isVisible()) {
                                    debugger.setVisible(true);
                                }
                            }
                        }
                    }

                    // Execute the script
                    executeScript(compiledScript, scope);

                    // Set response status and errorMsg
                    JavaScriptScopeUtil.getResponseDataFromScope(scope, userResponse);

                    // Return the result
                    return JavaScriptScopeUtil.getTransformedDataFromScope(scope, StringUtils.isNotBlank(template));
                } catch (Throwable t) {
                    if (t instanceof RhinoException) {
                        try {
                            String script = CompiledScriptCache.getInstance().getSourceScript(scriptId);
                            int linenumber = ((RhinoException) t).lineNumber();
                            String errorReport = JavaScriptUtil.getSourceCode(script, linenumber, 0);
                            t = new MirthJavascriptTransformerException((RhinoException) t, connector.getChannelId(), connectorName, 0, "response", errorReport);
                        } catch (Exception ee) {
                            t = new MirthJavascriptTransformerException((RhinoException) t, connector.getChannelId(), connectorName, 0, "response", null);
                        }
                    }

                    eventController.dispatchEvent(new ErrorEvent(connectorMessage.getChannelId(), connectorMessage.getMetaDataId(), connectorMessage.getMessageId(), ErrorEventType.RESPONSE_TRANSFORMER, connectorName, null, "Error evaluating response transformer", t));
                    throw new ResponseTransformerException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.RESPONSE_TRANSFORMER.toString(), "Error evaluating response transformer", t));
                } finally {
                    Context.exit();
                }
            }
        }

    }
}