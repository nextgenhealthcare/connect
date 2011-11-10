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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.Response;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.mule.transformers.JavaScriptPostprocessor;

public class HttpMessageReceiver extends AbstractMessageReceiver {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpConnector connector;
    private ConnectorType connectorType = ConnectorType.LISTENER;
    private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();

    private Server server = null;

    private class RequestHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
            logger.debug("received HTTP request");
            monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);

            try {
                servletResponse.setContentType(connector.getReceiverResponseContentType());
                MessageObject messageObjectResponse = processData(baseRequest);

                if (messageObjectResponse != null) {
                    // set the response headers
                    for (Entry<String, String> entry : connector.getReceiverResponseHeaders().entrySet()) {
                        servletResponse.setHeader(entry.getKey(), replacer.replaceValues(entry.getValue(), messageObjectResponse));
                    }

                    // set the status code
                    int statusCode = NumberUtils.toInt(replacer.replaceValues(connector.getReceiverResponseStatusCode(), messageObjectResponse), -1);

                    /*
                     * set the response body and status code (if we choose a
                     * response from the drop-down)
                     */
                    if (!connector.getReceiverResponse().equalsIgnoreCase("None")) {
                        Response destinationResponse = (Response) messageObjectResponse.getResponseMap().get(connector.getReceiverResponse());
                        servletResponse.getOutputStream().write(destinationResponse.getMessage().getBytes(connector.getReceiverCharset()));

                        /*
                         * If the status code is custom, use the
                         * entered/replaced string
                         * If is is not a variable, use the status of the
                         * destination's response (success = 200, failure = 500)
                         * Otherwise, return 200
                         */
                        if (statusCode != -1) {
                            servletResponse.setStatus(statusCode);
                        } else if (destinationResponse.getStatus().equals(Response.Status.FAILURE)) {
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
                } else {
                    servletResponse.setStatus(HttpStatus.SC_OK);
                }
            } catch (Exception e) {
                servletResponse.setContentType("text/plain");
                servletResponse.getOutputStream().write(ExceptionUtils.getFullStackTrace(e).getBytes());
                servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            } finally {
                monitoringController.updateStatus(connector, connectorType, Event.DONE);
            }

            baseRequest.setHandled(true);
        }
    };

    public HttpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
        super(connector, component, endpoint);
        this.connector = (HttpConnector) connector;
    }

    @Override
    public void doConnect() throws Exception {
        server = new Server();
        connector.getConfiguration().configureReceiver(server, endpoint, NumberUtils.toInt(replacer.replaceValues(connector.getReceiverTimeout()), 0));

        // add the request handler
        ContextHandler contextHandler = new ContextHandler();

        // Initialize contextPath to "" or its value after replacements
        String contextPath = (connector.getReceiverContextPath() == null ? "" : replacer.replaceValues(connector.getReceiverContextPath()));

        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        contextHandler.setContextPath(contextPath);
        contextHandler.setHandler(new RequestHandler());
        server.setHandler(contextHandler);

        logger.debug("starting HTTP server with address: " + endpoint.getEndpointURI().getUri());
        server.start();
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    private MessageObject processData(Request request) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        HttpMessageConverter converter = new HttpMessageConverter();

        HttpRequestMessage message = new HttpRequestMessage();
        message.setMethod(request.getMethod());
        message.setHeaders(converter.convertFieldEnumerationToMap(request));

        /*
         * XXX: extractParameters must be called before the parameters are
         * accessed, otherwise the map will be null.
         */
        request.extractParameters();
        message.setParameters(request.getParameters());

        message.setContent(IOUtils.toString(request.getInputStream(), converter.getDefaultHttpCharset(request.getCharacterEncoding())));
        message.setIncludeHeaders(!connector.isReceiverBodyOnly());
        message.setContentType(request.getContentType());
        message.setRemoteAddress(request.getRemoteAddr());
        message.setQueryString(request.getQueryString());
        message.setRequestUrl(request.getRequestURL().toString());

        UMOMessage response = routeMessage(new MuleMessage(connector.getMessageAdapter(message)), endpoint.isSynchronous());

        if ((response != null) && (response instanceof MuleMessage)) {
            Object payload = response.getPayload();

            if (payload instanceof MessageObject) {
                MessageObject messageObjectResponse = (MessageObject) payload;
                postProcessor.doPostProcess(messageObjectResponse);
                return messageObjectResponse;
            }
        }

        return null;
    }

    @Override
    public void doDisconnect() throws Exception {

    }

    @Override
    public void doStop() throws UMOException {
        super.doStop();

        try {
            logger.debug("stopping HTTP server");
            server.stop();
        } catch (Exception e) {
            throw new MuleException(new Message(Messages.FAILED_TO_STOP_X, "HTTP Listener"), e.getCause());
        }
    }

}
