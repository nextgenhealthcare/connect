package com.webreach.mirth.server.mule.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleEvent;
import org.mule.impl.MuleMessage;
import org.mule.impl.MuleSession;
import org.mule.providers.vm.VMConnector;
import org.mule.providers.vm.VMMessageReceiver;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueSession;

import com.webreach.mirth.server.controllers.ChannelController;

public class VMRouter {
    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(VMRouter.class);
    public void routeMessage(String channelName, String message){
    	routeMessage(channelName, message, true);
    }
    public void routeMessage(String channelName, String message, boolean useQueue){
    	String channelId = new ChannelController().getChannelId(channelName);
    	routeMessageByChannelId(channelId, message, useQueue);
    }
    public void routeMessageByChannelId(String channelId, Object message, boolean useQueue){
    	UMOMessage umoMessage = new MuleMessage(message);
    	VMMessageReceiver receiver = VMRegistry.getInstance().get(channelId);
    	UMOEvent event = new MuleEvent(umoMessage, receiver.getEndpoint(), new MuleSession(), false);
    	try {
			doDispatch(event, receiver, useQueue);
		} catch (Exception e) {
			logger.error("Unable to route: " + e.getMessage());
		}
    }
   
	 private void doDispatch(UMOEvent event, VMMessageReceiver receiver, boolean useQueue) throws Exception
	    {
	        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();

	        if (endpointUri == null) {
	            throw new DispatchException(new Message(Messages.X_IS_NULL, "Endpoint"),
	                                        event.getMessage(),
	                                        event.getEndpoint());
	        }
	        if (useQueue) {
	            QueueSession session = ((VMConnector)receiver.getConnector()).getQueueSession();
	            Queue queue = session.getQueue(endpointUri.getAddress());
	            queue.put(event);
	        } else {
	            if (receiver == null) {
	                logger.warn("No receiver for endpointUri: " + event.getEndpoint().getEndpointURI());
	                return;
	            }
	            receiver.onEvent(event);
	        }
	        if (logger.isDebugEnabled()) {
	            logger.debug("dispatched Event on endpointUri: " + endpointUri);
	        }
	    }

}
