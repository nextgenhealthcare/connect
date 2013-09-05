/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server;

import java.util.Properties;

import com.mirth.connect.donkey.server.event.EventDispatcher;

public class DonkeyConfiguration {
    private String appData;
    private Properties databaseProperties;
    private Encryptor encryptor;
    private EventDispatcher eventDispatcher;
    private String serverId;

    public DonkeyConfiguration(String appData, Properties databaseProperties, Encryptor encryptor, EventDispatcher eventDispatcher, String serverId) {
        this.appData = appData;
        this.databaseProperties = databaseProperties;
        this.encryptor = encryptor;
        this.eventDispatcher = eventDispatcher;
        this.serverId = serverId;
    }

    public String getAppData() {
        return appData;
    }

    public void setAppData(String appData) {
        this.appData = appData;
    }

    public Properties getDatabaseProperties() {
        return databaseProperties;
    }

    public void setDatabaseProperties(Properties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(Encryptor encryptor) {
        this.encryptor = encryptor;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
