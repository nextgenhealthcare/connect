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

import com.mirth.connect.model.Channel;

public interface EngineController {
    public void startEngine() throws ControllerException;

    public void stopEngine() throws ControllerException;

    public void deployChannels(List<Channel> channels) throws ControllerException;
    
    public void undeployChannels(List<String> channelIds) throws ControllerException;

    public void redeployAllChannels() throws ControllerException;
    
    public List<String> getDeployedChannelIds();
}