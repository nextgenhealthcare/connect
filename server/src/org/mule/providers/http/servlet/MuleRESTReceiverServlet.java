/*
 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/servlet/MuleRESTReceiverServlet.java,v 1.3 2005/10/23 15:22:48 holger Exp $
 * $Revision: 1.3 $
 * $Date: 2005/10/23 15:22:48 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.http.servlet;

import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.EndpointException;
import org.mule.umo.endpoint.EndpointNotFoundException;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOMessageReceiver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <code>MuleRESTReceiverServlet</code> is used for sending a receiving events
 * from the Mule server via a serlet container. The servlet uses the REST style
 * of request processing GET METHOD will do a receive from an external source if an
 * endpoint parameter is set otherwise it behaves the same way as POST.
 * you can either specify the transport name i.e. to read from Jms orders.queue
 * http://www.mycompany.com/rest/jms/orders/queue <p/> or a Mule endpoint name
 * to target a specific endpoint config. This would get the first email message
 * recieved by the orderEmailInbox endpoint. <p/>
 * http://www.mycompany.com/rest/ordersEmailInbox <p/> POST Do a sysnchrous call
 * and return a result
 * http://www.clientapplication.com/service/clientquery?custId=1234 <p/> PUT Do
 * an asysnchrous call without returning a result (other than an http status
 * code) http://www.clientapplication.com/service/orders?payload=<order>more
 * beer</order> <p/> DELETE Same as GET only without returning a result
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.3 $
 */

public class MuleRESTReceiverServlet extends MuleReceiverServlet
{
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        try {
            if(httpServletRequest.getParameter("endpoint")!=null)
            {
                UMOEndpoint endpoint = getEndpointForURI(httpServletRequest);
                String timeoutString = httpServletRequest.getParameter("timeout");
                long to = timeout;
                if (timeoutString != null) {
                    to = new Long(timeoutString).longValue();
                }
                if (logger.isDebugEnabled())
                    logger.debug("Making request using endpoint: " + endpoint.toString() + " timeout is: " + to);

                UMOMessage returnMessage = endpoint.getConnector().getDispatcher("ANY").receive(endpoint.getEndpointURI(),
                                                                                                to);

                writeResponse(httpServletResponse, returnMessage);
            } else {
                AbstractMessageReceiver receiver = getReceiverForURI(httpServletRequest);
                httpServletRequest.setAttribute(PAYLOAD_PARAMETER_NAME, payloadParameterName);
                UMOMessage message = new MuleMessage(receiver.getConnector().getMessageAdapter(httpServletRequest));
                UMOMessage returnMessage = receiver.routeMessage(message, true);
                writeResponse(httpServletResponse, returnMessage);
            }
        } catch (Exception e) {
            handleException(e, "Failed to route event through Servlet Receiver", httpServletResponse);
        }
    }

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        try {
            AbstractMessageReceiver receiver = getReceiverForURI(httpServletRequest);
            httpServletRequest.setAttribute(PAYLOAD_PARAMETER_NAME, payloadParameterName);
            UMOMessage message = new MuleMessage(receiver.getConnector().getMessageAdapter(httpServletRequest));
            UMOMessage returnMessage = receiver.routeMessage(message, true);
            writeResponse(httpServletResponse, returnMessage);

        } catch (Exception e) {
            handleException(e, "Failed to Post event to Mule", httpServletResponse);
        }
    }

    protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        try {
            AbstractMessageReceiver receiver = getReceiverForURI(httpServletRequest);
            httpServletRequest.setAttribute(PAYLOAD_PARAMETER_NAME, payloadParameterName);
            UMOMessage message = new MuleMessage(receiver.getConnector().getMessageAdapter(httpServletRequest));
            receiver.routeMessage(message, MuleManager.getConfiguration().isSynchronous());

            httpServletResponse.setStatus(HttpServletResponse.SC_CREATED);
            if (feedback) {
                httpServletResponse.getWriter().write("Item was created at endpointUri: " + receiver.getEndpointURI());
            }
        } catch (Exception e) {
            handleException(e, "Failed to Post event to Mule" + e.getMessage(), httpServletResponse);
        }
    }

    protected void doDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        try {
            UMOEndpoint endpoint = getEndpointForURI(httpServletRequest);
            String timeoutString = httpServletRequest.getParameter("timeout");
            long to = timeout;
            if (timeoutString != null) {
                to = new Long(timeoutString).longValue();
            }
            if (logger.isDebugEnabled())
                logger.debug("Making request using endpoint: " + endpoint.toString() + " timeout is: " + to);

            UMOMessage returnMessage = endpoint.getConnector().getDispatcher("ANY").receive(endpoint.getEndpointURI(),
                                                                                            to);
            if (returnMessage != null) {
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            } else {
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (Exception e) {
            handleException(e,
                            "Failed to Delete mule event via receive using uri: " + httpServletRequest.getPathInfo(),
                            httpServletResponse);
        }
    }

    protected UMOEndpoint getEndpointForURI(HttpServletRequest httpServletRequest) throws EndpointException,
            MalformedEndpointException
    {
        String endpointName = httpServletRequest.getParameter("endpoint");
        if(endpointName==null) {
            throw new EndpointException(new Message("http", 8, "endpoint"));
        }

        UMOEndpoint endpoint = MuleManager.getInstance().lookupEndpoint(endpointName);
        if (endpoint == null) {
            //if we dont find an endpoint for the given name, lets check the servlet receivers
            UMOMessageReceiver receiver = (UMOMessageReceiver)getReceivers().get(endpointName);
            if (receiver == null) {
                throw new EndpointNotFoundException(endpointName);
            }
            endpoint = receiver.getEndpoint();

        }
        return endpoint;
    }
}
