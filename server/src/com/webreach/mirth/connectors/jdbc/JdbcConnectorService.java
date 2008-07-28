package com.webreach.mirth.connectors.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webreach.mirth.connectors.ConnectorService;

public class JdbcConnectorService implements ConnectorService {

    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("getInformationSchema")) {
            Connection connection = null;
            Statement tableStatement = null;
            ResultSet tableResult = null;

            try {
                Map<String, String> properties = (Map<String, String>) object;
                Map<String, List<String>> schemaInfo = new HashMap<String, List<String>>();

                String driver = properties.get("driver");
                String address = properties.get("address");
                String user = properties.get("user");
                String password = properties.get("password");
                String schema = properties.get("schema");

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
                tableResult.close();
                tableStatement.close();
                connection.close();
            }
        }

        return null;
    }

}
