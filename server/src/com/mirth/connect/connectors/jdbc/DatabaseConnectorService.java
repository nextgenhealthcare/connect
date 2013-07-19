/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.ConnectorService;
import com.mirth.connect.server.util.TemplateValueReplacer;

public class DatabaseConnectorService implements ConnectorService {

    private final String[] TABLE_TYPES = { "TABLE", "VIEW" };
    private Logger logger = Logger.getLogger(this.getClass());
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public Object invoke(String channelId, String method, Object object, String sessionsId) throws Exception {
        if (method.equals("getInformationSchema")) {
            // method 'getInformationSchema' will return Set<Table>

            Connection connection = null;
            try {
                DatabaseConnectionInfo databaseConnectionInfo = (DatabaseConnectionInfo) object;
                String driver = databaseConnectionInfo.getDriver();
                String address = replacer.replaceValues(databaseConnectionInfo.getUrl(), channelId);
                String user = replacer.replaceValues(databaseConnectionInfo.getUsername(), channelId);
                String password = replacer.replaceValues(databaseConnectionInfo.getPassword(), channelId);

                // Although these properties are not persisted, they used by the JdbcConnectorService
                String tableNamePatternExp = databaseConnectionInfo.getTableNamePatternExpression();
                String selectLimit = databaseConnectionInfo.getSelectLimit();

                String schema = null;

                Class.forName(driver);
                int oldLoginTimeout = DriverManager.getLoginTimeout();
                DriverManager.setLoginTimeout(30);
                connection = DriverManager.getConnection(address, user, password);
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
                        if (user.equalsIgnoreCase(schemaResult)) {
                            schema = schemaResult;
                        }
                    }
                } finally {
                    if (schemasResult != null) {
                        schemasResult.close();
                    }
                }

                // based on the table name pattern, attempt to retrieve the table information
                List<String> tablePatternList = translateTableNamePatternExpression(tableNamePatternExp);
                List<String> tableNameList = new ArrayList<String>();

                // go through each possible table name patterns and query for the tables
                for (String tableNamePattern : tablePatternList) {
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
                                for (int i = 0; backupRs.next(); i++) {
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
                throw new Exception("Could not retrieve database tables and columns.", e);
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }

        return null;
    }

    /**
     * Translate the given pattern expression so that it can be used properly
     * for
     * searching tables in the database. Multiple table name patterns are
     * delimited by comma (,)
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
     * @return If table name pattern is an empty string, it'll never return
     *         NULL.
     */
    private List<String> translateTableNamePatternExpression(String tableNamePatternExpression) {
        if (tableNamePatternExpression == null) {
            throw new IllegalArgumentException("Parameter 'tableNamePatternExpression' cannot be NULL'");
        }

        List<String> tablePatternList = new ArrayList<String>();
        if (tableNamePatternExpression.isEmpty()) {
            tablePatternList.add("%");
        } else {
            final String[] tablePatterns = tableNamePatternExpression.trim().split("[, ]+");
            for (String tablePattern : tablePatterns) {
                tablePatternList.add(tablePattern.trim().replaceAll("\\*", "%"));
            }
        }
        return tablePatternList;
    }
}
