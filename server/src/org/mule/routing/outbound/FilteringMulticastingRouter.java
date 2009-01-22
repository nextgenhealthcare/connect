package org.mule.routing.outbound;

import java.util.List;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOSession;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.routing.CouldNotRouteOutboundMessageException;
import org.mule.umo.routing.RoutePathNotFoundException;
import org.mule.umo.routing.RoutingException;

public class FilteringMulticastingRouter extends FilteringOutboundRouter {
    public UMOMessage route(UMOMessage message, UMOSession session, boolean synchronous) throws RoutingException {
        UMOMessage result = null;
        List<String> destinations = (List<String>) message.getProperty("destinations");

        if (endpoints == null || endpoints.size() == 0) {
            throw new RoutePathNotFoundException(new Message(Messages.NO_ENDPOINTS_FOR_ROUTER), message, null);
        }

        if (enableCorrelation != ENABLE_CORRELATION_NEVER) {
            boolean correlationSet = message.getCorrelationId() != null;

            if (correlationSet && (enableCorrelation == ENABLE_CORRELATION_IF_NOT_SET)) {
                logger.debug("CorrelationId is already set, not setting Correlation group size");
            } else {
                // the correlationId will be set by the AbstractOutboundRouter
                message.setCorrelationGroupSize(endpoints.size());
            }
        }

        try {
        	synchronized (endpoints) {
                for (int i = 0; i < endpoints.size(); i++) {
                    UMOEndpoint endpoint = (UMOEndpoint) endpoints.get(i);

                    if (destinations == null || destinations.contains(endpoint.getConnector().getName())) {
                        if ((endpoint.getFilter() == null) || endpoint.getFilter().accept(message)) {
                        	if (synchronous) {
                                // Were we have multiple outbound endpoints
                                if (result == null) {
                                    result = send(session, message, endpoint);
                                } else {
                                    String def = (String) endpoint.getProperties().get("default");

                                    if (def != null) {
                                        result = send(session, message, endpoint);
                                    } else {
                                        send(session, message, endpoint);
                                    }
                                }
                            } else {
                                dispatch(session, message, endpoint);
                            }
                        }
                    }
                }
        	}
        } catch (UMOException e) {
            throw new CouldNotRouteOutboundMessageException(message, (UMOEndpoint) endpoints.get(0), e);
        }

        return result;
    }
}
