package com.webreach.mirth.connectors.jdbc;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
            Statement tableStatement = null;
            ResultSet tableResult = null;

            try {
                Properties properties = (Properties) object;
                Map<String, List<String>> schemaInfo = new HashMap<String, List<String>>();

                String driver = properties.getProperty(DatabaseReaderProperties.DATABASE_DRIVER);
                String address = properties.getProperty(DatabaseReaderProperties.DATABASE_URL);
                String user = properties.getProperty(DatabaseReaderProperties.DATABASE_USERNAME);
                String password = properties.getProperty(DatabaseReaderProperties.DATABASE_PASSWORD);
                String schema = new URI(address).getPath();

                Class.forName(driver);
                connection = DriverManager.getConnection(address, user, password);
                tableStatement = connection.createStatement();
                tableResult = tableStatement.executeQuery("SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' AND TABLE_SCHEMA='" + schema + "'");

                while (tableResult.next()) {
                    String tableName = tableResult.getString("table_name");
                    schemaInfo.put(tableName, new ArrayList<String>());

                    Statement columnStatement = null;
                    ResultSet columnResult = null;

                    try {
                        columnStatement = connection.createStatement();
                        columnResult = columnStatement.executeQuery("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='" + tableName + "'");

                        while (columnResult.next()) {
                            String columnName = columnResult.getString("column_name");
                            schemaInfo.get(tableName).add(columnName);
                        }
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        columnStatement.close();
                        columnResult.close();
                    }
                }

                return schemaInfo;
            } catch (Exception e) {
                throw new Exception("Could not retrieve database tables and columns.", e);
            } finally {
                if (tableResult != null) {
                    tableResult.close();    
                }
                
                if (tableStatement != null) {
                    tableStatement.close();    
                }
                
                if (connection != null) {
                    connection.close();    
                }
            }
        }

        return null;
    }

}
