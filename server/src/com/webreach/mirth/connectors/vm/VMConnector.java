/* 
 * $Header: /home/projects/mule/scm/mule/providers/vm/src/java/org/mule/providers/vm/VMConnector.java,v 1.11 2005/10/19 14:15:31 holger Exp $
 * $Revision: 1.11 $
 * $Date: 2005/10/19 14:15:31 $
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

import java.util.Iterator;

import org.mule.MuleManager;
import org.mule.config.QueueProfile;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.routing.filters.WildcardFilter;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.MessagingException;
import org.mule.umo.TransactionException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.EndpointException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.util.ClassHelper;
import org.mule.util.queue.QueueManager;
import org.mule.util.queue.QueueSession;

import com.webreach.mirth.server.util.VMRegistry;

/**
 * <code>VMConnector</code> A simple endpoint wrapper to allow a Mule
 * component to <p/> be accessed from an endpoint
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.11 $
 */

public class VMConnector extends AbstractServiceEnabledConnector
{
    private boolean queueEvents = false;
    private int maxQueues = 16;
    private QueueProfile queueProfile;
    private Class adapterClass = null;
    private String channelId;
    private boolean synchronised = false;
    private String template;
    public String getChannelId() {
		return this.channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractConnector#create()
     */
    public void doInitialise() throws InitialisationException
    {
        super.doInitialise();
        if (queueEvents) {
            if (queueProfile == null) {
                queueProfile = MuleManager.getConfiguration().getQueueProfile();
            }
        }

        try {
            adapterClass = ClassHelper.loadClass(serviceDescriptor.getMessageAdapter(), getClass());
        } catch (ClassNotFoundException e) {
            throw new InitialisationException(new Message(Messages.FAILED_LOAD_X, "Message Adapter: "
                    + serviceDescriptor.getMessageAdapter()), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#registerListener(org.mule.umo.UMOSession,
     *      org.mule.umo.endpoint.UMOEndpoint)
     */
    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception
    {
        if (queueEvents) {
            queueProfile.configureQueue(endpoint.getEndpointURI().getAddress());
        }
        return serviceDescriptor.createMessageReceiver(this, component, endpoint);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#getMessageAdapter(java.lang.Object)
     */
    public UMOMessageAdapter getMessageAdapter(Object message) throws MessagingException
    {
        if (message == null) {
            throw new MessageTypeNotSupportedException(null, adapterClass);

        } else if (message instanceof MuleMessage) {
            return ((MuleMessage) message).getAdapter();
        } else if (message instanceof UMOMessageAdapter) {
            return (UMOMessageAdapter) message;
        } else {
            throw new MessageTypeNotSupportedException(message, adapterClass);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#getProtocol()
     */
    public String getProtocol()
    {
        return "VM";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractConnector#doDispose()
     */
    protected void doDispose()
    {
    }
   
    public boolean isQueueEvents()
    {
        return queueEvents;
    }

    public void setQueueEvents(boolean queueEvents)
    {
        this.queueEvents = queueEvents;
    }

    public QueueProfile getQueueProfile()
    {
        return queueProfile;
    }

    public void setQueueProfile(QueueProfile queueProfile)
    {
        this.queueProfile = queueProfile;
    }

    public void setMaxQueues(int maxQueues)
    {
        this.maxQueues = maxQueues;
    }

    VMMessageReceiver getReceiver(UMOEndpointURI endpointUri) throws EndpointException
    {
        return (VMMessageReceiver) getReceiverByEndpoint(endpointUri);
    }

    public QueueSession getQueueSession() throws InitialisationException
    {
        QueueManager qm = MuleManager.getInstance().getQueueManager();
        UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
        if (tx != null) {
            if (tx.hasResource(qm)) {
                if (logger.isDebugEnabled()) {
                	logger.debug("Retrieving queue session from current transaction");
                }
                return (QueueSession) tx.getResource(qm);
            }
        }

        if (logger.isDebugEnabled()) {
			logger.debug("Retrieving new queue session from queue manager");
        }

        QueueSession session = qm.getQueueSession();
        if (tx != null) {
            logger.debug("Binding queue session to current transaction");
            try {
                tx.bindResource(qm, session);
            } catch (TransactionException e) {
                throw new RuntimeException("Could not bind queue session to current transaction", e);
            }
        }
        return session;
    }

    protected UMOMessageReceiver getReceiverByEndpoint(UMOEndpointURI endpointUri) throws EndpointException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up vm receiver for address: " + endpointUri.toString());
        }

        UMOMessageReceiver receiver;
        // If we have an exact match, use it
        receiver = (UMOMessageReceiver) VMRegistry.getInstance().get(endpointUri.getAddress());
        if (receiver != null) {
            if (logger.isDebugEnabled()) {
            	logger.debug("Found exact receiver match on endpointUri: " + endpointUri);
            }
            return receiver;
        }

        // otherwise check each one against a wildcard match
        for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();) {
            receiver = (UMOMessageReceiver) iterator.next();
            String filterAddress = receiver.getEndpointURI().getAddress();
            WildcardFilter filter = new WildcardFilter(filterAddress);
            if (filter.accept(endpointUri.getAddress())) {
                receiver.getEndpoint().setEndpointURI(new MuleEndpointURI(endpointUri, filterAddress));

                if (logger.isDebugEnabled()) {
	                logger.debug("Found receiver match on endpointUri: " +
	                		receiver.getEndpointURI() + " against " + endpointUri);
                }
                return receiver;
            }
        }
        if (logger.isDebugEnabled()) {
        	logger.debug("No receiver found for endpointUri: " + endpointUri);
        }
        return null;
    }

    public boolean isRemoteSyncEnabled() {
        return true;
}

	public boolean isSynchronised() {
		return synchronised;
	}

	public void setSynchronised(boolean synchronised) {
		this.synchronised = synchronised;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
}
