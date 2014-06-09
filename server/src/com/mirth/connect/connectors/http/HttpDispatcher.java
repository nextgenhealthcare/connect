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
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipCompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
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
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorMessageBuilder;

public class HttpDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpDispatcherProperties connectorProperties;

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private Map<Long, CloseableHttpClient> clients = new ConcurrentHashMap<Long, CloseableHttpClient>();
    private HttpConfiguration configuration;
    private RegistryBuilder<ConnectionSocketFactory> socketFactoryRegistry;

    @Override
    public void onDeploy() throws DeployException {
        this.connectorProperties = (HttpDispatcherProperties) getConnectorProperties();

        // load the default configuration
        String configurationClass = configurationController.getProperty(connectorProperties.getProtocol(), "httpConfigurationClass");

        try {
            configuration = (HttpConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Exception e) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultHttpConfiguration();
        }

        try {
            socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory());
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
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {
        for (CloseableHttpClient client : clients.values().toArray(new CloseableHttpClient[clients.size()])) {
            HttpClientUtils.closeQuietly(client);
        }

        clients.clear();
    }

    @Override
    public void onHalt() throws HaltException {
        for (CloseableHttpClient client : clients.values().toArray(new CloseableHttpClient[clients.size()])) {
            HttpClientUtils.closeQuietly(client);
        }

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
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.WRITING));

        String responseData = null;
        String responseError = null;
        String responseStatusMessage = null;
        Status responseStatus = Status.QUEUED;

        CloseableHttpClient client = null;
        HttpRequestBase httpMethod = null;
        CloseableHttpResponse httpResponse = null;
        File tempFile = null;
        int socketTimeout = NumberUtils.toInt(httpDispatcherProperties.getSocketTimeout(), 30000);

        try {
            configuration.configureDispatcher(this, httpDispatcherProperties);

            long dispatcherId = getDispatcherId();
            client = clients.get(dispatcherId);
            if (client == null) {
                BasicHttpClientConnectionManager httpClientConnectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry.build());
                httpClientConnectionManager.setSocketConfig(SocketConfig.custom().setSoTimeout(socketTimeout).build());
                client = HttpClients.custom().setConnectionManager(httpClientConnectionManager).build();
                clients.put(dispatcherId, client);
            }

            URI hostURI = new URI(httpDispatcherProperties.getHost());
            String host = hostURI.getHost();
            String scheme = hostURI.getScheme();
            int port = hostURI.getPort();
            if (port == -1) {
                if (scheme.equalsIgnoreCase("https")) {
                    port = 443;
                } else {
                    port = 80;
                }
            }

            // Parse the content type field first, and then add the charset if needed
            ContentType contentType = ContentType.parse(httpDispatcherProperties.getContentType());
            Charset charset = null;
            if (contentType.getCharset() == null) {
                charset = Charset.forName(httpDispatcherProperties.getCharset());
                contentType = contentType.withCharset(charset);
            } else {
                charset = contentType.getCharset();
            }

            HttpHost target = new HttpHost(host, port, scheme);

            httpMethod = buildHttpRequest(hostURI, httpDispatcherProperties, connectorMessage, tempFile, contentType);

            HttpClientContext context = HttpClientContext.create();

            // authentication
            if (httpDispatcherProperties.isUseAuthentication()) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
                Credentials credentials = new UsernamePasswordCredentials(httpDispatcherProperties.getUsername(), httpDispatcherProperties.getPassword());
                credsProvider.setCredentials(authScope, credentials);
                AuthCache authCache = new BasicAuthCache();
                RegistryBuilder<AuthSchemeProvider> registryBuilder = RegistryBuilder.<AuthSchemeProvider> create();

                if ("Digest".equalsIgnoreCase(httpDispatcherProperties.getAuthenticationType())) {
                    logger.debug("using Digest authentication");
                    // TODO: Cannot preemptively authenticate without realm/nonce values
                    //authCache.put(target, new DigestScheme());
                    registryBuilder.register("digest", new DigestSchemeFactory(charset));
                } else {
                    logger.debug("using Basic authentication");
                    registryBuilder.register("basic", new BasicSchemeFactory(charset));
                    authCache.put(target, new BasicScheme());
                }

                context.setCredentialsProvider(credsProvider);
                context.setAuthSchemeRegistry(registryBuilder.build());
                context.setAuthCache(authCache);

                logger.debug("using authentication with credentials: " + credentials);
            }

            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(socketTimeout).setSocketTimeout(socketTimeout).build();
            context.setRequestConfig(requestConfig);

            // execute the method
            logger.debug("executing method: type=" + httpMethod.getMethod() + ", uri=" + httpMethod.getURI().toString());
            httpResponse = client.execute(target, httpMethod, context);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            logger.debug("received status code: " + statusCode);

            Charset responseCharset = charset;
            ContentType responseContentType = ContentType.get(httpResponse.getEntity());
            if (responseContentType != null && responseContentType.getCharset() != null) {
                responseCharset = responseContentType.getCharset();
            }

            String responseBody = IOUtils.toString(httpResponse.getEntity().getContent(), responseCharset);

            if (httpDispatcherProperties.isIncludeHeadersInResponse()) {
                HttpMessageConverter converter = new HttpMessageConverter();
                responseData = converter.httpResponseToXml(httpResponse.getStatusLine().toString(), httpResponse.getAllHeaders(), responseBody);
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
        } finally {
            try {
                HttpClientUtils.closeQuietly(httpResponse);

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

    public RegistryBuilder<ConnectionSocketFactory> getSocketFactoryRegistry() {
        return socketFactoryRegistry;
    }

    private HttpRequestBase buildHttpRequest(URI hostURI, HttpDispatcherProperties httpDispatcherProperties, ConnectorMessage connectorMessage, File tempFile, ContentType contentType) throws Exception {
        String method = httpDispatcherProperties.getMethod();
        String content = getAttachmentHandler().reAttachMessage(httpDispatcherProperties.getContent(), connectorMessage);
        boolean isMultipart = httpDispatcherProperties.isMultipart();
        Map<String, String> headers = httpDispatcherProperties.getHeaders();
        Map<String, String> parameters = httpDispatcherProperties.getParameters();

        // populate the query parameters
        List<NameValuePair> queryParameters = new ArrayList<NameValuePair>(parameters.size());

        for (Entry<String, String> parameterEntry : parameters.entrySet()) {
            logger.debug("setting query parameter: [" + parameterEntry.getKey() + ", " + parameterEntry.getValue() + "]");
            queryParameters.add(new BasicNameValuePair(parameterEntry.getKey(), parameterEntry.getValue()));
        }

        HttpRequestBase httpMethod = null;
        HttpEntity httpEntity = null;
        URIBuilder uriBuilder = new URIBuilder(hostURI);

        // create the method
        if ("GET".equalsIgnoreCase(method)) {
            setQueryString(uriBuilder, queryParameters);
            httpMethod = new HttpGet(uriBuilder.build());
        } else if ("POST".equalsIgnoreCase(method)) {
            if (isMultipart) {
                logger.debug("setting multipart file content");
                setQueryString(uriBuilder, queryParameters);
                httpMethod = new HttpPost(uriBuilder.build());
                tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");

                if (content instanceof String) {
                    FileUtils.writeStringToFile(tempFile, (String) content, contentType.getCharset(), false);
                }

                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                multipartEntityBuilder.addPart(tempFile.getName(), new FileBody(tempFile, contentType, tempFile.getName()));
                httpEntity = multipartEntityBuilder.build();
            } else if (contentType.getMimeType().equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
                httpMethod = new HttpPost(uriBuilder.build());
                httpEntity = new UrlEncodedFormEntity(queryParameters, contentType.getCharset());
            } else {
                setQueryString(uriBuilder, queryParameters);
                httpMethod = new HttpPost(uriBuilder.build());
                httpEntity = new StringEntity(content, contentType);
            }
        } else if ("PUT".equalsIgnoreCase(method)) {
            setQueryString(uriBuilder, queryParameters);
            httpMethod = new HttpPut(uriBuilder.build());
            httpEntity = new StringEntity(content, contentType);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            setQueryString(uriBuilder, queryParameters);
            httpMethod = new HttpDelete(uriBuilder.build());
        }

        if (httpMethod instanceof HttpEntityEnclosingRequestBase) {
            // Compress the request entity if necessary
            String contentEncoding = (String) new CaseInsensitiveMap(headers).get(HTTP.CONTENT_ENCODING);
            if (contentEncoding != null && (contentEncoding.toLowerCase().equals("gzip") || contentEncoding.toLowerCase().equals("x-gzip"))) {
                httpEntity = new GzipCompressingEntity(httpEntity);
            }

            ((HttpEntityEnclosingRequestBase) httpMethod).setEntity(httpEntity);
        }

        // set the headers
        for (Entry<String, String> headerEntry : headers.entrySet()) {
            logger.debug("setting method header: [" + headerEntry.getKey() + ", " + headerEntry.getValue() + "]");
            httpMethod.addHeader(headerEntry.getKey(), headerEntry.getValue());
        }
        httpMethod.setHeader(HTTP.CONTENT_TYPE, contentType.toString());

        return httpMethod;
    }

    private void setQueryString(URIBuilder uriBuilder, List<NameValuePair> queryParameters) {
        if (queryParameters.size() > 0) {
            uriBuilder.setParameters(queryParameters);
        }
    }
}