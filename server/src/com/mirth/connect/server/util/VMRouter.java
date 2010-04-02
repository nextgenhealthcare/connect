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
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.controllers.ControllerFactory;

public class VMRouter {
    private static transient Log logger = LogFactory.getLog(VMRouter.class);

    public void routeMessage(String channelName, String message) {
        routeMessage(channelName, message, true);
    }

    public void routeMessage(String channelName, String message, boolean useQueue) {
        String channelId = ControllerFactory.getFactory().createChannelController().getChannelId(channelName);
        routeMessageByChannelId(channelId, message, useQueue, true);
    }

    public void routeMessage(String channelName, String message, boolean useQueue, boolean synchronised) {
        String channelId = ControllerFactory.getFactory().createChannelController().getChannelId(channelName);
        routeMessageByChannelId(channelId, message, useQueue, synchronised);
    }

    public void routeMessageByChannelId(String channelId, Object message, boolean useQueue, boolean synchronised) {
        UMOMessage umoMessage = null;

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
        UMOEvent event = new MuleEvent(umoMessage, receiver.getEndpoint(), new MuleSession(), false);

        try {
            doDispatch(event, receiver, useQueue, synchronised);
        } catch (Exception e) {
            logger.error("Unable to route: " + e.getMessage());
        }
    }

    private void doDispatch(UMOEvent event, VMMessageReceiver receiver, boolean useQueue, boolean synchronised) throws Exception {
        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();

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
                return;
            }

            receiver.routeMessage(event.getMessage(), synchronised);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("dispatched Event on endpointUri: " + endpointUri);
        }
    }
}
