/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;

public class HttpStaticResource implements Serializable {

    public enum ResourceType {
        FILE, DIRECTORY, CUSTOM;

        @Override
        public String toString() {
            return WordUtils.capitalizeFully(super.toString());
        }

        public static ResourceType fromString(String value) {
            for (ResourceType resourceType : values()) {
                if (resourceType.toString().equals(value)) {
                    return resourceType;
                }
            }
            return null;
        }

        public static String[] stringValues() {
            ResourceType[] values = values();
            String[] stringValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                stringValues[i] = values[i].toString();
            }
            return stringValues;
        }
    }

    private String contextPath;
    private ResourceType resourceType;
    private String value;
    private String contentType;
    private Map<String, List<String>> queryParameters;

    public HttpStaticResource(String contextPath, ResourceType resourceType, String value, String contentType) {
        this(contextPath, resourceType, value, contentType, null);
    }

    public HttpStaticResource(String contextPath, ResourceType resourceType, String value, String contentType, Map<String, List<String>> queryParameters) {
        this.contextPath = contextPath;
        this.resourceType = resourceType;
        this.value = value;
        this.contentType = contentType;
        this.queryParameters = queryParameters;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, List<String>> queryParameters) {
        this.queryParameters = queryParameters;
    }
}
