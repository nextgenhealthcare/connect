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

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.channel.FilterTransformerResult;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerException;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.ServerUUIDGenerator;
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

    public JavaScriptFilterTransformer(Connector connector, String connectorName, String script, String template) throws JavaScriptInitializationException {
        this.connector = connector;
        this.connectorName = connectorName;
        this.template = template;
        initialize(script);
    }

    private void initialize(String script) throws JavaScriptInitializationException {
        try {
            /*
             * Scripts are not compiled if they are blank or do not exist in the database. Note that
             * in Oracle, a blank script is the same as a NULL script.
             */
            if (StringUtils.isNotBlank(script)) {
                logger.debug("compiling filter/transformer scripts");
                scriptId = ServerUUIDGenerator.getUUID();
                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());
                contextFactoryId = contextFactory.getId();
                JavaScriptUtil.compileAndAddScript(contextFactory, scriptId, script, ContextType.MESSAGE_CONTEXT, null, null);
            }
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, connector.getChannelId(), connectorName, 0, "Filter/Transformer", null);
            }

            logger.error(ErrorMessageBuilder.buildErrorMessage("Filter/Transformer", null, e));
            throw new JavaScriptInitializationException("Error initializing JavaScript Filter/Transformer", e);
        }
    }

    @Override
    public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException, InterruptedException {
        try {
            MirthContextFactory contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());

            if (!contextFactoryId.equals(contextFactory.getId())) {
                synchronized (this) {
                    contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());

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
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    private class FilterTransformerTask extends JavaScriptTask<FilterTransformerResult> {
        private ConnectorMessage message;

        public FilterTransformerTask(MirthContextFactory contextFactory, ConnectorMessage message) {
            super(contextFactory);
            this.message = message;
        }

        @Override
        public FilterTransformerResult call() throws Exception {
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
