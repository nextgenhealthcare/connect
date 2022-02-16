package com.mirth.connect.model;

import java.awt.Color;
import java.util.Properties;

public class PublicServerSettings extends ServerSettings {

    public PublicServerSettings(ServerSettings serverSettings) {
        setEnvironmentName(serverSettings.getEnvironmentName());
        setServerName(serverSettings.getServerName());
        setProperties(serverSettings.getProperties());
    }
    
    @Override
    public void setProperties(Properties properties) {
        setDefaultAdministratorBackgroundColor(deserialize(properties.getProperty(DEFAULT_ADMINISTRATOR_COLOR), Color.class, DEFAULT_COLOR));
        setLoginNotificationEnabled(intToBooleanObject(properties.getProperty(LOGIN_NOTIFICATION_ENABLED)));
        setLoginNotificationMessage(properties.getProperty(LOGIN_NOTIFICATION_MESSAGE));
    }
    
}
