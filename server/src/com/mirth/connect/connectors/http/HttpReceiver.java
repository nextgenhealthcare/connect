/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.URIUtil;

import com.mirth.connect.connectors.http.HttpStaticResource.ResourceType;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
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
import com.mirth.connect.donkey.util.Base64Util;
import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;
import com.mirth.connect.server.controllers.ChannelController;
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
    private String[] binaryMimeTypesArray;
    private Pattern binaryMimeTypesRegex;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (HttpReceiverProperties) getConnectorProperties();

        if (connectorProperties.isXmlBody() && isProcessBatch()) {
            throw new ConnectorTaskException("Batch processing is not supported for Xml Body.");
        }

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
            throw new ConnectorTaskException(e);
        }

        String replacedBinaryMimeTypes = replacer.replaceValues(connectorProperties.getBinaryMimeTypes(), getChannelId());
        if (connectorProperties.isBinaryMimeTypesRegex()) {
            try {
                binaryMimeTypesRegex = Pattern.compile(replacedBinaryMimeTypes);
            } catch (PatternSyntaxException e) {
                throw new ConnectorTaskException("Invalid binary MIME types regular expression: " + replacedBinaryMimeTypes, e);
            }
        } else {
            binaryMimeTypesArray = StringUtils.split(replacedBinaryMimeTypes.replaceAll("\\s*,\\s*", ",").trim(), ',');
        }
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {
        configuration.configureConnectorUndeploy(this);
    }

    @Override
    public void onStart() throws ConnectorTaskException {
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

            HandlerCollection handlers = new HandlerCollection();

            // Add handlers for each static resource
            if (connectorProperties.getStaticResources() != null) {
                NavigableMap<String, List<HttpStaticResource>> staticResourcesMap = new TreeMap<String, List<HttpStaticResource>>();

                // Add each static resource to a map first to allow sorting and deduplication
                for (HttpStaticResource staticResource : connectorProperties.getStaticResources()) {
                    String resourceContextPath = replacer.replaceValues(staticResource.getContextPath(), getChannelId());
                    Map<String, Object> queryParameters = new HashMap<String, Object>();

                    // If query parameters were specified, extract them here
                    int queryIndex = resourceContextPath.indexOf('?');
                    if (queryIndex >= 0) {
                        String query = resourceContextPath.substring(queryIndex + 1);
                        resourceContextPath = resourceContextPath.substring(0, queryIndex);

                        for (NameValuePair param : URLEncodedUtils.parse(query, Charset.defaultCharset())) {
                            Object currentValue = queryParameters.get(param.getName());
                            String value = StringUtils.defaultString(param.getValue());

                            if (currentValue == null) {
                                queryParameters.put(param.getName(), value);
                            } else if (currentValue instanceof String[]) {
                                queryParameters.put(param.getName(), ArrayUtils.add((String[]) currentValue, value));
                            } else {
                                queryParameters.put(param.getName(), new String[] {
                                        (String) currentValue, value });
                            }
                        }
                    }

                    // We always want to append resources starting with "/" to the base context path
                    if (resourceContextPath.endsWith("/")) {
                        resourceContextPath = resourceContextPath.substring(0, resourceContextPath.length() - 1);
                    }
                    if (!resourceContextPath.startsWith("/")) {
                        resourceContextPath = "/" + resourceContextPath;
                    }
                    resourceContextPath = contextPath + resourceContextPath;

                    List<HttpStaticResource> staticResourcesList = staticResourcesMap.get(resourceContextPath);
                    if (staticResourcesList == null) {
                        staticResourcesList = new ArrayList<HttpStaticResource>();
                        staticResourcesMap.put(resourceContextPath, staticResourcesList);
                    }
                    staticResourcesList.add(new HttpStaticResource(resourceContextPath, staticResource.getResourceType(), staticResource.getValue(), staticResource.getContentType(), queryParameters));
                }

                // Iterate through each context path in reverse so that more specific contexts take precedence
                for (List<HttpStaticResource> staticResourcesList : staticResourcesMap.descendingMap().values()) {
                    for (HttpStaticResource staticResource : staticResourcesList) {
                        logger.debug("Adding static resource handler for context path: " + staticResource.getContextPath());
                        ContextHandler resourceContextHandler = new ContextHandler();
                        resourceContextHandler.setContextPath(staticResource.getContextPath());
                        // This allows resources to be requested without a relative context path (e.g. "/")
                        resourceContextHandler.setAllowNullPathInfo(true);
                        resourceContextHandler.setHandler(new StaticResourceHandler(staticResource));
                        handlers.addHandler(resourceContextHandler);
                    }
                }
            }

            // Add the main request handler
            ContextHandler contextHandler = new ContextHandler();
            contextHandler.setContextPath(contextPath);
            contextHandler.setHandler(new RequestHandler());
            handlers.addHandler(contextHandler);

            server.setHandler(handlers);

            logger.debug("starting HTTP server with address: " + host + ":" + port);
            server.start();
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
        } catch (Exception e) {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.FAILURE));
            throw new ConnectorTaskException("Failed to start HTTP Listener", e);
        }
    }

    @Override
    public void onStop() throws ConnectorTaskException {
        try {
            logger.debug("stopping HTTP server");
            server.stop();
        } catch (Exception e) {
            throw new ConnectorTaskException("Failed to stop HTTP Listener", e.getCause());
        }
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
        onStop();
    }

    private class RequestHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
            logger.debug("received HTTP request");
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.CONNECTED));
            DispatchResult dispatchResult = null;

            try {
                Map<String, Object> sourceMap = new HashMap<String, Object>();
                Object messageContent = null;

                try {
                    messageContent = getMessage(baseRequest, sourceMap);
                } catch (Throwable t) {
                    sendErrorResponse(servletResponse, dispatchResult, t);
                }

                if (messageContent != null) {
                    if (isProcessBatch()) {
                        if (messageContent instanceof byte[]) {
                            BatchMessageException e = new BatchMessageException("Batch processing is not supported for binary data.");
                            logger.error(e.getMessage() + " (channel: " + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + ")", e);
                            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), null, e));
                        } else {
                            try {
                                BatchRawMessage batchRawMessage = new BatchRawMessage(new BatchMessageReader((String) messageContent), sourceMap);
                                ResponseHandler responseHandler = new SimpleResponseHandler();

                                dispatchBatchMessage(batchRawMessage, responseHandler);

                                dispatchResult = responseHandler.getResultForResponse();
                                sendResponse(baseRequest, servletResponse, dispatchResult);
                            } catch (Throwable t) {
                                sendErrorResponse(servletResponse, dispatchResult, t);
                            }
                        }
                    } else {
                        try {
                            RawMessage rawMessage = null;
                            if (messageContent instanceof byte[]) {
                                rawMessage = new RawMessage((byte[]) messageContent, null, sourceMap);
                            } else {
                                rawMessage = new RawMessage((String) messageContent, null, sourceMap);
                            }

                            dispatchResult = dispatchRawMessage(rawMessage);

                            sendResponse(baseRequest, servletResponse, dispatchResult);
                        } catch (Throwable t) {
                            sendErrorResponse(servletResponse, dispatchResult, t);
                        } finally {
                            finishDispatch(dispatchResult);
                        }
                    }
                }
            } finally {
                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
            }
            baseRequest.setHandled(true);
        }
    }

    private void sendResponse(Request baseRequest, HttpServletResponse servletResponse, DispatchResult dispatchResult) throws Exception {
        servletResponse.setContentType(replacer.replaceValues(connectorProperties.getResponseContentType(), getChannelId()));

        // set the response headers
        for (Entry<String, String> entry : connectorProperties.getResponseHeaders().entrySet()) {
            servletResponse.setHeader(entry.getKey(), replaceValues(entry.getValue(), dispatchResult));
        }

        // set the status code
        int statusCode = NumberUtils.toInt(replaceValues(connectorProperties.getResponseStatusCode(), dispatchResult), -1);

        Response selectedResponse = dispatchResult.getSelectedResponse();

        /*
         * set the response body and status code (if we choose a response from the drop-down)
         */
        if (selectedResponse != null) {
            dispatchResult.setAttemptedResponse(true);

            Status newMessageStatus = selectedResponse.getStatus();

            /*
             * If the status code is custom, use the entered/replaced string If is is not a
             * variable, use the status of the destination's response (success = 200, failure = 500)
             * Otherwise, return 200
             */
            if (statusCode != -1) {
                servletResponse.setStatus(statusCode);
            } else if (newMessageStatus != null && newMessageStatus.equals(Status.ERROR)) {
                servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            } else {
                servletResponse.setStatus(HttpStatus.SC_OK);
            }

            String message = selectedResponse.getMessage();

            if (message != null) {
                OutputStream responseOutputStream = servletResponse.getOutputStream();
                byte[] responseBytes;
                if (connectorProperties.isResponseDataTypeBinary()) {
                    responseBytes = Base64Util.decodeBase64(message.getBytes("US-ASCII"));
                } else {
                    responseBytes = message.getBytes(connectorProperties.getCharset());
                }

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
            }
        } else {
            /*
             * If the status code is custom, use the entered/replaced string Otherwise, return 200
             */
            if (statusCode != -1) {
                servletResponse.setStatus(statusCode);
            } else {
                servletResponse.setStatus(HttpStatus.SC_OK);
            }
        }
    }

    private void sendErrorResponse(HttpServletResponse servletResponse, DispatchResult dispatchResult, Throwable t) throws IOException {
        String responseError = ExceptionUtils.getStackTrace(t);
        logger.error("Error receiving message (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", t);
        eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error receiving message", t));

        if (dispatchResult != null) {
            // TODO decide if we still want to send back the exception content or something else?
            dispatchResult.setAttemptedResponse(true);
            dispatchResult.setResponseError(responseError);
            // TODO get full HTTP payload with error message
            if (dispatchResult.getSelectedResponse() != null) {
                dispatchResult.getSelectedResponse().setMessage(responseError);
            }
        }

        servletResponse.setContentType("text/plain");
        servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        servletResponse.getOutputStream().write(responseError.getBytes());
    }

    private Object getMessage(Request request, Map<String, Object> sourceMap) throws IOException, ChannelException, MessagingException, DonkeyElementException, ParserConfigurationException {
        HttpRequestMessage requestMessage = new HttpRequestMessage();
        requestMessage.setMethod(request.getMethod());
        requestMessage.setHeaders(HttpMessageConverter.convertFieldEnumerationToMap(request));
        requestMessage.setParameters(extractParameters(request));

        InputStream requestInputStream = request.getInputStream();

        // If the request is GZIP encoded, uncompress the content
        String contentEncoding = requestMessage.getCaseInsensitiveHeaders().get(HTTP.CONTENT_ENCODING);
        if (contentEncoding != null && (contentEncoding.toLowerCase().equals("gzip") || contentEncoding.toLowerCase().equals("x-gzip"))) {
            requestInputStream = new GZIPInputStream(requestInputStream);
        }

        ContentType contentType;
        try {
            contentType = ContentType.parse(request.getContentType());
        } catch (RuntimeException e) {
            contentType = ContentType.TEXT_PLAIN;
        }
        requestMessage.setContentType(contentType);

        requestMessage.setRemoteAddress(StringUtils.trimToEmpty(request.getRemoteAddr()));
        requestMessage.setQueryString(StringUtils.trimToEmpty(request.getQueryString()));
        requestMessage.setRequestUrl(StringUtils.trimToEmpty(getRequestURL(request)));
        requestMessage.setContextPath(StringUtils.trimToEmpty(new URL(requestMessage.getRequestUrl()).getPath()));

        /*
         * First parse out the body of the HTTP request. Depending on the connector settings, this
         * could end up being a string encoded with the request charset, a byte array representing
         * the raw request payload, or a MimeMultipart object.
         */

        // Only parse multipart if XML Body is selected and Parse Multipart is enabled
        if (connectorProperties.isXmlBody() && connectorProperties.isParseMultipart() && ServletFileUpload.isMultipartContent(request)) {
            requestMessage.setContent(new MimeMultipart(new ByteArrayDataSource(requestInputStream, contentType.toString())));
        } else if (isBinaryContentType(contentType)) {
            requestMessage.setContent(IOUtils.toByteArray(requestInputStream));
        } else {
            requestMessage.setContent(IOUtils.toString(requestInputStream, HttpMessageConverter.getDefaultHttpCharset(request.getCharacterEncoding())));
        }

        /*
         * Now that we have the request body, we need to create the actual RawMessage message data.
         * Depending on the connector settings this could be our custom serialized XML, a Base64
         * string encoded from the raw request payload, or a string encoded from the payload with
         * the request charset.
         */
        Object rawMessageContent;

        if (connectorProperties.isXmlBody()) {
            rawMessageContent = HttpMessageConverter.httpRequestToXml(requestMessage, connectorProperties.isParseMultipart(), connectorProperties.isIncludeMetadata());
        } else {
            rawMessageContent = requestMessage.getContent();
        }

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.RECEIVING));

        sourceMap.put("remoteAddress", requestMessage.getRemoteAddress());
        sourceMap.put("remotePort", request.getRemotePort());
        sourceMap.put("localAddress", StringUtils.trimToEmpty(request.getLocalAddr()));
        sourceMap.put("localPort", request.getLocalPort());
        sourceMap.put("method", requestMessage.getMethod());
        sourceMap.put("url", requestMessage.getRequestUrl());
        sourceMap.put("uri", StringUtils.trimToEmpty(request.getUri().toString()));
        sourceMap.put("protocol", StringUtils.trimToEmpty(request.getProtocol()));
        sourceMap.put("query", requestMessage.getQueryString());
        sourceMap.put("contextPath", requestMessage.getContextPath());
        sourceMap.put("headers", Collections.unmodifiableMap(requestMessage.getCaseInsensitiveHeaders()));
        sourceMap.put("parameters", Collections.unmodifiableMap(requestMessage.getParameters()));

        // Add custom source map variables from the configuration interface
        sourceMap.putAll(configuration.getRequestInformation(request));

        return rawMessageContent;
    }

    private class StaticResourceHandler extends AbstractHandler {

        private HttpStaticResource staticResource;

        public StaticResourceHandler(HttpStaticResource staticResource) {
            this.staticResource = staticResource;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
            // Only allow GET requests, otherwise pass to the next request handler
            if (!baseRequest.getMethod().equalsIgnoreCase(HttpMethods.GET)) {
                return;
            }

            try {
                String contextPath = URLDecoder.decode(new URL(getRequestURL(baseRequest)).getPath(), "US-ASCII");
                if (contextPath.endsWith("/")) {
                    contextPath = contextPath.substring(0, contextPath.length() - 1);
                }
                logger.debug("Received static resource request at: " + contextPath);

                String value = replacer.replaceValues(staticResource.getValue(), getChannelId());
                String contentTypeString = replacer.replaceValues(staticResource.getContentType(), getChannelId());

                // If we're not reading from a directory and the context path doesn't match, pass to the next request handler
                if (staticResource.getResourceType() != ResourceType.DIRECTORY && !staticResource.getContextPath().equalsIgnoreCase(contextPath)) {
                    return;
                }

                // If the query parameters do not match, pass to the next request handler
                if (!parametersEqual(staticResource.getQueryParameters(), extractParameters(baseRequest))) {
                    return;
                }

                ContentType contentType;
                try {
                    contentType = ContentType.parse(contentTypeString);
                } catch (Exception e) {
                    contentType = ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), connectorProperties.getCharset());
                }

                Charset charset = contentType.getCharset();
                if (charset == null) {
                    charset = Charset.forName(connectorProperties.getCharset());
                }

                servletResponse.setContentType(contentType.toString());
                servletResponse.setStatus(HttpStatus.SC_OK);

                OutputStream responseOutputStream = servletResponse.getOutputStream();

                // If the client accepts GZIP compression, compress the content
                String acceptEncoding = baseRequest.getHeader("Accept-Encoding");
                if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                    servletResponse.setHeader(HTTP.CONTENT_ENCODING, "gzip");
                    responseOutputStream = new GZIPOutputStream(responseOutputStream);
                }

                if (staticResource.getResourceType() == ResourceType.FILE) {
                    // Just stream the file itself back to the client
                    IOUtils.copy(new FileInputStream(value), responseOutputStream);
                } else if (staticResource.getResourceType() == ResourceType.DIRECTORY) {
                    File file = new File(value);

                    if (file.isDirectory()) {
                        // Use the trailing path as the child path for the actual resource directory
                        String childPath = StringUtils.removeStartIgnoreCase(contextPath, staticResource.getContextPath());
                        if (childPath.startsWith("/")) {
                            childPath = childPath.substring(1);
                        }

                        if (!childPath.contains("/")) {
                            file = new File(file, childPath);
                        } else {
                            // If a subdirectory is specified, pass to the next request handler
                            servletResponse.reset();
                            return;
                        }
                    } else {
                        throw new Exception("File \"" + file.toString() + "\" does not exist or is not a directory.");
                    }

                    if (file.exists()) {
                        if (file.isDirectory()) {
                            // The directory itself was requested, instead of a specific file
                            servletResponse.reset();
                            return;
                        }

                        // A valid file was found; stream it back to the client
                        IOUtils.copy(new FileInputStream(file), responseOutputStream);
                    } else {
                        // File does not exist, pass to the next request handler
                        servletResponse.reset();
                        return;
                    }
                } else {
                    // Stream the value string back to the client
                    IOUtils.write(value, responseOutputStream, charset);
                }

                // If we gzipped, we need to finish the stream now
                if (responseOutputStream instanceof GZIPOutputStream) {
                    ((GZIPOutputStream) responseOutputStream).finish();
                }
            } catch (Throwable t) {
                logger.error("Error handling static HTTP resource request (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", t);
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error handling static HTTP resource request", t));

                servletResponse.reset();
                servletResponse.setContentType(ContentType.TEXT_PLAIN.toString());
                servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                servletResponse.getOutputStream().write(ExceptionUtils.getStackTrace(t).getBytes());
            }

            baseRequest.setHandled(true);
        }
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

    private Map<String, Object> extractParameters(Request request) {
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

        return parameterMap;
    }

    private boolean parametersEqual(Map<String, Object> params1, Map<String, Object> params2) {
        if (!params1.keySet().equals(params2.keySet())) {
            return false;
        }

        for (Entry<String, Object> entry : params1.entrySet()) {
            Object value1 = entry.getValue();
            Object value2 = params2.get(entry.getKey());

            if (value1 != null && value1 instanceof String[] && value2 != null && value2 instanceof String[]) {
                if (!Arrays.equals((String[]) value1, (String[]) value2)) {
                    return false;
                }
            } else {
                if (!ObjectUtils.equals(value1, value2)) {
                    return false;
                }
            }
        }

        return true;
    }

    private String getRequestURL(Request request) {
        String requestURL = request.getRequestURL().toString();

        try {
            // Verify whether the URL is valid
            new URL(requestURL);
        } catch (MalformedURLException e) {
            // The request URL returned by Jetty is invalid, so build it up without the URI instead
            StringBuilder builder = new StringBuilder();
            String scheme = request.getScheme();
            int port = request.getServerPort();

            builder.append(scheme);
            builder.append("://");
            builder.append(request.getServerName());

            // Don't include port 80 if HTTP, or port 443 if HTTPS
            if ((scheme.equalsIgnoreCase(URIUtil.HTTP) && port != 80) || (scheme.equalsIgnoreCase(URIUtil.HTTPS) && port != 443)) {
                builder.append(':');
                builder.append(port);
            }

            requestURL = builder.toString();
        }

        return requestURL;
    }

    private boolean isBinaryContentType(ContentType contentType) {
        String mimeType = contentType.getMimeType();

        if (connectorProperties.isBinaryMimeTypesRegex()) {
            return binaryMimeTypesRegex.matcher(mimeType).matches();
        } else {
            return StringUtils.startsWithAny(mimeType, binaryMimeTypesArray);
        }
    }
}