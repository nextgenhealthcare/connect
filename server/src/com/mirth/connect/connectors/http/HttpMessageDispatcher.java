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
import java.net.ConnectException;
import java.net.URLDecoder;
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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.QueueEnabledMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.QueuedMessage;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.VMRouter;

public class HttpMessageDispatcher extends AbstractMessageDispatcher implements QueueEnabledMessageDispatcher {
    private Logger logger = Logger.getLogger(this.getClass());
    private HttpConnector connector;

    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private ConnectorType connectorType = ConnectorType.WRITER;

    private HttpClient client = new HttpClient();

    public HttpMessageDispatcher(HttpConnector connector) {
        super(connector);
        this.connector = connector;
    }

    @Override
    public void doDispatch(UMOEvent event) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        MessageObject mo = messageObjectController.getMessageObjectFromEvent(event);

        if (mo == null) {
            return;
        }

        try {
            if (connector.isUsePersistentQueues()) {
                /*
                 * Note that the endpoint URI is not URL decoded before it is
                 * put into the queue, so it will need to be decoded it is taken
                 * off the queue in sendPayload
                 */
                connector.putMessageInQueue(event.getEndpoint().getEndpointURI(), mo);
                return;
            } else {
                /*
                 * We need to URL decode the endpoint since a MuleEndpointURI
                 * escapes special characters by default. This will allow map
                 * replacements.
                 * 
                 * See: MIRTH-1645
                 */
                submitHttpRequest(URLDecoder.decode(event.getEndpoint().getEndpointURI().toString(), "utf-8"), mo);
            }
        } catch (Exception e) {
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_404, "Error connecting to HTTP server.", e);
            messageObjectController.setError(mo, Constants.ERROR_404, "Error connecting to HTTP server.", e, null);
            connector.handleException(e);
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }
    }

    private void submitHttpRequest(String address, MessageObject mo) throws Exception {
        HttpMethod httpMethod = null;

        try {
            httpMethod = buildHttpRequest(replacer.replaceValues(address, mo), mo);

            // authentication

            if (connector.isDispatcherUseAuthentication()) {
                List<String> authenticationPreferences = new ArrayList<String>();

                if ("Digest".equalsIgnoreCase(connector.getDispatcherAuthenticationType())) {
                    authenticationPreferences.add(AuthPolicy.DIGEST);
                    logger.debug("using Digest authentication");
                } else {
                    authenticationPreferences.add(AuthPolicy.BASIC);
                    logger.debug("using Basic authentication");
                }

                client.getParams().setAuthenticationPreemptive(true);
                client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authenticationPreferences);
                Credentials credentials = new UsernamePasswordCredentials(replacer.replaceValues(connector.getDispatcherUsername(), mo), replacer.replaceValues(connector.getDispatcherPassword(), mo));
                client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM), credentials);
                logger.debug("using authentication with credentials: " + credentials);
            }

            client.getParams().setSoTimeout(connector.getDispatcherSocketTimeout());

            // execute the method
            logger.debug("executing method: type=" + httpMethod.getName() + ", uri=" + httpMethod.getURI().toString());
            int statusCode = client.executeMethod(httpMethod);
            logger.debug("received status code: " + statusCode);

            String response = null;

            if (connector.isDispatcherIncludeHeadersInResponse()) {
                HttpMessageConverter converter = new HttpMessageConverter();
                response = converter.httpResponseToXml(httpMethod.getStatusLine().toString(), httpMethod.getResponseHeaders(), httpMethod.getResponseBodyAsString());
            } else {
                response = httpMethod.getResponseBodyAsString();
            }

            if (statusCode < HttpStatus.SC_BAD_REQUEST) {
                messageObjectController.setSuccess(mo, response, null);

                // send to reply channel
                if ((connector.getDispatcherReplyChannelId() != null) && !connector.getDispatcherReplyChannelId().equals("sink")) {
                    new VMRouter().routeMessageByChannelId(connector.getDispatcherReplyChannelId(), response, true);
                }
            } else {
                alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_404, "Received error response from HTTP server.", null);
                messageObjectController.setError(mo, Constants.ERROR_404, response, null, null);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (httpMethod != null) {
                httpMethod.releaseConnection();
            }
        }
    }

    @Override
    public void doDispose() {

    }

    @Override
    public UMOMessage doSend(UMOEvent event) throws Exception {
        doDispatch(event);
        return event.getMessage();
    }

    @Override
    public Object getDelegateSession() throws UMOException {
        return null;
    }

    @Override
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        return null;
    }

    @Override
    public boolean sendPayload(QueuedMessage message) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);

        try {
            /*
             * We need to URL decode the endpoint since a MuleEndpointURI
             * escapes special characters by default. This will allow map
             * replacements.
             * 
             * See: MIRTH-1645 & MIRTH-1917
             */
            submitHttpRequest(URLDecoder.decode(message.getEndpointUri().toString(), "utf-8"), message.getMessageObject());
        } catch (Exception e) {
            if (e.getClass() == ConnectException.class) {
                logger.warn("Can't connect to the queued endpoint: " + channelController.getDeployedChannelById(connector.getChannelId()).getName() + " - " + channelController.getDeployedDestinationName(connector.getName()) + " \r\n'" + e.getMessage());
                messageObjectController.setError(message.getMessageObject(), Constants.ERROR_404, "Connection refused", e, null);
                throw e;
            }

            messageObjectController.setError(message.getMessageObject(), Constants.ERROR_404, "Error connecting to web service.", e, null);
            alertController.sendAlerts(connector.getChannelId(), Constants.ERROR_404, "Error connecting to web service.", e);
            connector.handleException(new Exception(e));
        } finally {
            monitoringController.updateStatus(connector, connectorType, Event.DONE);
        }

        return true;
    }
    
    private HttpMethod buildHttpRequest(String address, MessageObject mo) throws Exception {
        String method = connector.getDispatcherMethod();
        String content = replacer.replaceValues(connector.getDispatcherContent(), mo);
        String contentType = connector.getDispatcherContentType();
        String charset = connector.getDispatcherCharset();
        boolean isMultipart = connector.isDispatcherMultipart();
        Map<String, String> headers = replacer.replaceValuesInMap(connector.getDispatcherHeaders(), mo);
        Map<String, String> parameters = replacer.replaceValuesInMap(connector.getDispatcherParameters(), mo);

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