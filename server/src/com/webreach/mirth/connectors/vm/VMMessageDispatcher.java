/* 
 * $Header: /home/projects/mule/scm/mule/providers/vm/src/java/org/mule/providers/vm/VMMessageDispatcher.java,v 1.11 2005/08/31 13:08:27 rossmason Exp $
 * $Revision: 1.11 $
 * $Date: 2005/08/31 13:08:27 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */

package com.webreach.mirth.connectors.vm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleEvent;
import org.mule.impl.MuleMessage;
import org.mule.impl.MuleSession;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOExceptionPayload;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.EndpointException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.NoReceiverForEndpointException;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueSession;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.util.QueueUtil;

/**
 * <code>VMMessageDispatcher</code> is used for providing in memory
 * interaction between components.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.11 $
 */
public class VMMessageDispatcher extends AbstractMessageDispatcher
{
    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(VMMessageDispatcher.class);

    private VMConnector connector;
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.SENDER;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    public VMMessageDispatcher(VMConnector connector)
    {
        super(connector);
        this.connector = connector;
        monitoringController.updateStatus(connector, connectorType,  Event.INITIALIZED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOMessageDispatcher#getDelegateSession()
     */
    public Object getDelegateSession() throws UMOException
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOMessageDispatcher#receive(java.lang.String,
     *      org.mule.umo.UMOEvent)
     */
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception
    {
    	monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        if (!connector.isQueueEvents())
        {
            throw new UnsupportedOperationException("Receive only supported on the VM Queue Connector");
        }
        QueueSession queueSession = null;
        try
        {
            queueSession = connector.getQueueSession();
            Queue queue = queueSession.getQueue(endpointUri.getAddress());
            if (queue == null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("No queue with name " + endpointUri.getAddress());
                }
                return null;
            }
            else
            {
                UMOEvent event = null;
                if (logger.isDebugEnabled())
                {
                    logger.debug("Waiting for a message on " + endpointUri.getAddress());
                }
                try
                {
                    event = (UMOEvent) queue.poll(timeout);
                }
                catch (InterruptedException e)
                {
                    logger.error("Failed to receive event from queue: " + endpointUri);
                }
                if (event != null)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Event received: " + event);
                    }
                    return event.getMessage();
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("No event received after " + timeout + " ms");
                    }
                    return null;
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally{
        	 monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#dispatch(org.mule.umo.UMOEvent)
     */
    public void doDispatch(UMOEvent event) throws Exception
    {
    	monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
        if (messageObject == null)
        {
            return;
        }
        try
        {
            UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();

            if (endpointUri == null)
            {
                throw new DispatchException(new Message(Messages.X_IS_NULL, "Endpoint"), event.getMessage(), event.getEndpoint());
            }
            if (connector.isQueueEvents())
            {
                QueueSession session = connector.getQueueSession();
                Queue queue = session.getQueue(endpointUri.getAddress());
                queue.put(event);
            }
            else
            {
                VMMessageReceiver receiver = connector.getReceiver(event.getEndpoint().getEndpointURI());
                routeTemplatedMessage(messageObject, receiver, event.getEndpoint());
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("dispatched Event on endpointUri: " + endpointUri);
            }
        }
        catch (Exception e)
        {
            alertController.sendAlerts(((VMConnector) connector).getChannelId(), Constants.ERROR_412, "Error routing message", e);
            messageObjectController.setError(messageObject, Constants.ERROR_412, "Error routing message", e, null);
            throw (e);
        }
        finally{
        	 monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

	private void routeTemplatedMessage(MessageObject messageObject, VMMessageReceiver receiver,UMOEndpoint endpoint) throws UMOException {
		
		String template = "";
		UMOEvent event = null;
		Object response = null;
		
		// If the destination is the sink, it's not necessary to replace the template values
		if (!endpoint.getEndpointURI().getAddress().equals(VMConnector.SINK_CONNECTOR_ADDRESS)) {
			template = connector.getTemplate();
			if (template != null) {
				template = replacer.replaceValues(template, messageObject);
			} else {
				template = messageObject.getEncodedData();
			}
			// this could be done with the one-liner below, however we need to ensure something
			response = null;
			
			event = new MuleEvent(new MuleMessage(template), endpoint, new MuleSession(), connector.isSynchronised());
		} else {
			messageObjectController.setSuccess(messageObject, "Message sinked successfully", null);
			return;
		}
		
		// Check to see if the channel that is being written to exists.
		String channelId = event.getEndpoint().getName();
    	Channel channel = ControllerFactory.getFactory().createChannelController().getChannelCache().get(channelId);
    	boolean channelWriterNone = false;
    	if (channel == null) {
    		channelWriterNone = true;
    	}
		
    	// Use inline routing if "Wait for Channel Response" is set to true or the destination channel doesn't exist.
    	// If "Wait for Channel Response" is set to false, the message is written to the destination channel's queue.
    	// This is the same behavior that happens when using VMRouter.routeMessage("name", "message", true, false).
		if (connector.isSynchronised() || channelWriterNone) {
			if (receiver != null) 
				response = receiver.dispatchMessage(event);
			else 
				throw new NoReceiverForEndpointException(new Message(Messages.NO_RECEIVER_X_FOR_ENDPOINT_X, connector.getName(), endpoint.getEndpointURI()));
        } else {
        	String queueName = endpoint.getEndpointURI().getAddress();
        	QueueSession session = MuleManager.getInstance().getQueueManager().getQueueSession();
            Queue queue = session.getQueue(queueName);
            // Add some properties about the source to the new message
            Channel sourceChannel = ControllerFactory.getFactory().createChannelController().getChannelCache().get(messageObject.getChannelId());
            if (sourceChannel != null) {
                event.setProperty(VMConnector.SOURCE_CHANNEL_ID, sourceChannel.getId());
                event.setProperty(VMConnector.SOURCE_MESSAGE_ID, messageObject.getId());
            }
            // Add the related queue to the message data
            messageObject.getConnectorMap().put(QueueUtil.QUEUE_NAME, queueName);
            messageObject.getConnectorMap().put(QueueUtil.MESSAGE_ID, event.getId());
            
        	try {
        		// Set the message to queued in the DB before the queue event.  This ensures the 
        	    // message will be in DB before processing the message in the destination queue.
        		messageObjectController.setQueued(messageObject, "Message queued successfully", null);
	        	queue.put(event);
    			return;
            } catch (Exception e) {
                logger.error("Unable to route: " + e.getMessage());
                throw new EndpointException(new Message(Messages.INTERRUPTED_QUEUING_EVENT_FOR_X, connector.getName(), endpoint.getEndpointURI()));
            }
        }
	
		if ((response != null) && (response instanceof MuleMessage)) {
			MuleMessage muleResponse = (MuleMessage)response;
		    UMOExceptionPayload payload = muleResponse.getExceptionPayload();
		    if (payload != null) {
		        alertController.sendAlerts(((VMConnector) connector).getChannelId(), Constants.ERROR_412, "Error routing message", payload.getException());
		        messageObjectController.setError(messageObject, Constants.ERROR_412, "Error routing message", payload.getException(), null);
		    } else {
		        messageObjectController.setSuccess(messageObject, "Message routed successfully", null);
		    }
		} else {
			messageObjectController.setSuccess(messageObject, "Message routed successfully", null);
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#send(org.mule.umo.UMOEvent)
     */
    public UMOMessage doSend(UMOEvent event) throws Exception
    {

    	monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        UMOMessage retMessage = null;
        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();
        MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
        
        if (messageObject == null) {
            return null;
        }
        
        try {
            VMMessageReceiver receiver = connector.getReceiver(endpointUri);
            routeTemplatedMessage(messageObject, receiver, event.getEndpoint());
            logger.debug("sent event on endpointUri: " + event.getEndpoint().getEndpointURI());
        } catch (Exception e) {
            alertController.sendAlerts(((VMConnector) connector).getChannelId(), Constants.ERROR_412, "Error routing message", e);
            messageObjectController.setError(messageObject, Constants.ERROR_412, "Error routing message", e, null);
            throw (e);
        } finally {
        	 monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
        
        return retMessage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOMessageDispatcher#getConnector()
     */
    public UMOConnector getConnector()
    {
        return connector;
    }

    public void doDispose()
    {
    }
}
