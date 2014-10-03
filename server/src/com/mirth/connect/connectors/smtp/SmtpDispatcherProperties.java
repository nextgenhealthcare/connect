/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.util.CharsetUtils;

public class SmtpDispatcherProperties extends ConnectorProperties implements DestinationConnectorPropertiesInterface {

    private DestinationConnectorProperties destinationConnectorProperties;

    private String smtpHost;
    private String smtpPort;
    private String timeout;
    private String encryption;
    private boolean authentication;
    private String username;
    private String password;
    private String to;
    private String from;
    private String cc;
    private String bcc;
    private String replyTo;
    private Map<String, String> headers;
    private String subject;
    private String charsetEncoding;
    private boolean html;
    private String body;
    private List<Attachment> attachments;

    public SmtpDispatcherProperties() {
        destinationConnectorProperties = new DestinationConnectorProperties();

        this.smtpHost = "";
        this.smtpPort = "25";
        this.timeout = "5000";
        this.encryption = "none";
        this.authentication = false;
        this.username = "";
        this.password = "";
        this.to = "";
        this.from = "";
        this.cc = "";
        this.bcc = "";
        this.replyTo = "";
        this.headers = new LinkedHashMap<String, String>();
        this.subject = "";
        this.charsetEncoding = CharsetUtils.DEFAULT_ENCODING;
        this.html = false;
        this.body = "";
        this.attachments = new ArrayList<Attachment>();
    }

    public SmtpDispatcherProperties(SmtpDispatcherProperties props) {
        super(props);
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());

        smtpHost = props.getSmtpHost();
        smtpPort = props.getSmtpPort();
        timeout = props.getTimeout();
        encryption = props.getEncryption();
        authentication = props.isAuthentication();
        username = props.getUsername();
        password = props.getPassword();
        to = props.getTo();
        from = props.getFrom();
        cc = props.getCc();
        bcc = props.getBcc();
        replyTo = props.getReplyTo();
        headers = new LinkedHashMap<String, String>(props.getHeaders());
        subject = props.getSubject();
        charsetEncoding = props.getCharsetEncoding();
        html = props.isHtml();
        body = props.getBody();

        attachments = new ArrayList<Attachment>();
        for (Attachment attachment : props.getAttachments()) {
            attachments.add(new Attachment(attachment));
        }
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public boolean isAuthentication() {
        return authentication;
    }

    public void setAuthentication(boolean authentication) {
        this.authentication = authentication;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String getProtocol() {
        return "SMTP";
    }

    @Override
    public String getName() {
        return "SMTP Sender";
    }

    @Override
    public String toFormattedString() {
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";

        builder.append("HOST: ");
        builder.append(smtpHost + ":" + smtpPort);
        builder.append(newLine);

        if (StringUtils.isNotBlank(username)) {
            builder.append("USERNAME: ");
            builder.append(username);
            builder.append(newLine);
        }

        builder.append("TO: ");
        builder.append(to);
        builder.append(newLine);

        builder.append("FROM: ");
        builder.append(from);
        builder.append(newLine);

        builder.append("CC: ");
        builder.append(cc);
        builder.append(newLine);

        builder.append("SUBJECT: ");
        builder.append(subject);
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[HEADERS]");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.append(newLine);
            builder.append(header.getKey() + ": " + header.getValue());
        }
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[ATTACHMENTS]");
        for (Attachment attachment : attachments) {
            builder.append(newLine);
            builder.append(attachment.getName());
            builder.append(" (");
            builder.append(attachment.getMimeType());
            builder.append(")");
        }
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[CONTENT]");
        builder.append(newLine);
        builder.append(body);
        return builder.toString();
    }

    @Override
    public DestinationConnectorProperties getDestinationConnectorProperties() {
        return destinationConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new SmtpDispatcherProperties(this);
    }

    @Override
    public boolean canValidateResponse() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        super.migrate3_1_0(element);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("destinationConnectorProperties", destinationConnectorProperties.getPurgedProperties());
        purgedProperties.put("timeout", PurgeUtil.getNumericValue(timeout));
        purgedProperties.put("encryption", encryption);
        purgedProperties.put("authentication", authentication);
        purgedProperties.put("headerChars", headers.size());
        purgedProperties.put("charsetEncoding", charsetEncoding);
        purgedProperties.put("html", html);
        purgedProperties.put("bodyLines", PurgeUtil.countLines(body));
        purgedProperties.put("attachmentCount", attachments.size());
        return purgedProperties;
    }
}
