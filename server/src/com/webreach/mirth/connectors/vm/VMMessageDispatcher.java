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
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOExceptionPayload;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.NoReceiverForEndpointException;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.queue.Queue;
import org.mule.util.queue.QueueSession;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.Status;

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
    private MessageObjectController messageObjectController = MessageObjectController.getInstance();
    private AlertController alertController = AlertController.getInstance();
    private MonitoringController monitoringController = MonitoringController.getInstance();
    
    public VMMessageDispatcher(VMConnector connector)
    {
        super(connector);
        this.connector = connector;
        monitoringController.updateStatus(connector, Status.IDLE);
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
    	monitoringController.updateStatus(connector, Status.PROCESSING);
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
        	 monitoringController.updateStatus(connector, Status.IDLE);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#dispatch(org.mule.umo.UMOEvent)
     */
    public void doDispatch(UMOEvent event) throws Exception
    {
    	monitoringController.updateStatus(connector, Status.PROCESSING);
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
                if (receiver == null)
                {
                    logger.warn("No receiver for endpointUri: " + event.getEndpoint().getEndpointURI());
                    return;
                }

                UMOMessage response = receiver.routeMessage(new MuleMessage(messageObject.getEncodedData()), connector.isSynchronised());
                if (response != null && response instanceof MuleMessage)
                {

                    UMOExceptionPayload payload = response.getExceptionPayload();
                    if (payload != null)
                    {
                        alertController.sendAlerts(((VMConnector) connector).getChannelId(), Constants.ERROR_412, "Error routing message", payload.getException());
                        messageObjectController.setError(messageObject, Constants.ERROR_412, "Error routing message", payload.getException());
                    }
                    else
                    {
                        messageObjectController.setSuccess(messageObject, "Message routed successfully");
                    }
                }
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("dispatched Event on endpointUri: " + endpointUri);
            }
        }
        catch (Exception e)
        {
            alertController.sendAlerts(((VMConnector) connector).getChannelId(), Constants.ERROR_412, "Error routing message", e);
            messageObjectController.setError(messageObject, Constants.ERROR_412, "Error routing message", e);
            throw (e);
        }
        finally{
        	 monitoringController.updateStatus(connector, Status.IDLE);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#send(org.mule.umo.UMOEvent)
     */
    public UMOMessage doSend(UMOEvent event) throws Exception
    {

    	monitoringController.updateStatus(connector, Status.PROCESSING);
        UMOMessage retMessage = null;
        UMOEndpointURI endpointUri = event.getEndpoint().getEndpointURI();
        MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
        if (messageObject == null)
        {
            return null;
        }
        try
        {
            VMMessageReceiver receiver = connector.getReceiver(endpointUri);
            if (receiver == null)
            {
                if (connector.isQueueEvents())
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Writing to queue as there is no receiver on connector: " + connector.getName() + ", for endpointUri: " + event.getEndpoint().getEndpointURI());
                    }
                    doDispatch(event);
                    return null;
                }
                else
                {
                    throw new NoReceiverForEndpointException(new Message(Messages.NO_RECEIVER_X_FOR_ENDPOINT_X, connector.getName(), event.getEndpoint().getEndpointURI()));
                }
            }

            UMOMessage response = receiver.routeMessage(new MuleMessage(messageObject.getEncodedData()), connector.isSynchronised());

            if (response != null && response instanceof MuleMessage)
            {

                UMOExceptionPayload payload = response.getExceptionPayload();
                if (payload != null)
                {
                    alertController.sendAlerts(((VMConnector) connector).getChannelId(), Constants.ERROR_412, "Error routing message", payload.getException());
                    messageObjectController.setError(messageObject, Constants.ERROR_412, "Error routing message", payload.getException());
                }
                else
                {
                    messageObjectController.setSuccess(messageObject, "Message routed successfully");
                }
            }
            logger.debug("sent event on endpointUri: " + event.getEndpoint().getEndpointURI());
        }
        catch (Exception e)
        {
            alertController.sendAlerts(((VMConnector) connector).getChannelId(), Constants.ERROR_412, "Error routing message", e);
            messageObjectController.setError(messageObject, Constants.ERROR_412, "Error routing message", e);
            throw (e);
        }finally{
        	 monitoringController.updateStatus(connector, Status.IDLE);
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
