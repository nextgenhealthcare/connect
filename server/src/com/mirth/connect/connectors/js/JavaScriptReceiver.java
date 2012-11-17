/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;

public class JavaScriptReceiver extends PollConnector {
    private String scriptId;
    private JavaScriptReceiverProperties connectorProperties;
    private JavaScriptExecutor<Object> jsExecutor = new JavaScriptExecutor<Object>();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (JavaScriptReceiverProperties) getConnectorProperties();

        String scriptId = UUID.randomUUID().toString();

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, connectorProperties.getScript(), null, null);
        } catch (Exception e) {
            throw new DeployException("Error compiling " + connectorProperties.getName() + " script " + scriptId + ".", e);
        }

        this.scriptId = scriptId;
    }

    @Override
    public void onUndeploy() throws UndeployException {
        JavaScriptUtil.removeScriptFromCache(scriptId);
    }

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {}

    @Override
    public void handleRecoveredResponse(DispatchResult messageResponse) {}

    @Override
    public void poll() throws InterruptedException {
        Object result = null;
        
        try {
            result = jsExecutor.execute(new JavaScriptReceiverTask());
        } catch (JavaScriptExecutorException e) {
            logger.error("Error executing " + connectorProperties.getName() + " script " + scriptId + ".", e);
        }
        
        for (RawMessage rawMessage : convertJavaScriptResult(result)) {
            DispatchResult dispatchResult = null;
            
            try {
                try {
                    dispatchResult = dispatchRawMessage(rawMessage);
                } finally {
                    finishDispatch(dispatchResult);
                }
            } catch (Exception e) {
            }
        }
    }
    
    private class JavaScriptReceiverTask extends JavaScriptTask<Object> {
        @Override
        public Object call() throws Exception {
            Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(getContextFactory(), Logger.getLogger("js-connector"), getChannelId());
            return JavaScriptUtil.executeScript(scriptId, scope, getChannelId(), "Source");
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<RawMessage> convertJavaScriptResult(Object result) {
        List<RawMessage> messages = new ArrayList<RawMessage>();

        if (result instanceof NativeJavaObject) {
            Object object = ((NativeJavaObject) result).unwrap();

            if (object instanceof List<?>) {
                // Allow the user to pass in RawMessage objects as well as strings
                for (Object element : (List<Object>) object) {
                    if (element instanceof RawMessage) {
                        messages.add((RawMessage) element);
                    } else {
                        String rawData = element.toString();
                        if (StringUtils.isNotEmpty(rawData)) {
                            messages.add(new RawMessage(rawData));
                        }
                    }
                }
            } else if (object instanceof RawMessage) {
                messages.add((RawMessage) object);
            } else {
                // Assume it's a string
                String rawData = object.toString();
                if (StringUtils.isNotEmpty(rawData)) {
                    messages.add(new RawMessage(rawData));
                }
            }
        } else if (result != null && !(result instanceof Undefined)) {
            // This branch will catch all objects that aren't NativeJavaObject, Undefined, or null
            // Assume it's a string
            String rawData = result.toString();
            if (StringUtils.isNotEmpty(rawData)) {
                messages.add(new RawMessage(rawData));
            }
        }

        return messages;
    }
}
