package com.mirth.connect.connectors.file;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

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
            builder.append(hostKeyChecking);
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
}