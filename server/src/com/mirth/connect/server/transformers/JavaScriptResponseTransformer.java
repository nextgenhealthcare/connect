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
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformerException;
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

public class JavaScriptResponseTransformer implements ResponseTransformer {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private JavaScriptExecutor<String> jsExecutor = new JavaScriptExecutor<String>();

    private String channelId;
    private String connectorName;
    private String scriptId;
    private String template;

    public JavaScriptResponseTransformer(String channelId, String connectorName, String script, String template) throws JavaScriptInitializationException {
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
                logger.debug("compiling response transformer script");
                this.scriptId = UUIDGenerator.getUUID();
                JavaScriptUtil.compileAndAddScript(scriptId, script, null, null);
            }
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 0, "response", null);
            }

            logger.error(ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_300, null, e)); // TODO Add new error code for Response Transformer
            throw new JavaScriptInitializationException("Error initializing JavaScript response transformer", e);
        }
    }

    @Override
    public String doTransform(Response response, ConnectorMessage connectorMessage) throws ResponseTransformerException, InterruptedException {
        try {
            return jsExecutor.execute(new ResponseTransformerTask(response, connectorMessage, channelId, connectorName, scriptId, template));
        } catch (JavaScriptExecutorException e) {
            Throwable cause = e.getCause();

            if (cause instanceof ResponseTransformerException) {
                throw (ResponseTransformerException) cause;
            }

            throw new ResponseTransformerException(e.getMessage(), e);
        }
    }

    @Override
    public void dispose() {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    private class ResponseTransformerTask extends JavaScriptTask<String> {
        private Response response;
        private ConnectorMessage connectorMessage;
        private String channelId;
        private String connectorName;
        private String scriptId;
        private String template;

        public ResponseTransformerTask(Response response, ConnectorMessage connectorMessage, String channelId, String connectorName, String scriptId, String template) {
            this.response = response;
            this.connectorMessage = connectorMessage;
            this.channelId = channelId;
            this.connectorName = connectorName;
            this.scriptId = scriptId;
            this.template = template;
        }

        @Override
        public String call() throws Exception {
            Logger scriptLogger = Logger.getLogger("response");

            try {
                Scriptable scope = JavaScriptScopeUtil.getResponseTransformerScope(scriptLogger, response, connectorMessage, template);

                // Get the script from the cache
                Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

                if (compiledScript == null) {
                    throw new Exception("Could not find script " + scriptId + " in cache.");
                }

                // Execute the script
                executeScript(compiledScript, scope);

                // Set response status and errorMsg
                JavaScriptScopeUtil.getResponseDataFromScope(scope, response);

                // Return the result
                return JavaScriptScopeUtil.getTransformedDataFromScope(scope, StringUtils.isNotBlank(template));

            } catch (Throwable t) {
                if (t instanceof RhinoException) {
                    try {
                        String script = CompiledScriptCache.getInstance().getSourceScript(scriptId);
                        int linenumber = ((RhinoException) t).lineNumber();
                        String errorReport = JavaScriptUtil.getSourceCode(script, linenumber, 0);
                        t = new MirthJavascriptTransformerException((RhinoException) t, channelId, connectorName, 0, "response", errorReport);
                    } catch (Exception ee) {
                        t = new MirthJavascriptTransformerException((RhinoException) t, channelId, connectorName, 0, "response", null);
                    }
                }

                throw new ResponseTransformerException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_600, "Error evaluating response transformer", t));
            } finally {
                Context.exit();
            }
        }
    }
}
