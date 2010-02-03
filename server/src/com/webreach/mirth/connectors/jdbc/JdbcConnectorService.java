/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.webreach.mirth.connectors.ConnectorService;

public class JdbcConnectorService implements ConnectorService {

    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("getInformationSchema")) {
            Connection connection = null;
            ResultSet tableResult = null;

            try {
                Properties properties = (Properties) object;
                Map<String, List<String>> schemaInfo = new TreeMap<String, List<String>>();

                String driver = properties.getProperty(DatabaseReaderProperties.DATABASE_DRIVER);
                String address = properties.getProperty(DatabaseReaderProperties.DATABASE_URL);
                String user = properties.getProperty(DatabaseReaderProperties.DATABASE_USERNAME);
                String password = properties.getProperty(DatabaseReaderProperties.DATABASE_PASSWORD);
                String schema = null;

                Class.forName(driver);
                connection = DriverManager.getConnection(address, user, password);
                DatabaseMetaData dbMetaData = connection.getMetaData();

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

                tableResult = dbMetaData.getTables(null, schema, null, new String[] { "TABLE", "VIEW" });

                while (tableResult.next()) {
                    String tableName = tableResult.getString("TABLE_NAME");
                    ResultSet columnResult = null;

                    try {
                        columnResult = dbMetaData.getColumns(null, null, tableName, null);
                        List<String> columnNames = new ArrayList<String>();

                        while (columnResult.next()) {
                            columnNames.add(columnResult.getString("COLUMN_NAME"));
                        }

                        schemaInfo.put(tableName, columnNames);
                    } finally {
                        if (columnResult != null) {
                            columnResult.close();
                        }
                    }
                }

                return schemaInfo;
            } catch (Exception e) {
                throw new Exception("Could not retrieve database tables and columns.", e);
            } finally {
                if (tableResult != null) {
                    tableResult.close();
                }

                if (connection != null) {
                    connection.close();
                }
            }
        }

        return null;
    }
}
