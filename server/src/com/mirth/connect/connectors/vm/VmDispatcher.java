/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.AttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.VMRouter;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class VmDispatcher extends DestinationConnector {
    private VmDispatcherProperties connectorProperties;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.SENDER;
    private static transient Log logger = LogFactory.getLog(VMRouter.class);

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (VmDispatcherProperties) getConnectorProperties();
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
    }

    @Override
    public void onStop() throws StopException {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DISCONNECTED);
    }

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        VmDispatcherProperties vmDispatcherProperties = (VmDispatcherProperties) SerializationUtils.clone(connectorProperties);

        vmDispatcherProperties.setChannelId(replacer.replaceValues(vmDispatcherProperties.getChannelId(), connectorMessage));
        vmDispatcherProperties.setChannelTemplate(replacer.replaceValues(vmDispatcherProperties.getChannelTemplate(), connectorMessage));

        return vmDispatcherProperties;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
        VmDispatcherProperties vmDispatcherProperties = (VmDispatcherProperties) connectorProperties;

        String channelId = vmDispatcherProperties.getChannelId();

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY, "Target Channel: " + channelId);

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

                DispatchResult dispatchResult = ControllerFactory.getFactory().createEngineController().dispatchRawMessage(channelId, rawMessage);

                if (dispatchResult.getSelectedResponse() != null) {
                    // If a response was returned from the channel then use that message
                    responseData = dispatchResult.getSelectedResponse().getMessage();
                }
            }

            responseStatus = Status.SENT;
            responseStatusMessage = "Message routed successfully to channel id: " + channelId;
        } catch (Throwable e) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), ErrorEventType.DESTINATION_CONNECTOR, connectorProperties.getName(), "Error routing message to channel id: " + channelId, e));
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error routing message to channel id: " + channelId, e);
            responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_412, "Error routing message to channel id: " + channelId, e);
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError);
    }
}
