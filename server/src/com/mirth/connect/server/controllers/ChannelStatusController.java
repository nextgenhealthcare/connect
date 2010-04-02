/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.connect.model.ChannelStatus;

public abstract class ChannelStatusController extends Controller {
    public static ChannelStatusController getInstance() {
        return ControllerFactory.getFactory().createChannelStatusController();
    }
    
    public abstract void startChannel(String channelId) throws ControllerException;

    /**
     * Stops the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public abstract void stopChannel(String channelId) throws ControllerException;

    /**
     * Pauses the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public abstract void pauseChannel(String channelId) throws ControllerException;

    /**
     * Resumes the channel with the specified id.
     * 
     * @param channelId
     * @throws ControllerException
     */
    public abstract void resumeChannel(String channelId) throws ControllerException;

    /**
     * Returns a list of ChannelStatus objects representing the running
     * channels.
     * 
     * @return
     * @throws ControllerException
     */
    public abstract List<ChannelStatus> getChannelStatusList();
}
