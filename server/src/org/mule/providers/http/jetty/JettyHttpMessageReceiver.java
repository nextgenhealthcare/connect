/* 

 * $Header: /home/projects/mule/scm/mule/providers/http/src/java/org/mule/providers/http/jetty/JettyHttpMessageReceiver.java,v 1.3 2005/09/19 09:53:38 rossmason Exp $

 * $Revision: 1.3 $

 * $Date: 2005/09/19 09:53:38 $

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
package org.mule.providers.http.jetty;

import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.util.InetAddrPort;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.http.servlet.MuleRESTReceiverServlet;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.LifecycleException;
import org.mule.umo.provider.UMOConnector;

/**
 * <code>HttpMessageReceiver</code> is a simple http server that can be used
 * to listen for http requests on a particular port
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.3 $
 */
public class JettyHttpMessageReceiver extends AbstractMessageReceiver
{
    private Server httpServer;

    public JettyHttpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
        super(connector, component, endpoint);
    }

    public void doConnect() throws Exception {
        httpServer = new Server();
        SocketListener socketListener = new SocketListener(new InetAddrPort(endpoint.getEndpointURI().getPort()));
// Todo
//        socketListener.setMaxIdleTimeMs();
//        socketListener.setMaxThreads();
//        socketListener.setMinThreads();
        httpServer.addListener(socketListener);

        String path = endpoint.getEndpointURI().getPath();
        if(path == null || "".equals(path)) {
            path ="/";
        }

        if(!path.endsWith("/")) path += "/";

        HttpContext context = httpServer.getContext(path);
        context.setRequestLog(null);

        ServletHandler handler = new ServletHandler();
        if("rest".equals(endpoint.getEndpointURI().getScheme())) {
            handler.addServlet("MuleRESTReceiverServlet", path + "*", MuleRESTReceiverServlet.class.getName());
        } else {
            handler.addServlet("JettyReceiverServlet", path + "*", JettyReceiverServlet.class.getName());
        }

        context.addHandler(handler);
        context.setAttribute("messageReceiver", this);

    }

    public void doDisconnect() throws Exception {
        //stop is automativcally called by Mule
    }

    /**
     * Template method to dispose any resources associated with this receiver.
     * There is not need to dispose the connector as this is already done by the
     * framework
     */
    protected void doDispose() {
        try {
            httpServer.stop(false);
        } catch (InterruptedException e) {
            logger.error("Error disposing Jetty recevier on: " + endpoint.getEndpointURI().toString(), e);
        }
    }

    public void doStart() throws UMOException {
        try {
            httpServer.start();
        } catch (Exception e) {
            throw new LifecycleException(new Message(Messages.FAILED_TO_START_X, "Jetty Http Reciever"),e, this);
        }
    }

    public void doStop() throws UMOException {
        try {
            httpServer.stop(true);
        } catch (InterruptedException e) {
            throw new LifecycleException(new Message(Messages.FAILED_TO_STOP_X, "Jetty Http Reciever"), e, this);
        }
    }


}
