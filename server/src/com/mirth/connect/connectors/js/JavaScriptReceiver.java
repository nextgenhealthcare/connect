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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.JavaScriptScopeUtil;
import com.mirth.connect.server.util.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.JavaScriptExecutor;
import com.mirth.connect.server.util.javascript.JavaScriptExecutorException;
import com.mirth.connect.server.util.javascript.JavaScriptTask;

public class JavaScriptReceiver extends PollConnector {
    private final static ConnectorType CONNECTOR_TYPE = ConnectorType.READER;

    private String scriptId;
    private JavaScriptReceiverProperties connectorProperties;
    private JavaScriptExecutor<Object> jsExecutor = new JavaScriptExecutor<Object>();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (JavaScriptReceiverProperties) getConnectorProperties();

        String scriptId = UUID.randomUUID().toString();
        Set<String> scriptOptions = new HashSet<String>();
        scriptOptions.add("importUtilPackage");

        try {
            JavaScriptUtil.compileAndAddScript(scriptId, connectorProperties.getScript(), scriptOptions, null);
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
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult);
    }

    @Override
    public void poll() throws InterruptedException {
        Object result = null;
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.BUSY);

        try {
            result = jsExecutor.execute(new JavaScriptReceiverTask());
        } catch (JavaScriptExecutorException e) {
            logger.error("Error executing " + connectorProperties.getName() + " script " + scriptId + ".", e);
        }

        for (RawMessage rawMessage : convertJavaScriptResult(result)) {
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = dispatchRawMessage(rawMessage);
            } catch (ChannelException e) {
                if (e.isStopped()) {
                    logger.error("Channel ID " + getChannelId() + " failed to dispatch message from " + connectorProperties.getName() + ". The channel is stopped.");
                } else {
                    logger.error("Channel ID " + getChannelId() + " failed to dispatch message from " + connectorProperties.getName() + ". Cause: " + e.getMessage());
                }
            } finally {
                finishDispatch(dispatchResult);
            }
        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.DONE);
    }

    private class JavaScriptReceiverTask extends JavaScriptTask<Object> {
        @Override
        public Object call() throws Exception {
            Scriptable scope = JavaScriptScopeUtil.getMessageReceiverScope(Logger.getLogger("js-connector"), getChannelId());
            return JavaScriptUtil.executeScript(this, scriptId, scope, getChannelId(), "Source");
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
