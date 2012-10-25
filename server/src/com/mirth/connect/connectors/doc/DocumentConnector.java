/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.VariableFilenameParser;

import com.mirth.connect.connectors.file.FilenameParser;

public class DocumentConnector extends AbstractServiceEnabledConnector {
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_TEMPLATE = "template";
    public static final String PROPERTY_OUTPUT_PATTERN = "outputPattern";

    private FilenameParser filenameParser = new VariableFilenameParser();
    private String template;
    private String outputPattern;
    private boolean encrypt;
    private String password;
    private String documentType;
    private String channelId;

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getOutputPattern() {
        return this.outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
    }

    public FilenameParser getFilenameParser() {
        return this.filenameParser;
    }

    public void setFilenameParser(FilenameParser filenameParser) {
        this.filenameParser = filenameParser;
    }

    public boolean isEncrypt() {
        return this.encrypt;
    }

    public void setEncrypt(boolean encrypted) {
        this.encrypt = encrypted;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDocumentType() {
        return this.documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getProtocol() {
        return "doc";
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
