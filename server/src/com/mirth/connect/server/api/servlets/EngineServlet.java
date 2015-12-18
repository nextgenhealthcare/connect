/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.EngineServletInterface;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.channel.ErrorTaskHandler;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class EngineServlet extends MirthServlet implements EngineServletInterface {

    private static final EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private static final ChannelController channelController = ControllerFactory.getFactory().createChannelController();

    public EngineServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public void redeployAllChannels(boolean returnErrors) {
        if (userHasChannelRestrictions) {
            throw new MirthApiException(Status.FORBIDDEN);
        }
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.redeployAllChannels(context, handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void deployChannel(String channelId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.deployChannels(Collections.singleton(channelId), context, handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void deployChannels(Set<String> channelIds, boolean returnErrors) {
        if (CollectionUtils.isEmpty(channelIds)) {
            channelIds = channelController.getChannelIds();
        }
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.deployChannels(redactChannelIds(channelIds), context, handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void undeployChannel(String channelId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.undeployChannels(Collections.singleton(channelId), context, handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void undeployChannels(Set<String> channelIds, boolean returnErrors) {
        if (CollectionUtils.isEmpty(channelIds)) {
            channelIds = engineController.getDeployedIds();
        }
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.undeployChannels(redactChannelIds(channelIds), context, handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }
}