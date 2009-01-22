package com.webreach.mirth.connectors.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.webreach.mirth.connectors.ConnectorService;

public class JdbcConnectorService implements ConnectorService {

    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("getInformationSchema")) {
            Connection connection = null;
            ResultSet tableResult = null;

            try {
                Properties properties = (Properties) object;
                Map<String, List<String>> schemaInfo = new HashMap<String, List<String>>();

                String driver = properties.getProperty(DatabaseReaderProperties.DATABASE_DRIVER);
                String address = properties.getProperty(DatabaseReaderProperties.DATABASE_URL);
                String user = properties.getProperty(DatabaseReaderProperties.DATABASE_USERNAME);
                String password = properties.getProperty(DatabaseReaderProperties.DATABASE_PASSWORD);

                Class.forName(driver);
                connection = DriverManager.getConnection(address, user, password);
                DatabaseMetaData dbMetaData = connection.getMetaData();
                tableResult = dbMetaData.getTables(null, null, null, new String[] { "TABLE" });

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
