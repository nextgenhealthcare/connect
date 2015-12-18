/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DatabaseConnectorServlet extends MirthServlet implements DatabaseConnectorServletInterface {

    private static final String[] TABLE_TYPES = { "TABLE", "VIEW" };
    private static final Logger logger = Logger.getLogger(DatabaseConnectorServlet.class);
    private static final TemplateValueReplacer replacer = new TemplateValueReplacer();
    private static final ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();

    public DatabaseConnectorServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PLUGIN_POINT);
    }

    @Override
    public SortedSet<Table> getTables(String channelId, String channelName, String driver, String url, String username, String password, Set<String> tableNamePatterns, String selectLimit, Set<String> resourceIds) {
        CustomDriver customDriver = null;
        Connection connection = null;
        try {
            url = replacer.replaceValues(url, channelId, channelName);
            username = replacer.replaceValues(username, channelId, channelName);
            password = replacer.replaceValues(password, channelId, channelName);

            String schema = null;

            try {
                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(resourceIds);

                try {
                    ClassLoader isolatedClassLoader = contextFactory.getIsolatedClassLoader();
                    if (isolatedClassLoader != null) {
                        customDriver = new CustomDriver(isolatedClassLoader, driver);
                        logger.debug("Custom driver created: " + customDriver.toString() + ", Version " + customDriver.getMajorVersion() + "." + customDriver.getMinorVersion());
                    } else {
                        logger.debug("Custom classloader is not being used, defaulting to DriverManager.");
                    }
                } catch (Exception e) {
                    logger.debug("Error creating custom driver, defaulting to DriverManager.", e);
                }
            } catch (Exception e) {
                logger.debug("Error retrieving context factory, defaulting to DriverManager.", e);
            }

            if (customDriver == null) {
                Class.forName(driver);
            }

            int oldLoginTimeout = DriverManager.getLoginTimeout();
            DriverManager.setLoginTimeout(30);

            if (customDriver != null) {
                connection = customDriver.connect(url, username, password);
            } else {
                connection = DriverManager.getConnection(url, username, password);
            }

            DriverManager.setLoginTimeout(oldLoginTimeout);
            DatabaseMetaData dbMetaData = connection.getMetaData();

            // the sorted set to hold the table information
            SortedSet<Table> tableInfoList = new TreeSet<Table>();

            // Use a schema if the user name matches one of the schemas.
            // Fix for Oracle: MIRTH-1045
            ResultSet schemasResult = null;
            try {
                schemasResult = dbMetaData.getSchemas();
                while (schemasResult.next()) {
                    String schemaResult = schemasResult.getString(1);
                    if (username.equalsIgnoreCase(schemaResult)) {
                        schema = schemaResult;
                    }
                }
            } finally {
                if (schemasResult != null) {
                    schemasResult.close();
                }
            }

            // based on the table name pattern, attempt to retrieve the table information
            tableNamePatterns = translateTableNamePatterns(tableNamePatterns);
            List<String> tableNameList = new ArrayList<String>();

            // go through each possible table name patterns and query for the tables
            for (String tableNamePattern : tableNamePatterns) {
                ResultSet rs = null;
                try {
                    rs = dbMetaData.getTables(null, schema, tableNamePattern, TABLE_TYPES);

                    // based on the result set, loop through to store the table name so it can be used to
                    // retrieve the table's column information
                    while (rs.next()) {
                        tableNameList.add(rs.getString("TABLE_NAME"));
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            }

            // for each table, grab their column information
            for (String tableName : tableNameList) {
                ResultSet rs = null;
                ResultSet backupRs = null;
                boolean fallback = false;
                try {
                    // apparently it's much more efficient to use ResultSetMetaData to retrieve
                    // column information.  So each driver is defined with their own unique SELECT
                    // statement to query the table columns and use ResultSetMetaData to retrieve
                    // the column information.  If driver is not defined with the select statement
                    // then we'll define to the generic method of getting column information, but
                    // this could be extremely slow
                    List<Column> columnList = new ArrayList<Column>();
                    if (StringUtils.isEmpty(selectLimit)) {
                        logger.debug("No select limit is defined, using generic method");
                        rs = dbMetaData.getColumns(null, null, tableName, null);

                        // retrieve all relevant column information                         
                        for (int i = 0; rs.next(); i++) {
                            Column column = new Column(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getInt("COLUMN_SIZE"));
                            columnList.add(column);
                        }
                    } else {
                        logger.debug("Select limit is defined, using specific select query : '" + selectLimit + "'");

                        // replace the '?' with the appropriate schema.table name, and use ResultSetMetaData to 
                        // retrieve column information 
                        final String schemaTableName = StringUtils.isNotEmpty(schema) ? "\"" + schema + "\".\"" + tableName + "\"" : "\"" + tableName + "\"";
                        final String queryString = selectLimit.trim().replaceAll("\\?", Matcher.quoteReplacement(schemaTableName));
                        Statement statement = connection.createStatement();
                        try {
                            rs = statement.executeQuery(queryString);
                            ResultSetMetaData rsmd = rs.getMetaData();

                            // retrieve all relevant column information
                            for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
                                Column column = new Column(rsmd.getColumnName(i), rsmd.getColumnTypeName(i), rsmd.getPrecision(i));
                                columnList.add(column);
                            }
                        } catch (SQLException sqle) {
                            logger.info("Failed to execute '" + queryString + "', fall back to generic approach to retrieve column information");
                            fallback = true;
                        } finally {
                            if (statement != null) {
                                statement.close();
                            }
                        }

                        // failed to use selectLimit method, so we need to fall back to generic
                        // if this generic approach fails, then there's nothing we can do
                        if (fallback) {
                            // Re-initialize in case some columns were added before failing
                            columnList = new ArrayList<Column>();

                            logger.debug("Using fallback method for retrieving columns");
                            backupRs = dbMetaData.getColumns(null, null, tableName.replace("/", "//"), null);

                            // retrieve all relevant column information                         
                            while (backupRs.next()) {
                                Column column = new Column(backupRs.getString("COLUMN_NAME"), backupRs.getString("TYPE_NAME"), backupRs.getInt("COLUMN_SIZE"));
                                columnList.add(column);
                            }
                        }
                    }

                    // create table object and add to the list of table definitions
                    Table table = new Table(tableName, columnList);
                    tableInfoList.add(table);
                } finally {
                    if (rs != null) {
                        rs.close();
                    }

                    if (backupRs != null) {
                        backupRs.close();
                    }
                }
            }

            return tableInfoList;
        } catch (Exception e) {
            throw new MirthApiException(new Exception("Could not retrieve database tables and columns.", e));
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * Translate the given pattern expression so that it can be used properly for searching tables
     * in the database. Multiple table name patterns are delimited by comma (,)
     * <p>
     * This interpret and translate to the following:
     * <p>
     * <ul>
     * <li>"*" = wild card for more than one character, will be converted to be used as '%'</li>
     * <li>"_" = one character wild card</li>
     * <li>"" = empty string will retrieve all tables
     * </ul>
     * <p>
     * <i>Eg. rad*,table*test => Find all tables starts with 'rad' AND tables prefix with 'table'
     * and postfix with 'test'</i>
     * 
     * @param tableNamePatternExpression
     *            pattern expression to translate, cannot be NULL.
     * @return If table name pattern is an empty string, it'll never return NULL.
     */
    private Set<String> translateTableNamePatterns(Set<String> tableNamePatterns) {
        if (tableNamePatterns == null) {
            throw new IllegalArgumentException("Parameter 'tableNamePatterns' cannot be NULL'");
        }

        Set<String> patterns = new HashSet<String>();
        if (tableNamePatterns.isEmpty()) {
            patterns.add("%");
        } else {
            for (String pattern : tableNamePatterns) {
                patterns.add(pattern.trim().replaceAll("\\*", "%"));
            }
        }
        return patterns;
    }
}