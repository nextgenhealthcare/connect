/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageReader;
import com.mirth.connect.donkey.server.message.batch.ResponseHandler;
import com.mirth.connect.donkey.server.message.batch.SimpleResponseHandler;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public class WebServiceReceiver extends SourceConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private ExecutorService executor;
    private Endpoint webServiceEndpoint;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private WebServiceConfiguration configuration;
    private HttpServer server;
    private WebServiceReceiverProperties connectorProperties;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (WebServiceReceiverProperties) getConnectorProperties();

        // load the default configuration
        String configurationClass = configurationController.getProperty(connectorProperties.getProtocol(), "wsConfigurationClass");

        try {
            configuration = (WebServiceConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Exception e) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultWebServiceConfiguration();
        }

        try {
            configuration.configureConnectorDeploy(this);
        } catch (Exception e) {
            throw new ConnectorTaskException(e);
        }
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {
        configuration.configureConnectorUndeploy(this);
    }

    @Override
    public void onStart() throws ConnectorTaskException {
        String host = replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getHost(), getChannelId());
        int port = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getPort(), getChannelId()));

        logger.debug("starting Web Service HTTP server on port: " + port);

        java.util.logging.Logger.getLogger("javax.enterprise.resource.webservices.jaxws.server").setLevel(java.util.logging.Level.OFF);

        try {
            configuration.configureReceiver(this);
            server.bind(new InetSocketAddress(host, port), 5);
        } catch (Exception e) {
            throw new ConnectorTaskException("Error creating HTTP Server.", e);
        }

        executor = Executors.newFixedThreadPool(5);
        server.setExecutor(executor);
        server.start();

        AcceptMessage acceptMessageWebService = null;

        try {
            MirthContextFactory contextFactory = contextFactoryController.getContextFactory(getResourceIds());
            Class<?> clazz = Class.forName(replacer.replaceValues(connectorProperties.getClassName(), getChannelId()), true, contextFactory.getApplicationClassLoader());

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

        String serviceName = replacer.replaceValues(connectorProperties.getServiceName(), getChannelId());
        HttpContext context = server.createContext("/services/" + serviceName);

        if (CollectionUtils.isNotEmpty(connectorProperties.getUsernames())) {
            final List<String> usernames = new ArrayList<String>(connectorProperties.getUsernames());
            final List<String> passwords = new ArrayList<String>(connectorProperties.getPasswords());
            replacer.replaceValuesInList(usernames, getChannelId());
            replacer.replaceValuesInList(passwords, getChannelId());

            context.setAuthenticator(new BasicAuthenticator("/services/" + serviceName) {
                @Override
                public boolean checkCredentials(String username, String password) {
                    if (usernames.contains(username) && passwords.get(usernames.indexOf(username)).equals(password)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        webServiceEndpoint.publish(context);

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onStop() throws ConnectorTaskException {
        try {
            logger.debug("stopping Web Service HTTP server");

            if (webServiceEndpoint != null) {
                webServiceEndpoint.stop();
            }

            if (server != null) {
                server.stop(1);
            }

            if (executor != null) {
                executor.shutdown();
            }
        } catch (Exception e) {
            throw new ConnectorTaskException("Failed to stop Web Service Listener", e);
        }
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
        onStop();
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult);
    }

    public String processData(String message) {
        return processData(new RawMessage(message));
    }

    public String processData(RawMessage rawMessage) {
        String response = null;
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.RECEIVING));

        try {
            if (isProcessBatch()) {
                try {
                    if (rawMessage.isBinary()) {
                        throw new BatchMessageException("Batch processing is not supported for binary data.");
                    }

                    BatchRawMessage batchRawMessage = new BatchRawMessage(new BatchMessageReader(rawMessage.getRawData()), rawMessage.getSourceMap());

                    ResponseHandler responseHandler = new SimpleResponseHandler();
                    dispatchBatchMessage(batchRawMessage, responseHandler);

                    DispatchResult dispatchResult = responseHandler.getResultForResponse();
                    if (dispatchResult != null && dispatchResult.getSelectedResponse() != null) {
                        response = dispatchResult.getSelectedResponse().getMessage();
                    }
                } catch (BatchMessageException e) {
                    logger.error("Error processing batch message", e);
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), rawMessage.getOriginalMessageId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error processing batch message", e));
                }
            } else {
                DispatchResult dispatchResult = null;

                try {
                    dispatchResult = dispatchRawMessage(rawMessage);
                    dispatchResult.setAttemptedResponse(true);

                    if (dispatchResult.getSelectedResponse() != null) {
                        response = dispatchResult.getSelectedResponse().getMessage();
                    }
                } catch (ChannelException e) {
                    // TODO auto-generate an error response?
                } finally {
                    // TODO: response should be returned before it is marked as finished
                    // TODO: figure out how to get the error message if an error occurred in sending the response back
                    finishDispatch(dispatchResult);
                }
            }
        } finally {
            // TODO find a way to call this after the response was sent
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
        }
        return response;
    }

    public void setServer(HttpServer server) {
        this.server = server;
    }
}