/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.awt.Color;
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

    public static final Color DEFAULT_COLOR = new Color(0x9EB1C9);
    public static final String DEFAULT_LOGIN_NOTIFICATION_ENABLED_VALUE = "0";
    public static final String DEFAULT_LOGIN_NOTIFICATION_MESSAGE_VALUE = "";
    public static final String DEFAULT_ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_ENABLED_VALUE =  "0";
    public static final String DEFAULT_ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_FIELD_VALUE =  "5";

    private static final String CLEAR_GLOBAL_MAP = "server.resetglobalvariables";
    private static final String QUEUE_BUFFER_SIZE = "server.queuebuffersize";
    private static final String DEFAULT_METADATA_COLUMNS = "server.defaultmetadatacolumns";
    protected static final String DEFAULT_ADMINISTRATOR_COLOR = "server.defaultadministratorcolor";
    private static final String SMTP_HOST = "smtp.host";
    private static final String SMTP_PORT = "smtp.port";
    private static final String SMTP_TIMEOUT = "smtp.timeout";
    private static final String SMTP_FROM = "smtp.from";
    private static final String SMTP_SECURE = "smtp.secure";
    private static final String SMTP_AUTH = "smtp.auth";
    private static final String SMTP_USERNAME = "smtp.username";
    private static final String SMTP_PASSWORD = "smtp.password";
    protected static final String LOGIN_NOTIFICATION_ENABLED = "loginnotification.enabled";
    protected static final String LOGIN_NOTIFICATION_MESSAGE = "loginnotification.message";
    protected static final String ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_ENABLED = "administratorautologoutinterval.enabled";
    protected static final String ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_FIELD = "administratorautologoutinterval.field";
    
    // General
    private String environmentName;
    private String serverName;
    private Boolean clearGlobalMap;
    private Integer queueBufferSize;
    private List<MetaDataColumn> defaultMetaDataColumns;
    private Color defaultAdministratorBackgroundColor;

    // SMTP
    private String smtpHost;
    private String smtpPort;
    private String smtpTimeout;
    private String smtpFrom;
    private String smtpSecure;
    private Boolean smtpAuth;
    private String smtpUsername;
    private String smtpPassword;

    // Login Notification
    private Boolean loginNotificationEnabled;
    private String loginNotificationMessage;
    
    // Auto Logout
    private Boolean administratorAutoLogoutIntervalEnabled;
    private Integer administratorAutoLogoutIntervalField;
    
    public ServerSettings() {

    }

    public ServerSettings(String environmentName, String serverName, Properties properties) {
        setEnvironmentName(environmentName);
        setServerName(serverName);
        setProperties(properties);
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();

        if (getClearGlobalMap() != null) {
            properties.put(CLEAR_GLOBAL_MAP, BooleanUtils.toIntegerObject(getClearGlobalMap()).toString());
        }
        if (getQueueBufferSize() != null) {
            properties.put(QUEUE_BUFFER_SIZE, getQueueBufferSize().toString());
        }
        if (getDefaultMetaDataColumns() != null) {
            properties.put(DEFAULT_METADATA_COLUMNS, ObjectXMLSerializer.getInstance().serialize(getDefaultMetaDataColumns()));
        }
        if (getDefaultAdministratorBackgroundColor() != null) {
            properties.put(DEFAULT_ADMINISTRATOR_COLOR, ObjectXMLSerializer.getInstance().serialize(getDefaultAdministratorBackgroundColor()));
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
        if (getLoginNotificationEnabled() != null) {
            properties.put(LOGIN_NOTIFICATION_ENABLED, BooleanUtils.toIntegerObject(getLoginNotificationEnabled()).toString());
        }
        if (getLoginNotificationMessage() != null) {
            properties.put(LOGIN_NOTIFICATION_MESSAGE, getLoginNotificationMessage());
        }
        if (getAdministratorAutoLogoutIntervalEnabled() != null) {
            properties.put(ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_ENABLED, BooleanUtils.toIntegerObject(getAdministratorAutoLogoutIntervalEnabled()).toString());
        }
        if (getAdministratorAutoLogoutIntervalField() != null) {
            properties.put(ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_FIELD, getAdministratorAutoLogoutIntervalField().toString());
        }

        return properties;
    }

    @Override
    public void setProperties(Properties properties) {
        setClearGlobalMap(intToBooleanObject(properties.getProperty(CLEAR_GLOBAL_MAP)));
        setQueueBufferSize(toIntegerObject(properties.getProperty(QUEUE_BUFFER_SIZE)));
        setDefaultMetaDataColumns(toList(properties.getProperty(DEFAULT_METADATA_COLUMNS), MetaDataColumn.class, DefaultMetaData.DEFAULT_COLUMNS));
        setDefaultAdministratorBackgroundColor(deserialize(properties.getProperty(DEFAULT_ADMINISTRATOR_COLOR), Color.class, DEFAULT_COLOR));
        setSmtpHost(properties.getProperty(SMTP_HOST));
        setSmtpPort(properties.getProperty(SMTP_PORT));
        setSmtpTimeout(properties.getProperty(SMTP_TIMEOUT));
        setSmtpFrom(properties.getProperty(SMTP_FROM));
        setSmtpSecure(properties.getProperty(SMTP_SECURE));
        setSmtpAuth(intToBooleanObject(properties.getProperty(SMTP_AUTH)));
        setSmtpUsername(properties.getProperty(SMTP_USERNAME));
        setSmtpPassword(properties.getProperty(SMTP_PASSWORD));
        setLoginNotificationEnabled(intToBooleanObject(properties.getProperty(LOGIN_NOTIFICATION_ENABLED, DEFAULT_LOGIN_NOTIFICATION_ENABLED_VALUE)));
        setLoginNotificationMessage(properties.getProperty(LOGIN_NOTIFICATION_MESSAGE, DEFAULT_LOGIN_NOTIFICATION_MESSAGE_VALUE));
        setAdministratorAutoLogoutIntervalEnabled(intToBooleanObject(properties.getProperty(ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_ENABLED, DEFAULT_ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_ENABLED_VALUE)));
        setAdministratorAutoLogoutIntervalField(toIntegerObject(properties.getProperty(ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_FIELD, DEFAULT_ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_FIELD_VALUE)));
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
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

    public Color getDefaultAdministratorBackgroundColor() {
        return defaultAdministratorBackgroundColor;
    }

    public void setDefaultAdministratorBackgroundColor(Color defaultAdministratorBackgroundColor) {
        this.defaultAdministratorBackgroundColor = defaultAdministratorBackgroundColor;
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

    public Boolean getLoginNotificationEnabled() {
        return loginNotificationEnabled;
    }
    
    public void setLoginNotificationEnabled(Boolean loginNotificationEnabled) {
        this.loginNotificationEnabled = loginNotificationEnabled;
    }
    
    public String getLoginNotificationMessage() {
        return loginNotificationMessage;
    }
    
    public void setLoginNotificationMessage(String loginNotificationMessage) {
        this.loginNotificationMessage = loginNotificationMessage;
    }
    
    public Boolean getAdministratorAutoLogoutIntervalEnabled() {
        return administratorAutoLogoutIntervalEnabled;
    }
    
    public void setAdministratorAutoLogoutIntervalEnabled(Boolean administratorAutoLogoutIntervalEnabled) {
        this.administratorAutoLogoutIntervalEnabled = administratorAutoLogoutIntervalEnabled;
    }
    
    public Integer getAdministratorAutoLogoutIntervalField() {
        return administratorAutoLogoutIntervalField;
    }
    
    public void setAdministratorAutoLogoutIntervalField(Integer administratorAutoLogoutIntervalField) {
        this.administratorAutoLogoutIntervalField = administratorAutoLogoutIntervalField;
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
        purgedProperties.put("defaultAdministratorBackgroundColor", defaultAdministratorBackgroundColor);
        purgedProperties.put("smtpTimeout", PurgeUtil.getNumericValue(smtpTimeout));
        purgedProperties.put("smtpSecure", smtpSecure);
        purgedProperties.put("smtpAuth", smtpAuth);
        return purgedProperties;
    }
}
