package com.webreach.mirth.manager;

public class ManagerConstants {

    // Server ports
    public static final String SERVER_WEBSTART_PORT = "http.port";
    public static final String SERVER_ADMINISTRATOR_PORT = "https.port";
    public static final String SERVER_JMX_PORT = "jmx.port";

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
    public static final String PATH_SERVER_PROPERTIES = "conf\\mirth.properties";
    public static final String PATH_LOG4J_PROPERTIES = "conf\\log4j.properties";
    public static final String PATH_SERVER_LOGS = "logs\\";
    public static final String PATH_DERBY_PROPERTIES = "conf\\derby-SqlMapConfig.properties";
    public static final String PATH_POSTGRES_PROPERTIES = "conf\\postgres-SqlMapConfig.properties";
    public static final String PATH_MYSQL_PROPERTIES = "conf\\mysql-SqlMapConfig.properties";
    public static final String PATH_SQLSERVER_PROPERTIES = "conf\\sqlserver-SqlMapConfig.properties";
    public static final String PATH_SQLSERVER2005_PROPERTIES = "conf\\sqlserver2005-SqlMapConfig.properties";
    public static final String PATH_ORACLE_PROPERTIES = "conf\\oracle-SqlMapConfig.properties";
    public static final String PATH_VERSION_FILE = "conf\\version.properties";
    public static final String PATH_SERVER_ID_FILE = "server.id";

    // Windows service
    public static final String SERVICE_NAME = "Mirth";
    public static final String CMD_START = "cmd /c net start \"";
    public static final String CMD_STOP = "cmd /c net stop \"";
    public static final String CMD_WEBSTART_PREFIX = "cmd /c javaws http://localhost:";
    public static final String CMD_WEBSTART_SUFFIX = "/webstart.jnlp";
    public static final String CMD_TEST_JETTY_PREFIX = "https://localhost:";
    public static final String CMD_STATUS = "cmd /c net continue \"";
    public static final int STATUS_RUNNING = 2191;
    public static final int STATUS_STOPPED = 2184;
    public static final String STATUS_CHANGING = "2189";
    public static final String CMD_QUERY_REGEX = "NET HELPMSG ([0-9]{4})";

    // Registry commands
    public static final String CMD_REG_QUERY = "cmd /c REG QUERY HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /v Mirth";
    public static final String CMD_REG_DELETE = "cmd /c REG DELETE HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /f /v Mirth";
    public static final String CMD_REG_ADD = "cmd /c REG ADD HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /f /v Mirth /d ";
}
