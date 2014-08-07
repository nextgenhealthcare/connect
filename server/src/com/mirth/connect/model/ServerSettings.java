/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.DefaultMetaData;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("serverSettings")
public class ServerSettings extends AbstractSettings implements Serializable, Auditable, Purgable {

    private static final String SERVER_NAME = "server.name";
    private static final String ORGANIZATION_NAME = "server.organization";
    private static final String CLEAR_GLOBAL_MAP = "server.resetglobalvariables";
    private static final String QUEUE_BUFFER_SIZE = "server.queuebuffersize";
    private static final String DEFAULT_METADATA_COLUMNS = "server.defaultmetadatacolumns";
    private static final String SMTP_HOST = "smtp.host";
    private static final String SMTP_PORT = "smtp.port";
    private static final String SMTP_TIMEOUT = "smtp.timeout";
    private static final String SMTP_FROM = "smtp.from";
    private static final String SMTP_SECURE = "smtp.secure";
    private static final String SMTP_AUTH = "smtp.auth";
    private static final String SMTP_USERNAME = "smtp.username";
    private static final String SMTP_PASSWORD = "smtp.password";

    // General
    private String serverName;
    private String organizationName;
    private Boolean clearGlobalMap;
    private Integer queueBufferSize;
    private List<MetaDataColumn> defaultMetaDataColumns;

    // SMTP
    private String smtpHost;
    private String smtpPort;
    private String smtpTimeout;
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
        
        if (getServerName() != null) {
            properties.put(SERVER_NAME, getServerName());
        }
        
        if (getOrganizationName() != null) {
            properties.put(ORGANIZATION_NAME, getOrganizationName());
        }

        if (getClearGlobalMap() != null) {
            properties.put(CLEAR_GLOBAL_MAP, BooleanUtils.toIntegerObject(getClearGlobalMap()).toString());
        }
        if (getQueueBufferSize() != null) {
            properties.put(QUEUE_BUFFER_SIZE, getQueueBufferSize().toString());
        }
        if (getDefaultMetaDataColumns() != null) {
            properties.put(DEFAULT_METADATA_COLUMNS, ObjectXMLSerializer.getInstance().serialize(getDefaultMetaDataColumns()));
        }
        if (getSmtpHost() != null) {
            properties.put(SMTP_HOST, getSmtpHost());
        }
        if (getSmtpPort() != null) {
            properties.put(SMTP_PORT, getSmtpPort());
        }
        if (getSmtpTimeout() != null) {
            properties.put(SMTP_TIMEOUT, getSmtpTimeout().toString());
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
        setServerName(properties.getProperty(SERVER_NAME));
        setOrganizationName(properties.getProperty(ORGANIZATION_NAME));
        setClearGlobalMap(intToBooleanObject(properties.getProperty(CLEAR_GLOBAL_MAP)));
        setQueueBufferSize(toIntegerObject(properties.getProperty(QUEUE_BUFFER_SIZE)));
        setDefaultMetaDataColumns(toList(properties.getProperty(DEFAULT_METADATA_COLUMNS), MetaDataColumn.class, DefaultMetaData.DEFAULT_COLUMNS));
        setSmtpHost(properties.getProperty(SMTP_HOST));
        setSmtpPort(properties.getProperty(SMTP_PORT));
        setSmtpTimeout(properties.getProperty(SMTP_TIMEOUT));
        setSmtpFrom(properties.getProperty(SMTP_FROM));
        setSmtpSecure(properties.getProperty(SMTP_SECURE));
        setSmtpAuth(intToBooleanObject(properties.getProperty(SMTP_AUTH)));
        setSmtpUsername(properties.getProperty(SMTP_USERNAME));
        setSmtpPassword(properties.getProperty(SMTP_PASSWORD));
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public String getOrganizationName() {
        return organizationName;
    }
    
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Boolean getClearGlobalMap() {
        return clearGlobalMap;
    }

    public void setClearGlobalMap(Boolean clearGlobalMap) {
        this.clearGlobalMap = clearGlobalMap;
    }

    public Integer getQueueBufferSize() {
        return queueBufferSize;
    }

    public void setQueueBufferSize(Integer queueBufferSize) {
        this.queueBufferSize = queueBufferSize;
    }

    public List<MetaDataColumn> getDefaultMetaDataColumns() {
        return defaultMetaDataColumns;
    }

    public void setDefaultMetaDataColumns(List<MetaDataColumn> defaultMetaDataColumns) {
        this.defaultMetaDataColumns = defaultMetaDataColumns;
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

    public String getSmtpTimeout() {
        return smtpTimeout;
    }

    public void setSmtpTimeout(String smtpTimeout) {
        this.smtpTimeout = smtpTimeout;
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

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("clearGlobalMap", clearGlobalMap);
        purgedProperties.put("queueBufferSize", queueBufferSize);
        purgedProperties.put("defaultMetaDataColumns", PurgeUtil.purgeList(defaultMetaDataColumns));
        purgedProperties.put("smtpTimeout", PurgeUtil.getNumericValue(smtpTimeout));
        purgedProperties.put("smtpSecure", smtpSecure);
        purgedProperties.put("smtpAuth", smtpAuth);
        return purgedProperties;
    }
}
