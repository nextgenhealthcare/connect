/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.doc;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class DocumentDispatcherProperties extends ConnectorProperties implements DestinationConnectorPropertiesInterface {

    private DestinationConnectorProperties destinationConnectorProperties;

    private String host;
    private String outputPattern;
    private String documentType;
    private boolean encrypt;
    private String output;
    private String password;
    private String template;

    public static final String DOCUMENT_TYPE_PDF = "pdf";
    public static final String DOCUMENT_TYPE_RTF = "rtf";

    public DocumentDispatcherProperties() {
        destinationConnectorProperties = new DestinationConnectorProperties();

        this.host = "";
        this.outputPattern = "";
        this.documentType = DOCUMENT_TYPE_PDF;
        this.encrypt = false;
        this.password = "";
        this.template = "";
        this.output = "";
    }

    public DocumentDispatcherProperties(DocumentDispatcherProperties props) {
        super(props);
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());

        host = props.getHost();
        outputPattern = props.getOutputPattern();
        documentType = props.getDocumentType();
        encrypt = props.isEncrypt();
        password = props.getPassword();
        template = props.getTemplate();
        output = props.getOutput();
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

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
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
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";

        if (StringUtils.isNotBlank(output)) {
            builder.append("OUTPUT: ");
            if (output.equalsIgnoreCase("file")) {
                builder.append("File");
            } else if (output.equalsIgnoreCase("attachment")) {
                builder.append("Attachment");
            } else if (output.equalsIgnoreCase("both")) {
                builder.append("File and Attachment");
            }

            builder.append(newLine);
        }

        if (StringUtils.isBlank(output) || !output.equalsIgnoreCase("attachment")) {
            builder.append("URI: ");
            appendURIString(builder);
            builder.append(newLine);
        }

        builder.append("DOCUMENT TYPE: ");
        builder.append(documentType);
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[CONTENT]");
        builder.append(newLine);
        builder.append(template);
        return builder.toString();
    }

    public String toURIString() {
        StringBuilder builder = new StringBuilder();
        appendURIString(builder);
        return builder.toString();
    }

    private void appendURIString(StringBuilder builder) {
        builder.append(host);
        if (host.charAt(host.length() - 1) != '/') {
            builder.append("/");
        }
        builder.append(outputPattern);
    }

    @Override
    public DestinationConnectorProperties getDestinationConnectorProperties() {
        return destinationConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new DocumentDispatcherProperties(this);
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
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("documentType", documentType);
        purgedProperties.put("encrypt", encrypt);
        purgedProperties.put("templateLines", PurgeUtil.countLines(template));
        return purgedProperties;
    }
}
