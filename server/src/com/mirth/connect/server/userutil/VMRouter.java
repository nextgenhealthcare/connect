/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import org.apache.log4j.Logger;

import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.userutil.Response;
import com.mirth.connect.userutil.Status;
import com.mirth.connect.util.ErrorMessageBuilder;

/**
 * Utility class used to dispatch messages to channels.
 */
public class VMRouter {
    private Logger logger = Logger.getLogger(getClass());
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();

    /**
     * Instantiates a VMRouter object.
     */
    public VMRouter() {}

    /**
     * Dispatches a message to a channel, specified by the deployed channel name.
     * 
     * @param channelName
     *            The name of the deployed channel to dispatch the message to.
     * @param message
     *            The message to dispatch to the channel.
     * @return The Response object returned by the channel, if its source connector is configured to
     *         return one.
     */
    public Response routeMessage(String channelName, String message) {
        return routeMessage(channelName, new RawMessage(message));
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel name.
     * 
     * @param channelName
     *            The name of the deployed channel to dispatch the message to.
     * @param rawMessage
     *            A RawMessage object to dispatch to the channel.
     * @return The Response object returned by the channel, if its source connector is configured to
     *         return one.
     */
    public Response routeMessage(String channelName, RawMessage rawMessage) {
        com.mirth.connect.model.Channel channel = channelController.getDeployedChannelByName(channelName);

        if (channel == null) {
            logger.error("Could not find channel to route to for channel name: " + channelName);
            return new Response(Status.ERROR, "Could not find channel to route to for channel name: " + channelName);
        }

        return routeMessageByChannelId(channel.getId(), rawMessage);
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel name.
     * 
     * @param channelName
     *            The name of the deployed channel to dispatch the message to.
     * @param message
     *            The message to dispatch to the channel.
     * @param useQueue
     *            This parameter is no longer used. If you want the downstream channel to queue its
     *            message and return immediately upon receipt, enable the source queue on the source
     *            connector.
     * @return The Response object returned by the channel, if its source connector is configured to
     *         return one.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             routeMessage(channelName, message) instead. The useQueue parameter will not be
     *             used. If you want the downstream channel to queue its message and return
     *             immediately upon receipt, enable the source queue on the source connector.
     */
    // TODO: Remove in 3.1
    public Response routeMessage(String channelName, String message, boolean useQueue) {
        logger.error("The routeMessage(channelName, message, useQueue) method is deprecated and will soon be removed. Please use routeMessage(channelName, message) instead. The useQueue parameter will not be used. If you want the downstream channel to queue its message and return immediately upon receipt, enable the source queue on the source connector.");
        return routeMessage(channelName, message);
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel ID.
     * 
     * @param channelId
     *            The ID of the deployed channel to dispatch the message to.
     * @param message
     *            The message to dispatch to the channel.
     * @param useQueue
     *            This parameter is no longer used. If you want the downstream channel to queue its
     *            message and return immediately upon receipt, enable the source queue on the source
     *            connector.
     * @return The Response object returned by the channel, if its source connector is configured to
     *         return one.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             routeMessageByChannelId(channelId, message) instead. The useQueue parameter will
     *             not be used. If you want the downstream channel to queue its message and return
     *             immediately upon receipt, enable the source queue on the source connector.
     */
    // TODO: Remove in 3.1
    public Response routeMessageByChannelId(String channelId, Object message, boolean useQueue) {
        if (message instanceof MessageObject) {
            logger.error("The routeMessageByChannelId(channelId, messageObject, useQueue) method is deprecated and will soon be removed. Please use routeMessageByChannelId(channelId, message) instead. The useQueue parameter will not be used. If you want the downstream channel to queue its message and return immediately upon receipt, enable the source queue on the source connector.");
            return routeMessageByChannelId(channelId, ((MessageObject) message).getRawData());
        } else if (message instanceof ImmutableConnectorMessage) {
            logger.error("The routeMessageByChannelId(channelId, connectorMessage, useQueue) method is deprecated and will soon be removed. Please use routeMessageByChannelId(channelId, message) instead. The useQueue parameter will not be used. If you want the downstream channel to queue its message and return immediately upon receipt, enable the source queue on the source connector.");
            return routeMessageByChannelId(channelId, ((ImmutableConnectorMessage) message).getRawData());
        } else {
            logger.error("The routeMessageByChannelId(channelId, message, useQueue) method is deprecated and will soon be removed. Please use routeMessageByChannelId(channelId, message) instead. The useQueue parameter will not be used. If you want the downstream channel to queue its message and return immediately upon receipt, enable the source queue on the source connector.");
            return routeMessageByChannelId(channelId, message.toString());
        }
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel ID.
     * 
     * @param channelId
     *            The ID of the deployed channel to dispatch the message to.
     * @param message
     *            The message to dispatch to the channel.
     * @return The Response object returned by the channel, if its source connector is configured to
     *         return one.
     */
    public Response routeMessageByChannelId(String channelId, String message) {
        return routeMessageByChannelId(channelId, new RawMessage(message));
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel ID.
     * 
     * @param channelId
     *            The ID of the deployed channel to dispatch the message to.
     * @param rawMessage
     *            A RawMessage object to dispatch to the channel.
     * @return The Response object returned by the channel, if its source connector is configured to
     *         return one.
     */
    public Response routeMessageByChannelId(String channelId, RawMessage rawMessage) {
        try {
            return new Response(engineController.dispatchRawMessage(channelId, convertRawMessage(rawMessage), false).getSelectedResponse());
        } catch (Throwable e) {
            String message = "Error routing message to channel id: " + channelId;
            logger.error(message, e);
            String responseStatusMessage = ErrorMessageBuilder.buildErrorResponse(message, e);
            String responseError = ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), message, e);
            return new Response(Status.ERROR, null, responseStatusMessage, responseError);
        }
    }

    private com.mirth.connect.donkey.model.message.RawMessage convertRawMessage(RawMessage message) {
        if (message.isBinary()) {
            return new com.mirth.connect.donkey.model.message.RawMessage(message.getRawBytes(), message.getDestinationMetaDataIds(), message.getChannelMap());
        } else {
            return new com.mirth.connect.donkey.model.message.RawMessage(message.getRawData(), message.getDestinationMetaDataIds(), message.getChannelMap());
        }
    }
}
