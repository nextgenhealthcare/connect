/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.MessageAttachmentUtil;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorMessageBuilder;

public class HttpDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpDispatcherProperties connectorProperties;

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private Map<Long, HttpClient> clients = new ConcurrentHashMap<Long, HttpClient>();
    private HttpConfiguration configuration = null;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (HttpDispatcherProperties) getConnectorProperties();

        // load the default configuration
        String configurationClass = configurationController.getProperty(connectorProperties.getProtocol(), "configurationClass");

        try {
            configuration = (HttpConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Exception e) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultHttpConfiguration();
        }

        try {
            configuration.configureConnector(getChannelId(), getMetaDataId(), connectorProperties.getHost());
        } catch (Exception e) {
            throw new DeployException(e);
        }
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {
        clients.clear();
    }

    @Override
    public void onHalt() throws HaltException {
        clients.clear();
    }

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        HttpDispatcherProperties httpDispatcherProperties = (HttpDispatcherProperties) connectorProperties;

        // Replace all values in connector properties
        httpDispatcherProperties.setHost(replacer.replaceValues(httpDispatcherProperties.getHost(), connectorMessage));
        httpDispatcherProperties.setHeaders(replacer.replaceValuesInMap(httpDispatcherProperties.getHeaders(), connectorMessage));
        httpDispatcherProperties.setParameters(replacer.replaceValuesInMap(httpDispatcherProperties.getParameters(), connectorMessage));
        httpDispatcherProperties.setUsername(replacer.replaceValues(httpDispatcherProperties.getUsername(), connectorMessage));
        httpDispatcherProperties.setPassword(replacer.replaceValues(httpDispatcherProperties.getPassword(), connectorMessage));
        httpDispatcherProperties.setContent(replacer.replaceValues(httpDispatcherProperties.getContent(), connectorMessage));
        httpDispatcherProperties.setContentType(replacer.replaceValues(httpDispatcherProperties.getContentType(), connectorMessage));
        httpDispatcherProperties.setSocketTimeout(replacer.replaceValues(httpDispatcherProperties.getSocketTimeout(), connectorMessage));
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        HttpDispatcherProperties httpDispatcherProperties = (HttpDispatcherProperties) connectorProperties;

        String info = "Host: " + httpDispatcherProperties.getHost() + "   Method: " + httpDispatcherProperties.getMethod();
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.WRITING));

        String responseData = null;
        String responseError = null;
        String responseStatusMessage = null;
        Status responseStatus = Status.QUEUED;

        HttpMethod httpMethod = null;
        File tempFile = null;

        try {
            long dispatcherId = getDispatcherId();
            HttpClient client = clients.get(dispatcherId);
            if (client == null) {
                client = new HttpClient();
                clients.put(dispatcherId, client);
            }

            configuration.configureDispatcher(getChannelId(), getMetaDataId(), httpDispatcherProperties.getHost());
            httpMethod = buildHttpRequest(httpDispatcherProperties, connectorMessage, tempFile);

            // authentication
            if (httpDispatcherProperties.isUseAuthentication()) {
                List<String> authenticationPreferences = new ArrayList<String>();

                if ("Digest".equalsIgnoreCase(httpDispatcherProperties.getAuthenticationType())) {
                    authenticationPreferences.add(AuthPolicy.DIGEST);
                    logger.debug("using Digest authentication");
                } else {
                    authenticationPreferences.add(AuthPolicy.BASIC);
                    logger.debug("using Basic authentication");
                }

                client.getParams().setAuthenticationPreemptive(true);
                client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authenticationPreferences);
                Credentials credentials = new UsernamePasswordCredentials(httpDispatcherProperties.getUsername(), httpDispatcherProperties.getPassword());
                client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
                logger.debug("using authentication with credentials: " + credentials);
            }

            client.getParams().setSoTimeout(NumberUtils.toInt(replacer.replaceValues(httpDispatcherProperties.getSocketTimeout()), 30000));

            // execute the method
            logger.debug("executing method: type=" + httpMethod.getName() + ", uri=" + httpMethod.getURI().toString());
            int statusCode = client.executeMethod(httpMethod);
            logger.debug("received status code: " + statusCode);

            String responseBody = null;

            // If the response is GZIP encoded, uncompress the content
            boolean gzipEncoded = false;

            for (int i = 0; i < httpMethod.getResponseHeaders().length && !gzipEncoded; i++) {
                Header header = httpMethod.getResponseHeaders()[i];

                if (header.getName().equals("Content-Encoding") && header.getValue().equals("gzip")) {
                    responseBody = HttpUtil.uncompressGzip(httpMethod.getResponseBody(), httpDispatcherProperties.getCharset());
                    gzipEncoded = true;
                }
            }

            if (!gzipEncoded) {
                responseBody = new String(httpMethod.getResponseBody(), httpDispatcherProperties.getCharset());
            }

            if (httpDispatcherProperties.isIncludeHeadersInResponse()) {
                HttpMessageConverter converter = new HttpMessageConverter();
                responseData = converter.httpResponseToXml(httpMethod.getStatusLine().toString(), httpMethod.getResponseHeaders(), responseBody);
            } else {
                responseData = responseBody;
            }

            if (statusCode < HttpStatus.SC_BAD_REQUEST) {
                responseStatus = Status.SENT;
            } else {
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Received error response from HTTP server.", null));
                responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Received error response from HTTP server.", null);
                responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), responseData, null);
            }
        } catch (Exception e) {
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error connecting to HTTP server.", e));
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error connecting to HTTP server", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error connecting to HTTP server", e);

            // TODO: Handle Exception
            // connector.handleException(e);
        } finally {
            try {
                if (httpMethod != null) {
                    httpMethod.releaseConnection();
                }

                // Delete temp files if we created them
                if (tempFile != null) {
                    tempFile.delete();
                    tempFile = null;
                }
            } finally {
                eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
            }
        }

        return new Response(responseStatus, responseData, responseStatusMessage, responseError);
    }

    private HttpMethod buildHttpRequest(HttpDispatcherProperties httpDispatcherProperties, ConnectorMessage connectorMessage, File tempFile) throws Exception {
        String address = httpDispatcherProperties.getHost();
        String method = httpDispatcherProperties.getMethod();
        Object content = MessageAttachmentUtil.reAttachMessage(httpDispatcherProperties.getContent(), connectorMessage);
        String contentType = httpDispatcherProperties.getContentType();
        String charset = httpDispatcherProperties.getCharset();
        boolean isMultipart = httpDispatcherProperties.isMultipart();
        Map<String, String> headers = httpDispatcherProperties.getHeaders();
        Map<String, String> parameters = httpDispatcherProperties.getParameters();

        HttpMethod httpMethod = null;

        // populate the query parameters
        NameValuePair[] queryParameters = new NameValuePair[parameters.size()];
        int index = 0;

        for (Entry<String, String> parameterEntry : parameters.entrySet()) {
            queryParameters[index] = new NameValuePair(parameterEntry.getKey(), parameterEntry.getValue());
            index++;
            logger.debug("setting query parameter: [" + parameterEntry.getKey() + ", " + parameterEntry.getValue() + "]");
        }

        // If GZIP compression is enabled, compress the content
        if ("gzip".equals(headers.get("Content-Encoding"))) {
            content = HttpUtil.compressGzip((String) content, charset);
        }

        // create the method
        if ("GET".equalsIgnoreCase(method)) {
            httpMethod = new GetMethod(address);
            setQueryString(httpMethod, queryParameters);
        } else if ("POST".equalsIgnoreCase(method)) {
            PostMethod postMethod = new PostMethod(address);

            if (isMultipart) {
                logger.debug("setting multipart file content");
                tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");

                if (content instanceof String) {
                    FileUtils.writeStringToFile(tempFile, (String) content, charset);
                } else {
                    FileUtils.writeByteArrayToFile(tempFile, (byte[]) content);
                }

                Part[] parts = new Part[] { new FilePart(tempFile.getName(), tempFile, contentType, charset) };
                setQueryString(postMethod, queryParameters);
                postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
            } else if (StringUtils.equals(contentType, "application/x-www-form-urlencoded")) {
                postMethod.setRequestBody(queryParameters);
            } else {
                setQueryString(postMethod, queryParameters);
                setRequestEntity(postMethod, content, contentType, charset);
            }

            httpMethod = postMethod;
        } else if ("PUT".equalsIgnoreCase(method)) {
            PutMethod putMethod = new PutMethod(address);
            setRequestEntity(putMethod, content, contentType, charset);
            setQueryString(putMethod, queryParameters);

            httpMethod = putMethod;
        } else if ("DELETE".equalsIgnoreCase(method)) {
            httpMethod = new DeleteMethod(address);
            setQueryString(httpMethod, queryParameters);
        }

        // set the headers
        for (Entry<String, String> headerEntry : headers.entrySet()) {
            httpMethod.setRequestHeader(new Header(headerEntry.getKey(), headerEntry.getValue()));
            logger.debug("setting method header: [" + headerEntry.getKey() + ", " + headerEntry.getValue() + "]");
        }

        return httpMethod;
    }

    private void setRequestEntity(EntityEnclosingMethod method, Object content, String contentType, String charset) throws UnsupportedEncodingException {
        if (content instanceof String) {
            method.setRequestEntity(new StringRequestEntity((String) content, contentType, charset));
        } else {
            method.setRequestEntity(new ByteArrayRequestEntity((byte[]) content, contentType));
        }
    }

    private void setQueryString(HttpMethod method, NameValuePair[] queryParameters) {
        if (queryParameters.length > 0) {
            method.setQueryString(queryParameters);
        }
    }
}