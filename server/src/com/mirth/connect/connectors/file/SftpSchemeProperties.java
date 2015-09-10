/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class SftpSchemeProperties extends SchemeProperties {
    private boolean passwordAuth;
    private boolean keyAuth;
    private String keyFile;
    private String passPhrase;
    private String hostKeyChecking;
    private String knownHostsFile;
    private Map<String, String> configurationSettings;

    public SftpSchemeProperties() {
        passwordAuth = true;
        keyAuth = false;
        keyFile = "";
        passPhrase = "";
        hostKeyChecking = "ask";
        knownHostsFile = "";
        configurationSettings = new LinkedHashMap<String, String>();
    }

    public SftpSchemeProperties(SftpSchemeProperties props) {
        passwordAuth = props.isPasswordAuth();
        keyAuth = props.isKeyAuth();
        keyFile = props.getKeyFile();
        passPhrase = props.getPassPhrase();
        hostKeyChecking = props.getHostChecking();
        knownHostsFile = props.getKnownHostsFile();

        configurationSettings = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : props.getConfigurationSettings().entrySet()) {
            configurationSettings.put(entry.getKey(), entry.getValue());
        }
    }

    public boolean isPasswordAuth() {
        return passwordAuth;
    }

    public void setPasswordAuth(boolean passwordAuth) {
        this.passwordAuth = passwordAuth;
    }

    public boolean isKeyAuth() {
        return keyAuth;
    }

    public void setKeyAuth(boolean keyAuth) {
        this.keyAuth = keyAuth;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getHostChecking() {
        return hostKeyChecking;
    }

    public void setHostChecking(String hostKeyChecking) {
        this.hostKeyChecking = hostKeyChecking;
    }

    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    public void setKnownHostsFile(String knownHostsFile) {
        this.knownHostsFile = knownHostsFile;
    }

    public Map<String, String> getConfigurationSettings() {
        return configurationSettings;
    }

    public void setConfigurationSettings(Map<String, String> configurationSettings) {
        this.configurationSettings = configurationSettings;
    }

    @Override
    public SchemeProperties getFileSchemeProperties() {
        return this;
    }

    @Override
    public String toFormattedString() {
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";

        builder.append("HOST CHECKING: ");
        builder.append(hostKeyChecking);
        builder.append(newLine);

        if (MapUtils.isNotEmpty(configurationSettings)) {
            builder.append("[CONFIGURATION OPTIONS]");
            builder.append(newLine);

            for (Map.Entry<String, String> setting : configurationSettings.entrySet()) {
                builder.append(setting.getKey().toString());
                builder.append(": ");
                builder.append(setting.getValue().toString());
                builder.append(newLine);
            }
        }

        return builder.toString();
    }

    @Override
    public String getSummaryText() {
        StringBuilder builder = new StringBuilder();

        if (passwordAuth && keyAuth) {
            builder.append("Password and Public Key");
        } else if (keyAuth) {
            builder.append("Public Key");
        } else {
            builder.append("Password");
        }
        builder.append(" Authentication / Hostname Checking ");
        if (StringUtils.equals(hostKeyChecking, "yes")) {
            builder.append("On");
        } else if (StringUtils.equals(hostKeyChecking, "no")) {
            builder.append("Off");
        } else {
            builder.append("Ask");
        }

        return builder.toString();
    }

    @Override
    public SchemeProperties clone() {
        return new SftpSchemeProperties(this);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("passwordAuth", passwordAuth);
        purgedProperties.put("keyAuth", keyAuth);
        purgedProperties.put("hostKeyChecking", hostKeyChecking);
        purgedProperties.put("configurationSettingsCount", configurationSettings.size());

        return purgedProperties;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}