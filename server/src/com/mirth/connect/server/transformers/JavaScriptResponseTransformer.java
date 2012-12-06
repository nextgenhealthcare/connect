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

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.builders.ErrorMessageBuilder;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;

public class JavaScriptResponseTransformer implements ResponseTransformer {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private JavaScriptExecutor<Void> jsExecutor = new JavaScriptExecutor<Void>();

    private String channelId;
    private String connectorName;
    private String scriptId;

    public JavaScriptResponseTransformer(String channelId, String connectorName, String scriptId) throws JavaScriptInitializationException {
        this.channelId = channelId;
        this.connectorName = connectorName;
        this.scriptId = scriptId;

        initialize();
    }

    private void initialize() throws JavaScriptInitializationException {
        try {
            /*
             * Scripts are not compiled if they are blank or do not exist in the
             * database. Note that in Oracle, a blank script is the same as a
             * NULL script.
             */
            String script = scriptController.getScript(channelId, scriptId);

            if (StringUtils.isNotBlank(script)) {
                logger.debug("compiling response transformer script");
                JavaScriptUtil.compileAndAddScript(scriptId, script, null, null);
            }
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 0, "response", null);
            }

            logger.error(ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_300, null, e));
            throw new JavaScriptInitializationException("Error initializing JavaScript response transformer", e);
        }
    }

    @Override
    public void doTransform(Response response) throws DonkeyException, InterruptedException {
        try {
            jsExecutor.execute(new ResponseTransformerTask(response, channelId, connectorName, scriptId));
        } catch (JavaScriptExecutorException e) {
            throw new DonkeyException(e);
        }
    }

    @Override
    public void dispose() {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    private class ResponseTransformerTask extends JavaScriptTask<Void> {
        private Response response;
        private String channelId;
        private String connectorName;
        private String scriptId;
        
        public ResponseTransformerTask(Response response, String channelId, String connectorName, String scriptId) {
            this.response = response;
            this.channelId = channelId;
            this.connectorName = connectorName;
            this.scriptId = scriptId;
        }
        
        @Override
        public Void call() throws Exception {
            Logger scriptLogger = Logger.getLogger("response");

            try {
                Context context = JavaScriptScopeUtil.getContext();
                Scriptable scope = JavaScriptScopeUtil.getResponseTransformerScope(getContextFactory(), scriptLogger, response);

                // Get the script from the cache
                Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

                if (compiledScript == null) {
                    throw new Exception("Could not find script " + scriptId + " in cache.");
                }

                // Execute the script
                compiledScript.exec(context, scope);
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

                throw new DonkeyException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_600, "Error evaluating response transformer", t));
            } finally {
                Context.exit();
            }
            
            return null;
        }
    }
}
