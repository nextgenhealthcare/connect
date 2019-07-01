/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.sql.Connection;
import java.sql.Driver;
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

/**
 * Provides the ability to run SQL queries again the database connection object instantiated using
 * DatabaseConnectionFactory.
 */
public class DatabaseConnection {
    private Logger logger = Logger.getLogger(this.getClass());
    private Connection connection;
    private String address;

    /**
     * Instantiates a new database connection with the given server address.
     * 
     * @param address
     *            The server address to connect to.
     * @throws SQLException
     *             If a database access error occurs.
     */
    public DatabaseConnection(String address) throws SQLException {
        logger.debug("creating new database connection: address=" + address);
        this.address = address;
        connection = DriverManager.getConnection(address);
    }

    /**
     * Instantiates a new database connection with the given server address and connection
     * arguments.
     * 
     * @param address
     *            The server address to connect to.
     * @param info
     *            A Properties object containing all applicable connection arguments.
     * @throws SQLException
     *             If a database access error occurs.
     */
    public DatabaseConnection(String address, Properties info) throws SQLException {
        logger.debug("creating new database connection: address=" + address + ", " + info);
        this.address = address;
        connection = DriverManager.getConnection(address, info);
    }

    /**
     * Instantiates a new database connection with the given driver instance and server address.
     * 
     * @param driver
     *            The explicit driver instance to connect with.
     * @param address
     *            The server address to connect to.
     * @throws SQLException
     *             If a database access error occurs.
     */
    public DatabaseConnection(Driver driver, String address) throws SQLException {
        logger.debug("creating new database connection: address=" + address);
        this.address = address;
        connection = driver.connect(address, new Properties());
    }

    /**
     * Instantiates a new database connection with the given driver instance, server address, and
     * connection arguments.
     * 
     * @param driver
     *            The explicit driver instance to connect with.
     * @param address
     *            The server address to connect to.
     * @param info
     *            A Properties object containing all applicable connection arguments.
     * @throws SQLException
     *             If a database access error occurs.
     */
    public DatabaseConnection(Driver driver, String address, Properties info) throws SQLException {
        logger.debug("creating new database connection: address=" + address + ", " + info);
        this.address = address;
        connection = driver.connect(address, info);
    }

    /**
     * Returns the server address.
     * 
     * @return The server address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Executes a query on the database and returns a CachedRowSet.
     * 
     * @param expression
     *            The query expression to be executed.
     * @return The result of the query, as a CachedRowSet.
     * @throws SQLException
     *             If a database access error occurs.
     */
    public CachedRowSet executeCachedQuery(String expression) throws SQLException {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            logger.debug("executing query:\n" + expression);
            ResultSet result = statement.executeQuery(expression);
            CachedRowSet crs = new MirthCachedRowSet();
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
     *            The statement to be executed.
     * @return A count of the number of updated rows.
     * @throws SQLException
     *             If a database access error occurs.
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
     * Executes a prepared INSERT/UPDATE statement on the database and returns the row count.
     * 
     * @param expression
     *            The prepared statement to be executed.
     * @param parameters
     *            The parameters for the prepared statement.
     * @return A count of the number of updated rows.
     * @throws SQLException
     *             If a database access error occurs.
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
     *            The prepared statement to be executed.
     * @param parameters
     *            The parameters for the prepared statement.
     * @return The result of the query, as a CachedRowSet.
     * @throws SQLException
     *             If a database access error occurs.
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
            CachedRowSet crs = new MirthCachedRowSet();
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
     *            The value (true or false) to set the connection's auto-commit mode to.
     * @throws SQLException
     *             If a database access error occurs.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    /**
     * Undoes all changes made in the current transaction and releases any database locks currently
     * held by this Connection object.
     * 
     * @throws SQLException
     *             If a database access error occurs.
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Makes all changes made since the previous commit/rollback permanent and releases any database
     * locks currently held by this DatabaseConnection object.
     * 
     * @throws SQLException
     *             If a database access error occurs.
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Executes an INSERT/UPDATE statement on the database and returns a CachedRowSet containing any
     * generated keys.
     * 
     * @param expression
     *            The statement to be executed.
     * @return A CachedRowSet containing any generated keys.
     * @throws SQLException
     *             If a database access error occurs.
     */
    public CachedRowSet executeUpdateAndGetGeneratedKeys(String expression) throws SQLException {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            logger.debug("executing update:\n" + expression);
            statement.executeUpdate(expression, Statement.RETURN_GENERATED_KEYS);
            CachedRowSet crs = new MirthCachedRowSet();
            crs.populate(statement.getGeneratedKeys());
            return crs;
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Executes a prepared INSERT/UPDATE statement on the database and returns a CachedRowSet
     * containing any generated keys.
     * 
     * @param expression
     *            The prepared statement to be executed.
     * @param parameters
     *            The parameters for the prepared statement.
     * @return A CachedRowSet containing any generated keys.
     * @throws SQLException
     *             If a database access error occurs.
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
            CachedRowSet crs = new MirthCachedRowSet();
            crs.populate(statement.getGeneratedKeys());
            return crs;
        } catch (SQLException e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    /**
     * Returns the database connection (java.sql.Connection) this class is using.
     * 
     * @return The underlying java.sql.Connection object.
     */
    public Connection getConnection() {
        return this.connection;
    }
}