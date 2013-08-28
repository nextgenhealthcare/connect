/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.sun.rowset.CachedRowSetImpl;

/**
 * Provides the ability to run SQL queries again the database connection object
 * instantiated using DatabaseConnectionFactory.
 */
public class DatabaseConnection {
    private Logger logger = Logger.getLogger(this.getClass());
    private Connection connection;
    private String address;

    /**
     * Instantiates a new database connection with the given server address.
     * 
     * @param address
     *            - The server address to connect to.
     * @throws SQLException
     */
    public DatabaseConnection(String address) throws SQLException {
        logger.debug("creating new database connection: address=" + address);
        this.address = address;
        connection = DriverManager.getConnection(address);
    }

    /**
     * Instantiates a new database connection with the given server address and
     * connection arguments.
     * 
     * @param address
     *            - The server address to connect to.
     * @param info
     *            - A Properties object containing all applicable connection
     *            arguments.
     * @throws SQLException
     */
    public DatabaseConnection(String address, Properties info) throws SQLException {
        logger.debug("creating new database connection: address=" + address + ", " + info);
        this.address = address;
        connection = DriverManager.getConnection(address, info);
    }

    /**
     * Returns the server address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Executes a query on the database and returns a CachedRowSet.
     * 
     * @param expression
     *            - The query expression to be executed.
     * @return The result of the query, as a CachedRowSet.
     * @throws SQLException
     */
    public CachedRowSet executeCachedQuery(String expression) throws SQLException {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            logger.debug("executing query:\n" + expression);
            ResultSet result = statement.executeQuery(expression);
            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(result);
            DbUtils.closeQuietly(result);
            return crs;
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Executes an INSERT/UPDATE on the database and returns the row count.
     * 
     * @param expression
     *            - The statement to be executed.
     * @return A count of the number of updated rows.
     * @throws SQLException
     */
    public int executeUpdate(String expression) throws SQLException {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            logger.debug("executing update:\n" + expression);

            if (statement.execute(expression)) {
                return -1;
            } else {
                return statement.getUpdateCount();
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Executes a prepared INSERT/UPDATE statement on the database and returns
     * the row count.
     * 
     * @param expression
     *            - The prepared statement to be executed.
     * @param parameters
     *            - The parameters for the prepared statement.
     * @return A count of the number of updated rows.
     * @throws SQLException
     */
    public int executeUpdate(String expression, List<Object> parameters) throws SQLException {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(expression);
            logger.debug("executing prepared statement:\n" + expression);

            ListIterator<Object> iterator = parameters.listIterator();

            while (iterator.hasNext()) {
                int index = iterator.nextIndex() + 1;
                Object value = iterator.next();
                logger.debug("adding parameter: index=" + index + ", value=" + value);
                statement.setObject(index, value);
            }

            if (statement.execute()) {
                return -1;
            } else {
                return statement.getUpdateCount();
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Executes a prepared query on the database and returns a CachedRowSet.
     * 
     * @param expression
     *            - The prepared statement to be executed.
     * @param parameters
     *            - The parameters for the prepared statement.
     * @return The result of the query, as a CachedRowSet.
     * @throws SQLException
     */
    public CachedRowSet executeCachedQuery(String expression, List<Object> parameters) throws SQLException {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(expression);
            logger.debug("executing prepared statement:\n" + expression);

            ListIterator<Object> iterator = parameters.listIterator();

            while (iterator.hasNext()) {
                int index = iterator.nextIndex() + 1;
                Object value = iterator.next();
                logger.debug("adding parameter: index=" + index + ", value=" + value);
                statement.setObject(index, value);
            }

            ResultSet result = statement.executeQuery();
            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(result);
            DbUtils.closeQuietly(result);
            return crs;
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            DbUtils.close(connection);
        } catch (SQLException e) {
            logger.warn(e);
        }
    }

    /**
     * Sets this connection's auto-commit mode to the given state.
     * 
     * @param autoCommit
     *            - The value (true or false) to set the connection's
     *            auto-commit mode to.
     * @throws SQLException
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    /**
     * Undoes all changes made in the current transaction and releases any
     * database locks currently held by this Connection object.
     * 
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Makes all changes made since the previous commit/rollback permanent and
     * releases any database locks currently held by this DatabaseConnection
     * object.
     * 
     * @throws SQLException
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Executes an INSERT/UPDATE statement on the database and returns a
     * CachedRowSet containing any generated keys.
     * 
     * @param expression
     *            - The statement to be executed.
     * @return A CachedRowSet containing any generated keys.
     * @throws SQLException
     */
    public CachedRowSet executeUpdateAndGetGeneratedKeys(String expression) throws SQLException {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            logger.debug("executing update:\n" + expression);
            statement.executeUpdate(expression, Statement.RETURN_GENERATED_KEYS);
            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(statement.getGeneratedKeys());
            return crs;
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Executes a prepared INSERT/UPDATE statement on the database and returns a
     * CachedRowSet containing any generated keys.
     * 
     * @param expression
     *            - The prepared statement to be executed.
     * @param parameters
     *            - The parameters for the prepared statement.
     * @return A CachedRowSet containing any generated keys.
     * @throws SQLException
     */
    public CachedRowSet executeUpdateAndGetGeneratedKeys(String expression, List<Object> parameters) throws SQLException {
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(expression, Statement.RETURN_GENERATED_KEYS);
            logger.debug("executing prepared statement:\n" + expression);

            ListIterator<Object> iterator = parameters.listIterator();

            while (iterator.hasNext()) {
                int index = iterator.nextIndex() + 1;
                Object value = iterator.next();
                logger.debug("adding parameter: index=" + index + ", value=" + value);
                statement.setObject(index, value);
            }

            statement.executeUpdate();
            CachedRowSetImpl crs = new CachedRowSetImpl();
            crs.populate(statement.getGeneratedKeys());
            return crs;
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Returns the database connection (java.sql.Connection) this class is
     * using.
     */
    public Connection getConnection() {
        return this.connection;
    }
}