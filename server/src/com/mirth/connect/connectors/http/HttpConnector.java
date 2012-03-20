/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.Map;

import org.mule.providers.QueueEnabledConnector;
import org.mule.umo.lifecycle.InitialisationException;

import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.CharsetUtils;

public class HttpConnector extends QueueEnabledConnector {
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private String channelId;

    // Connector specific properties
    private String dispatcherMethod;
    private Map<String, String> dispatcherHeaders;
    private Map<String, String> dispatcherParameters;
    private String dispatcherContent;
    private String dispatcherContentType;
    private String dispatcherCharset;
    private boolean dispatcherUseAuthentication;
    private String dispatcherAuthenticationType;
    private String dispatcherUsername;
    private String dispatcherPassword;
    private boolean dispatcherMultipart;
    private String dispatcherReplyChannelId;
    private boolean dispatcherIncludeHeadersInResponse;
    private String dispatcherSocketTimeout;
    
    private String receiverResponseContentType;
    private boolean receiverBodyOnly;
    private String receiverResponse;
    private Map<String, String> receiverResponseHeaders;
    private String receiverResponseStatusCode;
    private String receiverCharset;
    private String receiverContextPath;
    private String receiverTimeout;
    
    private HttpConfiguration configuration = null;

    @Override
    public void doInitialise() throws InitialisationException {
        super.doInitialise();

        if (isUsePersistentQueues()) {
            setConnectorErrorCode(Constants.ERROR_404);
            setDispatcher(new HttpMessageDispatcher(this));
        }

        // load the default configuration
        String configurationClass = configurationController.getProperty(getProtocol(), "configurationClass");

        try {
            configuration = (HttpConfiguration) Class.forName(configurationClass).newInstance();
        } catch (Exception e) {
            logger.trace("could not find custom configuration class, using default");
            configuration = new DefaultHttpConfiguration();
        }

        configuration.configureConnector(this);
    }
    
    public HttpConfiguration getConfiguration() {
        return configuration;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getDispatcherReplyChannelId() {
        return dispatcherReplyChannelId;
    }

    public void setDispatcherReplyChannelId(String dispatcherReplyChannelId) {
        this.dispatcherReplyChannelId = dispatcherReplyChannelId;
    }

    public String getDispatcherMethod() {
        return dispatcherMethod;
    }

    public void setDispatcherMethod(String dispatcherMethod) {
        this.dispatcherMethod = dispatcherMethod;
    }

    public Map<String, String> getDispatcherHeaders() {
        return dispatcherHeaders;
    }

    public void setDispatcherHeaders(Map<String, String> dispatcherHeaders) {
        this.dispatcherHeaders = dispatcherHeaders;
    }

    public Map<String, String> getDispatcherParameters() {
        return dispatcherParameters;
    }

    public void setDispatcherParameters(Map<String, String> dispatcherParameters) {
        this.dispatcherParameters = dispatcherParameters;
    }

    public String getDispatcherContent() {
        return dispatcherContent;
    }

    public void setDispatcherContent(String dispatcherContent) {
        this.dispatcherContent = dispatcherContent;
    }

    public String getDispatcherContentType() {
        return dispatcherContentType;
    }

    public void setDispatcherContentType(String dispatcherContentType) {
        this.dispatcherContentType = dispatcherContentType;
    }

    public String getDispatcherCharset() {
        return dispatcherCharset;
    }

    public void setDispatcherCharset(String dispatcherCharset) {
        this.dispatcherCharset = CharsetUtils.getEncoding(dispatcherCharset);
    }

    public boolean isDispatcherUseAuthentication() {
        return dispatcherUseAuthentication;
    }

    public void setDispatcherUseAuthentication(boolean dispatcherUseAuthentication) {
        this.dispatcherUseAuthentication = dispatcherUseAuthentication;
    }

    public String getDispatcherAuthenticationType() {
        return dispatcherAuthenticationType;
    }

    public void setDispatcherAuthenticationType(String dispatcherAuthenticationType) {
        this.dispatcherAuthenticationType = dispatcherAuthenticationType;
    }

    public String getDispatcherUsername() {
        return dispatcherUsername;
    }

    public void setDispatcherUsername(String dispatcherUsername) {
        this.dispatcherUsername = dispatcherUsername;
    }

    public String getDispatcherPassword() {
        return dispatcherPassword;
    }

    public void setDispatcherPassword(String dispatcherPassword) {
        this.dispatcherPassword = dispatcherPassword;
    }

    public boolean isDispatcherMultipart() {
        return dispatcherMultipart;
    }

    public void setDispatcherMultipart(boolean dispatcherMultipart) {
        this.dispatcherMultipart = dispatcherMultipart;
    }

    public boolean isReceiverBodyOnly() {
        return receiverBodyOnly;
    }

    public void setReceiverBodyOnly(boolean receiverBodyOnly) {
        this.receiverBodyOnly = receiverBodyOnly;
    }

    public boolean isDispatcherIncludeHeadersInResponse() {
        return dispatcherIncludeHeadersInResponse;
    }

    public void setDispatcherIncludeHeadersInResponse(boolean dispatcherIncludeHeadersInResponse) {
        this.dispatcherIncludeHeadersInResponse = dispatcherIncludeHeadersInResponse;
    }

    public String getReceiverResponseContentType() {
        return receiverResponseContentType;
    }

    public void setReceiverResponseContentType(String receiverResponseContentType) {
        this.receiverResponseContentType = receiverResponseContentType;
    }

    public String getReceiverResponse() {
        return receiverResponse;
    }

    public void setReceiverResponse(String receiverResponse) {
        this.receiverResponse = receiverResponse;
    }

    public String getReceiverCharset() {
        return receiverCharset;
    }

    public void setReceiverCharset(String receiverCharset) {
        this.receiverCharset = CharsetUtils.getEncoding(receiverCharset, System.getProperty("ca.uhn.hl7v2.llp.charset"));
    }

    public String getDispatcherSocketTimeout() {
        return dispatcherSocketTimeout;
    }

    public void setDispatcherSocketTimeout(String dispatcherSocketTimeout) {
        this.dispatcherSocketTimeout = dispatcherSocketTimeout;
    }
    
    public String getReceiverContextPath() {
        return receiverContextPath;
    }

    public void setReceiverContextPath(String receiverContextPath) {
        this.receiverContextPath = receiverContextPath;
    }
    
    public Map<String, String> getReceiverResponseHeaders() {
        return receiverResponseHeaders;
    }

    public void setReceiverResponseHeaders(Map<String, String> receiverResponseHeaders) {
        this.receiverResponseHeaders = receiverResponseHeaders;
    }

    public String getReceiverResponseStatusCode() {
        return receiverResponseStatusCode;
    }

    public void setReceiverResponseStatusCode(String receiverResponseStatusCode) {
        this.receiverResponseStatusCode = receiverResponseStatusCode;
    }

    public String getReceiverTimeout() {
        return receiverTimeout;
    }

    public void setReceiverTimeout(String receiverTimeout) {
        this.receiverTimeout = receiverTimeout;
    }

    @Override
    public String getProtocol() {
        return "HTTP";
    }
}