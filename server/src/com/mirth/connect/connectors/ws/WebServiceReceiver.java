/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.io.IOException;
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

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public class WebServiceReceiver extends SourceConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    protected WebServiceReceiverProperties connectorProperties;
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private ConnectorType connectorType = ConnectorType.LISTENER;
    private HttpServer server;
    private ExecutorService threads;
    private Endpoint webServiceEndpoint;

    @Override
    public void onDeploy() {
        this.connectorProperties = (WebServiceReceiverProperties) getConnectorProperties();
    }

    @Override
    public void onUndeploy() throws UndeployException {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart() throws StartException {
        // TODO: TemplateValueReplacer
        String host = connectorProperties.getListenerConnectorProperties().getHost();
        int port = Integer.parseInt(connectorProperties.getListenerConnectorProperties().getPort());

        logger.debug("starting Web Service HTTP server on port: " + port);

        java.util.logging.Logger.getLogger("javax.enterprise.resource.webservices.jaxws.server").setLevel(java.util.logging.Level.OFF);

        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 5);
        } catch (IOException e) {
            throw new StartException("Error creating HTTP Server.", e.getCause());
        }

        threads = Executors.newFixedThreadPool(5);
        server.setExecutor(threads);
        server.start();

        AcceptMessage acceptMessageWebService = null;

        try {
            Class<?> clazz = Class.forName(connectorProperties.getClassName());

            if (clazz.getSuperclass().equals(AcceptMessage.class)) {
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                for (int i = 0; i < constructors.length; i++) {
                    Class<?>[] parameters = constructors[i].getParameterTypes();
                    if ((parameters.length == 1) && parameters[0].equals(this.getClass())) {
                        acceptMessageWebService = (AcceptMessage) constructors[i].newInstance(new Object[] { this });
                    }
                }

                if (acceptMessageWebService == null) {
                    logger.error("Custom web service class must implement the constructor: public AcceptMessage(WebServiceReceiver webServiceReceiver)");
                }
            } else {
                logger.error("Custom web service class must extend com.mirth.connect.connectors.ws.AcceptMessage");
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
        HttpContext context = server.createContext("/services/" + connectorProperties.getServiceName());

        if (connectorProperties.getUsernames().size() > 0) {
            context.setAuthenticator(new BasicAuthenticator("/services/" + connectorProperties.getServiceName()) {
                @Override
                public boolean checkCredentials(String username, String password) {
                    List<String> usernames = connectorProperties.getUsernames();
                    List<String> passwords = connectorProperties.getPasswords();
                    if (usernames.contains(username) && passwords.get(usernames.indexOf(username)).equals(password)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        webServiceEndpoint.publish(context);

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
    }

    @Override
    public void onStop() throws StopException {
        try {
            logger.debug("stopping Web Service HTTP server");
            webServiceEndpoint.stop();
            server.stop(1);
            threads.shutdown();
        } catch (Exception e) {
            throw new StopException("Failed to stop Web Service Listener", e.getCause());
        }
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
    	finishDispatch(dispatchResult);
    }

    public String processData(String message) {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY);

        RawMessage rawMessage = new RawMessage(message);
        DispatchResult dispatchResult = null;
        String response = null;
        
        try {
            dispatchResult = super.dispatchRawMessage(rawMessage);
            
            if (dispatchResult.getSelectedResponse() != null) {
                response = dispatchResult.getSelectedResponse().getMessage();
            }
        } catch (ChannelException e) {
            // TODO auto-generate an error response?
            response = "";
        } finally {
            // TODO: response should be returned before it is marked as finished
            // TODO: figure out how to get the error message if an error occurred in sending the response back
            finishDispatch(dispatchResult, true, response, null);
        }

        return response;
    }
}