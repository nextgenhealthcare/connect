/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.custom;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;

public class CustomHttpAuthProperties extends HttpAuthConnectorPluginProperties {

    private String authenticatorClass;
    private Map<String, String> properties;

    public CustomHttpAuthProperties() {
        super(AuthType.CUSTOM);
        authenticatorClass = "";
        properties = new LinkedHashMap<String, String>();
    }

    public CustomHttpAuthProperties(CustomHttpAuthProperties props) {
        super(AuthType.CUSTOM);
        authenticatorClass = props.getAuthenticatorClass();
        properties = new LinkedHashMap<String, String>(props.getProperties());
    }

    public String getAuthenticatorClass() {
        return authenticatorClass;
    }

    public void setAuthenticatorClass(String authenticatorClass) {
        this.authenticatorClass = authenticatorClass;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("authType", getAuthType());
        purgedProperties.put("propertiesCount", properties.size());
        return purgedProperties;
    }
}