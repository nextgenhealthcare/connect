/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
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
import com.mirth.connect.plugins.httpauth.AuthenticationResult;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.AuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.AuthenticatorProviderFactory;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties.AuthType;
import com.mirth.connect.plugins.httpauth.RequestInfo;
import com.mirth.connect.plugins.httpauth.RequestInfo.EntityProvider;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpServer;

public class WebServiceReceiver extends SourceConnector {
    // This determines how many client requests can queue up while waiting for the server socket to accept
    private static final int DEFAULT_BACKLOG = 256;

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
    private HttpAuthConnectorPluginProperties authProps;
    private AuthenticatorProvider authenticatorProvider;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (WebServiceReceiverProperties) getConnectorProperties();

        // load the default configuration
        String configurationClass = getConfigurationClass();

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

        if (connectorProperties.getPluginProperties() != null) {
            for (ConnectorPluginProperties pluginProperties : connectorProperties.getPluginProperties()) {
                if (pluginProperties instanceof HttpAuthConnectorPluginProperties) {
                    authProps = (HttpAuthConnectorPluginProperties) pluginProperties;
                }
            }
        }

        if (authProps != null && authProps.getAuthType() != AuthType.NONE) {
            try {
                authenticatorProvider = AuthenticatorProviderFactory.getAuthenticatorProvider(this, authProps);
            } catch (Exception e) {
                throw new ConnectorTaskException("Error creating authenticator provider.", e);
            }
        }
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {
        configuration.configureConnectorUndeploy(this);
    }

    @Override
    public void onStart() throws ConnectorTaskException {
        String channelId = getChannelId();
        String channelName = getChannel().getName();
        String host = replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getHost(), channelId, channelName);
        int port = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getPort(), channelId, channelName));

        logger.debug("starting Web Service HTTP server on port: " + port);

        java.util.logging.Logger.getLogger("javax.enterprise.resource.webservices.jaxws.server").setLevel(java.util.logging.Level.OFF);

        try {
            configuration.configureReceiver(this);
            server.bind(new InetSocketAddress(host, port), DEFAULT_BACKLOG);
        } catch (Exception e) {
            throw new ConnectorTaskException("Error creating HTTP Server.", e);
        }

        // TODO: Make a max connections property for this
        int processingThreads = connectorProperties.getSourceConnectorProperties().getProcessingThreads();
        if (processingThreads < 1) {
            processingThreads = 1;
        }

        // Allow more than the channel processing threads so WDSL requests can be accepted even if all processing threads are busy
        executor = Executors.newFixedThreadPool(processingThreads + 4);
        server.setExecutor(executor);
        server.start();

        AcceptMessage acceptMessageWebService = null;

        // Store the current context classloader so we can restore it later
        ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            try {
                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(getResourceIds());

                // Set the current thread context classloader in case custom web service classes need it 
                Thread.currentThread().setContextClassLoader(contextFactory.getApplicationClassLoader());

                Class<?> clazz = Class.forName(replacer.replaceValues(connectorProperties.getClassName(), channelId, channelName), true, contextFactory.getApplicationClassLoader());

                if (clazz.getSuperclass().equals(AcceptMessage.class)) {
                    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                    for (int i = 0; i < constructors.length; i++) {
                        Class<?>[] parameters = constructors[i].getParameterTypes();
                        if ((parameters.length == 1) && parameters[0].equals(this.getClass())) {
                            acceptMessageWebService = (AcceptMessage) constructors[i].newInstance(new Object[] {
                                    this });
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

            webServiceEndpoint = Endpoint.create(connectorProperties.getSoapBinding().getValue(), acceptMessageWebService);
            Binding binding = webServiceEndpoint.getBinding();
            List<Handler> handlerChain = new LinkedList<Handler>();
            handlerChain.add(new LoggingSOAPHandler(this));
            binding.setHandlerChain(handlerChain);

            String serviceName = replacer.replaceValues(connectorProperties.getServiceName(), channelId, channelName);
            HttpContext context = server.createContext("/services/" + serviceName);

            // Set a security authenticator if needed
            if (authenticatorProvider != null) {
                context.setAuthenticator(createAuthenticator());
            }

            webServiceEndpoint.publish(context);
        } finally {
            // Restore the thread context classloader
            Thread.currentThread().setContextClassLoader(currentContextClassLoader);
        }

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onStop() throws ConnectorTaskException {
        ConnectorTaskException firstCause = null;

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
            firstCause = new ConnectorTaskException("Failed to stop Web Service Listener", e);
        }

        if (authenticatorProvider != null) {
            authenticatorProvider.shutdown();
        }

        if (firstCause != null) {
            throw firstCause;
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

    @Override
    protected String getConfigurationClass() {
        return configurationController.getProperty(connectorProperties.getProtocol(), "wsConfigurationClass");
    }

    public String processData(String message) {
        return processData(new RawMessage(message));
    }

    public String processData(RawMessage rawMessage) {
        DispatchResult dispatchResult = processDataAndGetDispatchResult(rawMessage);
        if (dispatchResult != null && dispatchResult.getSelectedResponse() != null) {
            return dispatchResult.getSelectedResponse().getMessage();
        }
        return null;
    }

    public DispatchResult processDataAndGetDispatchResult(String message) {
        return processDataAndGetDispatchResult(new RawMessage(message));
    }

    public DispatchResult processDataAndGetDispatchResult(RawMessage rawMessage) {
        DispatchResult dispatchResult = null;
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.RECEIVING));
        String originalThreadName = Thread.currentThread().getName();

        try {
            Thread.currentThread().setName("Web Service Receiver Thread on " + getChannel().getName() + " (" + getChannelId() + ") < " + originalThreadName);

            if (isProcessBatch()) {
                try {
                    if (rawMessage.isBinary()) {
                        throw new BatchMessageException("Batch processing is not supported for binary data.");
                    }

                    BatchRawMessage batchRawMessage = new BatchRawMessage(new BatchMessageReader(rawMessage.getRawData()), rawMessage.getSourceMap());

                    ResponseHandler responseHandler = new SimpleResponseHandler();
                    dispatchBatchMessage(batchRawMessage, responseHandler);

                    dispatchResult = responseHandler.getResultForResponse();
                } catch (BatchMessageException e) {
                    logger.error("Error processing batch message", e);
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), rawMessage.getOriginalMessageId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error processing batch message", e));
                }
            } else {
                try {
                    dispatchResult = dispatchRawMessage(rawMessage);
                    dispatchResult.setAttemptedResponse(true);
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
            Thread.currentThread().setName(originalThreadName);
        }

        return dispatchResult;
    }

    public void setServer(HttpServer server) {
        this.server = server;
    }

    private com.sun.net.httpserver.Authenticator createAuthenticator() throws ConnectorTaskException {
        final Authenticator authenticator;
        try {
            authenticator = authenticatorProvider.getAuthenticator();
        } catch (Exception e) {
            throw new ConnectorTaskException("Unable to create authenticator.", e);
        }

        return new com.sun.net.httpserver.Authenticator() {
            @Override
            public Result authenticate(final HttpExchange exch) {
                String remoteAddress = StringUtils.trimToEmpty(exch.getRemoteAddress().getAddress().getHostAddress());
                int remotePort = exch.getRemoteAddress().getPort();
                String localAddress = StringUtils.trimToEmpty(exch.getLocalAddress().getAddress().getHostAddress());
                int localPort = exch.getLocalAddress().getPort();
                String protocol = StringUtils.trimToEmpty(exch.getProtocol());
                String method = StringUtils.trimToEmpty(exch.getRequestMethod());
                String requestURI = StringUtils.trimToEmpty(exch.getRequestURI().toString());
                Map<String, List<String>> headers = exch.getRequestHeaders();

                Map<String, List<String>> queryParameters = new LinkedHashMap<String, List<String>>();
                for (NameValuePair nvp : URLEncodedUtils.parse(exch.getRequestURI(), "UTF-8")) {
                    List<String> list = queryParameters.get(nvp.getName());
                    if (list == null) {
                        list = new ArrayList<String>();
                        queryParameters.put(nvp.getName(), list);
                    }
                    list.add(nvp.getValue());
                }

                EntityProvider entityProvider = new EntityProvider() {
                    @Override
                    public byte[] getEntity() throws IOException {
                        byte[] entity = (byte[]) exch.getAttribute(ATTRIBUTE_NAME);
                        if (entity == null) {
                            entity = IOUtils.toByteArray(exch.getRequestBody());
                            exch.setAttribute(ATTRIBUTE_NAME, entity);
                            exch.setStreams(new ByteArrayInputStream(entity), exch.getResponseBody());
                        }
                        return entity;
                    }
                };

                RequestInfo requestInfo = new RequestInfo(remoteAddress, remotePort, localAddress, localPort, protocol, method, requestURI, headers, queryParameters, entityProvider);

                try {
                    AuthenticationResult result = authenticator.authenticate(requestInfo);

                    for (Entry<String, List<String>> entry : result.getResponseHeaders().entrySet()) {
                        if (StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null) {
                            for (int i = 0; i < entry.getValue().size(); i++) {
                                if (i == 0) {
                                    exch.getResponseHeaders().set(entry.getKey(), entry.getValue().get(i));
                                } else {
                                    exch.getResponseHeaders().add(entry.getKey(), entry.getValue().get(i));
                                }
                            }
                        }
                    }

                    switch (result.getStatus()) {
                        case CHALLENGED:
                            return new com.sun.net.httpserver.Authenticator.Retry(HttpServletResponse.SC_UNAUTHORIZED);
                        case SUCCESS:
                            String username = StringUtils.trimToEmpty(result.getUsername());
                            String realm = StringUtils.trimToEmpty(result.getRealm());
                            return new com.sun.net.httpserver.Authenticator.Success(new HttpPrincipal(username, realm));
                        case FAILURE:
                        default:
                            return new com.sun.net.httpserver.Authenticator.Failure(HttpServletResponse.SC_UNAUTHORIZED);

                    }
                } catch (Throwable t) {
                    logger.error("Error in HTTP authentication for " + connectorProperties.getName() + " (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", t);
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), null, ErrorEventType.DESTINATION_CONNECTOR, "Source", connectorProperties.getName(), "Error in HTTP authentication for " + connectorProperties.getName(), t));
                    return new com.sun.net.httpserver.Authenticator.Failure(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        };
    }
}