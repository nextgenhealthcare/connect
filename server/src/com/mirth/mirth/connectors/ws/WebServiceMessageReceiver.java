/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.ws;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;

import org.apache.log4j.Logger;
import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Response;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.mule.transformers.JavaScriptPostprocessor;

public class WebServiceMessageReceiver extends AbstractMessageReceiver {
    private Logger logger = Logger.getLogger(this.getClass());
    protected WebServiceConnector connector;
    private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.LISTENER;
    private HttpServer server;
    private ExecutorService threads;
    private Endpoint webServiceEndpoint;

    public WebServiceMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
        super(connector, component, endpoint);
        this.connector = (WebServiceConnector) connector;
    }

    public void doConnect() throws Exception {
        logger.debug("starting Web Service HTTP server on port: " + endpoint.getEndpointURI().getUri().getPort());

        java.util.logging.Logger.getLogger("javax.enterprise.resource.webservices.jaxws.server").setLevel(java.util.logging.Level.OFF);

        server = HttpServer.create(new InetSocketAddress(endpoint.getEndpointURI().getUri().getHost(), endpoint.getEndpointURI().getUri().getPort()), 5);

        threads = Executors.newFixedThreadPool(5);
        server.setExecutor(threads);
        server.start();

        AcceptMessage acceptMessageWebService = null;

        try {
            Class<?> clazz = Class.forName(connector.getReceiverClassName());

            if (clazz.getSuperclass().equals(AcceptMessage.class)) {
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    Class<?>[] parameters = constructors[i].getParameterTypes();
                    if ((parameters.length == 1) && parameters[0].equals(this.getClass())) {
                        acceptMessageWebService = (AcceptMessage) constructors[i].newInstance(new Object[] { this });
                    }
                }

                if (acceptMessageWebService == null) {
                    logger.error("Custom web service class must implement the constructor: public AcceptMessage(WebServiceMessageReceiver webServiceMessageReceiver)");
                }
            } else {
                logger.error("Custom web service class must extend com.webreach.mirth.connectors.ws.AcceptMessage");
            }
        } catch (Exception e) {
            logger.error("Custom web service class initialization failed", e);
        }

        if (acceptMessageWebService == null) {
            logger.error("Custom web service class initialization failed, using DefaultAcceptMessage");
            acceptMessageWebService = new DefaultAcceptMessage(this);
        }

        webServiceEndpoint = Endpoint.create(acceptMessageWebService);
        Binding binding = webServiceEndpoint.getBinding();
        List<Handler> handlerChain = new LinkedList<Handler>();
        handlerChain.add(new LoggingSOAPHandler(this));
        binding.setHandlerChain(handlerChain);
        HttpContext context = server.createContext("/services/" + connector.getReceiverServiceName());

        if (connector.getReceiverUsernames().size() > 0) {
            context.setAuthenticator(new BasicAuthenticator("/services/" + connector.getReceiverServiceName()) {
                @Override
                public boolean checkCredentials(String username, String password) {
                    List<String> usernames = connector.getReceiverUsernames();
                    List<String> passwords = connector.getReceiverPasswords();
                    if (usernames.contains(username) && passwords.get(usernames.indexOf(username)).equals(password)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        webServiceEndpoint.publish(context);

        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    public void doDisconnect() throws Exception {

    }

    public void doStop() throws UMOException {
        super.doStop();

        try {
            logger.debug("stopping Web Service HTTP server");
            webServiceEndpoint.stop();
            server.stop(1);
            threads.shutdown();
        } catch (Exception e) {
            throw new MuleException(new Message(Messages.FAILED_TO_STOP_X, "Web Service Listener"), e.getCause());
        }
    }

    protected String processData(String message) {
        try {
            monitoringController.updateStatus(connector, connectorType, Event.BUSY);
            UMOMessageAdapter adapter = connector.getMessageAdapter(message);
            UMOMessage response = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous());

            if ((response != null) && (response instanceof MuleMessage)) {
                Object payload = response.getPayload();

                if (payload instanceof MessageObject) {
                    MessageObject messageObjectResponse = (MessageObject) payload;
                    postProcessor.doPostProcess(messageObjectResponse);

                    if (!connector.getReceiverResponseValue().equalsIgnoreCase("None")) {
                        return ((Response) messageObjectResponse.getResponseMap().get(connector.getReceiverResponseValue())).getMessage();
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing message in web service.  Channel: " + connector.getChannelId(), e);
        }
        return null;
    }
}