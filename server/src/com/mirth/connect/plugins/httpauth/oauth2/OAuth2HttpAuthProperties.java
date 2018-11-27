/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.oauth2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.donkey.model.channel.ConnectorPluginProperties;
import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;

public class OAuth2HttpAuthProperties extends HttpAuthConnectorPluginProperties {

    public enum TokenLocation {
        HEADER("Request Header"), QUERY("Query Parameter");

        private String value;

        private TokenLocation(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private TokenLocation tokenLocation;
    private String locationKey;
    private String verificationURL;
    private Set<ConnectorPluginProperties> connectorPluginProperties;

    public OAuth2HttpAuthProperties() {
        super(AuthType.OAUTH2_VERIFICATION);
        tokenLocation = TokenLocation.HEADER;
        locationKey = "Authorization";
        verificationURL = "";
    }

    public OAuth2HttpAuthProperties(OAuth2HttpAuthProperties props) {
        super(AuthType.OAUTH2_VERIFICATION);
        tokenLocation = props.getTokenLocation();
        locationKey = props.getLocationKey();
        verificationURL = props.getVerificationURL();

        if (props.getConnectorPluginProperties() != null) {
            connectorPluginProperties = new HashSet<ConnectorPluginProperties>();
            for (ConnectorPluginProperties pluginProperties : props.getConnectorPluginProperties()) {
                connectorPluginProperties.add(pluginProperties.clone());
            }
        }
    }

    public TokenLocation getTokenLocation() {
        return tokenLocation;
    }

    public void setTokenLocation(TokenLocation tokenLocation) {
        this.tokenLocation = tokenLocation;
    }

    public String getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }

    public String getVerificationURL() {
        return verificationURL;
    }

    public void setVerificationURL(String verificationURL) {
        this.verificationURL = verificationURL;
    }

    public Set<ConnectorPluginProperties> getConnectorPluginProperties() {
        return connectorPluginProperties;
    }

    public void setConnectorPluginProperties(Set<ConnectorPluginProperties> connectorPluginProperties) {
        this.connectorPluginProperties = connectorPluginProperties;
    }

    @Override
    public OAuth2HttpAuthProperties clone() {
        return new OAuth2HttpAuthProperties(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("authType", getAuthType());
        purgedProperties.put("tokenLocation", tokenLocation);
        if (connectorPluginProperties != null) {
            Set<Map<String, Object>> purgedPluginProperties = new HashSet<Map<String, Object>>();
            for (ConnectorPluginProperties cpp : connectorPluginProperties) {
                purgedPluginProperties.add(cpp.getPurgedProperties());
            }
            purgedProperties.put("connectorPluginProperties", purgedPluginProperties);
        }
        return purgedProperties;
    }
}