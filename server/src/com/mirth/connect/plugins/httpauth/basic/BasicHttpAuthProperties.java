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

import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class BasicHttpAuthProperties extends HttpAuthConnectorPluginProperties {

    private String realm;
    private Map<String, String> credentials;
    private boolean isUseCredentialsVariable;
    private String credentialsVariable;

    public BasicHttpAuthProperties() {
        super(AuthType.BASIC);
        realm = "My Realm";
        credentials = new LinkedHashMap<String, String>();
        isUseCredentialsVariable = false;
        credentialsVariable = "";
    }

    public BasicHttpAuthProperties(BasicHttpAuthProperties props) {
        super(AuthType.BASIC);
        realm = props.getRealm();
        credentials = new LinkedHashMap<String, String>(props.getCredentialsMap());
        isUseCredentialsVariable = props.isUseCredentialsVariable();
        credentialsVariable = props.getCredentialsVariable();
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public Map<String, String> getCredentialsMap() {
        return credentials;
    }

    public void setCredentialsMap(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public boolean isUseCredentialsVariable() {
        return isUseCredentialsVariable;
    }

    public void setUseCredentialsVariable(boolean isUseCredentialsVariable) {
        this.isUseCredentialsVariable = isUseCredentialsVariable;
    }

    public String getCredentialsVariable() {
        return credentialsVariable;
    }

    public void setCredentialsVariable(String credentialsVariable) {
        this.credentialsVariable = credentialsVariable;
    }

    @Override
    public BasicHttpAuthProperties clone() {
        return new BasicHttpAuthProperties(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("authType", getAuthType());
        purgedProperties.put("credentialsCount", credentials.size());
        return purgedProperties;
    }
}