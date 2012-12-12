/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JavaScriptDispatcher extends DestinationConnector {
    private Logger scriptLogger = Logger.getLogger("js-connector");
    private JavaScriptExecutor<Response> jsExecutor = new JavaScriptExecutor<Response>();
    private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
    private JavaScriptDispatcherProperties connectorProperties;
    private String scriptId;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (JavaScriptDispatcherProperties) getConnectorProperties();

        String scriptId = UUID.randomUUID().toString();

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, connectorProperties.getScript(), null, null);
        } catch (Exception e) {
            // TODO: handle exception
        }

        this.scriptId = scriptId;
    }

    @Override
    public void onUndeploy() throws UndeployException {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    @Override
    public void onStart() throws StartException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStop() throws StopException {
        // TODO Auto-generated method stub
    }

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage message) {
        return (ConnectorProperties) SerializationUtils.clone(connectorProperties);
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException {
        try {
            return jsExecutor.execute(new JavaScriptDispatcherTask(message));
        } catch (JavaScriptExecutorException e) {
            // TODO: log error?
            return new Response(Status.ERROR, ErrorMessageBuilder.buildErrorResponse("Error executing script", e), ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_414, "Error executing script", e));
        }
    }
    
    private class JavaScriptDispatcherTask extends JavaScriptTask<Response> {
        private ConnectorMessage message;
        
        public JavaScriptDispatcherTask(ConnectorMessage message) {
            this.message = message;
        }
        
        @Override
        public Response call() throws Exception {
            String responseData = null;
            String responseError = null;
            Status responseStatus = Status.QUEUED;

            Context context = JavaScriptScopeUtil.getContext();
            Scriptable scope = JavaScriptScopeUtil.getMessageDispatcherScope(scriptLogger, getChannelId(), message);

            Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

            if (compiledScript == null) {
                // TODO: throw exception?
                responseData = ErrorMessageBuilder.buildErrorResponse("Script not found in cache", null);
                responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_414, "Script not found in cache", null);
                responseStatus = Status.ERROR;
            } else {
                Object result = compiledScript.exec(context, scope);

                // Set the response message to the returned object (casted to a string)
                if (result != null) {
                    responseData = (String) Context.jsToJava(result, java.lang.String.class);
                }

                // TODO: Decide 1) if queuing is ever appropriate for JavaScript Writers and 2) how to implement it
                // If queuing is enabled, then only update the response status to SENT if the result value isn't null or Undefined
                if (result != null && !(result instanceof Undefined)) {
                    responseStatus = Status.SENT;
                }
            }

            return new Response(responseStatus, responseData, responseError);
        }
    }
}
