/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.model;

import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("serverSettings")
public class ServerSettings extends AbstractSettings implements Auditable {

    private static final String CLEAR_GLOBAL_MAP = "server.resetglobalvariables";
    private static final String MAX_QUEUE_SIZE = "server.maxqueuesize";
    private static final String SMTP_HOST = "smtp.host";
    private static final String SMTP_PORT = "smtp.port";
    private static final String SMTP_FROM = "smtp.from";
    private static final String SMTP_SECURE = "smtp.secure";
    private static final String SMTP_AUTH = "smtp.auth";
    private static final String SMTP_USERNAME = "smtp.username";
    private static final String SMTP_PASSWORD = "smtp.password";

    // Configuration
    private Boolean clearGlobalMap;
    private Integer maxQueueSize;

    // SMTP
    private String smtpHost;
    private String smtpPort;
    private String smtpFrom;
    private String smtpSecure;
    private Boolean smtpAuth;
    private String smtpUsername;
    private String smtpPassword;

    public ServerSettings() {

    }

    public ServerSettings(Properties properties) {
        setProperties(properties);
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        if (getClearGlobalMap() != null) {
            properties.put(CLEAR_GLOBAL_MAP, BooleanUtils.toIntegerObject(getClearGlobalMap()).toString());
        }
        if (getMaxQueueSize() != null) {
            properties.put(MAX_QUEUE_SIZE, getMaxQueueSize().toString());
        }
        if (getSmtpHost() != null) {
            properties.put(SMTP_HOST, getSmtpHost());
        }
        if (getSmtpPort() != null) {
            properties.put(SMTP_PORT, getSmtpPort());
        }
        if (getSmtpFrom() != null) {
            properties.put(SMTP_FROM, getSmtpFrom());
        }
        if (getSmtpSecure() != null) {
            properties.put(SMTP_SECURE, getSmtpSecure());
        }
        if (getSmtpAuth() != null) {
            properties.put(SMTP_AUTH, BooleanUtils.toIntegerObject(getSmtpAuth()).toString());
        }
        if (getSmtpUsername() != null) {
            properties.put(SMTP_USERNAME, getSmtpUsername());
        }
        if (getSmtpPassword() != null) {
            properties.put(SMTP_PASSWORD, getSmtpPassword());
        }

        return properties;
    }

    public void setProperties(Properties properties) {
        setClearGlobalMap(intToBooleanObject((String) properties.get(CLEAR_GLOBAL_MAP)));
        setMaxQueueSize(toIntegerObject((String) properties.get(MAX_QUEUE_SIZE)));
        setSmtpHost((String) properties.get(SMTP_HOST));
        setSmtpPort((String) properties.get(SMTP_PORT));
        setSmtpFrom((String) properties.get(SMTP_FROM));
        setSmtpSecure((String) properties.get(SMTP_SECURE));
        setSmtpAuth(intToBooleanObject((String) properties.get(SMTP_AUTH)));
        setSmtpUsername((String) properties.get(SMTP_USERNAME));
        setSmtpPassword((String) properties.get(SMTP_PASSWORD));
    }
    
    public Boolean getClearGlobalMap() {
        return clearGlobalMap;
    }

    public void setClearGlobalMap(Boolean clearGlobalMap) {
        this.clearGlobalMap = clearGlobalMap;
    }

    public Integer getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(Integer maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpFrom() {
        return smtpFrom;
    }

    public void setSmtpFrom(String smtpFrom) {
        this.smtpFrom = smtpFrom;
    }

    public String getSmtpSecure() {
        return smtpSecure;
    }

    public void setSmtpSecure(String smtpSecure) {
        this.smtpSecure = smtpSecure;
    }

    public Boolean getSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(Boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    @Override
    public String toAuditString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

}
