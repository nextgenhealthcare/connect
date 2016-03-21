/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.Set;

import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.ChannelDependencyException;
import com.mirth.connect.util.ChannelDependencyGraph;
import com.mirth.connect.util.ChannelDependencyUtil;
import com.mirth.connect.util.ChannelDependencyUtil.OrderedChannels;

public class ChannelDependencyServerUtil {

    private static final ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    public static ChannelDependencyGraph getDependencyGraph() throws ChannelDependencyException {
        return ChannelDependencyUtil.getDependencyGraph(configurationController.getChannelDependencies());
    }

    public static OrderedChannels getOrderedChannels(Set<String> channelIds) throws ChannelDependencyException {
        return ChannelDependencyUtil.getOrderedChannels(channelIds, getDependencyGraph());
    }
}