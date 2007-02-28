/* 

 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/servlet/MuleReceiverServlet.java,v 1.3 2005/09/07 10:23:14 rossmason Exp $

 * $Revision: 1.3 $

 * $Date: 2005/09/07 10:23:14 $

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

package org.mule.providers.http.servlet;

import org.mule.config.i18n.Message;
import org.mule.impl.MuleMessage;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.http.HttpConnector;
import org.mule.providers.service.ConnectorFactory;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.EndpointException;
import org.mule.umo.provider.NoReceiverForEndpointException;
import org.mule.util.PropertiesHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.3 $
 */

public class MuleReceiverServlet extends AbstractReceiverServlet
{
    protected ServletConnector connector = null;

    protected void doInit(ServletConfig servletConfig) throws ServletException {

        connector = (ServletConnector)ConnectorFactory.getConnectorByProtocol("servlet");
        if (connector == null) {
            throw new ServletException("No servlet connector found using protocol: servlet");
        }
    }

    protected void doInit() throws ServletException {
        super.doInit();    //To change body of overridden methods use File | Settings | File Templates.
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            AbstractMessageReceiver receiver = getReceiverForURI(request);
            UMOMessage responseMessage = null;
            UMOMessage requestMessage = new MuleMessage(new HttpRequestMessageAdapter(request));
            requestMessage.setProperty(HttpConnector.HTTP_METHOD_PROPERTY, "GET");
            responseMessage = receiver.routeMessage(requestMessage, true);
            writeResponse(response, responseMessage);

        } catch (Exception e) {
            handleException(e, e.getMessage(), response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            AbstractMessageReceiver receiver = getReceiverForURI(request);
            UMOMessage responseMessage = null;
            UMOMessage requestMessage = new MuleMessage(new HttpRequestMessageAdapter(request));
            requestMessage.setProperty(HttpConnector.HTTP_METHOD_PROPERTY, "POST");
            responseMessage = receiver.routeMessage(requestMessage, receiver.getEndpoint().isSynchronous());
            if (responseMessage != null) {
                writeResponse(response, responseMessage);
            }
        } catch (Exception e) {
            handleException(e, e.getMessage(), response);
        }
    }

    protected AbstractMessageReceiver getReceiverForURI(HttpServletRequest httpServletRequest) throws EndpointException
    {
        String uri = getReceiverName(httpServletRequest);
        if (uri == null) {
            throw new EndpointException(new Message("http", 4, httpServletRequest.getRequestURI()));
        }
        AbstractMessageReceiver receiver = (AbstractMessageReceiver) getReceivers().get(uri);
        if (receiver == null) {
            //Nothing found lets try stripping the path and only use the last
            //path element
            int i = uri.lastIndexOf("/");
            if(i > -1) {
                uri = uri.substring(i+1);
                receiver = (AbstractMessageReceiver) getReceivers().get(uri);
            }
            if (receiver == null) {
                throw new NoReceiverForEndpointException("No receiver found for endpointUri: " + uri);
            }
        }
        receiver.getEndpoint().setEndpointURI(new MuleEndpointURI(getRequestUrl(httpServletRequest)));
        return receiver;
    }

    protected String getRequestUrl(HttpServletRequest httpServletRequest) {
        StringBuffer url = new StringBuffer();
        url.append(connector.getProtocol().toLowerCase());
        url.append(":");
        url.append(httpServletRequest.getScheme());
        url.append("://");
        url.append(httpServletRequest.getServerName());
        url.append(":");
        url.append(httpServletRequest.getServerPort());
        url.append("/");
        url.append(getReceiverName(httpServletRequest));
        if(httpServletRequest.getQueryString()!=null) {
            url.append("?");
            url.append(httpServletRequest.getQueryString());
        }
        return url.toString();
    }

    protected String getReceiverName(HttpServletRequest httpServletRequest) {
        String name = httpServletRequest.getPathInfo();
        if(name==null) {
            name = httpServletRequest.getParameter("endpoint");
            if(name==null) {
                Properties params = PropertiesHelper.getPropertiesFromQueryString(httpServletRequest.getQueryString());
                name = params.getProperty("endpoint");
                if(name==null) return null;
            }
        }
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name;
    }

    protected Map getReceivers() {
        return connector.getReceivers();
    }
}