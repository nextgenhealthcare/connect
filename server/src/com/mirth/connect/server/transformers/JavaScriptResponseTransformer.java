/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
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
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformerException;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.ServerUUIDGenerator;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptResponseTransformer implements ResponseTransformer {
    private Logger logger = Logger.getLogger(this.getClass());
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    private String channelId;
    private String connectorName;
    private String scriptId;
    private String template;
    private Map<String, String> destinationNameMap;

    public JavaScriptResponseTransformer(String channelId, String connectorName, String script, String template, Map<String, String> destinationNameMap) throws JavaScriptInitializationException {
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
                logger.debug("compiling response transformer script");
                this.scriptId = ServerUUIDGenerator.getUUID();
                JavaScriptUtil.compileAndAddScript(scriptId, script, ContextType.MESSAGE_CONTEXT, null, null);
            }
        } catch (Exception e) {
            if (e instanceof RhinoException) {
                e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 0, "response", null);
            }

            logger.error(ErrorMessageBuilder.buildErrorMessage(ErrorEventType.RESPONSE_TRANSFORMER.toString(), null, e)); // TODO Add new error code for Response Transformer
            throw new JavaScriptInitializationException("Error initializing JavaScript response transformer", e);
        }
    }

    @Override
    public String doTransform(Response response, ConnectorMessage connectorMessage) throws ResponseTransformerException, InterruptedException {
        try {
            return JavaScriptUtil.execute(new ResponseTransformerTask(response, connectorMessage, channelId, connectorName, scriptId, template));
        } catch (JavaScriptExecutorException e) {
            Throwable cause = e.getCause();

            if (cause instanceof ResponseTransformerException) {
                throw (ResponseTransformerException) cause;
            }

            throw new ResponseTransformerException(cause.getMessage(), cause, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.RESPONSE_TRANSFORMER.toString(), null, cause));
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

            // Get the script from the cache
            Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

            if (compiledScript == null) {
                logger.debug("Could not find script " + scriptId + " in cache.");
                throw new ResponseTransformerException("Could not find script " + scriptId + " in cache.", null, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.RESPONSE_TRANSFORMER.toString(), "Could not find script " + scriptId + " in cache.", null));
            } else {
                try {
                    com.mirth.connect.userutil.Response userResponse = new com.mirth.connect.userutil.Response(response);
                    Scriptable scope = JavaScriptScopeUtil.getResponseTransformerScope(scriptLogger, userResponse, new ImmutableConnectorMessage(connectorMessage, true, destinationNameMap), template);
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
                            t = new MirthJavascriptTransformerException((RhinoException) t, channelId, connectorName, 0, "response", errorReport);
                        } catch (Exception ee) {
                            t = new MirthJavascriptTransformerException((RhinoException) t, channelId, connectorName, 0, "response", null);
                        }
                    }

                    eventController.dispatchEvent(new ErrorEvent(connectorMessage.getChannelId(), connectorMessage.getMetaDataId(), ErrorEventType.RESPONSE_TRANSFORMER, connectorName, null, "Error evaluating response transformer", t));
                    throw new ResponseTransformerException(t.getMessage(), t, ErrorMessageBuilder.buildErrorMessage(ErrorEventType.RESPONSE_TRANSFORMER.toString(), "Error evaluating response transformer", t));
                } finally {
                    Context.exit();
                }
            }
        }
    }
}