/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package org.mule.routing.inbound;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleEvent;
import org.mule.impl.MuleMessage;
import org.mule.management.stats.RouterStatistics;
import org.mule.routing.AbstractRouterCollection;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.routing.RoutingException;
import org.mule.umo.routing.UMOInboundMessageRouter;
import org.mule.umo.routing.UMOInboundRouter;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.StringMessageHelper;

import com.mirth.connect.connectors.vm.VMConnector;
import com.mirth.connect.model.Channel;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.StackTracePrinter;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArrayList;

/**
 * <code>InboundMessageRouter</code> is a collection of routers that will be
 * invoked when an event is received It is responsible for manageing a
 * collection of routers and also executing the routing logic. Each router must
 * match against the current event for the event to be routed.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason </a>
 * @version $Revision: 1.9 $
 */

public class InboundMessageRouter extends AbstractRouterCollection implements UMOInboundMessageRouter
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(InboundMessageRouter.class);

    private List endpoints = new CopyOnWriteArrayList();

    public InboundMessageRouter()
    {
        super(RouterStatistics.TYPE_INBOUND);
    }

    public UMOMessage route(UMOEvent event) throws MessagingException
    {
        if (endpoints.size() > 0 && routers.size() == 0) {
            addRouter(new InboundPassThroughRouter());
        }

        String componentName = event.getSession().getComponent().getDescriptor().getName();

        UMOEvent[] eventsToRoute = null;
        boolean noRoute = true;
        boolean match = false;
        UMOInboundRouter umoInboundRouter = null;

        for (Iterator iterator = getRouters().iterator(); iterator.hasNext();) {
            umoInboundRouter = (UMOInboundRouter) iterator.next();

            if (umoInboundRouter.isMatch(event)) {
                match = true;
                eventsToRoute = umoInboundRouter.process(event);
                
                // For the VM Connector, the new event should use the synchronous property from
                // the channel that is being routed to.  In order to do this, a new event must
                // be created from the old event and the channel's synchronous property set on it.
                if (eventsToRoute[0].getEndpoint().getConnector() instanceof VMConnector) {
                	eventsToRoute[0] = new MuleEvent(eventsToRoute[0].getMessage(), eventsToRoute[0]);
                	String channelId = eventsToRoute[0].getEndpoint().getName();
                	Channel channel = ControllerFactory.getFactory().createChannelController().getChannelCache().get(channelId);
                	
                	if (channel != null) {
                		boolean synchronizedChannel = Boolean.valueOf((String)channel.getProperties().get("synchronous"));
	                	eventsToRoute[0].setSynchronous(synchronizedChannel);
                	}
                }
                
                noRoute = (eventsToRoute == null);
                if (!matchAll) {
                    break;
                }
            }
        }

        // If the stopFurtherProcessing flag has been set
        // do not route events to the component.
        // This is the case when using a ForwardingConsumer
        // inbound router for example.
        if (!event.isStopFurtherProcessing()) {
            if (noRoute) {
                // Update stats
                if (getStatistics().isEnabled()) {
                    getStatistics().incrementNoRoutedMessage();
                }
                if (!match) {
                    if (getCatchAllStrategy() != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Message did not match any routers on: " + componentName
                                    + " - invoking catch all strategy");
                        }
                        getStatistics().incrementCaughtMessage();
                        return getCatchAllStrategy().catchMessage(event.getMessage(),
                                                                  event.getSession(),
                                                                  event.isSynchronous());

                    } else {
                        logger.warn("Message did not match any routers on: " + componentName
                                + " and there is no catch all strategy configured on this router.  Disposing message.");
                        if (logger.isDebugEnabled()) {
                            try {
                                logger.warn("Message fragment is: "
                                        + StringMessageHelper.truncate(event.getMessageAsString(), 100, true));
                            } catch (UMOException e) {

                            }
                        }
                    }
                }
            } else {
                try {
                    UMOMessage messageResult = null;
                    for (int i = 0; i < eventsToRoute.length; i++) {
                        if (event.isSynchronous()) {
                            messageResult = send(eventsToRoute[i]);
                        } else {
                            dispatch(eventsToRoute[i]);
                        }
                        // Update stats
                        if (getStatistics().isEnabled()) {
                            getStatistics().incrementRoutedMessage(eventsToRoute[i].getEndpoint());
                        }
                    }
                    return messageResult;
                } catch (UMOException e) {
                    throw new RoutingException(event.getMessage(), event.getEndpoint(), e);
                }
            }
        }
        try {
			return (eventsToRoute != null && eventsToRoute.length > 0
			        ? eventsToRoute[eventsToRoute.length - 1].getMessage() : new MuleMessage(event.getTransformedMessage()));
		} catch (TransformerException e) {
			logger.error("Error transforming: " + StackTracePrinter.stackTraceToString(e));
			return null;
		}

    }

    public void dispatch(UMOEvent event) throws UMOException
    {
        event.getSession().dispatchEvent(event);
    }

    public UMOMessage send(UMOEvent event) throws UMOException
    {

        return event.getSession().sendEvent(event);
    }

    public void addRouter(UMOInboundRouter router)
    {
        routers.add(router);
    }

    public UMOInboundRouter removeRouter(UMOInboundRouter router)
    {
        if (routers.remove(router)) {
            return router;
        } else {
            return null;
        }
    }

    public void addEndpoint(UMOEndpoint endpoint)
    {
        if (endpoint != null) {
            endpoint.setType(UMOEndpoint.ENDPOINT_TYPE_RECEIVER);
            endpoints.add(endpoint);
        } else {
            throw new NullPointerException("Endpoint cannot be null");
        }
    }

    public boolean removeEndpoint(UMOEndpoint endpoint)
    {
        return endpoints.remove(endpoint);
    }

    public List getEndpoints()
    {
        return endpoints;
    }

    public void setEndpoints(List endpoints)
    {
        this.endpoints = endpoints;
        if (endpoints != null) {
            for (Iterator it = endpoints.iterator(); it.hasNext();) {
                UMOEndpoint endpoint = (UMOEndpoint) it.next();
                endpoint.setType(UMOEndpoint.ENDPOINT_TYPE_RECEIVER);
            }
        }
    }

    /**
     * @param name the Endpoint identifier
     * @return the Endpoint or null if the endpointUri is not registered
     * @see org.mule.umo.routing.UMOInboundMessageRouter
     */
    public UMOEndpoint getEndpoint(String name)
    {
        UMOEndpoint endpointDescriptor;
        for (Iterator iterator = endpoints.iterator(); iterator.hasNext();) {
            endpointDescriptor = (UMOEndpoint) iterator.next();
            if (endpointDescriptor.getName().equals(name)) {
                return endpointDescriptor;
            }
        }
        return null;
    }
}
