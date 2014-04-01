/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.js;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;

public class JavaScriptReceiver extends PollConnector {
    private String scriptId;
    private JavaScriptReceiverProperties connectorProperties;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
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
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
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
    public void onHalt() throws HaltException {}

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult);
    }

    @Override
    public void poll() throws InterruptedException {
        Object result = null;
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.READING));

        try {
            result = JavaScriptUtil.execute(new JavaScriptReceiverTask());
        } catch (JavaScriptExecutorException e) {
            logger.error("Error executing " + connectorProperties.getName() + " script " + scriptId + ".", e);
        }

        for (RawMessage rawMessage : convertJavaScriptResult(result)) {
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = dispatchRawMessage(rawMessage);
            } catch (ChannelException e) {
                // Do nothing. An error should have been logged.
            } finally {
                finishDispatch(dispatchResult);
            }
        }

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
    }

    private class JavaScriptReceiverTask extends JavaScriptTask<Object> {
        @Override
        public Object call() throws Exception {
            try {
                Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(Logger.getLogger("js-connector"), getChannelId());
                return JavaScriptUtil.executeScript(this, scriptId, scope, getChannelId(), "Source");
            } finally {
                Context.exit();
            }
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
                    if (element instanceof com.mirth.connect.server.userutil.RawMessage) {
                        messages.add(convertRawMessage(element));
                    } else {
                        String rawData = element.toString();
                        if (StringUtils.isNotEmpty(rawData)) {
                            messages.add(new RawMessage(rawData));
                        }
                    }
                }
            } else if (object instanceof com.mirth.connect.server.userutil.RawMessage) {
                messages.add(convertRawMessage(object));
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

    private RawMessage convertRawMessage(Object object) {
        com.mirth.connect.server.userutil.RawMessage rawMessage = (com.mirth.connect.server.userutil.RawMessage) object;
        if (rawMessage.isBinary()) {
            return new RawMessage(rawMessage.getRawBytes(), rawMessage.getDestinationMetaDataIds(), rawMessage.getSourceMap());
        } else {
            return new RawMessage(rawMessage.getRawData(), rawMessage.getDestinationMetaDataIds(), rawMessage.getSourceMap());
        }
    }
}
