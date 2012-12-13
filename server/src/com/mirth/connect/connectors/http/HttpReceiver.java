/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class HttpReceiver extends SourceConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpReceiverProperties connectorProperties;
    private ConnectorType connectorType = ConnectorType.LISTENER;
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();

    private Server server = null;
    private HttpConfiguration configuration = null;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (HttpReceiverProperties) getConnectorProperties();

        // load the default configuration
        String configurationClass = configurationController.getProperty(connectorProperties.getProtocol(), "configurationClass");

        try {
            configuration = (HttpConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Exception e) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultHttpConfiguration();
        }

        configuration.configureConnector(connectorProperties);
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {
        String host = replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getHost());
        int port = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getPort()));
        int timeout = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getTimeout()), 0);

        // Initialize contextPath to "" or its value after replacements
        String contextPath = (connectorProperties.getContextPath() == null ? "" : replacer.replaceValues(connectorProperties.getContextPath()));

        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        try {
            server = new Server();
            configuration.configureReceiver(server, host, port, timeout);

            // add the request handler
            ContextHandler contextHandler = new ContextHandler();
            contextHandler.setContextPath(contextPath);
            contextHandler.setHandler(new RequestHandler());
            server.setHandler(contextHandler);

            logger.debug("starting HTTP server with address: " + host + ":" + port);
            server.start();
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.INITIALIZED);
        } catch (Exception e) {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.FAILURE);
            throw new StartException("Failed to start HTTP Listener", e);
        }
    }

    @Override
    public void onStop() throws StopException {
        try {
            logger.debug("stopping HTTP server");
            server.stop();
        } catch (Exception e) {
            throw new StopException("Failed to stop HTTP Listener", e.getCause());
        }
    }

    private class RequestHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
            logger.debug("received HTTP request");
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.CONNECTED);
            DispatchResult dispatchResult = null;
            String sentResponse = null;
            boolean attemptedResponse = false;
            String responseError = null;
            
            try {
                dispatchResult = processData(baseRequest);
                
                servletResponse.setContentType(replacer.replaceValues(connectorProperties.getResponseContentType()));

                // set the response headers
                for (Entry<String, String> entry : connectorProperties.getResponseHeaders().entrySet()) {
                    servletResponse.setHeader(entry.getKey(), replaceValues(entry.getValue(), dispatchResult));
                }

                // set the status code
                int statusCode = NumberUtils.toInt(replaceValues(connectorProperties.getResponseStatusCode(), dispatchResult), -1);

                Response selectedResponse = dispatchResult.getSelectedResponse();
                
                /*
                 * set the response body and status code (if we choose a
                 * response from the drop-down)
                 */
                if (selectedResponse != null) {
                    attemptedResponse = true;
                    String message = selectedResponse.getMessage();
                    
                    if (message != null) {
                        servletResponse.getOutputStream().write(message.getBytes(connectorProperties.getCharset()));

                        // TODO include full HTTP payload in sentResponse
                        sentResponse = message;
                    }
                    
                    Status newMessageStatus = selectedResponse.getNewMessageStatus();
                    
                    /*
                     * If the status code is custom, use the
                     * entered/replaced string
                     * If is is not a variable, use the status of the
                     * destination's response (success = 200, failure = 500)
                     * Otherwise, return 200
                     */
                    if (statusCode != -1) {
                        servletResponse.setStatus(statusCode);
                    } else if (newMessageStatus != null && newMessageStatus.equals(Status.ERROR)) {
                        servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    } else {
                        servletResponse.setStatus(HttpStatus.SC_OK);
                    }
                } else {
                    /*
                     * If the status code is custom, use the
                     * entered/replaced string
                     * Otherwise, return 200
                     */
                    if (statusCode != -1) {
                        servletResponse.setStatus(statusCode);
                    } else {
                        servletResponse.setStatus(HttpStatus.SC_OK);
                    }
                }
            } catch (Exception e) {
                responseError = ExceptionUtils.getStackTrace(e);
                
                // TODO decide if we still want to send back the exception content or something else?
                attemptedResponse = true;
                servletResponse.setContentType("text/plain");
                servletResponse.getOutputStream().write(responseError.getBytes());
                servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                
                // TODO get full HTTP payload with error message
                sentResponse = responseError;
            } finally {
                try {
                    finishDispatch(dispatchResult, attemptedResponse, sentResponse, responseError);
                } finally {
                    monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
                }
            }

            baseRequest.setHandled(true);
        }
    };

    private DispatchResult processData(Request request) throws IOException, ChannelException {
        HttpMessageConverter converter = new HttpMessageConverter();

        HttpRequestMessage requestMessage = new HttpRequestMessage();
        requestMessage.setMethod(request.getMethod());
        requestMessage.setHeaders(converter.convertFieldEnumerationToMap(request));

        /*
         * XXX: extractParameters must be called before the parameters are
         * accessed, otherwise the map will be null.
         */
        request.extractParameters();
        requestMessage.setParameters(request.getParameters());

        requestMessage.setContent(IOUtils.toString(request.getInputStream(), converter.getDefaultHttpCharset(request.getCharacterEncoding())));
        requestMessage.setIncludeHeaders(!connectorProperties.isBodyOnly());
        requestMessage.setContentType(request.getContentType());
        requestMessage.setRemoteAddress(request.getRemoteAddr());
        requestMessage.setQueryString(request.getQueryString());
        requestMessage.setRequestUrl(request.getRequestURL().toString());

        String rawMessageContent;

        if (requestMessage.isIncludeHeaders()) {
            rawMessageContent = new HttpMessageConverter().httpRequestToXml(requestMessage);
        } else {
            rawMessageContent = requestMessage.getContent();
        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY);
        
        return dispatchRawMessage(new RawMessage(rawMessageContent));
    }

    private String replaceValues(String template, DispatchResult dispatchResult) {
        ConnectorMessage mergedConnectorMessage = null;

        if (dispatchResult.getProcessedMessage() != null) {
            mergedConnectorMessage = dispatchResult.getProcessedMessage().getMergedConnectorMessage();
        }

        return (mergedConnectorMessage == null ? replacer.replaceValues(template, getChannelId()) : replacer.replaceValues(template, mergedConnectorMessage));
    }

	@Override
	public void handleRecoveredResponse(DispatchResult dispatchResult) {
		finishDispatch(dispatchResult);
	}
}
