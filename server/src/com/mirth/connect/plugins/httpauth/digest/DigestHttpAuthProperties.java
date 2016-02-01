/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.digest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.plugins.httpauth.HttpAuthConnectorPluginProperties;

public class DigestHttpAuthProperties extends HttpAuthConnectorPluginProperties {

    public enum Algorithm {
        MD5("MD5"), MD5_SESS("MD5-sess");

        private String value;

        private Algorithm(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum QOPMode {
        AUTH("auth"), AUTH_INT("auth-int");

        private String value;

        private QOPMode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private String realm;
    private Set<Algorithm> algorithms;
    private Set<QOPMode> qopModes;
    private String opaque;
    private Map<String, String> credentials;

    public DigestHttpAuthProperties() {
        super(AuthType.DIGEST);
        realm = "My Realm";
        algorithms = new LinkedHashSet<Algorithm>(Arrays.asList(Algorithm.values()));
        qopModes = new LinkedHashSet<QOPMode>(Arrays.asList(QOPMode.values()));
        opaque = "${UUID}";
        credentials = new LinkedHashMap<String, String>();
    }

    public DigestHttpAuthProperties(DigestHttpAuthProperties props) {
        super(AuthType.DIGEST);
        realm = props.getRealm();
        algorithms = new LinkedHashSet<Algorithm>(props.getAlgorithms());
        qopModes = new LinkedHashSet<QOPMode>(props.getQopModes());
        opaque = props.getOpaque();
        credentials = new LinkedHashMap<String, String>(props.getCredentials());
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public Set<Algorithm> getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(Set<Algorithm> algorithms) {
        this.algorithms = algorithms;
    }

    public Set<QOPMode> getQopModes() {
        return qopModes;
    }

    public void setQopModes(Set<QOPMode> qopModes) {
        this.qopModes = qopModes;
    }

    public String getOpaque() {
        return opaque;
    }

    public void setOpaque(String opaque) {
        this.opaque = opaque;
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
        purgedProperties.put("algorithms", algorithms);
        purgedProperties.put("qopModes", qopModes);
        purgedProperties.put("credentialsCount", credentials.size());
        return purgedProperties;
    }
}