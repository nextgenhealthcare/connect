/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.manager;

public class ManagerConstants {

    // Server ports
    public static final String SERVER_WEBSTART_PORT = "http.port";
    public static final String SERVER_ADMINISTRATOR_PORT = "https.port";
    public static final String SERVER_JMX_PORT = "jmx.port";

    // Configurable directories
    public static final String DIR_APPDATA = "dir.appdata";
    public static final String DIR_LOGS = "dir.logs";

    // Log4j
    public static final String LOG4J_MIRTH_LOG_LEVEL = "log4j.rootCategory";
    public static final String LOG4J_DATABASE_LOG_LEVEL = "log4j.logger.java.sql";
    public static final String[] LOG4J_ERROR_CODES = new String[]{"ERROR", "WARN", "DEBUG", "INFO"};

    // Database
    public static final String DATABASE_TYPE = "database";
    public static final String DATABASE_DRIVER = "driver";
    public static final String DATABASE_URL = "url";
    public static final String DATABASE_USERNAME = "username";
    public static final String DATABASE_PASSWORD = "password";
    public static final String DATABASE_DERBY = "derby";
    public static final String DATABASE_POSTGRES = "postgres";
    public static final String DATABASE_MYSQL = "mysql";
    public static final String DATABASE_SQLSERVER = "sqlserver";
    public static final String DATABASE_SQLSERVER2005 = "sqlserver2005";
    public static final String DATABASE_ORACLE = "oracle";

    // File paths
    public static final String PATH_SERVER_PROPERTIES = "conf/mirth.properties";
    public static final String PATH_LOG4J_PROPERTIES = "conf/log4j.properties";
    public static final String PATH_DERBY_PROPERTIES = "conf/derby/derby-SqlMapConfig.properties";
    public static final String PATH_POSTGRES_PROPERTIES = "conf/postgres/postgres-SqlMapConfig.properties";
    public static final String PATH_MYSQL_PROPERTIES = "conf/mysql/mysql-SqlMapConfig.properties";
    public static final String PATH_SQLSERVER_PROPERTIES = "conf/sqlserver/sqlserver-SqlMapConfig.properties";
    public static final String PATH_SQLSERVER2005_PROPERTIES = "conf/sqlserver2005/sqlserver2005-SqlMapConfig.properties";
    public static final String PATH_ORACLE_PROPERTIES = "conf/oracle/oracle-SqlMapConfig.properties";
    public static final String PATH_VERSION_FILE = "conf/version.properties";
    public static final String PATH_SERVER_ID_FILE = "server.id";
    public static final String PATH_SERVICE_VMOPTIONS = "mirthconnect_service.vmoptions";
    
    // Webstart commands
    public static final String CMD_WEBSTART_PREFIX = "javaws http://localhost:";
    public static final String CMD_WEBSTART_SUFFIX = "/webstart.jnlp";
    public static final String CMD_TEST_JETTY_PREFIX = "https://localhost:";
}
