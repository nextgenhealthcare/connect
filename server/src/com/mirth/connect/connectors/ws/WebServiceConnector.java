/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.mule.providers.QueueEnabledConnector;
import org.mule.umo.lifecycle.InitialisationException;

import com.mirth.connect.server.Constants;

public class WebServiceConnector extends QueueEnabledConnector {
    private String channelId;

    private String receiverClassName;
    private String receiverServiceName;
    private String receiverResponseValue;
    private List<String> receiverUsernames;
    private List<String> receiverPasswords;

    private String dispatcherReplyChannelId;
    private String dispatcherWsdlUrl;
    private String dispatcherService;
    private String dispatcherPort;
    private boolean dispatcherUseAuthentication;
    private String dispatcherUsername;
    private String dispatcherPassword;
    private String dispatcherEnvelope;
    private boolean dispatcherOneWay;
    private boolean dispatcherUseMtom;
    private List<String> dispatcherAttachmentNames;
    private List<String> dispatcherAttachmentContents;
    private List<String> dispatcherAttachmentTypes;
    private String dispatcherSoapAction;

    // Dispatch object used for pooling the soap connection
    private Dispatch<SOAPMessage> dispatch = null;

    @Override
    public void doInitialise() throws InitialisationException {
        super.doInitialise();

        if (isUsePersistentQueues()) {
            setConnectorErrorCode(Constants.ERROR_410);
            setDispatcher(new WebServiceMessageDispatcher(this));
        }
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getReceiverClassName() {
        return receiverClassName;
    }

    public void setReceiverClassName(String receiverClassName) {
        this.receiverClassName = receiverClassName;
    }

    public String getReceiverServiceName() {
        return receiverServiceName;
    }

    public void setReceiverServiceName(String receiverServiceName) {
        this.receiverServiceName = receiverServiceName;
    }

    public String getReceiverResponseValue() {
        return receiverResponseValue;
    }

    public void setReceiverResponseValue(String receiverResponseValue) {
        this.receiverResponseValue = receiverResponseValue;
    }

    public List<String> getReceiverUsernames() {
        return receiverUsernames;
    }

    public void setReceiverUsernames(List<String> receiverUsernames) {
        this.receiverUsernames = receiverUsernames;
    }

    public List<String> getReceiverPasswords() {
        return receiverPasswords;
    }

    public void setReceiverPasswords(List<String> receiverPasswords) {
        this.receiverPasswords = receiverPasswords;
    }

    public String getDispatcherReplyChannelId() {
        return dispatcherReplyChannelId;
    }

    public void setDispatcherReplyChannelId(String dispatcherReplyChannelId) {
        this.dispatcherReplyChannelId = dispatcherReplyChannelId;
    }

    public String getDispatcherWsdlUrl() {
        return dispatcherWsdlUrl;
    }

    public void setDispatcherWsdlUrl(String dispatcherWsdlUrl) {
        this.dispatcherWsdlUrl = dispatcherWsdlUrl;
    }

    public String getDispatcherService() {
        return dispatcherService;
    }

    public void setDispatcherService(String dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    public String getDispatcherPort() {
        return dispatcherPort;
    }

    public void setDispatcherPort(String dispatcherPort) {
        this.dispatcherPort = dispatcherPort;
    }

    public boolean isDispatcherUseAuthentication() {
        return dispatcherUseAuthentication;
    }

    public void setDispatcherUseAuthentication(boolean dispatcherUseAuthentication) {
        this.dispatcherUseAuthentication = dispatcherUseAuthentication;
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

    public String getDispatcherEnvelope() {
        return dispatcherEnvelope;
    }

    public void setDispatcherEnvelope(String dispatcherEnvelope) {
        this.dispatcherEnvelope = dispatcherEnvelope;
    }

    public boolean isDispatcherOneWay() {
        return dispatcherOneWay;
    }

    public void setDispatcherOneWay(boolean dispatcherOneWay) {
        this.dispatcherOneWay = dispatcherOneWay;
    }

    public boolean isDispatcherUseMtom() {
        return dispatcherUseMtom;
    }

    public void setDispatcherUseMtom(boolean dispatcherUseMtom) {
        this.dispatcherUseMtom = dispatcherUseMtom;
    }

    public List<String> getDispatcherAttachmentNames() {
        return dispatcherAttachmentNames;
    }

    public void setDispatcherAttachmentNames(List<String> dispatcherAttachmentNames) {
        this.dispatcherAttachmentNames = dispatcherAttachmentNames;
    }

    public List<String> getDispatcherAttachmentContents() {
        return dispatcherAttachmentContents;
    }

    public void setDispatcherAttachmentContents(List<String> dispatcherAttachmentContents) {
        this.dispatcherAttachmentContents = dispatcherAttachmentContents;
    }

    public List<String> getDispatcherAttachmentTypes() {
        return dispatcherAttachmentTypes;
    }

    public void setDispatcherAttachmentTypes(List<String> dispatcherAttachmentTypes) {
        this.dispatcherAttachmentTypes = dispatcherAttachmentTypes;
    }
    
    public String getDispatcherSoapAction() {
        return dispatcherSoapAction;
    }

    public void setDispatcherSoapAction(String dispatcherSoapAction) {
        this.dispatcherSoapAction = dispatcherSoapAction;
    }

    public String getProtocol() {
        return "WS";
    }

    public Dispatch<SOAPMessage> getDispatch() throws Exception {
        if (dispatch == null) {
            URL endpointUrl = WebServiceUtil.getWsdlUrl(this.getDispatcherWsdlUrl(), this.getDispatcherUsername(), this.getDispatcherPassword());
            QName serviceName = QName.valueOf(this.getDispatcherService());
            QName portName = QName.valueOf(this.getDispatcherPort());

            // create the service and dispatch
            logger.debug("Creating web service: url=" + endpointUrl.toString() + ", service=" + serviceName + ", port=" + portName);
            Service service = Service.create(endpointUrl, serviceName);

            dispatch = service.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
        }

        return dispatch;
    }
}
