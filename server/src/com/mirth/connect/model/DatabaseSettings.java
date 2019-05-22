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
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.donkey.model.DatabaseConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("databaseSettings")
public class DatabaseSettings extends AbstractSettings implements Serializable, Auditable {
    private static final long serialVersionUID = 1L;

    public static final String DIR_BASE = "dir.base";

    public static final int DEFAULT_MAX_CONNECTIONS = 20;

    private static Map<String, String> databaseDriverMap = null;
    private static Map<String, Boolean> databaseJdbc4Map = null;
    private static Map<String, String> databaseTestQueryMap = null;

    static {
        databaseDriverMap = new HashMap<String, String>();
        databaseDriverMap.put("derby", "org.apache.derby.jdbc.EmbeddedDriver");
        databaseDriverMap.put("mysql", "com.mysql.cj.jdbc.Driver");
        databaseDriverMap.put("oracle", "oracle.jdbc.OracleDriver");
        databaseDriverMap.put("postgres", "org.postgresql.Driver");
        databaseDriverMap.put("sqlserver", "net.sourceforge.jtds.jdbc.Driver");

        databaseJdbc4Map = new HashMap<String, Boolean>();
        databaseJdbc4Map.put("derby", true);
        databaseJdbc4Map.put("mysql", true);
        databaseJdbc4Map.put("oracle", true);
        databaseJdbc4Map.put("postgres", true);
        // JTDS does not support JDBC 4.0 operations
        databaseJdbc4Map.put("sqlserver", false);

        databaseTestQueryMap = new HashMap<String, String>();
        databaseTestQueryMap.put("derby", "SELECT 1");
        databaseTestQueryMap.put("mysql", "SELECT 1");
        databaseTestQueryMap.put("oracle", "SELECT 1 FROM DUAL");
        databaseTestQueryMap.put("postgres", "SELECT 1");
        databaseTestQueryMap.put("sqlserver", "SELECT 1");
    }

    private boolean splitReadWrite;
    private boolean writePoolCache;

    private String database;
    private String databaseUrl;
    private String databaseDriver;
    private String databaseUsername;
    private String databasePassword;
    private Integer databaseMaxConnections;
    private String databasePool;

    private String databaseReadOnly;
    private String databaseReadOnlyUrl;
    private String databaseReadOnlyDriver;
    private String databaseReadOnlyUsername;
    private String databaseReadOnlyPassword;
    private Integer databaseReadOnlyMaxConnections;
    private String databaseReadOnlyPool;

    private String dirBase;

    public DatabaseSettings() {

    }

    public DatabaseSettings(Properties properties) {
        setProperties(properties);
    }

    public boolean isSplitReadWrite() {
        return splitReadWrite;
    }

    public void setSplitReadWrite(boolean splitReadWrite) {
        this.splitReadWrite = splitReadWrite;
    }

    public boolean isWritePoolCache() {
        return writePoolCache;
    }

    public void setWritePoolCache(boolean writePoolCache) {
        this.writePoolCache = writePoolCache;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public void setDatabaseDriver(String databaseDriver) {
        this.databaseDriver = databaseDriver;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public Integer getDatabaseMaxConnections() {
        return databaseMaxConnections;
    }

    public void setDatabaseMaxConnections(Integer databaseMaxConnections) {
        this.databaseMaxConnections = databaseMaxConnections;
    }

    public String getDatabasePool() {
        return databasePool;
    }

    public void setDatabasePool(String databasePool) {
        this.databasePool = databasePool;
    }

    public String getDatabaseReadOnly() {
        return databaseReadOnly;
    }

    public void setDatabaseReadOnly(String databaseReadOnly) {
        this.databaseReadOnly = databaseReadOnly;
    }

    public String getDatabaseReadOnlyUrl() {
        return databaseReadOnlyUrl;
    }

    public void setDatabaseReadOnlyUrl(String databaseReadOnlyUrl) {
        this.databaseReadOnlyUrl = databaseReadOnlyUrl;
    }

    public String getDatabaseReadOnlyDriver() {
        return databaseReadOnlyDriver;
    }

    public void setDatabaseReadOnlyDriver(String databaseReadOnlyDriver) {
        this.databaseReadOnlyDriver = databaseReadOnlyDriver;
    }

    public String getDatabaseReadOnlyUsername() {
        return databaseReadOnlyUsername;
    }

    public void setDatabaseReadOnlyUsername(String databaseReadOnlyUsername) {
        this.databaseReadOnlyUsername = databaseReadOnlyUsername;
    }

    public String getDatabaseReadOnlyPassword() {
        return databaseReadOnlyPassword;
    }

    public void setDatabaseReadOnlyPassword(String databaseReadOnlyPassword) {
        this.databaseReadOnlyPassword = databaseReadOnlyPassword;
    }

    public Integer getDatabaseReadOnlyMaxConnections() {
        return databaseReadOnlyMaxConnections;
    }

    public void setDatabaseReadOnlyMaxConnections(Integer databaseReadOnlyMaxConnections) {
        this.databaseReadOnlyMaxConnections = databaseReadOnlyMaxConnections;
    }

    public String getDatabaseReadOnlyPool() {
        return databaseReadOnlyPool;
    }

    public void setDatabaseReadOnlyPool(String databaseReadOnlyPool) {
        this.databaseReadOnlyPool = databaseReadOnlyPool;
    }

    public String getDirBase() {
        return dirBase;
    }

    public void setDirBase(String dirBase) {
        this.dirBase = dirBase;
    }

    String getMappedDatabaseDriver() {
        if (StringUtils.isBlank(databaseDriver)) {
            return MapUtils.getString(databaseDriverMap, getDatabase());
        } else {
            return databaseDriver;
        }
    }

    String getMappedReadOnlyDatabaseDriver() {
        if (StringUtils.isBlank(databaseReadOnlyDriver)) {
            if (StringUtils.isBlank(getDatabaseReadOnly())) {
                return getMappedDatabaseDriver();
            } else {
                return MapUtils.getString(databaseDriverMap, getDatabaseReadOnly());
            }
        } else {
            return databaseReadOnlyDriver;
        }
    }

    private Boolean getMappedJdbc4() {
        return MapUtils.getBoolean(databaseJdbc4Map, getDatabase());
    }

    private Boolean getMappedReadOnlyJdbc4() {
        return MapUtils.getBoolean(databaseJdbc4Map, StringUtils.defaultIfBlank(getDatabaseReadOnly(), getDatabase()));
    }

    private String getMappedTestQuery() {
        return MapUtils.getString(databaseTestQueryMap, getDatabase());
    }

    private String getMappedReadOnlyTestQuery() {
        return MapUtils.getString(databaseTestQueryMap, StringUtils.defaultIfBlank(getDatabaseReadOnly(), getDatabase()));
    }

    @Override
    public void setProperties(Properties properties) {
        setSplitReadWrite(Boolean.parseBoolean(properties.getProperty(DatabaseConstants.DATABASE_ENABLE_READ_WRITE_SPLIT)));
        setWritePoolCache(Boolean.parseBoolean(properties.getProperty(DatabaseConstants.DATABASE_WRITE_POOL_CACHE)));

        setDatabase(properties.getProperty(DatabaseConstants.DATABASE));
        setDatabaseUrl(properties.getProperty(DatabaseConstants.DATABASE_URL));
        setDatabaseDriver(properties.getProperty(DatabaseConstants.DATABASE_DRIVER));
        setDatabaseUsername(properties.getProperty(DatabaseConstants.DATABASE_USERNAME));
        setDatabasePassword(properties.getProperty(DatabaseConstants.DATABASE_PASSWORD));
        setDatabaseMaxConnections(NumberUtils.toInt(properties.getProperty(DatabaseConstants.DATABASE_MAX_CONNECTIONS), DEFAULT_MAX_CONNECTIONS));
        setDatabasePool(properties.getProperty(DatabaseConstants.DATABASE_POOL));

        setDatabaseReadOnly(properties.getProperty(DatabaseConstants.DATABASE_READONLY));
        setDatabaseReadOnlyUrl(properties.getProperty(DatabaseConstants.DATABASE_READONLY_URL));
        setDatabaseReadOnlyDriver(properties.getProperty(DatabaseConstants.DATABASE_READONLY_DRIVER));
        setDatabaseReadOnlyUsername(properties.getProperty(DatabaseConstants.DATABASE_READONLY_USERNAME));
        setDatabaseReadOnlyPassword(properties.getProperty(DatabaseConstants.DATABASE_READONLY_PASSWORD));
        String readOnlyMaxConnectionsString = properties.getProperty(DatabaseConstants.DATABASE_READONLY_MAX_CONNECTIONS);
        if (StringUtils.isNotBlank(readOnlyMaxConnectionsString)) {
            setDatabaseReadOnlyMaxConnections(NumberUtils.toInt(readOnlyMaxConnectionsString, getDatabaseMaxConnections()));
        }
        setDatabaseReadOnlyPool(properties.getProperty(DatabaseConstants.DATABASE_READONLY_POOL));

        setDirBase(properties.getProperty(DIR_BASE));
    }

    @Override
    public Properties getProperties() {
        PropertiesConfiguration configuration = new PropertiesConfiguration();

        configuration.setProperty(DatabaseConstants.DATABASE_ENABLE_READ_WRITE_SPLIT, Boolean.toString(splitReadWrite));

        configuration.setProperty(DatabaseConstants.DATABASE_WRITE_POOL_CACHE, Boolean.toString(writePoolCache));

        if (getDirBase() != null) {
            configuration.setProperty(DIR_BASE, getDirBase());
        }

        if (getDatabase() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE, getDatabase());
        }

        if (getDatabaseUrl() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_URL, getDatabaseUrl());
        }

        if (getMappedDatabaseDriver() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_DRIVER, getMappedDatabaseDriver());
        }

        if (getDatabasePool() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_POOL, getDatabasePool());
        }

        if (getMappedJdbc4() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_JDBC4, getMappedJdbc4());
        }

        if (getMappedTestQuery() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_TEST_QUERY, getMappedTestQuery());
        }

        /*
         * MIRTH-1749: in case someone comments out the username and password properties
         */
        if (getDatabaseUsername() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_USERNAME, getDatabaseUsername());
        } else {
            configuration.setProperty(DatabaseConstants.DATABASE_USERNAME, StringUtils.EMPTY);
        }

        if (getDatabasePassword() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_PASSWORD, getDatabasePassword());
        } else {
            configuration.setProperty(DatabaseConstants.DATABASE_PASSWORD, StringUtils.EMPTY);
        }

        if (getDatabaseMaxConnections() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_MAX_CONNECTIONS, getDatabaseMaxConnections().toString());
        } else {
            configuration.setProperty(DatabaseConstants.DATABASE_MAX_CONNECTIONS, StringUtils.EMPTY);
        }

        /**** READ ONLY PROPERTIES ****/

        if (getDatabaseReadOnly() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY, getDatabaseReadOnly());
        }

        if (getDatabaseReadOnlyUrl() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY_URL, getDatabaseReadOnlyUrl());
        }

        if (getMappedDatabaseDriver() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY_DRIVER, getMappedReadOnlyDatabaseDriver());
        }

        if (getDatabaseReadOnlyPool() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY_POOL, getDatabaseReadOnlyPool());
        }

        if (getMappedJdbc4() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY_JDBC4, getMappedReadOnlyJdbc4());
        }

        if (getMappedTestQuery() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY_TEST_QUERY, getMappedReadOnlyTestQuery());
        }

        if (getDatabaseReadOnlyUsername() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY_USERNAME, getDatabaseReadOnlyUsername());
        }

        if (getDatabaseReadOnlyPassword() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY_PASSWORD, getDatabaseReadOnlyPassword());
        }

        if (getDatabaseReadOnlyMaxConnections() != null) {
            configuration.setProperty(DatabaseConstants.DATABASE_READONLY_MAX_CONNECTIONS, getDatabaseReadOnlyMaxConnections().toString());
        }

        return ConfigurationConverter.getProperties(configuration);
    }

    @Override
    public String toAuditString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

}
