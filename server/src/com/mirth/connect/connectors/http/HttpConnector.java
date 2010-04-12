package com.mirth.connect.connectors.http;

import java.util.Map;

import org.mule.providers.QueueEnabledConnector;

public class HttpConnector extends QueueEnabledConnector {
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
    private String receiverResponseContentType;
    private boolean receiverIncludeHeaders;
    private boolean dispatcherIncludeHeadersInResponse;
    private String receiverCharset;

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
        this.dispatcherCharset = dispatcherCharset;
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

    public boolean isReceiverIncludeHeaders() {
        return receiverIncludeHeaders;
    }

    public void setReceiverIncludeHeaders(boolean receiverIncludeHeaders) {
        this.receiverIncludeHeaders = receiverIncludeHeaders;
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

    public String getReceiverCharset() {
        return receiverCharset;
    }

    public void setReceiverCharset(String receiverCharset) {
        this.receiverCharset = receiverCharset;
    }

    @Override
    public String getProtocol() {
        return "HTTP";
    }
}