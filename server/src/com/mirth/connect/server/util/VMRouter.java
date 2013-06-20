/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class VMRouter {
    private static transient Log logger = LogFactory.getLog(VMRouter.class);
    private static final int DEFAULT_TIMEOUT = 10000;
    private static ExecutorService executor = Executors.newCachedThreadPool();

    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();

    public Response routeMessage(String channelName, String message) {
        return routeMessage(channelName, message, DEFAULT_TIMEOUT);
    }

    public Response routeMessage(String channelName, RawMessage rawMessage) {
        return routeMessage(channelName, rawMessage, DEFAULT_TIMEOUT);
    }

    public Response routeMessage(String channelName, String message, int timeout) {
        return routeMessage(channelName, new RawMessage(message, null, null), timeout);
    }

    public Response routeMessage(String channelName, RawMessage rawMessage, int timeout) {
        com.mirth.connect.model.Channel channel = channelController.getDeployedChannelByName(channelName);

        if (channel == null) {
            logger.error("Could not find channel to route to for channel name: " + channelName);
            return new Response(Status.ERROR, "Could not find channel to route to for channel name: " + channelName);
        }

        return routeMessageByChannelId(channel.getId(), rawMessage, timeout);
    }

    public Response routeMessageByChannelId(String channelId, String message) {
        return routeMessageByChannelId(channelId, message, DEFAULT_TIMEOUT);
    }

    public Response routeMessageByChannelId(String channelId, RawMessage rawMessage) {
        return routeMessageByChannelId(channelId, rawMessage, DEFAULT_TIMEOUT);
    }

    public Response routeMessageByChannelId(String channelId, String message, int timeout) {
        return routeMessageByChannelId(channelId, new RawMessage(message, null, null), timeout);
    }

    public Response routeMessageByChannelId(String channelId, RawMessage rawMessage, int timeout) {
        try {
            if (timeout > 0) {
                return executor.submit(new DispatchTask(channelId, rawMessage)).get(timeout, TimeUnit.MILLISECONDS).getSelectedResponse();
            } else {
                return engineController.dispatchRawMessage(channelId, rawMessage).getSelectedResponse();
            }
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

            logger.error(longMessage, cause);
            String responseStatusMessage = ErrorMessageBuilder.buildErrorResponse(shortMessage, cause);
            String responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_412, longMessage, cause);
            return new Response(Status.ERROR, null, responseStatusMessage, responseError);
        }
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
            return engineController.dispatchRawMessage(channelId, rawMessage);
        }
    }
}
