/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;

public class DatabaseReceiverProperties extends ConnectorProperties implements PollConnectorPropertiesInterface, ResponseConnectorPropertiesInterface {
    public static final String NAME = "Database Reader";
    public static final String DRIVER_DEFAULT = "Please Select One";
    public static final int UPDATE_NEVER = 1;
    public static final int UPDATE_ONCE = 2;
    public static final int UPDATE_EACH = 3;

    private PollConnectorProperties pollConnectorProperties;
    private ResponseConnectorProperties responseConnectorProperties;
    private String driver;
    private String url;
    private String username;
    private String password;
    private String select;
    private String update;
    private boolean useScript;
    private boolean cacheResults;
    private boolean keepConnectionOpen;
    private int updateMode;
    private String retryCount;
    private String retryInterval;
    private String fetchSize;

    public DatabaseReceiverProperties() {
        pollConnectorProperties = new PollConnectorProperties();
        responseConnectorProperties = new ResponseConnectorProperties();
        driver = DRIVER_DEFAULT;
        url = "";
        username = "";
        password = "";
        select = "";
        update = "";
        useScript = false;
        cacheResults = true;
        keepConnectionOpen = true;
        updateMode = UPDATE_NEVER;
        retryCount = "3";
        retryInterval = "10000";
        fetchSize = "1000";
    }

    @Override
    public String getProtocol() {
        return "jdbc";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toFormattedString() {
        return null;
    }

    @Override
    public PollConnectorProperties getPollConnectorProperties() {
        return pollConnectorProperties;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public boolean isUseScript() {
        return useScript;
    }

    public void setUseScript(boolean useScript) {
        this.useScript = useScript;
    }

    public boolean isCacheResults() {
        return cacheResults;
    }

    public void setCacheResults(boolean cacheResults) {
        this.cacheResults = cacheResults;
    }

    public boolean isKeepConnectionOpen() {
        return keepConnectionOpen;
    }

    public void setKeepConnectionOpen(boolean keepConnectionOpen) {
        this.keepConnectionOpen = keepConnectionOpen;
    }

    public int getUpdateMode() {
        return updateMode;
    }

    public void setUpdateMode(int updateMode) {
        this.updateMode = updateMode;
    }

    public String getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(String retryCount) {
        this.retryCount = retryCount;
    }

    public String getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(String retryInterval) {
        this.retryInterval = retryInterval;
    }

    public String getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(String fetchSize) {
        this.fetchSize = fetchSize;
    }

    @Override
    public ResponseConnectorProperties getResponseConnectorProperties() {
        return responseConnectorProperties;
    }
}
