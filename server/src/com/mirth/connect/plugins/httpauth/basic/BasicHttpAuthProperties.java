/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.basic;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;

public class BasicHttpAuthProperties extends HttpAuthConnectorPluginProperties {

    private String realm;
    private Map<String, String> credentials;

    public BasicHttpAuthProperties() {
        super(AuthType.BASIC);
        realm = "My Realm";
        credentials = new LinkedHashMap<String, String>();
    }

    public BasicHttpAuthProperties(BasicHttpAuthProperties props) {
        super(AuthType.BASIC);
        realm = props.getRealm();
        credentials = new LinkedHashMap<String, String>(props.getCredentials());
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("authType", getAuthType());
        purgedProperties.put("credentialsCount", credentials.size());
        return purgedProperties;
    }
}