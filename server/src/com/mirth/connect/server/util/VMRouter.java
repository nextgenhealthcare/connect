/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class VMRouter {
    private static transient Log logger = LogFactory.getLog(VMRouter.class);
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();

    // TODO: investigate the side-effects of removing the useQueue parameter
//    public Response routeMessage(String channelName, String message, boolean useQueue) {
    public Response routeMessage(String channelName, String message) {
        com.mirth.connect.model.Channel channel = channelController.getCachedChannelByName(channelName);

        if (channel == null) {
            logger.error("Could not find channel to route to for channel name: " + channelName);
            return null;
        }

        return routeMessageByChannelId(channel.getId(), message);
    }

    // TODO: Add composites to handle RawMessage objects
    public Response routeMessageByChannelId(String channelId, String message) {
        try {
            return engineController.handleRawMessage(channelId, new RawMessage(message, null, null));
        } catch (Throwable e) {
            logger.error(e);
            return new Response(Status.ERROR, null);
        }
    }
}
