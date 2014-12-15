/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.Set;

public class DatabaseConnectionInfo {
    private String driver;
    private String url;
    private String username;
    private String password;
    private String tableNamePatternExpression;
    private String selectLimit;
    private Set<String> resourceIds;

    public DatabaseConnectionInfo(String driver, String url, String username, String password, String tableNamePatternExpression, String selectLimit, Set<String> resourceIds) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.tableNamePatternExpression = tableNamePatternExpression;
        this.selectLimit = selectLimit;
        this.resourceIds = resourceIds;
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

    public String getTableNamePatternExpression() {
        return tableNamePatternExpression;
    }

    public void setTableNamePatternExpression(String tableNamePatternExpression) {
        this.tableNamePatternExpression = tableNamePatternExpression;
    }

    public String getSelectLimit() {
        return selectLimit;
    }

    public void setSelectLimit(String selectLimit) {
        this.selectLimit = selectLimit;
    }

    public Set<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(Set<String> resourceIds) {
        this.resourceIds = resourceIds;
    }
}
