/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;

public class DocumentDispatcherProperties extends ConnectorProperties implements QueueConnectorPropertiesInterface {

    private QueueConnectorProperties queueConnectorProperties;

    private String host;
    private String outputPattern;
    private String documentType;
    private boolean encrypt;
    private String password;
    private String template;
    
    public static final String DOCUMENT_TYPE_PDF = "pdf";
    public static final String DOCUMENT_TYPE_RTF = "rtf";

    public DocumentDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        this.host = "";
        this.outputPattern = "";
        this.documentType = DOCUMENT_TYPE_PDF;
        this.encrypt = false;
        this.password = "";
        this.template = "";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOutputPattern() {
        return outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public String getProtocol() {
        return "doc";
    }

    @Override
    public String getName() {
        return "Document Writer";
    }

    @Override
    public String toFormattedString() {
        return null;
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }
}
