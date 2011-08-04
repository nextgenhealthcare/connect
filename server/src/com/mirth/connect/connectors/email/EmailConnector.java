/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.email;

import java.util.List;

import org.mule.providers.AbstractServiceEnabledConnector;

/**
 * The Email connector is used to send and receive email messages using a
 * designated SMTP/IMAP/POP3 server.
 * 
 * @author GeraldB
 * 
 */
public class EmailConnector extends AbstractServiceEnabledConnector {
    private String channelId;

    // dispatcher properties
    private String dispatcherSmtpHost;
    private int dispatcherSmtpPort = 25;
    private int dispatcherTimeout = 5000;
    private String dispatcherEncryption;
    private boolean dispatcherAuthentication = false;
    private String dispatcherUsername;
    private String dispatcherPassword;
    private String dispatcherTo;
    private String dispatcherCc;
    private String dispatcherBcc;
    private String dispatcherReplyTo;
    private String dispatcherFrom;
    private String dispatcherSubject;
    private String dispatcherBody;
    private boolean dispatcherHtml = false;
    private List<Attachment> dispatcherAttachments;

    // receiver properties
    private String receiverProtocol;
    private String receiverHost;
    private int receiverPort;
    private String receiverEncryption;
    private String receiverUsername;
    private String receiverPassword;

    private String pollingType;
    private long pollingFrequency;
    private String pollingTime;

    private String receiverContent;
    private String receiverFilterExpression;

    private String receiverFolder;
    private boolean receiverProcessUnread;
    private String receiverOnProcess;
    private String receiverMoveFolder;
    private boolean receiverMarkRead;
    private String receiverMoveError;

    @Override
    public String getProtocol() {
        return "email";
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getDispatcherSmtpHost() {
        return dispatcherSmtpHost;
    }

    public void setDispatcherSmtpHost(String dispatcherSmtpHost) {
        this.dispatcherSmtpHost = dispatcherSmtpHost;
    }

    public int getDispatcherSmtpPort() {
        return dispatcherSmtpPort;
    }

    public void setDispatcherSmtpPort(int dispatcherSmtpPort) {
        this.dispatcherSmtpPort = dispatcherSmtpPort;
    }

    public int getDispatcherTimeout() {
        return dispatcherTimeout;
    }

    public void setDispatcherTimeout(int dispatcherTimeout) {
        this.dispatcherTimeout = dispatcherTimeout;
    }

    public String getDispatcherEncryption() {
        return dispatcherEncryption;
    }

    public void setDispatcherEncryption(String dispatcherEncryption) {
        this.dispatcherEncryption = dispatcherEncryption;
    }

    public boolean isDispatcherAuthentication() {
        return dispatcherAuthentication;
    }

    public void setDispatcherAuthentication(boolean dispatcherAuthentication) {
        this.dispatcherAuthentication = dispatcherAuthentication;
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

    public String getDispatcherTo() {
        return dispatcherTo;
    }

    public void setDispatcherTo(String dispatcherTo) {
        this.dispatcherTo = dispatcherTo;
    }

    public String getDispatcherCc() {
        return dispatcherCc;
    }

    public void setDispatcherCc(String dispatcherCc) {
        this.dispatcherCc = dispatcherCc;
    }

    public String getDispatcherBcc() {
        return dispatcherBcc;
    }

    public void setDispatcherBcc(String dispatcherBcc) {
        this.dispatcherBcc = dispatcherBcc;
    }

    public String getDispatcherReplyTo() {
        return dispatcherReplyTo;
    }

    public void setDispatcherReplyTo(String dispatcherReplyTo) {
        this.dispatcherReplyTo = dispatcherReplyTo;
    }

    public String getDispatcherFrom() {
        return dispatcherFrom;
    }

    public void setDispatcherFrom(String dispatcherFrom) {
        this.dispatcherFrom = dispatcherFrom;
    }

    public String getDispatcherSubject() {
        return dispatcherSubject;
    }

    public void setDispatcherSubject(String dispatcherSubject) {
        this.dispatcherSubject = dispatcherSubject;
    }

    public String getDispatcherBody() {
        return dispatcherBody;
    }

    public void setDispatcherBody(String dispatcherBody) {
        this.dispatcherBody = dispatcherBody;
    }

    public boolean isDispatcherHtml() {
        return dispatcherHtml;
    }

    public void setDispatcherHtml(boolean dispatcherHtml) {
        this.dispatcherHtml = dispatcherHtml;
    }

    public List<Attachment> getDispatcherAttachments() {
        return dispatcherAttachments;
    }

    public void setDispatcherAttachments(List<Attachment> dispatcherAttachments) {
        this.dispatcherAttachments = dispatcherAttachments;
    }

    public String getReceiverProtocol() {
        return receiverProtocol;
    }

    public void setReceiverProtocol(String receiverProtocol) {
        this.receiverProtocol = receiverProtocol;
    }

    public String getReceiverHost() {
        return receiverHost;
    }

    public void setReceiverHost(String receiverHost) {
        this.receiverHost = receiverHost;
    }

    public int getReceiverPort() {
        return receiverPort;
    }

    public void setReceiverPort(int receiverPort) {
        this.receiverPort = receiverPort;
    }

    public String getReceiverEncryption() {
        return receiverEncryption;
    }

    public void setReceiverEncryption(String receiverEncryption) {
        this.receiverEncryption = receiverEncryption;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getReceiverPassword() {
        return receiverPassword;
    }

    public void setReceiverPassword(String receiverPassword) {
        this.receiverPassword = receiverPassword;
    }

    public String getPollingType() {
        return pollingType;
    }

    public void setPollingType(String pollingType) {
        this.pollingType = pollingType;
    }

    public long getPollingFrequency() {
        return pollingFrequency;
    }

    public void setPollingFrequency(long pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
    }

    public String getPollingTime() {
        return pollingTime;
    }

    public void setPollingTime(String pollingTime) {
        this.pollingTime = pollingTime;
    }

    public String getReceiverContent() {
        return receiverContent;
    }

    public void setReceiverContent(String receiverContent) {
        this.receiverContent = receiverContent;
    }

    public String getReceiverFilterExpression() {
        return receiverFilterExpression;
    }

    public void setReceiverFilterExpression(String receiverFilterExpression) {
        this.receiverFilterExpression = receiverFilterExpression;
    }

    public String getReceiverFolder() {
        return receiverFolder;
    }

    public void setReceiverFolder(String receiverFolder) {
        this.receiverFolder = receiverFolder;
    }

    public boolean isReceiverProcessUnread() {
        return receiverProcessUnread;
    }

    public void setReceiverProcessUnread(boolean receiverProcessUnread) {
        this.receiverProcessUnread = receiverProcessUnread;
    }

    public String getReceiverOnProcess() {
        return receiverOnProcess;
    }

    public void setReceiverOnProcess(String receiverOnProcess) {
        this.receiverOnProcess = receiverOnProcess;
    }

    public String getReceiverMoveFolder() {
        return receiverMoveFolder;
    }

    public void setReceiverMoveFolder(String receiverMoveFolder) {
        this.receiverMoveFolder = receiverMoveFolder;
    }

    public boolean isReceiverMarkRead() {
        return receiverMarkRead;
    }

    public void setReceiverMarkRead(boolean receiverMarkRead) {
        this.receiverMarkRead = receiverMarkRead;
    }

    public String getReceiverMoveError() {
        return receiverMoveError;
    }

    public void setReceiverMoveError(String receiverMoveError) {
        this.receiverMoveError = receiverMoveError;
    }
}
