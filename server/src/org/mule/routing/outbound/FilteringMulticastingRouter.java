/*
 * $Header: /home/projects/mule/scm/mule/mule/src/java/org/mule/routing/outbound/MulticastingRouter.java,v 1.8 2005/06/23 08:01:27 gnt Exp $
 * $Revision: 1.8 $
 * $Date: 2005/06/23 08:01:27 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.routing.outbound;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.umo.UMOException;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOSession;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.routing.CouldNotRouteOutboundMessageException;
import org.mule.umo.routing.RoutePathNotFoundException;
import org.mule.umo.routing.RoutingException;

/**
 * <code>MulticastingRouter</code> will broadcast the current message to every
 * endpoint registed with the router.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.8 $
 */

public class FilteringMulticastingRouter extends FilteringOutboundRouter
{

	public UMOMessage route(UMOMessage message, UMOSession session,
			boolean synchronous) throws RoutingException
	{
		UMOMessage result = null;
		if (endpoints == null || endpoints.size() == 0)
		{
			throw new RoutePathNotFoundException(new Message(
					Messages.NO_ENDPOINTS_FOR_ROUTER), message, null);
		}
		if (enableCorrelation != ENABLE_CORRELATION_NEVER)
		{
			boolean correlationSet = message.getCorrelationId() != null;
			if (correlationSet
					&& (enableCorrelation == ENABLE_CORRELATION_IF_NOT_SET))
			{
				logger
						.debug("CorrelationId is already set, not setting Correlation group size");
			} else
			{
				// the correlationId will be set by the AbstractOutboundRouter
				message.setCorrelationGroupSize(endpoints.size());
			}
		}

		try
		{
			UMOEndpoint endpoint;
			synchronized (endpoints)
			{
				for (int i = 0; i < endpoints.size(); i++)
				{
					endpoint = (UMOEndpoint) endpoints.get(i);
					boolean processFilter = false;

					UMOFilter filter = endpoint.getFilter();
					if (filter == null)
					{
						processFilter = true;
					} else
					{
						
						if (endpoint.getFilter().accept(message))
							processFilter = true;
					}
					if (processFilter)
					{
						if (synchronous)
						{
							// Were we have multiple outbound endpoints
							if (result == null)
							{
								result = send(session, message, endpoint);
							} else
							{
								String def = (String) endpoint.getProperties()
										.get("default");
								if (def != null)
								{
									result = send(session, message, endpoint);
								} else
								{
									send(session, message, endpoint);
								}
							}
						} else
						{
							dispatch(session, message, endpoint);
						}
					}
				}
			}
		} catch (UMOException e)
		{
			throw new CouldNotRouteOutboundMessageException(message,
					(UMOEndpoint) endpoints.get(0), e);
		}
		return result;
	}
}
