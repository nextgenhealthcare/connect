/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.ChannelGroupServletInterface;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ChannelGroupServlet extends MirthServlet implements ChannelGroupServletInterface {

    private static final ChannelController channelController = ControllerFactory.getFactory().createChannelController();

    public ChannelGroupServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public List<ChannelGroup> getChannelGroups(Set<String> channelGroupIds) {
        if (CollectionUtils.isEmpty(channelGroupIds)) {
            return channelController.getChannelGroups(null);
        } else {
            return channelController.getChannelGroups(channelGroupIds);
        }
    }

    @Override
    public List<ChannelGroup> getChannelGroupsPost(Set<String> channelGroupIds) {
        return getChannelGroups(channelGroupIds);
    }

    @Override
    public boolean updateChannelGroups(Set<ChannelGroup> channelGroups, Set<String> channelGroupIds, boolean override) {
        try {
            return channelController.updateChannelGroups(channelGroups, channelGroupIds, override);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }
}