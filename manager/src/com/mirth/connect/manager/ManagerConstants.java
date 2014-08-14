/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.manager;

public class ManagerConstants {

    // Server ports
    public static final String SERVER_WEBSTART_PORT = "http.port";
    public static final String SERVER_ADMINISTRATOR_PORT = "https.port";

    // Configurable directories
    public static final String DIR_APPDATA = "dir.appdata";
    public static final String DIR_LOGS = "dir.logs";

    // Log4j
    public static final String LOG4J_MIRTH_LOG_LEVEL = "log4j.rootLogger";
    public static final String LOG4J_DATABASE_LOG_LEVEL = "log4j.logger.java.sql";
    public static final String[] LOG4J_CHANNEL_LOG_LEVELS = new String[]{"log4j.logger.transformer", "log4j.logger.preprocessor", "log4j.logger.postprocessor", "log4j.logger.deploy", "log4j.logger.undeploy", "log4j.logger.filter", "log4j.logger.db-connector", "log4j.logger.js-connector", "log4j.logger.attachment", "log4j.logger.batch", "log4j.logger.response"};
    public static final String[] LOG4J_ERROR_CODES = new String[]{"ERROR", "WARN", "INFO", "DEBUG", "TRACE"};
    public static final String[] LOG4J_ERROR_CODES_WITH_BLANK = new String[]{"", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"};

    // Database
    public static final String DATABASE_TYPE = "database";
    public static final String DATABASE_DRIVER = "database.driver";
    public static final String DATABASE_URL = "database.url";
    public static final String DATABASE_USERNAME = "database.username";
    public static final String DATABASE_PASSWORD = "database.password";
    
    // Other Properties
    public static final String PROPERTY_HTTP_CONTEXT_PATH = "http.contextpath";
    public static final String PROPERTY_SERVER_ID = "server.id";
    public static final String PROPERTY_SERVER_VERSION = "mirth.version";

    // File paths
    public static final String PATH_SERVER_PROPERTIES = "conf" + System.getProperty("file.separator") + "mirth.properties";
    public static final String PATH_LOG4J_PROPERTIES = "conf" + System.getProperty("file.separator") + "log4j.properties";
    public static final String PATH_VERSION_FILE = "version.properties";
    public static final String PATH_SERVER_ID_FILE = "server.id";
    public static final String PATH_SERVICE_VMOPTIONS = "mcservice.vmoptions";
    
    // Webstart commands
    public static final String CMD_WEBSTART_PREFIX = "javaws http://localhost:";
    public static final String CMD_WEBSTART_SUFFIX = "/webstart.jnlp";
    public static final String CMD_TEST_JETTY_PREFIX = "https://localhost:";
}
