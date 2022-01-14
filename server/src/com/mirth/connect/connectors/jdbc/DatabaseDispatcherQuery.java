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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DatabaseDispatcherQuery implements DatabaseDispatcherDelegate {
    private final static long MAX_CONNECTION_IDLE_TIME_NS = 300_000_000_000L;

    private DatabaseDispatcher connector;
    private Map<Long, SimpleDataSource> dataSources = new ConcurrentHashMap<>();
    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private CustomDriver customDriver;
    private Logger logger = Logger.getLogger(getClass());
    private volatile String contextFactoryId;

    public DatabaseDispatcherQuery(DatabaseDispatcher connector) {
        this.connector = connector;
    }

    @Override
    public void deploy() throws ConnectorTaskException {
        MirthContextFactory contextFactory;
        try {
            contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());
            contextFactoryId = contextFactory.getId();
        } catch (Exception e) {
            throw new ConnectorTaskException("Error retrieving context factory.", e);
        }

        try {
            initDriver(contextFactory);
        } catch (Exception e) {
            throw new ConnectorTaskException(e);
        }
    }

    @Override
    public void undeploy() throws ConnectorTaskException {}

    @Override
    public void start() throws ConnectorTaskException {}

    @Override
    public void stop() throws ConnectorTaskException {
        Throwable firstThrowable = null;

        for (SimpleDataSource dataSource : dataSources.values()) {
            try {
                dataSource.closeConnection();
            } catch (Throwable t) {
                if (firstThrowable == null) {
                    firstThrowable = t;
                }
            }
        }

        if (firstThrowable != null) {
            throw new ConnectorTaskException("Failed to close one or more connections.", firstThrowable);
        }

        dataSources.clear();
    }

    @Override
    public void halt() throws ConnectorTaskException {
        stop();
    }

    @Override
    public Response send(DatabaseDispatcherProperties connectorProperties, ConnectorMessage connectorMessage) throws DatabaseDispatcherException {
        long dispatcherId = connectorMessage.getDispatcherId();
        SimpleDataSource dataSource = dataSources.get(dispatcherId);

        if (dataSource == null) {
            dataSource = new SimpleDataSource();
            dataSources.put(dispatcherId, dataSource);
        }

        PreparedStatement statement = null;

        try {
            Connection connection = dataSource.getConnection(connectorProperties);
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
            String responseData = null;
            String responseMessageStatus = null;

            if (numRows == -1) {
                responseMessageStatus = "Database write success";
            } else {
                responseMessageStatus = "Database write success, " + numRows + " rows updated";
            }

            return new Response(Status.SENT, responseData, responseMessageStatus);
        } catch (Exception e) {
            throw new DatabaseDispatcherException("Failed to write to database", e);
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private void initDriver(MirthContextFactory contextFactory) throws Exception {
        customDriver = null;
        DatabaseDispatcherProperties props = (DatabaseDispatcherProperties) connector.getConnectorProperties();

        try {
            ClassLoader isolatedClassLoader = contextFactory.getIsolatedClassLoader();
            if (isolatedClassLoader != null) {
                customDriver = new CustomDriver(isolatedClassLoader, props.getDriver());
                logger.debug("Custom driver created: " + customDriver.toString() + ", Version " + customDriver.getMajorVersion() + "." + customDriver.getMinorVersion());
            } else {
                logger.debug("Custom classloader is not being used, defaulting to DriverManager.");
            }
        } catch (Exception e) {
            logger.debug("Error creating custom driver, defaulting to DriverManager.", e);
        }

        // If a custom driver was not created, use the default one
        if (customDriver == null) {
            Class.forName(props.getDriver());
        }
    }

    private boolean checkContextFactory() throws Exception {
        boolean contextFactoryChanged = false;
        MirthContextFactory contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());

        if (!contextFactoryId.equals(contextFactory.getId())) {
            contextFactoryChanged = true;

            synchronized (this) {
                contextFactory = contextFactoryController.getContextFactory(connector.getResourceIds());

                if (!contextFactoryId.equals(contextFactory.getId())) {
                    initDriver(contextFactory);
                    contextFactoryId = contextFactory.getId();
                }
            }
        }

        return contextFactoryChanged;
    }

    private class SimpleDataSource {
        private Connection connection;
        private String username;
        private String password;
        private String url;
        private long lastAccessTime;

        // TODO cache the PreparedStatement also

        public Connection getConnection(DatabaseDispatcherProperties properties) throws Exception {
            boolean contextFactoryChanged = checkContextFactory();

            /*
             * If a connection hasn't been initialized yet, or if the connection is stale, or if the
             * connection url/username/password have changed, then close the old connection (if
             * applicable) and create a new one.
             */
            if (contextFactoryChanged || connection == null || connection.isClosed() || lastAccessTime < (System.nanoTime() - MAX_CONNECTION_IDLE_TIME_NS) || !properties.getUsername().equals(username) || !properties.getPassword().equals(password) || !properties.getUrl().equals(url)) {
                closeConnection();

                logger.debug("Creating connection to " + properties.getUrl());
                if (customDriver != null) {
                    connection = customDriver.connect(properties.getUrl(), properties.getUsername(), properties.getPassword());
                } else {
                    connection = DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
                }
                connection.setAutoCommit(true);

                username = properties.getUsername();
                password = properties.getPassword();
                url = properties.getUrl();
            }

            lastAccessTime = System.nanoTime();
            return connection;
        }

        public void closeConnection() throws SQLException {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
}
