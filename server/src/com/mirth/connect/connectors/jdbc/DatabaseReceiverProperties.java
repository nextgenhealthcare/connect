/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorProperties;
import com.mirth.connect.donkey.model.channel.PollConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;

public class DatabaseReceiverProperties extends ConnectorProperties implements PollConnectorPropertiesInterface, ResponseConnectorPropertiesInterface {
    public static final String NAME = "Database Reader";

    private PollConnectorProperties pollConnectorProperties;
    private ResponseConnectorProperties responseConnectorProperties;

    private String driver;
    private String url;
    private String username;
    private String password;
    private String query;
    private boolean useScript;
    private boolean cacheResults;  // TODO: Not yet implemented on the server side
    private boolean useAck;
    private String ack;

    public static final String DRIVER_DEFAULT = "Please Select One";

    public DatabaseReceiverProperties() {
        pollConnectorProperties = new PollConnectorProperties();
        responseConnectorProperties = new ResponseConnectorProperties("None", new String[] { "None" });

        this.driver = DRIVER_DEFAULT;
        this.url = "";
        this.username = "";
        this.password = "";
        this.query = "";
        this.useScript = false;
        this.cacheResults = true;
        this.useAck = false;
        this.ack = "";
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
        // TODO Auto-generated method stub
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public boolean isUseAck() {
        return useAck;
    }

    public void setUseAck(boolean useAck) {
        this.useAck = useAck;
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    @Override
    public ResponseConnectorProperties getResponseConnectorProperties() {
        return responseConnectorProperties;
    }
}
