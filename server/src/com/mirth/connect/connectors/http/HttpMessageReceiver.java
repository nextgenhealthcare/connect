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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
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

    private Server server = null;

    private class RequestHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
            logger.debug("received HTTP request");
            monitoringController.updateStatus(connector, connectorType, Event.CONNECTED);

            try {
                servletResponse.setContentType(connector.getReceiverResponseContentType());
                Response response = processData(baseRequest);

                if (response != null) {
                    servletResponse.getOutputStream().write(response.getMessage().getBytes(connector.getReceiverCharset()));

                    /*
                     * If the destination sends a failure response, the listener
                     * should return a 500 error, otherwise 200.
                     */
                    if (response.getStatus().equals(Response.Status.FAILURE)) {
                        servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    } else {
                        servletResponse.setStatus(HttpStatus.SC_OK);
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
        connector.getConfiguration().configureReceiver(server, endpoint);

        // add the request handler
        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath(StringUtils.defaultString(connector.getReceiverContextPath(), "/"));
        contextHandler.setHandler(new RequestHandler());
        server.setHandler(contextHandler);

        logger.debug("starting HTTP server with address: " + endpoint.getEndpointURI().getUri());
        server.start();
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    private Response processData(Request request) throws Exception {
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

                if (!connector.getReceiverResponse().equalsIgnoreCase("None")) {
                    return (Response) messageObjectResponse.getResponseMap().get(connector.getReceiverResponse());
                }
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
