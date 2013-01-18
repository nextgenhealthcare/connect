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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.UndeployException;

public class DatabaseDispatcherQuery implements DatabaseDispatcherDelegate {
    private BasicDataSource dataSource;

    @Override
    public void deploy() {}

    @Override
    public void undeploy() throws UndeployException {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                throw new UndeployException("Failed to close data source", e);
            }
        }
    }

    @Override
    public Response send(DatabaseDispatcherProperties connectorProperties, ConnectorMessage connectorMessage) throws DatabaseDispatcherException {
        // send the message and retry (once) if the database connection fails
        return send(connectorProperties, true);
    }

    private Response send(DatabaseDispatcherProperties connectorProperties, boolean retryOnConnectionFailure) throws DatabaseDispatcherException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection(connectorProperties);
            statement = connection.prepareStatement(connectorProperties.getQuery());
            int i = 1;

            for (Object param : connectorProperties.getParameters()) {
                statement.setObject(i++, param);
            }

            /*
             * We do not use Statement.executeUpdate() here because it could prevent users from
             * executing a stored procedure. Executing a stored procedure in Postgres (and possibly
             * other databases) is done via SELECT myprocedure(), which breaks executeUpdate() since
             * it returns a result, even if the procedure itself returns void.
             */
            statement.execute();
            int numRows = statement.getUpdateCount();
            String message;

            if (numRows == -1) {
                message = "Database write success";
            } else {
                message = "Database write success, " + numRows + " rows updated";
            }

            return new Response(Status.SENT, message);
        } catch (SQLException e) {
            if (!JdbcUtils.isValidConnection(connection)) {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    throw new DatabaseDispatcherException("Failed to close an invalid database connection", e1);
                }
            }

            try {
                if (retryOnConnectionFailure && connection.isClosed()) {
                    // retry sending on a new connection and pass retryOnConnectionFailure = false this time, so that we only retry once
                    return send(connectorProperties, false);
                }
            } catch (SQLException e1) {
                // if connection.isClosed() threw a SQLException, ignore it and refer to the original exception
            }

            throw new DatabaseDispatcherException("Database write failed", e);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Get a database connection based on the given connector properties.
     */
    private Connection getConnection(DatabaseDispatcherProperties connectorProperties) throws SQLException {
        /*
         * If we have an existing connection pool and it is based on the same
         * driver/username/password/url that is set in the given connector properties, then
         * re-use the pool. Otherwise, close it and create a new pool since the connection
         * settings have changed.
         */

        if (dataSource != null && !dataSource.isClosed()) {
            if (connectorProperties.getDriver().equals(dataSource.getDriverClassName()) && connectorProperties.getUsername().equals(dataSource.getUsername()) && connectorProperties.getPassword().equals(dataSource.getPassword()) && connectorProperties.getUrl().equals(dataSource.getUrl())) {
                Connection connection = dataSource.getConnection();
                connection.setAutoCommit(true);
                return connection;
            }

            try {
                dataSource.close();
            } catch (SQLException e) {
            }
        }

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(connectorProperties.getDriver());
        dataSource.setUsername(connectorProperties.getUsername());
        dataSource.setPassword(connectorProperties.getPassword());
        dataSource.setUrl(connectorProperties.getUrl());

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }
}
