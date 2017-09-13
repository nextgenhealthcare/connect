/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class S3SchemeProperties extends SchemeProperties {

    private boolean useDefaultCredentialProviderChain;
    private boolean useTemporaryCredentials;
    private int duration;
    private Map<String, List<String>> customHeaders;

    public S3SchemeProperties() {
        useDefaultCredentialProviderChain = true;
        useTemporaryCredentials = true;
        duration = 7200;

        customHeaders = new LinkedHashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        values.add("AES256");
        customHeaders.put("x-amz-server-side​-encryption", values);
    }

    public S3SchemeProperties(S3SchemeProperties props) {
        useTemporaryCredentials = props.isUseTemporaryCredentials();
        duration = props.getDuration();

        customHeaders = new LinkedHashMap<String, List<String>>();
        if (MapUtils.isNotEmpty(props.getCustomHeaders())) {
            for (Entry<String, List<String>> entry : props.getCustomHeaders().entrySet()) {
                customHeaders.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
            }
        }
    }

    public boolean isUseDefaultCredentialProviderChain() {
        return useDefaultCredentialProviderChain;
    }

    public void setUseDefaultCredentialProviderChain(boolean useDefaultCredentialProviderChain) {
        this.useDefaultCredentialProviderChain = useDefaultCredentialProviderChain;
    }

    public boolean isUseTemporaryCredentials() {
        return useTemporaryCredentials;
    }

    public void setUseTemporaryCredentials(boolean useTemporaryCredentials) {
        this.useTemporaryCredentials = useTemporaryCredentials;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Map<String, List<String>> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, List<String>> customHeaders) {
        this.customHeaders = customHeaders;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("useDefaultCredentialProviderChain", useDefaultCredentialProviderChain);
        purgedProperties.put("useTemporaryCredentials", useTemporaryCredentials);
        purgedProperties.put("duration", duration);
        purgedProperties.put("customHeadersCount", customHeaders != null ? customHeaders.size() : 0);
        return purgedProperties;
    }

    @Override
    public SchemeProperties getFileSchemeProperties() {
        return this;
    }

    @Override
    public String getSummaryText() {
        StringBuilder builder = new StringBuilder();

        builder.append("Using ");
        if (useDefaultCredentialProviderChain) {
            builder.append("Default Credential Provider Chain");
        } else {
            builder.append("Explicit Credentials");
        }

        if (useTemporaryCredentials) {
            builder.append(" with STS temporary access");
        }

        return builder.toString();
    }

    @Override
    public String toFormattedString() {
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";

        if (MapUtils.isNotEmpty(customHeaders)) {
            builder.append("[CUSTOM HEADERS]");
            builder.append(newLine);

            for (Entry<String, List<String>> entry : customHeaders.entrySet()) {
                for (String value : entry.getValue()) {
                    builder.append(entry.getKey());
                    builder.append(": ");
                    builder.append(value);
                    builder.append(newLine);
                }
            }
        }

        return builder.toString();
    }

    @Override
    public SchemeProperties clone() {
        return new S3SchemeProperties(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
