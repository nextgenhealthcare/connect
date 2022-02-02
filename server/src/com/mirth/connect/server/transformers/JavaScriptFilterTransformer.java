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
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.FilterTransformerResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerException;
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

public class JavaScriptFilterTransformer implements FilterTransformer {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    private Connector connector;
    private String connectorName;
    private String template;
    private String scriptId;
    private volatile String contextFactoryId;
    private Boolean debug = false;
    private MirthMain debugger;
    private MirthScopeProvider scopeProvider = new MirthScopeProvider();
    private boolean ignoreBreakpoints = false;

    public JavaScriptFilterTransformer(Connector connector, String connectorName, String script, String template, DebugOptions debugOptions) throws JavaScriptInitializationException {
        this.connector = connector;
        this.connectorName = connectorName;
        this.template = template;
        initialize(script, debugOptions);
    }

    private void initialize(String script, DebugOptions debugOptions) throws JavaScriptInitializationException {

        Channel channel = connector.getChannel();
        ContextType contextType = null;

        scriptId = ScriptController.getScriptId("JavaScript_Filter_Transformer_" + connector.getMetaDataId(), connector.getChannelId());
        MirthContextFactory contextFactory;
        if (connector instanceof SourceConnector) {
            if (debugOptions != null && debugOptions.isSourceFilterTransformer()) {
                this.debug = true;
            }
            contextType = ContextType.SOURCE_FILTER_TRANSFORMER;
        } else if (connector instanceof DestinationConnector) {
            if (debugOptions != null && debugOptions.isDestinationFilterTransformer()) {
                this.debug = true;
            }
            contextType = ContextType.DESTINATION_FILTER_TRANSFORMER;
        }
        try {
            /*
             * Scripts are not compiled if they are blank or do not exist in the database. Note that
             * in Oracle, a blank script is the same as a NULL script.
             */
            if (StringUtils.isNotBlank(script)) {

                logger.debug("compiling filter/transformer scripts");

                if (debug) {
                    contextFactory = getDebugContextFactory();
                    contextFactory.setContextType(contextType);
                    contextFactory.setScriptText(script);
                    contextFactory.setDebugType(true);
                    debugger = getDebugger(channel, contextFactory);

                } else {
                    contextFactory = getContextFactory();

                }

                compileAndAddScript(script, contextFactory);
            }
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, connector.getChannelId(), connectorName, 0, "Filter/Transformer", null);
            }

            logger.error(ErrorMessageBuilder.buildErrorMessage("Filter/Transformer", null, e));
            throw new JavaScriptInitializationException("Error initializing JavaScript Filter/Transformer", e);
        }
    }

    protected MirthContextFactory getDebugContextFactory() throws Exception {
        return contextFactoryController.getDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), scriptId);
    }

    protected void compileAndAddScript(String script, MirthContextFactory contextFactory) throws Exception {
        JavaScriptUtil.compileAndAddScript(connector.getChannelId(), contextFactory, scriptId, script, connector instanceof SourceConnector ? ContextType.SOURCE_FILTER_TRANSFORMER : ContextType.DESTINATION_FILTER_TRANSFORMER, null, null);
    }

    protected MirthContextFactory getContextFactory() throws Exception {
        return contextFactoryController.getContextFactory(connector.getResourceIds());
    }

    @Override
    public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException, InterruptedException {
        try {

            MirthContextFactory contextFactory = debug ? getDebugContextFactory() : getContextFactory();

            if (!contextFactoryId.equals(contextFactory.getId())) {
                synchronized (this) {
                    contextFactory = debug ? getDebugContextFactory() : getContextFactory();

                    if (!contextFactoryId.equals(contextFactory.getId())) {
                        JavaScriptUtil.recompileGeneratedScript(contextFactory, scriptId);
                        contextFactoryId = contextFactory.getId();
                    }
                }
            }

            return JavaScriptUtil.execute(new FilterTransformerTask(contextFactory, message));
        } catch (JavaScriptExecutorException e) {
            Throwable cause = e.getCause();

            if (cause instanceof FilterTransformerException) {
                throw (FilterTransformerException) cause;
            }

            throw new FilterTransformerException(cause.getMessage(), cause, ErrorMessageBuilder.buildErrorMessage("Filter/Transformer", null, cause));
        } catch (Exception e) {
            throw new FilterTransformerException(e.getMessage(), e, ErrorMessageBuilder.buildErrorMessage("Filter/Transformer", null, e));
        }
    }

    @Override
    public void dispose() {
        removeScriptFromCache();

        if (debug && debugger != null) {
            contextFactoryController.removeDebugContextFactory(connector.getResourceIds(), connector.getChannelId(), scriptId);
            debugger.dispose();
            debugger = null;
        }

    }

    protected void removeScriptFromCache() {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    protected MirthMain getDebugger(Channel channel, MirthContextFactory contextFactory) {
        return JavaScriptUtil.getDebugger(contextFactory, scopeProvider, channel, scriptId);
    }

    private class FilterTransformerTask extends JavaScriptTask<FilterTransformerResult> {
        private ConnectorMessage message;

        public FilterTransformerTask(MirthContextFactory contextFactory, ConnectorMessage message) {
            super(contextFactory, (connector instanceof SourceConnector ? "Source" : "Destination") + " Filter/Transformer", connector);
            this.message = message;
        }

        @Override
        public FilterTransformerResult doCall() throws Exception {
            Logger scriptLogger = Logger.getLogger("filter");
            // Use an array to store the phase, otherwise java and javascript end up referencing two different objects.
            String[] phase = { new String() };

            // get the script from the cache and execute it
            Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

            if (compiledScript == null) {
                logger.debug("Could not find script " + scriptId + " in cache.");
                throw new FilterTransformerException("Could not find script " + scriptId + " in cache.", null, ErrorMessageBuilder.buildErrorMessage("Filter/Transformer", "Could not find script " + scriptId + " in cache.", null));
            } else {
                try {
                    // TODO: Get rid of template and phase
                    Scriptable scope = JavaScriptScopeUtil.getFilterTransformerScope(getContextFactory(), scriptLogger, new ImmutableConnectorMessage(message, true, connector.getDestinationIdMap()), template, phase);

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

                    String transformedData = JavaScriptScopeUtil.getTransformedDataFromScope(scope, StringUtils.isNotBlank(template));

                    return new FilterTransformerResult(!(Boolean) Context.jsToJava(result, java.lang.Boolean.class), transformedData);
                } catch (Throwable t) {
                    if (t instanceof RhinoException) {
                        try {
                            String script = CompiledScriptCache.getInstance().getSourceScript(scriptId);
                            int linenumber = ((RhinoException) t).lineNumber();
                            String errorReport = JavaScriptUtil.getSourceCode(script, linenumber, 0);
                            t = new MirthJavascriptTransformerException((RhinoException) t, connector.getChannelId(), connectorName, 0, phase[0].toUpperCase(), errorReport);
                        } catch (Exception ee) {
                            t = new MirthJavascriptTransformerException((RhinoException) t, connector.getChannelId(), connectorName, 0, phase[0].toUpperCase(), null);
                        }
                    }

                    if (phase[0].equals("filter")) {
                        eventController.dispatchEvent(new ErrorEvent(message.getChannelId(), message.getMetaDataId(), message.getMessageId(), ErrorEventType.FILTER, connectorName, null, "Error evaluating filter", t));
                        throw new FilterTransformerException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.FILTER.toString(), "Error evaluating filter", t));
                    } else {
                        eventController.dispatchEvent(new ErrorEvent(message.getChannelId(), message.getMetaDataId(), message.getMessageId(), ErrorEventType.TRANSFORMER, connectorName, null, "Error evaluating transformer", t));
                        throw new FilterTransformerException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.TRANSFORMER.toString(), "Error evaluating transformer", t));
                    }
                } finally {
                    Context.exit();
                }
            }
        }
    }
}
