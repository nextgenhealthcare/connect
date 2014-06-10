/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;

import edu.emory.mathcs.backport.java.util.Collections;

public class HttpReceiver extends SourceConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpReceiverProperties connectorProperties;
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private final TemplateValueReplacer replacer = new TemplateValueReplacer();
    private HttpConfiguration configuration = null;
    private Server server;
    private String host;
    private int port;
    private int timeout;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (HttpReceiverProperties) getConnectorProperties();

        // load the default configuration
        String configurationClass = configurationController.getProperty(connectorProperties.getProtocol(), "httpConfigurationClass");

        try {
            configuration = (HttpConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Exception e) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultHttpConfiguration();
        }

        try {
            configuration.configureConnectorDeploy(this);
        } catch (Exception e) {
            throw new DeployException(e);
        }
    }

    @Override
    public void onUndeploy() throws UndeployException {
        configuration.configureConnectorUndeploy(this);
    }

    @Override
    public void onStart() throws StartException {
        host = replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getHost(), getChannelId());
        port = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getPort(), getChannelId()));
        timeout = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getTimeout(), getChannelId()), 0);

        // Initialize contextPath to "" or its value after replacements
        String contextPath = (connectorProperties.getContextPath() == null ? "" : replacer.replaceValues(connectorProperties.getContextPath(), getChannelId())).trim();

        /*
         * Empty string and "/" are both valid and equal functionally. However if there is a
         * resource defined, we need to make sure that the context path starts with a slash and
         * doesn't end with one.
         */
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }

        try {
            server = new Server();
            configuration.configureReceiver(this);

            // add the request handler
            ContextHandler contextHandler = new ContextHandler();
            contextHandler.setContextPath(contextPath);
            contextHandler.setHandler(new RequestHandler());
            server.setHandler(contextHandler);

            logger.debug("starting HTTP server with address: " + host + ":" + port);
            server.start();
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
        } catch (Exception e) {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.FAILURE));
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

    @Override
    public void onHalt() throws HaltException {
        try {
            onStop();
        } catch (StopException e) {
            throw new HaltException(e);
        }
    }

    private class RequestHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
            logger.debug("received HTTP request");
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.CONNECTED));
            DispatchResult dispatchResult = null;
            String sentResponse = null;
            boolean attemptedResponse = false;
            String responseError = null;

            try {
                dispatchResult = processData(baseRequest);

                servletResponse.setContentType(replacer.replaceValues(connectorProperties.getResponseContentType(), getChannelId()));

                // set the response headers
                for (Entry<String, String> entry : connectorProperties.getResponseHeaders().entrySet()) {
                    servletResponse.setHeader(entry.getKey(), replaceValues(entry.getValue(), dispatchResult));
                }

                // set the status code
                int statusCode = NumberUtils.toInt(replaceValues(connectorProperties.getResponseStatusCode(), dispatchResult), -1);

                Response selectedResponse = dispatchResult.getSelectedResponse();

                /*
                 * set the response body and status code (if we choose a response from the
                 * drop-down)
                 */
                if (selectedResponse != null) {
                    attemptedResponse = true;
                    String message = selectedResponse.getMessage();

                    if (message != null) {
                        OutputStream responseOutputStream = servletResponse.getOutputStream();
                        byte[] responseBytes = message.getBytes(connectorProperties.getCharset());

                        // If the client accepts GZIP compression, compress the content
                        String acceptEncoding = baseRequest.getHeader("Accept-Encoding");
                        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                            servletResponse.setHeader(HTTP.CONTENT_ENCODING, "gzip");
                            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(responseOutputStream);
                            gzipOutputStream.write(responseBytes);
                            gzipOutputStream.finish();
                        } else {
                            responseOutputStream.write(responseBytes);
                        }

                        // TODO include full HTTP payload in sentResponse
                        sentResponse = message;
                    }

                    Status newMessageStatus = selectedResponse.getStatus();

                    /*
                     * If the status code is custom, use the entered/replaced string If is is not a
                     * variable, use the status of the destination's response (success = 200,
                     * failure = 500) Otherwise, return 200
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
                     * If the status code is custom, use the entered/replaced string Otherwise,
                     * return 200
                     */
                    if (statusCode != -1) {
                        servletResponse.setStatus(statusCode);
                    } else {
                        servletResponse.setStatus(HttpStatus.SC_OK);
                    }
                }
            } catch (Throwable t) {
                responseError = ExceptionUtils.getStackTrace(t);
                logger.error("Error receiving message (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", t);
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error receiving message", t));

                // TODO decide if we still want to send back the exception content or something else?
                attemptedResponse = true;
                servletResponse.setContentType("text/plain");
                servletResponse.getOutputStream().write(responseError.getBytes());
                servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

                // TODO get full HTTP payload with error message
                dispatchResult.getSelectedResponse().setMessage(responseError);
            } finally {
                try {
                    finishDispatch(dispatchResult, attemptedResponse, responseError);
                } finally {
                    eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
                }
            }

            baseRequest.setHandled(true);
        }
    };

    private DispatchResult processData(Request request) throws IOException, ChannelException, MessagingException, DonkeyElementException, ParserConfigurationException {
        HttpRequestMessage requestMessage = new HttpRequestMessage();
        requestMessage.setMethod(request.getMethod());
        requestMessage.setHeaders(HttpMessageConverter.convertFieldEnumerationToMap(request));

        /*
         * XXX: extractParameters must be called before the parameters are accessed, otherwise the
         * map will be null.
         */
        request.extractParameters();
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        for (Entry<String, Object> entry : request.getParameters().entrySet()) {
            if (entry.getValue() instanceof List<?>) {
                String name = entry.getKey();
                int index = name.indexOf("[]");
                if (index >= 0) {
                    name = name.substring(0, index);
                }
                List<String> list = (List<String>) entry.getValue();
                parameterMap.put(name, list.toArray(new String[list.size()]));
            } else {
                parameterMap.put(entry.getKey(), entry.getValue().toString());
            }
        }

        requestMessage.setParameters(parameterMap);

        InputStream requestInputStream = request.getInputStream();

        // If the request is GZIP encoded, uncompress the content
        String contentEncoding = requestMessage.getCaseInsensitiveHeaders().get(HTTP.CONTENT_ENCODING);
        if (contentEncoding != null && (contentEncoding.toLowerCase().equals("gzip") || contentEncoding.toLowerCase().equals("x-gzip"))) {
            requestInputStream = new GZIPInputStream(requestInputStream);
        }

        // Only parse multipart if XML Body is selected and Parse Multipart is enabled
        if (connectorProperties.isXmlBody() && connectorProperties.isParseMultipart() && ServletFileUpload.isMultipartContent(request)) {
            requestMessage.setContent(new MimeMultipart(new ByteArrayDataSource(requestInputStream, request.getContentType())));
        } else {
            requestMessage.setContent(IOUtils.toString(requestInputStream, HttpMessageConverter.getDefaultHttpCharset(request.getCharacterEncoding())));
        }

        ContentType contentType;
        try {
            contentType = ContentType.parse(request.getContentType());
        } catch (RuntimeException e) {
            contentType = ContentType.TEXT_PLAIN;
        }

        requestMessage.setContentType(contentType);
        requestMessage.setRemoteAddress(request.getRemoteAddr());
        requestMessage.setQueryString(request.getQueryString());
        requestMessage.setRequestUrl(request.getRequestURL().toString());
        requestMessage.setContextPath(new URL(requestMessage.getRequestUrl()).getPath());

        String rawMessageContent;

        if (connectorProperties.isXmlBody()) {
            rawMessageContent = HttpMessageConverter.httpRequestToXml(requestMessage, connectorProperties.isParseMultipart(), connectorProperties.isIncludeMetadata());
        } else {
            rawMessageContent = (String) requestMessage.getContent();
        }

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.RECEIVING));

        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put("remoteAddress", requestMessage.getRemoteAddress());
        sourceMap.put("remotePort", request.getRemotePort());
        sourceMap.put("localAddress", request.getLocalAddr());
        sourceMap.put("localPort", request.getLocalPort());
        sourceMap.put("method", requestMessage.getMethod());
        sourceMap.put("url", requestMessage.getRequestUrl());
        sourceMap.put("query", StringUtils.trimToEmpty(requestMessage.getQueryString()));
        sourceMap.put("contextPath", requestMessage.getContextPath());
        sourceMap.put("headers", Collections.unmodifiableMap(requestMessage.getCaseInsensitiveHeaders()));
        sourceMap.put("parameters", Collections.unmodifiableMap(requestMessage.getParameters()));

        return dispatchRawMessage(new RawMessage(rawMessageContent, null, sourceMap));
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

    public Server getServer() {
        return server;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }
}