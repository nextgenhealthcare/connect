/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class DatabaseDispatcherProperties extends ConnectorProperties implements DestinationConnectorPropertiesInterface {
    public static final String NAME = "Database Writer";

    private DestinationConnectorProperties destinationConnectorProperties;

    private String driver;
    private String url;
    private String username;
    private String password;
    private String query;
    private Object[] parameters;
    private boolean useScript;

    public static final String DRIVER_DEFAULT = "Please Select One";

    public DatabaseDispatcherProperties() {
        destinationConnectorProperties = new DestinationConnectorProperties(false);

        this.driver = DRIVER_DEFAULT;
        this.url = "";
        this.username = "";
        this.password = "";
        this.query = "";
        this.useScript = false;
    }

    public DatabaseDispatcherProperties(DatabaseDispatcherProperties props) {
        super(props);
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());

        this.driver = props.getDriver();
        this.url = props.getUrl();
        this.username = props.getUsername();
        this.password = props.getPassword();
        this.query = props.getQuery();
        this.useScript = props.isUseScript();
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
        builder.append(StringUtils.trim(query));

        for (int i = 0; i < parameters.length; i++) {
            builder.append(newLine);
            builder.append(newLine);
            builder.append("[PARAMETER ");
            builder.append(String.valueOf(i + 1));
            builder.append("]");
            builder.append(newLine);
            builder.append(parameters[i]);
        }

        return builder.toString();
    }

    @Override
    public DestinationConnectorProperties getDestinationConnectorProperties() {
        return destinationConnectorProperties;
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

    @Override
    public ConnectorProperties clone() {
        return new DatabaseDispatcherProperties(this);
    }

    @Override
    public boolean canValidateResponse() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        super.migrate3_1_0(element);
    }

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}

    @Override
    public void migrate3_6_0(DonkeyElement element) {}
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("destinationConnectorProperties", destinationConnectorProperties.getPurgedProperties());
        purgedProperties.put("driver", driver);
        purgedProperties.put("queryLines", PurgeUtil.countLines(query));
        purgedProperties.put("useScript", useScript);
        return purgedProperties;
    }
}