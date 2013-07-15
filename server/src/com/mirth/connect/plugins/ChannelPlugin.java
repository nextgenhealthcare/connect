/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerEventContext;

public interface ChannelPlugin extends ServerPlugin {
    public void save(Channel channel, ServerEventContext context);

    public void remove(Channel channel, ServerEventContext context);

    /**
     * This is invoked for every channel when deployed.
     * 
     * @param channel
     * @param context
     * @throws Exception
     */
    public void deploy(Channel channel, ServerEventContext context);
    
    /**
     * This is invoked once per deploy.
     * 
     * @param context
     * @throws Exception
     */
    public void deploy(ServerEventContext context);
    
    public void undeploy(String channelId, ServerEventContext context);
    
    public void undeploy(ServerEventContext context);
}
