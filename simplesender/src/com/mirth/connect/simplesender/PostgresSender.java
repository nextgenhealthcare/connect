package com.mirth.connect.simplesender;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresSender {
    private Connection connection = null;
    private int rowCount = 0;
    private ResultSet result;
    private final String JDBC_DRIVER_NAME = "org.postgresql.Driver";

    public PostgresSender(String IP, String port, String schema, String user, String password) {
        loadDriver();
        connectToDatabase(IP, port, schema, user, password);
    }

    public boolean receive(String query) {
        int newRowCount = 0;
        result = runQuery(query);

        try {
            result.next();
            newRowCount = Integer.parseInt(result.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (newRowCount == rowCount + 1) {
            rowCount = newRowCount;
            closeConnection();
            return true;
        }

        closeConnection();
        return false;

    }

    private final void loadDriver() {
        try {
            Class.forName(JDBC_DRIVER_NAME).newInstance();
        } catch (ClassNotFoundException cnf_excp) {
            System.out.println("Failed to load JDBC driver " + JDBC_DRIVER_NAME + ".");
        } catch (InstantiationException i_excp) {
            System.out.println("Failed to load JDBC driver " + JDBC_DRIVER_NAME + ".");
        } catch (IllegalAccessException ia_excp) {
            System.out.println("Failed to load JDBC driver " + JDBC_DRIVER_NAME + ".");
        }
    }

    private void connectToDatabase(String IP, String port, String schema, String user, String password) {
        String url = "jdbc:postgresql://" + IP + ":" + port + "/" + schema + "?user=" + user + "&password=" + password;

        try {
            this.connection = DriverManager.getConnection(url);
        } catch (SQLException sql_excp) {
            System.out.println("Failed to get database connection with url, " + url);
            sql_excp.printStackTrace();
        }
    }

    public ResultSet runQuery(String query) {
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = this.connection.createStatement();
            resultSet = statement.executeQuery(query);

            return resultSet;

        } catch (SQLException sql_excp) {
            System.out.println("Encountered error while trying to execute query.");
            sql_excp.printStackTrace();
        }

        /* Close the result set and dispose of the statement. */
        try {
            resultSet.close();
            statement.close();
        } catch (SQLException sql_excp) {
            System.out.println("Encountered error while trying to close result set or statement.");
            sql_excp.printStackTrace();
        }
        return resultSet;
    }

    public boolean runUpdate(String query) {
        Statement statement = null;

        try {
            statement = this.connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
            return true;

        } catch (SQLException sql_excp) {
            System.out.println("Encountered error while trying to execute query.");
            sql_excp.printStackTrace();
        }

        /* Close the result set and dispose of the statement. */
        try {
            if (statement != null)
                statement.close();
        } catch (SQLException sql_excp) {
            System.out.println("Encountered error while trying to close result set or statement.");
            sql_excp.printStackTrace();
        }
        return false;
    }

    private void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException sql_excp) {
            System.out.println("Failed to close database connection.");
        }
    }
}
