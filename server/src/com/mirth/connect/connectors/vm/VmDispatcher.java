/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.vm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.Constants;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorMessageBuilder;

public class VmDispatcher extends DestinationConnector {
    private static final String SOURCE_CHANNEL_ID = "sourceChannelId";
    private static final String SOURCE_CHANNEL_IDS = "sourceChannelIds";
    private static final String SOURCE_MESSAGE_ID = "sourceMessageId";
    private static final String SOURCE_MESSAGE_IDS = "sourceMessageIds";

    private VmDispatcherProperties connectorProperties;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (VmDispatcherProperties) getConnectorProperties();
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {}

    @Override
    public void onStart() throws ConnectorTaskException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onStop() throws ConnectorTaskException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.DISCONNECTED));
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.DISCONNECTED));
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

        String targetChannelId = vmDispatcherProperties.getChannelId();
        String currentChannelId = getChannelId();

        eventController.dispatchEvent(new ConnectionStatusEvent(currentChannelId, getMetaDataId(), getDestinationName(), ConnectionStatusEventType.SENDING, "Target Channel: " + targetChannelId));

        String responseData = null;
        String responseError = null;
        String responseStatusMessage = null;
        Status responseStatus = Status.QUEUED; // Always set the status to QUEUED
        boolean validateResponse = false;

        try {
            if (!targetChannelId.equals("none")) {
                boolean isBinary = ExtensionController.getInstance().getDataTypePlugins().get(this.getOutboundDataType().getType()).isBinary();
                byte[] data = getAttachmentHandler().reAttachMessage(vmDispatcherProperties.getChannelTemplate(), message, Constants.ATTACHMENT_CHARSET, isBinary);

                RawMessage rawMessage;

                if (isBinary) {
                    rawMessage = new RawMessage(data);
                } else {
                    rawMessage = new RawMessage(StringUtils.newString(data, Constants.ATTACHMENT_CHARSET));
                }

                Map<String, Object> rawSourceMap = rawMessage.getSourceMap();
                Map<String, Object> sourceMap = message.getSourceMap();

                /*
                 * Build the lists of source channel and message Ids if this channel is not the
                 * start of the chain.
                 */
                List<String> sourceChannelIds = getSourceChannelIds(sourceMap);
                List<Long> sourceMessageIds = getSourceMessageIds(sourceMap);

                // Add the current channelId to the chain if it is built
                if (sourceChannelIds != null) {
                    sourceChannelIds.add(currentChannelId);
                    rawSourceMap.put(SOURCE_CHANNEL_IDS, sourceChannelIds);
                }

                // Add the current messageId to the chain if it is built
                if (sourceMessageIds != null) {
                    sourceMessageIds.add(message.getMessageId());
                    rawSourceMap.put(SOURCE_MESSAGE_IDS, sourceMessageIds);
                }

                // Always store the originating channelId and messageId
                rawSourceMap.put(SOURCE_CHANNEL_ID, currentChannelId);
                rawSourceMap.put(SOURCE_MESSAGE_ID, message.getMessageId());

                // Remove the reference to the raw message so its doesn't hold the entire message in memory.
                data = null;

                DispatchResult dispatchResult = ControllerFactory.getFactory().createEngineController().dispatchRawMessage(targetChannelId, rawMessage, false, true);

                if (dispatchResult.getSelectedResponse() != null) {
                    // If a response was returned from the channel then use that message
                    responseData = dispatchResult.getSelectedResponse().getMessage();
                }

                validateResponse = vmDispatcherProperties.getDestinationConnectorProperties().isValidateResponse();
            }

            responseStatus = Status.SENT;
            responseStatusMessage = "Message routed successfully to channel id: " + targetChannelId;
        } catch (Throwable e) {
            eventController.dispatchEvent(new ErrorEvent(currentChannelId, getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error routing message to channel id: " + targetChannelId, e));
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error routing message to channel id: " + targetChannelId, e);
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error routing message to channel id: " + targetChannelId, e);
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(currentChannelId, getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError, validateResponse);
    }

    private List<String> getSourceChannelIds(Map<String, Object> map) {
        Object object = map.get(SOURCE_CHANNEL_ID);

        List<String> sourceChannelIds = null;

        /*
         * If the source channel id already exists, then a source channel id list needs to be
         * created to store the historical channel ids.
         */
        if (object != null && object instanceof String) {
            String sourceChannelId = (String) object;
            sourceChannelIds = new ArrayList<String>();

            Object listObject = map.get(SOURCE_CHANNEL_IDS);

            /*
             * If the source channel id list already exists, add all items into the new list.
             * Otherwise only add the previous channel id to the new list.
             */
            if (listObject == null) {
                sourceChannelIds.add(sourceChannelId);
            } else {
                try {
                    sourceChannelIds.addAll((List<String>) listObject);
                } catch (ClassCastException e) {
                    sourceChannelIds.add(sourceChannelId);
                }
            }
        }

        return sourceChannelIds;
    }

    private List<Long> getSourceMessageIds(Map<String, Object> map) {
        Object object = map.get(SOURCE_MESSAGE_ID);

        List<Long> sourceMessageIds = null;

        /*
         * If the source message id already exists, then a source message id list needs to be
         * created to store the historical message ids.
         */
        if (object != null && object instanceof Long) {
            Long sourceMessageId = (Long) object;
            sourceMessageIds = new ArrayList<Long>();

            Object listObject = map.get(SOURCE_MESSAGE_IDS);

            /*
             * If the source message id list already exists, add all items into the new list.
             * Otherwise only add the previous message id to the new list.
             */
            if (listObject == null) {
                sourceMessageIds.add(sourceMessageId);
            } else {
                try {
                    sourceMessageIds.addAll((List<Long>) listObject);
                } catch (ClassCastException e) {
                    sourceMessageIds.add(sourceMessageId);
                }
            }
        }

        return sourceMessageIds;
    }
}
