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
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;

public class DatabaseDispatcherProperties extends ConnectorProperties implements QueueConnectorPropertiesInterface {
    public static final String NAME = "Database Writer";

    private QueueConnectorProperties queueConnectorProperties;

    private String driver;
    private String url;
    private String username;
    private String password;
    private String query;
    private Object[] parameters;
    private boolean useScript;

    public static final String DRIVER_DEFAULT = "Please Select One";

    public DatabaseDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        this.driver = DRIVER_DEFAULT;
        this.url = "";
        this.username = "";
        this.password = "";
        this.query = "";
        this.useScript = false;
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
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";
        builder.append("URL: ");
        builder.append(url);
        builder.append(newLine);

        builder.append("USERNAME: ");
        builder.append(username);
        builder.append(newLine);

        builder.append(newLine);
        builder.append(useScript ? "[SCRIPT]" : "[QUERY]");
        builder.append(newLine);
        builder.append(query);

        if (parameters.length > 0) {
            builder.append(newLine);
            builder.append("[PARAMETERS]");
            builder.append(newLine);
            builder.append(parameters);
        }

        return builder.toString();
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
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

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public boolean isUseScript() {
        return useScript;
    }

    public void setUseScript(boolean useScript) {
        this.useScript = useScript;
    }
}
