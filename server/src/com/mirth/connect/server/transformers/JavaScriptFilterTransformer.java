/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.transformers;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ImmutableConnectorMessage;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.FilterTransformerResult;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerException;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.UUIDGenerator;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptFilterTransformer implements FilterTransformer {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private EventDispatcher eventDispatcher = Donkey.getInstance().getEventDispatcher();

    private String channelId;
    private String connectorName;
    private String scriptId;
    private String template;
    private Map<String, String> destinationNameMap;

    public JavaScriptFilterTransformer(String channelId, String connectorName, String script, String template, Map<String, String> destinationNameMap) throws JavaScriptInitializationException {
        this.channelId = channelId;
        this.connectorName = connectorName;
        this.template = template;
        this.destinationNameMap = destinationNameMap;
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
                this.scriptId = UUIDGenerator.getUUID();
                JavaScriptUtil.compileAndAddScript(scriptId, script, null, null);
            }
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 0, "Filter/Transformer", null);
            }

            logger.error(ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_300, null, e));
            throw new JavaScriptInitializationException("Error initializing JavaScript Filter/Transformer", e);
        }
    }

    @Override
    public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException, InterruptedException {
        try {
            return JavaScriptUtil.execute(new FilterTransformerTask(message));
        } catch (JavaScriptExecutorException e) {
            Throwable cause = e.getCause();

            if (cause instanceof FilterTransformerException) {
                throw (FilterTransformerException) cause;
            }

            throw new FilterTransformerException(e.getMessage(), e);
        }
    }

    @Override
    public void dispose() {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    private class FilterTransformerTask extends JavaScriptTask<FilterTransformerResult> {
        private ConnectorMessage message;

        public FilterTransformerTask(ConnectorMessage message) {
            this.message = message;
        }

        @Override
        public FilterTransformerResult call() throws Exception {
            Logger scriptLogger = Logger.getLogger("filter");
            String phase = new String();

            try {
                // TODO: Get rid of template and phase
                Scriptable scope = JavaScriptScopeUtil.getFilterTransformerScope(scriptLogger, new ImmutableConnectorMessage(message, true, destinationNameMap), template, phase);

                // get the script from the cache and execute it
                Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

                if (compiledScript == null) {
                    logger.debug("script could not be found in cache");
                    return new FilterTransformerResult(false, null);
                } else {
                    Object result = executeScript(compiledScript, scope);

                    String transformedData = JavaScriptScopeUtil.getTransformedDataFromScope(scope, StringUtils.isNotBlank(template));

                    return new FilterTransformerResult(!(Boolean) Context.jsToJava(result, java.lang.Boolean.class), transformedData);
                }
            } catch (Throwable t) {
                if (t instanceof RhinoException) {
                    try {
                        String script = CompiledScriptCache.getInstance().getSourceScript(scriptId);
                        int linenumber = ((RhinoException) t).lineNumber();
                        String errorReport = JavaScriptUtil.getSourceCode(script, linenumber, 0);
                        t = new MirthJavascriptTransformerException((RhinoException) t, channelId, connectorName, 0, phase.toUpperCase(), errorReport);
                    } catch (Exception ee) {
                        t = new MirthJavascriptTransformerException((RhinoException) t, channelId, connectorName, 0, phase.toUpperCase(), null);
                    }
                }

                if (phase.equals("filter")) {
                    eventDispatcher.dispatchEvent(new ErrorEvent(message.getChannelId(), message.getMetaDataId(), ErrorEventType.FILTER, connectorName, "Error evaluating filter", t));
                    throw new FilterTransformerException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_200, "Error evaluating filter", t));
                } else {
                    eventDispatcher.dispatchEvent(new ErrorEvent(message.getChannelId(), message.getMetaDataId(), ErrorEventType.TRANSFORMER, connectorName, "Error evaluating transformer", t));
                    throw new FilterTransformerException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_300, "Error evaluating transformer", t));
                }
            } finally {
                Context.exit();
            }
        }
    }
}
