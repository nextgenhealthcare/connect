/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.VMRouter;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class VmDispatcher extends DestinationConnector {
    private VmDispatcherProperties connectorProperties;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ExecutorService executor;
    private int timeout;
    private static transient Log logger = LogFactory.getLog(VMRouter.class);

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (VmDispatcherProperties) getConnectorProperties();
        timeout = NumberUtils.toInt(connectorProperties.getResponseTimeout(), 0);
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {
        if (timeout > 0) {
            executor = Executors.newSingleThreadExecutor();
        }
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
    }

    @Override
    public void onStop() throws StopException {
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.DISCONNECTED));
    }

    @Override
    public void onHalt() throws HaltException {
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.DISCONNECTED));
    }

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        VmDispatcherProperties vmDispatcherProperties = (VmDispatcherProperties) connectorProperties;

        vmDispatcherProperties.setChannelId(replacer.replaceValues(vmDispatcherProperties.getChannelId(), connectorMessage));
        vmDispatcherProperties.setChannelTemplate(replacer.replaceValues(vmDispatcherProperties.getChannelTemplate(), connectorMessage));
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
        VmDispatcherProperties vmDispatcherProperties = (VmDispatcherProperties) connectorProperties;

        String channelId = vmDispatcherProperties.getChannelId();

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.SENDING, "Target Channel: " + channelId));

        String responseData = null;
        String responseError = null;
        String responseStatusMessage = null;
        Status responseStatus = Status.QUEUED; // Always set the status to QUEUED

        try {
            if (!channelId.equals("none")) {
                boolean isBinary = ExtensionController.getInstance().getDataTypePlugins().get(this.getOutboundDataType().getType()).isBinary();
                byte[] data = AttachmentUtil.reAttachMessage(vmDispatcherProperties.getChannelTemplate(), message, Constants.ATTACHMENT_CHARSET, isBinary);

                RawMessage rawMessage;

                if (isBinary) {
                    rawMessage = new RawMessage(data, null, null);
                } else {
                    rawMessage = new RawMessage(StringUtils.newString(data, Constants.ATTACHMENT_CHARSET), null, null);
                }

                rawMessage.getChannelMap().put("sourceChannelId", getChannelId());
                rawMessage.getChannelMap().put("sourceMessageId", message.getMessageId());

                // Remove the reference to the raw message so its doesn't hold the entire message in memory.
                data = null;

                DispatchResult dispatchResult = null;

                if (timeout > 0) {
                    dispatchResult = executor.submit(new DispatchTask(channelId, rawMessage)).get(timeout, TimeUnit.MILLISECONDS);
                } else {
                    dispatchResult = ControllerFactory.getFactory().createEngineController().dispatchRawMessage(channelId, rawMessage);
                }

                if (dispatchResult.getSelectedResponse() != null) {
                    // If a response was returned from the channel then use that message
                    responseData = dispatchResult.getSelectedResponse().getMessage();
                }
            }

            responseStatus = Status.SENT;
            responseStatusMessage = "Message routed successfully to channel id: " + channelId;
        } catch (Throwable e) {
            Throwable cause;
            if (e instanceof ExecutionException) {
                cause = e.getCause();
            } else {
                cause = e;
            }

            String shortMessage = "Error routing message to channel id: " + channelId;
            String longMessage = shortMessage;

            if (e instanceof TimeoutException) {
                longMessage += ". A cycle may be present where a channel is attempting to dispatch a message to itself. If this is the case, enable queuing on the source or destination connectors.";
            }

            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, connectorProperties.getName(), longMessage, cause));
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse(shortMessage, cause);
            responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_412, longMessage, cause);
        } finally {
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError);
    }

    private class DispatchTask implements Callable<DispatchResult> {

        private String channelId;
        private RawMessage rawMessage;

        public DispatchTask(String channelId, RawMessage rawMessage) {
            this.channelId = channelId;
            this.rawMessage = rawMessage;
        }

        @Override
        public DispatchResult call() throws ChannelException {
            return ControllerFactory.getFactory().createEngineController().dispatchRawMessage(channelId, rawMessage);
        }
    }
}
