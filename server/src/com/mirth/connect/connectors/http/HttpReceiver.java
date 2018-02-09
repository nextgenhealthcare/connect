/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.security.AbstractLoginService.UserPrincipal;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.security.Constraint;

import com.mirth.connect.connectors.http.HttpStaticResource.ResourceType;
import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
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
import com.mirth.connect.plugins.httpauth.AuthenticationResult;
import com.mirth.connect.plugins.httpauth.Authenticator;
import com.mirth.connect.plugins.httpauth.AuthenticatorProvider;
import com.mirth.connect.plugins.httpauth.AuthenticatorProviderFactory;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties.AuthType;
import com.mirth.connect.plugins.httpauth.RequestInfo;
import com.mirth.connect.plugins.httpauth.RequestInfo.EntityProvider;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.userutil.MessageHeaders;
import com.mirth.connect.userutil.MessageParameters;
import com.mirth.connect.util.CharsetUtils;

public class HttpReceiver extends SourceConnector implements BinaryContentTypeResolver {
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
    private HttpAuthConnectorPluginProperties authProps;
    private AuthenticatorProvider authenticatorProvider;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (HttpReceiverProperties) getConnectorProperties();

        if (connectorProperties.isXmlBody() && isProcessBatch()) {
            throw new ConnectorTaskException("Batch processing is not supported for Xml Body.");
        }

        // load the default configuration
        String configurationClass = getConfigurationClass();

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

        String replacedBinaryMimeTypes = replacer.replaceValues(connectorProperties.getBinaryMimeTypes(), getChannelId(), getChannel().getName());
        if (connectorProperties.isBinaryMimeTypesRegex()) {
            try {
                binaryMimeTypesRegex = Pattern.compile(replacedBinaryMimeTypes);
            } catch (PatternSyntaxException e) {
                throw new ConnectorTaskException("Invalid binary MIME types regular expression: " + replacedBinaryMimeTypes, e);
            }
        } else {
            binaryMimeTypesArray = StringUtils.split(replacedBinaryMimeTypes.replaceAll("\\s*,\\s*", ",").trim(), ',');
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
        if (authenticatorProvider != null) {
            authenticatorProvider.shutdown();
        }

        configuration.configureConnectorUndeploy(this);
    }

    @Override
    public void onStart() throws ConnectorTaskException {
        String channelId = getChannelId();
        String channelName = getChannel().getName();
        host = replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getHost(), channelId, channelName);
        port = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getListenerConnectorProperties().getPort(), channelId, channelName));
        timeout = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getTimeout(), channelId, channelName), 0);

        // Initialize contextPath to "" or its value after replacements
        String contextPath = (connectorProperties.getContextPath() == null ? "" : replacer.replaceValues(connectorProperties.getContextPath(), channelId, channelName)).trim();

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
            Handler serverHandler = handlers;

            // Add handlers for each static resource
            if (connectorProperties.getStaticResources() != null) {
                NavigableMap<String, List<HttpStaticResource>> staticResourcesMap = new TreeMap<String, List<HttpStaticResource>>();

                // Add each static resource to a map first to allow sorting and deduplication
                for (HttpStaticResource staticResource : connectorProperties.getStaticResources()) {
                    String resourceContextPath = replacer.replaceValues(staticResource.getContextPath(), channelId, channelName);
                    Map<String, List<String>> queryParameters = new HashMap<String, List<String>>();

                    // If query parameters were specified, extract them here
                    int queryIndex = resourceContextPath.indexOf('?');
                    if (queryIndex >= 0) {
                        String query = resourceContextPath.substring(queryIndex + 1);
                        resourceContextPath = resourceContextPath.substring(0, queryIndex);

                        for (NameValuePair param : URLEncodedUtils.parse(query, Charset.defaultCharset())) {
                            List<String> currentValue = queryParameters.get(param.getName());
                            String value = StringUtils.defaultString(param.getValue());

                            if (currentValue == null) {
                                currentValue = new ArrayList<String>();
                                queryParameters.put(param.getName(), currentValue);
                            }
                            currentValue.add(value);
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

            // Wrap the handler collection in a security handler if needed
            if (authenticatorProvider != null) {
                serverHandler = createSecurityHandler(handlers);
            }
            server.setHandler(serverHandler);

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
        ConnectorTaskException firstCause = null;

        if (server != null) {
            try {
                logger.debug("stopping HTTP server");
                server.stop();
            } catch (Exception e) {
                firstCause = new ConnectorTaskException("Failed to stop HTTP Listener", e.getCause());
            }
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
    protected String getConfigurationClass() {
        return configurationController.getProperty(connectorProperties.getProtocol(), "httpConfigurationClass");
    }

    private class RequestHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
            logger.debug("received HTTP request");
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.CONNECTED));
            DispatchResult dispatchResult = null;
            String originalThreadName = Thread.currentThread().getName();

            try {
                Thread.currentThread().setName("HTTP Receiver Thread on " + getChannel().getName() + " (" + getChannelId() + ") < " + originalThreadName);
                Map<String, Object> sourceMap = new HashMap<String, Object>();
                List<Attachment> attachments = new ArrayList<Attachment>();
                Object messageContent = null;

                try {
                    messageContent = getMessage(baseRequest, sourceMap, attachments);
                } catch (Throwable t) {
                    sendErrorResponse(baseRequest, servletResponse, dispatchResult, t);
                }

                if (messageContent != null) {
                    if (isProcessBatch()) {
                        if (messageContent instanceof byte[]) {
                            BatchMessageException e = new BatchMessageException("Batch processing is not supported for binary data.");
                            logger.error(e.getMessage() + " (channel: " + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + ")", e);
                            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), null, ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), null, e));
                        } else {
                            try {
                                BatchRawMessage batchRawMessage = new BatchRawMessage(new BatchMessageReader((String) messageContent), sourceMap, attachments);
                                ResponseHandler responseHandler = new SimpleResponseHandler();

                                dispatchBatchMessage(batchRawMessage, responseHandler);

                                dispatchResult = responseHandler.getResultForResponse();
                                sendResponse(baseRequest, servletResponse, dispatchResult);
                            } catch (Throwable t) {
                                sendErrorResponse(baseRequest, servletResponse, dispatchResult, t);
                            }
                        }
                    } else {
                        try {
                            RawMessage rawMessage = null;
                            if (messageContent instanceof byte[]) {
                                rawMessage = new RawMessage((byte[]) messageContent, null, sourceMap, attachments);
                            } else {
                                rawMessage = new RawMessage((String) messageContent, null, sourceMap, attachments);
                            }

                            dispatchResult = dispatchRawMessage(rawMessage);

                            sendResponse(baseRequest, servletResponse, dispatchResult);
                        } catch (Throwable t) {
                            sendErrorResponse(baseRequest, servletResponse, dispatchResult, t);
                        } finally {
                            finishDispatch(dispatchResult);
                        }
                    }
                }
            } finally {
                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
                Thread.currentThread().setName(originalThreadName);
            }
            baseRequest.setHandled(true);
        }
    }

    protected void sendResponse(Request baseRequest, HttpServletResponse servletResponse, DispatchResult dispatchResult) throws Exception {
        ContentType contentType = ContentType.parse(replaceValues(connectorProperties.getResponseContentType(), dispatchResult));
        if (!connectorProperties.isResponseDataTypeBinary() && contentType.getCharset() == null) {
            /*
             * If text mode is used and a specific charset isn't already defined, use the one from
             * the connector properties. We can't use ContentType.withCharset here because it
             * doesn't preserve other parameters, like boundary definitions
             */
            contentType = ContentType.parse(contentType.toString() + "; charset=" + CharsetUtils.getEncoding(connectorProperties.getCharset()));
        }

        // Replace response headers
        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
        for (Entry<String, List<String>> entry : connectorProperties.getResponseHeaders().entrySet()) {
            String replacedKey = replaceValues(entry.getKey(), dispatchResult);

            for (String headerValue : entry.getValue()) {
                List<String> list = responseHeaders.get(replacedKey);
                if (list == null) {
                    list = new ArrayList<String>();
                    responseHeaders.put(replacedKey, list);
                }
                list.add(replaceValues(headerValue, dispatchResult));
            }
        }

        sendResponse(baseRequest, servletResponse, dispatchResult, contentType, responseHeaders, null);
    }

    protected void sendResponse(Request baseRequest, HttpServletResponse servletResponse, DispatchResult dispatchResult, ContentType contentType, Map<String, List<String>> responseHeaders, byte[] responseBytes) throws Exception {
        servletResponse.setContentType(contentType.toString());

        // set the response headers
        for (Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            for (String headerValue : entry.getValue()) {
                servletResponse.addHeader(entry.getKey(), headerValue);
            }
        }

        // set the status code
        int statusCode = NumberUtils.toInt(replaceValues(connectorProperties.getResponseStatusCode(), dispatchResult), -1);

        /*
         * set the response body and status code (if we choose a response from the drop-down)
         */
        if (dispatchResult != null && dispatchResult.getSelectedResponse() != null) {
            dispatchResult.setAttemptedResponse(true);

            Response selectedResponse = dispatchResult.getSelectedResponse();
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

            if (message != null || responseBytes != null) {
                OutputStream responseOutputStream = servletResponse.getOutputStream();
                if (responseBytes == null) {
                    if (connectorProperties.isResponseDataTypeBinary()) {
                        responseBytes = Base64Util.decodeBase64(message.getBytes("US-ASCII"));
                    } else {
                        responseBytes = message.getBytes(CharsetUtils.getEncoding(connectorProperties.getCharset()));
                    }
                }

                // If the client accepts GZIP compression, compress the content
                boolean gzipResponse = false;
                for (Enumeration<String> en = baseRequest.getHeaders("Accept-Encoding"); en.hasMoreElements();) {
                    String acceptEncoding = en.nextElement();

                    if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                        gzipResponse = true;
                        break;
                    }
                }

                if (gzipResponse) {
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

    protected void sendErrorResponse(Request baseRequest, HttpServletResponse servletResponse, DispatchResult dispatchResult, Throwable t) throws IOException {
        String responseError = ExceptionUtils.getStackTrace(t);
        logger.error("Error receiving message (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", t);
        eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), dispatchResult == null ? null : dispatchResult.getMessageId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error receiving message", t));

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

    protected Object getMessage(Request request, Map<String, Object> sourceMap, List<Attachment> attachments) throws IOException, ChannelException, MessagingException, DonkeyElementException, ParserConfigurationException {
        HttpRequestMessage requestMessage = createRequestMessage(request, false);

        /*
         * Now that we have the request body, we need to create the actual RawMessage message data.
         * Depending on the connector settings this could be our custom serialized XML, a Base64
         * string encoded from the raw request payload, or a string encoded from the payload with
         * the request charset.
         */
        Object rawMessageContent;

        if (connectorProperties.isXmlBody()) {
            rawMessageContent = HttpMessageConverter.httpRequestToXml(requestMessage, connectorProperties.isParseMultipart(), connectorProperties.isIncludeMetadata(), this);
        } else {
            rawMessageContent = requestMessage.getContent();
        }

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.RECEIVING));

        populateSourceMap(request, requestMessage, sourceMap);

        return rawMessageContent;
    }

    protected HttpRequestMessage createRequestMessage(Request request, boolean ignorePayload) throws IOException, MessagingException {
        // Only parse multipart if XML Body is selected and Parse Multipart is enabled
        boolean parseMultipart = connectorProperties.isXmlBody() && connectorProperties.isParseMultipart() && ServletFileUpload.isMultipartContent(request);
        return createRequestMessage(request, ignorePayload, parseMultipart);
    }

    protected HttpRequestMessage createRequestMessage(Request request, boolean ignorePayload, boolean parseMultipart) throws IOException, MessagingException {
        HttpRequestMessage requestMessage = new HttpRequestMessage();
        requestMessage.setMethod(request.getMethod());
        requestMessage.setHeaders(HttpMessageConverter.convertFieldEnumerationToMap(request));
        requestMessage.setParameters(extractParameters(request));

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

        if (!ignorePayload) {
            InputStream requestInputStream = request.getInputStream();
            // If a security handler already consumed the entity, get it from the request attribute instead
            try {
                byte[] entity = (byte[]) request.getAttribute(EntityProvider.ATTRIBUTE_NAME);
                if (entity != null) {
                    requestInputStream = new ByteArrayInputStream(entity);
                }
            } catch (Exception e) {
            }

            // If the request is GZIP encoded, uncompress the content
            List<String> contentEncodingList = requestMessage.getCaseInsensitiveHeaders().get(HTTP.CONTENT_ENCODING);
            if (CollectionUtils.isNotEmpty(contentEncodingList)) {
                for (String contentEncoding : contentEncodingList) {
                    if (contentEncoding != null && (contentEncoding.equalsIgnoreCase("gzip") || contentEncoding.equalsIgnoreCase("x-gzip"))) {
                        requestInputStream = new GZIPInputStream(requestInputStream);
                        break;
                    }
                }
            }

            /*
             * First parse out the body of the HTTP request. Depending on the connector settings,
             * this could end up being a string encoded with the request charset, a byte array
             * representing the raw request payload, or a MimeMultipart object.
             */
            if (parseMultipart) {
                requestMessage.setContent(new MimeMultipart(new ByteArrayDataSource(requestInputStream, contentType.toString())));
            } else if (isBinaryContentType(contentType)) {
                requestMessage.setContent(IOUtils.toByteArray(requestInputStream));
            } else {
                requestMessage.setContent(IOUtils.toString(requestInputStream, HttpMessageConverter.getDefaultHttpCharset(request.getCharacterEncoding())));
            }
        }

        return requestMessage;
    }

    protected void populateSourceMap(Request request, HttpRequestMessage requestMessage, Map<String, Object> sourceMap) {
        sourceMap.put("remoteAddress", requestMessage.getRemoteAddress());
        sourceMap.put("remotePort", request.getRemotePort());
        sourceMap.put("localAddress", StringUtils.trimToEmpty(request.getLocalAddr()));
        sourceMap.put("localPort", request.getLocalPort());
        sourceMap.put("method", requestMessage.getMethod());
        sourceMap.put("url", requestMessage.getRequestUrl());
        HttpURI uri = request.getHttpURI();
        sourceMap.put("uri", StringUtils.trimToEmpty(uri.isAbsolute() ? uri.toString() : uri.getPathQuery()));
        sourceMap.put("protocol", StringUtils.trimToEmpty(request.getProtocol()));
        sourceMap.put("query", requestMessage.getQueryString());
        sourceMap.put("contextPath", requestMessage.getContextPath());
        sourceMap.put("headers", new MessageHeaders(requestMessage.getCaseInsensitiveHeaders()));
        sourceMap.put("parameters", new MessageParameters(requestMessage.getParameters()));

        // Add custom source map variables from the configuration interface
        sourceMap.putAll(configuration.getRequestInformation(request));
    }

    private class StaticResourceHandler extends AbstractHandler {

        private HttpStaticResource staticResource;

        public StaticResourceHandler(HttpStaticResource staticResource) {
            this.staticResource = staticResource;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
            // Only allow GET requests, otherwise pass to the next request handler
            if (!baseRequest.getMethod().equalsIgnoreCase(HttpMethod.GET.asString())) {
                return;
            }

            String originalThreadName = Thread.currentThread().getName();

            try {
                Thread.currentThread().setName("HTTP Receiver Thread on " + getChannel().getName() + " (" + getChannelId() + ") < " + originalThreadName);
                HttpRequestMessage requestMessage = createRequestMessage(baseRequest, true);

                String contextPath = URLDecoder.decode(requestMessage.getContextPath(), "US-ASCII");
                if (contextPath.endsWith("/")) {
                    contextPath = contextPath.substring(0, contextPath.length() - 1);
                }
                logger.debug("Received static resource request at: " + contextPath);

                Map<String, Object> sourceMap = new HashMap<String, Object>();
                populateSourceMap(baseRequest, requestMessage, sourceMap);

                String value = replacer.replaceValues(staticResource.getValue(), getChannelId(), sourceMap);
                String contentTypeString = replacer.replaceValues(staticResource.getContentType(), getChannelId(), sourceMap);

                // If we're not reading from a directory and the context path doesn't match, pass to the next request handler
                if (staticResource.getResourceType() != ResourceType.DIRECTORY && !staticResource.getContextPath().equalsIgnoreCase(contextPath)) {
                    return;
                }

                // If the query parameters do not match, pass to the next request handler
                if (!parametersEqual(staticResource.getQueryParameters(), requestMessage.getParameters())) {
                    return;
                }

                ContentType contentType;
                try {
                    contentType = ContentType.parse(contentTypeString);
                } catch (Exception e) {
                    contentType = ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), CharsetUtils.getEncoding(connectorProperties.getCharset()));
                }

                Charset charset = contentType.getCharset();
                if (charset == null) {
                    charset = Charset.forName(CharsetUtils.getEncoding(connectorProperties.getCharset()));
                }

                servletResponse.setContentType(contentType.toString());
                servletResponse.setStatus(HttpStatus.SC_OK);

                OutputStream responseOutputStream = servletResponse.getOutputStream();

                // If the client accepts GZIP compression, compress the content
                List<String> acceptEncodingList = requestMessage.getCaseInsensitiveHeaders().get("Accept-Encoding");
                if (CollectionUtils.isNotEmpty(acceptEncodingList)) {
                    for (String acceptEncoding : acceptEncodingList) {
                        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
                            servletResponse.setHeader(HTTP.CONTENT_ENCODING, "gzip");
                            responseOutputStream = new GZIPOutputStream(responseOutputStream);
                            break;
                        }
                    }
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
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), null, ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), "Error handling static HTTP resource request", t));

                servletResponse.reset();
                servletResponse.setContentType(ContentType.TEXT_PLAIN.toString());
                servletResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                servletResponse.getOutputStream().write(ExceptionUtils.getStackTrace(t).getBytes());
            } finally {
                Thread.currentThread().setName(originalThreadName);
            }

            baseRequest.setHandled(true);
        }
    }

    protected String replaceValues(String template, DispatchResult dispatchResult) {
        ConnectorMessage mergedConnectorMessage = null;

        if (dispatchResult != null && dispatchResult.getProcessedMessage() != null) {
            mergedConnectorMessage = dispatchResult.getProcessedMessage().getMergedConnectorMessage();
        }

        return (mergedConnectorMessage == null ? replacer.replaceValues(template, getChannelId(), getChannel().getName()) : replacer.replaceValues(template, mergedConnectorMessage));
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

    protected Map<String, List<String>> extractParameters(Request request) {
        Map<String, List<String>> parameterMap = new HashMap<String, List<String>>();

        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            List<String> list = parameterMap.get(entry.getKey());

            if (list == null) {
                list = new ArrayList<String>();
                parameterMap.put(entry.getKey(), list);
            }

            list.addAll(Arrays.asList(entry.getValue()));
        }

        return parameterMap;
    }

    protected boolean parametersEqual(Map<String, List<String>> params1, Map<String, List<String>> params2) {
        if (!params1.keySet().equals(params2.keySet())) {
            return false;
        }

        for (Entry<String, List<String>> entry : params1.entrySet()) {
            if (!ListUtils.isEqualList(entry.getValue(), params2.get(entry.getKey()))) {
                return false;
            }
        }

        return true;
    }

    protected String getRequestURL(Request request) {
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

    private ConstraintSecurityHandler createSecurityHandler(Handler handler) throws Exception {
        final Authenticator authenticator = authenticatorProvider.getAuthenticator();

        final String authMethod;
        switch (authProps.getAuthType()) {
            case BASIC:
                authMethod = Constraint.__BASIC_AUTH;
                break;
            case DIGEST:
                authMethod = Constraint.__DIGEST_AUTH;
                break;
            default:
                authMethod = "customauth";
        }

        Constraint constraint = new Constraint();
        constraint.setName(authMethod);
        constraint.setRoles(new String[] { "user" });
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticator(new org.eclipse.jetty.security.Authenticator() {
            @Override
            public void setConfiguration(AuthConfiguration configuration) {}

            @Override
            public String getAuthMethod() {
                return authMethod;
            }

            @Override
            public void prepareRequest(ServletRequest request) {}

            @Override
            public Authentication validateRequest(final ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException {
                HttpServletRequest request = (HttpServletRequest) req;
                HttpServletResponse response = (HttpServletResponse) res;

                String remoteAddress = StringUtils.trimToEmpty(request.getRemoteAddr());
                int remotePort = request.getRemotePort();
                String localAddress = StringUtils.trimToEmpty(request.getLocalAddr());
                int localPort = request.getLocalPort();
                String protocol = StringUtils.trimToEmpty(request.getProtocol());
                String method = StringUtils.trimToEmpty(request.getMethod());
                String requestURI = StringUtils.trimToEmpty(request.getRequestURI());
                Map<String, List<String>> headers = HttpMessageConverter.convertFieldEnumerationToMap(request);

                Map<String, List<String>> queryParameters = new LinkedHashMap<String, List<String>>();
                for (Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
                    queryParameters.put(entry.getKey(), Arrays.asList(entry.getValue()));
                }

                EntityProvider entityProvider = new EntityProvider() {
                    @Override
                    public byte[] getEntity() throws IOException {
                        byte[] entity = (byte[]) req.getAttribute(ATTRIBUTE_NAME);
                        if (entity == null) {
                            entity = IOUtils.toByteArray(req.getInputStream());
                            req.setAttribute(ATTRIBUTE_NAME, entity);
                        }
                        return entity;
                    }
                };

                RequestInfo requestInfo = new RequestInfo(remoteAddress, remotePort, localAddress, localPort, protocol, method, requestURI, headers, queryParameters, entityProvider, configuration.getRequestInformation(request));

                try {
                    AuthenticationResult result = authenticator.authenticate(requestInfo);

                    for (Entry<String, List<String>> entry : result.getResponseHeaders().entrySet()) {
                        if (StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null) {
                            for (int i = 0; i < entry.getValue().size(); i++) {
                                if (i == 0) {
                                    response.setHeader(entry.getKey(), entry.getValue().get(i));
                                } else {
                                    response.addHeader(entry.getKey(), entry.getValue().get(i));
                                }
                            }
                        }
                    }

                    switch (result.getStatus()) {
                        case CHALLENGED:
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                            return org.eclipse.jetty.server.Authentication.SEND_CONTINUE;
                        case SUCCESS:
                            Principal userPrincipal = new UserPrincipal(StringUtils.trimToEmpty(result.getUsername()), null);
                            Subject subject = new Subject();
                            subject.getPrincipals().add(userPrincipal);
                            return new UserAuthentication(getAuthMethod(), new DefaultUserIdentity(subject, userPrincipal, new String[] {
                                    "user" }));
                        case FAILURE:
                        default:
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                            return org.eclipse.jetty.server.Authentication.SEND_FAILURE;
                    }
                } catch (Throwable t) {
                    logger.error("Error in HTTP authentication for " + connectorProperties.getName() + " (" + connectorProperties.getName() + " \"Source\" on channel " + getChannelId() + ").", t);
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), null, ErrorEventType.DESTINATION_CONNECTOR, "Source", connectorProperties.getName(), "Error in HTTP authentication for " + connectorProperties.getName(), t));
                    throw new ServerAuthException(t);
                }
            }

            @Override
            public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, User validatedUser) throws ServerAuthException {
                return true;
            }
        });
        securityHandler.addConstraintMapping(constraintMapping);

        securityHandler.setHandler(handler);
        return securityHandler;
    }

    @Override
    public boolean isBinaryContentType(ContentType contentType) {
        String mimeType = contentType.getMimeType();

        if (connectorProperties.isBinaryMimeTypesRegex()) {
            return binaryMimeTypesRegex.matcher(mimeType).matches();
        } else {
            return StringUtils.startsWithAny(mimeType, binaryMimeTypesArray);
        }
    }
}