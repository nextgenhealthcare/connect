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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class HttpDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpDispatcherProperties connectorProperties;

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private ConnectorType connectorType = ConnectorType.WRITER;

    private HttpClient client = new HttpClient();
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

        configuration.configureConnector(connectorProperties);
    }

    @Override
    public void onUndeploy() throws UndeployException {}

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {}

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage connectorMessage) {
        HttpDispatcherProperties httpDispatcherProperties = (HttpDispatcherProperties) SerializationUtils.clone(connectorProperties);

        // Replace all values in connector properties
        httpDispatcherProperties.setHost(replacer.replaceValues(httpDispatcherProperties.getHost(), connectorMessage));
        httpDispatcherProperties.setHeaders(replacer.replaceValuesInMap(httpDispatcherProperties.getHeaders(), connectorMessage));
        httpDispatcherProperties.setParameters(replacer.replaceValuesInMap(httpDispatcherProperties.getParameters(), connectorMessage));
        httpDispatcherProperties.setUsername(replacer.replaceValues(httpDispatcherProperties.getUsername(), connectorMessage));
        httpDispatcherProperties.setPassword(replacer.replaceValues(httpDispatcherProperties.getPassword(), connectorMessage));
        httpDispatcherProperties.setContent(replacer.replaceValues(httpDispatcherProperties.getContent(), connectorMessage));
        httpDispatcherProperties.setContentType(replacer.replaceValues(httpDispatcherProperties.getContentType(), connectorMessage));
        httpDispatcherProperties.setSocketTimeout(replacer.replaceValues(httpDispatcherProperties.getSocketTimeout(), connectorMessage));

        return httpDispatcherProperties;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
        HttpDispatcherProperties httpDispatcherProperties = (HttpDispatcherProperties) connectorProperties;

        String info = "Host: " + httpDispatcherProperties.getHost() + "   Method: " + httpDispatcherProperties.getMethod();
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.BUSY, info);

        String responseData = null;
        String responseError = null;
        Status responseStatus = Status.QUEUED;

        try {
            HttpMethod httpMethod = null;

            try {
                httpMethod = buildHttpRequest(httpDispatcherProperties);

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

                if (httpDispatcherProperties.isIncludeHeadersInResponse()) {
                    HttpMessageConverter converter = new HttpMessageConverter();
                    responseData = converter.httpResponseToXml(httpMethod.getStatusLine().toString(), httpMethod.getResponseHeaders(), httpMethod.getResponseBodyAsString());
                } else {
                    responseData = httpMethod.getResponseBodyAsString();
                }

                if (statusCode < HttpStatus.SC_BAD_REQUEST) {
                    responseStatus = Status.SENT;
                } else {
                    alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_404, "Received error response from HTTP server.", null);
                    responseData = ErrorMessageBuilder.buildErrorResponse(responseData, null);
                    responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_404, responseData, null);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (httpMethod != null) {
                    httpMethod.releaseConnection();
                }
            }
        } catch (Exception e) {
            alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_404, "Error connecting to HTTP server.", e);
            responseData = ErrorMessageBuilder.buildErrorResponse("Error connecting to HTTP server", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_403, "Error connecting to HTTP server", e);

            // TODO: Handle Exception
//            connector.handleException(e);
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), connectorType, Event.DONE);
        }

        return new Response(responseStatus, responseData, responseError);
    }

    private HttpMethod buildHttpRequest(HttpDispatcherProperties httpDispatcherProperties) throws Exception {
        String address = httpDispatcherProperties.getHost();
        String method = httpDispatcherProperties.getMethod();
        String content = httpDispatcherProperties.getContent();
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

        // create the method
        if ("GET".equalsIgnoreCase(method)) {
            httpMethod = new GetMethod(address);
            httpMethod.setQueryString(queryParameters);
        } else if ("POST".equalsIgnoreCase(method)) {
            PostMethod postMethod = new PostMethod(address);

            if (isMultipart) {
                logger.debug("setting multipart file content");
                File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
                FileUtils.writeStringToFile(tempFile, content, charset);
                Part[] parts = new Part[] { new FilePart(tempFile.getName(), tempFile, contentType, charset) };
                postMethod.setQueryString(queryParameters);
                postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
            } else if (StringUtils.equals(contentType, "application/x-www-form-urlencoded")) {
                postMethod.setRequestBody(queryParameters);
            } else {
                postMethod.setQueryString(queryParameters);
                postMethod.setRequestEntity(new StringRequestEntity(content, contentType, charset));
            }

            httpMethod = postMethod;
        } else if ("PUT".equalsIgnoreCase(method)) {
            PutMethod putMethod = new PutMethod(address);
            putMethod.setRequestEntity(new StringRequestEntity(content, contentType, charset));
            putMethod.setQueryString(queryParameters);
            httpMethod = putMethod;
        } else if ("DELETE".equalsIgnoreCase(method)) {
            httpMethod = new DeleteMethod(address);
            httpMethod.setQueryString(queryParameters);
        }

        // set the headers
        for (Entry<String, String> headerEntry : headers.entrySet()) {
            httpMethod.setRequestHeader(new Header(headerEntry.getKey(), headerEntry.getValue()));
            logger.debug("setting method header: [" + headerEntry.getKey() + ", " + headerEntry.getValue() + "]");
        }

        return httpMethod;
    }
}