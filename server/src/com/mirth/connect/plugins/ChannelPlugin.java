package com.mirth.connect.plugins;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerEventContext;

public interface ChannelPlugin extends ServerPlugin {
    public void save(Channel channel, ServerEventContext context) throws Exception;

    public void remove(Channel channel, ServerEventContext context) throws Exception;

    /**
     * This is invoked for every channel when deployed.
     * 
     * @param channel
     * @param context
     * @throws Exception
     */
    public void deploy(Channel channel, ServerEventContext context) throws Exception;
    
    /**
     * This is inokved once per deploy.
     * 
     * @param context
     * @throws Exception
     */
    public void deploy(ServerEventContext context) throws Exception;
    
    public void undeploy(String channelId, ServerEventContext context) throws Exception;
    
    public void undeploy(ServerEventContext context) throws Exception;
}
