/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.transformers;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerException;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.UUIDGenerator;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptFilterTransformer implements FilterTransformer {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private JavaScriptExecutor<FilterTransformerResult> jsExecutor = new JavaScriptExecutor<FilterTransformerResult>();

    private String channelId;
    private String connectorName;
    private String scriptId;
    private String template;

    public JavaScriptFilterTransformer(String channelId, String connectorName, String script, String template) throws JavaScriptInitializationException {
        this.channelId = channelId;
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
    public boolean doFilterTransform(ConnectorMessage message) throws FilterTransformerException, InterruptedException {
        try {
            FilterTransformerResult result = jsExecutor.execute(new FilterTransformerTask(message));
            String transformedContent = result.getTransformedContent();

            if (transformedContent != null) {
                message.getTransformed().setContent(transformedContent);
            }

            return result.isFiltered();
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
                Scriptable scope = JavaScriptScopeUtil.getFilterTransformerScope(scriptLogger, message, template, phase);

                // get the script from the cache and execute it
                Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

                if (compiledScript == null) {
                    logger.debug("script could not be found in cache");
                    return new FilterTransformerResult(true, null);
                } else {
                    Object result = executeScript(compiledScript, scope);

                    String transformedData = JavaScriptScopeUtil.getTransformedDataFromScope(scope, StringUtils.isNotBlank(template));

                    if (StringUtils.isBlank(transformedData)) {
                        transformedData = null;
                    }

                    return new FilterTransformerResult((Boolean) Context.jsToJava(result, java.lang.Boolean.class), transformedData);
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

                //TODO change Error code to something that's both filter and transformer
                throw new FilterTransformerException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_300, "Error evaluating filter/transformer", t));
            } finally {
                Context.exit();
            }
        }
    }

    private class FilterTransformerResult {
        private boolean filtered;
        private String transformedContent;

        public FilterTransformerResult(boolean filtered, String transformedContent) {
            this.filtered = filtered;
            this.transformedContent = transformedContent;
        }

        public boolean isFiltered() {
            return filtered;
        }

        public String getTransformedContent() {
            return transformedContent;
        }
    }
}
