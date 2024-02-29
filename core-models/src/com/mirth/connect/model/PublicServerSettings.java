package com.mirth.connect.model;

import java.awt.Color;
import java.util.Properties;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.util.Serializer;
import com.mirth.connect.model.util.DefaultMetaData;

@SuppressWarnings("serial")
public class PublicServerSettings extends ServerSettings {

    public PublicServerSettings(ServerSettings serverSettings, Serializer serializer) {
        setEnvironmentName(serverSettings.getEnvironmentName());
        setServerName(serverSettings.getServerName());
        setProperties(serverSettings.getProperties(serializer), serializer);
    }

    @Override
    public void setProperties(Properties properties, Serializer serializer) {
        setDefaultAdministratorBackgroundColor(deserialize(properties.getProperty(DEFAULT_ADMINISTRATOR_COLOR), serializer, Color.class, DEFAULT_COLOR));
        setLoginNotificationEnabled(intToBooleanObject(properties.getProperty(LOGIN_NOTIFICATION_ENABLED)));
        setLoginNotificationMessage(properties.getProperty(LOGIN_NOTIFICATION_MESSAGE));
        setAdministratorAutoLogoutIntervalEnabled(intToBooleanObject(properties.getProperty(ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_ENABLED)));
        setAdministratorAutoLogoutIntervalField(toIntegerObject(properties.getProperty(ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_FIELD, DEFAULT_ADMINISTRATOR_AUTO_LOGOUT_INTERVAL_FIELD_VALUE.toString())));
        setQueueBufferSize(toIntegerObject(properties.getProperty(QUEUE_BUFFER_SIZE)));
        setDefaultMetaDataColumns(toList(properties.getProperty(DEFAULT_METADATA_COLUMNS), serializer, MetaDataColumn.class, DefaultMetaData.DEFAULT_COLUMNS));
    }
}
