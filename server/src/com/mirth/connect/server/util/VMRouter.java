/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleEvent;
import org.mule.impl.MuleMessage;
import org.mule.impl.MuleSession;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueSession;

import com.mirth.connect.connectors.vm.VMConnector;
import com.mirth.connect.connectors.vm.VMMessageReceiver;
import com.mirth.connect.connectors.vm.VMResponse;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.controllers.ControllerFactory;

public class VMRouter {
    private static transient Log logger = LogFactory.getLog(VMRouter.class);

    public VMResponse routeMessage(String channelName, String message) {
        return routeMessage(channelName, message, true);
    }

    public VMResponse routeMessage(String channelName, String message, boolean useQueue) {
        Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelByName(channelName);

        if (channel == null) {
            logger.error("Could not find channel to route to for name: " + channelName);
            return null;
        } else {
            return routeMessageByChannelId(channel.getId(), message, useQueue);
        }
    }

    @Deprecated
    // TODO: Remove in 2.2
    public VMResponse routeMessage(String channelName, String message, boolean useQueue, boolean synchronised) {
        logger.error("The routeMessage(channelName, message, useQueue, synchronised) method is deprecated and will soon be removed. Please use routeMessage(channelName, message, useQueue)");
        return routeMessage(channelName, message, useQueue);
    }

    public VMResponse routeMessageByChannelId(String channelId, Object message, boolean useQueue) {
        UMOMessage umoMessage = null;
        VMResponse vmResponse = null;

        if (message instanceof MessageObject) {
            MessageObject messageObject = (MessageObject) message;
            umoMessage = new MuleMessage(messageObject.getRawData());

            // set the properties from the context
            for (Entry<String, Object> entry : messageObject.getContext().entrySet()) {
                umoMessage.setProperty(entry.getKey(), entry.getValue());
            }
        } else {
            umoMessage = new MuleMessage(message);
        }

        VMMessageReceiver receiver = VMRegistry.getInstance().get(channelId);

        if (receiver == null) {
            logger.error("Unable to route message. No receiver found for channel id: " + channelId);
            return null;
        }

        UMOEvent event = new MuleEvent(umoMessage, receiver.getEndpoint(), new MuleSession(), !useQueue);

        try {
            vmResponse = doDispatch(event, receiver, useQueue);
        } catch (Exception e) {
            logger.error("Unable to route: " + e.getMessage());
        }

        return vmResponse;
    }

    @Deprecated
    // TODO: Remove in 2.2
    public VMResponse routeMessageByChannelId(String channelId, Object message, boolean useQueue, boolean synchronised) {
        logger.error("The routeMessageByChannelId(channelId, message, useQueue, synchronised) method is deprecated and will soon be removed. Please use routeMessageByChannelId(channelId, message, useQueue)");
        return routeMessageByChannelId(channelId, message, useQueue);
    }

    private VMResponse doDispatch(UMOEvent event, VMMessageReceiver receiver, boolean useQueue) throws Exception {
        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();
        VMResponse vmResponse = null;

        if (endpointUri == null) {
            throw new DispatchException(new Message(Messages.X_IS_NULL, "Endpoint"), event.getMessage(), event.getEndpoint());
        }

        if (useQueue) {
            QueueSession session = ((VMConnector) receiver.getConnector()).getQueueSession();
            Queue queue = session.getQueue(endpointUri.getAddress());
            queue.put(event);
        } else {
            if (receiver == null) {
                logger.warn("No receiver for endpointUri: " + event.getEndpoint().getEndpointURI());
                return null;
            }

            vmResponse = receiver.dispatchMessage(event);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("dispatched Event on endpointUri: " + endpointUri);
        }

        return vmResponse;
    }
}
